package com.addondowner;

public class AddonDowner {

	private static final String appName = "AddonDowner";
	public final static String BD_CONNECTION = "jdbc:h2:./addon_downer;MODE=MySQL";

	public final static String PREF_KEY_WOW_ADDON_DIR = "wowaddondir";
	public final static String PREF_KEY_WOW_LAUNCHER = "wowlauncher";
	public final static String PREF_KEY_AUTO_UPDATE_ON_LAUNCH = "autoupdateonlaunch";
	public final static String PREF_KEY_AUTO_QUIT_AFTER_UPDATE = "autoquitafterupdate";
	public final static String PREF_KEY_AUTO_START_LAUNCHER = "autostartlauncher";

	public final static int HTTP_TIMEOUT = 30 * 1000;

	public static void main(String[] args) {
	    // DOSEN'T WORK ON NEWER JAVA
	    // see http://www.oracle.com/technetwork/articles/javase/javatomac-140486.html for better way
	    System.setProperty("apple.laf.useScreenMenuBar", "true");
	    System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
	    // end dosen't work

	    MainWindow.main(new String[]{appName});
		System.out.println("Hello World!");
    }
}