/*
 * Copyright (c) 2014 Pendulab Inc.
 * 111 N Chestnut St, Suite 200, Winston Salem, NC, 27101, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Pendulab Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Pendulab.
*/
package hoahong.facebook.messenger.fbclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.gson.Gson;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class MMConnector
{
	public static final String SESSION_HEADER_NAME = "Set-Cookie";
	public static final String DATE_HEADER_NAME = "Date";
	public static final int TIME_OUT_CONNECTION = 3000; 
	public static final int TIME_OUT_SOCKET  = 5000; 
	public MMConnector()
	{
	}

	public  AvatarFbReponseData sendRequest(String id, int size) 
	{
		AvatarFbReponseData responseData = null;
		try
		{
			Gson gson = new Gson();
			
			String link = "https://graph.facebook.com/" + id + "/picture?type=normal&redirect=0&width="+ size+"&height=" + size;
			// prepare request
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT_CONNECTION);
			HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT_SOCKET);
			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpGet postRequest = new HttpGet(link);
			//postRequest.setHeader("Content-Type", "application/json");
			//set session
			
			
			// execute request and send response message to MMClientContext
			HttpResponse httpResponse = httpClient.execute(postRequest);
			//System.out.println("response: " + httpResponse);
			
			/**** handle response for each command ***/
			//extract json Object
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
			// read entire content
			StringBuilder buffer = new StringBuilder();
			String ln;
			while ((ln = reader.readLine()) != null)
			{
				buffer.append(ln);
			}
			if (buffer.length() == 0)
			{
				// empty? do something... 
				throw new Exception("Empty response");
			} else
			{
				responseData = gson.fromJson(buffer.toString(), AvatarFbReponseData.class);
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// are you sure you have to do this?
//	        httpClient.getConnectionManager().shutdown();
	    }
		return responseData;
	}
	
	
	public String getValueFromHeader (String headerName, HttpResponse response){
		return response.getHeaders(headerName)[0].getValue();
	}
	
	public static boolean isOnline(Context activity) {
	    ConnectivityManager cm =
	        (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	
	//main method to test
	public static void main(String[] args) {
		
//		new MMConnector().sendRequest("", "", new LoginRequestData("539148240cf2b925cdce89a5", "84c3028b-beb0-3b7d-ac47-5f246d9632a9", "1.0.1"));
	}
}
