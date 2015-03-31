package com.lynspa.sdm.jobs.bloomberg.load.fields;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import au.com.bytecode.opencsv.CSVReader;

import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMJobDAO;
import com.lynspa.sdm.jobs.utils.SDMUtils;
import com.lynxspa.entities.UpdateAuditor;
import com.lynxspa.entities.jobs.SDMJob;
import com.lynxspa.entities.jobs.SDMJobField;
import com.lynxspa.entities.jobs.SDMJobType;
import com.lynxspa.hbt.utils.HibernateUtils;

public class CargaFieldsBBG {

	/**
	 * @param args
	 * @throws IOException 
	 */
	private final String BODY = "BDY";
	private final String BLOOMBERG = "BBG";
	
	public void loadFieldsFromFile(File _file, Session session,  StatelessSession statelessSession, String user) throws IOException, FileNotFoundException{
	
		HashMap<String,String> jobData = new HashMap<String, String>();
		jobData.put("name", "BBGFields");
		jobData.put("user", user);
		
		String path = _file.getAbsolutePath().substring(0,_file.getAbsolutePath().lastIndexOf('\\'));
		System.out.println("Path:"+path);
		
		SDMJobType jobt = SDMUtils.getJobType(session, jobData,path);
		
		SDMJob job = new SDMJob();
		if(_file.exists()){
			job.setFile(_file.getAbsolutePath());
		}
		job.setJobType(jobt);
		job.setAuditor(new UpdateAuditor(user)); 
		SDMJobDAO jobDao = new SDMJobDAO();
		try {
			jobDao.persist(job, statelessSession);
		}catch(Exception e){
			throw e;
		}
		insertFields(_file, session, statelessSession, user,jobt);
	}
	
	public void insertFields(File _file, Session session,  StatelessSession statelessSession, String user, SDMJobType jobt) throws IOException, FileNotFoundException{
		CSVReader reader = null;
		Transaction transaction=null;
		int count = 0;
		String [] line = null;
		SDMJobField jobField =null;
		long processingTime=0l;
		long proccessedWrong=0;
		int numFieldType=0;
		try{
			reader = new CSVReader(new FileReader(_file));
			//Leemos los datos de la cabecera (primera linea)
			line = reader.readNext();
			if (line!=null){
				int i=0;
				for (String field:line){
					if(field.equals("Field Type")){
						numFieldType=i;
					}
					i++;
				}
			}
			
			HibernateUtils.beguinTransaction(statelessSession);
			processingTime=System.currentTimeMillis();
			//Cogemos la primera linea de datos
			line = reader.readNext();
			while(line != null){
				count++;
				try {
					//Vemos si ya existe en bbdd, si existe se actualiza si no, se crea nuevo.
					jobField = getJobField(statelessSession,line[1],jobt.getId() );
					jobField.setName(line[1]);
					jobField.setDescription(line[2]);
					if(line[5].length() > 200)
						jobField.setLongDescription(line[5].substring(0, 200));
					else
						jobField.setLongDescription(line[5]);

					jobField.setAuditor(new UpdateAuditor(user));
					jobField.setJobType(jobt);
					jobField.setFieldType(line[numFieldType]);
					jobField.setPath(BODY+":"+BLOOMBERG+":"+line[1]);
					HibernateUtils.customSaveOrUpdate(statelessSession, jobField, user);
					if(count%1000==0){
						HibernateUtils.commitTransaction(statelessSession);
						HibernateUtils.beguinTransaction(statelessSession);
					}
				} catch (Exception e) {
					System.err.println("Error " +e.getMessage()+" loading field " + count +": "+line[1]);
					proccessedWrong++;
				}
				line = reader.readNext();
			}
			HibernateUtils.commitTransaction(statelessSession);
			processingTime=System.currentTimeMillis()-processingTime;
			System.out.println("Tiempo sin commits" + processingTime +" para " +count+ " y " +proccessedWrong +" erroneos");
		}catch(Exception e){
			System.err.println("Error "+e.getMessage());
		}finally{
			//TODO descomentar;
//			HibernateUtils.close(statelessSession);
//			reader.close();
		}
	}
	
	private SDMJobField getJobField (StatelessSession statelessSession, String name, long jobTypeId ){
		SDMJobField reply=null;
		Query _eventQuery= statelessSession.createQuery("" +
				"from SDMJobField as jf where jf.name=:jfname and jf.jobType.id=:jobTypeId");
		_eventQuery.setParameter("jfname",name);
		_eventQuery.setParameter("jobTypeId",jobTypeId);
		_eventQuery.setMaxResults(1);
		reply= (SDMJobField)_eventQuery.uniqueResult();
		if (reply==null){
			reply = new SDMJobField();
		}
		return reply;
	}
	
	private static void maxLength(CSVReader reader) throws IOException{

		String [] line = reader.readNext();
		int max = 0;
		while(line != null){
			System.out.println("Nueva linea: " + line.length + " campos");
			if(line[5].length() > max){
				max = line[5].length();
				System.out.println(line[5]);
			}
			line = reader.readNext();
		}
		
		System.out.println("MAX: " + max);
	}

}
