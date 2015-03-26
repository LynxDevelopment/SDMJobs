package com.lynspa.sdm.jobs.bloomberg.load.daos;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import com.lynxspa.entities.jobs.SDMJob;

public class SDMJobDAO {

	static Logger logger = Logger.getLogger(SDMJobDAO.class.getName());
	
	private Session session;
	private Transaction transaction;

	public SDMJobDAO() {
		//session = HibernateUtil.getSessionFactory().openSession();
	}

	public long insert(SDMJob job, Session session) {
		
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		long id = (Long) session.save(job);
		//logger.debug("commit");
		transaction.commit();
		
		return id;
	}
	
	public void persist(SDMJob job, StatelessSession session) {
		
		transaction = session.beginTransaction();
		session.insert(job);
		transaction.commit();
		
	}

	public void update(SDMJob jt, StatelessSession session) {
		
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		session.update(jt);
		//logger.debug("commit");
		transaction.commit();
	}

	public void delete(long pk) {
		
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		session.delete(findByPrimaryKey(pk));
		//logger.debug("commit");
		transaction.commit();
	}

	public List<SDMJob> findAll() {
		return session.createQuery("from SDMJob").list();
	}

	public SDMJob findByPrimaryKey(long pk) {
		return (SDMJob) session.get(SDMJob.class, pk);
	}
	
	protected void finalize() throws Throwable {
		if(session != null && session.isOpen())
			session.close();
		
		super.finalize();
	}
}
