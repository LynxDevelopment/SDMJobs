package com.lynspa.sdm.jobs;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.AnnotationConfiguration;

import com.lynspa.sdm.jobs.normalization.BeanShellStaticDataNormalizeProcessor;

public class TesterJobs {
	private static SessionFactory sessionFactory;
	
	private final static Logger logger = Logger.getLogger(TesterJobs.class);
	
	public static void main(String[] args) {
		
		logger.info("logger: "+ logger.getLevel() + " " + logger.getName() + " " + logger.isDebugEnabled());
		System.out.println("logger: "+ logger.getLevel() + " " + logger.getName() + " " + logger.isDebugEnabled());
		
		File hibernateFile = new File ("E:/workspace_sdm/SDM/SDMEntities/src/hibernate.cfg.xml");
		sessionFactory = new AnnotationConfiguration().configure(hibernateFile).buildSessionFactory();
		
		Session session = sessionFactory.openSession();
		StatelessSession statelessSession = sessionFactory.openStatelessSession();
		
		//Probando normalizaciones
		BeanShellStaticDataNormalizeProcessor normalizer = new BeanShellStaticDataNormalizeProcessor();
		try {
			normalizer.normalize(session, statelessSession, "TEST_USER", "ES");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
