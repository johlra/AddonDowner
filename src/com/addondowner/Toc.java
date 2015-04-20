package com.addondowner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johlar on 01/04/15.
 */
public class Toc {
	private String name;
	private String title;
	private String version;
	private Long crc;

	public Toc(String name, String title, String version, Long crc) {
		this.name = name;
		this.title = title;
		this.version = version;
		this.crc = crc;
	}

	public Toc(String fileName, StringBuilder fileContent, long crc) {
		String version = "";
		int versionIndex = fileContent.indexOf("## X-Curse-Packaged-Version:");
		if (versionIndex < 1) {
			versionIndex = fileContent.indexOf("## Version:");
		}
		if (versionIndex > 0) {
			version = removeColorMeta(fileContent.substring(fileContent.indexOf(":", versionIndex) + 1, fileContent.indexOf("\n", versionIndex))).trim();
		}

		String title = "";
		int titleIndex = fileContent.indexOf("## X-Curse-Project-Name:");
		if (titleIndex < 1) {
			titleIndex = fileContent.indexOf("Title:");
		}
		if (titleIndex > 0) {
			title = removeColorMeta(fileContent.substring(fileContent.indexOf(":", titleIndex) + 1, fileContent.indexOf("\n", titleIndex))).trim();
		}

		this.name = fileName;
		this.title = title;
		this.version = version;
		this.crc = crc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getCrc() {
		return crc;
	}

	public void setCrc(Long crc) {
		this.crc = crc;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public static String getUniqueVersion(List<Toc> tocs) {
		String version = "";
		if (tocs.size() == 1) {
			version = tocs.get(0).getVersion();
		} else {
			List<String> uniqueVersion = new ArrayList<String>();
			for (Toc toc : tocs) {
				if (!uniqueVersion.contains(toc.getVersion()) && !toc.getVersion().equals("")) {
					uniqueVersion.add(toc.getVersion());
				}
			}
			for (String ver : uniqueVersion) {
				if (version.length() > 0) {
					version += ", ";
				}
				version += ver;
			}
		}
		return version;
	}

	public static String getUniqueTitle(List<Toc> tocs) {
		String title = "";
		if (tocs.size() == 1) {
			title = tocs.get(0).getTitle();
		} else {
			List<String> uniqueTitle = new ArrayList<String>();
			for (Toc toc : tocs) {
				if (!uniqueTitle.contains(toc.getTitle()) && !toc.getTitle().equals("")) {
					uniqueTitle.add(toc.getTitle());
				}
			}
			for (String t : uniqueTitle) {
				if (title.length() > 0) {
					title += ", ";
				}
				title += t;
			}
		}
		return title;
	}

	private String removeColorMeta(String data) {
		// ## Title:|cffffe00a<|r|cffff7d0aDBM|r|cffffe00a>|r |cff69ccf0Blackrock Foundry|r
		return data.replaceAll("(\\|[0-9a-fA-F]{9})|(\\|r)", "");
	}

}
