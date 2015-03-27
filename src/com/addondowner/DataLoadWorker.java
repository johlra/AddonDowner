package com.addondowner;

import org.h2.jdbcx.JdbcConnectionPool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.List;

/**
 * Created by johlar on 12/03/15.
 *
 * DB DESIGN
 *
 * addon_list
 * ------------
 *  id
 *  name
 *  main_page_url
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
 */
public class DataLoadWorker extends SwingWorker<String, Addon> {

	private final DefaultTableModel tblAddon;

	public DataLoadWorker(DefaultTableModel tblAddon) {
		this.tblAddon = tblAddon;
	}

	@Override
	protected String doInBackground() throws Exception {
		try {
			JdbcConnectionPool cp = JdbcConnectionPool.create(AddonDowner.BD_CONNECTION, "sa", "sa");
			Connection conn = cp.getConnection();
			checkDBStruckt(conn);
			PreparedStatement preparedStatement = conn.prepareStatement("SELECT id,name, main_page_url FROM addon_list;");

			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				Addon addon = new Addon(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
				publish(addon);
			}

			resultSet.close();
			preparedStatement.close();
			conn.close();
			cp.dispose();
		} catch (SQLException e) {
			return e.getMessage();
		}
		return "Done";
	}

	@Override
	protected void process(List<Addon> chunks) {
		for (Addon addon : chunks) {
			tblAddon.addRow(new Object[]{"", addon.getId(), addon.getName(), addon.getMainPageUrl()});
		}
		//super.process(chunks);
	}

	@Override
	protected void done() {
		super.done();
	}

	private void checkDBStruckt(Connection conn) {
		try {
			boolean hasVersionTable = false;
			int dbVersion = 0;

			DatabaseMetaData connMetaData = conn.getMetaData();
			ResultSet tables = connMetaData.getTables(null, null, null, new String[]{"TABLE"});
			while (tables.next()) {
				//PreparedStatement ps = conn.prepareStatement("DROP TABLE " + tables.getString("TABLE_NAME"));
				//ps.execute();
				//ps.close();
				if ("db_version".equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
					hasVersionTable = true;
				}
			}
			if (hasVersionTable) {
				PreparedStatement ps = conn.prepareStatement("SELECT version FROM db_version;");
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					dbVersion = rs.getInt(1);
				}
				rs.close();
				ps.close();
			}

			for (int i = dbVersion; i < dbStruct.length; i++) {
				String sql = dbStruct[i];
				PreparedStatement ps = conn.prepareStatement(sql);
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
		}
	}

	String[] dbStruct = new String[]{
			"CREATE TABLE `db_version` (\n" +
					"  `version` int(11) unsigned NOT NULL,\n" +
					"  PRIMARY KEY (`version`)\n" +
					") ENGINE=InnoDB DEFAULT CHARSET=utf8;",
			"CREATE TABLE `addon_list` (\n" +
					"  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
					"  `name` varchar(255) NOT NULL DEFAULT '',\n" +
					"  `main_page_url` varchar(4000) NOT NULL DEFAULT '',\n" +
					"  PRIMARY KEY (`id`),\n" +
					") ENGINE=InnoDB DEFAULT CHARSET=utf8;",
			"CREATE TABLE `addon_version` (\n" +
					"  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
					"  `addon_list_id` int(11) unsigned NOT NULL,\n" +
					"  `version` varchar(255) NOT NULL DEFAULT '',\n" +
					"  `version_download_page` varchar(4000) DEFAULT NULL,\n" +
					"  `download_filename` varchar(4000) DEFAULT NULL,\n" +
					"  `installed` tinyint(4) NOT NULL DEFAULT '0',\n" +
					"  PRIMARY KEY (`id`),\n" +
					"  KEY `addon_list_id` (`addon_list_id`),\n" +
					"  CONSTRAINT `addon_version_ibfk_1` FOREIGN KEY (`addon_list_id`) REFERENCES `addon_list` (`id`)\n" +
					") ENGINE=InnoDB DEFAULT CHARSET=utf8;",
			"CREATE TABLE `prefs` (\n" +
					"  `name` varchar(50) NOT NULL DEFAULT '',\n" +
					"  `data` varchar(4000) DEFAULT NULL,\n" +
					"  PRIMARY KEY (`name`)\n" +
					") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
	};
}
