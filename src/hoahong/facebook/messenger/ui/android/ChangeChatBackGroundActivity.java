package hoahong.facebook.messenger.ui.android;

import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.data.User;
import hoahong.facebook.messenger.ui.Utils;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog.Builder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

public class ChangeChatBackGroundActivity extends ActionBarActivity {
	public static final String TAG = "ChangeChatBackGroundActivity";
	public static final String BACKGROUND_KEY = "bacground_key";
	public static final String BACKGROUND_PATH_KEY = "background_path_key";
	public static final int GALLERY_PHOTO_REQUEST = 1;
	private static final int [] BACKGROUNDS ={R.drawable.background_hd, R.drawable.flower, 
				R.drawable.love_couple };
	
	private ImageView largeBackgroundImageView;
	private LinearLayout previewScrollView;
	
	private SessionManager sessionManager;
	private int selectedBackgroundId;
	private int currentSelectedViewId;
	private String galleryImagePath;
	int displayWidth;
	int displayHeight;
	private String friendName;
	private String uid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sessionManager = new SessionManager(ChangeChatBackGroundActivity.this);
		if (getIntent() !=null) {
			friendName = getIntent().getStringExtra(User.USER_NAME_FIELD);
			uid = getIntent().getStringExtra(User.USER_JABBER_ID);
		}
		selectedBackgroundId = sessionManager.getBackGroundId(uid);
		setContentView(R.layout.activity_change_chat_back_ground);
		displayWidth = this.getResources().getDisplayMetrics().widthPixels;
		displayHeight = this.getResources().getDisplayMetrics().heightPixels;
		largeBackgroundImageView = (ImageView) findViewById(R.id.large_background_imageview);
		previewScrollView = (LinearLayout) findViewById(R.id.change_background_preview_horizontal_view);
		if (Utils.isEmpty(sessionManager.getBackgroundPath(uid)))
			Picasso.with(ChangeChatBackGroundActivity.this)
			.load(selectedBackgroundId)
			.resize(displayWidth, displayWidth - Utils.dp(45) )
			.centerCrop()
			.into(largeBackgroundImageView); 
		else 
			Picasso.with(ChangeChatBackGroundActivity.this)
			.load("file://" + sessionManager.getBackgroundPath(uid))
			.resize(displayWidth, displayWidth - Utils.dp(45) )
			.centerCrop()
			.into(largeBackgroundImageView); 
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		actionBar.getTabAt(0).
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		Tab tab = actionBar.newTab() 
				.setTag("cancel")
				.setText(getString(android.R.string.cancel))
				.setTabListener(new ChangeBackgroundTabListener("cancel"));
		actionBar.addTab(tab, false);
		
		tab = actionBar.newTab() 
				.setTag("set")
				.setText(getString(R.string.set))
				.setTabListener(new ChangeBackgroundTabListener("set"));
		actionBar.addTab(tab, false);
		
		
		addViewsToBackgroundPreview();
		
	}


	
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) 
	public void addViewsToBackgroundPreview (){
		LayoutInflater inflater = LayoutInflater.from(ChangeChatBackGroundActivity.this);
		final View galleryView = inflater.inflate(R.layout.background_preview_item, null );
		ImageView backgroundGalleryImage = (ImageView)galleryView.findViewById(R.id.image);
		Picasso.with(ChangeChatBackGroundActivity.this)
			.load(R.drawable.ic_gallery_background)
			.into(backgroundGalleryImage); 
		galleryView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChangeChatBackGroundActivity.this, BrowseImageActivity.class);
				startActivityForResult(intent, GALLERY_PHOTO_REQUEST);
			}
		});
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) 
			galleryView.setId(Utils.generateViewId());
		else 
			galleryView.setId(View.generateViewId());
		previewScrollView.addView(galleryView);
		
		
		
		for (final int i : BACKGROUNDS){	
			final View view = inflater.inflate(R.layout.background_preview_item, null );
			ImageView image = (ImageView)view.findViewById(R.id.image);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {

				view.setId(Utils.generateViewId());

		    } else {

		    	view.setId(View.generateViewId());

		    }
			final FrameLayout frame = (FrameLayout) view.findViewById(R.id.selection);
			if (i == selectedBackgroundId){
				currentSelectedViewId = view.getId();
				view.setSelected(true);
			}
			if (view.isSelected())
				frame.setVisibility(View.VISIBLE);
			else 
				frame.setVisibility(View.GONE);
			
			Picasso.with(ChangeChatBackGroundActivity.this)
				.load(i)
				.resizeDimen(R.dimen.background_preview_dimen, R.dimen.background_preview_dimen)
				.centerCrop() 
				.into(image); 
			
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					galleryImagePath = null;
					if (view.isSelected());
					else {
						removeCurrentSelected();
						view.setSelected(true);
						currentSelectedViewId = view.getId();
						frame.setVisibility(View.VISIBLE);
						
						
						Picasso.with(ChangeChatBackGroundActivity.this)
						.load(i)
						.resize(displayWidth, displayHeight - Utils.dp(45) )
						.centerCrop()
						.placeholder(selectedBackgroundId)
						.into(largeBackgroundImageView); 
						selectedBackgroundId = i;
					}
					
				}
			});
			previewScrollView.addView(view);
			
		}
		
	}
	
	public void removeCurrentSelected (){
		View currentView = previewScrollView.findViewById(currentSelectedViewId);
		if (currentView != null){
			currentView.setSelected(false);
			currentView.findViewById(R.id.selection).setVisibility(View.GONE);
			currentView.requestLayout();
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GALLERY_PHOTO_REQUEST && RESULT_OK == resultCode){
			galleryImagePath = data.getStringExtra(BrowseImageActivity.RETURN_IMAGE_URI_KEY);
			removeCurrentSelected();
			Picasso.with(ChangeChatBackGroundActivity.this)
				.load("file://" + galleryImagePath)
				.resize(displayWidth, displayHeight - Utils.dp(45) )
				.centerCrop()
				.placeholder(selectedBackgroundId)
				.into(largeBackgroundImageView); 
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	public class ChangeBackgroundTabListener implements ActionBar.TabListener {
		private String tag;

		public ChangeBackgroundTabListener(String tag){
			this.tag = tag;
		}
		@Override
		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
			onTabSelected(arg0, arg1);
			
		}

		@Override
		public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
			if (tag.equals("cancel")){
				setResult(RESULT_CANCELED); 
				finish();
			}else{
				if (!Utils.isEmpty(friendName) &&!Utils.isEmpty(uid))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(ChangeChatBackGroundActivity.this);
					builder.setMessage(String.format(getString(R.string.change_background_confirm), friendName))
							.setNegativeButton(friendName + " only", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent intent = new Intent(ChangeChatBackGroundActivity.this, ChatActivity.class);
									if (Utils.isEmpty(galleryImagePath )){
										sessionManager.setBackgroundId(selectedBackgroundId, uid);
										sessionManager.setBackgroundPath(null, uid);
										Log.e(TAG, "set background id: " + selectedBackgroundId  + "  for: " + uid);
										intent.putExtra(BACKGROUND_KEY, selectedBackgroundId);
									}else {
										sessionManager.setBackgroundPath(galleryImagePath, uid);
										Log.e(TAG, "set background path: " + galleryImagePath   + "  for: " + uid);
										intent.putExtra(BACKGROUND_PATH_KEY, galleryImagePath);
									}
									setResult(RESULT_OK, intent);
									finish();
								}
							})
							.setPositiveButton(R.string.change_background_for_all_user_options, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent intent = new Intent(ChangeChatBackGroundActivity.this, ChatActivity.class);
									if (Utils.isEmpty(galleryImagePath )){
										sessionManager.setBackgroundId(selectedBackgroundId);
										sessionManager.setBackgroundPath(null);
										Log.e(TAG, "set background id for all: " + selectedBackgroundId);
										intent.putExtra(BACKGROUND_KEY, selectedBackgroundId);
									}else {
										sessionManager.setBackgroundPath(galleryImagePath);
										Log.e(TAG, "set background path for all: " + galleryImagePath);
										intent.putExtra(BACKGROUND_PATH_KEY, galleryImagePath);
									}
									setResult(RESULT_OK, intent);
									finish();
								}
							}).show();
					
					/*
					*/
				}
				else {

					Intent intent = new Intent(ChangeChatBackGroundActivity.this, ChatActivity.class);
					if (Utils.isEmpty(galleryImagePath )){
						sessionManager.setBackgroundId(selectedBackgroundId);
						sessionManager.setBackgroundPath(null);
						Log.e(TAG, "set background id for all: " + selectedBackgroundId);
						intent.putExtra(BACKGROUND_KEY, selectedBackgroundId);
					}else {
						sessionManager.setBackgroundPath(galleryImagePath);
						Log.e(TAG, "set background path for all: " + galleryImagePath);
						intent.putExtra(BACKGROUND_PATH_KEY, galleryImagePath);
					}
					setResult(RESULT_OK, intent);
					finish();
				}
			}
			
		}

		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
		
	}
}