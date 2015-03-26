package com.lynspa.sdm.jobs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SDMLoaderUtilities {

	public static Properties leerConfiguracion(String fichero) {
		Properties propiedades = new Properties();
		try {
			propiedades.load(new FileInputStream(fichero));

		} catch (FileNotFoundException e) {
			System.out.println("Error, El archivo " + fichero + " no exite");
		} catch (IOException e) {
			System.out.println("Error, No se puede leer el archivo" + fichero);
		}
		return propiedades;
	}
}
