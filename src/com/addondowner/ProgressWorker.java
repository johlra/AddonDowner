package com.addondowner;

import javax.swing.*;
import java.sql.*;
import java.util.List;

/**
 * Created by johlar on 12/03/15.
 *
 */
public class ProgressWorker extends SwingWorker<String, Addon> {

	private final List<UpdateWorker> updateWorkers;

	public ProgressWorker(List<UpdateWorker> updateWorkers) {
		this.updateWorkers = updateWorkers;
	}

	@Override
	protected String doInBackground() throws Exception {
		int nbrUpdate = updateWorkers.size();
		while (nbrUpdate > 0){
			Thread.sleep(250);
			nbrUpdate = 0;
			for (UpdateWorker updateWorker : updateWorkers) {
				if(updateWorker.getProgress() != 100){
					nbrUpdate++;
				}
			}
			System.out.println("workers left " + nbrUpdate);
		}
		System.out.print("All update workers done");

		Boolean doAutoQuit = false;
		try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection(AddonDowner.BD_CONNECTION, "sa", "sa");
			PreparedStatement ps = conn.prepareStatement("SELECT data FROM prefs WHERE name = ?;");
			ps.setString(1, AddonDowner.PREF_KEY_AUTO_QUIT_AFTER_UPDATE);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				doAutoQuit = Boolean.valueOf(rs.getString(1));
			}
			rs.close();
			ps.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if(doAutoQuit){
			System.out.println(" quiting");
			System.exit(0);
		} else {
			System.out.println("");
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