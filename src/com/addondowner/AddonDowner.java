package com.addondowner;

import java.io.File;

public class AddonDowner {

	private static final String appName = "AddonDowner";
	public final static String BD_CONNECTION = "jdbc:h2:." + File.separator + "addon_downer;MODE=MySQL";

	public final static String PREF_KEY_WOW_ADDON_DIR = "wowaddondir";
	public final static String PREF_KEY_CMD_BEFORE = "wowlauncher";
	public final static String PREF_KEY_CMD_AFTER = "cmdafter";
	public final static String PREF_KEY_AUTO_UPDATE_ON_LAUNCH = "autoupdateonlaunch";
	public final static String PREF_KEY_AUTO_QUIT_AFTER_UPDATE = "autoquitafterupdate";
	public final static String PREF_KEY_DO_CMD_BEFORE = "autostartlauncher";
	public final static String PREF_KEY_DO_CMD_AFTER = "docmdafterupdate";
	public final static String PREF_KEY_WINDOW_POS = "windowpos";
	public final static String PREF_KEY_WINDOW_SIZE = "windowsize";
	public final static String PREF_KEY_SERVER_HOST = "serverhostaddress";

	public static final String TEMP_FILE_DIR = "." + File.separator + "download_cache" + File.separator;

	public final static int HTTP_TIMEOUT = 30 * 1000;

	public static void main(String[] args) {
	    // DOSEN'T WORK ON NEWER JAVA
	    // see http://www.oracle.com/technetwork/articles/javase/javatomac-140486.html for better way
	    System.setProperty("apple.laf.useScreenMenuBar", "true");
	    System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
	    // end dosen't work
		if(NetHandler.checkForNetwork()){
			try {
				Class.forName("org.h2.Driver");
				DataSource.checkDBStruckt();
				MainWindow.main(new String[]{appName});
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Quiting, error, no network");
		}
    }
}
