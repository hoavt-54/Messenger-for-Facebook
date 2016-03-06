package hoahong.facebook.messenger.ui.android;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.vending.billing.IInAppBillingService;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.ui.Utils;


public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	public static final int UPGRADE_REMOVEADS_INAPP_REQUEST = 2;
	private static final boolean ALWAYS_SIMPLE_PREFS = true;
	public static final String LOG_TAG = "SettingsActivity";
	private SharedPreferences mSettings;
	
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
	   }
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.setting_layout);
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		super.onCreate(savedInstanceState);
		// register billing transaction with google Play
 		Intent serviceIntent = new Intent(
 				"com.android.vending.billing.InAppBillingService.BIND");
 		serviceIntent.setPackage("com.android.vending");
 		bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		ListView lv = (ListView) findViewById(android.R.id.list);
		lv.setDivider(new ColorDrawable(this.getResources().getColor(R.color.theme_color)));;
		lv.setDividerHeight(1);
		
		setupSimplePreferencesScreen();
		
		
		LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Add 'notifications' preferences, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_notifications);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_notification);

		Preference changeBackgroundPreference = findPreference("pref_change_background");
		changeBackgroundPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(SettingsActivity.this, ChangeChatBackGroundActivity.class);
				startActivityForResult(intent, 1);
				return true;
			}
		});
		
		Preference upgrade = findPreference(FbTextApplication.UPGRAGE_REMOVE_ADS_KEY);
		upgrade.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {

				if (mSettings.getBoolean(FbTextApplication.UPGRAGE_REMOVE_ADS_KEY, false)){
					Toast.makeText(SettingsActivity.this, getString(R.string.upgraded_notification), Toast.LENGTH_SHORT).show();
			            return true;
				}
				if (mService == null) return true;
				String payload = FbTextMainActivity.randomPayloadString();
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
			
				return true;
			}
		});
		
		// handle change ringtone
		Preference changeRingtone = findPreference(FbTextApplication.CHANGE_RINGTONE_PREF_KEY);
		changeRingtone.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
	            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
	            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Notification Sounds");
	            String currentUri = mSettings.getString(FbTextApplication.CHANGE_RINGTONE_PREF_KEY, null);
	            if (currentUri != null)
	            	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentUri));
	            SettingsActivity.this.startActivityForResult(intent, 5);
	            return true;
			}
		});
		
		
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("font_size"));
//		bindPreferenceSummaryToValue(findPreference("example_list"));
		//bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}


	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				
				Utils.updateTextLevel();
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// unreagist with google play billing
	 	if (mService != null) {
		        unbindService(mServiceConn);
	    } 
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (UPGRADE_REMOVEADS_INAPP_REQUEST == requestCode){
			int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
		      String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
		      String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
		        
		      if (resultCode == RESULT_OK) {
		         try {
		            JSONObject jo = new JSONObject(purchaseData);
		            String sku = jo.getString("productId");
		            if (sku != null && FbTextApplication.UPGRADE_REMOVE_ADS_SKU_ID.equals(sku)){
			            Toast.makeText(SettingsActivity.this, getString(R.string.thank_support_message), Toast.LENGTH_LONG).show();
			            mSettings.edit().putBoolean(FbTextApplication
		            			.UPGRAGE_REMOVE_ADS_KEY, true).apply();
		            }
		          }
		          catch (JSONException e) {
		             e.printStackTrace();
		          }
		      }
		}else if (5 == requestCode && resultCode == RESULT_OK){
			Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
		    if (uri != null) {
			    String ringTonePath = uri.toString();
				mSettings.edit().putString(FbTextApplication.CHANGE_RINGTONE_PREF_KEY, ringTonePath).commit();
		    }
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
