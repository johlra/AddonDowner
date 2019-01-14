package com.addondowner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by johlar on 16/03/15.
 *
 */
public class Addon implements Comparable<Addon> {
	int id;
	String name;
	String url;
	String versionDownloadPage;
	String version;
	boolean selected;

	public Addon(int id, String name, String url) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.versionDownloadPage = "";
		this.version = "";
		this.selected = false;
	}

	public Addon(int id, String name, String url, String versionDownloadPage, String version) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.versionDownloadPage = versionDownloadPage;
		this.version = version;
		this.selected = false;
	}

	public Addon(String name, String url, String versionDownloadPage) {
		this.name = name;
		this.url = url;
		this.versionDownloadPage = versionDownloadPage;
	}

	public Addon(int id, String name, String url, boolean selected) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.selected = selected;
	}

	public Addon(String addonName) {
		for (int i = 0; i < AddonDowner.allAddons.size(); i++) {
			Addon addon = AddonDowner.allAddons.get(i);
			if(addon.getName().equalsIgnoreCase(addonName)){
				this.id = addon.getId();
				this.name = addon.getName();
				this.url = addon.getUrl();
				this.versionDownloadPage = addon.getVersionDownloadPage();
				this.version = addon.getVersionDownloadPage();
				this.selected = false;
			}
		}

/*
		this.id = 0;
		this.name = "";
		this.url = "";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DataSource.getInstance().getConnection();
			ps = conn.prepareStatement("SELECT id, name, main_page_url FROM addon_list WHERE id = ? ; ");
			ps.setInt(1, addonId);
			rs = ps.executeQuery();
			if (rs.next()) {
				this.id = rs.getInt(1);
				this.name = rs.getString(2);
				this.url = rs.getString(3);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DataSource.closeQuietly(rs, ps, conn);
		}*/
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getVersionDownloadPage() {
		return versionDownloadPage;
	}

	public void setVersionDownloadPage(String versionDownloadPage) {
		this.versionDownloadPage = versionDownloadPage;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public static Addon fetchOnNameOrUrl(String name, String url) {
		Addon r = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DataSource.getInstance().getConnection();
			ps = conn.prepareStatement("SELECT id, name, main_page_url FROM addon_list WHERE name=? OR main_page_url=? ; ");
			ps.setString(1, name);
			ps.setString(2, url);
			rs = ps.executeQuery();
			while (rs.next()){
				r = new Addon(rs.getInt(1), rs.getString(2), rs.getString(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DataSource.closeQuietly(rs, ps, conn);
		}
		return r;
	}

	public static java.util.List<Addon> fetchAddonListFromDB(){
		java.util.List<Addon> addons = new ArrayList<Addon>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DataSource.getInstance().getConnection();
			ps = conn.prepareStatement("SELECT al.id, al.name, al.main_page_url FROM addon_list al ORDER BY al.id;");
			rs = ps.executeQuery();
			while (rs.next()) {
				addons.add(new Addon(rs.getInt(1), rs.getString(2), rs.getString(3)));
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			// show message e.getMessage();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DataSource.closeQuietly(rs, ps, conn);
		}
		return addons;
	}

	public static void saveAddonListToJson(java.util.List<Addon> addonList){
		try(Writer writer = new OutputStreamWriter(new FileOutputStream(AddonDowner.ADDON_LIST_FILE) , "UTF-8")){
			Gson gson = new GsonBuilder().create();
			gson.toJson(addonList, writer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static java.util.List<Addon> getAddonListFromJson(){
		java.util.List<Addon> ret = new ArrayList<Addon>();
		try(Reader reader = new InputStreamReader(new FileInputStream(AddonDowner.ADDON_LIST_FILE), "UTF-8")){
			Gson gson = new GsonBuilder().create();
			Addon[] addonList = gson.fromJson(reader, Addon[].class);
			Collections.addAll(ret, addonList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public int compareTo(Addon a) {
		int nameDiff = name.compareToIgnoreCase(a.getName());
		if(nameDiff != 0){
			return nameDiff;
		}
		int urlDiff = url.compareToIgnoreCase(a.getUrl());
		if(urlDiff != 0){
			return urlDiff;
		}
		return versionDownloadPage.compareToIgnoreCase(a.getVersionDownloadPage());
	}
}
