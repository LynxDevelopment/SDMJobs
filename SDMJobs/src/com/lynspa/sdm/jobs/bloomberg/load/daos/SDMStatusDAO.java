package com.lynspa.sdm.jobs.bloomberg.load.daos;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.lynxspa.entities.jobs.SDMStatus;

public class SDMStatusDAO {

	static Logger logger = Logger.getLogger(SDMStatusDAO.class.getName());
	
	private Session session;
	private Transaction transaction;

	public SDMStatusDAO() {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
	}
	
	protected void finalize() throws Throwable {
		if(session != null && session.isOpen())
			session.close();
		
		super.finalize();
	}
	
	public SDMStatus findStatus (String _code){
		SDMStatus reply=null;
		session.beginTransaction();
		Query query = session.createQuery("from SDMStatus where code = :code ");
		query.setParameter("code", _code);
		reply = (SDMStatus) query.uniqueResult();
		session.getTransaction().commit();
		return reply;
	}
	
	public void loadStatus (){
		
//		SDMStatus status1 = new SDMStatus();
//		status1.setCode("PRSD");
//		status1.setDescription("description");
//		status1.setName("Parseado");
		
//		SDMStatus status2 = new SDMStatus();
//		status2.setCode("NORM");
//		status2.setDescription("description");
//		status2.setName("Normalizado");
//		
//		
//		session.beginTransaction();
//		session.persist(status2);
//		session.getTransaction().commit();
	}
}
