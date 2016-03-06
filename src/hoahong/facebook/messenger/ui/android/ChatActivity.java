package hoahong.facebook.messenger.ui.android;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jivesoftware.smack.util.StringUtils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import co.beem.project.beem.service.BeemMessage;
import co.beem.project.beem.service.Contact;
import co.beem.project.beem.service.PresenceAdapter;
import co.beem.project.beem.service.aidl.IBeemRosterListener;
import co.beem.project.beem.service.aidl.IChat;
import co.beem.project.beem.service.aidl.IChatManager;
import co.beem.project.beem.service.aidl.IChatManagerListener;
import co.beem.project.beem.service.aidl.IMessageListener;
import co.beem.project.beem.service.aidl.IRoster;
import co.beem.project.beem.service.aidl.IXmppFacade;
import co.beem.project.beem.ui.Login;
import co.beem.project.beem.ui.TypingDotsDrawable;
import co.beem.project.beem.utils.BeemBroadcastReceiver;
import co.beem.project.beem.utils.PresenceType;
import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.connection.FacebookTextConnectionListener;
import hoahong.facebook.messenger.data.ChatMessage;
import hoahong.facebook.messenger.data.ChatMessageType;
import hoahong.facebook.messenger.data.ChatSession;
import hoahong.facebook.messenger.data.User;
import hoahong.facebook.messenger.data.UserState;
import hoahong.facebook.messenger.ui.Emoji;
import hoahong.facebook.messenger.ui.EmojiView;
import hoahong.facebook.messenger.ui.NotificationCenter;
import hoahong.facebook.messenger.ui.SizeNotifierRelativeLayout;
import hoahong.facebook.messenger.ui.SizeNotifierRelativeLayout.SizeNotifierRelativeLayoutDelegate;
import hoahong.facebook.messenger.ui.Utils;
import hoahong.facebook.messenger.ui.android.LargeImageViewFragment.PhotoSupporter;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ChatActivity extends ActionBarActivity implements SizeNotifierRelativeLayoutDelegate, 
			NotificationCenter.NotificationCenterDelegate, FacebookTextConnectionListener, PhotoSupporter{
	
	public static final String LOG_TAG = "hoahong.facebook.messenger.ChatActivity";
	public static final String MEDIA_STORAGE_DIR= "CuteMessenger";
	public static final int REQUEST_CHANGE_BACKGROUND = 1;
	public static final int REQUEST_TAKE_PHOTO = 2;
	public static final int REQUEST_UPLOAD_PHOTO = 3;
	public static final String APP_GOOGLE_PLAY_LINK = "https://play.google.com/store/apps/details?id=hoahong.facebook.messenger";
	// connect with bacground service
    private static final Intent SERVICE_INTENT = new Intent();
    static {
		SERVICE_INTENT.setComponent(new ComponentName("hoahong.facebook.messenger",
				"co.beem.project.beem.FbTextService"));
	}
    private Handler mHandler = new Handler();
    private IRoster mRoster;
    private Contact mContact;
    
    private IChat mChat;
    private IChatManager mChatManager;
    private final IMessageListener mMessageListener = new OnMessageListener();
    private final IChatManagerListener mChatManagerListener = new ChatManagerListener();

    private final ServiceConnection mConn = new FacebookTextServiceConnection();
    private final BeemBroadcastReceiver mBroadcastReceiver = new BeemBroadcastReceiver();
    private final BeemRosterListener mBeemRosterListener = new BeemRosterListener();
    private IXmppFacade mXmppFacade;
    private boolean mBinded;
    private SessionManager sessionManager;
	 
	
	// view resource
	private ChatAdapter chatAdapter;
	private se.emilsjolander.stickylistheaders.StickyListHeadersListView chatListView;
	private EditText messsageEditText;
	private ImageButton sendBtn;
	private ImageView emojiButton;
	private PopupWindow emojiPopup;
	private hoahong.facebook.messenger.ui.EmojiView emojiView;
	private SizeNotifierRelativeLayout sizeNotifierRelativeLayout;
	private TypingDotsDrawable typingDotsDrawable;
	private android.support.v7.app.ActionBar  actionBar;
	private LinearLayout connectionAreaLayout;
	private ProgressBar connectionProgressBar;
	private TextView connectionMessageTv;
	private RenameDialogFragment renameDialog;
	private ImageView imageAttachButton;
	private CameraOrGalleryDialog selectImageDialog;
	private LargeImageViewFragment largerImageView;
	 
	//data resource
	private List<ChatMessage> listMessages;
	private List<String> listPhotoPaths;
	private static boolean isPhotoFilterRunning;
	private ChatSession currentSession;
	private int currentSessionId;
	private String currentUserId;
	private static User currentUser;
	private int keyboardHeight = 0;
    private int keyboardHeightLand = 0;
    private boolean keyboardVisible;
    private boolean isChangeBackground;
    private SharedPreferences settings;
    private boolean isConnected = true;
    private String currentImagePath;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    
    
	
	//connection resource
	private Dao<ChatMessage, Integer> chatMessageDao;
	private Dao<ChatSession, Integer> chatSessionDao;
	private Dao<User, String> userDao;
	
	// for action mode
	
	private int numberTextEventSelected;
	// isConnectIn is for start the reconnection manager
	// right away if there is network connection and reconnector is sleep
	private boolean isConnectingIn = false;
	private int connectingInSeconds;

	
	public int getStatusBarHeight() {
	      int result = 0;
	      int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	      if (resourceId > 0) {
	          result = getResources().getDimensionPixelSize(resourceId);
	      }
	      return result;
	}
	
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
	protected void onCreate(Bundle savedInstanceState) {
		
		this.registerReceiver(mBroadcastReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CLOSED));
		this.registerReceiver(mBroadcastReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTED));
		this.registerReceiver(mBroadcastReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTING));
		this.registerReceiver(mBroadcastReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_DISCONNECT));
		this.registerReceiver(mBroadcastReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CONNECTING_In));
		this.registerReceiver(mBroadcastReceiver, new IntentFilter(FbTextApplication.IMAGE_URL_FB));
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		sessionManager = new SessionManager(ChatActivity.this);
		if (Utils.applicationContext == null)
			Utils.applicationContext = getApplicationContext();
		
		// remove later
		Utils.setContext(getApplicationContext());
		Utils.setDensity(ChatActivity.this.getResources().getDisplayMetrics().density);
		Utils.setStatusBarHight(getStatusBarHeight());
		
		 NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStarted);
	        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStartError);
	        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStopped);
	        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordProgressChanged);
	        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
	        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidSent);
	        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
	        NotificationCenter.getInstance().addObserver(this, NotificationCenter.hideEmojiKeyboard);
	        
	        
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		
		
		
		//setup view reference
		actionBar = getSupportActionBar();
		setUpActionBar();
		sizeNotifierRelativeLayout  = (SizeNotifierRelativeLayout) findViewById(R.id.chat_activity_container);
		/*if (Utils.isEmpty(sessionManager.getBackgroundPath()))
			setChatBackGround(sessionManager.getBackGroundId());
		else
			setChatBackGround(sessionManager.getBackgroundPath());*/
		sizeNotifierRelativeLayout.delegate = this;
		chatListView = (StickyListHeadersListView) findViewById(R.id.chat_listview);
		messsageEditText = (EditText) findViewById(R.id.chat_text_edit);
		messsageEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == 4 && !keyboardVisible && emojiPopup != null && emojiPopup.isShowing()) {
                    if (keyEvent.getAction() == 1) {
                        showEmojiPopup(false);
                    }
                    return true;
                } /*else if (i == KeyEvent.KEYCODE_ENTER && true && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                	String textContent = messsageEditText.getText().toString();
    				messsageEditText.setText("");
    				if (textContent.trim().length() != 0)
    					sendMessage(textContent.trim(), true);
                    return true;
                }*/
                return false;
            }
        });
		
		messsageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emojiPopup != null && emojiPopup.isShowing()) {
                    showEmojiPopup(false);
                }
                if (chatListView != null && chatAdapter != null && mHandler!= null)
                	chatListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							chatListView.setSelection(chatAdapter.getCount() -1);
						}
					}, 1200);
            }
        });
		
		messsageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int i = 0;
                ImageSpan[] arrayOfImageSpan = editable.getSpans(0, editable.length(), ImageSpan.class);
                int j = arrayOfImageSpan.length;
                while (true) {
                    if (i >= j) {
                        Emoji.replaceEmoji(editable, messsageEditText.getPaint().getFontMetricsInt(), Utils.dp(20));
                        return;
                    }
                    editable.removeSpan(arrayOfImageSpan[i]);
                    i++;
                }
            }
        });
		
		
		
		emojiButton = (ImageView) findViewById(R.id.chat_smile_button);
		 emojiButton.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	                if (emojiPopup == null) {
	                    showEmojiPopup(true);
						chatListView.setSelection(chatAdapter.getCount() -1);
	                } else {
	                    showEmojiPopup(!emojiPopup.isShowing());
	                    if (chatListView != null && chatAdapter != null)
							chatListView.setSelection(chatAdapter.getCount() -1);
	                }
	            }
	        });
		sendBtn = (ImageButton) findViewById(R.id.chat_send_button);
		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String textContent = messsageEditText.getText().toString();
				messsageEditText.setText("");
				if (textContent.trim().length() != 0)
					sendMessage(textContent.trim(), true);

			}
		});
		
		imageAttachButton = (ImageView) findViewById(R.id.chat_image_attachment);
		imageAttachButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectImageDialog == null) 
					selectImageDialog = new CameraOrGalleryDialog ();
				if (!selectImageDialog.isVisible())
				selectImageDialog.show(getSupportFragmentManager(), "select_image_fragment");
			}
		});
		
		// get data from intent
		Intent intent = getIntent();
		if (intent != null){
			currentSessionId = intent.getIntExtra(ChatSession.SESSION_CHAT_ID_KEY, 0);
			mContact = new Contact(intent.getData());
			System.out.println("Contact received in Oncreate has name: " + 
							mContact.getName() + "  \t" + mContact.getJID());
			Editor editor = settings.edit();
			editor.putString(FbTextApplication.CURRENT_CHAT_ID, mContact.getJID());
			editor.apply();
		}
		listMessages = new ArrayList<ChatMessage>();
		chatAdapter = new ChatAdapter(ChatActivity.this, listMessages);
		chatListView.setAdapter(chatAdapter);
		
		
		chatListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				hideKeyboard();
				ChatMessage item = listMessages.get(position);
				if (ChatMessageType.image == item.getType() && !isPhotoFilterRunning){
					FilterPhotoTask photoTask = new FilterPhotoTask(position, view, item.getImagePath());
					Utils.executeAsyncTask(photoTask);
				}
				
			}
		});
		
		
		
		
		// set multiple choice mode
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    // we are on HoneyComb or above so its safe to use
		
			chatListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			chatListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					if (numberTextEventSelected > 0){
						MenuItem item = menu.findItem(R.id.context_action_copy);
						item.setVisible(true);
					}else{
						MenuItem item = menu.findItem(R.id.context_action_copy);
						item.setVisible(false);
					}
					return true;
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					((listSelectable)chatAdapter).emptySelection();
					numberTextEventSelected = 0;
					
				}
				
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
			        inflater.inflate(R.menu.chat_activity_context_menu, menu);
					return true;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
					case R.id.context_action_copy:
						String copyContent = "";
						for (int eventId : ((listSelectable)chatAdapter).getListIdSelected()){
							ChatMessage event = listMessages.get(eventId);
							if (event.getType() == ChatMessageType.text)
								copyContent += event.getMessageContent() + "\n";
						}
						
						
						if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
						    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) 
						    		getSystemService(Context.CLIPBOARD_SERVICE);
						    clipboard.setText(copyContent);
						} else {
						    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
						    		getSystemService(Context.CLIPBOARD_SERVICE);
						    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", copyContent);
						            clipboard.setPrimaryClip(clip);
						}
						break;

					case R.id.context_action_delete:
						//delete from chat list
						if(FbTextApplication.isDebug)
						Toast.makeText(getApplicationContext(), "on delete", Toast.LENGTH_SHORT).show();
						((listSelectable)chatAdapter).deleteSelectedChatMessages();
						break;
						
						
					default:
						
						break;
					}
					
					((listSelectable)chatAdapter).emptySelection();
					mode.finish();
					return true;
				}
				
				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position,
						long id, boolean checked) {
					if (checked){
						//if (FbTextApplication.isDebug)
						//Toast.makeText(getApplicationContext(), "checked id: " + position, Toast.LENGTH_SHORT).show();
						((listSelectable)chatAdapter).addSelection(position, checked);
						if (listMessages.get(position).getType() == ChatMessageType.text)
							numberTextEventSelected ++;
						
					}
					else {
						((listSelectable)chatAdapter).removeSelection(position);
						if (listMessages.get(position).getType() == ChatMessageType.text)
							numberTextEventSelected --;
					}
					
					// set text on the action mode
					mode.setTitle("Selected: " + ((listSelectable)chatAdapter).getSelectedCount());
					if (numberTextEventSelected == 1 || numberTextEventSelected == 0){
						mode.invalidate();
					}
					
				}
			});
		}
		
		// load message from database
		LoadChatEventsFromDatabase loadmessageTask = new LoadChatEventsFromDatabase(ChatActivity.this);
		Utils.executeAsyncTask(loadmessageTask);
		
		connectionAreaLayout = (LinearLayout) findViewById(R.id.connection_area);
		connectionMessageTv = (TextView) findViewById(R.id.connection_message_tv);
		connectionProgressBar = (ProgressBar) findViewById(R.id.connection_progressbar);
		
		// bind to service when all resource has been collected
		
		int clickCounts = settings.getInt(FbTextApplication.CLICK_CHAT_ACTIVITY_COUNT_KEY, 0) + 1;
		settings.edit().putInt(FbTextApplication.CLICK_CHAT_ACTIVITY_COUNT_KEY, clickCounts).apply();
		
		
		//view tutorial
		if(!settings.getBoolean(FbTextApplication.VIEW_CHAT_TUTORIAL_KEY, false)){
			Intent tutorialIntent = new Intent(ChatActivity.this, ChatTutorialActivity.class);
			startActivityForResult(tutorialIntent, 1000);
			settings.edit().putBoolean(FbTextApplication.VIEW_CHAT_TUTORIAL_KEY, true).commit();
		}
	}// end of OnCreate
	
	void setUpActionBar () {
		actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setIcon(R.drawable.user_placeholder);
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chatold, menu);
		if (currentUser != null){
			String name = currentUser.getName();
			
			String [] nameSplits = null;
			if (name != null)
				nameSplits = name.split("\\s+");
			if (nameSplits != null && nameSplits.length > 2)
				name = nameSplits[0];
				
			menu.findItem(R.id.acction_share_app).setTitle(getString(R.string.action_share_this_app) + " " + name);
			
			if (currentUser.isBlocked()){
				menu.findItem(R.id.acction_block_friend).setVisible(false);
				menu.findItem(R.id.acction_unblock_friend).setVisible(true);
			}
			else{
				menu.findItem(R.id.acction_block_friend).setVisible(true);
				menu.findItem(R.id.acction_unblock_friend).setVisible(false);
				}
			if (currentUser.isFavorite()){
				menu.findItem(R.id.action_add_favorite).setVisible(false);
				menu.findItem(R.id.action_remove_favorite).setVisible(true);
			}
			else {
				menu.findItem(R.id.action_add_favorite).setVisible(true);
				menu.findItem(R.id.action_remove_favorite).setVisible(false);
			}
		}
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			Intent intentsetting = new Intent(ChatActivity.this, SettingsActivity.class);
			startActivityForResult(intentsetting, 0);
			return true;
		case android.R.id.home:
			try{
				if (largerImageView != null && LargeImageViewFragment.isOpening){
					largerImageView.closePhoto();
					supportInvalidateOptionsMenu();
					if (currentUser !=  null && currentUser.getState() == UserState.available)
						actionBar.setSubtitle("is online");
					else 
						actionBar.setSubtitle("is offline");
					return true;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			Intent intentsd = new Intent(ChatActivity.this, FbTextMainActivity.class);
			if (!FbTextMainActivity.isRunning){
				startActivity(intentsd);
			}
			setResult(RESULT_OK, intentsd);
			finish();
			break;
		case R.id.acction_change_chat_background:
			Intent intent = new Intent(ChatActivity.this, ChangeChatBackGroundActivity.class);
			String name = "";
			String uid  = "";
			if (currentUser != null){
				name = currentUser.getName().split(" ")[0];
				uid = currentUser.getJabberId();
			}
			intent.putExtra(User.USER_NAME_FIELD, name);
			intent.putExtra(User.USER_JABBER_ID, uid);
			startActivityForResult(intent, REQUEST_CHANGE_BACKGROUND);
			break;
		case R.id.acction_unblock_friend:
		case R.id.acction_block_friend:
			BlockFriendTask blockFriendTask = new BlockFriendTask();
			Utils.executeAsyncTask(blockFriendTask);
			break;
			
		case R.id.action_rename_friend:
			renameDialog = new RenameDialogFragment();
			renameDialog.show(getSupportFragmentManager(), "rename_dialog");
			break;
			
		case R.id.action_remove_favorite:
		case R.id.action_add_favorite:
			AddFriendAsFavoriteTask addFavoriteTask = new AddFriendAsFavoriteTask();
			Utils.executeAsyncTask(addFavoriteTask);
			break;
			
		case R.id.acction_share_app:
			messsageEditText.setText(getString(R.string.share_message) + "\t" + APP_GOOGLE_PLAY_LINK);
			messsageEditText.requestFocus();
			
			break;

		case R.id.acction_rate:
			if (!FbTextApplication.isRatable) return true;
			String appPackageName = getPackageName();
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (Exception e) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
				e.printStackTrace();
			}
			
			break;
		case R.id.action_delete_chat:
			android.support.v7.app.AlertDialog.Builder builder = 
			new android.support.v7.app.AlertDialog.Builder(ChatActivity.this);
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.setMessage("Are you sure to delete this chat?");
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (chatSessionDao != null && currentSession != null){
						try{
							chatSessionDao.delete(currentSession);
							setResult(RESULT_OK);
							finish();
						}catch (Exception e){
							e.printStackTrace();
						}
					}
					
				}
			});
			builder.show();
			break;
		default:
			break;
		}
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(LOG_TAG, "onActivityResutl: requestCode: " + requestCode + "  resultCode: " + resultCode);
		if (requestCode == REQUEST_CHANGE_BACKGROUND){
			if (resultCode == RESULT_OK)
				if (Utils.isEmpty(data.getStringExtra(ChangeChatBackGroundActivity.BACKGROUND_PATH_KEY))){
					Log.e(LOG_TAG, "onActivityResutl: background path null");
					setChatBackGround(data.getIntExtra(ChangeChatBackGroundActivity.BACKGROUND_KEY,
						R.drawable.background_hd));
				}
				else {
					isChangeBackground = true;
				}
		}
		else if (REQUEST_TAKE_PHOTO == requestCode && RESULT_OK == resultCode){
			Log.e(LOG_TAG, "taken image path: " + currentImagePath);
			sendChatImageMessage (currentImagePath);
		}else if (REQUEST_UPLOAD_PHOTO == requestCode && RESULT_OK == resultCode){
			Log.e(LOG_TAG, "browsed image path : " + data.getStringExtra(BrowseImageActivity.RETURN_IMAGE_URI_KEY));
			sendChatImageMessage (data.getStringExtra(BrowseImageActivity.RETURN_IMAGE_URI_KEY));
		}
		
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	

	
	
	
	@Override
	protected void onRestart() {
		
		// get data from intent
				Intent intent = getIntent();
				if (intent != null){
					currentSessionId = intent.getIntExtra(ChatSession.SESSION_CHAT_ID_KEY, 0);
					mContact = new Contact(intent.getData());
					System.out.println("Contact received in onRestart has name: " + 
									mContact.getName() + "  \t" + mContact.getJID());
				}
		
		super.onRestart();
	}
	
	
	
	@Override
	protected void onStart() {
		// get data from intent
		Intent intent = getIntent();
		if (intent != null){
			currentSessionId = intent.getIntExtra(ChatSession.SESSION_CHAT_ID_KEY, 0);
			mContact = new Contact(intent.getData());
			System.out.println("Contact received in onStart has name: " + 
							mContact.getName() + "  \t" + mContact.getJID());
							
			Editor editor = settings.edit();
			editor.putString(FbTextApplication.CURRENT_CHAT_ID, mContact.getJID());
			editor.apply();
		}
		
		if (!mBinded) {
			bindService(SERVICE_INTENT, mConn, BIND_AUTO_CREATE);
			mBinded = true;
		}
		super.onStart();
	}
	
	
	
	
	
	@Override
	protected void onResume() {
		handleDisconnectAndLoginAgainFaild();
		// reconnect right away
		isConnected = true;
		
		Utils.setContext(getApplicationContext());
		Utils.setDensity(ChatActivity.this.getResources().getDisplayMetrics().density);
		if (isChangeBackground){
			if (Utils.isEmpty(sessionManager.getBackgroundPath(currentUserId)))
				setChatBackGround(sessionManager.getBackGroundId(currentUserId));
			else
				setChatBackGround(sessionManager.getBackgroundPath(currentUserId));
			//setChatBackGround(sessionManager.getBackgroundPath());
			isChangeBackground = false;
		}

		super.onResume();
		mContact = new Contact(getIntent().getData());
		Editor editor = settings.edit();
		editor.putString(FbTextApplication.CURRENT_CHAT_ID, mContact.getJID());
		editor.apply();
	}
    
	
	
	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (renameDialog != null && renameDialog.isVisible())
			renameDialog.dismiss();
		if (selectImageDialog != null && selectImageDialog.isVisible())
			selectImageDialog.dismiss();
		if (largerImageView != null && LargeImageViewFragment.isOpening){
			largerImageView.closePhoto();
		}
		try {
			if (mChat != null) {
				mChat.setOpen(false);
				mChat.removeMessageListener(mMessageListener);
			}
			if (mRoster != null)
				mRoster.removeRosterListener(mBeemRosterListener);
			if (mChatManager != null)
				mChatManager.removeChatCreationListener(mChatManagerListener);
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
			Log.e(LOG_TAG, e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
		}
		if (mBinded) {
			unbindService(mConn);
			mBinded = false;
		}
		mXmppFacade = null;
		mRoster = null;
		mChat = null;
		mChatManager = null;
		Editor editor = settings.edit();
		editor.putString(FbTextApplication.CURRENT_CHAT_ID, "");
		editor.apply();
	}
	@Override
	public void onBackPressed() {
		try{
			if (largerImageView != null && LargeImageViewFragment.isOpening){
				largerImageView.closePhoto();
				supportInvalidateOptionsMenu();
				if (currentUser != null && currentUser.getState() == UserState.available)
					actionBar.setSubtitle("is online");
				else 
					actionBar.setSubtitle("is offline");
				return;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		super.onBackPressed();
	}
	
    
	 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	 @Override
	protected void onNewIntent(Intent intent) {
			if (FbTextApplication.isDebug)
		 Log.v(LOG_TAG, "received new intent");
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			setIntent(intent);
			recreate();
		}
		else {
			setIntent(intent);
			finish();
			startActivity(intent);
		}
		super.onNewIntent(intent);
	}
    
    
    
    
	@Override
	protected void onDestroy() {
		// stop receiving services
		//stopService(new Intent(ChatActivity.this, BackgroundMessageService.class));
				// stop sending service
		if (emojiPopup != null){
			if (emojiPopup.isShowing())
				emojiPopup.dismiss();
		}
		this.unregisterReceiver(mBroadcastReceiver);
		

		
		if (sizeNotifierRelativeLayout != null) {
            sizeNotifierRelativeLayout.delegate = null;
        }
		
		NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStarted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStartError);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStopped);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordProgressChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidSent);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.hideEmojiKeyboard);
		
        Editor editor = settings.edit();
		editor.putString(FbTextApplication.CURRENT_CHAT_ID, "");
		editor.apply(); 
        
		super.onDestroy();
	}

	
	private void hideKeyboard() {   
	    // Check if no view has focus:
	    View view = this.getCurrentFocus();
	    if (view != null) {
	        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
	public void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (imageUri != null) {
	    	sendChatImageMessage(getPath(imageUri));
	    }
	}
	
	void handleSendMultipleImages(Intent intent) {
	    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    if (imageUris != null) {
	        for (Uri uri : imageUris){
	        	if (uri != null) {
	    	    	sendChatImageMessage(getPath(uri));
	    	    }
	        }
	    }
	}
	private String getPath(Uri uri) {
	    String[]  data = { MediaStore.Images.Media.DATA };
	    CursorLoader loader = new CursorLoader(ChatActivity.this, uri, data, null, null, null);
	    Cursor cursor = loader.loadInBackground();
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}
	
	public void sendChatImageMessage (String imagePath){
		Log.e("SharedImage", imagePath);
		final ChatMessage newChatMessage = new ChatMessage(null, new Date().getTime(), 
				null, ChatMessageType.image, false);
		newChatMessage.setTo(currentUserId);
		newChatMessage.setSession(currentSession);
		newChatMessage.setImagePath(imagePath);
		try {
			if (FbTextApplication.isDebug)
			Log.v(LOG_TAG, "sedding message: " + imagePath);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					addMessage2MessageList(newChatMessage);// update message list (UI thread)
				}
				
			});
			
			//run one thread to save it to database
			//TODO
			saveMessageTask saveMessage = new saveMessageTask();
			Utils.executeAsyncTask(saveMessage, newChatMessage);
			//putMessageToService(newChatMessage);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
     * Send an XMPP message.
     */
    private void sendMessage(String textContent, boolean isAdd2UI) {
    	if (FbTextApplication.isDebug)
    	Log.v(LOG_TAG, "isConnected: " + isConnected);
    	if (!((FbTextApplication)Utils.applicationContext).isConnected()){
    		Toast.makeText(ChatActivity.this, R.string.cannot_send_message_error,  Toast.LENGTH_LONG).show();;
    		FbTextApplication.isRatable = false;
    		if (textContent != null)
    			messsageEditText.setText(textContent);
    		return;
    	}
	final String inputContent = textContent;

	if (!"".equals(inputContent)) {
	    BeemMessage msgToSend = new BeemMessage(mContact.getJIDWithRes(), BeemMessage.MSG_TYPE_CHAT);
	    msgToSend.setBody(inputContent);

	    try {
		if (mChat == null) {
		    mChat = mChatManager.createChat(mContact, mMessageListener);
		    mChat.setOpen(true);
		}
		mChat.sendMessage(msgToSend);
	    } catch (RemoteException e) {
	    	if (FbTextApplication.isDebug)
		Log.e(LOG_TAG, e.getMessage());
	    }catch (Exception e) {
			e.printStackTrace();
			onDisconnect();
			
			//TOTO we can handle sent message here, if exception mean you've not sent
		}
	    if (!isAdd2UI) return;
	    
		final ChatMessage newChatMessage = new ChatMessage(textContent,
				new Date().getTime(), null, ChatMessageType.text, true);
		newChatMessage.setTo(currentUserId);
		newChatMessage.setSession(currentSession);

		try {
			if (FbTextApplication.isDebug)
			Log.v(LOG_TAG, "sedding message: " + textContent);
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					addMessage2MessageList(newChatMessage);// update message list
				}
			});
			
													// (UI thread)
			// run one thread to save it to database
			saveMessageTask saveMessage = new saveMessageTask();
			Utils.executeAsyncTask(saveMessage, newChatMessage);
			// putMessageToService(newChatMessage);
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
    }
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * this method add new chat event to listView and notify UI that list has
	 * been changed
	 */
	public void addMessage2MessageList(ChatMessage newChatMessage) {
		// check if this event is sent in new day, then make a separator
		// then add new chat Event
		
		
		removeTypingDot();
		int size = listMessages.size();
		
		/*if ( size == 0 || newChatMessage.getType() == ChatMessageType.typing || 
					newChatMessage.getType() == ChatMessageType.image || 
							listMessages.get(size -1).getType() ==  ChatMessageType.image ||
					(listMessages.get(size -1).isFromMe() ^ newChatMessage.isFromMe()) 
				|| ((String) listMessages.get(listMessages.size() - 1).getMessageContent()).split("\n").length > 1
				|| newChatMessage.getSentTime() - listMessages.get(size -1).getSentTime() > 200000 
				|| !dateFormat.format(newChatMessage.getSentTime()).equals(dateFormat.format(listMessages.get(size - 1).getSentTime()))){*/
		if (true){
			
			newChatMessage.compsitionLength = 0;
			listMessages.add(newChatMessage);
			
		}
		else {
			ChatMessage previous = listMessages.get(size -1 );
			previous.setMessageContent(previous.getMessageContent() 
					+ System.getProperty ("line.separator") + newChatMessage.getMessageContent());
			previous.compsitionLength += 1;
			listMessages.set(size - 1, previous);
		}
		
//		listMessages.add(newChatMessage);
		//add to list media as well if
		/*if (newChatMessage.getType() == ChatMessageType.image){
			listMediaEvent.add(newChatMessage);
		}*/
		chatAdapter.notifyDataSetChanged();
		chatListView.setSelection(listMessages.size() - 1);
	}


	
    public void removeTypingDot (){
    	int size = listMessages.size();
    	if (size > 0 && listMessages.get(size - 1).getType() == ChatMessageType.typing){
			listMessages.remove(size - 1);
			chatAdapter.notifyDataSetChanged();
    	}
    }
    
	
	/*this class has background thread to save message to database and update last event*/
	private class saveMessageTask extends AsyncTask<ChatMessage, Void, Void>{

		@Override
		protected Void doInBackground(ChatMessage... params) {
			try {
				if (chatMessageDao == null)
					chatMessageDao = ((FbTextApplication)getApplicationContext()).getHelper().getMessageDao();
				for (ChatMessage event : params){
					chatMessageDao.create(event);
					currentSession.setLastMessage(event);
					chatSessionDao.update(currentSession);
					currentUser.increaseMEssageCount();
					UpdateBuilder<User, String> updateBuilder = userDao.updateBuilder();
					updateBuilder.updateColumnValue(User.USER_MESSAGE_COUNT_FIELD, currentUser.getMessageCount());
					updateBuilder.where().eq(User.USER_JABBER_ID, currentUser.getJabberId());
					
					if (event.isFromMe() && ChatMessageType.image == event.getType()){
						sendImagePathToBackground(event.getImagePath(), event.getLocalId());
					}
					
					//userDao.update();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	


	
	/*this class has background thread to save message to database and update last event*/
	private class SaveImageMessage extends AsyncTask<String, Void, List<ChatMessage>>{

		@Override
		protected List<ChatMessage> doInBackground(String... params) {
			List<ChatMessage> messages = new ArrayList<ChatMessage>();
			try {
				if (chatMessageDao == null)
					chatMessageDao = ((FbTextApplication)getApplicationContext()).getHelper().getMessageDao();
				String messagebody = params[0];
				for (String imageLink : messagebody.split("\\s+")) {
					if (imageLink.contains("fbcdn") || imageLink.contains("scontent") || imageLink.contains("//cdn.fb")) {
						ChatMessage event = new ChatMessage("",	new Date().getTime(), 
								currentUser, ChatMessageType.image, false);
						event.setSession(currentSession);
						event.setImagePath(imageLink);
						chatMessageDao.create(event);
						currentSession.setLastMessage(event);
						currentUser.increaseMEssageCount();
						messages.add(event);
					} 
					chatSessionDao.update(currentSession);
					UpdateBuilder<User, String> updateBuilder = userDao	.updateBuilder();
					updateBuilder.updateColumnValue(User.USER_MESSAGE_COUNT_FIELD,
														currentUser.getMessageCount());
					updateBuilder.where().eq(User.USER_JABBER_ID,currentUser.getJabberId());
					
					//int rowAffted = userDao.update(updateBuilder.prepare());
					
					/*if (FbTextApplication.isDebug)
					Log.v(LOG_TAG, "update message count for " + rowAffted
							+ " row: " + currentUser.getMessageCount());*/

					// userDao.update();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			return messages;
		} 
		
		@Override
		protected void onPostExecute(final List<ChatMessage> result) {
			if (result!= null && result.size() > 0){
				for (ChatMessage message : result){
					addMessage2MessageList(message);
				}
			}
			super.onPostExecute(result);
		}
		
	}
	

	
	
	
	

	/*
	 * Define a class load chat events from database it has a background thread
	 */
	private class LoadChatEventsFromDatabase extends
			AsyncTask<Void, Void, Void> {
		public LoadChatEventsFromDatabase(Activity context) {
		}
		
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				// get friend DAO object and query friend, session, chatEvents
				// object from database by background thread
				
				chatSessionDao = ((FbTextApplication)getApplicationContext()).getHelper().getChatSessionDao();
				 userDao = ((FbTextApplication)getApplicationContext()).getHelper().getUserDao();
				chatMessageDao = ((FbTextApplication)getApplicationContext()).getHelper().getMessageDao();
				currentUser = userDao.queryForId(mContact.getJID());
				
				//in case this activity is open from notification, session ID would be 0
				if (currentSessionId == 0) {
					List<ChatSession> listSessionsWithThisFriend = null;
					try {
						listSessionsWithThisFriend = chatSessionDao
								.query(chatSessionDao.queryBuilder()
										.where().eq(ChatSession.USER_NAME_FIELD, currentUser)
										.prepare());
						
						/**
						 * Sometime arg for this query is null, when called from notification :(
						 * 
						 * 
						 * */
					} catch (SQLException e) {
						e.printStackTrace();
					}
					if (listSessionsWithThisFriend == null
							|| listSessionsWithThisFriend.size() == 0) {
						currentSession = new ChatSession(currentUser);
						try {
							chatSessionDao.create(currentSession);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}// else take the first session found with this friend
					else {
						currentSession = listSessionsWithThisFriend.get(0);
					}
				}
				// otherwise, when this activity called from userFragment
				else{
					
					currentSession = chatSessionDao.queryForId(currentSessionId);
					currentUserId = currentUser.getJabberId();
				}
				// query message these two users chat with each other
				List<ChatMessage> tmpMessages = new ArrayList<ChatMessage>();
				try {
					QueryBuilder<ChatMessage, Integer> builder = chatMessageDao.queryBuilder();
					builder.where()
							.eq(ChatMessage.MESSAGE_SESSION_ID, currentSession);
					tmpMessages.addAll(chatMessageDao.query(builder.prepare()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				int i = 0;
				for (ChatMessage event : tmpMessages){
					/*if ( i == 0 || event.getType() == ChatMessageType.image 
								|| listMessages.get(listMessages.size() - 1).getType() ==  ChatMessageType.image
							|| (listMessages.get(listMessages.size() - 1).isFromMe() ^ event.isFromMe()) 
								|| ((String) listMessages.get(listMessages.size() - 1)
											.getMessageContent()).split("\n").length > 1
											|| event.getSentTime() - listMessages.get(listMessages.size() -1).getSentTime() > 200000 
											|| !dateFormat.format(event.getSentTime()).equals(dateFormat.format(listMessages.get(listMessages.size() - 1).getSentTime()))){*/
					if (true){
						listMessages.add(event);
					}
					else {
						ChatMessage previous = listMessages.get(listMessages.size() - 1 );
						previous.setMessageContent(previous.getMessageContent() 
								+ "\n" + event.getMessageContent());
					}
					i ++;
					
					// set unread message to seen locally
					if (!event.isFromMe() && !event.isLocallySeen() ){
						event.setLocallySeen(true);
						try {
							
						} catch (Exception e) {
							chatMessageDao.update(event);
						}
					}
						
				}
				currentSession.setNumberOfUnreadMessages(0);
				chatSessionDao.update(currentSession);
				currentUser.increaseClickCount();
				userDao.update(currentUser);
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try{
			/*if (FbTextApplication.isDebug)
			Log.v("thread", "This LoadChatEventsFromDatabase thread has been killed");
			if (FbTextApplication.isDebug)
			Log.v("thread", "This LoadChatEventsFromDatabase got: " + listMessages.size() + " messages");*/
			((listSelectable) chatAdapter).setSesion(currentSession);
			chatAdapter.notifyDataSetChanged();
			chatListView.setSelection(listMessages.size() - 1);
			actionBar.setTitle(currentUser.getName());
			if (currentUser.getState() == UserState.available)
				actionBar.setSubtitle("is online");
			else 
				actionBar.setSubtitle("is offline");
			/*if (isForwarding){
				Log.v(LOGTAG, "start to forward message");
				ForwardingMessageTask forwardTask = new ForwardingMessageTask();
				Utils.executeAsyncTask(forwardTask, listEventsForwarded);
				isForwarding = false;
			}*/
			}catch (Exception e){
				e.printStackTrace();
			}
			
			if (Utils.isEmpty(sessionManager.getBackgroundPath(currentUserId)))
				setChatBackGround(sessionManager.getBackGroundId(currentUserId));
			else
				setChatBackGround(sessionManager.getBackgroundPath(currentUserId));
			
			/*for sharing*/
			if (FbTextApplication.sharedIntent != null){
				Intent intent = FbTextApplication.sharedIntent;
			    String action = FbTextApplication.sharedIntent.getAction();
			    String type = FbTextApplication.sharedIntent.getType();
			    FbTextApplication.sharedIntent = null;
			    if (Intent.ACTION_SEND.equals(action) && type != null) {
			        if ("text/plain".equals(type)) {
			        	String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			        	sendMessage(sharedText, true);
			        } else if (type.startsWith("image/")) {
			            handleSendImage(intent); // Handle single image being sent
			        }
			    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			        if (type.startsWith("image/")) {
			            handleSendMultipleImages(intent); // Handle multiple images being sent
			        }
			    }
		    }
			super.onPostExecute(result);
		}
	}
	
	/*
	 * This class customize the adapter for list priate chat events
	 * 
	 */

	private class ChatAdapter extends BaseAdapter implements listSelectable, StickyListHeadersAdapter {
		private Activity activity;
		private ChatSession session;
		private TextPaint messagePaint;
		private boolean isFirstTime;
		private LayoutInflater inflater = null;
		private static final int TOTAL_VIEW_TYPE_COUNT = 6;
		private static final int OUTGOING_TEXT_MESSAGE_TYPE = 2;
		private static final int INCOMING_TEXT_MESSAGE_TYPE = 1;
		private static final int DATE_SEPARATOR = 0;
		private static final int TYPING_TYPE = 3;
		private static final int INCOMING_IMAGE_MASSAGE_TYPE = 4;
		private static final int OUTGOING_IMAGE_MESSAGE_TYPE = 5;
/*		private static final int OUTGOING_IMAGE_MESSAGE_TYPE = 3;
		
		private static final int OUTGOING_VIDEO_MESSAGE_TYPE = 5;
		private static final int INCOMING_VIDEO_MESSAGE_TYPE = 6;
		private static final int OUTGOING_GIFT_EMOTION_MESSAGE_TYPE = 7;
		private static final int INCOMING_GIFT_EMOTION_MESSAGE_TYPE = 8;*/
//		private static final int GROUP_NOTIFICATION = 9;
		
		public static final String LOG_TAG = "ChatAdapter";
		private SparseBooleanArray mSelection;
		public ChatAdapter(Activity activity,
				 List<ChatMessage> listMessages ) {
			super();
			this.activity = activity;
			this.isFirstTime = true;
			inflater = LayoutInflater.from(this.activity);
			mSelection = new SparseBooleanArray();
			messagePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
	            messagePaint.setTextSize(Utils.dp(16));
	            messagePaint.setColor(0xff808080);
	      
		}

		@Override
		public void setSesion(ChatSession session) {
			this.session = session;
			
		}
		
		@Override
		public int getCount() {
			return listMessages.size();
			
		}
		@Override
		public boolean isEnabled(int position) {
			int viewType = getItemViewType(position);
			return (viewType == DATE_SEPARATOR || viewType == TYPING_TYPE )? false : true;
		}
		
		@Override
		public int getSelectedCount() {
			
			return mSelection.size();
		}
		
		@Override
		public ArrayList<Integer> getListIdSelected() {
			ArrayList<Integer> listForwardedIds = new ArrayList<Integer>();
			int key = 0;
			for (int i = 0; i < mSelection.size(); i ++){
				key = mSelection.keyAt(i);
				listForwardedIds.add(key);
			}
			return listForwardedIds;
		}
		
		
		@Override
		public void addSelection (int position, boolean value){
			mSelection.put(position, true);
		}
		@Override
		public void removeSelection (int position ){
			mSelection.delete(position);
		}
		
		@SuppressWarnings("unchecked")
		/* sort selected selected positions, we deleted from bigger position to smaller
		 * to make sure that the positions wont not be changed while deletion
		 * */
		public void deleteSelectedChatMessages (){
			/*if (FbTextApplication.isDebug)
			Log.v(LOG_TAG, "Ondelete in adapter, size selected: " + mSelection.size());*/
			int key = 0;
			ArrayList<ChatMessage> listDeletedIds = new ArrayList<ChatMessage>();
			ArrayList<Integer> selection = new ArrayList<Integer>();
			for (int i = 0; i < mSelection.size(); i ++){
				key = mSelection.keyAt(i);
				//get list all the locally id need to delete
				/*
				 * because each item in listview containing multiple chat events,
				 * therefor, we need to retrieve all the real locally id first
				 * by looking at start and end id of each itemview on listview
				 * */
				listDeletedIds.add(listMessages.get(key));
				
				
				//listDeletedIds.add(listMessages.get(key).getLocalId());
				selection.add(key);
//				listMessages.remove(key);
			}
			// sort seledted position
			Collections.sort(selection, new Comparator<Integer>() {

				@Override
				public int compare(Integer lhs, Integer rhs) {
					return - lhs.compareTo(rhs);
					
				}
			});
			
			// delete in listview
			for (int i : selection )
				listMessages.remove(i);
			
			// determine last event and remove empty date separators
			while (listMessages.size() > 0 && listMessages.get(listMessages.size() - 1).getType() == ChatMessageType.date_separator){
				listMessages.remove(listMessages.size() -1);
			}
			if (listMessages.size() > 0){
				session.setLastMessage(listMessages.get(listMessages.size() - 1));
			}
			else 
				session.setLastMessage(null);
			
			
			// remove events in database
			RemovingEventsTask removeTask = new RemovingEventsTask();
			Utils.executeAsyncTask(removeTask, listDeletedIds);
			emptySelection();
			notifyDataSetChanged();
		}
		
		@Override
		public void emptySelection() {
			if (mSelection.size() > 0)
				mSelection.clear();
		}
		
		
		@Override
        public int getItemViewType(int position) {
			ChatMessage item = listMessages.get(position);
			switch (item.getType()) {
			case date_separator:
				return DATE_SEPARATOR;
			case text:
				if (item.isFromMe())
	            	return OUTGOING_TEXT_MESSAGE_TYPE;
				else 
					return INCOMING_TEXT_MESSAGE_TYPE;
			case typing:
				return TYPING_TYPE;
			case image:
				if (item.isFromMe())
					return OUTGOING_IMAGE_MESSAGE_TYPE;
				return INCOMING_IMAGE_MASSAGE_TYPE;
			default:
				return 0;
			}
     
        }
		
		@Override
		public int getViewTypeCount() {
			return TOTAL_VIEW_TYPE_COUNT;
		}

		@Override
		public ChatMessage getItem(int position) {

			return listMessages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

//		public boolean isSelected (int position){
//			return mSelection.get(position);
//		}
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			View vi = convertView;
			int viewType = getItemViewType(position);
			final ViewHolder holder;
			if (vi == null) {
				holder = new ViewHolder();
				
				/*
				 * handle message type when save view component in holder 
				 * 
				 * */
				switch (viewType) {
				case OUTGOING_TEXT_MESSAGE_TYPE: 
					vi = inflater.inflate(R.layout.outgoing_text_chat_event_item_layout, arg2, false);
					holder.messageContent = (TextView) vi
							.findViewById(R.id.messageContent);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);
					holder.sentStatus = (ImageView) vi
							.findViewById(R.id.sentStatus);
					holder.seenStatus = (ImageView) vi
							.findViewById(R.id.seenStatus);
					holder.messageContentWrapper = (RelativeLayout) vi.findViewById(R.id.messageWrapper);
					break;
					
				case INCOMING_TEXT_MESSAGE_TYPE:
					vi = inflater.inflate(R.layout.incoming_text_chat_event_item_layout, arg2, false);
					holder.messageContent = (TextView) vi
							.findViewById(R.id.messageContent);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);
					holder.avatarImage = (ImageView) vi.findViewById(R.id.message_profile_image);
					holder.messageContentWrapper = (RelativeLayout) vi.findViewById(R.id.messageWrapper);
					break;
					
				case INCOMING_IMAGE_MASSAGE_TYPE:
					vi = inflater.inflate(R.layout.incoming_image_chat_event_item_layout, arg2, false);
					holder.messageImage = (ImageView) vi.findViewById(R.id.messageImage);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);
					holder.avatarImage = (ImageView) vi.findViewById(R.id.message_profile_image);
					holder.loaddingMediaProgressBar = (ProgressBar) vi.findViewById(R.id.image_loading_progress_bar);
					holder.messageContentWrapper = (RelativeLayout) vi.findViewById(R.id.messageWrapper);
					break;
				case OUTGOING_IMAGE_MESSAGE_TYPE:
					vi = inflater.inflate(R.layout.outgoing_image_chat_event_item_layout, arg2, false);
					holder.messageImage = (ImageView)vi.findViewById(R.id.messageImage);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);
					holder.sentStatus = (ImageView) vi
							.findViewById(R.id.sentStatus);
					holder.seenStatus = (ImageView) vi
							.findViewById(R.id.seenStatus);
					holder.loaddingMediaProgressBar = (ProgressBar) vi.findViewById(R.id.image_loading_progress_bar);
					holder.messageContentWrapper = (RelativeLayout) vi.findViewById(R.id.messageWrapper);
					holder.failSentImageView = (ImageView)vi.findViewById(R.id.image_fail_view);
					break;
					
					/*case INCOMING_IMAGE_MASSAGE_TYPE:
					vi = inflater.inflate(R.layout.incoming_image_chat_event_item_layout, null);
					holder.messageImage = (ImageView) vi.findViewById(R.id.messageImage);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);	
					holder.loaddingMediaProgressBar = (ProgressBar) vi.findViewById(R.id.image_loading_progress_bar);
					break;
				
				case OUTGOING_VIDEO_MESSAGE_TYPE:
					vi = inflater.inflate(R.layout.outgoing_video_chat_event_item_layout, null);
					holder.messageImage = (ImageView)vi.findViewById(R.id.messageImage);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);
					holder.sentStatus = (ImageView) vi
							.findViewById(R.id.sentStatus);
					holder.seenStatus = (ImageView) vi
							.findViewById(R.id.seenStatus);
					holder.loaddingMediaProgressBar = (ProgressBar) vi.findViewById(R.id.image_loading_progress_bar);
					break;
				
				case INCOMING_VIDEO_MESSAGE_TYPE:
					vi = inflater.inflate(R.layout.incoming_video_chat_event_item_layout, null);
					holder.messageImage = (ImageView)vi.findViewById(R.id.messageImage);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);
					holder.loaddingMediaProgressBar = (ProgressBar) vi.findViewById(R.id.image_loading_progress_bar);
					break;*/ 
					
				/*case OUTGOING_GIFT_EMOTION_MESSAGE_TYPE:
					vi = inflater.inflate(R.layout.outgoing_gift_emoticon_event_layout, null);
					holder.messageImage = (ImageView)vi.findViewById(R.id.messageImage);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);
					holder.sentStatus = (ImageView) vi
							.findViewById(R.id.sentStatus);
					holder.seenStatus = (ImageView) vi
							.findViewById(R.id.seenStatus);
					break;
				case INCOMING_GIFT_EMOTION_MESSAGE_TYPE:
					vi = inflater.inflate(R.layout.incoming_gift_emo_chat_event_item_layout, null);
					holder.messageImage = (ImageView)vi.findViewById(R.id.messageImage);
					holder.senttime = (TextView) vi
							.findViewById(R.id.sentTimeTxtView);
					break;*/
				case DATE_SEPARATOR:
					vi = inflater.inflate(R.layout.separator_in_chat_layout, arg2, false);
					holder.messageContent = (TextView) vi.findViewById(R.id.dateSeparatorTxtView);
					break;
					
				case TYPING_TYPE:
					vi = inflater.inflate(R.layout.typing_event_item_layout, arg2, false);
					holder.messageContentWrapper = (RelativeLayout) vi.findViewById(R.id.messageWrapper);
					holder.avatarImage = (ImageView) vi.findViewById(R.id.message_profile_image);
					break;
				/*case GROUP_NOTIFICATION:
					vi = inflater.inflate(R.layout.separator_in_chat_layout, null);
					holder.messageContent = (TextView) vi.findViewById(R.id.dateSeparatorTxtView);
					break;*/
				default:
					break;
				}
							vi.setTag(holder);
							
				
			} else
				holder = (ViewHolder) vi.getTag();
			final ChatMessage dataItem = listMessages.get(position);
			
			
			
			
			/*
			 * handle incoming and outgoing message. Check whether message has been sent
			 * 
			 * And set sent time as well
			 * */ 
			if (dataItem.isFromMe() && dataItem.getType() != ChatMessageType.date_separator 
					&& dataItem.getType() != ChatMessageType.typing ) {
				
				//set avarvar 
				if (position > 0 && listMessages.get(position - 1).isFromMe()){
					RelativeLayout.LayoutParams params = (LayoutParams) holder.messageContentWrapper.getLayoutParams();
					params.setMargins(Utils.dp(10), 0, Utils.dp(15), 0);
					if (ChatMessageType.text.equals(dataItem.getType()))
						holder.messageContentWrapper.setBackgroundResource(R.drawable.chat_outgoing_media_states);
					holder.messageContentWrapper.setLayoutParams(params);
				}
				else if (position >= 0){
					if (ChatMessageType.text.equals(dataItem.getType()))
						holder.messageContentWrapper.setBackgroundResource(R.drawable.chat_outgoing_text_states);
					RelativeLayout.LayoutParams params = (LayoutParams) holder.messageContentWrapper.getLayoutParams();
					params.setMargins(Utils.dp(10), Utils.dp(7), Utils.dp(9), 0);
					holder.messageContentWrapper.setLayoutParams(params);
				}
				
				// set sent status
			if (dataItem.isSent()){
				// handle image sent status
					if (ChatMessageType.image == dataItem.getType()){
						holder.sentStatus.setBackgroundResource(R.drawable.msg_check);
						holder.seenStatus.setBackgroundDrawable(null);
					}else 
					{//handle text message, always sent @@
						if (position == getCount() - 1){
							if (isFirstTime){
								holder.sentStatus.setVisibility(View.VISIBLE);
								isFirstTime = false;
							}else {
								holder.sentStatus.setVisibility(View.GONE);
								holder.sentStatus.postDelayed(new Runnable() {
									@Override
									public void run() {
										holder.sentStatus.setVisibility(View.VISIBLE);
									}
								}, 200);
							}
						}else {
							holder.sentStatus.setVisibility(View.VISIBLE);
						}
					}
					
				}
				else{
					holder.sentStatus.setBackgroundResource(R.drawable.msg_clock);
					holder.seenStatus.setBackgroundDrawable(null);
				}
				
				
			}
			
			if (!dataItem.isFromMe() && dataItem.getType() != ChatMessageType.date_separator 					
					&& dataItem.getType() != ChatMessageType.typing ) {
			
				//set avarvar 
				if (dataItem.getType() != ChatMessageType.image && position > 0 && !listMessages.get(position - 1).isFromMe()){
					holder.avatarImage.setVisibility(View.GONE);
					RelativeLayout.LayoutParams params = (LayoutParams) holder.messageContentWrapper.getLayoutParams();
					holder.messageContentWrapper.setBackgroundResource(R.drawable.chat_incoming_text_no_avatar);
					params.setMargins(Utils.dp(50), 0, Utils.dp(60), 0);
					holder.messageContentWrapper.setLayoutParams(params);
				}
				else {
					if (dataItem.getType() != ChatMessageType.image) {
						holder.messageContentWrapper.setBackgroundResource(R.drawable.chat_incoming_text_states);
						holder.avatarImage.setVisibility(View.VISIBLE);
						RelativeLayout.LayoutParams params = (LayoutParams) holder.messageContentWrapper
								.getLayoutParams();
						params.setMargins(0, Utils.dp(7), Utils.dp(60), 0);
						params.addRule(RelativeLayout.RIGHT_OF,
								R.id.message_profile_image);
						holder.messageContentWrapper.setLayoutParams(params);
					}else{
						
						holder.avatarImage.setVisibility(View.VISIBLE);
						RelativeLayout.LayoutParams params = (LayoutParams) holder.messageContentWrapper
								.getLayoutParams();
						params.setMargins(Utils.dp(10), 0, Utils.dp(60), 0);
						params.addRule(RelativeLayout.RIGHT_OF,
								R.id.message_profile_image);
						holder.messageContentWrapper.setLayoutParams(params);
					}

					 int avatarSize = (int) activity.getResources().getDimension(R.dimen.avatar_size);
					 Picasso.with(activity)
				 		.load(Utils.getImageURLForIdLarge(currentUser.getJabberId().split("@")[0].substring(1)) )
				 		.resize(avatarSize, avatarSize)
				 		.centerCrop()
				 		.placeholder(R.drawable.user_placeholder_fb)
				 		.error(R.drawable.user_placeholder_fb)
				 		.into(holder.avatarImage);
					
				}
			}
			
			
			/*
			 * Handle message event type: ChatMessageType
			 * */ 
			switch (dataItem.getType()) {
			case date_separator:
				holder.messageContent.setText(dataItem.getDate());
				break;

			case typing:
				TypingDotsDrawable typingDotAnimation = new TypingDotsDrawable();//(AnimationDrawable) holder.messageContentWrapper.getBackground();
				typingDotAnimation.setIsChat(true);
				holder.messageContentWrapper.setBackgroundDrawable(typingDotAnimation);
				typingDotAnimation.start();
				int avatarSize = (int) activity.getResources().getDimension(R.dimen.avatar_size);
					 Picasso.with(activity)
				 		.load(Utils.getImageURLForIdLarge(currentUser.getJabberId().split("@")[0].substring(1)) )
				 		.resize(avatarSize, avatarSize)
				 		.centerCrop()
				 		.placeholder(R.drawable.user_placeholder_fb)
				 		.error(R.drawable.user_placeholder_fb)
				 		.into(holder.avatarImage);
				
				break;
			case group_notification_separator:
				holder.messageContent.setText(dataItem.getMessageContent());
				break;
			case text:
				/*CharSequence messageString = Emoji.replaceEmoji(Html.fromHtml(String
							.format("<font color=#316f9f>%s:</font> <font color=#316f9f>%s</font>", "name", 
									dataItem.getMessageContent())), messagePaint.getFontMetricsInt(), Utils.dp(20));*/
				
				CharSequence messageString = Emoji.replaceEmoji(dataItem.getMessageContent(), messagePaint.getFontMetricsInt(), Utils.dp(22));
				holder.messageContent.setText("");
				StringBuilder test = new StringBuilder();
//				test.append(messageString);
				test.append(activity.getString(R.string.multiple_space));
				holder.messageContent.append(messageString);
				holder.messageContent.append(test);
				holder.messageContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, Utils.fontSizeLevel * 15);
				//set sent time
				holder.senttime.setText(dataItem.getShortTime());	
				holder.messageContent.setMaxWidth((int) (((float) Utils.displaySize.x *3)/4.0));
				
				break;
			case image:
				holder.messageImage.setMaxWidth((int) (((float) Utils.displaySize.x *3)/4.5 - Utils.dp(10)));
				if (dataItem.getImagePath() != null)
					
					if (dataItem.isFromMe()){
						Picasso.with(activity)
						.load("file://" + dataItem.getImagePath())
						.resize(800, 800).centerInside()
						.placeholder(R.drawable.nophotos)
						.error(R.drawable.nophotos)
						.into(holder.messageImage);
						if (dataItem.isSent()){
							holder.loaddingMediaProgressBar.setVisibility(View.GONE);
							holder.failSentImageView.setVisibility(View.GONE);
						}else {
							// may be it's already fail
							if (dataItem.isSentFail()){
								holder.loaddingMediaProgressBar.setVisibility(View.GONE);
								holder.failSentImageView.setVisibility(View.VISIBLE);
								holder.failSentImageView.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										((ChatActivity)activity).sendImagePathToBackground(dataItem.getImagePath(), dataItem.getLocalId());
										dataItem.setSentFail(false);
										dataItem.setSent(false);
										ChatAdapter.this.notifyDataSetChanged();
										Toast.makeText(activity, "Retry ...", Toast.LENGTH_SHORT).show();
									}
								});
							}
							else {
								holder.loaddingMediaProgressBar.setVisibility(View.VISIBLE);
								holder.failSentImageView.setVisibility(View.GONE);
							}
						}

							
					}
					else
						Picasso.with(activity)
						.load(dataItem.getImagePath())
						.resizeDimen(R.dimen.image_chat_width, R.dimen.image_chat_hight)
						.centerInside()
						.placeholder(R.drawable.nophotos)
						.error(R.drawable.nophotos)
						.into(holder.messageImage, new Callback() {
							
							@Override
							public void onSuccess() {
								holder.messageImage.setVisibility(View.VISIBLE);
								holder.loaddingMediaProgressBar.setVisibility(View.GONE);
								
							}
							@Override
							public void onError() {
								holder.messageImage.setVisibility(View.VISIBLE);
								holder.loaddingMediaProgressBar.setVisibility(View.GONE);
							}
						});
				
				// display avatar
				
				
				holder.senttime.setText(dataItem.getShortTime());
				break;
			}
			
			
			
			
			if (mSelection.get(position)){
				vi.setSelected(true);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					vi.setActivated(true);
				}
			
			}
			else{
				vi.setSelected(false);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					vi.setActivated(false);
				}
			}
			
			
			return vi;
		}

		private class ViewHolder {
			RelativeLayout messageContentWrapper;
			TextView messageContent; // content message
			ImageView messageImage;
			TextView senttime;
			ImageView sentStatus;
			ImageView seenStatus;
			ImageView avatarImage;
			ProgressBar loaddingMediaProgressBar;
			ImageView failSentImageView;
		}

		@Override
		public View getHeaderView(int position, View convertView,
				ViewGroup parent) {
			View view = convertView;
			HeaderHolder holder = null;
			if (view == null) {
				holder = new HeaderHolder();
				view = inflater
						.inflate(R.layout.separator_in_chat_layout, null);
				holder.headerTv = (TextView) view
						.findViewById(R.id.dateSeparatorTxtView);
				view.setTag(holder);
			}
			else
				holder = (HeaderHolder) view.getTag();
			holder.headerTv.setText(listMessages.get(position).getDate());
			return view;
		}

		@Override
		public long getHeaderId(int position) {
			return Utils.getStart(new Date(listMessages.get(position).getSentTime())).getTime();
		}
		
		private class HeaderHolder{
			TextView headerTv;
		}

	}
	/*End of ChatAdapter*/
	

	/*interface handle action select chat events*/
	public interface listSelectable {
		public void addSelection (int position, boolean value);
		
		public void removeSelection (int position );
		
		public void deleteSelectedChatMessages ();
		
		public int getSelectedCount ();
		
		public void emptySelection();
		
		public ArrayList<Integer> getListIdSelected ();

		public void setSesion (ChatSession session);
	}
	
	
	
	
	  private void setTypingAnimation(boolean start) {
	        if (actionBar == null) {
	            return;
	        }
	        if (start) {
	            try {
	            	actionBar.setSubtitle("typing ...");
	            	//        	setSubTitleIcon(0, typingDotsDrawable, Utils.dp(4));
	                typingDotsDrawable.start();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        } else {
	        	actionBar.setSubtitle("");
	            //actionBarLayer.setSubTitleIcon(0, null, 0);
	            if (typingDotsDrawable != null) {
	                typingDotsDrawable.stop();
	            }
	        }
	    }
    
	  
	  
		/*thread in back ground to remove chat events in database*/

		public class RemovingEventsTask extends
				AsyncTask<ArrayList<ChatMessage>, Void, Void> {

			@Override
			protected Void doInBackground(ArrayList<ChatMessage>... params) {
				try {
					if (chatMessageDao == null)
						chatMessageDao = ((FbTextApplication)getApplicationContext()).getHelper().getMessageDao();
					
					for (ChatMessage message : params[0]) {
						QueryBuilder<ChatMessage, Integer> builder = chatMessageDao.queryBuilder();
						builder.where()
								.eq(ChatMessage.MESSAGE_SESSION_ID, currentSession);
						List<ChatMessage> tmpMessages = chatMessageDao.query(builder.prepare());
						
						//get index of the first one in bubble chat
						int index = tmpMessages.indexOf(message);
						
						int amountDelete = message.compsitionLength;
						if (FbTextApplication.isDebug)
						Log.v(LOG_TAG, "compsitionLength: " + amountDelete);
						if (FbTextApplication.isDebug)
						Log.v(LOG_TAG, "index of first one: " + index);
						while (amountDelete > 0){
							amountDelete --;
							chatMessageDao.delete(tmpMessages.get(++index ));
						}
						
						// finally delete the first one
						chatMessageDao.delete(message);
						
					}
					// update session with last message in database
					try {
						QueryBuilder<ChatMessage, Integer> builder = chatMessageDao.queryBuilder();
						builder.where()
								.eq(ChatMessage.MESSAGE_SESSION_ID, currentSession);
						List<ChatMessage> tmpMessages = chatMessageDao.query(builder.prepare());
						
						if (tmpMessages.size() > 0){
							currentSession.setLastMessage(tmpMessages.get(tmpMessages.size() - 1));
						}
						else 
							currentSession.setLastMessage(null);
						chatSessionDao.update(currentSession);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				params[0].clear();
				return null;
			}
			}
	  
	  
	  
	  
		/**
		 * Change the displayed chat.
		 * 
		 * @param contact
		 *            the targeted contact of the new chat
		 * @throws RemoteException
		 *             If a Binder remote-invocation error occurred.
		 */
		private void changeCurrentChat(Contact contact) throws RemoteException {
			if (mChat != null) {
				mChat.setOpen(false);
				mChat.removeMessageListener(mMessageListener);
			}
			mChat = mChatManager.getChat(contact);
			if (mChat != null) {
				mChat.setOpen(true);
				mChat.addMessageListener(mMessageListener);
				mChatManager.deleteChatNotification(mChat);
				//updateOtrInformations(mChat.getOtrStatus());
			}
			mContact = mRoster.getContact(contact.getJID());
			String res = contact.getSelectedRes();
			if (mContact == null)
				mContact = contact;
			if (!"".equals(res)) {
				mContact.setSelectedRes(res);
			}
		}
	  
		
		 
		
		
		
	   /* // handle image 
	     for (String s : msg.getBody().split("\\s+")){
	    	 try {
				URL url = new URL(s);
				if (s.contains("fbcdn-sphotos")){
					final ChatMessage newImageMessage = 
							new ChatMessage("", msg.getTimestamp().getTime(), 
							currentUser, ChatMessageType.image, true);
					newImageMessage.setImagePath(s);
					addMessage2MessageList(newImageMessage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	     }*/
	    
	     
		
		
	    /**
	     * {@inheritDoc}.
	     */
	    private class OnMessageListener extends IMessageListener.Stub {

		/**
		 * Constructor.
		 */
		public OnMessageListener() {
		}

		/**
		 * {@inheritDoc}.
		 */
		@Override
		public void processMessage(IChat chat, final BeemMessage msg) throws RemoteException {
		    final String fromBareJid = StringUtils.parseBareAddress(msg.getFrom());
			if (FbTextApplication.isDebug)
		    Log.v(LOG_TAG, "from Bare Jid" + fromBareJid + "  \t current id" + mContact.getJID());
		   
		    if (mContact.getJID().equals(fromBareJid)) {
			mHandler.post(new Runnable() {

			    @Override
			    public void run() {
				if (msg.getType() == BeemMessage.MSG_TYPE_ERROR) {
					listMessages.add(new ChatMessage("message error", msg.getTimestamp().getTime(), 
							currentUser, ChatMessageType.text, true));
				    chatAdapter.notifyDataSetChanged();
				} else if (msg.getBody() != null) {
					if (FbTextApplication.isDebug)
					Log.v(LOG_TAG, "new message come here");
					if (msg.getBody().contains("fbcdn") || msg.getBody().contains("scontent") || msg.getBody().contains("//cdn.fb")){
						handleImageMessage(msg.getBody());
						return ;
					}
					
					final ChatMessage newMessage = new ChatMessage(msg.getBody(), msg.getTimestamp().getTime(), 
							currentUser, ChatMessageType.text, true);
					newMessage.setSession(currentSession);
					//listMessages.add(newMessage);
					saveMessageTask saveMessage = new saveMessageTask();
					Utils.executeAsyncTask(saveMessage, newMessage);
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
						
							addMessage2MessageList(newMessage);
						}
					});
					
					
				
				}
			    }
			});
		    }
		}
		
		public void handleImageMessage (String messageBody){
			if (!isConnected)
				return;
			SaveImageMessage saveImageTask = new SaveImageMessage();
			Utils.executeAsyncTask(saveImageTask, messageBody);
		}
		


		/**
		 * {@inheritDoc}.
		 */
		@Override
		public void stateChanged(IChat chat) throws RemoteException {
		    final String state = chat.getState();
			if (FbTextApplication.isDebug)
		    Log.v(LOG_TAG, "state changed: " + chat.getState());
		    mHandler.post(new Runnable() {
			@Override
			public void run() {
			    if ("active".equals(state)) {
			    	actionBar.setSubtitle("is online");
			    	removeTypingDot();
			    } else if ("composing".equals(state)) {
		    		actionBar.setSubtitle("is typing ...");
		    		mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							addMessage2MessageList(new ChatMessage("", new Date().getTime(), 
									currentUser, ChatMessageType.typing, false));
						}
					});
		    		
		    		
		    		
			    } else if ("gone".equals(state)) {
			    	actionBar.setSubtitle("is offline");
			    	removeTypingDot();
			    } else if ("inactive".equals(state)) {
			    	actionBar.setSubtitle("is inactive");
			    	removeTypingDot();
			    } else if ("paused".equals(state)) {
			    	actionBar.setSubtitle("is pause");
			    }
			}
		    });

		}

		@Override
		public void otrStateChanged(final String otrState) throws RemoteException {
		    mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "ort state changed", Toast.LENGTH_SHORT).show();;
			    //updateOtrInformations(otrState);
			   // mListMessages.add(new MessageText("", "", otrState, false));
			    //mMessagesListAdapter.notifyDataSetChanged();
			}
		    });

		}
	    }
	  
	  
	  
	    
	    
	    
	    
	    
	    
	    
	    
	    /**
		 * This class is in charge of getting the new chat in the activity if
		 * someone talk to you.
		 */
		private class ChatManagerListener extends IChatManagerListener.Stub {

			/**
			 * Constructor.
			 */
			public ChatManagerListener() {
			}

			@Override
			public void chatCreated(IChat chat, boolean locally) {
				if (locally)
					return;
				try {
					String contactJid = mContact.getJIDWithRes();
					String chatJid = chat.getParticipant().getJIDWithRes();
					if (chatJid.equals(contactJid)) {
						// This should not be happened but to be sure
						if (mChat != null) {
							mChat.setOpen(false);
							mChat.removeMessageListener(mMessageListener);
						}
						mChat = chat;
						mChat.setOpen(true);
						mChat.addMessageListener(mMessageListener);
						mChatManager.deleteChatNotification(mChat);
					}
				} catch (RemoteException ex) {
					if (FbTextApplication.isDebug)
					Log.e(LOG_TAG,"A remote exception occurs during the creation of a chat",
							ex);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	    
	    
		/**
		 * {@inheritDoc}.
		 */
		private final class FacebookTextServiceConnection implements
				ServiceConnection {

			/**
			 * Constructor.
			 */
			public FacebookTextServiceConnection() {
			}

			/**
			 * {@inheritDoc}.
			 */
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mXmppFacade = IXmppFacade.Stub.asInterface(service);
				try {
					mRoster = mXmppFacade.getRoster();
					if (mRoster != null)
						isConnected = true;
						mRoster.addRosterListener(mBeemRosterListener);
					mChatManager = mXmppFacade.getChatManager();
					if (mChatManager != null) {
						mChatManager.addChatCreationListener(mChatManagerListener);
						changeCurrentChat(mContact);
					}
				} catch (RemoteException e) {
					if (FbTextApplication.isDebug)
					Log.e(LOG_TAG, e.getMessage());
				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}

			/**
			 * {@inheritDoc}.
			 */
			@Override
			public void onServiceDisconnected(ComponentName name) {
				isConnected = false;
				mXmppFacade = null;
				try {
					mRoster.removeRosterListener(mBeemRosterListener);
					mChatManager.removeChatCreationListener(mChatManagerListener);
				} catch (RemoteException e) {
					if (FbTextApplication.isDebug)
					Log.e(LOG_TAG, e.getMessage());
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		
		
		
		
		
		/**
		 * {@inheritDoc}.
		 */
		private class BeemRosterListener extends IBeemRosterListener.Stub {

			/**
			 * Constructor.
			 */
			public BeemRosterListener() {
			}

			/**
			 * {@inheritDoc}.
			 */
			@Override
			public void onEntriesAdded(List<String> addresses)
					throws RemoteException {
			}

			/**
			 * {@inheritDoc}.
			 */
			@Override
			public void onEntriesDeleted(List<String> addresses)
					throws RemoteException {
			}

			/**
			 * {@inheritDoc}.
			 */
			@Override
			public void onEntriesUpdated(List<String> addresses)
					throws RemoteException {
			}

			/**
			 * {@inheritDoc}.
			 */
			@Override
			public void onPresenceChanged(final PresenceAdapter presence)
					throws RemoteException {
				
				if (FbTextApplication.isDebug)
				Log.v(LOG_TAG, "presence changed: " + presence.getFrom() + presence.getType());
				if (mContact.getJID().equals(
						StringUtils.parseBareAddress(presence.getFrom()))) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							actionBar.setSubtitle(presence.getType() == PresenceType.AVAILABLE ? R.string.is_online : R.string.is_offline);
						}
					});
				}
				
				
				updateUserState(StringUtils
						.parseBareAddress(presence.getFrom()), presence.getType() == PresenceType.AVAILABLE ? 1 : 0);
			}
			
		}

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	    
	    
	    
	    
	  
	  
	  
	  
	  
	  
	  
	  
    
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 
	 * Handle emoji
	 * 
	 * 
	 * */
	
    public void hideEmojiPopup() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            showEmojiPopup(false);
        }
    }
    public boolean isEmojiPopupShowing() {
        return emojiPopup != null && emojiPopup.isShowing();
    }
	
    private void showEmojiPopup(boolean show) {
        InputMethodManager localInputMethodManager = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show) {
            if (emojiPopup == null) {
                createEmojiPopup();
            }
            int currentHeight;
            WindowManager manager = (WindowManager) getApplicationContext().getSystemService(Activity.WINDOW_SERVICE);
            int rotation = manager.getDefaultDisplay().getRotation();
            if (keyboardHeight <= 0) {
                keyboardHeight = getApplicationContext().getSharedPreferences("emoji", 0).getInt("kbd_height", Utils.dp(200));
                Log.d(LOG_TAG, "keyboardHeight: " + keyboardHeight);
            }
            if (keyboardHeightLand <= 0) {
                keyboardHeightLand = getApplicationContext().getSharedPreferences("emoji", 0).getInt("kbd_height_land3", Utils.dp(200));
                Log.d(LOG_TAG, "keyboardHeightLand: " + keyboardHeightLand);
            }
            if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
                currentHeight = keyboardHeightLand;
            } else {
                currentHeight = keyboardHeight;
            }
        	if (FbTextApplication.isDebug)
            Log.v(LOG_TAG, "popup window hight: " + currentHeight);
            
            emojiPopup.setHeight(View.MeasureSpec.makeMeasureSpec(currentHeight, View.MeasureSpec.EXACTLY));
            if (sizeNotifierRelativeLayout != null) {
                emojiPopup.setWidth(View.MeasureSpec.makeMeasureSpec(Utils.displaySize.x, View.MeasureSpec.EXACTLY));
            }

            emojiPopup.showAtLocation(ChatActivity.this.getWindow().getDecorView(), 83, 0, 0);
            if (!keyboardVisible) {
                if (sizeNotifierRelativeLayout != null) {
                    sizeNotifierRelativeLayout.setPadding(0, 0, 0, currentHeight);
                    emojiButton.setImageResource(R.drawable.ic_msg_panel_hide);
                }
                return;
            }
            emojiButton.setImageResource(R.drawable.ic_msg_panel_kb);
            return;
        }
        if (emojiButton != null) {
            emojiButton.setImageResource(R.drawable.ic_msg_panel_smiles);
        }
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }
        if (sizeNotifierRelativeLayout != null) {
            sizeNotifierRelativeLayout.post(new Runnable() {
                public void run() {
                    if (sizeNotifierRelativeLayout != null) {
                        sizeNotifierRelativeLayout.setPadding(0, 0, 0, 0);
                    }
                }
            });
        }
    }
	
	
	
	
    private void createEmojiPopup() {
        
        emojiView = new EmojiView(ChatActivity.this);
        emojiView.setListener(new EmojiView.Listener() {
            public void onBackspace() {
                messsageEditText.dispatchKeyEvent(new KeyEvent(0, 67));
            }

            public void onEmojiSelected(String paramAnonymousString) {
                int i = messsageEditText.getSelectionEnd();
                CharSequence localCharSequence = Emoji.replaceEmoji(paramAnonymousString, messsageEditText.getPaint().getFontMetricsInt(), Utils.dp(20));
                messsageEditText.setText(messsageEditText.getText().insert(i, localCharSequence));
                int j = i + localCharSequence.length();
                messsageEditText.setSelection(j, j);
            }
        });
        emojiPopup = new PopupWindow(emojiView);
//        emojiPopup.sho

        /*try {
            Method method = emojiPopup.getClass().getMethod("setWindowLayoutType", int.class);
            if (method != null) {
                method.invoke(emojiPopup, WindowManager.LayoutParams.LAST_SUB_WINDOW);
            }
        } catch (Exception e) {
            //don't promt
        }*/
    }
	
	
    @Override
    public void onSizeChanged(int height) {
        Rect localRect = new Rect();
        ChatActivity.this.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);

        WindowManager manager = (WindowManager) getApplicationContext().getSystemService(Activity.WINDOW_SERVICE);
        if (manager == null || manager.getDefaultDisplay() == null) {
            return;
        }
        int rotation = manager.getDefaultDisplay().getRotation();

        if (height > Utils.dp(50) && keyboardVisible) {
            if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
                keyboardHeightLand = height;
                getApplicationContext().getSharedPreferences("emoji", 0).edit().putInt("kbd_height_land3", keyboardHeightLand).commit();
            } else {
                keyboardHeight = height;
                getApplicationContext().getSharedPreferences("emoji", 0).edit().putInt("kbd_height", keyboardHeight).commit();
            }
        }

        if (emojiPopup != null && emojiPopup.isShowing()) {
            int newHeight = 0;
            if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
                newHeight = keyboardHeightLand;
            } else {
                newHeight = keyboardHeight;
            }
            final WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)emojiPopup.getContentView().getLayoutParams();
            if (layoutParams.width != Utils.displaySize.x || layoutParams.height != newHeight) {
                WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                layoutParams.width = Utils.displaySize.x;
                layoutParams.height = newHeight;
                wm.updateViewLayout(emojiPopup.getContentView(), layoutParams);
                if (!keyboardVisible) {
                    sizeNotifierRelativeLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if (sizeNotifierRelativeLayout != null) {
                                sizeNotifierRelativeLayout.setPadding(0, 0, 0, layoutParams.height);
                                sizeNotifierRelativeLayout.requestLayout();
                            }
                        }
                    });
                }
            }
        }

        boolean oldValue = keyboardVisible;
        keyboardVisible = height > 0;
        if (keyboardVisible && sizeNotifierRelativeLayout.getPaddingBottom() > 0) {
            showEmojiPopup(false);
        } else if (!keyboardVisible && keyboardVisible != oldValue && emojiPopup != null && emojiPopup.isShowing()) {
            showEmojiPopup(false);
        }
    }
    
    
    
    
    
    
    
    
	
	
	
	
	
	
	
	
	
	@Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.emojiDidLoaded) {
            if (emojiView != null) {
                emojiView.invalidateViews();
            }
        } 
         else if (id == NotificationCenter.hideEmojiKeyboard) {
            hideEmojiPopup();
        }
    }
	
	
	/*Task to change friend favorite state*/
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public class AddFriendAsFavoriteTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			currentUser.setFavorite(!currentUser.isFavorite());
			try {
				if (userDao == null)
					userDao = ((FbTextApplication)getApplicationContext()).getHelper().getUserDao();
				
				UpdateBuilder<User, String> updateBuilder = userDao.updateBuilder();
				updateBuilder.updateColumnValue(User.USER_FAVORITE_FIELD, currentUser.isFavorite());
				updateBuilder.where().eq(User.USER_JABBER_ID, currentUser.getJabberId());
				
				int rowAffted = 
						userDao.update(updateBuilder.prepare());
				if (FbTextApplication.isDebug)
				Log.v(LOG_TAG, "update favourite for " + rowAffted + " row: " + currentUser.isFavorite());
				//userDao.update(currentUser);
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (result)
				if (currentUser.isFavorite()){
				Toast.makeText(ChatActivity.this, getString(R.string.added) + " " + currentUser.getName() + " " 
						+ getString(R.string.as_favorite), Toast.LENGTH_SHORT).show();
				Toast.makeText(ChatActivity.this, getString(R.string.favorite_description), Toast.LENGTH_SHORT).show();
				}
				else 
					Toast.makeText(ChatActivity.this, getString(R.string.removed) + " " + currentUser.getName() + " " 
							+ getString(R.string.from_favorite_list), Toast.LENGTH_SHORT).show();
			if (Build.VERSION.SDK_INT >= 11)
				invalidateOptionsMenu();
			else 
				supportInvalidateOptionsMenu();
			super.onPostExecute(result);
		}
		
	}
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	public class BlockFriendTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			currentUser.setBlocked(!currentUser.isBlocked());
			try {
				if (userDao == null)
					userDao = ((FbTextApplication)getApplicationContext()).getHelper().getUserDao();
				
				UpdateBuilder<User, String> updateBuilder = userDao.updateBuilder();
				updateBuilder.updateColumnValue(User.USER_BLOCK_FIELD, currentUser.isBlocked());
				updateBuilder.where().eq(User.USER_JABBER_ID, currentUser.getJabberId());
				
				int rowAffted = 
						userDao.update(updateBuilder.prepare());
				if (FbTextApplication.isDebug)
				Log.v(LOG_TAG, "update block for " + rowAffted + " row: " + currentUser.isFavorite());
				
				
				//userDao.update(currentUser);
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (result)
				if (currentUser.isBlocked()){
				Toast.makeText(ChatActivity.this, getString(R.string.added)+ " " + currentUser.getName() + " "
						+ getString(R.string.into_block_list), Toast.LENGTH_SHORT).show();
				Toast.makeText(ChatActivity.this, R.string.block_description, Toast.LENGTH_SHORT).show();
				}
				else 
					Toast.makeText(ChatActivity.this,  getString(R.string.removed)+  " " +currentUser.getName() + " "
							+getString(R.string.from_block_list), Toast.LENGTH_SHORT).show();
			if (Build.VERSION.SDK_INT >= 11)
				invalidateOptionsMenu();
			else 
				supportInvalidateOptionsMenu();
			super.onPostExecute(result);
		}
		
	}
	
	public void setChatBackGround (final int bacgroundId){
		
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if (sizeNotifierRelativeLayout != null && bacgroundId != 0){
					sizeNotifierRelativeLayout.setBackgroundImage(bacgroundId);
					sizeNotifierRelativeLayout.invalidate();
				}
			}
		});
		
	}
	
	public void setChatBackGround (final String bacgroundPath){
		
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if (sizeNotifierRelativeLayout != null && !Utils.isEmpty(bacgroundPath )){
					Target target = new Target() {
						@Override
						public void onPrepareLoad(Drawable arg0) {
					        sizeNotifierRelativeLayout.setBackgroundImage(arg0);
							sizeNotifierRelativeLayout.invalidate();
							Log.e("Change background", "on prepared");
						}
						
						@Override
						public void onBitmapLoaded(Bitmap bitmap, LoadedFrom arg1) {
							Resources res = ChatActivity.this.getResources();
					        BitmapDrawable bd = new BitmapDrawable(res, bitmap);
					        sizeNotifierRelativeLayout.setBackgroundImage(bd);
							sizeNotifierRelativeLayout.invalidate();
							Log.e("Change background", "onBitmapLoaded");
						}
						
						@Override
						public void onBitmapFailed(Drawable arg0) {
							sizeNotifierRelativeLayout.setBackgroundImage(arg0);
							sizeNotifierRelativeLayout.invalidate();
							Log.e("Change background", "onBitmapFailed");
						}
					};
					int width =  ChatActivity.this.getResources().getDisplayMetrics().widthPixels;
					int height =  ChatActivity.this.getResources().getDisplayMetrics().heightPixels;
					Picasso.with(ChatActivity.this).load("file://" + bacgroundPath)
							.placeholder(R.drawable.background_hd)
							.resize(width, height - Utils.dp(45))
							.centerCrop()
							.error(R.drawable.background_hd)
							.into(target);
				}
				else 
					setChatBackGround(R.drawable.background_hd);
			}
		});
		
	}

	public void sendImagePathToBackground (String path, int localId) {
		Log.e(LOG_TAG, "send broadcast with: " + path);
		Intent intent = new Intent();
		intent.putExtra(ChatMessage.MESSAGE_IMAGE_PATH, path);
		intent.putExtra(User.USER_JID_WITH_RES_KEY, mContact.getJIDWithRes());
		intent.putExtra(ChatMessage.MESSAGE_ID_KEY, localId);
		intent.putExtra(User.USER_NAME_FIELD, mContact.getName());
		intent.setAction(FbTextApplication.SEND_IMAGE_MESSAGE);
		sendBroadcast(intent);
	}
	
	
	public void onSendImageFinished (Intent intent) {
		boolean result = intent.getBooleanExtra(FbTextApplication.SEND_IMAGE_RESULT, false);
		
		String jidWithRes = intent.getStringExtra(User.USER_JID_WITH_RES_KEY);
		int id = intent.getIntExtra(ChatMessage.MESSAGE_ID_KEY, 0);
		if (mContact.getJIDWithRes().equals(jidWithRes) && id != 0){
			String fbPath = intent.getStringExtra(ChatMessage.MESSAGE_FB_PHOTO_PATH);
			/*if (result && fbPath != null)
				sendMessage(" sent you a photo " +fbPath, false);*/
			for (ChatMessage message : listMessages){
				if (id == message.getLocalId()){
					message.setSent(result);
					message.setSentFail(!result);
					chatAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
	}
	

	public static void putUserToGetAvatar (String jId, Activity activity){
		/*if (!isConnected)
			return;*/
		if (jId.split("@").length > 2)
			return;
		Intent intent = new Intent();
		 intent.putExtra(User.USER_JABBER_ID, jId);
		 intent.setAction(FbTextApplication.GET_AVATAR);
		 activity.sendBroadcast(intent); 
			if (FbTextApplication.isDebug)
		 Log.v(LOG_TAG, "chat activity wanna avatar for " + jId);
	}
	
	
	public void updateUserState (String jId, int state){
		if (!isConnected)
			return;
		Intent intent = new Intent();
		 intent.putExtra(User.USER_JABBER_ID, jId);
		 intent.putExtra(User.USER_STATE_FIELD, state);
		 intent.setAction(FbTextApplication.UPDATE_USER_STATE);
		 sendBroadcast(intent); 
			if (FbTextApplication.isDebug)
		 Log.d(LOG_TAG, "change state for: " + jId);
	}

	
	
	
	
	
	
	
	@Override
	public void onConnected() {
		isConnected = true;
		mHandler.removeCallbacksAndMessages(null);
		if (!mBinded) {
			bindService(SERVICE_INTENT, mConn, BIND_AUTO_CREATE);
			mBinded = true;
		}
		
		connectionAreaLayout.setVisibility(View.VISIBLE);
		connectionAreaLayout.setBackgroundColor(getResources().getColor(R.color
				.connection_message_background_connected));
    	connectionProgressBar.setVisibility(View.VISIBLE);
    	connectionAreaLayout.bringToFront();
    	connectionMessageTv.setText(R.string.connected);
    	connectionProgressBar.setVisibility(View.GONE);
    	connectionAreaLayout.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				connectionAreaLayout.setBackgroundColor(getResources().getColor(R.color
						.connection_message_background));
				connectionAreaLayout.setVisibility(View.GONE);
			}
		}, 10);
    	if (FbTextApplication.isDebug)
    	Log.v(LOG_TAG, "on connected");
	}

	@Override
	public void onDisconnect() {
		isConnected = false;
		FbTextApplication.isRatable = false;
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				connectionAreaLayout.setVisibility(View.VISIBLE);
		    	connectionProgressBar.setVisibility(View.VISIBLE);
		    	connectionAreaLayout.bringToFront();
		    	connectionMessageTv.setText(R.string.not_connect);
				
			}
		}, 5000);
		
		
    	
    	
    	try {
			if (mChat != null) {
				mChat.setOpen(false);
				mChat.removeMessageListener(mMessageListener);
			}
			if (mRoster != null)
				mRoster.removeRosterListener(mBeemRosterListener);
			if (mChatManager != null)
				mChatManager.removeChatCreationListener(mChatManagerListener);
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
			Log.e(LOG_TAG, e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
		}
		if (mBinded) {
			unbindService(mConn);
			mBinded = false;
		}
		mXmppFacade = null;
		mRoster = null;
		mChat = null;
		mChatManager = null;
		if (FbTextApplication.isDebug)
		Log.v(LOG_TAG, "on onDisconnect");
	}

	@Override
	public void onConnecting() {
		isConnected = false;
		FbTextApplication.isRatable = false;
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				connectionAreaLayout.setVisibility(View.VISIBLE);
		    	connectionProgressBar.setVisibility(View.VISIBLE);
		    	connectionAreaLayout.bringToFront();
		    	connectionMessageTv.setText(R.string.connecting);
			}
		}, 5000);
		
    	if (FbTextApplication.isDebug)
    	Log.v(LOG_TAG, "on onConnecting");
	}

	@Override
	public void onNoInternetConnection() {
		isConnected = false;
		connectionAreaLayout.setVisibility(View.VISIBLE);
    	connectionAreaLayout.bringToFront();
    	connectionMessageTv.setText(R.string.no_internet_connection);
		
    	
    	
    	try {
			if (mChat != null) {
				mChat.setOpen(false);
				mChat.removeMessageListener(mMessageListener);
			}
			if (mRoster != null)
				mRoster.removeRosterListener(mBeemRosterListener);
			if (mChatManager != null)
				mChatManager.removeChatCreationListener(mChatManagerListener);
		} catch (RemoteException e) {
			
			if (FbTextApplication.isDebug)
			Log.e(LOG_TAG, e.getMessage());
		}
    	catch (Exception e) {
			e.printStackTrace();
		}
		if (mBinded) {
			unbindService(mConn);
			mBinded = false;
		}
		mXmppFacade = null;
		mRoster = null;
		mChat = null;
		mChatManager = null;
		
		if (FbTextApplication.isDebug)
		Log.v(LOG_TAG, "on onNoInternetConnection");
	}

	@Override
	public void onConnectionError() {
		isConnected = false;
		connectionAreaLayout.setVisibility(View.VISIBLE);
    	connectionProgressBar.setVisibility(View.VISIBLE);
    	connectionAreaLayout.bringToFront();
    	connectionMessageTv.setText(R.string.not_connect);	
	}
	
    
    
    
    public void setConnectionMessage (String text) {
    	connectionMessageTv.setText(text);
    	
    }
    
    
    public class RenameDialogFragment extends DialogFragment{
    	public RenameDialogFragment (){
    		super();
    	}
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
    		
    		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    		
    		  final EditText nameInputEdt = new EditText(getActivity());  
    		  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
    		                        LinearLayout.LayoutParams.MATCH_PARENT,
    		                        LinearLayout.LayoutParams.MATCH_PARENT);
    		  int margin = Utils.dp(15);
    		  lp.setMargins(margin, margin, margin, margin);
    		  nameInputEdt.setLayoutParams(lp);
    		  nameInputEdt.setText(currentUser.getName() + "");
    		builder.setView(nameInputEdt)
    				.setTitle(R.string.enter_new_name)
    				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String newName = nameInputEdt.getText().toString();
							if (newName == null || newName.length() <= 0){
								// do nothing to force user input again
							}
							else {
								rename(newName);
								dialog.dismiss();
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new AlertDialog.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
    		
    		setRetainInstance(true);
    		
    		return builder.create();
    	}
    }
	
    
    public void rename (String newName){
    	if (!isConnected)
    		return;
    	if (newName != currentUser.getName()){
    		RenameTask renameTask = new RenameTask(newName);
    		Utils.executeAsyncTask(renameTask);
    	}
    }
    
    public class RenameTask extends AsyncTask<String, Void, Boolean> {
    	private String newNAme;
    	public RenameTask (String newName){
    		this.newNAme = newName;
    	}
    	
		@Override
		protected Boolean doInBackground(String... params) {
			if (mRoster != null) {
				try {
					mRoster.setContactName(currentUserId, newNAme);
				} catch (RemoteException e) {
					e.printStackTrace();
					return false;
				}catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			currentUser.setName(newNAme);
			
			UpdateBuilder<User, String> updateBuilder = userDao.updateBuilder();
			try{
				updateBuilder.updateColumnValue(User.USER_NAME_FIELD, newNAme);
				updateBuilder.where().eq(User.USER_JABBER_ID, currentUser.getJabberId());
				userDao.update(updateBuilder.prepare());
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
    	
		@Override
		protected void onPostExecute(Boolean result) {
			if(result)
				actionBar.setTitle(newNAme);
			super.onPostExecute(result);
		}
    }




	@Override
	public void onConnectingIn(int arg0) {
		isConnectingIn = true;
		connectingInSeconds = arg0;
		
		onConnecting();
		
	}
	
	public void handleDisconnectAndLoginAgainFaild(){
		//String password = mSettings.getString(FbTextApplication.ACCOUNT_PASSWORD_KEY, "");
		//boolean mIsAccountConfigured = !TextUtils.isEmpty((password));
		if(FbTextApplication.isDebug)
			Log.v(LOG_TAG, "Fbt application isDisconnected: " + FbTextApplication.isDisconnected);
		if (FbTextApplication.isDisconnected){
			Intent intent = new Intent(ChatActivity.this, Login.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			if(FbTextApplication.isDebug)
				Log.v(LOG_TAG, "application disconnected and we finished: " );
			finish();
		}
			
		
	}
	
	
	
	
	
	/*
	 * For ads
	 * */
	
	
	public static class AdFragment extends Fragment {

        private AdView mAdView;

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
            // values/strings.xml.
            mAdView = (AdView) getView().findViewById(R.id.adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_banner_ads_on_chat_act, container, false);
        }

        /** Called when leaving the activity */
        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        /** Called when returning to the activity */
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        /** Called before the activity is destroyed */
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }

    }

	
	/*
	 * Dialog fragment to sendImage from camera or gallery
	 * */
	public class CameraOrGalleryDialog extends DialogFragment {
		
		public CameraOrGalleryDialog (){
			super();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			android.support.v7.app.AlertDialog.Builder builder = 
					new android.support.v7.app.AlertDialog.Builder(getActivity());
			builder.setItems(R.array.attach_image_choices, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0){
						Intent intent = new Intent(getActivity(), BrowseImageActivity.class);
						getActivity().startActivityForResult(intent, REQUEST_UPLOAD_PHOTO);
					}else {
						takePhoto(REQUEST_TAKE_PHOTO);
					}
				}
			} );
			return builder.create();
		}
	}
	
	
	
	
	

    /**
     * Take a photo here
     * */
    public void takePhoto(int requestCode){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(requestCode);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }


    private File createImageFile(int requestCode) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = new File (Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), ChatActivity.MEDIA_STORAGE_DIR);
        // Create the storage directory if it does not exist
        if (! storageDir.exists()){
            if (! storageDir.mkdirs()){
                Log.d(LOG_TAG, "failed to create directory");
                return null;
            }
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentImagePath = image.getAbsolutePath();

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    
    private class FilterPhotoTask extends AsyncTask<Void, Void, Void>{
    	private int position;
    	private View view;
    	private String firstPath;
    	public FilterPhotoTask (int position, View view, String firstPath) {
    		this.view = view;
    		this.firstPath = firstPath;
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {
			isPhotoFilterRunning = true;
			if (listPhotoPaths == null)
				listPhotoPaths = new ArrayList<String>();
			else 
				listPhotoPaths.clear();
			for (ChatMessage message : listMessages){
				if (ChatMessageType.image.equals(message.getType()))
					listPhotoPaths.add(message.getImagePath());
			}
			this.position = listPhotoPaths.indexOf(firstPath);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			isPhotoFilterRunning = false;
			largerImageView = LargeImageViewFragment.getInstance();
			largerImageView.setParentActivity(ChatActivity.this);
			largerImageView.openPhoto(ChatActivity.this, position, view);
			super.onPostExecute(result);
		}
    	
    } 
    


	@Override
	public int getSize() {
		return listPhotoPaths == null ? 0 : listPhotoPaths.size();
	}



	@Override
	public String getPhotoItem(int position) {
		return listPhotoPaths.get(position);
	}



	@Override
	public void getMorePhoto() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public int getMaxSize() {
		return listPhotoPaths == null ? 0 : listPhotoPaths.size();
	}
	
}
