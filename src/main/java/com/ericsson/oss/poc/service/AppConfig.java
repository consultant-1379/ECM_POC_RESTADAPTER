package com.ericsson.oss.poc.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * The singleton class to take care of the Application initialization and also
 * to provide access to the properties file used for application configuration.
 * 
 * 
 * @author evigkum
 *
 */

public class AppConfig {
 
	private static Properties props = null;
	
	private static String DEFAULT_APPCONFIG_FILE = "appconfig.properties";
	
	//If required move to props file later
	public static final String SUCCESS_CODE = "SUCCESS";

	static
	{
		String configFile = System.getProperty("appconfig.properties");
		configFile = (configFile != null && configFile.trim().length() > 0) ? configFile
				: DEFAULT_APPCONFIG_FILE;

		System.out.println(" Resolved the application config to : "+configFile);
		try
		{
			props = new Properties();
			props.load(new FileInputStream(new File(configFile)));
			//props.load(AppConfig.class.getClassLoader().getResourceAsStream(
			//		configFile));
			
			
			//Initalise the logger
			LogManager manager = LogManager.getLogManager();
			manager.readConfiguration(AppConfig.class.getClassLoader().getResourceAsStream(
					"logging.properties"));
			
		} catch (IOException e) {
			throw new RuntimeException("Failed to Load properties "	+ configFile);
		}
	} 
	/**
	 * Actually a dummy init method, calling this ensures
	 * that static block is executed or the class itself is built.
	 */
	public static void init(){
		
	  Logger LOGGER = Logger.getLogger(AppConfig.class .getName()); 
	  
	  LOGGER.info(" --------------~AppConfig intialisation successful~-----------------------");
	}
	
	/**
	 * 
	 * @param propName name of the property key
	 * @param defaultValue in case the key value is missing
	 * @return value corresponding to the key passed in.
	 */
	public static String getStringProperty(String propName,String defaultValue)
	{
		return props.getProperty(propName, defaultValue);
	}
	
	/**
	 * 
	 * @param propName name of the property key
	 * @param defaultValue in case the key value is missing
	 * @return value corresponding integer value to the key passed in.
	 */
	
	public static int getIntProperty(String propName,int defaultVal)
	{
		 
		int returnVal = defaultVal;
		
		try {
			returnVal =Integer.parseInt( props.getProperty(propName));
		} catch (NumberFormatException ignore) { }
		
		return returnVal;
	}
	
}
