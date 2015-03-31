package com.test;

import java.io.File;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.AnnotationConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMJobDAO;
import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMJobTypeDAO;
import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMRowDAO;
import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMValueDAO;
import com.lynspa.sdm.jobs.bloomberg.load.fields.ImportBlooomberFieldsJob;
import com.lynspa.sdm.jobs.bloomberg.load.securities.ImportBloomberSecuritiesJob;
import com.lynxspa.entities.jobs.SDMJob;
import com.lynxspa.entities.jobs.SDMJobType;
import com.lynxspa.entities.jobs.SDMStaticRow;
import com.lynxspa.entities.jobs.SDMValue;

public class SimpleNormRule {

	private static StatelessSession statelessSession;
	private static Session session;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Start SetUpClass");

		File hibernateFile = new File(
				"H:/GitHub/SDMEntities/SDMEntities/src/hibernate.cfg.xml");
		SessionFactory sessionFactory = new AnnotationConfiguration()
				.configure(hibernateFile).buildSessionFactory();
		session = sessionFactory.openSession();
		statelessSession = sessionFactory.openStatelessSession();

		final ImportBlooomberFieldsJob importFields = new ImportBlooomberFieldsJob();
		File bloombergFields = new File(
				"H:/GitHub/SDMCore/SDMCore/input/securities/bloomberg/fields/fields.csv");
		importFields.importBloombergFields(session, statelessSession,
				bloombergFields, "TEST_USER", "es");
		System.out.println("Fields");
		final ImportBloomberSecuritiesJob importSecurities = new ImportBloomberSecuritiesJob();
		File bloombergSec = new File(
				"H:/GitHub/SDMCore/SDMCore/input/securities/bloomberg/equity_euro.sdm");
		importSecurities.importBloombergSecurities(session, statelessSession,
				bloombergSec, "TEST_USER", "es");
		//TODO renombrar de nuevo a equity_euro.sdm;

		System.out.println("End SetUpClass");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Start ShutDownClass");

		File hibernateFile = new File(
				"H:/GitHub/SDMEntities/SDMEntities/src/hibernate.cfg.xml");
		SessionFactory sessionFactory = new AnnotationConfiguration()
				.configure(hibernateFile).buildSessionFactory();
		session = sessionFactory.openSession();
		statelessSession = sessionFactory.openStatelessSession();

		SDMValueDAO valueDao = new SDMValueDAO(session);
		List<SDMValue> allValues = valueDao.findAll();
		for (SDMValue value : allValues)
			valueDao.delete(value.getId());

		SDMRowDAO rowDao = new SDMRowDAO();
		List<SDMStaticRow> allRows = rowDao.findAll(session);
		for (SDMStaticRow stRow : allRows)
			rowDao.delete(stRow.getId());

		SDMJobDAO jobDao = new SDMJobDAO();
		List<SDMJob> allJ = jobDao.findAll();
		for(SDMJob j : allJ)
			jobDao.delete(j.getId());
		
		SDMJobTypeDAO jobTypeDao = new SDMJobTypeDAO(session);
		List<SDMJobType> allJT = jobTypeDao.findAll();
		for(SDMJobType jt : allJT)
			jobTypeDao.delete(jt.getId());

		System.out.println("End ShutDownClass");
	}

	@Before
	public void setUp() throws Exception {
		System.out.println("Start setUp");
		System.out.println("End setUp");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Start Shutdown");
		System.out.println("End Shutdown");
	}

	@Test
	public void test() {
		System.out.println("Start Test prueba");
		System.out.println("End Test prueba");

		/*
		 * BeanShellStaticDataNormalizeProcessor normalizer = new
		 * BeanShellStaticDataNormalizeProcessor();
		 * normalizer.normalize(session, statelessSession, "TEST_USER", "es");
		 */
	}

}
