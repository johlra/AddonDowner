package com.addondowner;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class ViewAddonList extends JDialog {
	private JPanel contentPane;
	private JButton buttonCancel;
	private JTextPane textPane1;
	private JButton importButton;
	private JTextPane helpTextPane;

	public ViewAddonList() {
		java.util.List<Addon> addons =  AddonDowner.allAddons;
		String list = "";
		for (Addon addon : addons) {
			list += addon.getId() + ";" + addon.getName() + ";" + addon.getUrl() + "\n";
		}
		textPane1.setText(list);

		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonCancel);

		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
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
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

	}

	private void onOK() {
		List<Addon> addons = new ArrayList<Addon>();
		String text = textPane1.getText();
		String[] lines = text.split("\n");
		List<FetchNewAddonWorker> workers = new ArrayList<FetchNewAddonWorker>();
		for (String line : lines) {
			String[] cols = line.split(";");
			int id = 0;
			String name = "";
			String url = "";
			if(cols.length == 3){
				try {
					id = Integer.parseInt(cols[0]);
				} catch (NumberFormatException e1) {
					// ignore wrong number
				}
				name = cols[1];
				url = cols[2];
			} else if(cols.length == 2){
				name = cols[0];
				url = cols[1];
			} else if(cols.length == 1){
				url = cols[0];
			}
			Addon addon = new Addon(0,"","");
			if(id > 0){
				addon = new Addon(name);
			}
			if(!name.equals("")){
				addon.setName(name);
			}
			if(!url.equals("")){
				addon.setUrl(url);
			}

			boolean hasAddon = false;
			for (int i = 0; i < AddonDowner.allAddons.size(); i++) {
				Addon listAddon = AddonDowner.allAddons.get(i);
				if(listAddon.getName().equalsIgnoreCase(addon.getName()) || listAddon.getUrl().equalsIgnoreCase(addon.getUrl())){
					hasAddon = true;
				}
			}

			if(!hasAddon){
				FetchNewAddonWorker fetchNewAddonWorker = new FetchNewAddonWorker(addon.getUrl(), false, helpTextPane);
				fetchNewAddonWorker.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if ("progress".equals(evt.getPropertyName())) {
						} else if ("state".equals(evt.getPropertyName())){
							if("DONE".equalsIgnoreCase(String.valueOf(evt.getNewValue()))){
							} else if(!"STARTED".equalsIgnoreCase(String.valueOf(evt.getNewValue()))) {
								System.out.print("unhandled state = " + evt.getNewValue() + "\n");
							}
						} else {
							System.out.print("Unhandled property change: " + evt.getPropertyName() + " = " + evt.getNewValue() + "\n");
						}
						//System.out.print(String.format("Completed %d%%.\n", progress));
					}
				});
				workers.add(fetchNewAddonWorker);
				fetchNewAddonWorker.execute();
			}
		}
		Addon.saveAddonListToJson(addons);

		boolean fetching = true;
		while (fetching){
			fetching = false;
			String progress = "";
			for (FetchNewAddonWorker fetchNewAddonWorker : workers) {
				progress += fetchNewAddonWorker.getProgressText();
				helpTextPane.setText(progress);
				helpTextPane.revalidate();
				helpTextPane.repaint();
				if (!fetchNewAddonWorker.isDone()) {
					fetching = true;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		dispose();
	}

	private void onCancel() {
		dispose();
	}

	public static void main(String[] args) {
		ViewAddonList dialog = new ViewAddonList();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}
}
