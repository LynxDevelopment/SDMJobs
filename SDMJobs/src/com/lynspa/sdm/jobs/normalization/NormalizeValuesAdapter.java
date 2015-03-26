package com.lynspa.sdm.jobs.normalization;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.lynxspa.entities.jobs.SDMRow;
import com.lynxspa.sdm.processors.normalize.NormalizeScriptConfigBean;

public interface NormalizeValuesAdapter {
		
		public NormalizeValueResultBean test(Session _session,List<NormalizeScriptConfigBean> _scripts,SDMRow row) throws Exception;
		public void normalize(Session _session,StatelessSession statelessSession, String _user, String _locale) throws Exception;

}
