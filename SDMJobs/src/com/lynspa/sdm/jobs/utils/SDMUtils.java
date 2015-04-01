package com.lynspa.sdm.jobs.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.lynspa.sdm.jobs.bloomberg.load.daos.SDMJobTypeDAO;
import com.lynspa.sdm.jobs.bloomberg.load.securities.SDMJobProcessor;
import com.lynxspa.entities.UpdateAuditor;
import com.lynxspa.entities.application.flow.Flow;
import com.lynxspa.entities.application.flow.State;
import com.lynxspa.entities.application.flow.StateId;
import com.lynxspa.entities.jobs.SDMJobType;
import com.lynxspa.entities.jobs.SDMJobTypeFields;
import com.lynxspa.sdm.dictionaries.flows.StaticDataWorkflow;
import com.lynxspa.sdm.dictionaries.flows.states.CAStatesEVENTMESSAGEFlow;

public class SDMUtils {

	static Logger logger = Logger.getLogger(SDMUtils.class.getName());

	public static final String EQUI = "BLOOMBERG_EQUITY";
	public static final String EQUI_NAME = "equity";
	public static final String FUND = "BLOOMBERG_FUND";
	public static final String FUND_NAME = "fund";
	public static final String FIXI = "BLOOMBERG_FIXINCOME";
	public static final String FIXI_NAME = "fixincome";
	public static final String BETC = "BLOOMBERG_ETC";
	public static final String BETC_NAME = "etc";
	public static final String BFLD = "IMPORTBLOOMBERGFIELDS";
	public static final String BFLD_NAME = "field";

	// TODO: Pasar y recoger los datos especificos para cada job en el map
	public static SDMJobType getJobType(Session session,
			Map<String, String> infoJob) {
		SDMJobType jobt = new SDMJobTypeDAO(session).findByName(infoJob
				.get("name"));

		if (jobt == null) {
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

	public static SDMJobType getJobType(Session session,
			Map<String, String> infoJob, String fileName) {

		List<SDMJobType> jobsType = new SDMJobTypeDAO(session).findAll();
		SDMJobType out = null;

		for (SDMJobType jt : jobsType) {

			if (jt.getName().equals(SDMUtils.EQUI)
					&& fileName.contains(SDMUtils.EQUI_NAME.subSequence(0,
							SDMUtils.EQUI_NAME.length()))) {
				out = jt;
			}
			if (jt.getName().equals(SDMUtils.FUND)
					&& fileName.contains(SDMUtils.FUND_NAME.subSequence(0,
							SDMUtils.FUND_NAME.length()))) {
				out = jt;
			}
			if (jt.getName().equals(SDMUtils.FIXI)
					&& fileName.contains(SDMUtils.FIXI_NAME.subSequence(0,
							SDMUtils.FIXI_NAME.length()))) {
				out = jt;
			}
			if (jt.getName().equals(SDMUtils.BETC)
					&& fileName.contains(SDMUtils.BETC_NAME.subSequence(0,
							SDMUtils.BETC_NAME.length()))) {
				out = jt;
			}
			if (jt.getName().equals(SDMUtils.BFLD)
					&& fileName.contains(SDMUtils.BFLD_NAME.subSequence(0,
							SDMUtils.BFLD_NAME.length()))) {
				out = jt;
			}
		}

		if (out != null)
			System.out.println("DEBUG: JobType name = " + out.getName());
		else {
			System.out.println("DEBUG: JobType Name = null");
			logger.error("Load Error: can not find the JobType in file "
					+ fileName);
		}
		System.out.println("DEBUG: fileName: " + fileName);

		return out;
	}

	public static State getState(Session session, String codeState) {
		StateId stateId = null;
		State state;
		Flow flow = (Flow) session.load(Flow.class,
				StaticDataWorkflow.STATICMESSAGE.getId());

		if (codeState.equalsIgnoreCase(CAStatesEVENTMESSAGEFlow.PRSD.getId())) {
			stateId = new StateId(flow, CAStatesEVENTMESSAGEFlow.PRSD.getId());
		}
		state = (State) session.load(State.class, stateId);

		return state;
	}
}
