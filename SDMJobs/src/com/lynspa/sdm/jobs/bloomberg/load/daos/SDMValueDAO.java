package com.lynspa.sdm.jobs.bloomberg.load.daos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import com.lynxspa.entities.jobs.SDMValue;

public class SDMValueDAO {

	static Logger logger = Logger.getLogger(SDMValueDAO.class.getName());
	
	private Session session;
	private Transaction transaction;

//	public SDMValueDAO() {
//		//session = HibernateUtil.getSessionFactory().openSession();
//	}

	public SDMValueDAO(Session session) {
		this.session = session;
	}

	
	public long insert(SDMValue v) {
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		long id = (Long) session.save(v);
		//logger.debug("commit");
		transaction.commit();

		return id;
	}

	public synchronized void insert(List<SDMValue> values, StatelessSession statelessSession) {

		List<Long> out = new ArrayList<Long>();
		try {
			//logger.debug("beginTransaction");
			transaction = session.beginTransaction();
			Iterator<SDMValue> it = values.iterator();
			while (it.hasNext()){
				session.persist(it.next());
			}
			transaction.commit();
		} catch (HibernateException e) {
			transaction.rollback();
		}
	}

	public void update(SDMValue v) {
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		session.saveOrUpdate(v);
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

	public List<SDMValue> findAll() {
		return session.createQuery("from SDMValue").list();
	}

	public SDMValue findByPrimaryKey(long pk) {
		return (SDMValue) session.get(SDMValue.class, pk);
	}

	public SDMValue findByName(String name) {
		Query query = session.createQuery("from SDMValue where name = :name ");
		query.setParameter("name", name);
		return (SDMValue) query.uniqueResult();
	}

	public void finalize() throws Throwable {
		if (session != null && session.isOpen())
			session.close();

		super.finalize();
	}
}
