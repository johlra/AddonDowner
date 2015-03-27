package com.addondowner;

/**
 * Created by johlar on 23/03/15.
 */
public class AddonVersion {

	int addonListId;
	String version;
	String versionDownloadPage;
	String downloadFilename;
	boolean installed;

	public AddonVersion(int addonListId, String version, String versionDownloadPage, String downloadFilename, boolean installed) {
		this.addonListId = addonListId;
		this.version = version;
		this.versionDownloadPage = versionDownloadPage;
		this.downloadFilename = downloadFilename;
		this.installed = installed;
	}

	public int getAddonListId() {
		return addonListId;
	}

	public void setAddonListId(int addonListId) {
		this.addonListId = addonListId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersionDownloadPage() {
		return versionDownloadPage;
	}

	public void setVersionDownloadPage(String versionDownloadPage) {
		this.versionDownloadPage = versionDownloadPage;
	}

	public String getDownloadFilename() {
		return downloadFilename;
	}

	public void setDownloadFilename(String downloadFilename) {
		this.downloadFilename = downloadFilename;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}
}
