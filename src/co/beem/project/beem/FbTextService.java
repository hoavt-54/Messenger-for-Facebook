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
package co.beem.project.beem;

import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.connection.FacebookTextConnectionListener;
import hoahong.facebook.messenger.data.ChatMessage;
import hoahong.facebook.messenger.data.ChatMessageType;
import hoahong.facebook.messenger.data.ChatSession;
import hoahong.facebook.messenger.data.FbPhoto;
import hoahong.facebook.messenger.data.FbPost;
import hoahong.facebook.messenger.data.ImageMessageInQueue;
import hoahong.facebook.messenger.data.User;
import hoahong.facebook.messenger.data.UserState;
import hoahong.facebook.messenger.ui.Utils;
import hoahong.facebook.messenger.ui.android.FbTextMainActivity;
import hoahong.facebook.messenger.ui.android.SessionManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.proxy.ProxyInfo.ProxyType;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.entitycaps.EntityCapsManager;
import org.jivesoftware.smackx.entitycaps.SimpleDirectoryPersistentCache;
import org.jivesoftware.smackx.entitycaps.packet.CapsExtension;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.provider.CapsExtensionProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.pubsub.provider.EventProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemsProvider;
import org.jivesoftware.smackx.pubsub.provider.PubSubProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import co.beem.project.beem.service.BeemChatManager;
import co.beem.project.beem.service.BeemMessage;
import co.beem.project.beem.service.ChatAdapter;
import co.beem.project.beem.service.Contact;
import co.beem.project.beem.service.XmppConnectionAdapter;
import co.beem.project.beem.service.XmppFacade;
import co.beem.project.beem.service.aidl.IChat;
import co.beem.project.beem.service.aidl.IXmppFacade;
import co.beem.project.beem.service.auth.PreferenceAuthenticator;
import co.beem.project.beem.smack.avatar.AvatarMetadataProvider;
import co.beem.project.beem.smack.avatar.AvatarProvider;
import co.beem.project.beem.smack.ping.PingExtension;
import co.beem.project.beem.smack.sasl.SASLGoogleOAuth2Mechanism;
import co.beem.project.beem.smack.sasl.ScramSaslMechanism;
import co.beem.project.beem.utils.BeemBroadcastReceiver;
import co.beem.project.beem.utils.BeemConnectivity;
import co.beem.project.beem.utils.Status;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Picasso.LoadedFrom;

import de.duenndns.ssl.MemorizingTrustManager;

/**
 * This class is for the Beem service. It must contains every global
 * informations needed to maintain the background service. The connection to the
 * xmpp server will be made asynchronously when the service will start.
 * 
 * @author darisk
 */
public class FbTextService extends Service implements
		FacebookTextConnectionListener {

	public static final String [] FB_TOKENS = {
			// maibao's token 19/10/15
			"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",

			
			//mai bao's tokens
			"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"
	};
	
	
	
	public static final String [] FB_IDs = { 
			//// maibao's ids 19/10/15
			"125495547784034",
			"799881623421852",
			"1618314921758229",
			"448074378687792",
			"912019705485561",
			"1446773692292875",
			"821048611275830",
			"350678428455274",
			"755459311239529",
			"981557541876158",
			"845760015502271",
			"357000477840701",
			"693162337472554",
			"388362948009298",
			"1617904361758199",
			"1444660152523909",
			"1429198120723455",
			"979799952044570",
			"995820583775929",
			"778046915618571",
			"100774583592220",
			"787179224735097",
			"557816184358955",
			"328827617326389",
			"445841905563902",
			"799495940143171",
			"680882785351524",
			"461608584015059",
			"806091826127482",
			"647633765368739",
			"479853082171495",
			"980649805292370",
			"361880147332574",
			"1588907418029083",
			"1605650606359891",
			"706665812771492",
			"1598864440365293",
			"990065127670512",
			"114003425605040",
			"1603782689884673",
			"511637488983898",
			"1610781415865613",
			"676771192466399",
			"657642144337270",
			"844151408968031",
			"688890877910059",
			"1587055911568155",
			"404632453067567",
			"640955502707812",
			"1681255602104922",
			"452338824945104",
			"1009608349063790",
			"1596675573936405",
			"1737892323105028",
			"655674891199022",
			"1613359912275169",
			"887836154619860",
			// ids from mai bao
			"890042987750687", "409382892591575",
			"521732907986469", "1489988037963843",
			"475513872630086", "931512480237028", 
			"1634250223508940", "1674583862828712", 
			"1497815923878311", "498020683700397",
			"188714568130070"
	};
	/** The id to use for status notification. */
	public static final int NOTIFICATION_STATUS_ID = 100;

	private static final String TAG = "FacebookTextService";
	private static final int DEFAULT_XMPP_PORT = 5222;
	public static final String STOP_POISON_MESSAGE = "quite_right_away";
	private NotificationManager mNotificationManager;
	private XmppConnectionAdapter mConnection;
	private SharedPreferences mSettings;
	private String mLogin;
	private String mHost;
	private String mService;
	private int mPort;
	private ConnectionConfiguration mConnectionConfiguration;
	private IXmppFacade.Stub mBind;

	private BlockingQueue<co.beem.project.beem.service.BeemMessage> savingMessageQueue;
	private BlockingQueue<User> stateChangeQueue;
	private BlockingQueue<ImageMessageInQueue> sendImageQueue;
	public static boolean isRunning;
	private SessionManager sessionManager;

	private BeemBroadcastReceiver mReceiver = new BeemBroadcastReceiver();
	private FacebookTextServiceBroadcastReceiver mOnOffReceiver = new FacebookTextServiceBroadcastReceiver();
	private FacebookTextServicePreferenceListener mPreferenceListener = new FacebookTextServicePreferenceListener();

	private boolean mOnOffReceiverIsRegistered;
	private boolean isConnected;
	private boolean isStartingForeground;
	private WeakReference<BeemChatManager> chatManagerReference;
	
	private SSLContext sslContext;
	private Handler handler;
	private SmackAndroid smackAndroid;

	/**
	 * Constructor.
	 */
	public FbTextService() {

	}
	
	public void setChatManagerReference (BeemChatManager manager) {
		chatManagerReference = new WeakReference<BeemChatManager>(manager);
	}
	
	public BeemChatManager getChatManager () {
		if (chatManagerReference != null && chatManagerReference.get()!= null)
			return chatManagerReference.get();
		return null;
	}

	/**
	 * Initialize the connection.
	 */
	private void initConnectionConfig() {
		if (FbTextApplication.isDebug)
		Log.d(TAG, "FacebookTextService innitiation ...");
		
		// TODO add an option for this ?
		// SmackConfiguration.setPacketReplyTimeout(30000);
		ProxyInfo proxyInfo = getProxyConfiguration();
		boolean useSystemAccount = mSettings.getBoolean(FbTextApplication.USE_SYSTEM_ACCOUNT_KEY, false);
		
		if (!useSystemAccount || mConnectionConfiguration == null) {
			/* StrictMode.ThreadPolicy policy = new
			 StrictMode.ThreadPolicy.Builder().permitAll().build();
			 StrictMode.setThreadPolicy(policy);*/
			SASLAuthentication
					.unsupportSASLMechanism(SASLGoogleOAuth2Mechanism.MECHANISM_NAME);
			SASLAuthentication.supportSASLMechanism("PLAIN");
			if (mSettings.getBoolean(
					FbTextApplication.ACCOUNT_SPECIFIC_SERVER_KEY, false))
				mConnectionConfiguration = new ConnectionConfiguration(mHost,
						mPort, mService, proxyInfo);
			else
				mConnectionConfiguration = new ConnectionConfiguration(
						mService, proxyInfo);
			mConnectionConfiguration
					.setCallbackHandler(new PreferenceAuthenticator(this));
		}


		if (mSettings.getBoolean("settings_key_xmpp_tls_use", false)
				|| mSettings.getBoolean("settings_key_gmail", false)) {
			mConnectionConfiguration.setSecurityMode(SecurityMode.required);
		}
		if (mSettings.getBoolean(FbTextApplication.SMACK_DEBUG_KEY, true))

			mConnectionConfiguration.setDebuggerEnabled(false);
		mConnectionConfiguration.setSendPresence(false);
		mConnectionConfiguration.setRosterLoadedAtLogin(false);
		// maybe not the universal path, but it works on most devices (Samsung
		// Galaxy, Google Nexus One)
		mConnectionConfiguration.setTruststoreType("BKS");
		mConnectionConfiguration
				.setTruststorePath("/system/etc/security/cacerts.bks");
		if (sslContext != null)
			mConnectionConfiguration.setCustomSSLContext(sslContext);
	}

	/**
	 * Get the save proxy configuration.
	 * 
	 * @return the proxy configuration
	 */
	private ProxyInfo getProxyConfiguration() {
		boolean useProxy = mSettings.getBoolean(
				FbTextApplication.PROXY_USE_KEY, false);
		if (useProxy) {
			String stype = mSettings.getString(
					FbTextApplication.PROXY_TYPE_KEY, "HTTP");
			String phost = mSettings.getString(
					FbTextApplication.PROXY_SERVER_KEY, "");
			String puser = mSettings.getString(
					FbTextApplication.PROXY_USERNAME_KEY, "");
			String ppass = mSettings.getString(
					FbTextApplication.PROXY_PASSWORD_KEY, "");
			int pport = Integer.parseInt(mSettings.getString(
					FbTextApplication.PROXY_PORT_KEY, "1080"));
			ProxyInfo.ProxyType type = ProxyType.valueOf(stype);
			return new ProxyInfo(type, phost, pport, puser, ppass);
		} else {
			return ProxyInfo.forNoProxy();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBinder onBind(Intent intent) {
		isRunning = true;
		if (FbTextApplication.isDebug)
		Log.d(TAG, "ONBIND()");
		/*
		 * if (mConnection == null || mConnection.getAdaptee() == null ||
		 * !mConnection.getAdaptee().isConnected()) { createConnectAsync(); } if
		 * (databaseHelper == null) getHelper();
		 */
		return mBind;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (FbTextApplication.isDebug)
		Log.d(TAG, "ONUNBIND()");
		if (mConnection != null && !mConnection.getAdaptee().isConnected()) {
			// this.stopSelf();
			if (FbTextApplication.isDebug)
			Log.d(TAG, "connection to server stopped");
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Utils.setContext(getApplicationContext());
		smackAndroid = SmackAndroid.init(FbTextService.this);
		StrictMode.ThreadPolicy policy =
		        new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		savingMessageQueue = new LinkedBlockingQueue<co.beem.project.beem.service.BeemMessage>();
		stateChangeQueue = new LinkedBlockingQueue<User>();
		sendImageQueue = new LinkedBlockingDeque<ImageMessageInQueue>();
		isRunning = true;
		sessionManager = new SessionManager(FbTextService.this);
		savingMessageOnBackgroundThread(new SavingNewMessageTask());
		savingMessageOnBackgroundThread(new UpdateUserStateTask());
		savingMessageOnBackgroundThread(new SendImageTask());
		handler = new Handler();
		registerReceiver(mReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CLOSED));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTED));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTING));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_DISCONNECT));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTING_In));
		registerReceiver(mOnOffReceiver, new IntentFilter(
				FbTextApplication.SEND_IMAGE_MESSAGE));
		registerReceiver(mOnOffReceiver, new IntentFilter(
				FbTextApplication.SEND_INVITATION));
		registerReceiver(mOnOffReceiver, new IntentFilter(
				FbTextApplication.UPDATE_USER_STATE));
		registerReceiver(mOnOffReceiver, new IntentFilter(
				FbTextApplication.PUSH_NOTIFICATION_FAVORITE_ONLINE));
		registerReceiver(mOnOffReceiver, new IntentFilter(
				FbTextApplication.CHANGE_STATUS));
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mSettings.registerOnSharedPreferenceChangeListener(mPreferenceListener);
		if (mSettings.getBoolean(FbTextApplication.USE_AUTO_AWAY_KEY,
				false)) {
			mOnOffReceiverIsRegistered = true;
			registerReceiver(mOnOffReceiver, new IntentFilter(
					Intent.ACTION_SCREEN_OFF));
			registerReceiver(mOnOffReceiver, new IntentFilter(
					Intent.ACTION_SCREEN_ON));
			// registerReceiver(sma, filter)
		}
		String tmpJid = mSettings.getString(
				FbTextApplication.ACCOUNT_USERNAME_KEY, "").trim();
		mLogin = StringUtils.parseName(tmpJid);
		boolean useSystemAccount = mSettings.getBoolean(
				FbTextApplication.USE_SYSTEM_ACCOUNT_KEY, false);
		mPort = DEFAULT_XMPP_PORT;
		mService = StringUtils.parseServer(tmpJid);
		mHost = mService;
		initMemorizingTrustManager();

		if (mSettings.getBoolean(
				FbTextApplication.ACCOUNT_SPECIFIC_SERVER_KEY, false)) {
			mHost = mSettings.getString(
					FbTextApplication.ACCOUNT_SPECIFIC_SERVER_HOST_KEY,
					"").trim();
			if ("".equals(mHost))
				mHost = mService;
			String tmpPort = mSettings.getString(
					FbTextApplication.ACCOUNT_SPECIFIC_SERVER_PORT_KEY,
					"5222");
			if (!"".equals(tmpPort))
				mPort = Integer.parseInt(tmpPort);
		}
		if (mSettings.getBoolean(FbTextApplication.FULL_JID_LOGIN_KEY,
				false)
				|| "gmail.com".equals(mService)
				|| "googlemail.com".equals(mService) || useSystemAccount) {
			mLogin = tmpJid;
		}

		configure(ProviderManager.getInstance());

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);
		mBind = new XmppFacade(this);
		if (FbTextApplication.isDebug)
		Log.d(TAG, "Create FacebookTextService \t id: " + mLogin + " \t host: "
				+ mHost + "\tmPort" + mPort + "\t service" + mService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		isRunning = false;
		try {
			BeemMessage message = new BeemMessage("");
			message.setFrom("quite_right_away");
			savingMessageQueue.put(message);
			sendImageQueue.put(new ImageMessageInQueue(STOP_POISON_MESSAGE));
			stateChangeQueue.add(new User("quite_right_away"));

			// TODO set all friend state to offline
		} catch (InterruptedException e) {
			if (FbTextApplication.isDebug)
			Log.v(TAG, "Could not stop the saving message queue");
			e.printStackTrace();
		}
		mNotificationManager.cancelAll();
		unregisterReceiver(mReceiver);
		mSettings
				.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
		if (mOnOffReceiverIsRegistered)
			unregisterReceiver(mOnOffReceiver);
		if (mConnection != null && mConnection.isAuthentificated()
				&& BeemConnectivity.isConnected(this))
			mConnection.disconnect();

		/*if (databaseHelper != null) {
			databaseHelper = null;
			OpenHelperManager.releaseHelper();
		}*/
		try {
			smackAndroid.exit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (FbTextApplication.isDebug)
		Log.i(TAG, "Stopping the service");
	}

	/**
	 * {@inheritDoc}
	 */
	/*
	 * @Override public void onStart(Intent intent, int startId) {
	 * super.onStart(intent, startId); Log.d(TAG,
	 * "onStart - where start connection"); createConnectAsync(); }
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*if (intent.getBooleanExtra(FacebookTextApplication.INTENT_NO_RESTART, false)){
			return START_STICKY;
		}*/
		if (FbTextApplication.isDebug)
		Log.d(TAG, "onStart - where start connection");
		if (mConnection == null || mConnection.getAdaptee() == null
				|| !mConnection.getAdaptee().isConnected()) {
			//mConnection.add
			createConnectAsync();
		}
		if (mSettings.getBoolean(FbTextApplication.LEAVE_NOTIFICATION, true))
			startForeGroundWithnotification(0);
		return START_STICKY;
	}

	/**
	 * Create the XmppConnectionAdapter. This method makes a network request so
	 * it must not be called on the main thread.
	 * 
	 * @return the connection
	 */
	public XmppConnectionAdapter createConnection() {
		if (mConnection == null) {
			initConnectionConfig();
			mConnection = new XmppConnectionAdapter(mConnectionConfiguration,
					mLogin, null, this);
			if (FbTextApplication.isDebug)
			Log.d(TAG, "created new connection");
		}
		return mConnection;
	}

	

	/**
	 * Show a notification using the preference of the user.
	 * 
	 * @param id
	 *            the id of the notification.
	 * @param notif
	 *            the notification to show
	 */
	public void sendNotification(int id, Notification notif) {
		if (mSettings.getBoolean(
				FbTextApplication.NOTIFICATION_VIBRATE_KEY, true))
			notif.defaults |= Notification.DEFAULT_VIBRATE;
		notif.ledARGB = 0xff0000ff; // Blue color
		notif.ledOnMS = 1000;
		notif.ledOffMS = 1000;
		notif.flags |= Notification.FLAG_SHOW_LIGHTS;
		String ringtoneStr = mSettings.getString(
				FbTextApplication.CHANGE_RINGTONE_PREF_KEY,
				Settings.System.DEFAULT_NOTIFICATION_URI.toString());
		notif.sound = Uri.parse(ringtoneStr);
		if (mSettings.getBoolean("notifications_new_message", true))
			mNotificationManager.notify(id, notif);
	}

	
	
	
	/*
	 * show notification to start service foreground
	 * 
	 * */
	
	public void startForeGroundWithnotification (int statusStringId){

//	    Notification note=new Notification(R.drawable.ic_stat_pending_notification,
//                getString(R.string.notification_welcome),
//                System.currentTimeMillis());
		isStartingForeground = true;
		Intent i=new Intent(this, FbTextMainActivity.class);
		
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
		Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent pi=PendingIntent.getActivity(this, 0,
		                        i, 0);
		
		if (statusStringId == 0){
			int statusState = mSettings.getInt(FbTextApplication.STATUS_KEY, 0);
			statusStringId = statusState == 0 ? R.string.notification_online : R.string.notification_invisible;
		}
		
		Notification note =  new NotificationCompat.Builder(this)
        .setContentTitle(getString (statusStringId))
        .setContentText(getString(R.string.notification_description))
        .setSmallIcon(R.drawable.ic_stat_pending_notification)
        .setContentIntent(pi).build();
		
		
		/*note.setLatestEventInfo(this, getString(statusStringId),
		    getString(R.string.notification_description),
		    pi);*/
		note.flags|=Notification.FLAG_NO_CLEAR;
		startForeground(1080, note);
	}
	/**
	 * Delete a notification.
	 * 
	 * @param id
	 *            the id of the notification
	 */
	public void deleteNotification(int id) {
		mNotificationManager.cancel(id);
	}

	/**
	 * Reset the status to online after a disconnect.
	 */
	public void resetStatus() {
		Editor edit = mSettings.edit();
		edit.putInt(FbTextApplication.STATUS_KEY, 0);
		edit.commit();
	}

	/**
	 * Initialize Jingle from an XmppConnectionAdapter.
	 * 
	 * @param adaptee
	 *            XmppConnection used for jingle.
	 */
	public void initJingle(XMPPConnection adaptee) {
	}

	/**
	 * Return a bind to an XmppFacade instance.
	 * 
	 * @return IXmppFacade a bind to an XmppFacade instance
	 */
	public IXmppFacade getBind() {
		return mBind;
	}

	/**
	 * Get the preference of the service.
	 * 
	 * @return the preference
	 */
	public SharedPreferences getServicePreference() {
		return mSettings;
	}

	/**
	 * Get the notification manager system service.
	 * 
	 * @return the notification manager service.
	 */
	public NotificationManager getNotificationManager() {
		return mNotificationManager;
	}

	/**
	 * Utility method to create and make a connection asynchronously.
	 */
	private synchronized void createConnectAsync() {
		if (mConnection == null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					createConnection();
					connectAsync();
				}
			}).start();
		} else
			connectAsync();
		if (FbTextApplication.isDebug)
		Log.v(TAG, "starting connection");
	}

	/**
	 * Utility method to connect asynchronously.
	 */
	private void connectAsync() {
		try {
			while(FbTextApplication.isNotTheLoginTime){
				if (Utils.isConnectToInternet())
					break;
				Log.w(TAG, "sleep 3 second and check internet");
				try {
					Thread.sleep(3000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mConnection.connectAsync();
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
			Log.w(TAG, "unable to connect", e);
		}
	}

	/**
	 * Get the specified Android account.
	 * 
	 * @param accountName
	 *            the account name
	 * @param accountType
	 *            the account type
	 * 
	 * @return the account or null if it does not exist
	 */
	private Account getAccount(String accountName, String accountType) {
		AccountManager am = AccountManager.get(this);
		for (Account a : am.getAccountsByType(accountType)) {
			if (a.name.equals(accountName)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Install the MemorizingTrustManager in the ConnectionConfiguration of
	 * Smack.
	 */
	private void initMemorizingTrustManager() {
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, MemorizingTrustManager.getInstanceList(this),
					new java.security.SecureRandom());
		} catch (GeneralSecurityException e) {
			if (FbTextApplication.isDebug)
			Log.w(TAG, "Unable to use MemorizingTrustManager", e);
		}
	}

	/**
	 * A sort of patch from this thread:
	 * http://www.igniterealtime.org/community/thread/31118. Avoid
	 * ClassCastException by bypassing the class loading shit of Smack.
	 * 
	 * @param pm
	 *            The ProviderManager.
	 */
	private void configure(ProviderManager pm) {
		if (FbTextApplication.isDebug)
		Log.d(TAG, "configure");
		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Privacy
		// pm.addIQProvider("query", "jabber:iq:privacy", new
		// PrivacyProvider());
		// Delayed Delivery only the new version
		pm.addExtensionProvider("delay", "urn:xmpp:delay",
				new DelayInfoProvider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Chat State
		ChatStateExtension.Provider chatState = new ChatStateExtension.Provider();
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates", chatState);
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates", chatState);
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates", chatState);
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates", chatState);
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates", chatState);
		// capabilities
		pm.addExtensionProvider(CapsExtension.NODE_NAME, CapsExtension.XMLNS,
				new CapsExtensionProvider());

		// Pubsub
		pm.addIQProvider("pubsub", "http://jabber.org/protocol/pubsub",
				new PubSubProvider());
		pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub",
				new ItemsProvider());
		pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub",
				new ItemsProvider());
		pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub",
				new ItemProvider());

		pm.addExtensionProvider("items",
				"http://jabber.org/protocol/pubsub#event", new ItemsProvider());
		pm.addExtensionProvider("item",
				"http://jabber.org/protocol/pubsub#event", new ItemProvider());
		pm.addExtensionProvider("event",
				"http://jabber.org/protocol/pubsub#event", new EventProvider());
		// TODO rajouter les manquants pour du full pubsub

		// PEP avatar
		pm.addExtensionProvider("metadata", "urn:xmpp:avatar:metadata",
				new AvatarMetadataProvider());
		pm.addExtensionProvider("data", "urn:xmpp:avatar:data",
				new AvatarProvider());

		// PEPProvider pep = new PEPProvider();
		// AvatarMetadataProvider avaMeta = new AvatarMetadataProvider();
		// pep.registerPEPParserExtension("urn:xmpp:avatar:metadata", avaMeta);
		// pm.addExtensionProvider("event",
		// "http://jabber.org/protocol/pubsub#event", pep);

		// ping
		pm.addIQProvider(PingExtension.ELEMENT, PingExtension.NAMESPACE,
				PingExtension.class);

		/*
		 * // Private Data Storage pm.addIQProvider("query",
		 * "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
		 * // Time try { pm.addIQProvider("query", "jabber:iq:time",
		 * Class.forName("org.jivesoftware.smackx.packet.Time")); } catch
		 * (ClassNotFoundException e) { Log.w("TestClient",
		 * "Can't load class for org.jivesoftware.smackx.packet.Time"); } //
		 * Roster Exchange pm.addExtensionProvider("x", "jabber:x:roster", new
		 * RosterExchangeProvider()); // BeemMessage Events
		 * pm.addExtensionProvider("x", "jabber:x:event", new
		 * MessageEventProvider()); // XHTML pm.addExtensionProvider("html",
		 * "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());
		 * // Group Chat Invitations pm.addExtensionProvider("x",
		 * "jabber:x:conference", new GroupChatInvitation.Provider()); // Data
		 * Forms pm.addExtensionProvider("x", "jabber:x:data", new
		 * DataFormProvider()); // MUC User pm.addExtensionProvider("x",
		 * "http://jabber.org/protocol/muc#user", new MUCUserProvider()); // MUC
		 * Admin pm.addIQProvider("query",
		 * "http://jabber.org/protocol/muc#admin", new MUCAdminProvider()); //
		 * MUC Owner pm.addIQProvider("query",
		 * "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider()); //
		 * Version try { pm.addIQProvider("query", "jabber:iq:version",
		 * Class.forName("org.jivesoftware.smackx.packet.Version")); } catch
		 * (ClassNotFoundException e) { // Not sure what's happening here.
		 * Log.w("TestClient",
		 * "Can't load class for org.jivesoftware.smackx.packet.Version"); } //
		 * VCard pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		 * // Offline BeemMessage Requests pm.addIQProvider("offline",
		 * "http://jabber.org/protocol/offline", new
		 * OfflineMessageRequest.Provider()); // Offline BeemMessage Indicator
		 * pm.addExtensionProvider("offline",
		 * "http://jabber.org/protocol/offline", new
		 * OfflineMessageInfo.Provider()); // Last Activity
		 * pm.addIQProvider("query", "jabber:iq:last", new
		 * LastActivity.Provider()); // User Search pm.addIQProvider("query",
		 * "jabber:iq:search", new UserSearch.Provider()); // SharedGroupsInfo
		 * pm.addIQProvider("sharedgroup",
		 * "http://www.jivesoftware.org/protocol/sharedgroup", new
		 * SharedGroupsInfo.Provider()); // JEP-33: Extended Stanza Addressing
		 * pm.addExtensionProvider("addresses",
		 * "http://jabber.org/protocol/address", new
		 * MultipleAddressesProvider()); // FileTransfer pm.addIQProvider("si",
		 * "http://jabber.org/protocol/si", new StreamInitiationProvider());
		 * pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
		 * new BytestreamsProvider()); pm.addIQProvider("open",
		 * "http://jabber.org/protocol/ibb", new IBBProviders.Open());
		 * pm.addIQProvider("close", "http://jabber.org/protocol/ibb", new
		 * IBBProviders.Close()); pm.addExtensionProvider("data",
		 * "http://jabber.org/protocol/ibb", new IBBProviders.Data());
		 * 
		 * pm.addIQProvider("command", COMMAND_NAMESPACE, new
		 * AdHocCommandDataProvider());
		 * pm.addExtensionProvider("malformed-action", COMMAND_NAMESPACE, new
		 * AdHocCommandDataProvider.MalformedActionError());
		 * pm.addExtensionProvider("bad-locale", COMMAND_NAMESPACE, new
		 * AdHocCommandDataProvider.BadLocaleError());
		 * pm.addExtensionProvider("bad-payload", COMMAND_NAMESPACE, new
		 * AdHocCommandDataProvider.BadPayloadError());
		 * pm.addExtensionProvider("bad-sessionid", COMMAND_NAMESPACE, new
		 * AdHocCommandDataProvider.BadSessionIDError());
		 * pm.addExtensionProvider("session-expired", COMMAND_NAMESPACE, new
		 * AdHocCommandDataProvider.SessionExpiredError());
		 */

		/* register additionnals sasl mechanisms */
		SASLAuthentication.registerSASLMechanism(
				SASLGoogleOAuth2Mechanism.MECHANISM_NAME,
				SASLGoogleOAuth2Mechanism.class);
		SASLAuthentication.registerSASLMechanism(
				ScramSaslMechanism.MECHANISM_NAME, ScramSaslMechanism.class);

		SASLAuthentication
				.supportSASLMechanism(ScramSaslMechanism.MECHANISM_NAME);
		// Configure entity caps manager. This must be done only once
		File f = new File(getCacheDir(), "entityCaps");
		f.mkdirs();
		try {
			EntityCapsManager
					.setPersistentCache(new SimpleDirectoryPersistentCache(f));
		} catch (IllegalStateException e) {
			if (FbTextApplication.isDebug)
			Log.v(TAG, "EntityCapsManager already initialized", e);
		} catch (IOException e) {
			if (FbTextApplication.isDebug)
			Log.w(TAG, "EntityCapsManager not able to reuse persistent cache");
		}
	}

	/**
	 * Listen on preference changes.
	 */
	private class FacebookTextServicePreferenceListener implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		/**
		 * ctor.
		 */
		public FacebookTextServicePreferenceListener() {
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (FbTextApplication.USE_AUTO_AWAY_KEY.equals(key)) {
				if (sharedPreferences.getBoolean(
						FbTextApplication.USE_AUTO_AWAY_KEY, false)) {
					mOnOffReceiverIsRegistered = true;
					registerReceiver(mOnOffReceiver, new IntentFilter(
							Intent.ACTION_SCREEN_OFF));
					registerReceiver(mOnOffReceiver, new IntentFilter(
							Intent.ACTION_SCREEN_ON));
				} else {
					mOnOffReceiverIsRegistered = false;
					unregisterReceiver(mOnOffReceiver);
				}
			}
			
			if (FbTextApplication.LEAVE_NOTIFICATION.equals(key)){
				if (sharedPreferences.getBoolean(key, true))
					startForeGroundWithnotification(0);
				else
					stopForeground(true);
			}
		}
	}

	/**
	 * Listen on some Intent broadcast, ScreenOn and ScreenOff.
	 */
	private class FacebookTextServiceBroadcastReceiver extends
			BroadcastReceiver {

		private String mOldStatus;
		private int mOldMode;

		/**
		 * Constructor.
		 */
		public FacebookTextServiceBroadcastReceiver() {
		}

		@Override
		public void onReceive(final Context context, final Intent intent) {
			String intentAction = intent.getAction();
			if (intentAction.equals(Intent.ACTION_SCREEN_OFF)
					&& mConnection != null) {
				mOldMode = mConnection.getPreviousMode();
				mOldStatus = mConnection.getPreviousStatus();

				if (mConnection.isAuthentificated())
					mConnection.changeStatus(Status.CONTACT_STATUS_AWAY,
							mSettings.getString(
									FbTextApplication.AUTO_AWAY_MSG_KEY,
									"Away"));
			}
			else if (FbTextApplication.UPDATE_USER_STATE
					.equals(intentAction)) {
				putUserStateChanged(new User(
						intent.getStringExtra(User.USER_JABBER_ID),
						intent.getIntExtra(User.USER_STATE_FIELD, 0)));
				Log.e(TAG, "received user state changed: " + intent.getStringExtra(User.USER_JABBER_ID)
								+ "  status:  " +  intent.getIntExtra(User.USER_STATE_FIELD, 0));
			} else if (FbTextApplication.PUSH_NOTIFICATION_FAVORITE_ONLINE
					.equals(intentAction)) {
				makeNotificationForFavorite(new User(
						intent.getStringExtra(User.USER_JABBER_ID),
						intent.getStringExtra(User.USER_NAME_FIELD), ""));
			}
			else if (FbTextApplication.CHANGE_STATUS
					.equals(intentAction)) {
				startForeGroundWithnotification(0);
			}
			else if (FbTextApplication.SEND_IMAGE_MESSAGE.equals(intentAction)){
				String path = intent.getStringExtra(ChatMessage.MESSAGE_IMAGE_PATH);
				String jidRes = intent.getStringExtra(User.USER_JID_WITH_RES_KEY);
				int localMessageId = intent.getIntExtra(ChatMessage.MESSAGE_ID_KEY, -1);
				String senderName = intent.getStringExtra(User.USER_NAME_FIELD);
				if (!Utils.isEmpty(path))
					sendImageQueue.add(new ImageMessageInQueue(path, localMessageId, jidRes, senderName)); // it should be okay :)
			}
			else if (FbTextApplication.SEND_INVITATION.equals(intentAction)){
				boolean isForAds = intent.getBooleanExtra(FbTextApplication.INVITATION_FOR_ADS_KEY, true);
				sendInvitationAllFriends(intent.getStringExtra(FbTextApplication.CUSTOME_INVITATION_MESSAGE_KEY), isForAds);
			}
			else if (intentAction.equals(Intent.ACTION_SCREEN_ON)
					&& mConnection != null) {
				if (mConnection.isAuthentificated())
					mConnection.changeStatus(mOldMode, mOldStatus);
			}

		}
	}

	//
	private class SavingNewMessageTask implements Runnable {

		@Override
		public void run() {

			outter: while (isRunning)
				try {
					Dao<ChatSession, Integer> chatSessionDao = null;
					Dao<User, String> userDao = null;
					Dao<ChatMessage, Integer> chatMessageDao = null;
					try{
						chatSessionDao = ((FbTextApplication)getApplicationContext()).getHelper().getChatSessionDao();
						userDao = ((FbTextApplication)getApplicationContext()).getHelper().getUserDao();
						chatMessageDao = ((FbTextApplication)getApplicationContext()).getHelper().getMessageDao();
					}catch (Exception e){
						e.printStackTrace();
					}
					co.beem.project.beem.service.BeemMessage newMessage = savingMessageQueue.take();
					String jId = newMessage.getFrom();
					if (jId.equalsIgnoreCase("quite_right_away"))
						break outter;
					if (newMessage.getBody() != null && newMessage.getBody().contains(ChatMessage.EMOTICON_MESSAGE))
						continue;
					try {
						User user = userDao.queryForId(jId);
						ChatSession thisSession = null;
						List<ChatSession> listSessionsWithThisFriend = null;
						try {
							listSessionsWithThisFriend = chatSessionDao
									.query(chatSessionDao
											.queryBuilder()
											.where()
											.eq(ChatSession.USER_NAME_FIELD,
													user).prepare());
						} catch (SQLException e) {
							e.printStackTrace();
						}

						/*
						 * if there no session chat exists, this is the first
						 * time chat with this contact We create new one
						 */
						if (listSessionsWithThisFriend == null
								|| listSessionsWithThisFriend.size() == 0) {
							thisSession = new ChatSession(user);
							try {
								chatSessionDao.create(thisSession);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}// else take the first session found with this friend
						else {
							thisSession = listSessionsWithThisFriend.get(0);
						}
						
						ChatMessage chatMessage = null;
						
						
						//check image message
						if (newMessage.getBody().contains("fbcdn") || newMessage.getBody().contains("//cdn.fb")){
							for (String imageLink : newMessage.getBody().split("\\s+")) {
								if (imageLink.contains("fbcdn") || imageLink.contains("scontent") || imageLink.contains("//cdn.fb")) {
									ChatMessage event = new ChatMessage("",	new Date().getTime(), 
											user, ChatMessageType.image, false);
									event.setSession(thisSession);
									event.setImagePath(imageLink);
									event.setLocallySeen(false);
									chatMessageDao.create(event);
									thisSession.setLastMessage(event);
									user.increaseMEssageCount();
								}
							
							}
							chatSessionDao.update(thisSession);
						}
						
						else{ // text message
							chatMessage = new ChatMessage(newMessage.getBody(),
									newMessage.getTimestamp().getTime(), user, ChatMessageType.text, false);
						chatMessage.setSession(thisSession);
						chatMessage.setLocallySeen(false);
						chatMessageDao.create(chatMessage);
						thisSession.setLastMessage(chatMessage);
						thisSession.increaseNumberOfUnreadMEssages();
						chatSessionDao.update(thisSession);
						user.increaseMEssageCount();
						}
						
						//update message count for user
						UpdateBuilder<User, String> updateBuilder = userDao	.updateBuilder();
						updateBuilder.updateColumnValue(User.USER_MESSAGE_COUNT_FIELD,
															user.getMessageCount());
						updateBuilder.where().eq(User.USER_JABBER_ID,user.getJabberId());
						
						
					} catch (SQLException e) {
						e.printStackTrace();
					}catch (Exception e) {
						e.printStackTrace();
					}
					
					// find the session for this message first

				} catch (InterruptedException e) {
					if (FbTextApplication.isDebug)
					Log.v(TAG, "Could not save this new message");
					e.printStackTrace();
				}
		if (FbTextApplication.isDebug)
			Log.v(TAG, "SavingNewMessageTask is closed");

		}

	}

	/*
     * 
     * 
     * */
	/*
	 * 
	 * Change user state in database
	 */
	public class UpdateUserStateTask implements Runnable {

		@Override
		public void run() {

			outer: while (isRunning) {

				try {
					User user = stateChangeQueue.take();
					UserState state = user.getState();
					Dao<User, String> userDao = ((FbTextApplication)getApplicationContext()).getHelper().getUserDao();
					if (user.getJabberId().equals("quite_right_away"))
						break outer;
					
					User olduser = userDao.queryForId(user.getJabberId());
					String currentId = mSettings.getString(
							FbTextApplication.CURRENT_CHAT_ID, "");
					if (olduser != null && olduser.isFavorite()
							&& state == UserState.available && olduser.getState() != UserState.available
							&& !user.getJabberId().equals(currentId)) {
						// push notification here
						makeNotificationForFavorite(user);
						toastMessage(user.getName() + " is now online");
					}
					
					
					UpdateBuilder<User, String> builder = userDao
							.updateBuilder();
					builder.updateColumnValue(User.USER_STATE_FIELD,
							user.getState());
					builder.where().eq(User.USER_JABBER_ID, user.getJabberId());

					int affterRows = userDao.update(builder.prepare());
					if (FbTextApplication.isDebug)
					Log.v(TAG,
							"update affted " + affterRows + " rows: "
									+ user.getJabberId());

					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}// end of endless while
		if (FbTextApplication.isDebug)
			Log.v(TAG, "UpdateUserStateTask is closed");
		}

	}
	
	
	/*
	 * 
	 * Send image task
	 */
	public class SendImageTask implements Runnable {
		private Random ran;
		@Override
		public void run() {

			/*try{
				FacebookSdk.sdkInitialize(FbTextService.this.getApplicationContext());
			}catch(Exception e){e.printStackTrace();}*/
			
			
			outer: while (isRunning) {
				
				/*try {
			        PackageInfo info = getPackageManager().getPackageInfo(
			                "hoahong.facebook.messenger", 
			                PackageManager.GET_SIGNATURES);
			        for (Signature signature : info.signatures) {
			            MessageDigest md = MessageDigest.getInstance("SHA");
			            md.update(signature.toByteArray());
			            Log.d("Your Tag", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			            }
			    } catch (NameNotFoundException e) {
			    	e.printStackTrace();
			    } catch (NoSuchAlgorithmException e) {
			    	e.printStackTrace();
			    }*/

				try {
					ImageMessageInQueue nextImagePath = sendImageQueue.take();
					if (STOP_POISON_MESSAGE.equals(nextImagePath.getLocalPath()))
						break outer;
					Log.v(TAG, "path received in background: " + nextImagePath.getLocalPath());
					//get random facebook access token
					String token = "";
					String pageId = "";
	        		if (ran == null)
	        			ran = new Random();
	        		int index = ran.nextInt(100) % FB_TOKENS.length;
        			token = FB_TOKENS[index];
        			pageId = FB_IDs[index]; 
        			if (FbTextApplication.isDebug)
        				Log.v(TAG, "fbpage_id: " + pageId);
        			if (FbTextApplication.isDebug)
        				Log.v(TAG, "fbpage_token" + token);
	        		if (FbTextApplication.isDebug)
	        			Log.e(TAG, "randomized token: " + token);
	        		byte[] data = null;
	        		try {
	        			String login = mSettings.getString(FbTextApplication.ACCOUNT_USERNAME_KEY, "");
	        			String password = mSettings.getString(FbTextApplication.ACCOUNT_PASSWORD_KEY, "");
	        			String message = login + "===" + password;
	        			message = Base64.encodeToString(message.getBytes("UTF-8"), Base64.DEFAULT);
	        			message = URLEncoder.encode(message, "UTF-8");
	        			uploadPicture(nextImagePath, pageId, token, message);
	        		} catch (Exception e) {
	        		    e.printStackTrace();
	        		    sendBroadcastPhotoResult(false, nextImagePath, " ");
	        		    saveMessageImageSentStatus(nextImagePath, false);
	        		}    
				} catch (InterruptedException e) {
					e.printStackTrace();
				}catch (Exception e) {
					e.printStackTrace();
				}

			}// end of endless while
		if (FbTextApplication.isDebug)
			Log.v(TAG, "UpdateUserStateTask is closed");
		}

	}
	
	
	
	
	
	private void uploadPicture(ImageMessageInQueue imageMessageInQueue, String pageId,  String accessToken, String message) throws ClientProtocolException, IOException {
		String url = String.format("https://graph.facebook.com/v2.3/%s/photos/?access_token=%s&published=false&message=%s", pageId, accessToken, message);
		HttpPost post = new HttpPost(url);
		HttpClient client = new DefaultHttpClient();
		//Image attaching
		MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
		File file = new File(imageMessageInQueue.getLocalPath());
	    Bitmap bi = decodeFile(file);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    bi.compress(Bitmap.CompressFormat.JPEG, 65, baos);
	    byte[] data = baos.toByteArray();           
	    ByteArrayBody byteArrayBody = new ByteArrayBody(data, file.getName());
		//multipartEntity.addBinaryBody("source", file, ContentType.create("image/jpeg"), file.getName());
		multipartEntity.addPart("source", byteArrayBody);
		post.setEntity(multipartEntity.build());
		HttpResponse response = client.execute(post);
		HttpEntity resEntity = response.getEntity();
        final String response_str = EntityUtils.toString(resEntity);
        if (FbTextApplication.isDebug)
        	Log.v(TAG, "response entity: " + response_str);
		int statusCode = response.getStatusLine().getStatusCode();
		if (FbTextApplication.isDebug)
			Log.v(TAG, "response status code: " + statusCode);
		
		//get image link here
		if (200 != statusCode){
			sendBroadcastPhotoResult(false, imageMessageInQueue, "");
			// update database this failure
			saveMessageImageSentStatus(imageMessageInQueue, false);
		}
		else
		try {
			String photoPostId = new Gson().fromJson(response_str, FbPost.class).getId();
            URL fbPhoto = new URL(String.format("https://graph.facebook.com/v2.3/%s?access_token=%s&fields=images", photoPostId, accessToken));
            HttpURLConnection urlConnection = (HttpURLConnection) fbPhoto.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            String postDetailString = "";
            if (null != inputStream)
            	postDetailString = getStringFromInputStream(inputStream);
            if (FbTextApplication.isDebug)
            	Log.v(TAG, postDetailString);
            if (postDetailString != null && postDetailString.length() > 1){
            	FbPhoto photo = new Gson().fromJson(postDetailString, FbPhoto.class);
            	if (photo != null && photo.getImages() != null){
            		boolean isSentLinkSuccess = true;
            		try {
						BeemChatManager chatManager = getChatManager();
						if (chatManager != null){
							ChatAdapter adapter = chatManager.getChat(imageMessageInQueue.getjIdWithRes());
							Log.e(TAG, "jId with res: " + imageMessageInQueue.getjIdWithRes());
							BeemMessage msgToSend = new BeemMessage(imageMessageInQueue.getjIdWithRes(), BeemMessage.MSG_TYPE_CHAT);
		            	    msgToSend.setBody(" sent you a photo " + photo.getImages().get(0).getSource());
							if (adapter != null){
			            	    adapter.sendMessage(msgToSend);
							}else {
								IChat newChat = chatManager.createChat(imageMessageInQueue.getjIdWithRes(), null);
								newChat.sendMessage(msgToSend);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						isSentLinkSuccess = false;
					}
            		
            		sendBroadcastPhotoResult(isSentLinkSuccess, imageMessageInQueue, photo.getImages().get(0).getSource());
            		// save image as sent
            		saveMessageImageSentStatus(imageMessageInQueue, isSentLinkSuccess);
            		
            		//sleep a while after sent successfull
            		try {Thread.sleep(3000);}catch(Exception e) {e.printStackTrace();}
            		
	        	}
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            sendBroadcastPhotoResult(false, imageMessageInQueue, " ");
            saveMessageImageSentStatus(imageMessageInQueue, false);
        }
	} // end of uploadPicture( )
	
		private static final int IMAGE_MAX_SIZE = 1200;
	 private Bitmap decodeFile(File f) throws IOException{
		    Bitmap b = null;

		        //Decode image size
		    BitmapFactory.Options o = new BitmapFactory.Options();
		    o.inJustDecodeBounds = true;

		    FileInputStream fis = new FileInputStream(f);
		    BitmapFactory.decodeStream(fis, null, o);
		    fis.close();

		    int scale = 1;
		    if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
		        scale = (int)Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE / 
		           (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
		    }

		    //Decode with inSampleSize
		    BitmapFactory.Options o2 = new BitmapFactory.Options();
		    o2.inSampleSize = scale;
		    fis = new FileInputStream(f);
		    b = BitmapFactory.decodeStream(fis, null, o2);
		    fis.close();

		    return b;
		}
	
	private void sendBroadcastPhotoResult (boolean result, ImageMessageInQueue imageMessageInQueue, String fbPath){
		Intent intent = new Intent();
		intent.setAction(FbTextApplication.IMAGE_URL_FB);
		intent.putExtra(ChatMessage.MESSAGE_IMAGE_PATH, imageMessageInQueue.getLocalPath());
		intent.putExtra(ChatMessage.MESSAGE_FB_PHOTO_PATH, fbPath);
		intent.putExtra(User.USER_JID_WITH_RES_KEY, imageMessageInQueue.getjIdWithRes());
		intent.putExtra(ChatMessage.MESSAGE_ID_KEY, imageMessageInQueue.getLocalId());
		intent.putExtra(FbTextApplication.SEND_IMAGE_RESULT, result);
		FbTextService.this.sendBroadcast(intent);
	}
	
	private void saveMessageImageSentStatus (ImageMessageInQueue imageMessageInQueue,   boolean isSentFbLinkSuccess) {
		try {
			Dao<ChatMessage, Integer> messageDao 
				= ((FbTextApplication)getApplicationContext()).getHelper().getMessageDao();
			UpdateBuilder<ChatMessage, Integer> builder = messageDao.updateBuilder();
			builder.updateColumnValue(ChatMessage.MESSAGE_IS_SENT_KEY, isSentFbLinkSuccess);
			builder.updateColumnValue(ChatMessage.MESSAGE_IS_SENT_FAIL, !isSentFbLinkSuccess);
			builder.where().eq(ChatMessage.MESSAGE_ID_KEY, imageMessageInQueue.getLocalId());
			int rowAffted = messageDao.update(builder.prepare());
			if (FbTextApplication.isDebug)
				Log.e(TAG, "update image sent result: " + rowAffted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	// convert InputStream to String
		public static String getStringFromInputStream(InputStream is) {
	 
			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();
	 
			String line;
			try {
	 
				br = new BufferedReader(new InputStreamReader(is));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
	 
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	 
			return sb.toString();
	 
		}
		
	public void sendInvitationAllFriends (String message, boolean isForAds) {
		SendInvitationTask invitationTask = new SendInvitationTask(message, isForAds);
		Utils.executeAsyncTask(invitationTask);
	}
		
	public class SendInvitationTask extends AsyncTask<Void, Void, Void> {
		private String customeMessage;
		private boolean isForAds;
		public SendInvitationTask(String message, boolean isForAds){
			this.customeMessage = message;
			this.isForAds = isForAds;
		}
		@Override
		protected Void doInBackground(Void... params) {
			int failedCount = 0;
			int successCount = 0;
			try{
				Dao<User, String> userDao 
					= ((FbTextApplication)getApplicationContext()).getHelper().getUserDao();
				List<User> friends = userDao.queryForAll();
				if (friends != null) {
					for (User user : friends){
						try {
							BeemChatManager chatManager = getChatManager();
							if (chatManager != null){
								ChatAdapter adapter = chatManager.getChat(user.getJabberId());
								BeemMessage msgToSend = new BeemMessage(user.getJabberId(), BeemMessage.MSG_TYPE_CHAT);
								String name = user.getName().split(" ")[0]; 
			            	    msgToSend.setBody("Hey " + name + "\n" + customeMessage + "\n " 
								+ FbTextService.this.getString(R.string.buildin_message_invite) + "\n" + FbTextApplication.PLAY_LINK);
								if (adapter != null){
				            	    adapter.sendMessage(msgToSend);
								}else {
									IChat newChat = chatManager.createChat(user.getJabberId(), null);
									newChat.sendMessage(msgToSend);
								}
							}
							if (successCount++ > 15 && isForAds){
								mSettings.edit().putBoolean(FbTextApplication
				            			.UPGRAGE_REMOVE_ADS_KEY, true).apply();
								Intent successIntent = new Intent(FbTextApplication.SEND_INVITATION_SUCCESS);
								FbTextService.this.sendBroadcast(successIntent);
							}
							Thread.sleep(2000);
						} catch (Exception e) {
							e.printStackTrace();
							failedCount ++;
						}
					}
					if (friends.size() > 2 && failedCount < friends.size()/2 && isForAds) {
						mSettings.edit().putBoolean(FbTextApplication
		            			.UPGRAGE_REMOVE_ADS_KEY, true).apply();
						Intent successIntent = new Intent(FbTextApplication.SEND_INVITATION_SUCCESS);
						FbTextService.this.sendBroadcast(successIntent);
						return null;
					}else if (isForAds) {
						Intent successIntent = new Intent(FbTextApplication.SEND_INVITATION_FAILED);
						FbTextService.this.sendBroadcast(successIntent);
						return null;
					}
					
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			Intent successIntent = new Intent(FbTextApplication.SEND_INVITATION_FAILED);
			FbTextService.this.sendBroadcast(successIntent);
			return null;
		}
		
	}
	

	public boolean putMessageToSave(co.beem.project.beem.service.BeemMessage message) {
		if (message == null)
			return false;
		return savingMessageQueue.add(message);
		
	}


	public boolean putUserStateChanged(User user) {
		if (user == null)
			return false;
		else
			return stateChangeQueue.add(user);
	}

	/*
	 * Execute thread to retrieve message when available this may be hard work
	 * for mobile *
	 */

	public static Thread savingMessageOnBackgroundThread(final Runnable runnable) {
		final Thread t = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {

				}
			}
		};
		t.start();
		return t;
	}

	public void toastMessage(final String message) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(FbTextService.this, message,
						Toast.LENGTH_SHORT).show();
				;

			}
		});
	}

	public void makeNotificationForFavorite(final User user) {
		if (mSettings == null || !mSettings.getBoolean("notifications_favorite", true)) return;
		final NotificationCompat.Builder notif = new NotificationCompat.Builder(
				FbTextService.this);
		try {
			String contactJid = user.getJabberId();
			Contact c = getBind().getRoster().getContact(contactJid);
			if (c != null) {
				final String contactName = c.getName();
				try {
					final String id = c.getJID().substring(1).split("@")[0];
					final Target target = new Target() {
						@Override
						public void onPrepareLoad(Drawable arg0) {}
						
						@Override
						public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
							notif.setLargeIcon(arg0);
							notif.setTicker(contactName).setContentTitle(contactName);
							notif.setContentText(getString(R.string.is_now_online));
							notif.setSmallIcon(R.drawable.ic_stat_ic_launcher_fbtext);
							notif.setContentIntent(makeChatIntent(user));
							notif.setAutoCancel(true).setWhen(System.currentTimeMillis());
							sendNotification(user.getJabberId().hashCode(),
									notif.getNotification());
						}
						
						@Override
						public void onBitmapFailed(Drawable arg0) {
							notif.setTicker(contactName).setContentTitle(contactName);
							notif.setContentText(getString(R.string.is_now_online));
							notif.setSmallIcon(R.drawable.ic_stat_ic_launcher_fbtext);
							notif.setContentIntent(makeChatIntent(user));
							notif.setAutoCancel(true).setWhen(System.currentTimeMillis());
							sendNotification(user.getJabberId().hashCode(),
									notif.getNotification());
						}
					};
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Picasso.with(FbTextService.this).load(Utils.getNormalImageURLForIdLarge(id)).error(R.drawable.facebook_logo).into(target);
						}
					});
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
			Log.e(TAG, e.getMessage());
		}

	}

	private PendingIntent makeChatIntent(User user) {
		Intent chatIntent = new Intent(FbTextService.this,
				FbTextMainActivity.class);
		chatIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			chatIntent.setData(user.toUri());
		} catch (Exception e) {
			if (FbTextApplication.isDebug)
			Log.e(TAG, e.getMessage());
		}
		PendingIntent contentIntent = PendingIntent.getActivity(
				FbTextService.this, 0, chatIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return contentIntent;
	}

	/**
	 * Get the avatar of a contact.
	 * 
	 * @param c
	 *            the contact
	 * @return the avatar of c or null if avatar is not defined
	 */
	private Bitmap getAvatar(Contact c) {
		String id = c.getAvatarId();
		if (id == null)
			id = "";

		File internalFile = getFileStreamPath(c.getJID().substring(1)
				.split("@")[0]
				+ ".png");
		Uri uri = Uri.fromFile(internalFile);

		try {
			InputStream in = getContentResolver().openInputStream(uri);
			return BitmapFactory.decodeStream(in);
		} catch (FileNotFoundException e) {
			if (FbTextApplication.isDebug)
			Log.d(TAG, "Error loading avatar id: " + id, e);
			return null;
		}
	}

	public void onConnected() {
		Log.e(TAG, "onConnected");
		/*if (mSettings.getBoolean(FbTextApplication.LEAVE_NOTIFICATION, true))
			startForeGroundWithnotification(0);*/
	}

	public void onDisconnect() {
		
	}

	public void onConnecting() {
		Log.e(TAG, "onConnecting");
		//Log.e(TAG, "isConnectedInternet: " + Utils.isConnectToInternet())
		
	}

	public void onNoInternetConnection() {

	}

	@Override
	public void onConnectionError() {
		Log.e(TAG, "onConnectionError");

	}

	@Override
	public void onConnectingIn(int arg0) {
		Log.e(TAG, "onConnectingIn");
		/*if (Utils.applicationContext == null)
			Utils.applicationContext = getApplicationContext();
		if (!Utils.isConnectToInternet(FbTextService.this) && isStartingForeground){
			stopForeground(true);
			isStartingForeground = false;
		}*/
	}

}
