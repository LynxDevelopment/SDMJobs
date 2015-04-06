package com.lynspa.sdm.jobs.bloomberg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * 
 * @author Esteban Calderon
 * File Iterator, return line by line;
 */
public class BBGFileIterator implements Iterator {

	private final String STARTOFDATA = "START-OF-DATA";
	private final String ENDOFDATA = "END-OF-DATA";
	private final String HEADER = "HEADER";
	private final String DATEFORMAT = "DATEFORMAT";
	
	private String nextLine = null;
	private String dateFormat = null;
	private BufferedReader buffer = null;
	private boolean header = false;
	
	public BBGFileIterator(File file) {
		try {
			buffer = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			this.nextLine = null;
		}
		new BBGFileIterator(buffer);
	}
	
	public BBGFileIterator(BufferedReader buffer){
		this.buffer = buffer;
		try {
			String line = buffer.readLine();
			while(!line.equals(STARTOFDATA)){
				if(line.startsWith(HEADER)){
					if(line.endsWith("yes"))
						header = true;
				}
				if(line.startsWith(DATEFORMAT)){
					line.replaceAll("m", "M");
					String [] stringSplit = line.split("=");
					dateFormat = stringSplit[1];
				}
				line = buffer.readLine();
			}
			if(header)
				buffer.readLine();
			this.nextLine = this.buffer.readLine();
		} catch (IOException e) {
			this.nextLine = null;
			this.buffer = null;
		}
	}
	
	@Override
	public boolean hasNext() {
		try {
			 if( nextLine == null) {
				 if (buffer != null) {
					 buffer.close();
				 }
		       }
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
		return nextLine != null;
	}

	@Override
	public String next() {
		String out = nextLine;
		
		if(buffer != null){
			try {
				nextLine = buffer.readLine();
				if(nextLine.equals(ENDOFDATA))
					nextLine = null;
			} catch (IOException e) {
				nextLine = null;
			}
		}
		
		return out;
	}

	/**
	 * No tiene efecto;
	 */
	@Override
	public void remove() {
		
	}

}
