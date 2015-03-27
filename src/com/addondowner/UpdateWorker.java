package com.addondowner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
		int rowWalker = 0;
		while (tblAddon.getRowCount() > rowWalker) {
			String name = (String) tblAddon.getValueAt(rowWalker, 2);
			String url = (String) tblAddon.getValueAt(rowWalker, 3);
			if(addon.getName().equalsIgnoreCase(name) && addon.getMainPageUrl().equalsIgnoreCase(url)){
				break;
			}
			rowWalker++;
		}

		setProgress(1);
		tblAddon.setValueAt("Checking", rowWalker, 0);

		System.out.println("Doing " + addon.getName() + " from " + addon.getMainPageUrl());
		try {
			String fileurl = getDataHrefFromUrl(addon.getMainPageUrl());
			String fileName = fileurl.substring(fileurl.lastIndexOf("/")+1);
			setProgress(10);

			System.out.println("File " + fileName + " from " + fileurl);

			List<AddonVersion> avs = new ArrayList<AddonVersion>();

			Class.forName("org.h2.Driver");
			java.sql.Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION, "sa", "sa");
			PreparedStatement ps = conn.prepareStatement("SELECT addon_list_id, version, version_download_page, download_filename, installed FROM addon_version " +
					"WHERE addon_list_id = ? AND installed = 1; ");
			ps.setInt(1, addon.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				 avs.add(new AddonVersion(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBoolean(5)));
			}
			ps.close();

			boolean hasVersion = false;
			for (AddonVersion av : avs) {
				if (av.getDownloadFilename().equalsIgnoreCase(fileName)) {
					hasVersion = true;
				}
			}
			setProgress(20);
			if (hasVersion) {
				tblAddon.setValueAt("Up to date", rowWalker, 0);
				setProgress(99);
			} else {
				// Downloading
				tblAddon.setValueAt("Downloading", rowWalker, 0);

				System.out.println("Downloading " + fileurl);
				Connection.Response fileResponse = Jsoup.connect(fileurl).maxBodySize(0).ignoreContentType(true).timeout(AddonDowner.HTTP_TIMEOUT).execute();
				FileOutputStream out = (new FileOutputStream(new java.io.File("./" + fileName)));
				out.write(fileResponse.bodyAsBytes());
				out.close();
				setProgress(50);

				String extractDir = "";
				ps = conn.prepareStatement("SELECT data FROM prefs WHERE name = ?;");
				ps.setString(1, AddonDowner.PREF_KEY_WOW_ADDON_DIR);
				rs = ps.executeQuery();
				if(rs.next()){
					extractDir = rs.getString(1);
				}
				ps.close();

				tblAddon.setValueAt("Extracting", rowWalker, 0);
				System.out.println("Unziping ./" + fileName + " to " + extractDir);
				try {
					unzip("./" + fileName, extractDir);
					ps = conn.prepareStatement("REPLACE INTO addon_version (addon_list_id, version, version_download_page, download_filename, installed) " + "VALUES (?,?,?,?,?)");
					ps.setInt(1, addon.getId());
					ps.setString(2, "");
					ps.setString(3, fileurl);
					ps.setString(4, fileName);
					ps.setInt(5, 1);
					ps.executeUpdate();
					tblAddon.setValueAt("Updated", rowWalker, 0);
				} catch (IOException e) {
					tblAddon.setValueAt("Error in zip", rowWalker, 0);
					System.out.println("Unzip exception; " + e.getMessage());
					e.printStackTrace();
				}
				setProgress(99);
			}
			conn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			tblAddon.setValueAt("Error: " + e.getMessage(), rowWalker, 0);
		} catch (SQLException e) {
			e.printStackTrace();
			tblAddon.setValueAt("Error: " + e.getMessage(), rowWalker, 0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			tblAddon.setValueAt("Error: " + e.getMessage(), rowWalker, 0);
		} catch (IOException e) {
			e.printStackTrace();
			tblAddon.setValueAt("Error: " + e.getMessage(), rowWalker, 0);
		}

		setProgress(100);
		return "Done";
	}

	private static final int BUFFER_SIZE = 4096;

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 *
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	private void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdirs();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * Extracts a zip entry (file entry)
	 *
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		int lastIndexOf = filePath.lastIndexOf("/");
		String path = filePath.substring(0, lastIndexOf);
		File dir = new File(path);
		dir.mkdirs();

		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	private String getDataHrefFromUrl(String url) throws IOException {
		String data = "";
		Map<String, String> cookies = new HashMap<String, String>();
		Connection.Response response = null;
		String referrer = "http://www.google.com";
		while (url.length() > 1){
			Connection connect = Jsoup.connect(url).
					followRedirects(false)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:36.0) Gecko/20100101 Firefox/36.0")
					.referrer(referrer)
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
					.timeout(AddonDowner.HTTP_TIMEOUT);
			if(cookies.size() > 0){
				for (Map.Entry<String, String> entry : cookies.entrySet()) {
					connect = connect.cookie(entry.getKey(), entry.getValue());
					//System.out.println(entry.getKey() + " = " + entry.getValue());
				}
			}
			response = connect.execute();
			Map<String, String> cookies1 = response.cookies();
			for (Map.Entry<String, String> entry : cookies1.entrySet()) {
				cookies.put(entry.getKey(), entry.getValue());
				//System.out.println("Adding cookie: " + entry.getKey() + " = " + entry.getValue());
			}
			String responseLocation = response.header("Location");
			if (null != responseLocation && responseLocation.length() > 2) {
				referrer = url;
				if(responseLocation.startsWith("/")){
					url = url.substring(0, url.indexOf("/", url.indexOf("/", url.indexOf("/") + 1) + 1)) + responseLocation;
				} else if(responseLocation.startsWith("http://") || responseLocation.startsWith("https://")) {
					url = responseLocation;
				} else {
					url = url + responseLocation;
				}
				//System.out.println("Found redirect: " + url);
			} else {
				url = "";
			}
		}

		if(null != response){
			Document document = response.parse();
			Elements select = document.select("a[data-href]");
			if(select.size() > 0){
				Element first = select.first();
				data = first.attr("data-href");
			} else {
				Elements links = document.select("a[href]");
				for (Element link : links) {
					String href = link.attr("href");
					if (href.contains("downloads/elvui")) {
						data = href;
						break;
					}
				}
			}
		}
		return data;
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