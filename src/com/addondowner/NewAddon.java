package com.addondowner;

import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NewAddon extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextPane selectTypeFromDropTextPane;
	private JTextField txtName;
	private JTextField txtURL;

	public NewAddon() {
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		buttonOK.addActionListener(new ActionListener() {
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
		// add your code here
		String name = txtName.getText();
		String url = txtURL.getText();
		//JOptionPane.showMessageDialog(buttonOK, "Do save: " + downloadMethodId + " : " + name + " : " + url);

		try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION, "sa", "sa");

			PreparedStatement ps = conn.prepareStatement("INSERT INTO addon_list (name, main_page_url) VALUES (?,?) ; ");
			ps.setString(1, name);
			ps.setString(2, url);
			ps.execute();
			ps.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
