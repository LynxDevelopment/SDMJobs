package com.lynspa.sdm.jobs.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMValueDAO;
import com.lynxspa.entities.jobs.SDMValue;

public class SaveRowThread extends Thread {

	static Logger logger = Logger.getLogger(SaveRowThread.class.getName());
	private List<SDMValue> values;
	private StatelessSession statelessSession;
	private Session session;
	
	public SaveRowThread(List<SDMValue> values, StatelessSession statelessSession, Session session) {
		this.values = values;
		this.statelessSession=statelessSession;
		this.session = session;
	}

	public void run() {

		//logger.debug("Ready to save " + values.size() + " values");
		List<List<SDMValue>> rows = sortRows();
		//logger.debug("Sorted Rows");
		Iterator<List<SDMValue>> itRows = rows.iterator();
		
		while (itRows.hasNext()) {
			SDMValueDAO valueDao = new SDMValueDAO(this.session);
			List<SDMValue> row = itRows.next();
			if (row.size() > 0) {
				valueDao.insert(row,statelessSession);
			}
		}
		
		logger.debug("Thread [" + this.getId() + "] ha finalizado");
	}

	private List<List<SDMValue>> sortRows() {
		// Sort values per Rows
		List<List<SDMValue>> out = new ArrayList<List<SDMValue>>();

		Iterator<SDMValue> it = values.iterator();
		while (it.hasNext()) {
			SDMValue valueToSort = it.next();
			boolean found = false;
			Iterator<List<SDMValue>> itRows = out.iterator();
			while (!found && itRows.hasNext()) {
				List<SDMValue> rowValues = itRows.next();
				if (rowValues.get(0) != null
						&& rowValues.get(0).getRow().getId() == valueToSort
								.getRow().getId()) {
					found = true;
					rowValues.add(valueToSort);
				}
			}
			if (!found) {
				List<SDMValue> newRow = new ArrayList<SDMValue>();
				newRow.add(valueToSort);
				out.add(newRow);
			}
		}

		return out;
	}
}
