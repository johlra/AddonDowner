package com.addondowner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * Created by johlra on 2018-04-27.
 */
public class Preference {
	String name;
	Object data;

	public Preference(String name, Object data) {
		this.name = name;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public static void saveAllPrefs(Preference[] prefs){
		Preference[] conf = fillOutSoWeHaveAllPrefs(prefs);
		try(Writer writer = new OutputStreamWriter(new FileOutputStream(AddonDowner.CONFIG_FILE) , "UTF-8")){
			Gson gson = new GsonBuilder().create();
			gson.toJson(conf, writer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static  Preference[] fillOutSoWeHaveAllPrefs (Preference[] prefs){
		final String[] allPrefsKey = new String[]{"http_timeout",
				"wow_addon_dir",
				"cmd_before",
				"do_cmd_before",
				"cmd_after",
				"do_cmd_after",
				"auto_update_on_launch",
				"auto_quit_after_update",
				"window_pos",
				"window_size",
				"network_check_url"};
		if(null == prefs){
			prefs = new Preference[0];
		}
		if(prefs.length == allPrefsKey.length) {
			return prefs;
		} else {
			Preference[] ret = new Preference[allPrefsKey.length];
			for (int i = 0; i < allPrefsKey.length; i++) {
				String key = allPrefsKey[i];
				boolean hasPref = false;
				for (Preference pref : prefs) {
					if (key.equalsIgnoreCase(pref.getName())) {
						hasPref = true;
						ret[i] = pref;
					}
				}
				if(!hasPref){
					Object value;
					switch (key) {
						case "http_timeout":
							value = 30000;
							break;
						case "do_cmd_before":
						case "do_cmd_after":
						case "auto_update_on_launch":
						case "auto_quit_after_update":
							value = false;
							break;
						case "wow_addon_dir":
							value = "C:\\Program Files (x86)\\World of Warcraft\\Interface\\AddOns";
							break;
						case "network_check_url":
							value = "www.curseforge.com";
							break;
						case "window_size":
							value = "800;500";
							break;
						default:
							value = "";
							break;
					}
					ret[i] = new Preference(key, value);
				}
			}
			return ret;
		}
	}

	public static Preference[] getAllPrefs() throws IOException {
		File configFile = new File(AddonDowner.CONFIG_FILE);
		boolean fileExist = false;
		if(!configFile.exists()){
			if(configFile.createNewFile()){
				fileExist = true;
			}
		} else {
			fileExist = true;
		}
		if(fileExist){
			try(Reader reader = new InputStreamReader(new FileInputStream(configFile), "UTF-8")){
				Gson gson = new GsonBuilder().create();
				return fillOutSoWeHaveAllPrefs(gson.fromJson(reader, Preference[].class));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new Preference[0];
	}

	public static int HTTP_TIMEOUT() {
		return (int) getPref("http_timeout");
	}

	public static String WOW_ADDON_DIR() {
		return (String) getPref("wow_addon_dir");
	}
	public static void WOW_ADDON_DIR(String value) {
		setPref("wow_addon_dir",value);
	}

	public static String CMD_BEFORE() {
		return (String) getPref("cmd_before");
	}
	public static void CMD_BEFORE(String value) {
		setPref("cmd_before", value);
	}

	public static boolean DO_CMD_BEFORE() {
		return (boolean) getPref("do_cmd_before");
	}
	public static void DO_CMD_BEFORE(boolean value) {
		setPref("do_cmd_before",value);
	}

	public static String CMD_AFTER() {
		return (String) getPref("cmd_after");
	}
	public static void CMD_AFTER(String value) {
		setPref("cmd_after",value);
	}

	public static boolean DO_CMD_AFTER() {
		return (boolean) getPref("do_cmd_after");
	}
	public static void DO_CMD_AFTER(boolean value) {
		setPref("do_cmd_after",value);
	}

	public static boolean AUTO_UPDATE_ON_LAUNCH() {
		return (boolean) getPref("auto_update_on_launch");
	}
	public static void AUTO_UPDATE_ON_LAUNCH(boolean value) {
		setPref("auto_update_on_launch",value);
	}

	public static boolean AUTO_QUIT_AFTER_UPDATE() {
		return (boolean) getPref("auto_quit_after_update");
	}
	public static void AUTO_QUIT_AFTER_UPDATE(boolean value) {
		setPref("auto_quit_after_update",value);
	}

	public static String WINDOW_POS() {
		return (String) getPref("window_pos");
	}
	public static void WINDOW_POS(String value) {
		setPref("window_pos", value);
	}

	public static String WINDOW_SIZE() {
		return (String) getPref("window_size");
	}
	public static void WINDOW_SIZE(String value) {
		setPref("window_size",value);
	}

	public static String NETWORK_CHECK_URL() {
		return (String) getPref("network_check_url");
	}

	private static void setPref(String prefName, Object value) {
		for (int i = 0; i < AddonDowner.allPrefs.length; i++) {
			Preference pref = AddonDowner.allPrefs[i];
			if(pref.getName().equalsIgnoreCase(prefName)){
				pref.setData(value);
			}
		}
	}


	private static Object getPref(String prefName) {
		for (int i = 0; i < AddonDowner.allPrefs.length; i++) {
			Preference pref = AddonDowner.allPrefs[i];
			if(pref.getName().equalsIgnoreCase(prefName)){
				return pref.getData();
			}
		}
		return null;
	}
/*
	public static Preference[] getPrefsFromDB(){
		Preference[] prefs = new Preference[9];
		prefs[0] = new Preference("wow_addon_dir", DataSource.getPref(AddonDowner.PREF_KEY_WOW_ADDON_DIR));
		prefs[1] = new Preference("cmd_before", DataSource.getPref(AddonDowner.PREF_KEY_CMD_BEFORE));
		prefs[2] = new Preference("cmd_after", DataSource.getPref(AddonDowner.PREF_KEY_CMD_AFTER));
		prefs[3] = new Preference("auto_update_on_launch", "true".equalsIgnoreCase(DataSource.getPref(AddonDowner.PREF_KEY_AUTO_UPDATE_ON_LAUNCH)));
		prefs[4] = new Preference("auto_quit_after_update", "true".equalsIgnoreCase(DataSource.getPref(AddonDowner.PREF_KEY_AUTO_QUIT_AFTER_UPDATE)));
		prefs[5] = new Preference("do_cmd_before", "true".equalsIgnoreCase(DataSource.getPref(AddonDowner.PREF_KEY_DO_CMD_BEFORE)));
		prefs[6] = new Preference("do_cmd_after", "true".equalsIgnoreCase(DataSource.getPref(AddonDowner.PREF_KEY_DO_CMD_AFTER)));
		prefs[7] = new Preference("window_pos", DataSource.getPref(AddonDowner.PREF_KEY_WINDOW_POS));
		prefs[8] = new Preference("window_size", DataSource.getPref(AddonDowner.PREF_KEY_WINDOW_SIZE));
		return fillOutSoWeHaveAllPrefs(prefs);
	}*/

}
