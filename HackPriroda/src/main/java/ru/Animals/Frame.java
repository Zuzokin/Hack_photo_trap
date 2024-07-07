package ru.Animals;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import py4j.GatewayServer;

public class Frame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private boolean flagDatasetPath = false;
	private boolean flagCorePath = false;
	private boolean flagSaveFolderPath = false;
	private JTable table;
	public List<String> results = new ArrayList<String>();
	private int startNum;
	private String startDateAndTime, nextDateAndTime, currentDateAndTime, startClass, nextClass, currentClass;
	private LocalDateTime startDateTime, nextDateTime, currentDateTime;
	ArrayList<ArrayList<String>> dataLines = new ArrayList<>();
	ArrayList<ArrayList<String>> resultsForCSV = new ArrayList<>();
	ArrayList<ArrayList<String>> resultList = new ArrayList<>();
	ArrayList<ArrayList<String>> imageList = new ArrayList<>();

	public static void main(String[] args) {

		// FlatGitHubDarkContrastIJTheme.setup();
		FlatCyanLightIJTheme.setup();

		// UIManager.put("Button.arc", 999);
		UIManager.put("Component.arrowType", "chevron");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame frame = new Frame();
					GatewayServer server = new GatewayServer(frame, 25333); // Р�Р·РјРµРЅРёС‚Рµ РїРѕСЂС‚ РЅР° 25335
					server.start();
					// System.out.println("Установлено соединение 25333");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Frame() {
		setResizable(false);
		setTitle("Обработка изображений с фотоловушек");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 980);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(15, 45, 450, 209);
		panel.setBorder(BorderFactory.createTitledBorder("Входные данные"));
		contentPane.add(panel);

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(15, 662, 946, 256);
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		panel.setLayout(null);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(15, 259, 946, 398);
		panel_1.setBorder(BorderFactory.createTitledBorder("Таблица результатов"));
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		JScrollPane scrollPane_1_1 = new JScrollPane();
		scrollPane_1_1.setBounds(21, 22, 902, 355);
		panel_1.add(scrollPane_1_1);

		DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] { "\u2116",
				"\u041D\u0430\u0437\u0432\u0430\u043D\u0438\u044F \u0444\u0430\u0439\u043B\u043E\u0432",
				"\u041A\u043B\u0430\u0441\u0441", "\u0414\u0430\u0442\u0430 \u043D\u0430\u0447\u0430\u043B\u0430",
				"\u0414\u0430\u0442\u0430 \u043A\u043E\u043D\u0446\u0430",
				"\u041C\u0430\u043A\u0441\u0438\u043C\u0430\u043B\u044C\u043D\u043E\u0435 \u0447\u0438\u0441\u043B\u043E \u0436\u0438\u0432\u043E\u0442\u043D\u044B\u0445" });
		table = new JTable(model);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(5);
		columnModel.getColumn(1).setPreferredWidth(180);
		columnModel.getColumn(2).setPreferredWidth(60);
		columnModel.getColumn(3).setPreferredWidth(110);
		columnModel.getColumn(4).setPreferredWidth(110);
		columnModel.getColumn(5).setPreferredWidth(150);
		table.setFocusable(false);

		scrollPane_1_1.setViewportView(table);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(20, 23, 902, 212);
		panel_2.add(scrollPane_1);

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFocusable(false);
		scrollPane_1.setViewportView(textArea);

		JLabel lblNewLabel_8 = new JLabel("Выберите путь к исходной папке с фотографиями: ");
		lblNewLabel_8.setBounds(20, 31, 299, 13);
		panel.add(lblNewLabel_8);

		textField = new JTextField();
		textField.setBounds(20, 54, 285, 30);
		textField.setEnabled(false);
		panel.add(textField);

		JButton btnNewButton = new JButton("Выбор папки");
		btnNewButton.setBounds(315, 54, 115, 30);
		btnNewButton.setFocusable(false);
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileopen = new JFileChooser();
				panel.add(fileopen);
				fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int ret = fileopen.showDialog(null, "Выбрать...");
				if (ret == JFileChooser.APPROVE_OPTION) {
					File selectedFolder = fileopen.getSelectedFile();
					String absolutePath = selectedFolder.getAbsolutePath();
					textField.setText(absolutePath);
					flagDatasetPath = true;
				} else {
					textArea.append("[Ошибка] Папка с датасетом не была выбрана.\n");
				}
			}
		});
		panel.add(btnNewButton);

		JLabel lblNewLabel_8_1 = new JLabel("Выберите путь к папке с нейросетевой моделью: ");
		lblNewLabel_8_1.setBounds(20, 115, 299, 13);
		panel.add(lblNewLabel_8_1);

		textField_1 = new JTextField();
		textField_1.setEnabled(false);
		textField_1.setBounds(20, 138, 285, 30);
		panel.add(textField_1);

		JButton btnNewButton_1 = new JButton("Выбор папки");
		btnNewButton_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileopen = new JFileChooser();
				panel.add(fileopen);
				fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int ret = fileopen.showDialog(null, "Выбрать...");
				if (ret == JFileChooser.APPROVE_OPTION) {
					File selectedFolder = fileopen.getSelectedFile();
					String absolutePath = selectedFolder.getAbsolutePath();
					textField_1.setText(absolutePath);
					flagCorePath = true;
				} else {
					textArea.append("[Ошибка] Папка с нейросетевой моделью не была выбрана.\n");
				}
			}
		});
		btnNewButton_1.setFocusable(false);
		btnNewButton_1.setBounds(315, 138, 115, 30);
		panel.add(btnNewButton_1);
		panel_2.setBorder(BorderFactory.createTitledBorder("Консоль программы"));

		JPanel panel_3 = new JPanel();
		panel_3.setBounds(475, 45, 486, 209);
		contentPane.add(panel_3);
		panel_3.setBorder(BorderFactory.createTitledBorder("Панель управления"));
		panel_3.setLayout(null);

		JLabel lblNewLabel_8_2 = new JLabel("Папка для сохранения CSV-файла: ");
		lblNewLabel_8_2.setBounds(20, 31, 320, 13);
		panel_3.add(lblNewLabel_8_2);

		textField_2 = new JTextField();
		textField_2.setEnabled(false);
		textField_2.setBounds(20, 54, 195, 30);
		panel_3.add(textField_2);

		textField_3 = new JTextField();
		textField_3.setFocusable(true);
		textField_3.setBounds(20, 138, 195, 30);
		panel_3.add(textField_3);

		JButton btnNewButton_2 = new JButton("Выбор папки");
		btnNewButton_2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileopen = new JFileChooser();
				panel.add(fileopen);
				fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int ret = fileopen.showDialog(null, "Выбрать...");
				if (ret == JFileChooser.APPROVE_OPTION) {
					File selectedFolder = fileopen.getSelectedFile();
					String absolutePath = selectedFolder.getAbsolutePath();
					textField_2.setText(absolutePath);
					flagSaveFolderPath = true;
				} else {
					textArea.append("[Ошибка] Папка для сохранения отчёта не выбрана.\n");
				}
			}
		});
		btnNewButton_2.setFocusable(false);
		btnNewButton_2.setBounds(225, 53, 115, 30);
		panel_3.add(btnNewButton_2);

		// Запуск программы
		JButton btnNewButton_2_1 = new JButton("Запустить");
		btnNewButton_2_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (flagDatasetPath && flagCorePath) {
					textArea.append("Успешно установлено соединение с сервером на порту 25333\n");
					String resourceFilePath = textField_1.getText() + "/main.py";
					String venvPath = textField_1.getText() + "/venv/Scripts/activate";
					String pyPath = textField_1.getText() + "/venv/Scripts/python.exe";

					try {
						URL pythonFile = getClass().getResource("/pythonProject/main.py");
						URL venv = getClass().getResource("/pythonProject/venv/Scripts/activate");
						URL py = getClass().getResource("/pythonProject/venv/Scripts/python.exe");
						if (pythonFile != null) {

							URI resourceUri = pythonFile.toURI();
							URI venvURI = venv.toURI();
							URI pyURI = py.toURI();

							File resourceFile = new File(resourceUri);
							File venvFile = new File(venvURI);
							File pyFile = new File(pyURI);

							resourceFilePath = resourceFile.getAbsolutePath();
							venvPath = venvFile.getAbsolutePath();
							pyPath = pyFile.getAbsolutePath();
						}
					} catch (URISyntaxException exp) {
						exp.printStackTrace();
					}

					String resFilePath = resourceFilePath.replace("\\", "/");
					String venvActivate = venvPath.replace("\\", "/");
					String coreDirPath = textField_1.getText().replace("\\", "/");
					String pyPathFile = pyPath.replace("\\", "/");
					String datasetPath = textField.getText().replace("\\", "/");

					/*
					 * String resFilePath = "C:/Users/Александр/Desktop/pythonProject/main.py";
					 * String venvActivate =
					 * "C:/Users/Александр/Desktop/pythonProject/venv/Scripts/activate"; String
					 * coreDirPath = "C:/Users/Александр/Desktop/pythonProject/"; String pyPathFile
					 * = "C:/Users/Александр/Desktop/pythonProject/venv/Scripts/python.exe"; String
					 * datasetPath = "C:/Users/Александр/Desktop/data";
					 */
					/*
					 * System.out.println(resFilePath); System.out.println(venvActivate);
					 * System.out.println(coreDirPath); System.out.println(pyPathFile);
					 * System.out.println(datasetPath);
					 */
					textArea.append("Успешная инициализация параметров. Старт обработки изображений...\n");
					try {

						String batchFilePath = "call " + venvActivate + " && " + pyPathFile + " " + resFilePath + " \""
								+ coreDirPath + "\" \"" + datasetPath + "\"";
						System.out.println(batchFilePath);

						ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", batchFilePath);

//			            ProcessBuilder pb = new ProcessBuilder("python", "C:/Users/Admin2/Desktop/animals/pythonProject/main.py");
//			            pb.redirectErrorStream(true);
						Process p = pb.start();

						BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							textArea.append("Обработано изображение: " + line + "\n");
							results.add(line);
							resultsForCSV.add(new ArrayList<String>(Arrays.asList(line.split(","))));
						}

						int exitCode = p.waitFor();

						textArea.append("Обработка изображений завершена с кодом: " + exitCode + "\n");
						p.destroy();
					} catch (Exception exep) {
						exep.printStackTrace();
					}
					LocalDateTime now = LocalDateTime.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy HH-mm-ss");
					String formattedDateTime = now.format(formatter);
					String pathToSave = textField_2.getText().replace("\\", "/") + "record_" + formattedDateTime
							+ ".csv";
					imageList = processCSV(resultsForCSV, pathToSave);
					for (int i = 0; i < resultList.size(); i++) {
						String img = String.join(", ", imageList.get(i));
						model.addRow(new Object[] { String.valueOf(i + 1), img, resultList.get(i).get(1),
								resultList.get(i).get(2), resultList.get(i).get(3), resultList.get(i).get(4) });
					}
				}
				results.clear();
			}
		});
		btnNewButton_2_1.setFocusable(false);
		btnNewButton_2_1.setBounds(350, 53, 115, 30);
		panel_3.add(btnNewButton_2_1);

		JButton btnNewButton_2_1_1 = new JButton("Найти");
		btnNewButton_2_1_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Получаем ключевое слово из текстового поля
				String keyword = textField_3.getText();

				// Устанавливаем кастомный рендерер ячеек с новым ключевым словом
				table.setDefaultRenderer(Object.class, new HighlightTableCellRenderer(keyword));
				table.repaint();
			}
		});
		btnNewButton_2_1_1.setFocusable(false);
		btnNewButton_2_1_1.setBounds(225, 137, 115, 30);
		panel_3.add(btnNewButton_2_1_1);

		JLabel lblNewLabel_8_2_1 = new JLabel("Поиск в таблице по ключевому слову: ");
		lblNewLabel_8_2_1.setBounds(20, 115, 320, 13);
		panel_3.add(lblNewLabel_8_2_1);

		JButton btnNewButton_2_1_1_1 = new JButton("Отмена");
		btnNewButton_2_1_1_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Устанавливаем стандартный рендерер ячеек
				table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
				table.repaint();
			}
		});

		// Загружаем изображения для кнопки
		ImageIcon sunIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sun.png")));
		ImageIcon moonIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/moon.png")));

		btnNewButton_2_1_1_1.setFocusable(false);
		btnNewButton_2_1_1_1.setBounds(350, 137, 115, 30);
		panel_3.add(btnNewButton_2_1_1_1);

		// Создаем кнопку для переключения темы с начальной иконкой
		// JButton switchThemeButton = new JButton("Switch Theme", moonIcon);
		JButton btnNewButton_2_1_1_1_1 = new JButton("", sunIcon);
		btnNewButton_2_1_1_1_1.setBounds(15, 5, 36, 30);
		contentPane.add(btnNewButton_2_1_1_1_1);
		btnNewButton_2_1_1_1_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Получаем текущую тему
				LookAndFeel currentLaf = UIManager.getLookAndFeel();

				// Проверяем текущую тему и переключаем на другую
				if (currentLaf instanceof FlatCyanLightIJTheme) {
					FlatNordIJTheme.setup();
					btnNewButton_2_1_1_1_1.setIcon(moonIcon);
				} else if (currentLaf instanceof FlatNordIJTheme) {
					FlatCyanLightIJTheme.setup();
					btnNewButton_2_1_1_1_1.setIcon(sunIcon);
				}

				// Обновляем LookAndFeel для всех открытых окон
				FlatLaf.updateUI();
			}
		});
		btnNewButton_2_1_1_1_1.setFocusable(false);
	}

	public void receiveResults(List<String> finalRes) {
		for (String row : finalRes) {
			System.out.println(row);
		}
	}

	public final ArrayList<ArrayList<String>> processCSV(ArrayList<ArrayList<String>> files, String path) {

		Duration duration;
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime firstDT, middleDT;
		String firstClass, middleClass;
		HashSet<String> filesNames = new HashSet<String>();
		ArrayList<ArrayList<String>> fNames = new ArrayList<>();
		for (int i = 1; i < files.size() - 1; i++) {
			firstDT = LocalDateTime.parse(files.get(i - 1).get(2), inputFormatter);
			middleDT = LocalDateTime.parse(files.get(i).get(2), inputFormatter);
			firstClass = files.get(i - 1).get(1);
			middleClass = files.get(i).get(1);
			if ((Duration.between(firstDT, middleDT).getSeconds() < 120)
					&& (!(firstClass.equalsIgnoreCase(middleClass)))) {
				if (Double.parseDouble(files.get(i - 1).get(4)) < Double.parseDouble(files.get(i).get(4))) {
					files.remove(i - 1);

				} else {
					files.remove(i);

				}
				i--;

			}
		}

		for (ArrayList<String> innerList : files) {
			filesNames.add(innerList.get(0));
		}

		try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(path)).withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
				.build()) {

			int i = 0;
			while (i < files.size()) {
				filesNames.clear();

				startNum = Integer.parseInt(files.get(i).get(5));

				startDateAndTime = files.get(i).get(2);

				startDateTime = LocalDateTime.parse(startDateAndTime, inputFormatter);

				startClass = files.get(i).get(1);

				currentDateTime = startDateTime;
				currentClass = startClass;
				currentDateAndTime = startDateAndTime;
				if (currentClass.equalsIgnoreCase("Empty")) {
					i++;
					continue;
				}
				filesNames.add(files.get(i).get(0));

				int j = i + 1;
				while (j < files.size()) {

					nextDateAndTime = files.get(j).get(2);

					nextDateTime = LocalDateTime.parse(nextDateAndTime, inputFormatter);
					startNum = Math.max(startNum, Integer.parseInt(files.get(j).get(5)));
					nextClass = files.get(j).get(1);
					duration = Duration.between(startDateTime, nextDateTime);

					// ArrayList<ArrayList<String>> resultList = new ArrayList<>();

					if (j == files.size() - 1) {
                        currentDateTime = nextDateTime;
                        currentClass = nextClass;
                        currentDateAndTime = nextDateAndTime;
                    }
					
					if (((duration.toMinutes() > 30) && (nextClass.equalsIgnoreCase("Empty")))
							|| ((!(startClass.equalsIgnoreCase(nextClass)) && !(nextClass.equalsIgnoreCase("Empty"))))
							|| (j == files.size() - 1)) {

//			if ((currentDateTime.plusMinutes(30).compareTo(nextDateTime) < 0) || (currentClass != nextClass))  {
						// разница больше 30 минут
						// закрываем запись
						String[] strList = new String[5];
						strList[0] = files.get(i).get(3); // номер папки
						strList[1] = files.get(i).get(1); // номер класса
						strList[2] = LocalDateTime.parse(startDateAndTime, inputFormatter).format(outputFormatter); // начало
																													// записи
						strList[3] = LocalDateTime.parse(currentDateAndTime, inputFormatter).format(outputFormatter); // конец
																														// записи
						strList[4] = startNum + ""; // количество животных (максимальное)

						resultList.add(new ArrayList<String>(Arrays.asList(strList)));
						writer.writeNext(strList);
						i = j - 1;
						break;

					}
					currentDateTime = nextDateTime;
					currentClass = nextClass;
					currentDateAndTime = nextDateAndTime;
					filesNames.add(files.get(j).get(0));
					j++;
				}
				fNames.add(new ArrayList<String>(filesNames));
				i++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fNames;
	}
}

class HighlightTableCellRenderer extends DefaultTableCellRenderer {
	private final String keyword;

	public HighlightTableCellRenderer(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// Проверяем, содержит ли строка ключевое слово
		boolean containsKeyword = false;
		for (int i = 0; i < table.getColumnCount(); i++) {
			Object cellValue = table.getValueAt(row, i);
			if (cellValue != null && cellValue.toString().contains(keyword)) {
				containsKeyword = true;
				break;
			}
		}

		// Если содержит, устанавливаем цвет фона
		if (containsKeyword) {
			c.setBackground(Color.GREEN.brighter());
		} else {
			c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
		}

		return c;
	}
}
