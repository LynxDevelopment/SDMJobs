package com.lynspa.sdm.jobs.utils;

import java.util.Map;

import org.hibernate.Session;

import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMJobTypeDAO;
import com.lynxspa.entities.UpdateAuditor;
import com.lynxspa.entities.application.flow.Flow;
import com.lynxspa.entities.application.flow.State;
import com.lynxspa.entities.application.flow.StateId;
import com.lynxspa.entities.jobs.SDMJobType;
import com.lynxspa.sdm.dictionaries.flows.StaticDataWorkflow;
import com.lynxspa.sdm.dictionaries.flows.states.CAStatesEVENTMESSAGEFlow;

public class SDMUtils {
	//TODO: Pasar y recoger los datos especificos para cada job en el map
	public static SDMJobType getJobType (Session session, Map<String, String> infoJob){
		SDMJobType jobt = new SDMJobTypeDAO(session).findByName(infoJob.get("name"));
		
		if(jobt == null){
			jobt = new SDMJobType();
			jobt.setName(infoJob.get("name"));
			jobt.setAllOrNothing(false);
			jobt.setAllowMultithreading(true);
			jobt.setClassExe("");
			jobt.setCommitDirectory("H:\\develop\\workspace_NEWSDM\\SDM_LynxIberica_Madrid\\files");
			jobt.setCommitSuffix(".done");
			jobt.setCronExpression("1/10 * * * * ?");
			jobt.setDescription("Bloomberg Security Fields");
			jobt.setInputDirectory("D:\\develop\\workspace_NEWSDM\\SDM_LynxIberica_Madrid\\files");
			jobt.setPattern(".sdm");
			jobt.setRollbackDirectory("H:\\develop\\workspace_NEWSDM\\SDM_LynxIberica_Madrid\\files");
			jobt.setRollbackSuffix(".error");
			jobt.setTemporalSuffix(".tmp");
			jobt.setVersion(0);
			jobt.setAuditor(new UpdateAuditor(infoJob.get("user")));
			
			new SDMJobTypeDAO(session).insert(jobt);
		}
		
		return jobt;
	}
	
	public static SDMJobType getJobType(Session session, Map<String, String> infoJob, String path){
		
		SDMJobType jobt = new SDMJobTypeDAO(session).findByPath(path);
		
		if(jobt == null){
			jobt = new SDMJobType();
			jobt.setName(infoJob.get("name"));
			jobt.setAllOrNothing(false);
			jobt.setAllowMultithreading(true);
			jobt.setClassExe("");
			jobt.setCommitDirectory(path);
			jobt.setCommitSuffix(".done");
			jobt.setCronExpression("1/10 * * * * ?");
			jobt.setDescription("Bloomberg Security Fields");
			jobt.setInputDirectory(path);
			jobt.setPattern(".sdm");
			jobt.setRollbackDirectory(path);
			jobt.setRollbackSuffix(".error");
			jobt.setTemporalSuffix(".tmp");
			jobt.setVersion(0);
			jobt.setAuditor(new UpdateAuditor(infoJob.get("user")));
			
			new SDMJobTypeDAO(session).insert(jobt);
		}
		
		return jobt;
	}
	
	public static State getState (Session session, String codeState){
		StateId stateId = null;
		State state;
		Flow flow = (Flow)session.load(Flow.class, StaticDataWorkflow.STATICMESSAGE.getId());
		
		if (codeState.equalsIgnoreCase(CAStatesEVENTMESSAGEFlow.PRSD.getId())){
			stateId = new StateId(flow,CAStatesEVENTMESSAGEFlow.PRSD.getId());
		}
		state = (State)session.load(State.class, stateId);
		
		return state;
	}
}
