package hoahong.facebook.messenger.ui.android;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jivesoftware.smack.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.internal.fn;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import co.beem.project.beem.service.Contact;
import co.beem.project.beem.service.PresenceAdapter;
import co.beem.project.beem.service.aidl.IBeemRosterListener;
import co.beem.project.beem.service.aidl.IChatManager;
import co.beem.project.beem.service.aidl.IRoster;
import co.beem.project.beem.service.aidl.IXmppFacade;
import co.beem.project.beem.ui.Login;
import co.beem.project.beem.ui.wizard.Account;
import co.beem.project.beem.utils.BeemBroadcastReceiver;
import co.beem.project.beem.utils.PresenceType;
import co.beem.project.beem.utils.Status;
import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.connection.FacebookTextConnectionListener;
import hoahong.facebook.messenger.data.User;
import hoahong.facebook.messenger.data.UserState;
import hoahong.facebook.messenger.ui.Utils;

public class FbTextMainActivity extends AppCompatActivity implements FacebookTextConnectionListener, NotGoodFeedBackListener{

	public static final String LOG_TAG = "FbTextMainActivity";
	public static final String STATE_SELECTED_NAVIGATION_ITEM = "selected tab";
	public static final int START_CHAT_REQUEST_CODE = 1;
	public static final int UPGRADE_REMOVEADS_INAPP_REQUEST = 2;

	// connection to beem serivce
	private static Intent SERVICE_INTENT = new Intent();
	static {
		SERVICE_INTENT.setComponent(new ComponentName("hoahong.facebook.messenger",
				"co.beem.project.beem.FbTextService"));
		SERVICE_INTENT.setAction(FbTextApplication.QUIT_RIGHT_AWAY);
	}
	// connection to billing service
	IInAppBillingService mService;

	ServiceConnection mServiceConn = new ServiceConnection() {
	   @Override
	   public void onServiceDisconnected(ComponentName name) {
	       mService = null;
	   }

	   @Override
	   public void onServiceConnected(ComponentName name, 
	      IBinder service) {
	       mService = IInAppBillingService.Stub.asInterface(service);
	       if (FbTextApplication.isDebug)
	       Log.v(LOG_TAG, "connected to Play service");
	       if (!mSettings.getBoolean(FbTextApplication.UPGRAGE_REMOVE_ADS_KEY, false)){
				GetPurchasedTask testInApp = new GetPurchasedTask();
				Utils.executeAsyncTask(testInApp);
	       }
	   }
	};
	
	
	
	private final ServiceConnection mServConn = new FacebookTextServiceConnection();
	private final BeemBroadcastReceiver mReceiver = new BeemBroadcastReceiver();
	private Handler mHandler = new Handler();	
	private IXmppFacade mXmppFacade;
	private final BeemRosterListener mBeemRosterListener = new BeemRosterListener();
	private IRoster mRoster;
	private boolean mBinded;
	private IChatManager mChatManager;
	private SharedPreferences mSettings;
	// view reference
	//private ActionBar actionBar;
	//private UserFragment usersFragment;
	//private ChatFragment chatsFragment;
	public static int loadingTime;
	private static boolean isOnline = false;
	private static int selectedState;
	private	SessionManager sessionManager;
	private boolean isConnected;
	private boolean isSavingData;
	// isConnectIn is for start the reconnection manager
	// right away if there is network connection and reconnector is sleep
	private boolean isConnectingIn = false;
	private int connectingInSeconds;
	private FeedbackDialog feedbackDialog;
	private boolean isShowFeedBack;
	private AskFeedBackDialog askFeedBackDialog;
	private InterstitialAd mInterstitialAd;
	
	// Whether or not this is a search result view of this fragment, only used
	// on pre-honeycomb
	// OS versions as search results are shown in-line via Action Bar search
	// from honeycomb onward
	private boolean mIsSearchResultView = false;

	private Dao<User, String> userDao;
	private String mSearchTerm;
	private static SearchView searchView;
	private SelectStateFragment changeStatusFragment;
	private	AlertDialog alertDialog;

	
	public static boolean isRunning;
	
	ViewPagerAdapter fragmentAdapter;
	Toolbar toolbar;
	ViewPager viewpager;
	TabLayout tabs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//startService(new Intent(FbTextMainActivity.this, FbTextService.class));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(FbTextApplication.isDebug)
			Log.v(LOG_TAG, "Main activity oncreate");
		// bind to background service when create
		// automaticBindBackgroundService();
		sessionManager = new SessionManager(FbTextMainActivity.this);

		//go back to screen login if account not configured
		FbTextApplication mApplication = (FbTextApplication) getApplication();
		if (mApplication != null && !mApplication.isAccountConfigured()) {
			Log.v("Login", " and app is not connected -> call Account");
			startActivity(new Intent(this, Account.class));
			finish();
		}
		
		
		
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CLOSED));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTED));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTING));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_DISCONNECT));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_NEW_MESSAGE_ARRIVED));
		this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTING_In));
		this.registerReceiver(mReceiver, new IntentFilter(FbTextApplication.SEND_INVITATION_FAILED));
		this.registerReceiver(mReceiver, new IntentFilter(FbTextApplication.SEND_INVITATION_SUCCESS));
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		if (Utils.applicationContext == null)
			Utils.applicationContext = getApplicationContext();
		
		if (savedInstanceState == null) {
			// remove create fragment here

		} else {
			// If we're restoring state after this fragment was recreated then
			// retrieve previous search term and previously selected search
			// result.
			mSearchTerm = savedInstanceState.getString(SearchManager.QUERY);
		}
		
		
		toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
	    setSupportActionBar(toolbar);
	    fragmentAdapter =  new ViewPagerAdapter(getSupportFragmentManager(),2);
		viewpager = (ViewPager) findViewById(R.id.pager);
		viewpager.setAdapter(fragmentAdapter);
		tabs = (TabLayout) findViewById(R.id.tabs);
		tabs.setTabMode(TabLayout.MODE_FIXED);
		tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setupWithViewPager(viewpager);
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            	viewpager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
    });
		
        
		// get status state
		int statusCode = mSettings.getInt(FbTextApplication.STATUS_KEY, 0);
		if (FbTextApplication.isDebug)
		Log.v(LOG_TAG, "status code: " + statusCode);
		if (statusCode == 4) {
			selectedState = 1;
			isOnline = false;
			supportInvalidateOptionsMenu();
		}else {
			selectedState = 0 ;
			isOnline = true;
			supportInvalidateOptionsMenu();
		}
		
		Utils.setContext(getApplicationContext());
		
		if (!mSettings.getBoolean(FbTextApplication.UPGRAGE_REMOVE_ADS_KEY, false)){
	        mInterstitialAd = new InterstitialAd(this);
	        mInterstitialAd.setAdUnitId("ca-app-pub-8484240274312605/1318420773");
	
	        mInterstitialAd.setAdListener(new AdListener() {
	            @Override
	            public void onAdClosed() {
	                requestNewInterstitial();
	            }
	        });
	        requestNewInterstitial();
		}
     // register billing transaction with google Play
 		Intent serviceIntent = new Intent(
 				"com.android.vending.billing.InAppBillingService.BIND");
 		serviceIntent.setPackage("com.android.vending");
 		bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	
	
	
	

	@Override
	protected void onStart() {
		Log.v(LOG_TAG, "Main activity onstart");
		super.onStart();
		/*Intent intent = null;
		 * 
		
			intent = new Intent(this, FacebookTextService.class);
		startService(intent);*/
		if (!mBinded)
		mBinded = bindService(SERVICE_INTENT, mServConn, BIND_AUTO_CREATE);
	}

	/*
	 * restore connection to background service when resume
	 */
	@Override
	protected void onResume() {
		Log.v(LOG_TAG, "Main activity onreSume");
		handleDisconnectAndLoginAgainFaild();
		isRunning = true;
		
		
		super.onResume();
	}
	
	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (isShowFeedBack){
			feedbackDialog = new FeedbackDialog();
    		feedbackDialog.show(getSupportFragmentManager(), "feedback_dialog");
    		isShowFeedBack = false;
		}
	}

	@Override
	  public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Restore the previously serialized current tab position.
	    /*if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
	    	getSupportActionBar().setSelectedNavigationItem(savedInstanceState
	    			.getInt(STATE_SELECTED_NAVIGATION_ITEM));
	    }*/
	  }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(SearchManager.QUERY, mSearchTerm);
		/*outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar()
		        .getSelectedNavigationIndex());*/
		super.onSaveInstanceState(outState);
	}

	/**
	 * unregister to service background
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		// dissmis and close view on pause
		if (searchView != null && !searchView.isIconified()) {
	        searchView.setIconified(true);
	    }
		
		if (changeStatusFragment != null && changeStatusFragment.isVisible())
			changeStatusFragment.dismiss();
		
		if (alertDialog != null && alertDialog.isShowing())
			alertDialog.dismiss();
		if (feedbackDialog != null && feedbackDialog.isVisible())
			feedbackDialog.dismiss();
		if (askFeedBackDialog != null && askFeedBackDialog.isVisible())
			askFeedBackDialog.dismiss();
		try {
			if (mRoster != null) {
				mRoster.removeRosterListener(mBeemRosterListener);
				mRoster = null;
			}
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
			Log.d(LOG_TAG, "Remote exception", e);
		}
		try{
			if (mBinded) {
				unbindService(mServConn);
				mBinded = false;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		mXmppFacade = null;
		

	}
	
	
	
	private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                  .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                  .build();
        if (mInterstitialAd != null)
        	mInterstitialAd.loadAd(adRequest);
    }
	
	

	/*
	 * on destroy, we release resource, connection, service
	 */
	@Override
	protected void onDestroy() {
		if (FbTextApplication.isDebug)
			Log.v(LOG_TAG, "Main activity onDestroy");
		isRunning = false;
		/*
		 * // stop receiving services try { doUnbindBackgroundService(); } catch
		 * (Throwable t) { Log.e(LOG_TAG,
		 * "Failed to unbind from the receving service", t); } stopService(new
		 * Intent(FbTextMainActivity.this, BackgroundMessageService.class));
		 */
		// stop sending service
		
		//remove static view
		searchView = null;
		
		this.unregisterReceiver(mReceiver);
		
		// save current orientation before destroyed 
		sessionManager.setCurrentOrientation(getResources().getConfiguration().orientation);
		
		if (FbTextApplication.isDebug)
		Log.d(LOG_TAG, "FbTextMainActivity on destroy");
		super.onDestroy();
		
		// unreagist with google play billing
	 	if (mService != null) {
		        unbindService(mServiceConn);
	    }  
		
	}
	
	// get Users fragment
	public UserFragment getUserFragment (){
		UserFragment userFragment =  (UserFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + 0);
		if (userFragment == null && fragmentAdapter != null)
			userFragment = (UserFragment) fragmentAdapter.getRegisteredFragment(0);
		return userFragment;
	}
	
	
	//get Chat fragment 
	
	public ChatFragment getChatFragment (){
		ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + 1);
		if (chatFragment == null && fragmentAdapter != null)
			chatFragment = (ChatFragment) fragmentAdapter.getRegisteredFragment(1);
		return chatFragment;
	}
	
	
	
	
	
	
	

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_old, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		MenuItem onlineItem = menu.findItem(R.id.action_change_state);
		if (!isOnline)
			onlineItem.setIcon(R.drawable.ic_ab_offline_user_indicator);
		else
			onlineItem.setIcon(R.drawable.ic_ab_online_user_indicator);
		
		/*if (getSupportActionBar().getSelectedTab().getTag() == "chats")
			searchItem.setVisible(false);
		else
			searchItem.setVisible(true);*/
		// display support or not
		if ( mSettings != null && mSettings.getBoolean(FbTextApplication.UPGRAGE_REMOVE_ADS_KEY, false))
			menu.findItem(R.id.action_upgrade_remove_ads).setVisible(false);
		else
			menu.findItem(R.id.action_upgrade_remove_ads).setVisible(true);
			
		if (FbTextApplication.isRatable)
			menu.findItem(R.id.action_rate).setVisible(true);
		else
			menu.findItem(R.id.action_rate).setVisible(false);
		// SearchView searchView = (SearchView)
		// MenuItemCompat.getActionView(searchItem);
		// Configure the search info and add any event listeners

		// In versions prior to Android 3.0, hides the search item to prevent
		// additional
		// searches. In Android 3.0 and later, searching is done via a
		// SearchView in the ActionBar.
		// Since the search doesn't create a new Activity to do the searching,
		// the menu item
		// doesn't need to be turned off.
		if (mIsSearchResultView) {
			searchItem.setVisible(false);
		}

		// In version 3.0 and later, sets up and configures the ActionBar
		// SearchView
		if (Utils.hasHoneycomb()) {

			// Retrieves the system search manager service
			final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

			// Retrieves the SearchView from the search menu item
			searchView = (SearchView) searchItem
					.getActionView();

			// Assign searchable info to SearchView
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));

			// Set listeners for SearchView
			searchView
					.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
						@Override
						public boolean onQueryTextSubmit(String queryText) {
							// Nothing needs to happen when the user submits the
							// search string
							return true;
						}

						@Override
						public boolean onQueryTextChange(String newText) {
							// Called when the action bar search text has
							// changed. Updates
							// the search filter, and restarts the loader to do
							// a new query
							// using the new search string.
							String newFilter = !TextUtils.isEmpty(newText) ? newText
									: null;

							// Don't do anything if the filter is empty
							if (mSearchTerm == null && newFilter == null) {
								return true;
							}

							// Don't do anything if the new filter is the same
							// as the current filter
							if (mSearchTerm != null
									&& mSearchTerm.equals(newFilter)) {
								return true;
							}

							// Updates current filter to new filter
							mSearchTerm = newFilter;
							UserFragment usersFragment = getUserFragment();
							if (usersFragment != null){
								usersFragment.onSearchQuery(mSearchTerm);
							}
							return true;
						}
					});

			/*if (Utils.hasICS()) {
				// This listener added in ICS
				searchItem
						.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
							@Override
							public boolean onMenuItemActionExpand(
									MenuItem menuItem) {
								// Nothing to do when the action item is
								// expanded
								return true;
							}

							@Override
							public boolean onMenuItemActionCollapse(
									MenuItem menuItem) {
								// When the user collapses the SearchView the
								// current search string is
								// cleared and the loader restarted.
								
								 * if (!TextUtils.isEmpty(mSearchTerm)) {
								 * onSelectionCleared(); }
								 
								
								Toast.makeText(FbTextMainActivity.this, "on Close search view at hasICS", Toast.LENGTH_SHORT).show();
								
								mSearchTerm = null;
								UserFragment usersFragment = getUserFragment();
								if (usersFragment != null)
									usersFragment.onSearchQuery(mSearchTerm);
								return true;
							}
						});
			}*/

			if (mSearchTerm != null) {
				// If search term is already set here then this fragment is
				// being restored from a saved state and the search menu item
				// needs to be expanded and populated again.

				// Stores the search term (as it will be wiped out by
				// onQueryTextChange() when the menu item is expanded).
				final String savedSearchTerm = mSearchTerm;

				// Expands the search menu item
				if (Utils.hasICS()) {
					searchItem.expandActionView();
				}

				// Sets the SearchView to the previous search string
				searchView.setQuery(savedSearchTerm, false);
			}
		}

		// When using the support library, the setOnActionExpandListener()
		// method is
		// static and accepts the MenuItem object as an argument
		MenuItemCompat.setOnActionExpandListener(searchItem,
				new OnActionExpandListener() {
					@Override
					public boolean onMenuItemActionCollapse(MenuItem item) {
						mSearchTerm = null;
						UserFragment usersFragment = getUserFragment();
						if (usersFragment != null)
							usersFragment.onSearchQuery(mSearchTerm);
						return true; // Return true to collapse action view
					}

					@Override
					public boolean onMenuItemActionExpand(MenuItem item) {
						// Do something when expanded
						return true; // Return true to expand action view
					}
				});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			Intent intentsetting = new Intent(FbTextMainActivity.this, SettingsActivity.class);
			startActivityForResult(intentsetting, 0);
			return true;
			
		case R.id.action_log_out :
			stopService(SERVICE_INTENT);
			Editor editor = mSettings.edit();
			editor.putString(FbTextApplication.ACCOUNT_PASSWORD_KEY, "");
			((FbTextApplication)getApplicationContext()).setConnected(false);
			editor.commit();
			// clean all users when user logout
			try {
				((FbTextApplication)getApplicationContext()).getHelper().cleanUsers();
				((FbTextApplication)getApplicationContext()).getHelper().delelteOldMessage();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			finish();
			return true;
		case R.id.action_change_state:
			
			changeStatusFragment = new SelectStateFragment();
			changeStatusFragment.setChanger(new StatusChanger() {
				
				@Override
				public void changeStatus() {
					changeStatusState();
					
				}
			});
			changeStatusFragment.show(getSupportFragmentManager(), "status_change");
			break;
		case R.id.action_share: 
			String message = FbTextApplication.PLAY_LINK;
					Intent share = new Intent(Intent.ACTION_SEND);
					share.setType("text/plain");
					share.putExtra(Intent.EXTRA_TEXT, message);

					startActivity(Intent.createChooser(share, "Share using: "));
			return true;
		case R.id.action_invite_friends:
			showInviationMessageDialog(false);
			return true;
		case R.id.action_rate:
			if (!FbTextApplication.isRatable)
				return true;
			String appPackageName = getPackageName();
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (Exception e) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
			}
			break;
			
		case R.id.action_upgrade_remove_ads:
			if (!FbTextApplication.isAdsRemovable){
				upgrade_remove_ads();
				return true;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(FbTextMainActivity.this);
			builder.setMessage(R.string.remove_ads_prompt_message)
					.setPositiveButton(R.string.invite_friends_to_remove_ads, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showInviationMessageDialog(true);
						}
					})
					.setNeutralButton(R.string.upgrade_to_remove_ads, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							upgrade_remove_ads();
						}
					});
			builder.show();
			break;
			
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	
	public void showInviationMessageDialog (final boolean isForAds) {
		AlertDialog.Builder builder = new AlertDialog.Builder(FbTextMainActivity.this);
		 final EditText input = new EditText(FbTextMainActivity.this);  
		 input.setHint(R.string.invitaiton_hint_message);
		  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		                        LinearLayout.LayoutParams.MATCH_PARENT,
		                        LinearLayout.LayoutParams.MATCH_PARENT);
		  input.setLayoutParams(lp);
		  builder.setView(input);
		  builder.setTitle(R.string.invite_friends_to_remove_ads)
		  		.setPositiveButton(R.string.invite, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(FbTextApplication.SEND_INVITATION);
						if (input.getText() != null)
							intent.putExtra(FbTextApplication.CUSTOME_INVITATION_MESSAGE_KEY, 
										input.getText().toString());
						intent.putExtra(FbTextApplication.INVITATION_FOR_ADS_KEY, isForAds);
						FbTextMainActivity.this.sendBroadcast(intent);
					}
				})
		  		.setNegativeButton(android.R.string.cancel, null);
		  builder.show();
	}
	
	
	
	
	
	@Override
	public void onBackPressed() {
		/*if (searchView != null && !searchView.isIconified())
			searchView.setIconified(true);
		else*/
			super.onBackPressed();
	}
	
	
/*	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    // Checks the orientation o	f the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	    }
	}
	*/
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int clickTimes = mSettings.getInt(FbTextApplication.CLICK_CHAT_ACTIVITY_COUNT_KEY, 0);
		if (FbTextApplication.isDebug)
			Log.v(LOG_TAG, "click times: " + clickTimes + "   isAlreadyRate: " + mSettings.getBoolean(FbTextApplication.USER_ALREADY_RATE_KEY, false) );
		if (START_CHAT_REQUEST_CODE == requestCode && !FbTextApplication.isDisconnected){
			UserFragment usersFragment = getUserFragment();
			ChatFragment chatsFragment = getChatFragment();
			if (usersFragment != null & clickTimes % 3 == 0)
				usersFragment.reloadListUser();
			if (chatsFragment != null)
				chatsFragment.refreshChatList();
			
			
			if (!mSettings.getBoolean(FbTextApplication.USER_ALREADY_RATE_KEY, false) 
					&& (clickTimes % 16 == 6)){
				isShowFeedBack = true;
        	}
        	if (mInterstitialAd != null && clickTimes > 6 && clickTimes % 10 == 6 && mInterstitialAd.isLoaded()){
        		mInterstitialAd.show();
        	}
            return;
		}
		else if (UPGRADE_REMOVEADS_INAPP_REQUEST == requestCode){
			int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
		      String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
		      String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
		        
		      if (resultCode == RESULT_OK) {
		         try {
		            JSONObject jo = new JSONObject(purchaseData);
		            String sku = jo.getString("productId");
		            if (sku != null && FbTextApplication.UPGRADE_REMOVE_ADS_SKU_ID.equals(sku)){
			            displayDialogMessage(getString(R.string.thank_support_message));
			            mSettings.edit().putBoolean(FbTextApplication
			            			.UPGRAGE_REMOVE_ADS_KEY, true).apply();
			            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.adFragment);
			            if (fragment != null)
			            	getSupportFragmentManager().beginTransaction().hide(fragment).commit();
		            }
		          }
		          catch (JSONException e) {
		             e.printStackTrace();
		          }
		      }
		}
		
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	
	/*
	 * private void automaticBindBackgroundService() { if
	 * (BackgroundMessageService.isRunning()) { doBindBackgroundService(); }
	 * else{
	 * 
	 * startService(new Intent(FbTextMainActivity.this,
	 * BackgroundMessageService.class)); doBindBackgroundService(); } }
	 */

	/**
	 * The service connection used to connect to the Beem service.
	 */
	private class FacebookTextServiceConnection implements ServiceConnection {

		/**
		 * Constructor.
		 */
		public FacebookTextServiceConnection() {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (FbTextApplication.isDebug)
			Log.d(LOG_TAG, "on connected to FacebookTextService - > get Roster" + "\tloading time: " + loadingTime);
			mXmppFacade = IXmppFacade.Stub.asInterface(service);
			try {
				mRoster = mXmppFacade.getRoster();
				if (mRoster != null && (loadingTime % 3 == 0) && !isSavingData) {
					Log.e(LOG_TAG, "start saving contacts");
					UpdateContactsDatabase updateContactTask = new UpdateContactsDatabase();
					Utils.executeAsyncTask(updateContactTask, mRoster.getContactList());
				}
				loadingTime = loadingTime +1;
				if (mRoster != null && mXmppFacade != null){
					isConnected = true;
					mRoster.addRosterListener(mBeemRosterListener);
					mChatManager = mXmppFacade.getChatManager();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			try {
				mRoster.removeRosterListener(mBeemRosterListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mXmppFacade = null;
			mChatManager = null;
			mRoster = null;
			// mListGroup.clear();
			// mContactOnGroup.clear();
			mBinded = false;

		}


	}

	/**
	 * Listener on service event.
	 */
	private class BeemRosterListener extends IBeemRosterListener.Stub {
		/**
		 * Constructor.
		 */
		public BeemRosterListener() {
		}

		/**
		 * {@inheritDoc} Simple stategy to handle the onEntriesAdded event. if
		 * contact has to be shown :
		 * <ul>
		 * <li>add him to his groups</li>
		 * <li>add him to the specials groups</>
		 * </ul>
		 */
		@Override
		public void onEntriesAdded(final List<String> addresses)
				throws RemoteException {
			if (FbTextApplication.isDebug)
			Log.d(LOG_TAG,
					"RosterListener on Entries added " + addresses.size()
							+ " addresses");
			
			/*AddingContactsDatabaseTask addingTask = new AddingContactsDatabaseTask();
			if (addresses != null && addresses.size() > 0)
				Utils.executeAsyncTask(addingTask, addresses);*/
		}

		/**
		 * {@inheritDoc} Simple stategy to handle the onEntriesDeleted event.
		 * <ul>
		 * <li>Remove the contact from all groups</li>
		 * </ul>
		 */
		@Override
		public void onEntriesDeleted(final List<String> addresses)
				throws RemoteException {
			if (FbTextApplication.isDebug)
			Log.d(LOG_TAG, "onEntries deleted " + addresses);
//			ArrayList<User> deletedList = new ArrayList<User>();
//			for (String cToDelete : addresses) {
//				final Contact contact = new Contact(cToDelete);
//				User deletedUser = new User(contact.getJID(),
//						contact.getName(), null);
//				deletedList.add(deletedUser);
//			}
//			usersFragment.onFriendEntryDeleted(deletedList);

		}

		/**
		 * {@inheritDoc} Simple stategy to handle the onEntriesUpdated event.
		 * <ul>
		 * <li>Remove the contact from all groups</li>
		 * <li>if contact has to be shown add it to his groups</li>
		 * <li>if contact has to be shown add it to the specials groups</li>
		 * </ul>
		 */
		@Override
		public void onEntriesUpdated(final List<String> addresses)
				throws RemoteException {
			if (FbTextApplication.isDebug)
			Log.d(LOG_TAG, "onEntries updated " + addresses.size()
					+ " addresses");
		}

		/**
		 * {@inheritDoc} Simple stategy to handle the onPresenceChanged event.
		 * <ul>
		 * <li>Remove the contact from all groups</li>
		 * <li>if contact has to be shown add it to his groups</li>
		 * <li>if contact has to be shown add it to the specials groups</li>
		 * </ul>
		 */
		@Override
		public void onPresenceChanged(PresenceAdapter presence)
				throws RemoteException {

			String from = presence.getFrom();
			final Contact contact = mRoster.getContact(StringUtils
					.parseBareAddress(from));
			/*usersFragment.onUserChangeState(contact.getJID(),
					presence.getType() == PresenceType.AVAILABLE ? 1 : 0);*/
			updateUserState(StringUtils
					.parseBareAddress(from), presence.getType() == PresenceType.AVAILABLE ? 1 : 0);
			if (FbTextApplication.isDebug)
			Log.d(LOG_TAG, "On presence changed " + contact.getJID());

		}
		
		

	}
	
	
	/*
	 * Update user database when activity bind to service and get list users
	 * 
	 * May be this one should be runned on the first time after user login
	 * 
	 * 
	 * */
	
	public class UpdateContactsDatabase extends AsyncTask<List<Contact>, List<Contact>, Boolean> {
		private boolean saySorry = false;
		
		@Override
		protected void onPreExecute() {
			isSavingData = true;
			UserFragment usersFragment = getUserFragment();
			if ((loadingTime == 0 || loadingTime == 1) && usersFragment != null){
				usersFragment.diplayProgressBar();
				usersFragment.reloadListUser();
			}
			
			super.onPreExecute();
		}
		@Override
		protected void onProgressUpdate(List<Contact>... values) {
			UserFragment usersFragment = getUserFragment();
			if (usersFragment != null && values == null) {
				usersFragment.diplayProgressBar();
				usersFragment.reloadListUser();
			}
			else if (usersFragment != null && values != null && values.length >0)
				usersFragment.onFirstTime(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected Boolean doInBackground(List<Contact>... params) {
			List<Contact> contacts = params[0];
			int update_threshold = 200;
			if (contacts != null && contacts.size() < 300)
				update_threshold = 50;
			if (contacts != null && contacts.size() >300)
				FbTextApplication.isAdsRemovable = true;
			else 
				FbTextApplication.isAdsRemovable = false;
			if (FbTextApplication.isDebug)
				Log.v(LOG_TAG, "saving " + contacts.size() + " contacts in database");
			if (contacts.size() < 1){
				onDisconnect();
				FbTextApplication.isRatable = false;
			}
			
			try {
				if (userDao == null)
					userDao = ((FbTextApplication)getApplicationContext()).getHelper().getUserDao();
				if (userDao != null && userDao.countOf() < 1){
					loadingTime = 0;
					if (contacts.size() < 1 && Utils.isConnectToInternet(getApplicationContext())){
						saySorry = true;
						mSettings.edit().putBoolean(FbTextApplication
		            			.UPGRAGE_REMOVE_ADS_KEY, true).apply();
					}
				}
				if (loadingTime == 0){
					Log.e("first time update ", "loading time in first: " + loadingTime);
					publishProgress(params);
				}
				int count = 0;
				for (Contact c : contacts) {
					// I do update instead
					
					
					
					if (count++ % update_threshold == 0 && (loadingTime == 1)){
						publishProgress();
					}
					
					User newUser = new User(c.getJID(), c.getName(), "");
					if (c.getStatus() == co.beem.project.beem.utils.Status.CONTACT_STATUS_AVAILABLE
							|| c.getStatus() == co.beem.project.beem.utils.Status.CONTACT_STATUS_AVAILABLE_FOR_CHAT) {
						newUser.setState(UserState.available);
					} else {
						newUser.setState(UserState.unavailable);
					}
					try {
						UpdateBuilder<User, String> updateBuilder = userDao.updateBuilder();
						updateBuilder.updateColumnValue(User.USER_NAME_FIELD, newUser.getName());
						updateBuilder.updateColumnValue(User.USER_STATE_FIELD, newUser.getState());
						updateBuilder.where().eq(User.USER_JABBER_ID, newUser.getJabberId());
						User oldUser = userDao.queryForId(newUser.getJabberId());
						if (oldUser != null && oldUser.isFavorite() && oldUser.getState() == UserState.unavailable 
									&& newUser.getState() == UserState.available)
							pushnotificationUserOnline(oldUser);
						
						int rowAffted = 
								userDao.update(updateBuilder.prepare());
						if (rowAffted == 0){
							userDao.create(newUser);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			isSavingData = false;
			UserFragment usersFragment = getUserFragment();
			if (result && usersFragment!= null){
				loadingTime  ++;
				//sessionManager.setLoadingAvatar(true);
				usersFragment.reloadListUser();
				sessionManager.setLoadAllContacts(false);
			}
			if (saySorry && isRunning) {
				Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.adFragment);
	            if (fragment != null)
	            	getSupportFragmentManager().beginTransaction().hide(fragment).commit();
				AlertDialog.Builder builder = new Builder(FbTextMainActivity.this);
				builder.setTitle(R.string.sorry_ttile)
					.setMessage(R.string.sorry_no_contact)
					.setPositiveButton(R.string.sorry_okay, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				builder.show();
			}
			supportInvalidateOptionsMenu();
			//
			// Get intent, action and MIME type
		    if (FbTextApplication.sharedIntent != null){
			    String action = FbTextApplication.sharedIntent.getAction();
			    String type = FbTextApplication.sharedIntent.getType();
			    	Toast.makeText(getApplicationContext(), R.string.share_intent_toast_message, Toast.LENGTH_LONG).show();
		    }
			super.onPostExecute(result);
		}
		
		
	}
	
	public void putUserToGetAvatar (String jId){
		Intent intent = new Intent();
		 intent.putExtra(User.USER_JABBER_ID, jId);
		 intent.setAction(FbTextApplication.GET_AVATAR);
		 sendBroadcast(intent); 
	}
	
	public void updateUserState (String jId, int state){
		Intent intent = new Intent();
		 intent.putExtra(User.USER_JABBER_ID, jId);
		 intent.putExtra(User.USER_STATE_FIELD, state);
		 intent.setAction(FbTextApplication.UPDATE_USER_STATE);
		 sendBroadcast(intent); 
	}
	
	public void pushnotificationUserOnline (final User user){
		try{
		Intent intent = new Intent();
		 intent.putExtra(User.USER_JABBER_ID, user.getJabberId());
		 intent.putExtra(User.USER_NAME_FIELD, user.getName());
		 intent.setAction(FbTextApplication.PUSH_NOTIFICATION_FAVORITE_ONLINE);
		 sendBroadcast(intent); 
	}catch (Exception e){
		e.printStackTrace();
	}
	}
	
	public void changeStatusNotification (int status){
		try{
		Intent intent = new Intent();
		 intent.setAction(FbTextApplication.CHANGE_STATUS);
		 sendBroadcast(intent);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public static class SelectStateFragment extends DialogFragment {
		static StatusChanger statusChanger;
		public SelectStateFragment (){
			super();
		}
		public void setChanger (StatusChanger statusChanger){
			SelectStateFragment.statusChanger = statusChanger;
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
		   
		    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    // Set the dialog title
		    builder.setTitle(R.string.change_status_title)
		    // Specify the list array, the items to be selected by default (null for none),
		    // and the listener through which to receive callbacks when items are selected
		    	.setSingleChoiceItems(R.array.status_array, isOnline ? 0 : 1, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedState = which;
					}
				})
				
		    // Set the action buttons
		           .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		            	   statusChanger.changeStatus();
		            	   dialog.dismiss();
		               }
		           })
		           .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		            	   dialog.dismiss();
		               }
		           });

		    return builder.create();
		}
	}
	
	public interface StatusChanger {
		public void changeStatus ();
	}
	
	public void changeStatusState() {

		if (!isConnected)
			return;
		isOnline = (selectedState == 0);
		int status = getStatusForService(selectedState == 1 ? 4 : 0);
		Editor edit = mSettings.edit();
		try {
			if (mXmppFacade != null)
			mXmppFacade.changeStatus(status, "status");
			edit.putInt(FbTextApplication.STATUS_KEY,
					selectedState == 1 ? 4 : 0);
			changeStatusNotification(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		edit.commit();
		supportInvalidateOptionsMenu();
	}
	
	
	
	   /**
     * convert status text to.
     * @param item selected item text.
     * @return item position in the array.
     */
    private int getStatusForService(int item) {
	int result;
	switch (item) {

	    case 0:
		result = Status.CONTACT_STATUS_AVAILABLE;
		
		break;

	    case 4:
		result = Status.CONTACT_STATUS_UNAVAILABLE;

		break;
	    default:
		result = Status.CONTACT_STATUS_AVAILABLE;
	
		break;
	}
	return result;
	
	
    }
    
    
    
    

	@Override
	public void onConnected() {
		isConnected = true;
		UserFragment usersFragment = getUserFragment();
		ChatFragment chatsFragment = getChatFragment();
		if (usersFragment !=null)
			usersFragment.onConnected();
		
		if (chatsFragment !=null)
			chatsFragment.onConnected();
/*		Intent intent = null;
		if (!mBinded)
			intent = new Intent(this, FacebookTextService.class);*/
		// startService(intent);
		mBinded = bindService(SERVICE_INTENT, mServConn, BIND_AUTO_CREATE);
		if (FbTextApplication.isDebug)
		Log.v(LOG_TAG, "on connected again :D");
	}

	@Override
	public void onDisconnect() {
		Log.v(LOG_TAG, "on disconnect");
		try {
			if (mRoster != null) {
				mRoster.removeRosterListener(mBeemRosterListener);
				mRoster = null;
			}
		} catch (RemoteException e) {
			Log.d(LOG_TAG, "Remote exception", e);
		}
		if (mBinded) {
			unbindService(mServConn);
			mBinded = false;
		}
		mXmppFacade = null;
		
		
		isConnected = false;
		// display
		UserFragment usersFragment = getUserFragment();
		ChatFragment chatsFragment = getChatFragment();
		if (usersFragment !=null)
		usersFragment.onDisconnected();
		if (chatsFragment !=null)
			chatsFragment.onDisconnected();
		
	}

	@Override
	public void onConnecting() {
		isConnected = false;
		UserFragment usersFragment = getUserFragment();
		ChatFragment chatsFragment = getChatFragment();
		if (usersFragment !=null)
		usersFragment.onConnecting();
		
		if (chatsFragment !=null)
			chatsFragment.onConnecting();
		
		if (FbTextApplication.isDebug)
		Log.v(LOG_TAG, "on connecting");
	}

	@Override
	public void onNoInternetConnection() {
		isConnected = false;
		UserFragment usersFragment = getUserFragment();
		ChatFragment chatsFragment = getChatFragment();
		if (usersFragment !=null)
		usersFragment.onNoInternetConnection();
		
		if (chatsFragment !=null)
			chatsFragment.onNoInternetConnection();
		
		if (FbTextApplication.isDebug)
		Log.v(LOG_TAG, "on no internet connection");
		
		try {
			if (mRoster != null) {
				mRoster.removeRosterListener(mBeemRosterListener);
				mRoster = null;
			}
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
			Log.d(LOG_TAG, "Remote exception", e);
		}
		if (mBinded) {
			unbindService(mServConn);
			mBinded = false;
		}
		mXmppFacade = null;
	}

	@Override
	public void onConnectionError() {
		isConnected = false;
		// display
		UserFragment usersFragment = getUserFragment();
		if (usersFragment !=null)
		usersFragment.onDisconnected();
		if (FbTextApplication.isDebug)
		Log.v(LOG_TAG, "onConnectionError");
		
		
		try {
			if (mRoster != null) {
				mRoster.removeRosterListener(mBeemRosterListener);
				mRoster = null;
			}
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
			Log.d(LOG_TAG, "Remote exception", e);
		}
		if (mBinded) {
			unbindService(mServConn);
			mBinded = false;
		}
		mXmppFacade = null;
	}

	
	
	public void onNewMessageArrived (){
		final ChatFragment chatsFragment = getChatFragment();
		if (chatsFragment != null)
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					chatsFragment.refreshChatList();
				}
			}, 50);
			
	}

	@Override
	public void onConnectingIn(int seconds) {
		isConnectingIn = true;
		this.onConnecting();
		connectingInSeconds = seconds;
		if (FbTextApplication.isDebug)
			Log.v(LOG_TAG, "connecting in :  " + seconds);
	}
    
	
	/*
	 * in app purchase
	 * 
	 * */ 

	//method to support developer
	public void upgrade_remove_ads (){
		if (mSettings.getBoolean(FbTextApplication.UPGRAGE_REMOVE_ADS_KEY, false)){
			 Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.adFragment);
	            if (fragment != null)
	    		getSupportFragmentManager().beginTransaction().hide(fragment).commit();
	            return;
		}
		String payload = randomPayloadString();
		try {
				
			Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
						FbTextApplication.UPGRADE_REMOVE_ADS_SKU_ID, "inapp", payload);
			
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			startIntentSenderForResult(pendingIntent.getIntentSender(),
					   UPGRADE_REMOVEADS_INAPP_REQUEST, new Intent(), 
					   Integer.valueOf(0), Integer.valueOf(0),
					   Integer.valueOf(0));
			
			
			
		} catch (RemoteException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), R.string.cannot_send_request,
							Toast.LENGTH_SHORT).show();
		} catch (SendIntentException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), R.string.cannot_send_request,
					Toast.LENGTH_SHORT).show();
		}catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), R.string.cannot_send_request,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public void onSendInvitationFailed () {
		AlertDialog.Builder builder = new AlertDialog.Builder(FbTextMainActivity.this);
		builder.setTitle(R.string.invite_friends_to_remove_ads)
			.setMessage(R.string.invite_friend_failed)
			.setPositiveButton(android.R.string.ok, null);
		builder.show();
	}
	
	public void onSendInvitationSuccess() {
		 Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.adFragment);
         if (fragment != null)
        	 getSupportFragmentManager().beginTransaction().hide(fragment).commit();
         return;
	}
	
	
	public class GetPurchasedTask extends AsyncTask<Void, String, Boolean> {

		@Override
		protected void onProgressUpdate(String ... values) {
			Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT).show();
			super.onProgressUpdate(values);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				if (mService == null)
					Thread.sleep(5000);
				Bundle ownedItems = mService.getPurchases(3, getPackageName(),
						"inapp", null);

				int response = ownedItems.getInt("RESPONSE_CODE");
				if (response == 0) {
					ArrayList<String> ownedSkus = ownedItems
							.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
					ArrayList<String> purchaseDataList = ownedItems
							.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
					if (purchaseDataList.size() == 0) {
						SharedPreferences.Editor editor = mSettings.edit();
						editor.putBoolean(FbTextApplication.UPGRAGE_REMOVE_ADS_KEY, false);
						editor.apply();
					}
					else
					for (int i = 0; i < purchaseDataList.size(); ++i) {
						//String purchaseData = purchaseDataList.get(i);
						String sku = ownedSkus.get(i);
						if (FbTextApplication.UPGRADE_REMOVE_ADS_SKU_ID.equals(sku)){
							SharedPreferences.Editor editor = mSettings.edit();
							editor.putBoolean(FbTextApplication.UPGRAGE_REMOVE_ADS_KEY, true);
							editor.apply();
							
						}
							
						// do something with this purchase information
						// e.g. display the updated list of products owned by
						// user
						
					}

					// if continuationToken != null, call getPurchases again
					// and pass in the token to retrieve more items
				}

			} catch (RemoteException e) {
				e.printStackTrace();
				return false;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result)
				supportInvalidateOptionsMenu();
			super.onPostExecute(result);
		}
		
	}
	public static String randomPayloadString (){
		Random rng = new Random();
		String characters = "qwertyuiopasdfghjklzxcvbnm";
		char[] text = new char[15];
	    for (int i = 0; i < 15; i++)
	    {
	        text[i] = characters.charAt(rng.nextInt(characters.length()));
	    }
	    return new String(text);
		
		
	}
	
	public void displayDialogMessage (String message){
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getApplicationContext());
 
			// set title
			alertDialogBuilder.setTitle(R.string.app_name);
			
			// set dialog message
						alertDialogBuilder
							.setMessage(message)
							.setCancelable(false)
							.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									dialog.dismiss();
								}
							  });
			 
							// create alert dialog
							alertDialog = alertDialogBuilder.create();
			 
							// show it
							alertDialog.show();
		
	}
	
	public void handleDisconnectAndLoginAgainFaild(){
		//String password = mSettings.getString(FbTextApplication.ACCOUNT_PASSWORD_KEY, "");
		//boolean mIsAccountConfigured = !TextUtils.isEmpty((password));
		if(FbTextApplication.isDebug)
			Log.v(LOG_TAG, "Fbt application isDisconnected: " + FbTextApplication.isDisconnected);
		if (FbTextApplication.isDisconnected){
			Intent intent = new Intent(FbTextMainActivity.this, Login.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			if(FbTextApplication.isDebug)
				Log.v(LOG_TAG, "application disconnected and we finished: " );
			finish();
		}
			
		
	}
	
	
	
	
	/*
	 * For ads
	 * 
	 * */
	
	public static class AdFragment extends Fragment {
		AdView mAdView;
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                             Bundle savedInstanceState) {
	    	if (getActivity() != null) {
	    		FbTextApplication application = (FbTextApplication) getActivity().getApplication();	
	    		if (application.isUpgraged())
	    			return inflater.inflate(R.layout.fragment_transperant_ads, container, false);
	    	}
	        return inflater.inflate(R.layout.fragment_banner_ads_on_mainac, container, false);
	    }

	    @Override
	    public void onActivityCreated(Bundle bundle) {
	        super.onActivityCreated(bundle);
	        if (getActivity() != null) {
	    		FbTextApplication application = (FbTextApplication) getActivity().getApplication();	
	    		if (application.isUpgraged())
	    			return;
	    	}
	        mAdView = (AdView) getView().findViewById(R.id.adView);
	        AdRequest adRequest = new AdRequest.Builder().build();
	        if (mAdView != null)
	        mAdView.loadAd(adRequest);
	    }
	    
	    @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        //** Called when returning to the activity *//*
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        //** Called before the activity is destroyed *//*
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }
	    
	    
	    
	}
	
	
	 public static class FeedbackDialog extends DialogFragment{
		 private Activity activity;
		 private SharedPreferences settings;
	    	@Override
	    	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    		this.activity = getActivity();
	    		settings = PreferenceManager.getDefaultSharedPreferences(FeedbackDialog.this.activity);
	    		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    		builder.setTitle(R.string.feed_back_title);
	    		LinearLayout feedbackLayout = (LinearLayout) LayoutInflater
	    						.from(getActivity())
	    						.inflate(R.layout.rating_dialog_layout, null);
	    		feedbackLayout.findViewById(R.id.rating_layout_good).setOnClickListener(feedbackSelectionListener);
	    		feedbackLayout.findViewById(R.id.rating_layout_later).setOnClickListener(feedbackSelectionListener);
	    		feedbackLayout.findViewById(R.id.rating_layout_not_good).setOnClickListener(feedbackSelectionListener);
	    		builder.setView(feedbackLayout);
	    		
	    		return builder.create();
	    	}
	    	
	    	View.OnClickListener feedbackSelectionListener = new View.OnClickListener() {
	    		 
				@Override
				public void onClick(View v) {
					int id = v.getId();
					if (id == R.id.rating_layout_good){
						settings.edit().putBoolean(FbTextApplication.USER_ALREADY_RATE_KEY, true).commit();
						final String appPackageName = FeedbackDialog.this.activity.getPackageName(); 
						try {
						    startActivity(new Intent(Intent.ACTION_VIEW, 
						    		Uri.parse("market://details?id=" + appPackageName)));
						} catch (android.content.ActivityNotFoundException anfe) {
						    startActivity(new Intent(Intent.ACTION_VIEW, 
						    		Uri.parse("http://play.google.com/store/apps/details?id=" 
						    								+ appPackageName)));
						    
						}
					}else if (id == R.id.rating_layout_not_good){
						settings.edit().putBoolean(FbTextApplication.USER_ALREADY_RATE_KEY, true).commit();
						((NotGoodFeedBackListener)FeedbackDialog.this.activity).onNotGoodSelected();
					}else if (id == R.id.rating_layout_later){
					}
					dismiss();
				}
			};
	    }

	@Override
	public void onNotGoodSelected() {
		askFeedBackDialog = new AskFeedBackDialog();
		askFeedBackDialog.show(getSupportFragmentManager(), "ask_feedback_dialog");
		
	}
	
	
	private class AskFeedBackDialog extends DialogFragment {
		
		public AskFeedBackDialog () {
			
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Please tell us your feedback")
					.setPositiveButton(R.string.OkButton, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
						            "mailto",FbTextApplication.DEVELOPER_EMAIL, null));
							emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Fast Messenger");
							emailIntent.putExtra(Intent.EXTRA_TEXT, "My feedback: ");
							startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
						}
					})
					.setNegativeButton(android.R.string.no, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					});
			
			return builder.create();
		}
	}
	

}
