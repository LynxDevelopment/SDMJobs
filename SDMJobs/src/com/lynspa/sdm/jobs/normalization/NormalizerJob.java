package com.lynspa.sdm.jobs.normalization;

import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.lynxspa.exception.FPMException;
import com.lynxspa.sdm.dictionaries.config.CAConfiguration;
import com.lynxspa.sdm.managers.SDMConfigManager;

public class NormalizerJob {
	
	public String normalize (Session session, StatelessSession statelessSession, String _user, String _locale){
		NormalizeValuesAdapter normalizeProcess=null;
		String reply="";
		SDMConfigManager manager=null;
		
		try {
			manager=SDMConfigManager.getInstance();
			normalizeProcess=(NormalizeValuesAdapter)manager.getProcessor(session,CAConfiguration.NORMALIZESTATICPROCESSOR);
			
			normalizeProcess.normalize(session, statelessSession,_user, _locale);
			
		} catch (FPMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			
		}
		
		return reply;
	}
}
