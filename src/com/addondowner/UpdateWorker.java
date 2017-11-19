package com.addondowner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.List;

/**
 * Created by johlar on 12/03/15.
 *
 */
public class UpdateWorker extends SwingWorker<String, Addon> {

	private final DefaultTableModel tblAddon;
	private final Addon addon;

	public UpdateWorker(DefaultTableModel tblAddon, Addon addon) {
		this.tblAddon = tblAddon;
		this.addon = addon;
	}

	@Override
	protected String doInBackground() {
		Date start = new Date();
		setProgress(0);
		System.out.println("Doing " + addon.getName() + " from " + addon.getUrl());
		int rowWalker = 0;
		while (tblAddon.getRowCount() > rowWalker) {
			String name = (String) tblAddon.getValueAt(rowWalker, 2);
			String url = (String) tblAddon.getValueAt(rowWalker, 3);
			if(addon.getName().equalsIgnoreCase(name) && addon.getUrl().equalsIgnoreCase(url)){
				break;
			}
			rowWalker++;
		}

		setProgress(1);
		SetProgress(rowWalker, "Checking");

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String fileUrl = NetHandler.getDataHrefFromUrl(addon.getUrl());
			setProgress(10);

			System.out.println("File from " + fileUrl);

			boolean hasVersion = false;
			conn = DataSource.getInstance().getConnection();
			ps = conn.prepareStatement("SELECT version, version_download_page FROM addon_version " +
				"WHERE addon_list_id = ? AND installed = 1 AND version_download_page = ?; ");
			ps.setInt(1, addon.getId());
			ps.setString(2, fileUrl);
			rs = ps.executeQuery();
			if (rs.next()) {
				hasVersion = true;
			}
			ps.close();
			setProgress(20);
			if (hasVersion) {
				SetProgress(rowWalker, "Up to date");
				setProgress(99);
			} else {
				// Downloading
				SetProgress(rowWalker, "Downloading");

				System.out.println("Downloading " + fileUrl);
				String fileName = NetHandler.fileDownloader(fileUrl);
				setProgress(50);

				String extractDir = "";
				ps = conn.prepareStatement("SELECT data FROM prefs WHERE name = ?;");
				ps.setString(1, AddonDowner.PREF_KEY_WOW_ADDON_DIR);
				rs = ps.executeQuery();
				if(rs.next()){
					extractDir = rs.getString(1);
				}
				ps.close();
				setProgress(52);

				SetProgress(rowWalker, "Extracting");
				System.out.println("Unziping " + AddonDowner.TEMP_FILE_DIR + fileName + " to " + extractDir);
				try {
					ZipHandler zipHandler = new ZipHandler(AddonDowner.TEMP_FILE_DIR + fileName, extractDir);
					zipHandler.unzip();
					String version = zipHandler.getUniqueVersion();
					setProgress(96);
					ps = conn.prepareStatement("REPLACE INTO addon_version (addon_list_id, version, version_download_page, download_filename, installed) VALUES (?,?,?,?,?)");
					ps.setInt(1, addon.getId());
					ps.setString(2, version);
					ps.setString(3, fileUrl);
					ps.setString(4, fileName);
					ps.setInt(5, 1);
					ps.executeUpdate();
					SetProgress(rowWalker, "Updated");
					UpdateToServerWorker updateToServerWorker = new UpdateToServerWorker(addon, fileUrl, fileName, version, zipHandler.getTopDirs(), zipHandler.getTocs());
					updateToServerWorker.execute();
				} catch (IOException e) {
					SetProgress(rowWalker, "Error in zip");
					System.out.println("Unzip exception; " + e.getMessage());
					e.printStackTrace();
				}
				setProgress(98);

				setProgress(99);
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			SetProgress(rowWalker, "Error: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			SetProgress(rowWalker, "Error: " + e.getMessage());
		} catch (FileNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			SetProgress(rowWalker, "Error: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			SetProgress(rowWalker, "Error: " + e.getMessage());
		} finally {
			DataSource.closeQuietly(rs, ps, conn);
		}

		System.out.println("Check/Update of " + addon.getName() + " took " + (new Date().getTime() - start.getTime()) + " ms");
		setProgress(100);
		return "Done";
	}

	private void SetProgress(int rowWalker, String status) {
		if(tblAddon.getRowCount() > 0){
			tblAddon.setValueAt(status, rowWalker, 0);
		}
	}

	@Override
	protected void process(List<Addon> chunks) {
		super.process(chunks);
	}
	@Override
	protected void done() {
		super.done();
	}
}