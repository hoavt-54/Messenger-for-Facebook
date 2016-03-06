package co.beem.project.beem.ui.wizard;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import hoahong.facebook.messenger.R;

public class WebViewFragment extends Fragment{
	public static final String URL_KEY = "url_of_this_fragment";
	private WebView webView;
	private ProgressBar loadingBar;
	private String url;
	private static final int FILECHOOSER_RESULTCODE   = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
	public WebViewFragment(){
		super();
	}
	
	public static WebViewFragment instanciate (String url) {
		WebViewFragment fragment = new WebViewFragment();
		Bundle args = new Bundle();
		args.putString(URL_KEY, url);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null){
			this.url = getArguments().getString(URL_KEY);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.login_wizard_webview, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		webView = (WebView) view.findViewById(R.id.login_webview);
		loadingBar = (ProgressBar) view.findViewById(R.id.loading_progressbar);
		webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) 
            {
            if(progress < 100 && loadingBar.getVisibility() == ProgressBar.GONE){
                loadingBar.setVisibility(ProgressBar.VISIBLE);
            }
            loadingBar.setProgress(progress);
            if(progress == 100) {
                loadingBar.setVisibility(ProgressBar.GONE);
            }
         }
            
            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){  
                
                // Update message
                mUploadMessage = uploadMsg;
                 
                try{    
                 
                    // Create AndroidExampleFolder at sdcard
                     
                    File imageStorageDir = new File(
                                           Environment.getExternalStoragePublicDirectory(
                                           Environment.DIRECTORY_PICTURES)
                                           , "AndroidExampleFolder");
                                            
                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }
                     
                    // Create camera captured image file path and name 
                    File file = new File(
                                    imageStorageDir + File.separator + "IMG_"
                                    + String.valueOf(System.currentTimeMillis()) 
                                    + ".jpg");
                                     
                    mCapturedImageURI = Uri.fromFile(file); 
                     
                    // Camera capture image intent
                    final Intent captureIntent = new Intent(
                                                  android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                   
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                    
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT); 
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                     
                    // Create file chooser intent
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                     
                    // Set camera intent to file chooser 
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                                           , new Parcelable[] { captureIntent });
                     
                    // On select image call onActivityResult method of activity
                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                     
                  }
                 catch(Exception e){
                     Toast.makeText(getActivity(), "Exception:"+e, 
                                Toast.LENGTH_LONG).show();
                 }
                 
            }
             
            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                openFileChooser(uploadMsg, "");
            }
             
            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg, 
                                       String acceptType, 
                                       String capture) {
                                        
                openFileChooser(uploadMsg, acceptType);
            }
            // The webPage has 2 filechoosers and will send a 
            // console message informing what action to perform, 
            // taking a photo or updating the file
             
            public boolean onConsoleMessage(ConsoleMessage cm) {  
                   
                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }
             
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Log.d("androidruntime", "Show console messages, Used for debugging: " + message);
                 
            }
            
     });
	
		webView.setWebViewClient(new WebViewClient() {
	        @Override
	        public void onReceivedError(WebView view, int errorCode,
	                String description, String failingUrl) {
	            // Handle the error
	        }

	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);
	            return true;
	        }
	    });
		webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginState(PluginState.ON);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setSupportZoom(true); 
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.loadUrl(url);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==FILECHOOSER_RESULTCODE)  
	     {  
	            if (null == this.mUploadMessage) {
	                return;
	            }
	           Uri result=null;
	           try{
	                if (resultCode != Activity.RESULT_OK) {
	                    result = null;
	                } else {
	                     
	                    // retrieve from the private variable if the intent is null
	                    result = data == null ? mCapturedImageURI : data.getData(); 
	                } 
	            }
	            catch(Exception e)
	            {
	                Toast.makeText(getActivity(), "activity :"+e,
	                 Toast.LENGTH_LONG).show();
	            }
	             
	            mUploadMessage.onReceiveValue(result);
	            mUploadMessage = null;
	      
	     }
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	public void goback () {
		if (webView.canGoBack()) {
	        webView.goBack();
	    } 
	}
}
