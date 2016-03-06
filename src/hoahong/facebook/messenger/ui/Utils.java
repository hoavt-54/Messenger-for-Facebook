
package hoahong.facebook.messenger.ui;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.data.ChatMessage;

public class Utils {
	public static float density = 1;
	public static final String	SQUARE	= "square";
	public static final String	SMALL	= "small";
	public static final String	NORMAL	= "normal";
	public static final String	LARGE	= "large";
    static {
    	try{
        density = FbTextApplication.context.getResources().getDisplayMetrics().density;
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    }
	
	public static float fontSizeLevel = 1;
	private static boolean isTablet;
	public static volatile Handler applicationHandler = null;
	public static Point displaySize = new Point();
	public static int statusBarHeight = 0;
    public static <P, T extends AsyncTask<P, ?, ?>> void executeAsyncTask(T task) {
        executeAsyncTask(task, (P[]) null);
    }

    @SuppressLint("NewApi")
    public static <P, T extends AsyncTask<P, ?, ?>> void executeAsyncTask(T task, P... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            task.execute(params);
        }
    }
    public static void RunOnUIThread(Runnable runnable) {
        RunOnUIThread(runnable, 0);
    }
    
    public static int textSizeSp (int sp){
    	return (int) (dp(sp) * fontSizeLevel);
    }
    
    public static void updateTextLevel (){
    	if (applicationContext == null) return;
    	String levelString = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("font_size", "1");
    	Log.v(LOG_TAG, "font size level: " + levelString);
    	fontSizeLevel = Float.valueOf(levelString);
    }

    public static void RunOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            applicationHandler.post(runnable);
        } else {
            applicationHandler.postDelayed(runnable, delay);
        }
    }
    public static void setContext (Context context) {
    	Utils.applicationContext = context;
    	applicationHandler = new Handler(applicationContext.getMainLooper());
    	checkDisplaySize();
    	updateTextLevel();
    }
    public static int dp(int value) {
        return (int)(Math.max(1, density * value));
    }
    public static int dpf(float value) {
        return (int)Math.ceil(density * value);
    }
    static final String LOG_TAG = "PullToRefresh";

	public static void warnDeprecation(String depreacted, String replacement) {
		Log.w(LOG_TAG, "You're using the deprecated " + depreacted + " attr, please switch over to " + replacement);
	}
	public static int fontSize = Utils.dp(16);
	
	  /*static {
	        density = ApplicationLoader.applicationContext.getResources().getDisplayMetrics().density;
	        checkDisplaySize();
	    }*/
	
	public static void setDensity (float density){
		Utils.density = density;
	}
	public native static void loadBitmap(String path, int[] bitmap, int scale, int format, int width, int height);
	/*public static void loadBitmap(String path, int[] bitmap, int scale, int format, BitmapFactory.Options option){
		
		option.inJustDecodeBounds = false;
		Bitmap bmp =  BitmapFactory.decodeFile(path, option);
		
		 bmp.getPixels(bitmap, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight()); 
		 
		 for (int i=0; i < bitmap.length; i++)  
	        {  
	            if(true)
	            {  
	            	bitmap[i] =  0x00000000;  
	            }  
	        } 
	}*/
	
	  /* public static boolean isTablet() {
	        if (isTablet == null) {
	            isTablet = ApplicationLoader.applicationContext.getResources().getBoolean(R.bool.isTablet);
	        }
	        return isTablet;
	    }*/
	
	public static boolean isTablet(){
		 return isTablet;
	}
	
	public static void setStatusBarHight (int height){
		statusBarHeight = height;
	}
	
	public static boolean isEmpty (String s) {
		return s == null || s.length() < 1;
	} 
	
    @SuppressLint("NewApi") 
    public static void checkDisplaySize() {
        try {
            WindowManager manager = (WindowManager)applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    if(android.os.Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                    Log.e("Facebook text", "display size = " + displaySize.x + " " + displaySize.y);
                }
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
    
    
    
	
    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    
    
    
    
    public static boolean copyFile(InputStream sourceFile, File destFile) throws IOException {
        OutputStream out = new FileOutputStream(destFile);
        byte[] buf = new byte[4096];
        int len;
        while ((len = sourceFile.read(buf)) > 0) {
            Thread.yield();
            out.write(buf, 0, len);
        }
        out.close();
        return true;
    }
	
    
    
	public static void downloadFileWithDownloadManager (Context context, String url, String description, String title, String fileName){
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDescription(description);
		request.setTitle(title);
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.allowScanningByMediaScanner();
		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

		// get download service and enqueue file
		DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
	}
    
    
    
	public static float dipToPixels(Context context, int dipValue) {
	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}
	
	public static Bitmap getImage(URL url) {
	    HttpURLConnection connection = null;
	    try {
	      connection = (HttpURLConnection) url.openConnection();
	      connection.connect();
	      int responseCode = connection.getResponseCode();
	      if (responseCode == 200) {
	        return BitmapFactory.decodeStream(connection.getInputStream());
	      } else
	        return null;
	    } catch (Exception e) {
	      return null;
	    } finally {
	      if (connection != null) {
	        connection.disconnect();
	      }
	    }
	  }
	  public static Bitmap getImage(String urlString) {
	    try {
	      URL url = new URL(urlString);
	      return getImage(url);
	    } catch (MalformedURLException e) {
	      return null;
	    }
	  }
	
	
	
	public static boolean isConnectToInternet (Context context){
		ConnectivityManager cm =
		        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}
	
	public static boolean isConnectToInternet (){
		if (applicationContext == null) {
			Log.e(LOG_TAG, "applicationContext == null");
			return true;
		}
		ConnectivityManager cm =
		        (ConnectivityManager)applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}
	
	
	
	
	
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
	public static Context applicationContext = null;

	/**
	 * Generate a value suitable for use in {@link #setId(int)}.
	 * This value will not collide with ID values generated at build time by aapt for R.id.
	 *
	 * @return a generated ID value
	 */
	public static int generateViewId() {
	    for (;;) {
	        final int result = sNextGeneratedId.get();
	        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
	        int newValue = result + 1;
	        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
	        if (sNextGeneratedId.compareAndSet(result, newValue)) {
	            return result;
	        }
	    }
	}
	/**
	 * @param id id of the user, page, event, ...
	 * @param imageFormat must one of <code>FacebookUtil.SQUARE, FacebookUtil.SMALL, FacebookUtil.NORMAL, FacebookUtil.LARGE</code>
	 * @return the url of the image
	 */
	public static String getImageURLForId(String id, String imageFormat)
	{
		return new StringBuilder("https://graph.facebook.com/v2.4/").append(id).append("/picture?type=").append(imageFormat).toString();
	}
	
	/**
	 * @param id id of the user, page, event, ...
	 * @return the url of a 50px square image
	 */
	public static String getImageURLForId(String id)
	{
		return getImageURLForId(id, SQUARE);
	}
	
	public static String getImageURLForIdLarge(String id)
	{
		if (density <= 2) 
				return getImageURLForId(id, NORMAL);
		return getImageURLForId(id, LARGE);
	}
	
	public static String getNormalImageURLForIdLarge(String id)
	{
		return getImageURLForId(id, NORMAL);
	}
	
	
	public static boolean isDownloadManagerAvailable(Context context) {
	    try {
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
	            return false;
	        }
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
	                PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	  // Prevents instantiation.
    private Utils() {}

    /**
     * Enables strict mode. This should only be called when debugging the application and is useful
     * for finding some potential bugs or best practice violations.
     */
    @TargetApi(11)
    public static void enableStrictMode() {
        // Strict mode is only available on gingerbread or later
        if (Utils.hasGingerbread()) {

            // Enable all thread strict mode policies
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Enable all VM strict mode policies
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Use builders to enable strict mode policies
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    /**
     * Uses static final constants to detect if the device's platform version is Gingerbread or
     * later.
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb or
     * later.
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb MR1 or
     * later.
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Uses static final constants to detect if the device's platform version is ICS or
     * later.
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
    
    
    public static ChatMessage checkAndAddTimeStamp (ChatMessage currentEvent, ChatMessage lastEvent){
    	if (lastEvent == null) // the first time send message, so no last event
    		return currentEvent.makeSeparator();
    	SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    	if( fmt.format(new Date(currentEvent.getSentTime())).equals(fmt.format(new Date(lastEvent.getSentTime()))))
    		return null;
    	else
    		return currentEvent.makeSeparator();
    } 
    
    
    /** Returns the given date with the time set to the start of the day. */
    public static Date getStart(Date date) {
        return clearTime(date);
    }
    
    
    
    /** Returns the given date with the time values cleared. */
    public static Date clearTime(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }    
    // Load library
    static {
        System.loadLibrary("tmessages");
    }
 }