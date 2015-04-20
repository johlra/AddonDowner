package com.addondowner;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by johlar on 12/03/15.
 *
 */
public class UpdateToServerWorker extends SwingWorker<String, Addon> {

	private Addon addon;
	private final String url;
	private final String name;
	private String version;
	private List<String> topDirs;
	private List<Toc> tocs;

	public UpdateToServerWorker(Addon addon, String fileUrl, String fileName, String version, List<String> topDirs, List<Toc> tocs) {
		this.addon = addon;
		this.url = fileUrl;
		this.name = fileName;
		this.version = version;
		this.topDirs = topDirs;
		this.tocs = tocs;
	}

	@Override
	protected String doInBackground() throws Exception {

		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("addonId", String.valueOf(addon.getId()));
		params.put("addonName", addon.getName());
		params.put("fileName", name);
		params.put("fileUrl", url);
		params.put("version", version);

		for (int i = 0; i < topDirs.size(); i++) {
			params.put("dir" + i, topDirs.get(i));
		}

		for (int i = 0; i < tocs.size(); i++) {
			Toc toc = tocs.get(i);
			params.put("toc" + i + "name", toc.getName());
			params.put("toc" + i + "version", toc.getVersion());
			params.put("toc" + i + "crc", String.valueOf(toc.getCrc()));
		}

		NetHandler.serverUploadAddonMeta(params);

		return "Done";
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