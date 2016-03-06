/*
    BEEM is a videoconference application on the Android Platform.

    Copyright (C) 2009 by Frederic-Charles Barthelery,
                          Jean-Manuel Da Silva,
                          Nikita Kozlov,
                          Philippe Lago,
                          Jean Baptiste Vergely,
                          Vincent Veronis.

    This file is part of BEEM.

    BEEM is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BEEM is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BEEM.  If not, see <http://www.gnu.org/licenses/>.

    Please send bug reports with examples or suggestions to
    contact@beem-project.com or http://dev.beem-project.com/

    Epitech, hereby disclaims all copyright interest in the program "Beem"
    written by Frederic-Charles Barthelery,
               Jean-Manuel Da Silva,
               Nikita Kozlov,
               Philippe Lago,
               Jean Baptiste Vergely,
               Vincent Veronis.

    Nicolas Sadirac, November 26, 2009
    President of Epitech.

    Flavien Astraud, November 26, 2009
    Head of the EIP Laboratory.

*/
package co.beem.project.beem.utils;

import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.connection.FacebookTextConnectionListener;
import hoahong.facebook.messenger.ui.android.ChatActivity;
import hoahong.facebook.messenger.ui.android.FbTextMainActivity;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Manage broadcast disconnect intent.
 * @author nikita
 */
public class BeemBroadcastReceiver extends BroadcastReceiver {

    /** Broadcast intent type. */
	
	public static final String Log_tag = "";
    public static final String BEEM_CONNECTION_CLOSED = "BeemConnectionClosed";
    public static final String BEEM_CONNECTION_CONNECTED = "BeemConnectionConnected";
    public static final String BEEM_CONNECTION_CONNECTING = "BeemConnectionConnecting";
    public static final String BEEM_CONNECTION_CONNECTING_In = "BeemConnectionConnecting_in";
    public static final String BEEM_CONNECTION_DISCONNECT = "BeemConnectionDisconnecting";
    
    public static final String BEEM_NEW_MESSAGE_ARRIVED = "newmessagearrived";
    
    public static final String TIME_CONNECTIONG_IN = "time_connecting_in";
    //public static final String BEEM_CONNECTION_NO_INTERNET = "BeemConnectionNoInternet";
    /**
     * constructor.
     */
    public BeemBroadcastReceiver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
	String intentAction = intent.getAction();
	
	
	
	if (intentAction.equals(BEEM_CONNECTION_CLOSED)) {
	    CharSequence message = intent.getCharSequenceExtra("message");
	    //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	    if (context instanceof Activity) {
		Activity act = (Activity) context;
		if (context instanceof FacebookTextConnectionListener){
			((FacebookTextConnectionListener) context).onDisconnect();
		}
		//act.finish();
		// The service will be unbinded in the destroy of the activity.
	    }
	    
	} else if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
	    if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
		
		//context.stopService(new Intent(context, FacebookTextService.class));
		if (context instanceof FacebookTextConnectionListener){
			((FacebookTextConnectionListener) context).onNoInternetConnection();
			Log.d(Log_tag, "No internet");
		}
	    }
	}
	else if (intentAction.equals(BEEM_CONNECTION_DISCONNECT)) {
		FbTextApplication.isRatable = false;
		if (context instanceof FacebookTextConnectionListener){
			((FacebookTextConnectionListener) context).onDisconnect();
			Log.d(Log_tag, "Disconnect");
		}
	}
	else if (intentAction.equals(BEEM_CONNECTION_CONNECTED)) {
		FbTextApplication.isRatable = true;
		if (context instanceof FacebookTextConnectionListener){
			((FacebookTextConnectionListener) context).onConnected();;
			Log.d(Log_tag, "connected");
		}
	}else if (intentAction.equals(BEEM_CONNECTION_CONNECTING)) {
		
		if (context instanceof FacebookTextConnectionListener){
			((FacebookTextConnectionListener) context).onConnecting();
			Log.d(Log_tag, "connecting");
		}
	}else if (intentAction.equals(BEEM_NEW_MESSAGE_ARRIVED)){
		if (context instanceof FbTextMainActivity){
			((FbTextMainActivity) context).onNewMessageArrived();
		}
	}else if (intentAction.equals(BEEM_CONNECTION_CONNECTING_In)) {
		FbTextApplication.isRatable = false;
		if (context instanceof FacebookTextConnectionListener){
			int seconds = intent.getIntExtra(TIME_CONNECTIONG_IN, 0);
			Log.d(Log_tag, "Connecting_in");
			((FacebookTextConnectionListener) context).onConnectingIn(seconds);
		}
	}else if (intentAction.equals(FbTextApplication.IMAGE_URL_FB) ){
		Log.d(Log_tag, "receive broadcats fb url finished: " + FbTextApplication.IMAGE_URL_FB);
		if (context instanceof ChatActivity)
		((ChatActivity)context).onSendImageFinished(intent);
	}else if (intentAction.equals(FbTextApplication.SEND_INVITATION_FAILED)){
		Log.d(Log_tag, "receive broadcats send invitation failed ");
		if (context instanceof FbTextMainActivity){
			((FbTextMainActivity)context).onSendInvitationFailed();
		}
	}else if (intentAction.equals(FbTextApplication.SEND_INVITATION_SUCCESS)){
		Log.d(Log_tag, "receive broadcats send invitation failed ");
		if (context instanceof FbTextMainActivity){
			((FbTextMainActivity)context).onSendInvitationSuccess();
		}
	}
	
	
    }
}
