import argparse
import os
from pathlib import Path

import cv2
import torch
import numpy as np
from tqdm import tqdm
from utils.utils import load_detector, load_classificator, open_mapping, extract_crops
import pandas as pd
from itertools import repeat
from PIL import Image
from PIL.ExifTags import TAGS
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters, java_import
from py4j.java_collections import ListConverter
from LACC import LACC
from LACE import LACE

def get_exif_date(image_path):
    try:
        image = Image.open(image_path)
        exif_data = image._getexif()
        if exif_data:
            for tag, value in exif_data.items():
                tag_name = TAGS.get(tag, tag)
                if tag_name == 'DateTimeOriginal':
                    return value
        return None
    except Exception as e:
        return None

def process_image(img, beta=2, use_processing=True):
    if use_processing:
        img, _ = LACC(img / 255.0)
        img = LACE(img * 255, beta)
    return img


def main(program_path, dataset_path):
    # Задание конфигурации вручную
    main_config = {
        "src_dir": dataset_path,
        "mapping": f"{program_path}/class_map_animal.txt",
        "device": "cuda",
        "dst_save_table": f"{program_path}/data/table.csv",
        "detector": {
            "weights": f"{program_path}/weights/detection/yolov8n.pt",
            "batch_size": 1,
            "iou": 0.45,
            "conf": 0.4,
            "imgsz": [640, 640]
        },
        "classificator": {
            "weights": f"{program_path}/weights/classification/efficientnet_b0.pt",
            "batch_size": 8,
            "imgsz": [288, 288]
        }
    }

    device = main_config["device"]

    # Load imgs from source dir
    # pathes_to_imgs = [i for i in Path(main_config["src_dir"]).glob("*") if i.suffix.lower() in [".jpeg", ".jpg", ".png"]]
    pathes_to_imgs = [i for i in Path(main_config["src_dir"]).rglob("*") if i.suffix.lower() in [".jpeg", ".jpg", ".png"]]

    # Load mapping for classification task
    try:
        mapping = open_mapping(path_mapping=main_config["mapping"])
    except Exception as e:
        return []

    try:
        # Separate main config
        detector_config = main_config["detector"]
        classificator_config = main_config["classificator"]
    except Exception as e:
        return []

    # Load models
    detector = load_detector(detector_config).to(device)
    classificator = load_classificator(classificator_config).to(device)

    # Inference
    if len(pathes_to_imgs):
        list_predictions = []
        num_packages_det = np.ceil(len(pathes_to_imgs) / detector_config["batch_size"]).astype(np.int32)
        with torch.no_grad():
            for i in tqdm(range(num_packages_det), colour="green"):
                # Inference detector
                batch_images_det = pathes_to_imgs[detector_config["batch_size"] * i: detector_config["batch_size"] * (1 + i)]

                # Process each image
                process_batch_images_det = [process_image(cv2.imread(str(img)), use_processing=False) for img in
                                    batch_images_det]

                results_det = detector(process_batch_images_det, iou=detector_config["iou"], conf=detector_config["conf"], imgsz=detector_config["imgsz"], verbose=False, device=device)

                if len(results_det) > 0:
                    # Extract crop by bboxes
                    dict_crops = extract_crops(results_det, config=classificator_config)

                    # Inference classificator
                    for img_name, batch_images_cls in dict_crops.items():
                        num_packages_cls = np.ceil(len(batch_images_cls) / classificator_config["batch_size"]).astype(np.int32)
                        for j in range(num_packages_cls):
                            batch_images_cls = batch_images_cls[classificator_config["batch_size"] * j: classificator_config["batch_size"] * (1 + j)]
                            logits = classificator(batch_images_cls.to(device))
                            probabilities = torch.nn.functional.softmax(logits, dim=1)
                            top_p, top_class_idx = probabilities.topk(1, dim=1)

                            # Locate torch Tensors to cpu and convert to numpy
                            top_p = top_p.cpu().numpy().ravel()
                            top_class_idx = top_class_idx.cpu().numpy().ravel()

                            class_names = [mapping[top_class_idx[idx]] for idx, _ in enumerate(batch_images_cls)]

                            date_taken = get_exif_date(batch_images_det[0])
                            last_folder = batch_images_det[0].parent.name
                            name = batch_images_det[0].name

                            # is_writen = cv2.imwrite(f'processed_images/{name}', process_batch_images_det[0])
                            # print(is_writen, f'processed_images/{name}'.lower())

                            list_predictions.extend([[name, cls, prob, date_taken, last_folder] for _, cls, prob in zip(repeat(img_name, len(class_names)), class_names, top_p)])



        # Create Dataframe with predictions
        table = pd.DataFrame(list_predictions,
                             columns=["image_name", "class_name", "confidence", "date", "folder_name"])

        # Aggregation functions
        agg_functions = {
            'class_name': ['count'],
            "confidence": ["mean"],
            "date": ['first'],
            "folder_name": ['first'],
        }

        # Group by and aggregate
        groupped = table.groupby(['image_name', "class_name"]).agg(agg_functions)
        groupped.columns = ['_'.join(col).strip() for col in groupped.columns.values]

        img_names = groupped.index.get_level_values("image_name").unique()

        final_res = []

        for img_name in img_names:
            groupped_per_img = groupped.query(f"image_name == '{img_name}'")
            max_num_objects = groupped_per_img["class_name_count"].max()
            statistic_by_max_objects = groupped_per_img[groupped_per_img["class_name_count"] == max_num_objects]

            if len(statistic_by_max_objects) > 1:
                statistic_by_max_mean_conf = statistic_by_max_objects.loc[
                    [statistic_by_max_objects["confidence_mean"].idxmax()]]
                final_res.extend(statistic_by_max_mean_conf.reset_index().values)
            else:
                final_res.extend(statistic_by_max_objects.reset_index().values)

        # Create final DataFrame
        final_table = pd.DataFrame(final_res,
                                   columns=["image_name", "class_name", "class_name_count", "confidence_mean",
                                            "date_first", "folder_name_first"])

        # Rename columns to the desired order
        final_table.rename(columns={
            "class_name_count": "count",
            "confidence_mean": "confidence",
            "date_first": "date",
            "folder_name_first": "folder_name"
        }, inplace=True)

        # Reorder columns
        final_table = final_table[["image_name", "class_name", "date", "folder_name", "confidence", "count"]]

        final_table.to_csv(f"{program_path}/table_final.csv", index=False)

        # Convert final_table to list of lists to send to Java
        final_res_list = [",".join(map(str, row)) for row in final_table.values.tolist()]

        return final_res_list
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Process some parameters.')
    parser.add_argument('program_path', type=str, help='Path to the program directory')
    parser.add_argument('dataset_path', type=str, help='Path to the dataset directory')

    args = parser.parse_args()

    gateway = None
    try:
        # Connect to Java Gateway
        gateway = JavaGateway(gateway_parameters=GatewayParameters(port=25333),
                              callback_server_parameters=CallbackServerParameters(port=25334))

        # Run main function and get final_res
        final_res = main(args.program_path, args.dataset_path)

        for line in final_res:
            print(line)

        # Импорт Java List в пространство имен JVM
        java_import(gateway.jvm, 'java.util.*')

        # Конвертация Python списка в Java список
        java_list = ListConverter().convert(final_res, gateway._gateway_client)

        # Передача списка в Java
        gateway.entry_point.receiveResults(java_list)

    finally:
        # Остановка Gateway
        if gateway is not None:
            gateway.shutdown()
