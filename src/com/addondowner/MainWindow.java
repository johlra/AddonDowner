package com.addondowner;

import org.h2.jdbcx.JdbcConnectionPool;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

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
	private JButton btnUpdate;
	private JTextField txtWowLauncher;
	private JCheckBox chkbAutoStartLauncher;
	private JButton btnLauncher;
	private JCheckBox chkbQuitAfterUpdate;

	private DefaultTableModel dtm;

	private static String OS = System.getProperty("os.name").toLowerCase();
	private java.util.List<UpdateWorker> updateWorkers = new ArrayList<UpdateWorker>();
	private DataLoadWorker dataLoadWorker = null;

	private static JdbcConnectionPool cp;
	private static Connection conn;

	public MainWindow() {
		loadAddons();

		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NewAddon newAddon = new NewAddon();
				//newAddon.setLocationRelativeTo(mainPanel);
				newAddon.setSize(new Dimension(500, 300));
				newAddon.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						loadAddons();
					}
				});
				newAddon.setVisible(true);
			}
		});
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Integer selectedData = null;
				java.util.List<Integer> rowsToDelete = new ArrayList<Integer>();
				int[] selectedRows = tblAddon.getSelectedRows();
				for (int aSelectedRow : selectedRows) {
					selectedData = (Integer) tblAddon.getValueAt(aSelectedRow, 1);
					if (null != selectedData) {
						rowsToDelete.add(selectedData);
					}
				}
				if(rowsToDelete.size() > 0){
					String addonsToDelete = "";
					for (int i = 0; i < rowsToDelete.size(); i++) {
						Integer addonId = rowsToDelete.get(i);
						if(i>0){
							addonsToDelete = addonsToDelete + ",";
						}
						addonsToDelete = addonsToDelete + addonId;
					}
					try {
						Class.forName("org.h2.Driver");
						Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION, "sa", "sa");

						PreparedStatement ps = conn.prepareStatement("DELETE FROM addon_version WHERE addon_list_id IN (" + addonsToDelete + "); ");
						ps.execute();
						ps.close();

						ps = conn.prepareStatement("DELETE FROM addon_list WHERE id IN (" + addonsToDelete + "); ");
						ps.execute();
						ps.close();

						conn.close();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					loadAddons();
				}
			}

		});

		btnDeleteVersion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Integer selectedData = null;
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
					try {
						Class.forName("org.h2.Driver");
						Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION, "sa", "sa");

						PreparedStatement ps = conn.prepareStatement("DELETE FROM addon_version WHERE addon_list_id IN (" + addonsToDelete + "); ");
						ps.execute();
						ps.close();

						for (int aSelectedRow : selectedRows) {
							selectedData = (Integer) tblAddon.getValueAt(aSelectedRow, 1);
							if (null != selectedData) {
								tblAddon.setValueAt("Version info removed", aSelectedRow, 0);
							}
						}
						conn.close();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}

		});

		txtAddonDir.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				saveEntry(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				saveEntry(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				saveEntry(e);
			}

			public void saveEntry(DocumentEvent e) {
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_WOW_ADDON_DIR, txtAddonDir.getText());
				dataSaverWorker.execute();
			}
		});

		txtWowLauncher.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				saveEntry(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				saveEntry(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				saveEntry(e);
			}

			public void saveEntry(DocumentEvent e) {
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

		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAddonUpdate();

			}
		});

		btnLauncher.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLuncherStart();
			}
		});

		boolean doAutoUpdate = false;
		boolean doStartLauncher = false;
		try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION, "sa", "sa");

			boolean hasPrefs = false;
			PreparedStatement ps = conn.prepareStatement("SHOW TABLES");
			ResultSet resultSet = ps.executeQuery();
			while(resultSet.next()) {
				if("prefs".equalsIgnoreCase(resultSet.getString(1))){
					hasPrefs = true;
				}
			}
			if(hasPrefs){
				resultSet.close();
				ps.close();
				ps = conn.prepareStatement("SELECT data FROM prefs WHERE name = ?;");
				ps.setString(1, AddonDowner.PREF_KEY_WOW_ADDON_DIR);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					txtAddonDir.setText(rs.getString(1));
				}
				rs.close();
				ps.setString(1, AddonDowner.PREF_KEY_WOW_LAUNCHER);
				rs = ps.executeQuery();
				if (rs.next()) {
					txtWowLauncher.setText(rs.getString(1));
				}
				rs.close();
				ps.setString(1, AddonDowner.PREF_KEY_AUTO_UPDATE_ON_LAUNCH);
				rs = ps.executeQuery();
				if (rs.next()) {
					doAutoUpdate = Boolean.valueOf(rs.getString(1));
					chkbAutoUpdate.setSelected(doAutoUpdate);
				}
				rs.close();
				ps.setString(1, AddonDowner.PREF_KEY_AUTO_QUIT_AFTER_UPDATE);
				rs = ps.executeQuery();
				if (rs.next()) {
					chkbQuitAfterUpdate.setSelected(Boolean.valueOf(rs.getString(1)));
				}
				rs.close();
				ps.setString(1, AddonDowner.PREF_KEY_AUTO_START_LAUNCHER);
				rs = ps.executeQuery();
				if (rs.next()) {
					doStartLauncher = Boolean.valueOf(rs.getString(1));
					chkbAutoStartLauncher.setSelected(doStartLauncher);
				}
				rs.close();
				ps.close();
			}
			conn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if(doAutoUpdate){
			doAddonUpdate();
		}
		if(doStartLauncher){
			doLuncherStart();
		}
		//System.getProperties().list(System.out);

	}

	private void doLuncherStart() {
		try {
			// launcherPath = "/Applications/World of Warcraft/World of Warcraft Launcher.app";
			String launcherPath = "";
			try {
				Class.forName("org.h2.Driver");
				Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION, "sa", "sa");

				PreparedStatement ps = conn.prepareStatement("SELECT data FROM prefs WHERE name = ?;");
				ps.setString(1, AddonDowner.PREF_KEY_WOW_LAUNCHER);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					launcherPath = rs.getString(1);
				}
				rs.close();
				ps.close();
				conn.close();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (null != launcherPath && launcherPath.length() > 0) {
				String[] cmdline;
				if(isMac()){
					cmdline = new String[]{"open", "-a", launcherPath};
				} else {
					cmdline = new String[]{launcherPath};
				}
				Runtime.getRuntime().exec(cmdline);
				//Process proc = Runtime.getRuntime().exec("open \"/Applications/World of Warcraft/World of Warcraft Launcher.app\"");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doAddonUpdate() {

		java.util.List<Addon> addons = new ArrayList<Addon>();
		try {
			JdbcConnectionPool cp = JdbcConnectionPool.create(AddonDowner.BD_CONNECTION, "sa", "sa");
			Connection conn = cp.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("SELECT id,name, main_page_url FROM addon_list;");

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				addons.add(new Addon(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)));
			}
			resultSet.close();
			preparedStatement.close();
			conn.close();
			cp.dispose();
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			// show message e.getMessage();
		}

		boolean isUpdating = false;
		for (UpdateWorker updateWorker : updateWorkers) {
			if (updateWorker.getProgress() != 100) {
				isUpdating = true;
			}
		}
		if(!isUpdating){
			try {
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
					ProgressWorker pgw = new ProgressWorker(updateWorkers);
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
		frame.setVisible(true);
	}

	public void loadAddons() {
		btnDelete.setVisible(false);
		btnDeleteVersion.setVisible(false);

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
			}
		});

		try {
			dataLoadWorker = new DataLoadWorker(dtm);
			dataLoadWorker.execute();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new MainWindow().mainPanel, "DataLoadWorker Error: " + e.getMessage());
			e.printStackTrace();
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

	@Override
	protected void finalize() throws Throwable {
		cp.dispose();
		super.finalize();
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

/*	void dbconnect(){
		try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION);


			conn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}


		try {
			JdbcConnectionPool cp = JdbcConnectionPool.create(AddonDowner.BD_CONNECTION, "sa", "sa");
			Connection conn = cp.getConnection();
			conn.close();
			cp.dispose();
		} catch (SQLException e) {
			e.printStackTrace();
		}


	} */
}
