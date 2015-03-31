package com.lynspa.sdm.jobs.bloomberg.load.securities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.lynxspa.exception.FPMException;
import com.lynxspa.exception.dict.BasicErrorDict;

public class ImportBloomberSecuritiesJob {
	
	
	public String importBloombergSecurities(Session _session,StatelessSession statelessSession, File _file, String _user, String _locale) throws FPMException{

		String reply=null;
		boolean error=false;
		File fileTemp = null;
		
		try{
			
			SDMJobProcessor process = new SDMJobProcessor();
			fileTemp = new File(_file.getAbsolutePath()+".tmp");
			_file.renameTo(fileTemp);
			process.process(_session, statelessSession,fileTemp, _user, _locale);
			
		}catch (FileNotFoundException e) {
			error = true;
			throw new FPMException(BasicErrorDict.FILENOTEXIST,e);
		}catch (IOException e) {
			error = true;
			throw new FPMException(BasicErrorDict.FILECANTREAD,e);
		}
		catch (Exception e) {
			error = true;
			throw new FPMException(BasicErrorDict.UNEXPECTED_ERROR,e);
		}finally{
			
			if (error){
				fileTemp.renameTo(new File(fileTemp.getAbsolutePath()+".error"));
			}else{
				System.out.println("Renombrando fichero "+fileTemp.getAbsolutePath());
				
				fileTemp.renameTo(new File(fileTemp.getAbsolutePath()+".done"));
//				if (!flag) {
//					Path source = FileSystems.getDefault().getPath(fileTemp.getAbsolutePath());
//					Path dest = FileSystems.getDefault().getPath(fileTemp.getAbsolutePath()+".done");
//					try {
//					Files.move(source, dest);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
			}
		}
		
		return reply;
	}
}
