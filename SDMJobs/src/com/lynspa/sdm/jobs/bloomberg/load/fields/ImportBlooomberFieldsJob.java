package com.lynspa.sdm.jobs.bloomberg.load.fields;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.lynxspa.exception.FPMException;
import com.lynxspa.exception.dict.BasicErrorDict;

public class ImportBlooomberFieldsJob {
	
	
	public String importBloombergFields(Session _session,StatelessSession statelessSession, File _file, String _user, String _locale) throws FPMException{

		String reply=null;
		boolean error=false;
		File fileTemp = null;
		try{
			
			CargaFieldsBBG process = new CargaFieldsBBG ();
			fileTemp = new File(_file.getAbsolutePath()+".tmp");
			_file.renameTo(fileTemp);
			process.loadFieldsFromFile(fileTemp, _session, statelessSession, _user);
			
		}catch (FileNotFoundException e) {
			error = true;
			throw new FPMException(BasicErrorDict.FILENOTEXIST,e);
		}catch (IOException e) {
			error = true;
			throw new FPMException(BasicErrorDict.FILECANTREAD,e);
		}catch (Exception e) {
			error = true;
			throw new FPMException(BasicErrorDict.UNEXPECTED_ERROR,e);
		}finally{
			if (error) {
				fileTemp.renameTo(new File(fileTemp.getAbsolutePath()+".error"));
			} else {
				fileTemp.renameTo(new File(fileTemp.getAbsolutePath()+".done"));
			}
		}
		
		return reply;
	}
}
