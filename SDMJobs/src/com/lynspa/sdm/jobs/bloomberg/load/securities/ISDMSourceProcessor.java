package com.lynspa.sdm.jobs.bloomberg.load.securities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.hibernate.StatelessSession;

import com.lynxspa.entities.application.flow.State;
import com.lynxspa.entities.jobs.SDMJobField;
import com.lynxspa.entities.jobs.SDMJobType;
import com.lynxspa.entities.jobs.SDMStaticRow;
import com.lynxspa.entities.jobs.SDMValue;

public interface ISDMSourceProcessor {

	public List<SDMJobField> getFields(StatelessSession statelessSession);
	public SDMJobType getJobType();
	public boolean hasMoreRows() throws FileNotFoundException;
	public SDMStaticRow getNextRow() throws FileNotFoundException;
	public List<SDMValue> getValues(SDMStaticRow row, StatelessSession statelessSession);
	public void setFile(File file);
	public File getFile();
	public void setState(State state);

}
