package com.lynspa.sdm.jobs.normalization;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import bsh.Interpreter;

import com.lynxspa.entities.UpdateAuditor;
import com.lynxspa.entities.application.flow.Flow;
import com.lynxspa.entities.application.flow.State;
import com.lynxspa.entities.application.flow.StateId;
import com.lynxspa.entities.application.flow.operations.OperationStatus;
import com.lynxspa.entities.jobs.SDMRow;
import com.lynxspa.entities.jobs.SDMStaticRow;
import com.lynxspa.entities.jobs.SDMValue;
import com.lynxspa.entities.securities.assets.AssetDetails;
import com.lynxspa.entities.securities.assets.AssetType;
import com.lynxspa.entities.securities.assets.SecurityAsset;
import com.lynxspa.entities.securities.assets.providers.Provider;
import com.lynxspa.entities.securities.markets.SPMarket;
import com.lynxspa.hbt.utils.HibernateUtils;
import com.lynxspa.sdm.dictionaries.flows.StaticDataWorkflow;
import com.lynxspa.sdm.dictionaries.flows.states.StaticStatesSTATICMESSAGEFlow;
import com.lynxspa.sdm.entities.events.details.CAEventDetailExtended;
import com.lynxspa.sdm.managers.StaticConfigManager;
import com.lynxspa.sdm.processors.normalize.NormalizeScriptConfigBean;
import com.lynxspa.sdm.processors.normalize.utils.StaticMessageField;

public class BeanShellStaticDataNormalizeProcessor implements NormalizeValuesAdapter{
	
	private static Logger logger = Logger.getLogger(BeanShellStaticDataNormalizeProcessor.class);
	
	private StringBuffer buildScript(List<NormalizeScriptConfigBean> _scripts) throws Exception{
		
		logger.info("Start buildScript");
		
		StringBuffer reply=null;
		String[] fieldPathSplitted=null;
		String extension=null;
		String field=null;
		int position=0;

		reply=new StringBuffer();
		//Field Normalization
		for(NormalizeScriptConfigBean config:_scripts){
			if((config.getScript()!=null)&&(!"".equals(config.getScript()))){
				
				logger.info(" config.getScript " + config.getScript() + " " + config.getTableFieldPath());
				
				fieldPathSplitted=config.getTableFieldPath().split(":");
				if (fieldPathSplitted.length==2){
					extension="";
					field=fieldPathSplitted[0];
					position=Integer.parseInt(fieldPathSplitted[1]);
				}else{
					extension=fieldPathSplitted[0];
					field=(fieldPathSplitted.length>=2)? fieldPathSplitted[1] : null;
					position=(fieldPathSplitted.length==3)? Integer.parseInt(fieldPathSplitted[2]) : 0;
				}
				
				if(!config.isExtension()){
					
					reply.append("Object get"+extension+field+position+"(StaticEventMessageWrapper _message){\n");
					reply.append("	\ntry{\n");
					reply.append("		"+config.getScript());
					reply.append("	\n}catch(FieldNotFoundException e){\n");
					reply.append("		print(e);");
					reply.append("	\n}\n");
					reply.append("\n	return null;");
					reply.append("\n}\n");
					if("BODY".equals(extension)){
						reply.append("Object value"+extension+field+position+"=get"+extension+field+position+"(initialContext);\n");
					}else{
						reply.append("Object "+extension+field+position+"=get"+extension+field+position+"(initialContext);\n");
					}
				}
			}
		}
		//Field Recovery data by extension
		for(NormalizeScriptConfigBean config:_scripts){
			if((config.getScript()!=null)&&(!"".equals(config.getScript()))){
				fieldPathSplitted=config.getTableFieldPath().split(":");
				extension=fieldPathSplitted[0];
				if(config.isExtension()){
					reply.append("List get"+extension+"(StaticEventMessageWrapper _message){");
					reply.append("	"+config.getScript());
					reply.append("\n}\n");
					reply.append("List value"+extension+"Fields=get"+extension+"(initialContext);\n");
					reply.append("List value"+extension+"=new ArrayList();\n");
					reply.append("for(int ic1=0;ic1<value"+extension+"Fields.size();ic1++){\n");
					reply.append("	Map content=new HashMap();\n");
					for(NormalizeScriptConfigBean extensionConfigs:_scripts){
						if((!extensionConfigs.isExtension())&&(extensionConfigs.getScript()!=null)&&(!"".equals(extensionConfigs.getScript()))){
							String[] extensionfieldPathSplitted=extensionConfigs.getTableFieldPath().split(":");
							if(extension.equals(extensionfieldPathSplitted[0])){
								field=(extensionfieldPathSplitted.length>=2)? extensionfieldPathSplitted[1] : null;
								position=(extensionfieldPathSplitted.length==3)? Integer.parseInt(extensionfieldPathSplitted[2]) : 0;
								reply.append("	content.put(\""+field+position+"\",get"+extension+field+position+"(new StaticEventMessageWrapper(session,value"+extension+"Fields.get(ic1),initialContext.getNormalizedOperation(),initialContext.getNormalizedProvider(),initialContext.getNormalizedEventType())));\n");
							}
						}
					}
					reply.append("	value"+extension+".add(content);\n");
					reply.append("\n}\n");
				}
			}
		}
		
		logger.debug(" buildScript reply: " + reply);
		
		return reply;
	}
	
	private Interpreter prepareContext(Session _session, SDMRow row,PrintStream _stream) throws Exception {

		logger.info("Start prepareContext");
		
		Interpreter reply=null;
		List<StaticMessageField> fields=null;
		StringBuffer initialScript=null;

		_session.lock(row, LockMode.NONE);
		initialScript=new StringBuffer();
		initialScript.append("import java.util.*;\n");
		initialScript.append("import java.text.*;\n");
		initialScript.append("import org.hibernate.Session;\n");
		initialScript.append("import com.lynxspa.entities.jobs.SDMJobField;\n");
		initialScript.append("import com.lynxspa.sdm.processors.normalize.utils.StaticMessageField;\n");
		initialScript.append("import com.lynxspa.sdm.processors.normalize.utils.StaticEventMessageWrapper;\n");
		//initialScript.append("import com.lynxspa.entities.securities.assets.messages.AssetMessageType;\n");
		initialScript.append("import com.lynxspa.sdm.processors.normalize.utils.FieldNotFoundException;\n");
		initialScript.append("import com.lynxspa.sdm.processors.normalize.utils.SDMSwiftParser;\n");
		//initialScript.append("import com.lynxspa.sdm.processors.normalize.utils.MessageField;\n");
		initialScript.append("import com.prowidesoftware.swift.*;\n");
		initialScript.append("Session session=null;\n");
		initialScript.append("List fields=null;\n");
		//initialScript.append("AssetMessageType messageType=null;\n");
		initialScript.append("StaticEventMessageWrapper initialContext=null;\n");
		reply=new Interpreter(null,_stream,_stream,false);
		reply.eval(initialScript.toString());
		reply.set("session",_session);
		fields=new ArrayList<StaticMessageField>();
		
		for(SDMValue sdmValue:row.getSdmValues()){
			
			if((sdmValue.getValue()!=null)&&(sdmValue.getValue().trim().length()>0)){
				
				if (sdmValue.getJobField()==null){
					System.out.println("El valor "+sdmValue.getId() + " del row " +row.getId() +" no tiene field asociado");
					logger.info("El valor "+sdmValue.getId() + " del row " +row.getId() +" no tiene field asociado");
				}else{
					fields.add(new StaticMessageField(sdmValue.getJobField(),sdmValue.getValue()));
					logger.debug("El valor "+sdmValue.getId() + " del row " +row.getId() +" tiene field " + sdmValue.getJobField() + " " + sdmValue.getValue());
				}
			}
		}
		
		if (fields.size()>0){
		
			reply.set("fields",fields);
	//		reply.set("normalizedProvider",_session.load(Provider.class,row.getJob().getJobType().getFields().get("ProviderId").getFieldName()));
	//		reply.set("normalizedAssetType",_session.load(AssetType.class,row.getJob().getJobType().getFields().get("AssetTypeId").getFieldName()));
			reply.set("originalMessage",row.getValue());
			//reply.eval("initialContext=new StaticEventMessageWrapper(session,fields,normalizedProvider,normalizedAssetType,originalMessage);\n\n\n");
			System.out.println();
			reply.eval("initialContext=new StaticEventMessageWrapper(session,fields,originalMessage);\n\n\n");
		}else{
			System.out.println("Row " +row.getId() + " value: "+row.getValue() +" No tiene fields");
			logger.info("Row " +row.getId() + " value: "+row.getValue() +" No tiene fields");
		}
		logger.info("prepareContext reply " +reply.toString());
		return reply;
	}

	private void setExtensionFieldValues(CAEventDetailExtended _extensions,Map<String,Object> _values) throws Exception{

		logger.info("Start setExtensionFieldValues");
		
		Object value=null;
		
		for(String _valueName:_values.keySet()){
			value=_values.get(_valueName);
			if(value!=null){
				_extensions.set(_valueName, value);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private NormalizeValueResultBean recoverDataFromScriptContext(NormalizeValueResultBean _result,List<NormalizeScriptConfigBean> _scripts,Interpreter _context) throws Exception{

		logger.info("Start recoverDataFromScriptContext");
		
		NormalizeValueResultBean reply=null;
		String[] fieldPathSplitted=null;
		String extension=null;
		String field=null;
		int position=0;
		Object value=null;

		reply=_result;	
		for(NormalizeScriptConfigBean config:_scripts){
			if((config.getScript()!=null)&&(!"".equals(config.getScript()))){
				fieldPathSplitted=config.getTableFieldPath().split(":");
				if(fieldPathSplitted.length==2){
					extension="";
					field= fieldPathSplitted[0];
					position= Integer.parseInt(fieldPathSplitted[1]);
					if((value=_context.get(extension+field+position))!=null)
						reply.getDetail().set(field+position,value);
				}else{
					extension=fieldPathSplitted[0];
					if(!config.isExtension()){
						if("BODY".equals(extension)){
							field=(fieldPathSplitted.length>=2)? fieldPathSplitted[1] : null;
							position=(fieldPathSplitted.length==3)? Integer.parseInt(fieldPathSplitted[2]) : 0;
							if((value=_context.get("value"+extension+field+position))!=null)
								reply.getDetail().set(field+position,value);
						}
					}
				}
			}
		}

		return reply;
	}

	
	protected NormalizeValueResultBean scriptsProcessing(Session _session,List<NormalizeScriptConfigBean> _scripts, SDMRow row) throws Exception {
		
		logger.info("Start scriptsProcessing");
		
		NormalizeValueResultBean reply=null;
		Interpreter beanshellContext=null;
		StringBuffer script=null;
		PrintStream stream=null;
		ByteArrayOutputStream outputStream=null;
		
		reply=new NormalizeValueResultBean();
		if((_scripts!=null)&&(_scripts.size()>0)){
			
			logger.info("_scripts size: " + _scripts.size());
			
			//Prepare bean shell script
			script=buildScript(_scripts);
			reply.setGeneratedScript(script.toString());
			
			
			logger.info("_scripts string: " + script.toString());
			logger.info("_scripts replyy: " + reply);
			
			//Evaluate Script
			outputStream=new ByteArrayOutputStream();
			stream=new PrintStream(outputStream,false,"ISO-8859-1");
			beanshellContext=prepareContext(_session,row,stream);
			//Evaluate Script
			try{
				beanshellContext.eval(script.toString());
				reply=recoverDataFromScriptContext(reply,_scripts,beanshellContext);
				
				logger.info("_scripts final: " + reply);
				
			}catch (Exception e) {
				reply.setException(e);
				logger.error(e);
			}
			stream.flush();
			reply.setOutput(outputStream.toString());
			stream.close();
		}
		
		logger.info("_scripts return: " + reply);
		
		return reply;
	}

	
	public void normalize(Session _session, StatelessSession statelessSession, String _user, String _locale) throws Exception{
		
		logger.info("Start normalize");
		
		NormalizeValueResultBean result=null;
		List<NormalizeScriptConfigBean> scripts=null;
		
		String query = "from SDMStaticRow as row where row.operationStatus.state.id.code=:status";
		Query _eventQuery= _session.createQuery(query);
		_eventQuery.setParameter("status",StaticStatesSTATICMESSAGEFlow.PRSD.getId());
		List<SDMStaticRow> sdmRows =new ArrayList<SDMStaticRow>();
		sdmRows = _eventQuery.list();

		result=new NormalizeValueResultBean(); 
		
		logger.info("sdmRows size " + sdmRows.size());
		
		if (!sdmRows.isEmpty()){
			//OperationStatus opeStatus = (OperationStatus)_session.load(OperationStatus.class, StaticStatesSTATICMESSAGEFlow.NORM.getId());
			int rowNormalized = 0;
			HibernateUtils.beguinTransaction(statelessSession);
			
			for(SDMStaticRow row:sdmRows){
				try{
					
					logger.info("row " + row.getSecurityIsin() + " " + row.getSecurityName() + " " + row.getValue());
						
				query = "from Provider where code=:codeProvider";
				_eventQuery= _session.createQuery(query);
				_eventQuery.setParameter("codeProvider",row.getJob().getJobType().getFields().get("ProviderId").getFieldName());
				Provider provider = (Provider)_eventQuery.uniqueResult();
				AssetType assetType = (AssetType)_session.load(AssetType.class,row.getJob().getJobType().getFields().get("AssetTypeId").getFieldName()); 
				
				scripts=StaticConfigManager.getInstance().getNormalizationScripts(_session, provider, assetType,row.getJob().getJobType().getEnterprise());
				//call script processing
				
				logger.info("scripts size " + scripts.size());
				
				if((scripts!=null)&&(scripts.size()>0)){
					
						logger.info(" entra en if de script");

						//Search for scripts configuration
						result = executeNormalization(_session, row, provider, assetType,scripts);
		//				if(result==null)
		//					throw new FPMException(LogErrorDict.NORMALIZATION_RESULT_NULL,new Object[]{row.getId()});
		//				if(result.getException()!=null)
		//					throw new FPMException(LogErrorDict.NORMALIZATION_SCRIPTING_ERROR,new Object[]{row.getId()},result.getException());
						if(result!=null){
							
							logger.info("result isin " + result.getIsin());
							
							AssetDetails assetDetails = result.getDetail();
							HibernateUtils.customSave(statelessSession,	assetDetails, _user);
							SecurityAsset asset = new SecurityAsset(_user);
							asset.setAssetDetail(assetDetails);
							asset.setAssetType(assetType);
							asset.setAuditor(new UpdateAuditor(_user));
							asset.setProvider(provider);
							asset.setOperationStatus(getOperationStatus(StaticStatesSTATICMESSAGEFlow.NORM,_session));
							asset.setName(row.getSecurityName());
							asset.setIsin(row.getSecurityIsin());
							asset.setSecurityMessageId(row.getId());
							
							if (row.getSecurityMic()!=null){
								query = "from SPMarket as market where market.mic=:mic";
								_eventQuery= _session.createQuery(query);
								_eventQuery.setParameter("mic",row.getSecurityMic());
								asset.setMarket((SPMarket)_eventQuery.uniqueResult());
							}
							
							HibernateUtils.customSave(statelessSession,	asset, _user);
							
							row.setOperationStatus(getOperationStatus(StaticStatesSTATICMESSAGEFlow.NORM,_session));

							System.out.println("session: " + statelessSession);
							System.out.println("Row: " + row);
							System.out.println("User: " +_user);
							
							logger.debug("session: " + statelessSession + "Row: " + row + "User: " +_user);
							
							HibernateUtils.customUpdate(statelessSession,	row, _user);
							HibernateUtils.commitTransaction(statelessSession);
							HibernateUtils.beguinTransaction(statelessSession);
							rowNormalized++;
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					System.err.println("Error normalizando row "+row.getId()+ " "+e.getMessage());
					logger.error("Error normalizando row "+row.getId()+ " "+e.getMessage());
				}
			}
			System.out.println("Actualizados "+rowNormalized+" mensajes");
			logger.info("Actualizados "+rowNormalized+" mensajes");
			HibernateUtils.commitTransaction(statelessSession);
			
		}
	}
	
	
	private NormalizeValueResultBean  executeNormalization (Session _session, SDMRow row, Provider provider, AssetType assetType, List<NormalizeScriptConfigBean> scripts) throws Exception{
		NormalizeValueResultBean reply=null;

		reply=scriptsProcessing(_session,scripts,row);
		
		return reply;
	}
	
	public NormalizeValueResultBean test(Session _session,List<NormalizeScriptConfigBean> _scripts, SDMRow row) throws Exception {
		return scriptsProcessing(_session,_scripts,row);
	}
	
	public OperationStatus getOperationStatus(StaticStatesSTATICMESSAGEFlow state, Session session){
		OperationStatus operationStatus;
		Flow flow = (Flow)session.get(Flow.class, StaticDataWorkflow.STATICMESSAGE.getId());
		String code = state.getId();
		StateId stateId = new StateId(flow, code);
		operationStatus = new OperationStatus((State)session.get(State.class, stateId));
		
		return operationStatus;
	}
}
