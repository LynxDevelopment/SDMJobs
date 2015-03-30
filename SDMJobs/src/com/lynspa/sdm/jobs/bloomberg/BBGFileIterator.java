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
	
	private String nextLine = null;
	private BufferedReader buffer = null;
	
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
			while(!line.equals(STARTOFDATA))
				line = buffer.readLine();
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
