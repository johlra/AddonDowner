package com.addondowner;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by johlar on 12/03/15.
 *
 */
public class FetchNewAddonWorker extends SwingWorker<String, Addon> {

	private final String url;
	private final JTextPane helpTextPane;
	private final boolean doServerUpload;
	private String progress = "";

	public FetchNewAddonWorker(String url, boolean doServerUpload, JTextPane helpTextPane) {
		this.url = url;
		this.doServerUpload = doServerUpload;
		this.helpTextPane = helpTextPane;
	}

	public String getProgressText() {
		return progress;
	}

	@Override
	protected String doInBackground() throws Exception {
		try {
			updateStatus("Searching for addon", 0);
			String fileUrl = NetHandler.getDataHrefFromUrl(url);
			updateStatus("Found file " + fileUrl, 40);
			updateStatus("Downloading...", 40);
			String fileName = NetHandler.fileDownloader(fileUrl);
			updateStatus("Have " + fileName, 80);

			updateStatus("Extracting info", 80);
			ZipHandler zipHandler = new ZipHandler(AddonDowner.TEMP_FILE_DIR + fileName);
			zipHandler.unzip();
			List<Toc> tocs = zipHandler.getTocs();
			String title = Toc.getUniqueTitle(tocs);
			System.out.println("Title: " + title);

			String version = zipHandler.getUniqueVersion();
			System.out.println("Version " + version);

			List<String> topDirs = zipHandler.getTopDirs();
			System.out.println(topDirs.toString());
			updateStatus("Found title: " + title, 85);

			if(doServerUpload) {
				updateStatus("Fetching id", 85);
				Map<String, String> params = new LinkedHashMap<String, String>();
				params.put("addonId", "new");
				params.put("addonName", title);
				params.put("addonUrl", url);
				params.put("fileName", fileName);
				params.put("fileUrl", fileUrl);
				params.put("version", version);

				for (int i = 0; i < topDirs.size(); i++) {
					params.put("dir" + i, topDirs.get(i));
				}

				for (int i = 0; i < tocs.size(); i++) {
					Toc toc = tocs.get(i);
					params.put("toc" + i + "name", toc.getName());
					params.put("toc" + i + "title", toc.getTitle());
					params.put("toc" + i + "version", toc.getVersion());
					params.put("toc" + i + "crc", String.valueOf(toc.getCrc()));
				}

				NetHandler.serverUploadAddonMeta(params);
			} else {
				updateStatus("Adding to list", 85);
				List<Addon> addons = new ArrayList<Addon>();
				addons.add(new Addon(0, title, url));
				DataSource.saveAddonList(addons);
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			updateStatus("Error downloading addon:\n" + e1.getMessage(), 1);//http://www.curse.com/addons/wow/omen-threat-mter/download
			//JOptionPane.showMessageDialog(helpTextPane, "Error downloading addon:\n" + e1.getMessage());
		}
		updateStatus("Done", 100);
		return "Done";
	}

	private void updateStatus(String progressMessage, int progress) {
		this.progress += progressMessage + "\n";
		helpTextPane.setText(this.progress);
		setProgress(progress);
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