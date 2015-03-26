package com.lynspa.sdm.jobs.bloomberg.load.daos;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import com.lynxspa.entities.jobs.SDMRow;
import com.lynxspa.entities.jobs.SDMStaticRow;

public class SDMRowDAO {
	
	static Logger logger = Logger.getLogger(SDMRowDAO.class.getName());

	private Session session;
	private Transaction transaction;

	public SDMRowDAO() {
		//session = HibernateUtil.getSessionFactory().openSession();
	}

	public long insert(SDMRow r) {
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		long id = (Long) session.save(r);
		//logger.debug("commit");
		transaction.commit();
		
		return id;
	}

	public void persist(SDMRow row, StatelessSession statelessSession) {
		transaction = statelessSession.beginTransaction();
		statelessSession.insert(row);
		transaction.commit();
	}

	
	public void update(SDMRow row,Session session) {
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		session.update(row);
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

	public List<SDMStaticRow> findAll(Session session) {
		session.beginTransaction();
		List<SDMStaticRow> securitiesRow = session.createQuery("from SDMStaticRow where operationStatus.state.id.code='PRSD'").setMaxResults(100).list();
		session.getTransaction().commit();
		return securitiesRow;
	}

	public SDMRow findByPrimaryKey(long pk) {
		return (SDMRow) session.get(SDMRow.class, pk);
	}
	
	public SDMStaticRow findByName(String name) {
		Query query = session.createQuery("from SDMStaticRow where name = :name ");
		query.setParameter("name", name);
		return (SDMStaticRow) query.uniqueResult();
	}
	
	protected void finalize() throws Throwable {
		if(session != null && session.isOpen())
			session.close();
		
		super.finalize();
	}
}
