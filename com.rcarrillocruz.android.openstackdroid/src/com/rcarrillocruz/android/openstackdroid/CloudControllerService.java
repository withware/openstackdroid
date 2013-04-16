package com.rcarrillocruz.android.openstackdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

public class CloudControllerService extends IntentService {
	public static final String TAG = CloudControllerResultReceiver.class.getName();
	public static final String GET_TOKEN_OPERATION = "com.rcarrillocruz.android.openstackdroid.GetTokenOperation";
	
	public static final String OPERATION = "com.rcarrillocruz.android.openstackdroid.OPERATION";
	public static final String PARAMS = "com.rcarrillocruz.android.openstackdroid.PARAMS";
	public static final String RECEIVER = "com.rcarrillocruz.android.openstackdroid.RECEIVER";

	public CloudControllerService() {				
		super("CloudControllerService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Uri data = intent.getData();
		Bundle extras = intent.getExtras();
		
		String operation = extras.getString(OPERATION);
		ResultReceiver receiver = extras.getParcelable(RECEIVER);
		Bundle params = extras.getParcelable(PARAMS);

		String tenantId = params.getString("tenantId");
		String token = null;
		
		ApiOperation apiOperation = getOperationInstance(operation);

		HttpRequestBase request = apiOperation.invoke(data, token, tenantId, params);

		HttpResponse response = null;
		response = executeOperation(request);
		//android.os.Debug.waitForDebugger();
		returnResultstoReceiver(response, receiver);
	}

	private ApiOperation getOperationInstance(String operation) {
		// TODO Auto-generated method stub
		ApiOperation apiOperation = null;
		
		try {
			apiOperation = (ApiOperation) Class.forName(operation).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return apiOperation;
	}

	private HttpResponse executeOperation(HttpRequestBase request) {
		// TODO Auto-generated method stub
		HttpResponse response = null;
		HttpClient client = new DefaultHttpClient();
		
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	private void returnResultstoReceiver(HttpResponse response,
			ResultReceiver receiver) {
		// TODO Auto-generated method stub
		Bundle results = new Bundle();

		try {
			results.putString("results", EntityUtils.toString(response.getEntity()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		receiver.send(response.getStatusLine().getStatusCode(), results);
	}
	
	private static List<BasicNameValuePair> paramsToList(Bundle params) {
        ArrayList<BasicNameValuePair> formList = new ArrayList<BasicNameValuePair>(params.size());
        
        for (String key : params.keySet()) {
            Object value = params.get(key);
            formList.add(new BasicNameValuePair(key, value.toString()));
        }
        
        return formList;
    }
}