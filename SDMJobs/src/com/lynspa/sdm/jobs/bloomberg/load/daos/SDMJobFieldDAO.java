package com.lynspa.sdm.jobs.bloomberg.load.daos;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import com.lynxspa.entities.jobs.SDMJobField;

public class SDMJobFieldDAO {

	static Logger logger = Logger.getLogger(SDMJobFieldDAO.class.getName());
	
	private Session session;
	private Transaction transaction;

	public SDMJobFieldDAO() {
		//session = HibernateUtil.getSessionFactory().openSession();
	}

	public SDMJobFieldDAO(Session session) {
		this.session = session;
	}
	public void insert(SDMJobField jf, StatelessSession statelessSession, int numInserted) {
		//logger.debug("beginTransaction");
		statelessSession.insert(jf);
		transaction = statelessSession.beginTransaction();
		//logger.debug("commit");
		if(numInserted%100==0)
			transaction.commit();
		
	}

	public void update(SDMJobField jf) {
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		session.saveOrUpdate(jf);
		//logger.debug("commit");
		transaction.commit();
	}

	public void delete(long pk) {
		//.debug("beginTransaction");
		transaction = session.beginTransaction();
		session.delete(findByPrimaryKey(pk));
		//logger.debug("commit");
		transaction.commit();
	}

	public List<SDMJobField> findAll() {
		return session.createQuery("from SDMJobField").list();
	}

	public SDMJobField findByPrimaryKey(long pk) {
		return (SDMJobField) session.get(SDMJobField.class, pk);
	}
	
	public SDMJobField findByName(String name, StatelessSession statelessSession) {
		Transaction transaction = statelessSession.beginTransaction();
		Query query = statelessSession.createQuery("from SDMJobField where name = :name ");
		query.setParameter("name", name);
		SDMJobField jobField = (SDMJobField) query.uniqueResult();
		transaction.commit();
		return jobField;
	}
	
	protected void finalize() throws Throwable {
//		if(session != null && session.isOpen())
//			session.close();
//		
		//super.finalize();
	}
}
