package com.addondowner;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;
import java.util.List;

/**
 * Created by johlar on 31/03/15.
 *
 * DB DESIGN
 *
 * addon_list
 * ------------
 *  id
 *  name
 *  main_page_url
 ?? using
 *
 * addon_version
 * --------------
 *  addon_list_id
 *  version
 *  version_download_page
 *  download_filename
 *  installed
 *
 * prefs
 * -----------
 *  name
 *  data
 *
 */

public class DataSource {
	private static DataSource datasource;
	private JdbcConnectionPool cp;

	private DataSource() throws ClassNotFoundException {
		Class.forName("org.h2.Driver");
		cp = JdbcConnectionPool.create(AddonDowner.BD_CONNECTION, "sa", "sa");
	}

	public static DataSource getInstance() throws ClassNotFoundException {
		if (datasource == null) {
			datasource = new DataSource();
			return datasource;
		} else {
			return datasource;
		}
	}

	public Connection getConnection() throws SQLException {
		return this.cp.getConnection();
	}

	public static void checkDBStruckt() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DataSource.getInstance().getConnection();
			boolean hasVersionTable = false;
			int dbVersion = 0;

			DatabaseMetaData connMetaData = conn.getMetaData();
			rs = connMetaData.getTables(null, null, null, new String[]{"TABLE"});
			while (rs.next()) {
				//ps = conn.prepareStatement("DROP TABLE " + rs.getString("TABLE_NAME"));
				//ps.execute();
				//ps.close();
				if ("db_version".equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
					hasVersionTable = true;
				}
			}
			if (hasVersionTable) {
				ps = conn.prepareStatement("SELECT version FROM db_version;");
				rs = ps.executeQuery();
				if (rs.next()) {
					dbVersion = rs.getInt(1);
				}
				rs.close();
				ps.close();
			}

			for (int i = dbVersion; i < dbStruct.length; i++) {
				String sql = dbStruct[i];
				ps = conn.prepareStatement(sql);
				ps.execute();
				ps.close();

				if (i == 0) {
					ps = conn.prepareStatement("INSERT INTO db_version (version) VALUES (?); ");
					ps.setInt(1, i);
					ps.execute();
					ps.close();
				}

				ps = conn.prepareStatement("UPDATE db_version SET version = ?; ");
				ps.setInt(1, i + 1);
				ps.execute();
				ps.close();
			}
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

	private final static String[] dbStruct = new String[]{"CREATE TABLE `db_version` (\n" +
			"  `version` int(11) unsigned NOT NULL,\n" +
			"  PRIMARY KEY (`version`)\n" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;", "CREATE TABLE `addon_list` (\n" +
			"  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
			"  `name` varchar(255) NOT NULL DEFAULT '',\n" +
			"  `main_page_url` varchar(4000) NOT NULL DEFAULT '',\n" +
			"  PRIMARY KEY (`id`),\n" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;", "CREATE TABLE `addon_version` (\n" +
			"  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
			"  `addon_list_id` int(11) unsigned NOT NULL,\n" +
			"  `version` varchar(255) NOT NULL DEFAULT '',\n" +
			"  `version_download_page` varchar(4000) DEFAULT NULL,\n" +
			"  `download_filename` varchar(4000) DEFAULT NULL,\n" +
			"  `installed` tinyint(4) NOT NULL DEFAULT '0',\n" +
			"  PRIMARY KEY (`id`),\n" +
			"  KEY `addon_list_id` (`addon_list_id`),\n" +
			"  CONSTRAINT `addon_version_ibfk_1` FOREIGN KEY (`addon_list_id`) REFERENCES `addon_list` (`id`)\n" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;", "CREATE TABLE `prefs` (\n" +
			"  `name` varchar(50) NOT NULL DEFAULT '',\n" +
			"  `data` varchar(4000) DEFAULT NULL,\n" +
			"  PRIMARY KEY (`name`)\n" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"};

	public static String getPref(String prefKey) {
		String ret = "";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getInstance().getConnection();
			ps = conn.prepareStatement("SELECT data FROM prefs WHERE name = ?;");
			ps.setString(1, prefKey);
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getString(1);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
		}
		return ret;
	}

	public static void saveAddonList(List<Addon> addons) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getInstance().getConnection();
			ps = conn.prepareStatement("REPLACE INTO addon_list (id, name, main_page_url) VALUES (?,?,?) ; ");
			for (Addon addon : addons) {
				ps.setInt(1, addon.getId());
				ps.setString(2, addon.getName());
				ps.setString(3, addon.getUrl());
				ps.execute();
			}
			ps.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
		}
	}
}
