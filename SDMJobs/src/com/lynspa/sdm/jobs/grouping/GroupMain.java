package com.lynspa.sdm.jobs.grouping;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.lynspa.sdm.jobs.normalization.BeanShellStaticDataNormalizeProcessor;
import com.lynxspa.entities.application.flow.Flow;
import com.lynxspa.entities.application.flow.State;
import com.lynxspa.entities.application.flow.StateId;
import com.lynxspa.entities.application.flow.operations.OperationStatus;
import com.lynxspa.entities.securities.assets.SecurityAsset;
import com.lynxspa.hbt.utils.HibernateUtils;
import com.lynxspa.sdm.dictionaries.flows.StaticDataWorkflow;
import com.lynxspa.sdm.dictionaries.flows.states.StaticStatesSTATICMESSAGEFlow;

public class GroupMain {

	static String separador = ";";
	static SessionFactory sessionFactory = null;
	static Session statefullSession=null;
	
	private static Logger logger = Logger.getLogger(GroupMain.class);
	
	@SuppressWarnings({ "unchecked" })
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		createConcatField();
			
	}
	
	
	public static void createConcatField(){
		
		try{
			//Rellenar ConcatField para los que tengan vacíos
			logger.info("Rellenar ConcatField");
			sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
			statefullSession = sessionFactory.openSession();
			HibernateUtils.beguinTransaction(statefullSession);
			//for(SecurityAsset sa:(List<SecurityAsset>)statefullSession.createQuery("select sa from SecurityAsset sa where sa.concatField is null order by sa.id asc").list()){
			// TODO alberto modify to make test
			// for(SecurityAsset sa:(List<SecurityAsset>)statefullSession.createQuery("select sa from SecurityAsset sa where sa.completed is true order by sa.id asc").list()){
			for(SecurityAsset sa:(List<SecurityAsset>)statefullSession.createQuery("select sa from SecurityAsset sa where sa.completed is true order by sa.id asc").list()){
				logger.info("sa.getIsin():"+sa.getIsin());
				logger.info("sa.getName():"+sa.getName());
				logger.info("sa.getCusip():"+sa.getCusip());
				logger.info("sa.getSedol():"+sa.getSedol());
				logger.info("sa.getCountry():"+sa.getCountry());
				if(sa.getConcatField()==null || sa.getConcatField().compareToIgnoreCase("")==0){
					sa.setConcatField(sa.getIsin()+separador+sa.getName()+separador+sa.getCusip()+separador+sa.getSedol()+separador+sa.getCountry());
				}
				groupFields(sa);
				statefullSession.update(sa);
			}
			HibernateUtils.commitTransaction(statefullSession);
			statefullSession.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}finally{
			if(statefullSession!=null){
				statefullSession.close();
			}
			if(sessionFactory!=null){
				sessionFactory.close();
			}
		}
	}
	
	public static void groupFields(SecurityAsset sa){
		int porcentajeAgrupacion = 80;
		int porcentajeAgrupacionActual = 0;
		Query rq = null;
		try{
			//Agrupar registros parecidos
			logger.info("Agrupación ID:" + sa.getId() + " ISIN:" + sa.getIsin() );
			//Agrupar los registros con un 80%
			int apariencias = 0;
			int totalCampos = 0;
			SecurityAsset saRowEncontrado;
			if(sa.getConcatField().contains(separador)){
				//rq = statefullSession.createQuery("select sa from SecurityAsset sa where sa.concatField is not null and sa.id<>:id order by sa.id asc");
				// TODO alberto modify to test it
				//rq = statefullSession.createQuery("select sa from SecurityAsset sa where sa.completed is true and sa.operationStatus.state.id.code = :state order by sa.id asc");
				rq = statefullSession.createQuery("select sa from SecurityAsset sa where sa.completed is true and sa.operationStatus.state.id.code = :state order by sa.id asc");
				//rq.setLong("id", sa.getId());
				// TODO alberto modify to test it
				//rq.setString("state", StaticStatesSTATICMESSAGEFlow.MTCH.getId());
				rq.setString("state", StaticStatesSTATICMESSAGEFlow.MTCH.getId()); // update
				if(rq.list().size()>0){
					for(SecurityAsset saRow:(List<SecurityAsset>)rq.list()){
						logger.info("entra en for, row: " + saRow.getIsin());
						
						//Dividimos el campo con el separador
						totalCampos = 0;
						apariencias = 0;
						// Buscamos apariencias 
						porcentajeAgrupacionActual = getPercentageGrouping(sa, saRow);
						// Si encontramos apariencias de un 80%
						if (porcentajeAgrupacionActual>=porcentajeAgrupacion && (porcentajeAgrupacionActual > sa.getPercentageGrouping())){
							logger.info("% mayor q 80");
							sa.setPercentageGrouping(porcentajeAgrupacionActual);
							//Si tiene groupId lo utilizamos
							logger.info("saRow.getGroupId():"+saRow.getGroupId());
							if(saRow.getGroupId()!=0){
								sa.setGroupId(saRow.getGroupId());
								sa.setOperationStatus(getOperationStatus(StaticStatesSTATICMESSAGEFlow.MTCH,statefullSession));
								/*if(porcentajeAgrupacionActual>saRow.getPercentageGrouping()){
								saRow.setPercentageGrouping(porcentajeAgrupacionActual);
							}*/
							}else{
								//Sino tiene groupId se crea de los dos registros(comparador y comparado)
								saRow.setPercentageGrouping(porcentajeAgrupacionActual);
								long groupId = getId();
								saRow.setGroupId(groupId);
								saRow.setOperationStatus(getOperationStatus(StaticStatesSTATICMESSAGEFlow.MTCH,statefullSession));
								sa.setGroupId(groupId);
								sa.setOperationStatus(getOperationStatus(StaticStatesSTATICMESSAGEFlow.MTCH,statefullSession));
								
								logger.info("Se updatea row " + saRow.getIsin() + " id " + groupId);
								
								// Se actualiza el registro comparador
								statefullSession.update(saRow);
							}
						}
					}
				}
			}else{
				// ERROR NO TIENE SEPARADOR
				logger.error("ERROR NO TIENE SEPARADOR");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			if(statefullSession!=null){
				statefullSession.close();
			}
			if(sessionFactory!=null){
				sessionFactory.close();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static long getId(){
		long id = 0;
		Iterator<Long> it = null;
		try {
			it = statefullSession.createQuery("select sa.groupId from SecurityAsset sa order by groupId desc").iterate();
			if(it.hasNext()){
				logger.info("existe groupId");
				id = it.next() + 1;
			}else {
				logger.info("No existe groupId");
				id = 1;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			if(statefullSession!=null){
				statefullSession.close();
			}
			if(sessionFactory!=null){
				sessionFactory.close();
			}
		}
		
		return id;
		
	}
	
	public static int getPercentageGrouping(SecurityAsset saNuevo , SecurityAsset saExistente){
		// Buscamos apariencias
		float percentageGrouping = 0;
		try{
			String[] camposNuevo = saNuevo.getConcatField().split(separador);
			String[] camposExistente = saExistente.getConcatField().split(separador);
			float apariencias = 0;
			float totalCampos = 0;
			if(camposNuevo.length==camposExistente.length){
				totalCampos = camposNuevo.length;
				for(int i = 0; i<totalCampos; i++){
					if(camposNuevo[i]!=null && camposNuevo[i].compareToIgnoreCase("null")!=0 && camposNuevo[i].compareToIgnoreCase("N.A.")!=0 &&
							camposExistente[i]!=null && camposExistente[i].compareToIgnoreCase("null")!=0 && camposExistente[i].compareToIgnoreCase("N.A.")!=0 && 
							camposNuevo[i].compareTo(camposExistente[i])==0){
						apariencias++;
					}
				}
				float resultadoDivision = (apariencias/totalCampos);
				logger.info("resultadoDivision:"+resultadoDivision);
				percentageGrouping = Math.round((apariencias/totalCampos)*100);
				logger.info("percentageGrouping:"+percentageGrouping);
			}
			logger.info("totalCampos:"+totalCampos+" apariencias:"+apariencias);
			logger.info("% "+(int)percentageGrouping);
			return (int) percentageGrouping;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			if(statefullSession!=null){
				statefullSession.close();
			}
			if(sessionFactory!=null){
				sessionFactory.close();
			}
			return (int) percentageGrouping;
		}
	}
	
	public static OperationStatus getOperationStatus(StaticStatesSTATICMESSAGEFlow state, Session session){
		OperationStatus operationStatus;
		Flow flow = (Flow)session.get(Flow.class, StaticDataWorkflow.STATICMESSAGE.getId());
		String code = state.getId();
		StateId stateId = new StateId(flow, code);
		operationStatus = new OperationStatus((State)session.get(State.class, stateId));
		
		return operationStatus;
	}

}
