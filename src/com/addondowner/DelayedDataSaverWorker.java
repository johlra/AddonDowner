package com.addondowner;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by johlar on 12/03/15.
 *
 */
public class DelayedDataSaverWorker extends SwingWorker<String, Addon> {
	private static final int delay = 10000;

	private final String field;
	private final String data;

	public DelayedDataSaverWorker(String field, String data) {
		this.field = field;
		this.data = data;
	}

	@Override
	protected String doInBackground() throws Exception {
		Thread.sleep(delay);
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
		System.out.println("Delayed pref saved");
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