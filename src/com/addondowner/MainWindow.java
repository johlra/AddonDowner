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
	private JTextField txtCmdBefore;
	private JCheckBox chkbDoRunBefore;
	private JButton btnLauncher;
	private JCheckBox chkbQuitAfterUpdate;
	private JButton btnUpdate;
	private JCheckBox doRunAfterCheckBox;
	private JTextField txtCmdAfter;
	private JButton btnExport;

	private DefaultTableModel dtm;

	private static String OS = System.getProperty("os.name").toLowerCase();
	private java.util.List<UpdateWorker> updateWorkers = new ArrayList<UpdateWorker>();

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public MainWindow() {
		loadAddons();

		btnExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewAddonList viewAddonList = new ViewAddonList();
				viewAddonList.setLocationRelativeTo(mainPanel);
				viewAddonList.setSize(new Dimension(900,700));
				SetWindowPosCenter(viewAddonList);
				viewAddonList.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						loadAddons();
					}
				});
				viewAddonList.setVisible(true);
			}
		});
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddAddon addAddon = new AddAddon();
				addAddon.setLocationRelativeTo(mainPanel);
				addAddon.setSize(new Dimension(700, 450));
				SetWindowPosCenter(addAddon);
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

		txtCmdBefore.getDocument().addDocumentListener(new DocumentListener() {
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
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_CMD_BEFORE, txtCmdBefore.getText());
				dataSaverWorker.execute();
			}
		});
		txtCmdAfter.getDocument().addDocumentListener(new DocumentListener() {
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
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_CMD_AFTER, txtCmdAfter.getText());
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
		chkbDoRunBefore.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_DO_CMD_BEFORE, String.valueOf(chkbDoRunBefore.isSelected()));
				dataSaverWorker.execute();
			}
		});
		doRunAfterCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_DO_CMD_AFTER, String.valueOf(doRunAfterCheckBox.isSelected()));
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
				doCmdBefore();
			}
		});

		txtAddonDir.setText(DataSource.getPref(AddonDowner.PREF_KEY_WOW_ADDON_DIR));

		txtCmdBefore.setText(DataSource.getPref(AddonDowner.PREF_KEY_CMD_BEFORE));
		txtCmdAfter.setText(DataSource.getPref(AddonDowner.PREF_KEY_CMD_AFTER));


		boolean doAutoUpdate = DataSource.getPref(AddonDowner.PREF_KEY_AUTO_UPDATE_ON_LAUNCH).equalsIgnoreCase("true");
		chkbAutoUpdate.setSelected(doAutoUpdate);

		chkbQuitAfterUpdate.setSelected(DataSource.getPref(AddonDowner.PREF_KEY_AUTO_QUIT_AFTER_UPDATE).equalsIgnoreCase("true"));

		boolean doStartLauncher = DataSource.getPref(AddonDowner.PREF_KEY_DO_CMD_BEFORE).equalsIgnoreCase("true");
		chkbDoRunBefore.setSelected(doStartLauncher);

		doRunAfterCheckBox.setSelected(DataSource.getPref(AddonDowner.PREF_KEY_DO_CMD_AFTER).equalsIgnoreCase("true"));

		if (doStartLauncher) {
			doCmdBefore();
		}

		if(doAutoUpdate){
			doAddonUpdate(false);
		}
	}

	static void SetWindowPosCenter(JDialog addAddon) {
		Point windowsStartLocation = getWindowsStartLocation();

		if (null != windowsStartLocation) {
			Dimension windowStartSize = getWindowStartSize();
			int x = (int)windowsStartLocation.getX() + (((int)windowStartSize.getWidth()- addAddon.getWidth()) /2);
			int y = (int)windowsStartLocation.getY() + (((int)windowStartSize.getHeight()-addAddon.getHeight())/2);
			addAddon.setLocation(x,y);
		} else {
			addAddon.setLocationRelativeTo(null);
		}
	}

	private void doCmdBefore() {
		try {
			// launcherPath = "/Applications/World of Warcraft/World of Warcraft Launcher.app";
			String launcherPath = DataSource.getPref(AddonDowner.PREF_KEY_CMD_BEFORE);

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
		frame.setSize(getWindowStartSize());
		Point windowsStartLocation = getWindowsStartLocation();
		if(null != windowsStartLocation){
			frame.setLocation(windowsStartLocation);
		} else {
			frame.setLocationRelativeTo(null);
		}
		frame.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				saveWindowSize(e);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				saveWindowPos(e);
			}

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		frame.setVisible(true);
	}

	private static void saveWindowSize(ComponentEvent e) {
		int x = e.getComponent().getWidth();
		int y = e.getComponent().getHeight();
		String pos = x + ";" + y;
		DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_WINDOW_SIZE, pos);
		dataSaverWorker.execute();
	}

	private static Dimension getWindowStartSize() {
		try {
			String comprPos = DataSource.getPref(AddonDowner.PREF_KEY_WINDOW_SIZE);
			comprPos = comprPos.replace("x", "").replace("y", "").replace(":", "");
			String[] pos = comprPos.split(";");
			int posx = Integer.parseInt(pos[0]);
			int posy = Integer.parseInt(pos[1]);
			return new Dimension(posx, posy);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return new Dimension(800, 500);
	}

	private static void saveWindowPos(ComponentEvent e) {
		int x = e.getComponent().getX();
		int y = e.getComponent().getY();
		String pos = x + ";" + y;
		DataSaverWorker dataSaverWorker = new DataSaverWorker(AddonDowner.PREF_KEY_WINDOW_POS, pos);
		dataSaverWorker.execute();
	}

	private static Point getWindowsStartLocation() {
		try {
			String comprPos = DataSource.getPref(AddonDowner.PREF_KEY_WINDOW_POS);
			comprPos = comprPos.replace("x","").replace("y","").replace(":", "");
			String[] pos = comprPos.split(";");
			int posx = Integer.parseInt(pos[0]);
			int posy = Integer.parseInt(pos[1]);

			boolean posXOnScreen = false;
			boolean posYOnScreen = false;
			GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			for (GraphicsDevice device : devices) {
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				Point location = bounds.getLocation();
				Dimension size = bounds.getSize();
				if(posx > location.getX() && posx < (location.getX() + size.getWidth())){
					posXOnScreen = true;
				}
				if (posy > location.getY() && posy < (location.getY() + size.getHeight())) {
					posYOnScreen = true;
				}
			}
			if(posXOnScreen && posYOnScreen){
				return new Point(posx,posy);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (HeadlessException e) {
			e.printStackTrace();
		}
		return null;
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
