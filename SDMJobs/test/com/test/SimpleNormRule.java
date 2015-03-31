package com.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.AnnotationConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMRowDAO;
import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMValueDAO;
import com.lynspa.sdm.jobs.bloomberg.load.fields.ImportBlooomberFieldsJob;
import com.lynspa.sdm.jobs.bloomberg.load.securities.ImportBlooomberSecuritiesJob;
import com.lynspa.sdm.jobs.normalization.BeanShellStaticDataNormalizeProcessor;
import com.lynxit.fpm.events.fileevents.FileCreatedEvent;
import com.lynxspa.entities.jobs.SDMStaticRow;
import com.lynxspa.entities.jobs.SDMValue;

public class SimpleNormRule {
	
	private static StatelessSession statelessSession;
	private static Session session;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		System.out.println("Start SetUp");
		
		File hibernateFile = new File ("H:/GitHub/SDMEntities/SDMEntities/src/hibernate.cfg.xml");
		SessionFactory sessionFactory = new AnnotationConfiguration().configure(hibernateFile).buildSessionFactory();
		session = sessionFactory.openSession();
		statelessSession = sessionFactory.openStatelessSession();
		
		final ImportBlooomberFieldsJob importFields = new ImportBlooomberFieldsJob();
		File bloombergFields = new File("H:/GitHub/SDMCore/SDMCore/input/securities/bloomberg/fields/fields.csv.tmp.done");
		importFields.importBloombergFields(session, statelessSession, bloombergFields, "TEST_USER", "es");
		System.out.println("Fields");
		final ImportBlooomberSecuritiesJob importSecurities = new ImportBlooomberSecuritiesJob();
		File bloombergSec = new File("H:/GitHub/SDMCore/SDMCore/input/securities/bloomberg/equity_euro.sdm");
		importSecurities.importBloombergSecurities(session, statelessSession, bloombergSec, "TEST_USER", "es");
		System.out.println("End SetUp");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Start ShutDown");
		
		File hibernateFile = new File ("H:/GitHub/SDMEntities/SDMEntities/src/hibernate.cfg.xml");
		SessionFactory sessionFactory = new AnnotationConfiguration().configure(hibernateFile).buildSessionFactory();
		session = sessionFactory.openSession();
		statelessSession = sessionFactory.openStatelessSession();
		
		SDMValueDAO valueDao = new SDMValueDAO(session);
		List<SDMValue> allValues = valueDao.findAll();
		for(SDMValue value : allValues)
			valueDao.delete(value.getId());
		
		SDMRowDAO rowDao = new SDMRowDAO();
		List<SDMStaticRow> allRows = rowDao.findAll(session);
		for(SDMStaticRow stRow : allRows)
			rowDao.delete(stRow.getId());
		System.out.println("End ShutDown");
	}

	@Test
	public void test() {
		System.out.println("Start Test prueba");
		System.out.println("End Test prueba");
		
		/*
		BeanShellStaticDataNormalizeProcessor normalizer = new BeanShellStaticDataNormalizeProcessor();
		normalizer.normalize(session, statelessSession, "TEST_USER", "es");
		*/
	}

}
