package com.addondowner;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by johlar on 10/03/15.
 *
 */
public class MainWindow {
	private JPanel mainPanel;
	private JTable tblAddon;
	private JButton btnDelete;
	private JLabel topLabel;
	private JTextField txtAddonDir;
	private JButton btnDeleteVersion;
	private JCheckBox chkbAutoUpdate;
	private JButton btnAdd;
	private JButton btnUpdateAll;
	private JTextField txtWowLauncher;
	private JCheckBox chkbAutoStartLauncher;
	private JButton btnLauncher;
	private JCheckBox chkbQuitAfterUpdate;
	private JButton btnUpdate;

	private DefaultTableModel dtm;

	private static String OS = System.getProperty("os.name").toLowerCase();
	private java.util.List<UpdateWorker> updateWorkers = new ArrayList<UpdateWorker>();

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public MainWindow() {
		loadAddons();

		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddAddon addAddon = new AddAddon();
				addAddon.setLocationRelativeTo(mainPanel);
				addAddon.setLocation(100,100);
				addAddon.setSize(new Dimension(700, 450));
				addAddon.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						loadAddons();
					}
				});
				addAddon.setVisible(true);
			}
		});
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedAddonsFromDB();
			}

		});

		btnDeleteVersion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedAddonsVersionInfo();
			}

		});

		txtAddonDir.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				saveEntry();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				saveEntry();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				saveEntry();
			}

			public void saveEntry() {
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_WOW_ADDON_DIR, txtAddonDir.getText());
				dataSaverWorker.execute();
			}
		});

		txtWowLauncher.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				saveEntry();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				saveEntry();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				saveEntry();
			}

			public void saveEntry() {
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_WOW_LAUNCHER, txtWowLauncher.getText());
				dataSaverWorker.execute();
			}
		});

		chkbAutoUpdate.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_AUTO_UPDATE_ON_LAUNCH, String.valueOf(chkbAutoUpdate.isSelected()));
				dataSaverWorker.execute();
			}
		});
		chkbQuitAfterUpdate.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_AUTO_QUIT_AFTER_UPDATE, String.valueOf(chkbQuitAfterUpdate.isSelected()));
				dataSaverWorker.execute();
			}
		});
		chkbAutoStartLauncher.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_AUTO_START_LAUNCHER, String.valueOf(chkbAutoStartLauncher.isSelected()));
				dataSaverWorker.execute();
			}
		});

		btnUpdateAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAddonUpdate(false);

			}
		});

		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAddonUpdate(true);

			}
		});

		btnLauncher.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLauncherStart();
			}
		});

		txtAddonDir.setText(DataSource.getPref(AddonDowner.PREF_KEY_WOW_ADDON_DIR));

		txtWowLauncher.setText(DataSource.getPref(AddonDowner.PREF_KEY_WOW_LAUNCHER));

		boolean doAutoUpdate = DataSource.getPref(AddonDowner.PREF_KEY_AUTO_UPDATE_ON_LAUNCH).equalsIgnoreCase("true");
		chkbAutoUpdate.setSelected(doAutoUpdate);

		chkbQuitAfterUpdate.setSelected(DataSource.getPref(AddonDowner.PREF_KEY_AUTO_QUIT_AFTER_UPDATE).equalsIgnoreCase("true"));

		boolean doStartLauncher = DataSource.getPref(AddonDowner.PREF_KEY_AUTO_START_LAUNCHER).equalsIgnoreCase("true");
		chkbAutoStartLauncher.setSelected(doStartLauncher);

		if(doAutoUpdate){
			doAddonUpdate(false);
		}
		if(doStartLauncher){
			doLauncherStart();
		}
	}

	private void doLauncherStart() {
		try {
			// launcherPath = "/Applications/World of Warcraft/World of Warcraft Launcher.app";
			String launcherPath = DataSource.getPref(AddonDowner.PREF_KEY_WOW_LAUNCHER);

			if (null != launcherPath && launcherPath.length() > 0) {
				String[] cmdline;
				if(isMac()){
					cmdline = new String[]{"open", "-a", launcherPath};
				} else {
					cmdline = new String[]{launcherPath};
				}
				Runtime.getRuntime().exec(cmdline);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doAddonUpdate(boolean onlySelected) {

		boolean isUpdating = false;
		for (UpdateWorker updateWorker : updateWorkers) {
			if (updateWorker.getProgress() != 100) {
				isUpdating = true;
			}
		}
		if(!isUpdating){
			Date start = new Date();
			try {
				java.util.List<Addon> addons = new ArrayList<Addon>();
				if(onlySelected){
					Integer selectedData;
					int[] selectedRows = tblAddon.getSelectedRows();
					for (int aSelectedRow : selectedRows) {
						selectedData = (Integer) tblAddon.getValueAt(aSelectedRow, 1);
						if (null != selectedData) {
							addons.add(new Addon(selectedData));
						}
					}
				} else {
					addons = Addon.fetchAddonList();
				}

				int waitCounter = 0;
				while (tblAddon.getRowCount() < 1 && waitCounter < 100){
					System.out.println("Waiting for table load");
					Thread.sleep(100);
					waitCounter++;
				}
				if(waitCounter < 100){
					System.out.println("Starting all updating workers");
					updateWorkers = new ArrayList<UpdateWorker>();
					for (Addon addon : addons) {
						UpdateWorker updateWorker = new UpdateWorker(dtm, addon);
						updateWorkers.add(updateWorker);
						updateWorker.execute();
					}
					ProgressWorker pgw = new ProgressWorker(updateWorkers, start);
					pgw.execute();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Is already updating dont starting a new");
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("MainWindow");
		frame.setTitle(args[0]);
		frame.setContentPane(new MainWindow().mainPanel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(800, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void loadAddons() {
		btnDelete.setVisible(false);
		btnDeleteVersion.setVisible(false);
		btnUpdate.setVisible(false);

		String header[] = new String[]{"Status", "Id", "Name", "URL"};
		dtm = new DefaultTableModel(0, header.length){
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		dtm.setColumnIdentifiers(header);
		tblAddon.setModel(dtm);
		ListSelectionModel selectionModel = tblAddon.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				btnDelete.setVisible(tblAddon.getSelectedRows().length > 0);
				btnDeleteVersion.setVisible(tblAddon.getSelectedRows().length > 0);
				btnUpdate.setVisible(tblAddon.getSelectedRows().length > 0);
			}
		});

		java.util.List<Addon> addons = Addon.fetchAddonList();
		for (Addon addon : addons) {
			dtm.addRow(new Object[]{"", addon.getId(), addon.getName(), addon.getUrl()});
		}

		tblAddon.getColumnModel().getColumn(0).setPreferredWidth(130);
		tblAddon.getColumnModel().getColumn(1).setPreferredWidth(30);
		tblAddon.getColumnModel().getColumn(2).setPreferredWidth(180);
		tblAddon.getColumnModel().getColumn(3).setPreferredWidth(500);
		//tblAddon.getColumn("Id").setPreferredWidth(30);
		//tblAddon.getColumn("Id").setWidth(30);
		//tblAddon.getColumn("Id").setMaxWidth(30);
		//tblAddon.getColumn("Name").setPreferredWidth(80);
		//tblAddon.getColumn("Name").setWidth(80);
	}

	private void deleteSelectedAddonsVersionInfo() {
		Integer selectedData;
		java.util.List<Integer> rowsToDelete = new ArrayList<Integer>();
		int[] selectedRows = tblAddon.getSelectedRows();
		for (int aSelectedRow : selectedRows) {
			selectedData = (Integer) tblAddon.getValueAt(aSelectedRow, 1);
			if (null != selectedData) {
				rowsToDelete.add(selectedData);
			}
		}
		if (rowsToDelete.size() > 0) {
			String addonsToDelete = "";
			for (int i = 0; i < rowsToDelete.size(); i++) {
				Integer addonId = rowsToDelete.get(i);
				if (i > 0) {
					addonsToDelete = addonsToDelete + ",";
				}
				addonsToDelete = addonsToDelete + addonId;
			}
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = DataSource.getInstance().getConnection();
				ps = conn.prepareStatement("DELETE FROM addon_version WHERE addon_list_id IN (" + addonsToDelete + "); ");
				ps.execute();
				for (int aSelectedRow : selectedRows) {
					selectedData = (Integer) tblAddon.getValueAt(aSelectedRow, 1);
					if (null != selectedData) {
						tblAddon.setValueAt("Version info removed", aSelectedRow, 0);
					}
				}
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			} finally {
				DataSource.closeQuietly(null, ps, conn);
			}
		}
	}

	private void deleteSelectedAddonsFromDB() {
		Integer selectedData;
		java.util.List<Integer> rowsToDelete = new ArrayList<Integer>();
		int[] selectedRows = tblAddon.getSelectedRows();
		for (int aSelectedRow : selectedRows) {
			selectedData = (Integer) tblAddon.getValueAt(aSelectedRow, 1);
			if (null != selectedData) {
				rowsToDelete.add(selectedData);
			}
		}
		if (rowsToDelete.size() > 0) {
			String addonsToDelete = "";
			for (int i = 0; i < rowsToDelete.size(); i++) {
				Integer addonId = rowsToDelete.get(i);
				if (i > 0) {
					addonsToDelete = addonsToDelete + ",";
				}
				addonsToDelete = addonsToDelete + addonId;
			}
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = DataSource.getInstance().getConnection();

				ps = conn.prepareStatement("DELETE FROM addon_version WHERE addon_list_id IN (" + addonsToDelete + "); ");
				ps.execute();
				ps.close();

				ps = conn.prepareStatement("DELETE FROM addon_list WHERE id IN (" + addonsToDelete + "); ");
				ps.execute();
				ps.close();

			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			} finally {
				DataSource.closeQuietly(null, ps, conn);
			}
			loadAddons();
		}
	}

	public static boolean isWindows() {
		return (OS.contains("win"));
	}

	public static boolean isMac() {
		return (OS.contains("mac"));
	}

	public static boolean isUnix() {
		return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (OS.contains("sunos"));
	}
}
