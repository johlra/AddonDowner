package com.addondowner;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

public class NewAddon extends JDialog {
	private JPanel contentPane;
	private JTextPane helpTextPane;
	private JTextField txtURL;
	private JButton btnFetch;
	private int progress;

	public NewAddon() {
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(btnFetch);

		btnFetch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnFetch.setEnabled(false);
				progress = 0;
				FetchNewAddonWorker fetchNewAddonWorker = new FetchNewAddonWorker(txtURL.getText(), true, helpTextPane);
				fetchNewAddonWorker.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if ("progress".equals(evt.getPropertyName())) {
							progress = (Integer) evt.getNewValue();
						} else if ("state".equals(evt.getPropertyName())){
							if("DONE".equalsIgnoreCase(String.valueOf(evt.getNewValue()))){
								btnFetch.setEnabled(true);
								if(progress == 100){
									//System.out.print("Close and go back");
									onOK();
								}
							} else if(!"STARTED".equalsIgnoreCase(String.valueOf(evt.getNewValue()))) {
								System.out.print("unhandled state = " + evt.getNewValue() + "\n");
							}
						} else {
							System.out.print("Unhandled property change: " + evt.getPropertyName() + " = " + evt.getNewValue() + "\n");
						}
						//System.out.print(String.format("Completed %d%%.\n", progress));
					}
				});
				fetchNewAddonWorker.execute();
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

		try {
			Addon[] addonsServer = NetHandler.getServerAddonList();
			List<Addon> localAddons = Addon.fetchAddonList();
			for (Addon la : localAddons) {
				boolean hasAddonOnServer = false;
				for (Addon sa : addonsServer) {
					if(la.getUrl().equalsIgnoreCase(sa.getUrl())){
						hasAddonOnServer = true;
					}
				}
				if(!hasAddonOnServer){
					txtURL.setText(la.getUrl());
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void onOK() {
		// add your code here
		dispose();
	}

	private void onCancel() {
		// add your code here if necessary
		dispose();
	}

	public static void main(String[] args) {
		NewAddon dialog = new NewAddon();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}
}
