package com.addondowner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by johlar on 16/03/15.
 *
 */
public class Addon {
	int id;
	String name;
	String url;
	boolean selected;

	public Addon(int id, String name, String url) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.selected = false;
	}

	public Addon(int id, String name, String url, boolean selected) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.selected = selected;
	}

	public Addon(Integer addonId) {
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
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
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

	public static Addon fetchOnNameAndUrl(String name, String url) {
		Addon r = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DataSource.getInstance().getConnection();
			ps = conn.prepareStatement("SELECT id, name, main_page_url FROM addon_list WHERE name=? AND main_page_url=? ; ");
			ps.setString(1, name);
			ps.setString(2, url);
			rs = ps.executeQuery();
			while (rs.next()){
				r = new Addon(rs.getInt(1), rs.getString(2), rs.getString(3));
			}
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return r;
	}

	public static java.util.List<Addon> fetchAddonList(){
		java.util.List<Addon> addons = new ArrayList<Addon>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DataSource.getInstance().getConnection();
			ps = conn.prepareStatement("SELECT id,name, main_page_url FROM addon_list;");
			rs = ps.executeQuery();
			while (rs.next()) {
				addons.add(new Addon(rs.getInt(1), rs.getString(2), rs.getString(3)));
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			// show message e.getMessage();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return addons;
	}
}
