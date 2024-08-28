package com.ericsson.oss.poc.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import com.ericsson.oss.poc.util.Base64Coder;
import com.ericsson.oss.poc.vos.ResponseVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * 
 * This is the adapter class for calling the REST service. 
 * For every REST call a public method would exist.
 * And other utility methods common across operations are private.
 * 
 * @author evigkum
 *
 */

public class RestAdapter {

	private final static Logger LOGGER = Logger.getLogger(RestAdapter.class .getName()); 

	private final static Logger REQUESTLOGGER = Logger.getLogger(RestAdapter.class.getName()+".RIO"); 

	public static void main(String[] args) {   

		if(args == null || args.length<2)
		{
			System.err.println("Wrong usage pls pass vmName vmImage and vnicCardName as arguments");
			System.exit(1);
		}

		String vmName = args[0];
		String vmImage = args[1];
		String vnicCardName = args[2];

		try
		{
			AppConfig.init();

			LOGGER.info("starting the main class ");

			new RestAdapter().createVM(vmName,vmImage,vnicCardName); 

			LOGGER.info("ending the main class ");

		}catch(Throwable ex)
		{
			ex.printStackTrace();
			System.err.println("Error in REST call :"+ex);
			System.exit(1);

		}

	} 


	/**
	 * This the method which calls the REST post operation.
	 * 
	 * @param vmName VM name to be created.
	 * @param vmImage VM Image name.
	 * @param vnicCardName vnic card name.
	 * 
	 * @return a string containing "SUCCESS" for successful 
	 *         or runtime exception for the creation request.
	 */


	public String createVM(String vmName,String vmImage,String vnicCardName)
	{

		LOGGER.entering(RestAdapter.class.getName(), "createVM");

		//Get the JSON Template from props file
		String jsonTempl = AppConfig.getStringProperty("createvm-vnic.template", "");
		jsonTempl = jsonTempl.trim();

		//Replace the standard configurable Params
		jsonTempl = jsonTempl.replace("$VM_VHD_TYPE$", AppConfig.getStringProperty("vmhdName", ""));
		jsonTempl = jsonTempl.replace("$VDC_ID$", AppConfig.getStringProperty("vdcID", ""));
		jsonTempl = jsonTempl.replace("$VN_ID$", AppConfig.getStringProperty("vnID", ""));
		jsonTempl = jsonTempl.replace("$TENANT_NAME$", AppConfig.getStringProperty("tenantName", ""));


		//Replace the params for VM Creation
		jsonTempl = jsonTempl.replace("$VM_IMG_NAME$", vmImage);
		jsonTempl = jsonTempl.replace("$VM_NAME$", vmName);
		jsonTempl = jsonTempl.replace("$VNIC_CARD_NAME$", vnicCardName);

		JsonObject jsonRequest = (JsonObject)new JsonParser().parse(jsonTempl);

		LOGGER.info("starting the rest call ");

		String urlStr = AppConfig.getStringProperty("rest.server.endpoint", "") + AppConfig.getStringProperty("rest.server.getvdcs.uri", "");

		LOGGER.info("Endpoint URL build --> : "+urlStr);

		HttpURLConnection urlConnection = null;
		String returnResponse = ""; 


		try {

			URL url = new URL(urlStr);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setRequestProperty("Content-Type", "application/json");
			
			 
			//Set the generic header string
			setHeaderRequestProperty("rest.server.header1",urlConnection);
			setHeaderRequestProperty("rest.server.header2",urlConnection);
			setHeaderRequestProperty("rest.server.header3",urlConnection);
			setHeaderRequestProperty("rest.server.header4",urlConnection);

			/*if(AppConfig.getStringProperty("rest.server.header1", "")!=null &&						
					AppConfig.getStringProperty("rest.server.header1", "").trim().length()>0 )
			{
				String headerStr = AppConfig.getStringProperty("rest.server.header1", "");
				String headerName = headerStr.split(":")[0];
				String headerValue = headerStr.split(":")[1];					
				urlConnection.setRequestProperty(headerName, headerValue);
				LOGGER.info("headerName : "+headerName + "headerValue --> "+headerValue);

			}*/
 
			urlConnection.setDoOutput(true);
			
			//Set the authentication header
			urlConnection.setRequestProperty("Authorization", RestAdapter.buildAuthHeader());

			REQUESTLOGGER.info("REQUEST : "+jsonRequest.toString());

			OutputStream os = urlConnection.getOutputStream();
			os.write(jsonRequest.toString().getBytes());
			os.flush();

			if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				
				LOGGER.warning("Failed REST call for  HTTP error code :"+urlConnection.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : "+ urlConnection.getResponseCode());
			}

			String output = readResponse(urlConnection.getInputStream());

			LOGGER.info("rest call succeed with response code 200");

			REQUESTLOGGER.info("RESPONSE :"+output);

			GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
					JsonObject.class,new JsonObjectTypeAdapter());

			Gson gson = builder.create();

			ResponseVO responseJson = (ResponseVO) gson.fromJson(output, ResponseVO.class);

			LOGGER.info("response JSON unmarshalled : "+responseJson); 
 
			if(AppConfig.SUCCESS_CODE.equalsIgnoreCase(
					responseJson.getStatus().getReqStatus()))
			{
				returnResponse = "SUCCESS";
			}


			LOGGER.info("SUCCESS returned for Create : ");

		} catch (MalformedURLException  e) { 
			LOGGER.warning("Failed REST call for MalformedURLException : "+e.getMessage());
			throw new RuntimeException("Failed REST call for MalformedURLException",e);

		} catch (IOException e) { 
			LOGGER.warning("Failed REST call for IOException : "+e.getMessage());
			throw new RuntimeException("Failed REST call for IOException",e);
		}

		finally
		{
		 
			urlConnection.disconnect();
		}


		LOGGER.exiting(RestAdapter.class.getName(), "createVM");

		return returnResponse;
	}


	/**
	 * The get VDC method to invoke REST GET method to get the VDCS .
	 * The tenant  name and other filters are part of the URL. The 
	 * endpoint URL are configured in the appconfig.properties.
	 * 
	 * 
	 * @return <code>ResponseVO</code> containing the response.
	 */

	public ResponseVO getVDCS()
	{

		LOGGER.entering(RestAdapter.class.getName(), "getVDCS"); 

		LOGGER.info("starting the rest getVDCS call ");

		String urlStr = AppConfig.getStringProperty("rest.server.endpoint", "") + 
				AppConfig.getStringProperty("rest.server.getvdcs.uri", "");

		LOGGER.info("REST endpoint url built to : "+urlStr);

		HttpURLConnection urlConnection = null;
		ResponseVO responseJson =  null;
		try {

			URL url = new URL(urlStr);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Accept", "application/json");

			//Set the authentication header
			urlConnection.setRequestProperty("Authorization", RestAdapter.buildAuthHeader());


			REQUESTLOGGER.warning("REQUEST :");

			if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : "+ urlConnection.getResponseCode());
			}

			String output = readResponse(urlConnection.getInputStream());

			LOGGER.info("rest call succeed with response code 200");

			REQUESTLOGGER.warning("RESPONSE :"+output);

			GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
					JsonObject.class,new JsonObjectTypeAdapter());

			Gson gson = builder.create();

			responseJson = (ResponseVO) gson.fromJson(output, ResponseVO.class);

			LOGGER.info("response JSON unmarshalled : "+responseJson);





		} catch (MalformedURLException  e) { 
			LOGGER.warning("Failed REST call for MalformedURLException : "+e.getMessage());
			throw new RuntimeException("Failed REST call for MalformedURLException");

		} catch (IOException e) { 
			LOGGER.warning("Failed REST call for IOException : "+e.getMessage());
			throw new RuntimeException("Failed REST call for IOException");
		}

		finally
		{
			urlConnection.disconnect();
		}


		LOGGER.exiting(RestAdapter.class.getName(), "getVDCS");

		return responseJson;
	}

	/**
	 * Inputstream to String converter.
	 * @param is Inputsream of data.
	 * @return a string extracted from Inputstream.
	 */

	private static String readResponse(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";

	}
	/**
	 * This method is used to build the authentication header.
	 * 
	 * @return authString "Basic "+Base64Encoded(user:password)
	 */

	private static String buildAuthHeader(){

		String name = AppConfig.getStringProperty("rest.server.auth.username", "");
		String password = AppConfig.getStringProperty("rest.server.auth.password", "");

		LOGGER.info("Authentication user Name  "+name+" Password (only for debug to be removed) --> "+password);

		String authString = name + ":" + password;	

		String authStringEnc = Base64Coder.encodeString(authString);

		LOGGER.info("Authentication authStringEnc --> "+("Basic " + authStringEnc));

		return "Basic " + authStringEnc;
	} 

	/**
	 * This is the GSON deserializer for mapping the JsonObject from string.
	 * 
	 *
	 */

	private static class JsonObjectTypeAdapter implements JsonDeserializer<JsonElement>
	{
		public JsonObject deserialize(JsonElement json, Type JsonObject, JsonDeserializationContext context)
				throws JsonParseException {
			return  (JsonObject)json;
		}
	} 

	private static void setHeaderRequestProperty(String headerProperty,HttpURLConnection urlConnection){
		if(AppConfig.getStringProperty(headerProperty, "")!=null &&                        
				AppConfig.getStringProperty(headerProperty, "").trim().length()>0 &&
				AppConfig.getStringProperty(headerProperty, "").contains(":"))
		{
			String headerStr = AppConfig.getStringProperty(headerProperty, "");
			String headerName = headerStr.split(":")[0];
			String headerValue = headerStr.split(":")[1];                    
			urlConnection.setRequestProperty(headerName, headerValue);
			LOGGER.info("headerName : "+headerName + "headerValue --> "+headerValue);

		}
	}
}
