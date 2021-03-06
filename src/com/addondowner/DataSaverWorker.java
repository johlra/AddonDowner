package com.addondowner;

import javax.swing.*;
import java.sql.*;
import java.util.List;

/**
 * Created by johlar on 12/03/15.
 *
 */
public class DataSaverWorker extends SwingWorker<String, Addon> {

	private final String field;
	private final String data;

	public DataSaverWorker(String field, String data) {
		this.field = field;
		this.data = data;
	}

	@Override
	protected String doInBackground() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DataSource.getInstance().getConnection();
			ps = conn.prepareStatement("REPLACE INTO prefs (name, data) VALUES (?,?) ; ");
			ps.setString(1, field);
			ps.setString(2, data);
			ps.execute();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DataSource.closeQuietly(null, ps, conn);
		}
		return "Done";
	}

	@Override
	protected void process(List<Addon> chunks) {
		super.process(chunks);
	}
	@Override
	protected void done() {
		super.done();
	}
}