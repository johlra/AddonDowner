package com.addondowner;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	private JButton btnBrowse;
	private JButton btnBrowsBefore;
	private JButton btnBrowseAfter;
	private JFileChooser fcDir;
	private JFileChooser fcFile;

	private DefaultTableModel dtm;

	private static String OS = System.getProperty("os.name").toLowerCase();
	private java.util.List<UpdateWorker> updateWorkers = new ArrayList<UpdateWorker>();

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public MainWindow() {
		loadAddons();

		fcDir = new JFileChooser();
		fcDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fcFile = new JFileChooser();
		fcFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);


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
				NewAddon addAddon = new NewAddon();
				addAddon.setLocationRelativeTo(mainPanel);
				addAddon.setSize(new Dimension(700, 450));
				SetWindowPosCenter(addAddon);
				addAddon.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						loadAddons();
						Date start = new Date();
						updateWorkers = new ArrayList<UpdateWorker>();
						for (int i = 0; i < tblAddon.getRowCount(); i++) {
							if(((String) tblAddon.getValueAt(i, 0)).equalsIgnoreCase("No version")){
								UpdateWorker updateWorker = new UpdateWorker(dtm, new Addon((String) tblAddon.getValueAt(i, 2)));
								updateWorkers.add(updateWorker);
								updateWorker.execute();
							}
						}
						ProgressWorker pgw = new ProgressWorker(updateWorkers, start);
						pgw.execute();

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
				Preference.WOW_ADDON_DIR(txtAddonDir.getText());
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
				Preference.CMD_BEFORE(txtCmdBefore.getText());
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
				Preference.CMD_AFTER(txtCmdAfter.getText());
			}
		});

		chkbAutoUpdate.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Preference.AUTO_UPDATE_ON_LAUNCH(chkbAutoUpdate.isSelected());
			}
		});
		chkbQuitAfterUpdate.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Preference.AUTO_QUIT_AFTER_UPDATE(chkbQuitAfterUpdate.isSelected());
			}
		});
		chkbDoRunBefore.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Preference.DO_CMD_BEFORE(chkbDoRunBefore.isSelected());
			}
		});
		doRunAfterCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Preference.DO_CMD_AFTER(doRunAfterCheckBox.isSelected());
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

		txtAddonDir.setText(Preference.WOW_ADDON_DIR());

		txtCmdBefore.setText(Preference.CMD_BEFORE());
		txtCmdAfter.setText(Preference.CMD_AFTER());


		boolean doAutoUpdate = Preference.AUTO_UPDATE_ON_LAUNCH();
		chkbAutoUpdate.setSelected(doAutoUpdate);

		chkbQuitAfterUpdate.setSelected(Preference.AUTO_QUIT_AFTER_UPDATE());

		boolean doStartLauncher = Preference.DO_CMD_BEFORE();
		chkbDoRunBefore.setSelected(doStartLauncher);

		doRunAfterCheckBox.setSelected(Preference.DO_CMD_AFTER());

		if (doStartLauncher) {
			doCmdBefore();
		}

		if(doAutoUpdate){
			doAddonUpdate(false);
		}
		btnBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fcDir.showOpenDialog(btnBrowse);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fcDir.getSelectedFile();
					txtAddonDir.setText(file.getAbsolutePath());
				}
			}
		});
		btnBrowsBefore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fcFile.showOpenDialog(btnBrowsBefore);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fcFile.getSelectedFile();
					txtCmdBefore.setText(file.getAbsolutePath());
				}
			}
		});
		btnBrowseAfter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fcFile.showOpenDialog(btnBrowseAfter);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fcFile.getSelectedFile();
					txtCmdAfter.setText(file.getAbsolutePath());
				}
			}
		});
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
			String launcherPath = Preference.CMD_BEFORE();

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
					int[] selectedRows = tblAddon.getSelectedRows();
					for (int aSelectedRow : selectedRows) {
						String selectedData = (String) tblAddon.getValueAt(aSelectedRow, 2);
						if (null != selectedData) {
							addons.add(new Addon(selectedData));
						}
					}
				} else {
					addons = AddonDowner.allAddons;
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
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				Addon.saveAddonListToJson(AddonDowner.allAddons);
				Preference.saveAllPrefs(AddonDowner.allPrefs);
				//e.getWindow().dispose();
			}
		});
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
		Preference.WINDOW_SIZE(x + ";" + y);
	}

	private static Dimension getWindowStartSize() {
		try {
			String comprPos = Preference.WINDOW_SIZE();
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
		Preference.WINDOW_POS(x + ";" + y);
	}

	private static Point getWindowsStartLocation() {
		try {
			String comprPos = Preference.WINDOW_POS();
			if("".equals(comprPos)){
				return null;
			}
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

		Collections.sort(AddonDowner.allAddons);
		for (Addon addon : AddonDowner.allAddons) {
			String info = "";
			if(addon.getVersionDownloadPage().isEmpty()){
				info = "No version";
			}
			dtm.addRow(new Object[]{info, addon.getId(), addon.getName(), addon.getUrl()});
		}

		tblAddon.getColumnModel().getColumn(0).setPreferredWidth(130);
		//tblAddon.getColumnModel().getColumn(1).setPreferredWidth(30);
		tblAddon.getColumnModel().getColumn(1).setMinWidth(0);
		tblAddon.getColumnModel().getColumn(1).setMaxWidth(0);
		tblAddon.getColumnModel().getColumn(1).setWidth(0);
		tblAddon.getColumnModel().getColumn(2).setPreferredWidth(280);
		tblAddon.getColumnModel().getColumn(3).setPreferredWidth(500);
		//tblAddon.getColumn("Id").setPreferredWidth(30);
		//tblAddon.getColumn("Id").setWidth(30);
		//tblAddon.getColumn("Id").setMaxWidth(30);
		//tblAddon.getColumn("Name").setPreferredWidth(80);
		//tblAddon.getColumn("Name").setWidth(80);
	}

	private void deleteSelectedAddonsVersionInfo() {
		int[] selectedRows = tblAddon.getSelectedRows();
		for (int aSelectedRow : selectedRows) {
			String delAddonName = (String) tblAddon.getValueAt(aSelectedRow, 2);
			for (int i = 0; i < AddonDowner.allAddons.size(); i++) {
				Addon addon = AddonDowner.allAddons.get(i);
				if(addon.getName().equalsIgnoreCase(delAddonName)){
					addon.setVersionDownloadPage("");
				}
			}
		}
	}

	private void deleteSelectedAddonsFromDB() {
		int[] selectedRows = tblAddon.getSelectedRows();
		for (int aSelectedRow : selectedRows) {
			String delAddonName = (String) tblAddon.getValueAt(aSelectedRow, 2);
			for (int i = 0; i < AddonDowner.allAddons.size(); i++) {
				Addon addon = AddonDowner.allAddons.get(i);
				if(addon.getName().equalsIgnoreCase(delAddonName)){
					AddonDowner.allAddons.remove(i);
				}
			}
		}
		loadAddons();
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
