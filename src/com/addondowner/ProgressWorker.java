package com.addondowner;

import javax.swing.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by johlar on 12/03/15.
 *
 */
public class ProgressWorker extends SwingWorker<String, Addon> {

	private final List<UpdateWorker> updateWorkers;
	private final Date startTime;

	public ProgressWorker(List<UpdateWorker> updateWorkers, Date start) {
		this.updateWorkers = updateWorkers;
		this.startTime = start;
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
			//TODO Need to check all UpdateToServerWorker too

			//System.out.println("workers left " + nbrUpdate);
		}


		System.out.print("All update workers done, total time " + (new Date().getTime()-startTime.getTime()) + " ms");

		Boolean doAutoQuit = Preference.AUTO_QUIT_AFTER_UPDATE();
		Boolean doCmdAfter = Preference.DO_CMD_AFTER();

		if(doCmdAfter){
			doCmdAfter();
		}

		if(doAutoQuit){
			Thread.sleep(10000);
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

	private void doCmdAfter() {
		try {
			String launcherPath = Preference.CMD_AFTER();

			if (null != launcherPath && launcherPath.length() > 0) {
				String[] cmdline;
				if (MainWindow.isMac()) {
					cmdline = new String[]{"open", "-a", launcherPath};
				} else {
					cmdline = new String[]{launcherPath};
				}
				Runtime.getRuntime().exec(cmdline);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}