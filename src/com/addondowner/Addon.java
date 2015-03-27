package com.addondowner;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by johlar on 16/03/15.
 */
public class Addon {
	int id;
	String name;
	String mainPageUrl;

	public Addon(int id, String name, String mainPageUrl) {
		this.id = id;
		this.name = name;
		this.mainPageUrl = mainPageUrl;
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

	public String getMainPageUrl() {
		return mainPageUrl;
	}

	public void setMainPageUrl(String mainPageUrl) {
		this.mainPageUrl = mainPageUrl;
	}

	public static Addon fetchOnNameAndUrl(String name, String url) {
		Addon r = null;
		try {
			Class.forName("org.h2.Driver");
			java.sql.Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION, "sa", "sa");

			PreparedStatement ps = conn.prepareStatement("SELECT id, name, main_page_url FROM addon_list WHERE name=? AND main_page_url=? ; ");
			ps.setString(1, name);
			ps.setString(2, url);
			ResultSet rs = ps.executeQuery();
			while (rs.next()){
				r = new Addon(rs.getInt(1), rs.getString(2), rs.getString(3));
			}
			ps.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return r;
	}
}
