package me.dragonflyer.rltrading;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

public class UserInterface extends JFrame {
	private static final long serialVersionUID = 1L;
	JCheckBox exactmatch;
	JButton start;
	private String[] columnNames = new String[] { "Has", "Wants", "Steamname", "RLG Username", "Tradeurl", "Notes" };
	DefaultTableModel model;
	private JTable table;
	private ArrayList<HashMap<String, String>> ids;
	JProgressBar progress;
	Item item;

	UserInterface() {
		super("Rocket League Trading");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		setSize(726, 700);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(3);
		setLayout(null);
		getContentPane().setBackground(Color.WHITE);

		JLabel has = new JLabel("I have this item");
		has.setBounds(0, 0, 200, 20);
		add(has);
		JLabel hasitem = new JLabel("SELECT ITEM");
		hasitem.setBounds(0, 20, 200, 20);
		add(hasitem);
		JComboBox<String> hasitems = new JComboBox<String>();
		hasitems.setMaximumRowCount(30);
		hasitems.setBounds(0, 40, 200, 20);
		add(hasitems);
		JLabel hascertification = new JLabel("SELECT CERTIFICATION");
		hascertification.setBounds(200, 20, 200, 20);
		add(hascertification);
		JComboBox<String> hascertifications = new JComboBox<String>();
		hascertifications.setMaximumRowCount(30);
		hascertifications.setBounds(200, 40, 200, 20);
		add(hascertifications);
		JLabel haspaint = new JLabel("SELECT PAINT");
		haspaint.setBounds(400, 20, 200, 20);
		add(haspaint);
		JComboBox<String> haspaints = new JComboBox<String>();
		haspaints.setMaximumRowCount(30);
		haspaints.setBounds(400, 40, 200, 20);
		add(haspaints);
		JLabel wants = new JLabel("I want this item");
		wants.setBounds(0, 60, 200, 20);
		add(wants);
		JLabel wantsitem = new JLabel("SELECT ITEM");
		wantsitem.setBounds(0, 80, 200, 20);
		add(wantsitem);
		JComboBox<String> wantsitems = new JComboBox<String>();
		wantsitems.setMaximumRowCount(30);
		wantsitems.setBounds(0, 100, 200, 20);
		add(wantsitems);
		JLabel wantscertification = new JLabel("SELECT CERTIFICATION");
		wantscertification.setBounds(200, 80, 200, 20);
		add(wantscertification);
		JComboBox<String> wantscertifications = new JComboBox<String>();
		wantscertifications.setMaximumRowCount(30);
		wantscertifications.setBounds(200, 100, 200, 20);
		add(wantscertifications);
		JLabel wantspaint = new JLabel("SELECT PAINT");
		wantspaint.setBounds(400, 80, 200, 20);
		add(wantspaint);
		JComboBox<String> wantspaints = new JComboBox<String>();
		wantspaints.setMaximumRowCount(30);
		wantspaints.setBounds(400, 100, 200, 20);
		add(wantspaints);
		JLabel timeout = new JLabel("Timeout (s)");
		timeout.setBounds(620, 0, 100, 20);
		add(timeout);
		JSpinner timeoutspinner = new JSpinner(new SpinnerNumberModel(0, 0, 600, 1));
		timeoutspinner.setBounds(620, 20, 100, 20);
		add(timeoutspinner);
		JLabel maxthreads = new JLabel("Max Threads");
		maxthreads.setBounds(620, 40, 100, 20);
		add(maxthreads);
		JSpinner maxthreadsspinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
		maxthreadsspinner.setBounds(620, 60, 100, 20);
		add(maxthreadsspinner);
		exactmatch = new JCheckBox("Exact match");
		exactmatch.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (Main.offers != null) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (Main.currentMode == Main.Mode.HasWants)
							updateTable(Main.filter(Main.Mode.ExactHasWants));
						else if (Main.currentMode == Main.Mode.WantsHas)
							updateTable(Main.filter(Main.Mode.ExactWantsHas));
					} else {
						if (Main.currentMode == Main.Mode.ExactHasWants)
							updateTable(Main.filter(Main.Mode.HasWants));
						else if (Main.currentMode == Main.Mode.ExactWantsHas)
							updateTable(Main.filter(Main.Mode.WantsHas));
					}
				}
			}
		});
		exactmatch.setBounds(620, 80, 100, 20);
		add(exactmatch);
		progress = new JProgressBar();
		progress.setStringPainted(true);
		progress.setBounds(0, 130, 720, 20);
		progress.setVisible(false);
		add(progress);
		model = new DefaultTableModel(columnNames, 0);
		table = new JTable(model);
		table.setDefaultRenderer(String.class, new MultiLineTableCellRenderer());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == 1 && e.getClickCount() == 2) {
					int column = table.getSelectedColumn();
					if (column == 4)
						openWebpage("https://rocket-league.com/trade/"
								+ model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), column));
				}
			}
		});
		JScrollPane tablesp = new JScrollPane(table);
		tablesp.setBounds(0, 160, 720, 511);
		add(tablesp);
		start = new JButton("Start");
		start.setBounds(620, 100, 100, 20);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String url = "https://rocket-league.com/trading?filterItem=";
				String wantsurl = url + ids.get(0).get(wantsitems.getSelectedItem()) + "&filterCertification="
						+ ids.get(1).get(wantscertifications.getSelectedItem()) + "&filterPaint="
						+ ids.get(2).get(wantspaints.getSelectedItem())
						+ "&filterName=&filterPlatform=1&filterSearchType=1";
				int maxthreads = (int) maxthreadsspinner.getValue(), timeout = (int) timeoutspinner.getValue();
				if (!hasitems.getSelectedItem().equals("Any")) {
					String hasurl = url + ids.get(0).get(hasitems.getSelectedItem()) + "&filterCertification="
							+ ids.get(1).get(hascertifications.getSelectedItem()) + "&filterPaint="
							+ ids.get(2).get(haspaints.getSelectedItem())
							+ "&filterName=&filterPlatform=1&filterSearchType=2";
					if (!wantsitems.getSelectedItem().equals("Any")) {
						ArrayList<String> rawsourcehas = Main.getUrlSource(hasurl),
								rawsourcewants = Main.getUrlSource(wantsurl);
						int pageshas = Main.getPages(rawsourcehas), pageswants = Main.getPages(rawsourcewants);
						item = new Item();
						item.amount = 1;
						if (pageshas <= pageswants) {
							item.name = (String) wantsitems.getSelectedItem();
							String paint = (String) wantspaints.getSelectedItem();
							if (!paint.equals("Any"))
								item.paint = paint;
							String certification = (String) wantscertifications.getSelectedItem();
							if (!certification.equals("Any"))
								item.certification = certification;
							Main.getOffers(hasurl, maxthreads, timeout,
									exactmatch.isSelected() ? Main.Mode.ExactHasWants : Main.Mode.HasWants,
									rawsourcehas, pageshas);
						} else {
							item.name = (String) hasitems.getSelectedItem();
							String paint = (String) haspaints.getSelectedItem();
							if (!paint.equals("Any"))
								item.paint = paint;
							String certification = (String) hascertifications.getSelectedItem();
							if (!certification.equals("Any"))
								item.certification = certification;
							Main.getOffers(wantsurl, maxthreads, timeout,
									exactmatch.isSelected() ? Main.Mode.ExactWantsHas : Main.Mode.WantsHas,
									rawsourcewants, pageswants);
						}
					} else if (exactmatch.isSelected())
						Main.getOffers(hasurl, maxthreads, timeout, Main.Mode.ExactHas);
					else
						openWebpage(hasurl);
				} else if (!wantsitems.getSelectedItem().equals("Any")) {
					if (exactmatch.isSelected())
						Main.getOffers(wantsurl, maxthreads, timeout, Main.Mode.ExactWants);
					else
						openWebpage(wantsurl);
				}
			}
		});
		add(start);

		setVisible(true);

		hasitems.requestFocus();

		new Thread(new Runnable() {
			@Override
			public void run() {
				ids = Main.loadIDs();
				ArrayList<String> items = new ArrayList<String>(ids.get(0).keySet());
				items.remove("Any");
				Collections.sort(items);
				items.add(0, "Any");
				ArrayList<String> certifications = new ArrayList<String>(ids.get(1).keySet());
				certifications.remove("Any");
				certifications.remove("None");
				Collections.sort(certifications);
				certifications.add(0, "Any");
				certifications.add(1, "None");
				ArrayList<String> paints = new ArrayList<String>(ids.get(2).keySet());
				paints.remove("Any");
				paints.remove("None");
				Collections.sort(paints);
				paints.add(0, "Any");
				paints.add(1, "None");
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						for (String item : items) {
							hasitems.addItem(item);
							wantsitems.addItem(item);
						}
						hasitems.setSelectedItem("Any");
						wantsitems.setSelectedItem("Any");
						for (String certification : certifications) {
							hascertifications.addItem(certification);
							wantscertifications.addItem(certification);
						}
						hascertifications.setSelectedItem("Any");
						wantscertifications.setSelectedItem("Any");
						for (String paint : paints) {
							haspaints.addItem(paint);
							wantspaints.addItem(paint);
						}
						haspaints.setSelectedItem("Any");
						wantspaints.setSelectedItem("Any");
					}
				});
			}
		}).start();
	}

	private void openWebpage(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	void updateTable(ArrayList<Offer> offers) {
		ArrayList<ArrayList<Object>> dataArray = new ArrayList<>();
		for (int i = 0; i < offers.size(); i++) {
			dataArray.add(new ArrayList<Object>());
			dataArray.get(i).add(toString(offers.get(i).has));
			dataArray.get(i).add(toString(offers.get(i).wants));
			dataArray.get(i).add(offers.get(i).steamname);
			dataArray.get(i).add(offers.get(i).rlgusername);
			dataArray.get(i).add(offers.get(i).tradeurl);
			dataArray.get(i).add(offers.get(i).notes);
		}
		ArrayList<Object[]> data1 = new ArrayList<>();
		for (ArrayList<Object> row : dataArray)
			data1.add(row.toArray(new Object[6]));
		Object[][] data = new Object[dataArray.size()][6];
		data1.toArray(data);
		model = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public Class<?> getColumnClass(int columnIndex) {
				return String.class;
			}

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setModel(model);
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model);
		table.setRowSorter(sorter);
		ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
		sorter.setSortKeys(sortKeys);
		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int o1amount = 0, o2amount = 0;
				for (String o1item : o1.split("\n"))
					o1amount += Integer.valueOf(o1item.split(" ")[0]);
				for (String o2item : o2.split("\n"))
					o2amount += Integer.valueOf(o2item.split(" ")[0]);
				return o1amount < o2amount ? -1 : o1amount > o2amount ? 1 : 0;
			}
		};
		sorter.setComparator(0, comparator);
		sorter.setComparator(1, comparator);
		sorter.sort();
		table.getColumnModel().getColumn(4).setPreferredWidth(30);
	}

	private String toString(ArrayList<Item> items) {
		String result = "";
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			result += item.amount + " " + item.name + (item.paint.isEmpty() ? "" : "(" + item.paint + ")")
					+ (item.certification.isEmpty() ? "" : "[" + item.certification + "]");
			if (i != items.size() - 1)
				result += "\n";
		}
		return result;
	}

	private class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private ArrayList<ArrayList<Integer>> rowColHeight = new ArrayList<ArrayList<Integer>>();

		public MultiLineTableCellRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setFont(table.getFont());
			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					setForeground(UIManager.getColor("Table.focusCellForeground"));
					setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			} else
				setBorder(new EmptyBorder(1, 2, 1, 2));
			setText(value == null ? "" : value.toString());
			adjustRowHeight(table, row, column);
			return this;
		}

		private void adjustRowHeight(JTable table, int row, int column) {
			int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
			setSize(new Dimension(cWidth, 1000));
			int prefH = getPreferredSize().height;
			while (rowColHeight.size() <= row)
				rowColHeight.add(new ArrayList<Integer>(column));
			ArrayList<Integer> colHeights = rowColHeight.get(row);
			while (colHeights.size() <= column)
				colHeights.add(0);
			colHeights.set(column, prefH);
			int maxH = prefH;
			for (int colHeight : colHeights)
				if (colHeight > maxH)
					maxH = colHeight;
			if (table.getRowHeight(row) != maxH)
				table.setRowHeight(row, maxH);
		}
	}
}
