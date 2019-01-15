package com.addondowner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AddonDowner {

	private static final String appName = "AddonDowner";
	public final static String BD_CONNECTION = "jdbc:h2:." + File.separator + "addon_downer;MODE=MySQL";
/*
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
*/
	public final static String TEMP_FILE_DIR = "." + File.separator + "download_cache" + File.separator;

	public final static String CONFIG_FILE = "." + File.separator + "config.json";
	public final static String ADDON_LIST_FILE = "." + File.separator + "addons.json";

	public final static int HTTP_TIMEOUT = 30 * 1000;

	public static java.util.List<Addon> allAddons = new ArrayList<Addon>();
	public static Preference[] allPrefs = new Preference[0];

	public static void main(String[] args) {
	    // DOSEN'T WORK ON NEWER JAVA
	    // see http://www.oracle.com/technetwork/articles/javase/javatomac-140486.html for better way
	    System.setProperty("apple.laf.useScreenMenuBar", "true");
	    System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
	    // end dosen't work

		try {
			allPrefs = Preference.getAllPrefs();
			allAddons = Addon.getAddonListFromJson();
			// Convert from old format of save
			/*
			if(new File("addon_downer.mv.db").exists()) {
				if (allPrefs.length == 0) {
					allPrefs = Preference.getPrefsFromDB();
					Preference.saveAllPrefs(allPrefs);
				}
				if (allAddons.size() == 0) {
					allAddons = Addon.fetchAddonListFromDB();
					Addon.saveAddonListToJson(allAddons);
				}
			}*/
			if(NetHandler.checkForNetwork()){
				MainWindow.main(new String[]{appName});
			} else {
				System.out.println("Quiting, error, no network");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
