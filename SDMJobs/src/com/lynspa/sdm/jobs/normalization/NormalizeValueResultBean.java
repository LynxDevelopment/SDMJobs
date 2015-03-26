package com.lynspa.sdm.jobs.normalization;

import java.io.Serializable;

import com.lynxspa.entities.securities.assets.AssetDetails;
import com.lynxspa.entities.securities.assets.AssetType;
import com.lynxspa.entities.securities.markets.SPMarket;

public class NormalizeValueResultBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6995597313077964785L;

	private String output=null;
	private String generatedScript=null; 
	private Exception exception=null;
	private AssetType type=null;
	private AssetDetails detail=null;
	private String isin;
	private String name;
	private String cusip=null;
	private String sedol=null;
	private String market;
	private String country= null;
	
	public NormalizeValueResultBean(){
		this.detail=new AssetDetails("NORMALIZER");
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getGeneratedScript() {
		return generatedScript;
	}

	public void setGeneratedScript(String generatedScript) {
		this.generatedScript = generatedScript;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public AssetType getType() {
		return type;
	}

	public void setType(AssetType type) {
		this.type = type;
	}

	public AssetDetails getDetail() {
		return detail;
	}

	public void setDetail(AssetDetails detail) {
		this.detail = detail;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCusip() {
		return cusip;
	}

	public void setCusip(String cusip) {
		this.cusip = cusip;
	}

	public String getSedol() {
		return sedol;
	}

	public void setSedol(String sedol) {
		this.sedol = sedol;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	
}
