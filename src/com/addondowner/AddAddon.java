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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by johlar on 12/04/15.
 *
 */
public class AddAddon extends JDialog {
	private JPanel panel1;
	private JTable tblAddon;
	private JButton newButton;
	private JButton OKButton;
	private JButton scanForInstalledButton;
	private JTextField textFieldFilter;

	private DefaultTableModel dtm;

	private final static String header[] = new String[]{"Check", "Id", "Name", "URL"};
	private final static Class[] columnClass = new Class[]{Boolean.class, Integer.class, String.class, String.class};

	Addon[] addons = new Addon[0];

	public static void main(String[] args) {
		AddAddon dialog = new AddAddon();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

	public AddAddon() {
		setContentPane(panel1);
		setModal(true);

		OKButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NewAddon newAddon = new NewAddon();
				newAddon.setSize(new Dimension(500, 400));
				MainWindow.SetWindowPosCenter(newAddon);
				newAddon.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						loadAddons();
					}
				});
				newAddon.setVisible(true);
			}
		});

		scanForInstalledButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String addonDir = DataSource.getPref(AddonDowner.PREF_KEY_WOW_ADDON_DIR);
				File file = new File(addonDir);
				String[] directories = file.list(new FilenameFilter() {
					@Override
					public boolean accept(File current, String name) {
						return new File(current, name).isDirectory();
					}
				});
				System.out.println("Asking with: " + Arrays.toString(directories));
				try {
					Addon[] addons = NetHandler.getAddonsFromDirs(directories);
					for (Addon addon : addons) {
						for (int i = 0; i < tblAddon.getRowCount(); i++) {
							Integer addonId = (Integer) tblAddon.getValueAt(i, 1);
							if (addonId == addon.getId()) {
								tblAddon.setValueAt(Boolean.TRUE,i,0);
								break;
							}
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		dtm = new DefaultTableModel(0, header.length) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnClass[columnIndex];
			}
		};
		dtm.setColumnIdentifiers(header);

		tblAddon.setModel(dtm);
		ListSelectionModel selectionModel = tblAddon.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean stateIsChanged = false;
				for (int i = 0; i < tblAddon.getRowCount(); i++) {
					Integer addonId = (Integer) tblAddon.getValueAt(i, 1);
					for (Addon addon : addons) {
						if(addonId == addon.getId()){
							if(addon.isSelected() != (Boolean) tblAddon.getValueAt(i, 0)){
								addon.setSelected((Boolean) tblAddon.getValueAt(i, 0));
								stateIsChanged = true;
							}
							break;
						}
					}
				}
				if(stateIsChanged){
					updateAddonList();
				}
			}
		});
		loadAddons();
		tblAddon.getColumnModel().getColumn(0).setPreferredWidth(30);
		tblAddon.getColumnModel().getColumn(1).setPreferredWidth(30);
		tblAddon.getColumnModel().getColumn(2).setPreferredWidth(180);
		tblAddon.getColumnModel().getColumn(3).setPreferredWidth(500);


		textFieldFilter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterAddons();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				filterAddons();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				filterAddons();
			}
		});

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		// call onCancel() on ESCAPE
		panel1.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void filterAddons() {
		updateAddonList(textFieldFilter.getText());
	}

	private void onOK() {
		// add your code here
		java.util.List<Addon> checkedAddonId = new ArrayList<Addon>();
		for (int i = 0; i < tblAddon.getRowCount(); i++) {
			if((Boolean) tblAddon.getValueAt(i, 0)){
				Integer addonId = (Integer) tblAddon.getValueAt(i, 1);
				for (Addon addon : addons) {
					if (addonId == addon.getId()) {
						checkedAddonId.add(addon);
						break;
					}
				}
			}
		}
		DataSource.saveAddonList(checkedAddonId);
		dispose();
	}

	private void onCancel() {
		// add your code here if necessary
		dispose();
	}

	private void loadAddons() {
		try {
			Addon[] serverAddonList = NetHandler.getServerAddonList();
			Addon[] newAddonList = new Addon[serverAddonList.length];
			for (int i = 0; i < serverAddonList.length; i++) {
				Addon na = serverAddonList[i];
				for (Addon oa : addons) {
					if (oa.getId() == na.getId()) {
						if (oa.isSelected()) {
							na.setSelected(true);
						}
						break;
					}
				}
				newAddonList[i] = na;
			}
			addons = newAddonList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateAddonList();
	}

	private void updateAddonList() {
		updateAddonList("");
	}
	private void updateAddonList(String filter) {
		boolean filterOn = false;
		if(null != filter && filter.length() > 0){
			filterOn = true;
		}
		while (dtm.getRowCount() > 0) {
			dtm.removeRow(0);
		}
		for (Addon addon : addons) {
			boolean doDisplay = true;
			if(filterOn){
				doDisplay = String.valueOf(addon.getId()).contains(filter);
				if(addon.getName().contains(filter))
					doDisplay = true;
				if(addon.getUrl().contains(filter))
					doDisplay = true;
			}
			if(doDisplay)
				dtm.addRow(new Object[]{addon.isSelected(), addon.getId(), addon.getName(), addon.getUrl()});
		}

	}
}
