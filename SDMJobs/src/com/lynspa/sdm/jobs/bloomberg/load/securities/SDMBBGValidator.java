package com.lynspa.sdm.jobs.bloomberg.load.securities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.lynspa.sdm.jobs.bloomberg.load.securities.exceptions.SDMValidationException;

/**
 * SDMBBGValidator implements SDMValidator interface
 * 
 * @author Esteban Calderon
 * 
 */
public class SDMBBGValidator implements ISDMValidator {

	private final String[] validators = { "START-OF-FILE", "START-OF-FIELDS",
			"END-OF-FIELDS", "START-OF-DATA", "END-OF-DATA", "END-OF-FILE" };

	public SDMBBGValidator() {
	}

	@Override
	public boolean validate(File file) {

		int i = 0;
		boolean result;

		BufferedReader bFile;
		try {
			bFile = new BufferedReader(new FileReader(file));

			String line = bFile.readLine();
			while (line != null && i < validators.length) {
				if (line.equals(validators[i]))
					i++;
				line = bFile.readLine();
			}
			bFile.close();

			if (i != validators.length)
				throw new SDMValidationException();
			
			result = true;
		} catch (FileNotFoundException e) {
			result = false;
		} catch (IOException e) {
			result = false;
		}
		
		return result;
	}
}
