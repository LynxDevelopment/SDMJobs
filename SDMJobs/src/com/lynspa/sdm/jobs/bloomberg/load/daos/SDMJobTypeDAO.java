package com.lynspa.sdm.jobs.bloomberg.load.daos;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import com.lynxspa.entities.jobs.SDMJobType;

public class SDMJobTypeDAO {

	static Logger logger = Logger.getLogger(SDMJobTypeDAO.class.getName());
	
	private Session session;
	private Transaction transaction;

	public SDMJobTypeDAO() {
		//session = HibernateUtil.getSessionFactory().openSession();
	}

	public SDMJobTypeDAO(Session session) {
		this.session = session;
	}
	public long insert(SDMJobType jt) {
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		long id = (Long) session.save(jt);
		//logger.debug("commit");
		transaction.commit();
		
		return id;
	}

	public void update(SDMJobType jt) {
		//logger.debug("beginTransaction");
		transaction = session.beginTransaction();
		session.saveOrUpdate(jt);
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

	public List<SDMJobType> findAll() {
		return session.createQuery("from SDMJobType").list();
	}

	public SDMJobType findByPrimaryKey(long pk) {
		return (SDMJobType) session.get(SDMJobType.class, pk);
	}

	public SDMJobType findByName(String name) {
		Query query = session.createQuery("from SDMJobType where name = :name ");
		query.setParameter("name", name);
		
		return (SDMJobType) query.uniqueResult();
	}
	
	public SDMJobType findByPath(String path) {
		Query query = session.createQuery("from SDMJobType where inputDirectory = :path ");
		query.setParameter("path", path);
		
		return (SDMJobType) query.uniqueResult();
	}
	
	protected void finalize() throws Throwable {
		if(session != null && session.isOpen())
			session.close();
		
		super.finalize();
	}
}
