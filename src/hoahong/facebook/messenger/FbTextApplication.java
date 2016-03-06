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

package hoahong.facebook.messenger;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import hoahong.facebook.messenger.database.DatabaseHelper;
import hoahong.facebook.messenger.ui.Utils;

public class FbTextApplication extends Application {

    /* Constants for PREFERENCE_KEY
     * The format of the Preference key is :
     * $name_KEY = "$name"
     */
    /** Preference key for account username. */
    public static final String ACCOUNT_USERNAME_KEY = "account_username";
    /** Preference key for account password. */
    public static final String ACCOUNT_PASSWORD_KEY = "account_password";
    /** Preference key set to true if using an Android account . */
    public static final String USE_SYSTEM_ACCOUNT_KEY = "use_system_account";

    /** Preference key for Android account type . */
    public static final String ACCOUNT_SYSTEM_TYPE_KEY = "account_system_type";

    /** Preference key set to true if using specific server details. */
    public static final String ACCOUNT_SPECIFIC_SERVER_KEY = "account_specific_server";

    /** Preference key for specific server hostname. */
    public static final String ACCOUNT_SPECIFIC_SERVER_HOST_KEY = "account_specific_server_host";

    /** Preference key for specific server port. */
    public static final String ACCOUNT_SPECIFIC_SERVER_PORT_KEY = "account_specific_server_port";

    /** Preference key for status (available, busy, away, ...). */
    public static final String STATUS_KEY = "status";
    /** Preference key for status message. */
    public static final String STATUS_TEXT_KEY = "status_text";
    /** Preference key for connection resource . */
    public static final String CONNECTION_RESOURCE_KEY = "connection_resource";
    /** Preference key for connection priority. */
    public static final String CONNECTION_PRIORITY_KEY = "connection_priority";
    /** Preference key for the use of a proxy. */
    public static final String PROXY_USE_KEY = "proxy_use";
    /** Preference key for the type of proxy. */
    public static final String PROXY_TYPE_KEY = "proxy_type";
    /** Preference key for the proxy server. */
    public static final String PROXY_SERVER_KEY = "proxy_server";
    /** Preference key for the proxy port. */
    public static final String PROXY_PORT_KEY = "proxy_port";
    /** Preference key for the proxy username. */
    public static final String PROXY_USERNAME_KEY = "proxy_username";
    /** Preference key for the proxy password. */
    public static final String PROXY_PASSWORD_KEY = "proxy_password";
    /** Preference key for vibrate on notification. */
    public static final String NOTIFICATION_VIBRATE_KEY = "notification_vibrate_fbtext";
    /** Preference key for notification sound. */
    public static final String NOTIFICATION_SOUND_KEY = "notification_sound";
    /** Preference key for smack debugging. */
    public static final String SMACK_DEBUG_KEY = "smack_debug_fbtext";
    /** Preference key for full Jid for login. */
    public static final String FULL_JID_LOGIN_KEY = "full_jid_login";
    /** Preference key for display offline contact. */
    public static final String SHOW_OFFLINE_CONTACTS_KEY = "show_offline_contacts";
    /** Preference key for hide the groups. */
    public static final String HIDE_GROUPS_KEY = "hide_groups";
    /** Preference key for auto away enable. */
    public static final String USE_AUTO_AWAY_KEY = "use_auto_away";
    /** Preference key for auto away message. */
    public static final String AUTO_AWAY_MSG_KEY = "auto_away_msg";
    /** Preference key for compact chat ui. */
    public static final String USE_COMPACT_CHAT_UI_KEY = "use_compact_chat_ui";
    /** Preference key for history path on the SDCard. */
    public static final String CHAT_HISTORY_KEY = "settings_chat_history_path";
    /** Preference key to show the jid in the contact list. */
    public static final String SHOW_JID = "show_jid";
    
    public static final String GET_AVATAR = "get_avatar";
    public static final String SEND_IMAGE_MESSAGE = "send_image_message";
    public static final String SEND_INVITATION = "send_inviation_link";
    public static final String SEND_INVITATION_SUCCESS = "send_invitation_sucess";
    public static final String SEND_INVITATION_FAILED = "send_invitation_failed";
    public static final String CUSTOME_INVITATION_MESSAGE_KEY = "invitation_custome_message_key";
    public static final String INVITATION_FOR_ADS_KEY = "invitation_for_ads";
    public static final String IMAGE_URL_FB = "facebook.image.url";
    public static final String SEND_IMAGE_RESULT = "send_image_result";
    public static final String UPDATE_USER_STATE = "update_user_state";
    public static final String PUSH_NOTIFICATION_FAVORITE_ONLINE = "favorite_online";
    public static final String CHANGE_STATUS = "change_status";
    public static final String STATUS_INT_KEY = "status_int_key";
    public static final String LOAD_AVATAR_KEY = "load_avatar";
    public static final String CURRENT_CHAT_ID = "current_chat_id";
    public static final String GOOD_CONFIGURED = "good_configured";
    public static final String QUIT_RIGHT_AWAY = "quit_right_away";
    public static final String INTENT_NO_RESTART = "intent_no_restart";
    public static final String PLAY_LINK = "https://play.google.com/store/apps/details?id=hoahong.facebook.messenger";
    public static boolean isDebug = true;
    public static final String USER_ALREADY_RATE_KEY = "user_already_rate";
    public static final String CLICK_CHAT_ACTIVITY_COUNT_KEY = "click_count_key";
    public static final String VIEW_CHAT_TUTORIAL_KEY = "view_chat_tutorial";
    public static final String UPGRADE_REMOVE_ADS_SKU_ID = "upgrade_remove_ads";
    public static final String base64EncodedPublicKey 
    	= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtKH4t2XyA/xHYaqb0IldZ99lJ/V7ewZhnM/oGJTw+c1BN09ig0xjpii2zhDPqZiG5D1mRio5Nzfez8a48ATMpTT8vCLrB9bZmr6nc6pvyl3OsdhXfe5g/d9HPNukyCGYHCrY9fPrxFrhQoXnVMGQ/Tr4EREnMe90kq51E0WTR5rMyaC1Bt5PXvoVw6rPRY7hO8nPJW4pGo/Glshn+esXjzSdeFh56BDfFQygRhgj9DInkJbzzkkr4Lp1I6135ddtP3LhtCm62s6cX/bH7ggJWPDgYT0tILdmEaZZ6qls9SR68JfPsxkgCJyJmFC7aYBlrldAwxZy8VuAl5PFitFZCQIDAQAB";
    public static final String UPGRAGE_REMOVE_ADS_KEY = "hoahong.android.facebook.messenger.upgrade";// shouldn't change this
    public static final String CHANGE_RINGTONE_PREF_KEY = "hoahong.android.facebook.messenger.change.ringtone";
    //public static final String CURRENT_RINGTONE_URI_KEY = "current.ringtone";
    public static final String LEAVE_NOTIFICATION = "fasdfasdf_dfasdfasd";
    public static final String DEVELOPER_EMAIL = "hong.coltech@gmail.com";
    public static final String LOGIN_USING_WEB_KEY = "login_webview";
    //TODO add the other one

    public static boolean isDisconnected;
    public static boolean isAdsRemovable = false;
    public static boolean isRatable = true;
    private boolean mIsConnected;
    public static boolean mIsConnecting = false;
    private boolean mIsAccountConfigured;
    private boolean mIsGoodConfigured;
    private boolean mPepEnabled;
    private SharedPreferences mSettings;
    private boolean isRemoveAds;
    public boolean isUsingWebview;
    public static boolean isNotTheLoginTime = true;
    private final PreferenceListener mPreferenceListener = new PreferenceListener();
    public static Context context;
    public static DatabaseHelper databaseHelper;
    
    public static Intent sharedIntent;
    /**
     * Constructor.
     */
    public FbTextApplication() {
    }

    @Override
    public void onCreate() {
	super.onCreate();
	mSettings = PreferenceManager.getDefaultSharedPreferences(this);
	String username = mSettings.getString(FbTextApplication.ACCOUNT_USERNAME_KEY, "");
	String password = mSettings.getString(FbTextApplication.ACCOUNT_PASSWORD_KEY, "");
	mIsAccountConfigured = !TextUtils.isEmpty(username) && (!TextUtils.isEmpty((password)));
	mIsGoodConfigured = (mIsAccountConfigured && mSettings.getBoolean(GOOD_CONFIGURED, false));
	isRemoveAds = mSettings.getBoolean(UPGRAGE_REMOVE_ADS_KEY, false);
	isUsingWebview = mSettings.getBoolean(LOGIN_USING_WEB_KEY, false);
	mSettings.registerOnSharedPreferenceChangeListener(mPreferenceListener);
	context = getApplicationContext();
	Utils.applicationContext = FbTextApplication.this;
	Utils.checkDisplaySize();
	//SmileyParser.init(this);
    }

    @Override
    public void onTerminate() {
	super.onTerminate();
	mSettings.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    /**
     * Tell if Beem is connected to a XMPP server.
     * @return false if not connected.
     */
    public boolean isConnected() {
	return mIsConnected;
    }

    /**
     * Set the status of the connection to a XMPP server of BEEM.
     * @param isConnected set for the state of the connection.
     */
    public void setConnected(boolean isConnected) {
	mIsConnected = isConnected;
    }

    /*
	 * get database helper
	 */
	public DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(
					FbTextApplication.this, DatabaseHelper.class);
			// delete old message
			try {
				boolean checkStoreMessage = PreferenceManager
						.getDefaultSharedPreferences(FbTextApplication.this)
						.getBoolean(getString(R.string.store_old_message_key),
								true);
				if (!checkStoreMessage)
					databaseHelper.delelteOldMessage();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return databaseHelper;
	}
    
	
	
    /**
     * Tell if a XMPP account is configured.
     * @return false if there is no account configured.
     */
    public boolean isAccountConfigured() {
	return mIsAccountConfigured;
    }

    public boolean isAccountGoodConfigured() {
    	return mIsGoodConfigured;
    }
    /**
     * Enable Pep in the application context.
     *
     * @param enabled true to enable pep
     */
    public void setPepEnabled(boolean enabled) {
	mPepEnabled = enabled;
    }

    /**
     * Check if Pep is enabled.
     *
     * @return true if enabled
     */
    public boolean isPepEnabled() {
	return mPepEnabled;
    }


	public boolean isUpgraged() {
		return mSettings.getBoolean(UPGRAGE_REMOVE_ADS_KEY, false);
	}

	public void setSupportDevelopers(boolean isSupportDevelopers) {
		this.isRemoveAds = isSupportDevelopers;
		mSettings.edit().putBoolean(UPGRAGE_REMOVE_ADS_KEY, isSupportDevelopers).apply();
	}

	/**
     * A listener for all the change in the preference file. It is used to maintain the global state of the application.
     */
    private class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

	/**
	 * Constructor.
	 */
	public PreferenceListener() {
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences  sharedPreferences, String key) {
	    if (FbTextApplication.ACCOUNT_USERNAME_KEY.equals(key) || FbTextApplication.ACCOUNT_PASSWORD_KEY.equals(key) || FbTextApplication.USE_SYSTEM_ACCOUNT_KEY.equals(key)) {
		String login = mSettings.getString(FbTextApplication.ACCOUNT_USERNAME_KEY, "");
		String password = mSettings.getString(FbTextApplication.ACCOUNT_PASSWORD_KEY, "");
		boolean useSystemAccount = mSettings.getBoolean(FbTextApplication.USE_SYSTEM_ACCOUNT_KEY, false);
		mIsAccountConfigured = !TextUtils.isEmpty(login) && (useSystemAccount || !TextUtils.isEmpty((password)));
	    }
	    if (FbTextApplication.UPGRAGE_REMOVE_ADS_KEY.equals(key)){
	    	isRemoveAds = mSettings.getBoolean(UPGRAGE_REMOVE_ADS_KEY, false);
	    }
	}
    }
}
