package com.lynspa.sdm.jobs.bloomberg.load.securities;

import java.io.File;

/**
 * SDMValidator interface
 * @author Esteban Calderon
 *
 */
public interface ISDMValidator {

	public boolean validate(File file);
}
