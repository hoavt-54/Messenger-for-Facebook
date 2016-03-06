/*
    BEEM is a videoconference application on the Android Platform.

    Copyright (C) 2009-2011 by Frederic-Charles Barthelery,
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
package co.beem.project.beem.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import co.beem.project.beem.service.LoginAsyncTask;
import co.beem.project.beem.service.aidl.IXmppFacade;
import de.duenndns.ssl.MemorizingTrustManager;
import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.ui.android.FbTextMainActivity;

/**
 * This class is an activity which display an animation during the connection with the server.
 * @author Da Risk <darisk972@gmail.com>
 */
public class LoginAnim extends Activity {

    private static final String TAG = "LoginAnim";
    private static final Intent SERVICE_INTENT = new Intent();
    private static final int RECEIVER_PRIORITY = 50;
    static {
	SERVICE_INTENT.setComponent(new ComponentName("hoahong.facebook.messenger", "co.beem.project.beem.FbTextService"));
    }
    
    
    private final ServiceConnection mServConn = new LoginServiceConnection();
    private IXmppFacade mXmppFacade;
    private AsyncTask<IXmppFacade, Integer, Boolean> mTask;
    private TextView mLoginState;
    private boolean mBinded;
    private BroadcastReceiver mSslReceiver;

    /**
     * Constructor.
     */
    public LoginAnim() {
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.login_anim);
	mLoginState = (TextView) findViewById(R.id.loginanim_status_text);
	FbTextMainActivity.loadingTime = 0;
	try{
		Thread.sleep(10);
	}catch (Exception e){
		e.printStackTrace();
	}
	FbTextApplication mFacebookTextApplication = (FbTextApplication) getApplication();
	if (mFacebookTextApplication.isConnected() || mFacebookTextApplication.mIsConnecting) {
		startActivity(new Intent(this, FbTextMainActivity.class));
		finish();

	}
	mSslReceiver = new BroadcastReceiver() {
	    public void onReceive(Context ctx, Intent i) {
		try {
			if (FbTextApplication.isDebug)
				Log.i(TAG, "Interception the SSL notification");
		    PendingIntent pi = i.getParcelableExtra(MemorizingTrustManager.INTERCEPT_DECISION_INTENT_LAUNCH);
		    pi.send();
		    abortBroadcast();
		} catch (PendingIntent.CanceledException e) {
			if (FbTextApplication.isDebug)
				Log.e(TAG, "Error while displaying the SSL dialog", e);
		}
	    }
	};
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
	super.onStart();
	//mLogo.startAnimation(mRotateAnim);
	if (mTask == null)
	    mTask = new LoginTask();
	if (!mBinded){
		/*if (!FbTextService.isRunning){
			Log.v(TAG, "cal startService here");
			startService(new Intent(LoginAnim.this, FbTextService.class));
		}*/
		Log.v("LoginAni", " on Start and bind Service here");
	    mBinded = bindService(LoginAnim.SERVICE_INTENT, mServConn, BIND_AUTO_CREATE);
	}
	IntentFilter filter = new IntentFilter(MemorizingTrustManager.INTERCEPT_DECISION_INTENT
		+ "/" + getPackageName());
	filter.setPriority(RECEIVER_PRIORITY);
	registerReceiver(mSslReceiver, filter);

	if (FbTextApplication.isDebug)
		Log.v("LoginAnim", "status code: " + PreferenceManager.getDefaultSharedPreferences
			(LoginAnim.this).getInt(FbTextApplication.STATUS_KEY, 0));
	
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onStop() {
	super.onStop();
	if (mBinded && mTask.getStatus() != AsyncTask.Status.RUNNING) {
	    unbindService(mServConn);
	    mXmppFacade = null;
	    mBinded = false;
	}
	unregisterReceiver(mSslReceiver);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	// TODO use onBackPressed on Eclair (2.0)
	if (keyCode == KeyEvent.KEYCODE_BACK && mTask.getStatus() != AsyncTask.Status.FINISHED) {
	    if (!mTask.cancel(true)) {
		Log.d(TAG, "Can't interrupt the connection");
	    }
	    setResult(Activity.RESULT_CANCELED);
	}
	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onBackPressed() {
    	if (mTask.getStatus() != AsyncTask.Status.FINISHED){
    		if (!mTask.cancel(true)) {
    			Log.d(TAG, "Can't interrupt the connection");
    		    }
    		    setResult(Activity.RESULT_CANCELED);
    	}
    	super.onBackPressed(); 
    }


    /**
     * Asynchronous class for connection.
     */
    private class LoginTask extends LoginAsyncTask {

	/**
	 * Constructor.
	 */
	LoginTask() {
		super.setContext(LoginAnim.this);
	}
	
	@Override
		protected void onPreExecute() {
			super.onPreExecute();
			FbTextApplication.mIsConnecting = true;
		}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		FbTextApplication.mIsConnecting = false;
	    if (result == null || !result) { // Task cancelled or exception
		if (!result) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginAnim.this).edit();
			//editor.putBoolean(FbTextApplication.GOOD_CONFIGURED, false);
			editor.apply();
		    Intent i = new Intent();
		    i.putExtra("message", getErrorMessage());
		    LoginAnim.this.setResult(Activity.RESULT_CANCELED, i);
		} else
		    LoginAnim.this.setResult(Activity.RESULT_CANCELED);
		LoginAnim.this.finish();
	    } else {
		LoginAnim.this.startService(LoginAnim.SERVICE_INTENT);
		Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginAnim.this).edit();
		editor.putBoolean(FbTextApplication.GOOD_CONFIGURED, true);
		editor.apply();
		LoginAnim.this.setResult(Activity.RESULT_OK);
		LoginAnim.this.finish();
	    }
	}

	@Override
	protected void onProgressUpdate(Integer ... values) {
	    mLoginState.setText(getResources().getStringArray(R.array.loganim_state)[values[0]]);
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onCancelled()
	 */
	@Override
	protected void onCancelled() {
	    super.onCancelled();
	    LoginAnim.this.stopService(LoginAnim.SERVICE_INTENT);
	}

    }

    /**
     * The service connection used to connect to the Beem service.
     */
    private class LoginServiceConnection implements ServiceConnection {

	/**
	 * Constructor.
	 */
	public LoginServiceConnection() {
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		if (FbTextApplication.isDebug)
			Log.v("Login", " Login ani connected service and execute asynTask here");
	    mXmppFacade = IXmppFacade.Stub.asInterface(service);
	    if (mTask.getStatus() == AsyncTask.Status.PENDING)
		mTask = mTask.execute(mXmppFacade);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	    mXmppFacade = null;
	}
    }
}
