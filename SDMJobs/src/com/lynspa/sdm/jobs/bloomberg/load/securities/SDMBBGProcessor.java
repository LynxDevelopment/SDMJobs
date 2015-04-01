package com.lynspa.sdm.jobs.bloomberg.load.securities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.hibernate.StatelessSession;

import com.lynspa.sdm.jobs.bloomberg.BBGFileIterator;
import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMJobFieldDAO;
import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMJobTypeDAO;
import com.lynspa.sdm.jobs.bloomberg.load.securities.exceptions.SDMRuntimeException;
import com.lynxspa.entities.UpdateAuditor;
import com.lynxspa.entities.application.flow.State;
import com.lynxspa.entities.application.flow.operations.OperationStatus;
import com.lynxspa.entities.jobs.SDMJobField;
import com.lynxspa.entities.jobs.SDMJobType;
import com.lynxspa.entities.jobs.SDMStaticRow;
import com.lynxspa.entities.jobs.SDMValue;
import com.lynxspa.hbt.utils.HibernateUtils;

/**
 * SDMBBGProcessor implements SDMProcessor interface
 * 
 * @author Esteban Calderon
 * 
 */
public class SDMBBGProcessor implements ISDMSourceProcessor {

	static Logger logger = Logger.getLogger(SDMBBGProcessor.class.getName());
	
	private final String STARTOFFIELDS = "START-OF-FIELDS";
	private final String ENDOFFIELDS = "END-OF-FIELDS";
	private final String BBG_DELIM = "|";
	private final String BBG_JOBTYPE = "BBGSecurity";

	private File file;
	private List<SDMJobField> fields = null;
	private BBGFileIterator fileIterator = null;
	private State state;
	private String user;
	
	public SDMBBGProcessor (File fileToProcess){
		this.file=fileToProcess;
	}
	
	public SDMBBGProcessor (File fileToProcess, State state, String user){
		this.file=fileToProcess;
		this.state = state;
		this.user = user;
	}
	
	@Override
	public List<SDMJobField> getFields(StatelessSession statelessSession) {

		List<SDMJobField> out = new ArrayList<SDMJobField>();
		List<String> fields = new ArrayList<String>();
		BufferedReader buffer = null;
		try {
			buffer = new BufferedReader(new FileReader(file));
			
			boolean goOut = false;
			while (!goOut) {
				String line = buffer.readLine();
				if (line.equals(STARTOFFIELDS))
					goOut = true;
			}

			goOut = false;
			while (!goOut) {
				String aux = buffer.readLine();
				if (aux.equals(ENDOFFIELDS))
					goOut = true;
				else {
					if (!aux.equals("") && !aux.startsWith("#"))
						fields.add(aux);
				}
			}
		} catch (Exception e) {
			throw new SDMRuntimeException(e.getMessage());
		}finally{
			try{
				if(buffer!=null)
					buffer.close();
			} catch (IOException e) {
				throw new SDMRuntimeException(e.getMessage());
			}
		}

		Iterator<String> it = fields.iterator();
		while (it.hasNext()) {
			String name = it.next();
			SDMJobField aux = new SDMJobFieldDAO().findByName(name,statelessSession); 
			out.add(aux);
		}

		return out;
	}

	@Override
	public SDMJobType getJobType() {
		return new SDMJobTypeDAO().findByName(BBG_JOBTYPE);
	}

	@Override
	public boolean hasMoreRows() throws FileNotFoundException {
		if (fileIterator == null){
			fileIterator = new BBGFileIterator(new BufferedReader(new FileReader(file)));
		}
		return fileIterator.hasNext();
	}

	@Override
	public SDMStaticRow getNextRow() throws FileNotFoundException {
		SDMStaticRow out = null;

		if (fileIterator.hasNext()) {
			out = new SDMStaticRow();
			out.setValue(fileIterator.next());
			out.setAuditor(new UpdateAuditor("BLOOMBERGPARSER"));
			out.setOperationStatus(new OperationStatus(this.state));
		}
		
		return out;
	}

	@Override
	public List<SDMValue> getValues(SDMStaticRow row, StatelessSession statelessSession) { 
		String sValue=null;
		boolean rowModified=false;
		if(fields == null)
			fields = getFields(statelessSession);

		List<SDMValue> out = new ArrayList<SDMValue>();

		StringTokenizer stTokens = new StringTokenizer(row.getValue(),BBG_DELIM);
		int i = 0;
		stTokens.nextToken();
		stTokens.nextToken();
		stTokens.nextToken();
		while (stTokens.hasMoreTokens()) {
			SDMValue value = new SDMValue();
			value.setRow(row);
			sValue = stTokens.nextToken();
			value.setValue(sValue);
			value.setJobField(fields.get(i));
			if (fields.get(i)!=null){
				if(fields.get(i).getName().equals("TICKER")){
					row.setSecurityTicker(sValue);
					rowModified=true;
				}else if(fields.get(i).getName().equals("NAME")){
					row.setSecurityName(sValue);
					rowModified=true;
				}else if(fields.get(i).getName().equals("ID_ISIN")){
					row.setSecurityIsin(sValue);
					rowModified=true;
				}else if(fields.get(i).getName().equals("ID_MIC_PRIM_EXCH")){
					row.setSecurityMic(sValue);
					rowModified=true;
				}
			}
			value.setAuditor(new UpdateAuditor(this.user));
			i++;
			out.add(value);
		}

		if (rowModified){
			try {
				HibernateUtils.customUpdate(statelessSession, row, user);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return out;
	}

	@Override
	public void setFile(File file) {
		this.file = file;
	}
	public File getFile() {
		return file;
	}

	
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	
	

}
