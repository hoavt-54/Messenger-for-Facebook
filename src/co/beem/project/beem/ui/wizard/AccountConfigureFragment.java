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
package co.beem.project.beem.ui.wizard;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.proxy.ProxyInfo.ProxyType;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import co.beem.project.beem.ui.Login;
import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.ui.Utils;

/**
 * Fragment to enter the information required in order to configure a XMPP account.
 *
 */
public class AccountConfigureFragment extends Fragment implements OnClickListener {

    private static final String TAG = AccountConfigureFragment.class.getSimpleName();

    private static final int MANUAL_CONFIGURATION_CODE = 2;
    public static final String FB_WEB_URL = "https://m.facebook.com/messages?stype=lo&jlou=AfexQ4RxdQ_I6LYc3eBv3ihkaXmUQL0OrnL3pnR3pZZrUzzF9Y7200t5k28z25KRTxqxFuhO0aE07CZjcAKn7Jf8NjZVBkykG73KsgLXcO_bsw&lh=Ac96bq4XrM4uvW6R&refid=7&_rdr";
    private TextView mNextButton;
    private TextView mErrorLabel;
    private TextView mSettingsWarningLabel;
    private View errorDivider;
    private EditText mAccountJID;
    private boolean isAccountEdtFocused;
    private EditText mAccountPassword;
    private TextView showBtn;
    private boolean isPasswordShowing;
    private boolean isPasswordEdtFocused;
    private final JidTextWatcher mJidTextWatcher = new JidTextWatcher();
    private final PasswordTextWatcher mPasswordTextWatcher = new PasswordTextWatcher();
    private boolean mValidJid;
    private boolean mValidPassword;
    private SharedPreferences settings;
    private boolean useSystemAccount;
    
    private TextView createNewAccountButton;
    private TextView forgotPasswordTv;
    private TextView helpTextview;
    //private AppCompatCheckBox statusCheckBox;
     
    private int errorMEssageId;

    private co.beem.project.beem.ui.wizard.AccountConfigureFragment.ConnectionTestTask task;

    /**
     * Create a new AccountConfigureFragment.
     *
     * @return a new AccountConfigureFragment
     */
    public static AccountConfigureFragment newInstance() {
    	return new AccountConfigureFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate");
    	setRetainInstance(true);

    	settings =  PreferenceManager.getDefaultSharedPreferences(getActivity());
    	hideKeyboard();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	hideKeyboard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.wizard_account_configure, container, false);

    	mNextButton = (TextView) v.findViewById(R.id.next);
    	mNextButton.setEnabled(false);
    	mNextButton.setOnClickListener(this);
    	mErrorLabel = (TextView) v.findViewById(R.id.error_label);
    	mSettingsWarningLabel = (TextView) v.findViewById(R.id.settings_warn_label);
    	errorDivider = v.findViewById(R.id.error_divider);
    	mAccountJID = (EditText) v.findViewById(R.id.account_username);
    	mAccountJID.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				isAccountEdtFocused = hasFocus;
			}
		});
    	mAccountPassword = (EditText) v.findViewById(R.id.account_password);
    	mAccountPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				isPasswordEdtFocused = hasFocus;
			}
		});
    	//mAccountJID.setFilters(newFilters);
    	mAccountJID.addTextChangedListener(mJidTextWatcher);
    	mAccountPassword.addTextChangedListener(mPasswordTextWatcher);
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) // true to disable the feature until ready
    		v.findViewById(R.id.account_layout).setVisibility(View.GONE);
    	mAccountPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
    	mAccountPassword.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			    if (event != null) {
			        if (!event.isShiftPressed()) {
			            onClick(mNextButton);
			            return true;
			        }
			        return false;
			    }
			    onClick(mNextButton);
			    return true;
			}
		});
    	showBtn = (TextView) v.findViewById(R.id.login_show_btn);
    	showBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isPasswordShowing) {
					showBtn.setText(getText(R.string.hide));
					mAccountPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				}else {
					showBtn.setText(getText(R.string.show));
					mAccountPassword.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
				mAccountPassword.setSelection(mAccountPassword.length());
				isPasswordShowing = !isPasswordShowing;
			}
		});
    	createNewAccountButton = (TextView) v.findViewById(R.id.create_new_account);
    	createNewAccountButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((Account)getActivity()).pushFragment(WebViewFragment.instanciate("https://m.facebook.com/r.php?loc=bottom&refid=8"));
			}
		});
    	forgotPasswordTv = (TextView) v.findViewById(R.id.forgot_password_tv);
    	forgotPasswordTv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((Account)getActivity()).pushFragment(WebViewFragment.instanciate("https://m.facebook.com/login/identify/?ctx=recover"));
				
			}
		});
    	helpTextview = (TextView) v.findViewById(R.id.help_tv);
    	helpTextview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((Account)getActivity()).pushFragment(WebViewFragment.instanciate("https://m.facebook.com/help/?refid=8"));
			}
		});
    	return v;
    }

    @Override
    public void onStart() {
		super.onStart();
		// temporaly disable jid watcher
		mAccountJID.removeTextChangedListener(mJidTextWatcher);
		String oldAccount = settings.getString(FbTextApplication.ACCOUNT_USERNAME_KEY, "");
		if (oldAccount != null && oldAccount.length() > "@chat.facebook.com".length())
			oldAccount = oldAccount.substring(0, oldAccount.length() - "@chat.facebook.com".length());
		mAccountJID.setText(oldAccount);
		if (oldAccount.length() > 3)
			mValidJid = true;
		
		mAccountJID.addTextChangedListener(mJidTextWatcher);
		if (settings.getBoolean(FbTextApplication.ACCOUNT_SPECIFIC_SERVER_KEY, false)
		    || settings.getBoolean(FbTextApplication.PROXY_USE_KEY, false)) {
		    mSettingsWarningLabel.setVisibility(View.VISIBLE);
		    errorDivider.setVisibility(View.VISIBLE);
		} else{
		    mSettingsWarningLabel.setVisibility(View.GONE);
		    errorDivider.setVisibility(View.GONE);
		}
		
		Intent intent = getActivity().getIntent();
		String message = intent.getStringExtra("message");
		if (intent != null && message != null){
			mErrorLabel.setVisibility(View.VISIBLE);
			errorDivider.setVisibility(View.VISIBLE);
			//Connection failed. No response from server.
			if (message.equalsIgnoreCase("Connection failed. No response from server."))
				mErrorLabel.setText(R.string.connect_server_error);
			else{
				if (message.contains("to resolve host"))
					mErrorLabel.setText(R.string.connect_server_error);
				else
					mErrorLabel.setText(message + " ");
			}
			if (Utils.isConnectToInternet(getActivity())) {
					settings.edit().putBoolean(FbTextApplication.LOGIN_USING_WEB_KEY, true).commit();
					((Account)getActivity()).pushFragment(WebViewFragment.instanciate(FB_WEB_URL));
			}
		}
    }


    @Override
	public void onClick(View v) {
    	FbTextApplication.isNotTheLoginTime = false;
		if (v == mNextButton) {
			
			if (!Utils.isConnectToInternet(getActivity())) {
				((Account)getActivity()).pushFragment(WebViewFragment.instanciate("https://m.facebook.com/loc=bottom&refid=8"));
				return;
			}
			
			SharedPreferences.Editor edit = settings.edit();
			/*edit.putBoolean(FbTextApplication.LOAD_AVATAR_KEY, avatarCheckBox.isChecked());*/
			edit.putInt(FbTextApplication.STATUS_KEY, 0);
			edit.apply();;
			
			
			
			String mEmail = mAccountJID.getText().toString();
			mEmail = StringUtils.parseBareAddress(mEmail);
			String mPassword = mAccountPassword.getText().toString();
			//Log.v(TAG, "jid: " + mEmail + "\t password : " + mPassword);
			task = new ConnectionTestTask();
			task.execute(mEmail + "@chat.facebook.com", mPassword);
			hideKeyboard();
		}
	}

 

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	 if (requestCode == MANUAL_CONFIGURATION_CODE) {
	    String login = settings.getString(FbTextApplication.ACCOUNT_USERNAME_KEY, "");
	    String password = settings.getString(FbTextApplication.ACCOUNT_PASSWORD_KEY, "");
	    mAccountJID.setText(login);
	    mAccountPassword.setText(password);
	} else {
	    super.onActivityResult(requestCode, resultCode, data);
	}
    }

    /**
     * Callback called when the account was connected successfully.
     *
     * @param jid the jid used to connect
     * @param password the password used to connect
     *
     */
	private void onAccountConnectionSuccess(String jid, String password) {
		Activity a = getActivity();
		saveCredential(jid, password);
		// launch login
		Intent i = new Intent(a, Login.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		a.finish();
	}

    /**
     * Callback called when the account connection failed.
     *
     */
	private void onAccountConnectionFailed() {
		mAccountPassword.setText("");
		mErrorLabel.setVisibility(View.VISIBLE);
		errorDivider.setVisibility(View.VISIBLE);
		if (errorMEssageId != 0)
		mErrorLabel.setText(errorMEssageId);
		if (Utils.isConnectToInternet(getActivity())) {
			settings.edit().putBoolean(FbTextApplication.LOGIN_USING_WEB_KEY, true).commit();
			((Account)getActivity()).pushFragment(WebViewFragment.instanciate(FB_WEB_URL));
	}
	}

    /**
     * Save the user credentials.
     *
     * @param jid the jid of the user
     * @param pass the password of the user
     *
     */
    private void saveCredential(String jid, String pass) {
	SharedPreferences.Editor edit = settings.edit();
	edit.putString(FbTextApplication.ACCOUNT_USERNAME_KEY, jid);
	edit.putString(FbTextApplication.ACCOUNT_PASSWORD_KEY, pass);
	edit.putBoolean(FbTextApplication.USE_SYSTEM_ACCOUNT_KEY, false);
	edit.commit();
    }

    public void hideKeyboard(){
        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }
    

    /**
     * Check that the username is really a JID.
     * @param username the username to check.
     */
    private void checkUsername(String username) {
	if (username == null || username.length() < 4) {
	    mValidJid = false;
	} else {
	    mValidJid = true;
	} 
    }

    /**
     * Check password.
     * @param password the password to check.
     */
    private void checkPassword(String password) {
	if (password.length() > 3)
	    mValidPassword = true;
	else
	    mValidPassword = false;
    }

    /**
     * Text watcher to test the existence of a password.
     */
    private class PasswordTextWatcher implements TextWatcher {

	/**
	 * Constructor.
	 */
	public PasswordTextWatcher() {
	}

	@Override
	public void afterTextChanged(Editable s) {
	    checkPassword(s.toString());
	    mNextButton.setEnabled(mValidJid && mValidPassword);
	}

	@Override
	public void beforeTextChanged(CharSequence  s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence  s, int start, int before, int count) {
	}
    }

    /**
     * TextWatcher to check the validity of a JID.
     */
    private class JidTextWatcher implements TextWatcher {

	/**
	 * Constructor.
	 */
	public JidTextWatcher() {
	}

	@Override
	public void afterTextChanged(Editable s) {
	    checkUsername(s.toString());
	    mNextButton.setEnabled(mValidJid && mValidPassword);
	    if (useSystemAccount) {
		mAccountPassword.setEnabled(true);
	    	mAccountPassword.setText("");
	    }
	    useSystemAccount = false;
	}

	@Override
	public void beforeTextChanged(CharSequence  s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence  s, int start, int before, int count) {
	}
    }

    /**
     * AsyncTask use to test the credentials.
     */
    class ConnectionTestTask extends AsyncTask<String, Void, Boolean> {

    private AlertDialog dialog;
	private ConnectionConfiguration config;
	private Exception exception;
	private String jid;
	private String password;
	private String server;

	@Override
	protected void onPreExecute() {
		if (FbTextApplication.isDebug)
		Log.v(TAG, "onPreExecute");
	    mErrorLabel.setVisibility(View.GONE);
	    errorDivider.setVisibility(View.GONE);
	    AlertDialog.Builder builder = new Builder(getActivity());
	    builder
	    		.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_adding_user_to_group, null));
    	dialog = builder.create();
    	dialog.setCancelable(false);
    	dialog.show();
	    
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (FbTextApplication.isDebug)
		Log.v(TAG, "onPostExecute");
		try {
			if (dialog.isShowing())
		    	dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
	    if (result) {
		onAccountConnectionSuccess(jid, password);
	    } else {
		onAccountConnectionFailed();
	    }
	   
	}

	@Override
		protected Boolean doInBackground(String... params) {
		if (FbTextApplication.isDebug)
			Log.d(TAG, "Xmpp login task");
			jid = params[0];
			password = params[1];

			int port = -1;
			if (params.length > 2) {
				server = params[2];
			}
			if (params.length > 3) {
				if (!TextUtils.isEmpty(params[3])) {
					port = Integer.parseInt(params[3]);
				}
			}
			if (FbTextApplication.isDebug)
				Log.d(TAG, "jid " + jid + " server  " + server + " port " + port);
			String login = StringUtils.parseName(jid);
			String serviceName = StringUtils.parseServer(jid);
			if (FbTextApplication.isDebug)
				Log.v(TAG, "very long 1");
			Connection connection = prepareConnection(jid, server, port);
			if (settings.getBoolean(FbTextApplication.FULL_JID_LOGIN_KEY,
					false)
					|| "gmail.com".equals(serviceName)
					|| "googlemail.com".equals(serviceName)) {
				login = jid;
			}
			try {
				try{
					if (FbTextApplication.isDebug)
						Log.v(TAG, "very long 2 connect");
				 connection.connect();
				}catch (Exception e) {
					e.printStackTrace();
					errorMEssageId = R.string.cannot_connection;
					exception = e;
					return false;
				}
				if (FbTextApplication.isDebug)
					Log.v(TAG, "very long 3 login");
				 connection.login(login, password);
			} catch (Exception e) {
				if (FbTextApplication.isDebug)
					Log.e(TAG, "Unable to connect to Xmpp server", e);
				exception = e;
				return false;
			} finally {
				connection.disconnect();
			}
			return true;
		}

	/**
	 * Initialize the XMPP connection.
	 *
	 * @param jid the jid to use
	 * @param server the server to use (not using dns srv)  may be null
	 * @param port the port
	 *
	 * @return the XMPPConnection prepared to connect
	 */
		private Connection prepareConnection(String jid, String server, int port) {
			boolean useProxy = settings.getBoolean(
					FbTextApplication.PROXY_USE_KEY, false);
			ProxyInfo proxyinfo = ProxyInfo.forNoProxy();
			if (useProxy) {
				String stype = settings.getString(
						FbTextApplication.PROXY_TYPE_KEY, "HTTP");
				String phost = settings.getString(
						FbTextApplication.PROXY_SERVER_KEY, "");
				String puser = settings.getString(
						FbTextApplication.PROXY_USERNAME_KEY, "");
				String ppass = settings.getString(
						FbTextApplication.PROXY_PASSWORD_KEY, "");
				int pport = Integer.parseInt(settings.getString(
						FbTextApplication.PROXY_PORT_KEY, "1080"));
				ProxyInfo.ProxyType type = ProxyType.valueOf(stype);
				proxyinfo = new ProxyInfo(type, phost, pport, puser, ppass);
			}
			String serviceName = StringUtils.parseServer(jid);
			if (port != -1 || !TextUtils.isEmpty(server)) {
				if (port == -1)
					port = 5222;
				if (TextUtils.isEmpty(server))
					server = serviceName;
				config = new ConnectionConfiguration(server, port, serviceName,
						proxyinfo);
			} else {
				config = new ConnectionConfiguration(serviceName, proxyinfo);
			}
			if (settings.getBoolean(FbTextApplication.SMACK_DEBUG_KEY, false))
				config.setDebuggerEnabled(true);
			config.setSendPresence(false);
			config.setRosterLoadedAtLogin(false);
			return new XMPPConnection(config);
		}
	
	
	
	
	
	
	
    }
//    setFinishOnTouchOutside(false)
    
    public boolean exitOnBackPress() {
    	
    	if (isAccountEdtFocused || isPasswordEdtFocused) return false;    	
    	return true;
    }
}
