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
package co.beem.project.beem.ui;

import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.ui.Utils;
import hoahong.facebook.messenger.ui.android.FbTextMainActivity;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import co.beem.project.beem.ui.wizard.Account;
import co.beem.project.beem.utils.BeemConnectivity;

/**
 * This class is the main Activity for the Beem project.
 * 
 * @author Da Risk <darisk@beem-project.com>
 */
public class Login extends Activity {

	private static final int LOGIN_REQUEST_CODE = 1;
	private TextView mTextView;
	private boolean mIsResult;
	private FbTextApplication mFacebookTextApplication;

	/**
	 * Constructor.
	 */
	public Login() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		mTextView = (TextView) findViewById(R.id.log_as_msg);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		Application app = getApplication();
		Utils.setDensity(Login.this.getApplicationContext()
				.getResources().getDisplayMetrics().density);
		try{
			Thread.sleep(10);
		}catch (Exception e){
			e.printStackTrace();
		}
		
		// Get intent, action and MIME type
	    Intent intent = getIntent();
	    if (intent != null){
		    String action = intent.getAction();
		    String type = intent.getType();
		    if ((Intent.ACTION_SEND.equals(action) || 
		    		Intent.ACTION_SEND_MULTIPLE.equals(action)) && type != null) {
		    	FbTextApplication.sharedIntent = intent;
		    }
	    }
		
		
		if (app instanceof FbTextApplication) {
			mFacebookTextApplication = (FbTextApplication) app;
			if (mFacebookTextApplication.isConnected() || mFacebookTextApplication.mIsConnecting) {
				startActivity(new Intent(this, FbTextMainActivity.class));
				finish();

			}
			else if (!mFacebookTextApplication.isAccountConfigured()) {
				if (FbTextApplication.isDebug)
					Log.v("Login", " and app is not connected -> call Account");
				startActivity(new Intent(this, Account.class));
				finish();
			}
			else if (mFacebookTextApplication.isUsingWebview) {
				startActivity(new Intent(this, Account.class));
				finish();
			}
		}
		setContentView(R.layout.login);
		mTextView = (TextView) findViewById(R.id.log_as_msg);
	}

	@Override
	protected void onStart() {
		super.onStart();

		/*
		 * if application is logined and network connect, we start Login
		 * animation to reconnect else waiting for user's input
		 */
		if (mFacebookTextApplication.isAccountConfigured() && !mIsResult
				&& BeemConnectivity.isConnected(getApplicationContext())) {
			if (FbTextApplication.isDebug)
				Log.v("Login",
					" and app is configured and connectivity is okay -> call Login Anim");
			mTextView.setText("");
			Intent i = new Intent(this, LoginAnim.class);
			startActivityForResult(i, LOGIN_REQUEST_CODE);
			mIsResult = false;
		} else {
			mTextView.setText("");
			Intent i = new Intent(this, LoginAnim.class);
			startActivityForResult(i, LOGIN_REQUEST_CODE);
		}
	}
	 @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * Handle result returned by Login Animation
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST_CODE) {
			mIsResult = true;
			if (resultCode == Activity.RESULT_OK) {
				Log.v("Login",
						" after LoginAni and result okay - > call contact list");
				startActivity(new Intent(this, FbTextMainActivity.class));
				finish();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				if (FbTextApplication.isDebug)
					Log.v("Login",
						" after LoginAni and result not okay - > display error messsage");
				if (Utils.isConnectToInternet(getApplicationContext())) {
					// this error because cannot login, xmpp reject -> webview
				}
				Intent failedIntent = new Intent(this, Account.class);
				if (data != null) {
					failedIntent.putExtra("message", data.getExtras()
							.getString("message"));
				}
				startActivity(failedIntent);
				finish();
			}
		}
	}

}
