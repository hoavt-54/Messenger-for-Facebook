package hoahong.facebook.messenger.ui.android;

import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.ui.Utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;

public class SessionManager {
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;
	
	private static final String PREF_NAME = "hoahong.facebook.messenger";
	private static final String USER_NAME = "account_balance";
	private static final String START_LOADING_AVATAR = "start_load_avatar";
	private static final String BACKGROUND_ID = "background_id";
	private static final String BACKGROUND_PATH_ID = "background_path_id";
	private static final String LOAD_ALL_CONTACTS = "create_all_contacts";
	private static final String ORIENTATION_CHANGE = "orientation_change";
	
	 // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
	
    public void saveAccountBalance (String username){
    	editor.putString(USER_NAME, username);
    	editor.commit();
    }
	public String getAccountBalance (){
		return pref.getString(USER_NAME, "");
	}
	
	public void setLoadingAvatar (boolean loadAvatar){
		editor.putBoolean(START_LOADING_AVATAR, loadAvatar);
		editor.commit();
	}
	public boolean checkLoaddingAvatar (){
		return pref.getBoolean(START_LOADING_AVATAR, false);
	}
	
	
	
	
	
	
	public void setBackgroundId (int id){
		editor.putInt(BACKGROUND_ID, id);
		editor.commit();
	}
	
	public void setBackgroundId (int id, String userId){
		editor.putInt(BACKGROUND_ID + userId, id);
		editor.commit();
	}
	
	public int getBackGroundId (){
		return pref.getInt(BACKGROUND_ID, R.drawable.background_hd);
	}
	
	public int getBackGroundId (String userId){
		int result = pref.getInt(BACKGROUND_ID + userId, 0);
		if (result != 0)
			return result;
		return pref.getInt(BACKGROUND_ID, R.drawable.background_hd);
	}


	public String getBackgroundPath () {
		return pref.getString(BACKGROUND_PATH_ID, null);
	}
	
	public String getBackgroundPath (String userIdKey) {
		String path = pref.getString(BACKGROUND_PATH_ID + userIdKey, null);
		if (!Utils.isEmpty(path)) return path;
		if (pref.getInt(BACKGROUND_ID + userIdKey, 0) != 0)
			return null;
		return pref.getString(BACKGROUND_PATH_ID, null);
	}
	
	public void setBackgroundPath (String path) {
		editor.putString(BACKGROUND_PATH_ID, path).commit();
	}
	
	public void setBackgroundPath (String path, String userId) {
		editor.putString(BACKGROUND_PATH_ID+userId, path).commit();
	}
	
	
	
	public void setLoadAllContacts(boolean loadContact){
		editor.putBoolean(LOAD_ALL_CONTACTS, loadContact);
		editor.commit();
	}
	
	public boolean isLdoadAllContacts(){
		return pref.getBoolean(LOAD_ALL_CONTACTS, true);
	}
	
	public void setCurrentOrientation(int currentOrientaion){
		editor.putInt(ORIENTATION_CHANGE, currentOrientaion);
		editor.apply();
	}
	
	public boolean isOrientationChange(int currentOrientaion){
		//get last orientation and compares with current orientaion
		return (pref.getInt(ORIENTATION_CHANGE, Configuration.ORIENTATION_PORTRAIT) == currentOrientaion);
	}
}
