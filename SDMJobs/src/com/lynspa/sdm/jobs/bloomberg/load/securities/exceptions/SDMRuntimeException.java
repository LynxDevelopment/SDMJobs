package com.lynspa.sdm.jobs.bloomberg.load.securities.exceptions;

public class SDMRuntimeException extends RuntimeException {

	public SDMRuntimeException(){
		super();
	}
	
	public SDMRuntimeException(String message){
		super(message);
	}
}
