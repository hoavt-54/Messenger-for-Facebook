package hoahong.facebook.messenger.ui.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import co.beem.project.beem.FbTextService;
import co.beem.project.beem.service.Contact;
import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.data.ChatSession;
import hoahong.facebook.messenger.data.Contactable;
import hoahong.facebook.messenger.data.FbAvatar;
import hoahong.facebook.messenger.data.User;
import hoahong.facebook.messenger.data.UserState;
import hoahong.facebook.messenger.database.DatabaseHelper;
import hoahong.facebook.messenger.ui.Utils;
import hoahong.facebook.messenger.ui.android.UserFragment.UserAdapter.UserViewHolder.HolderClickListener;

public class UserFragment extends Fragment{
	public static final String LOGTAG = "UserFragment";
	
	private LinearLayout loadingScreenLayout;
	private RecyclerView usersListView;
	private TextView userLoadMessageTv;
	private List<User> users;
	private UserAdapter userAdapter;
	private static String mSearchTerm;
	private boolean isSearching;
	private LinearLayout connectionAreaLayout;
	private TextView connectionMessageTv;
	private ProgressBar connectionProgressBar;
	Handler mHandler;
	private boolean isLoading;
	Picasso picasso;
	public UserFragment() {
		
	}
	
	public static UserFragment newInstance (){
		UserFragment fFragment = new UserFragment();
		return fFragment;
	}
	
	
	private Dao<User, String> userDao;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_users_layout, null);
		mHandler = new Handler();
		connectionAreaLayout = (LinearLayout) view.findViewById(R.id.connection_area);
		connectionProgressBar = (ProgressBar) view.findViewById(R.id.connection_progressbar);
		connectionMessageTv = (TextView) view.findViewById(R.id.connection_message_tv);
 		loadingScreenLayout = (LinearLayout) view.findViewById(R.id.user_fragment_loading_screen);
		usersListView = (RecyclerView) view.findViewById(R.id.users_list_view);
		usersListView.setLayoutManager(new LinearLayoutManager(getActivity()));
		userLoadMessageTv = (TextView) view.findViewById(R.id.user_fragment_error_tv);
		Picasso.Builder picassoBuilder = new Picasso.Builder(getActivity());

		// add our custom eat foody request handler (see below for full class)
		picassoBuilder.addRequestHandler(new FbImageRequestHandler());

		// Picasso.Builder creates the Picasso object to do the actual requests
		picasso = picassoBuilder.build();
		return view;
	}
	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (users == null) {
			users = new ArrayList<User>();
			LoadUserTask loadUserTask = new LoadUserTask();
			Utils.executeAsyncTask(loadUserTask);
		}
		userAdapter = new UserAdapter(getActivity(), users, new HolderClickListener() {
			
			@Override
			public void onChatClick(View v, int position) {
				Contactable contact = users.get(position);
				if (contact instanceof User)
					new GetSessionChatId(getMainActivity(), (User)users.get(position))
					.execute();
				else 
					new GetSessionChatId(getMainActivity(), new User(contact.getJabberId(), contact.getName(), "")).execute();;
			}

			@Override
			public void onAvatarClick(View v, int position) {
				Contactable contact = users.get(position);
				showImage(contact.getJabberId().split("@")[0].substring(1));
				
			}
		});
		usersListView.setAdapter(userAdapter);
		setRetainInstance(true);
		super.onViewCreated(view, savedInstanceState);
	}
	public void showImage(String id) {
	    Dialog builder = new Dialog(getActivity());
	    builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    builder.getWindow().setBackgroundDrawable(
	        new ColorDrawable(android.graphics.Color.TRANSPARENT));
	    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
	        @Override
	        public void onDismiss(DialogInterface dialogInterface) {
	            //nothing;
	        }
	    });

	    LinearLayout dialog = (LinearLayout) LayoutInflater
	    		.from(getActivity()).inflate(R.layout.contact_view_bagde, null);
	    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT);
	    dialog.setLayoutParams(lp);
	    //dialog.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
	    ImageView imageView = (ImageView) dialog.findViewById(R.id.contact_bagde_imageview);
	    picasso.load(FbImageRequestHandler.FACEBOOK_REQUEST_SCHEME + "://" + id)
	    		.fit()
	    		.centerCrop()
	    		.placeholder(R.drawable.user_placeholder_fb)
		 		.error(R.drawable.user_placeholder_fb)
		 		.into(imageView);
	    /*LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
	    imageView.setLayoutParams(lp);*/
	    builder.setContentView(dialog);
	    builder.show();
	}
	public class FbImageRequestHandler extends RequestHandler{
		public static final String FACEBOOK_REQUEST_SCHEME = "facebook";
		
		@Override
		public boolean canHandleRequest(Request arg0) {
			Log.e(FACEBOOK_REQUEST_SCHEME, arg0.uri.getScheme());
			return FACEBOOK_REQUEST_SCHEME.equals(arg0.uri.getScheme());
		}

		@Override
		public Result load(Request request, int arg1) throws IOException {
			String imageKey = request.uri.getHost();
			Log.e(FACEBOOK_REQUEST_SCHEME, imageKey);
			URL fbPhoto = new URL(String.format("https://graph.facebook.com/%s?fields=picture.width(%s).height(%s)", imageKey, Utils.dp(230), Utils.dp(200)));
			Log.e(FACEBOOK_REQUEST_SCHEME, fbPhoto.getPath());
            HttpURLConnection urlConnection = (HttpURLConnection) fbPhoto.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            String postDetailString = "";
            if (null != inputStream)
            	postDetailString = FbTextService.getStringFromInputStream(inputStream);
            if (postDetailString != null && postDetailString.length() > 1){
            	FbAvatar photo = new Gson().fromJson(postDetailString, FbAvatar.class);
            	if (photo != null && photo.getPicture() != null){
            		Bitmap bm = Utils.getImage(photo.getPicture().getData().url);
            		return new Result(bm, Picasso.LoadedFrom.NETWORK);
            	}
            }
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_placeholder_fb);
            return new Result(bitmap, Picasso.LoadedFrom.DISK);
		}
		
	}
	
	
	/*
	 * get reference to main activity
	 * */
	public FbTextMainActivity getMainActivity(){
		return (FbTextMainActivity) getActivity();
	}
	/*
	 * get database helper from main activity
	 * */
	public DatabaseHelper getDatabaseHelper (){
		return ((FbTextApplication)getActivity().getApplicationContext()).getHelper();
	}
	
	
	
	public void onFirstTime (final List<Contact> contacts){
		UserAdapter contactAdapter = new UserAdapter(getMainActivity(), contacts, new HolderClickListener() {
			
			@Override
			public void onChatClick(View v, int position) {
				Contact contact = contacts.get(position); 
				new GetSessionChatId(getMainActivity(), new User(contact.getJabberId(), contact.getName(), "")).execute();;
			}

			@Override
			public void onAvatarClick(View v, int position) {
				Contact contact = contacts.get(position);
				showImage(contact.getJabberId().split("@")[0].substring(1));
				
			}
		});
		usersListView.setAdapter(contactAdapter);
	}
	
	/*
	 * General reload listview from database
	 * 
	 * */
	void reloadListUser (){
		if (isLoading) return;
		LoadUserTask loadUserTask = new LoadUserTask();
		Utils.executeAsyncTask(loadUserTask);
	}
	

	void onUserOnline (String jid) {
		if (jid != null && users != null && users.size() > 0){
			AsyncTask<String, Void, Void> changeOnlineUserTask = new AsyncTask<String, Void, Void>(){

				@Override
				protected Void doInBackground(String... params) {
					String jid = params[0];
					for (User u : users)
						if (jid.equals(u.getJabberId())){
							u.setState(UserState.available);
							break;
						}
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					userAdapter.notifyDataSetChanged();
					super.onPostExecute(result);
				}
				
			};
			Utils.executeAsyncTask(changeOnlineUserTask, jid);
		}
	}
	
	
	
	/*
	 * when user do a search on list friend, 
	 * we display a list view with hightlight matched name
	 * */ 
	public void onSearchQuery (String searchQuery){
		this.mSearchTerm = searchQuery;
		if (mSearchTerm == null || mSearchTerm.length() < 1)
			isSearching = false;
		else isSearching = true;
		LoadUserTask loadUserTask = new LoadUserTask();
		Utils.executeAsyncTask(loadUserTask);
	}
	
	
	
	public static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

		private Activity activity;
		private LayoutInflater inflater;
		private List<? extends Contactable> users;
		private TextAppearanceSpan highlightTextSpan;
		private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
		private HolderClickListener mListener;
		private boolean isLoadAvatar;
		
		public UserAdapter(Activity activity, List<?extends Contactable> users, HolderClickListener listener){
			this.activity = activity;
			this.users = users;
			this.mListener = listener;
			isLoadAvatar = PreferenceManager.getDefaultSharedPreferences(activity)
				 	.getBoolean(FbTextApplication.LOAD_AVATAR_KEY, true);
			inflater = LayoutInflater.from(activity);
			highlightTextSpan = new TextAppearanceSpan(activity, R.style.searchTextHiglight);
		}
		
		/**
         * Identifies the start of the search string in the display name column of a Cursor row.
         * E.g. If displayName was "Adam" and search query (mSearchTerm) was "da" this would
         * return 1.
         *
         * @param displayName The contact display name.
         * @return The starting position of the search string in the display name, 0-based. The
         * method returns -1 if the string is not found in the display name, or if the search
         * string is empty or null.
         */
        private int indexOfSearchQuery(String displayName) {
            if (!TextUtils.isEmpty(mSearchTerm)) {
                return displayName.toLowerCase(Locale.getDefault()).indexOf(
                        mSearchTerm.toLowerCase(Locale.getDefault()));
            }
            return -1;
        }

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		
		public static class UserViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
			public UserViewHolder(View itemView, HolderClickListener listener) {
				super(itemView);
				itemView.setOnClickListener(this);
				this.avatar = (ImageView) itemView.findViewById(R.id.user_avatar_imagev);
				this.avatar.setOnClickListener(this);
				this.userName = (TextView) itemView.findViewById(R.id.user_name_tv);
				this.stateIndicator = (View) itemView.findViewById(R.id.user_state_indicator);
				this.mListener = listener;
			}
			ImageView avatar;
			TextView userName;
			View stateIndicator;
			HolderClickListener mListener;
			
			
			public static interface HolderClickListener {
				public void onChatClick(View v, int position);
				public void onAvatarClick(View v, int position);
			}


			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.user_avatar_imagev)
					mListener.onAvatarClick(v, getPosition());
				else mListener.onChatClick(v, getPosition());
			}
		}

		@Override
		public int getItemCount() {
			return users == null ? 0 : users.size();
		}

		@Override
		public void onBindViewHolder(UserViewHolder holder, int position) {
			Contactable user = users.get(position);
			// setData
			String userName = user.getName();
			 final int startIndex = indexOfSearchQuery(userName);
			 if (startIndex == -1){
				 // user doesnt do a search or not match
				 holder.userName.setTextSize(TypedValue.COMPLEX_UNIT_SP, Utils.fontSizeLevel * 16);
				 holder.userName.setText(userName + "");
			 }else {
				// Wraps the display name in the SpannableString
	                final SpannableString highlightedName = new SpannableString(userName);

	                // Sets the span to start at the starting point of the match and end at "length"
	                // characters beyond the starting point
	                highlightedName.setSpan(highlightTextSpan, startIndex,
	                        startIndex + mSearchTerm.length(), 0);

	                // Binds the SpannableString to the display name View object
	                holder.userName.setText(highlightedName);
			 }

			 int avatarSize = (int) activity.getResources().getDimension(R.dimen.avatar_size);
			 if(isLoadAvatar)
			 Picasso.with(activity)
			 		.load(Utils.getImageURLForIdLarge(user.getJabberId().split("@")[0].substring(1)) )
			 		.resize(avatarSize, avatarSize)
			 		.centerCrop()
			 		.placeholder(R.drawable.user_placeholder_fb)
			 		.error(R.drawable.user_placeholder_fb)
			 		.into(holder.avatar);
		
			if (UserState.available == user.getState())
				holder.stateIndicator.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.online_user_indicator));
			else if (UserState.unavailable == user.getState())
				holder.stateIndicator.setBackgroundDrawable(null);
		}

		@Override
		public UserViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
			// create a new view
	        View v = inflater.inflate(R.layout.user_item_in_listview_layout, arg0, false);
	        UserViewHolder vh = new UserViewHolder(v, mListener);
	        return vh;
		}
		
		
	}
	
	
	/*
	 * Task on background to load users from database
	 * 
	 * */
	
	public class LoadUserTask extends AsyncTask<Void, Void, Boolean>{
		
		@Override
		protected void onPreExecute() {
			loadingScreenLayout.setVisibility(View.VISIBLE);
			isLoading = true;
			if (usersListView != null && usersListView.getAdapter() != userAdapter)
				usersListView.setAdapter(userAdapter);
			super.onPreExecute();
		} 
		
		@Override
		protected void onProgressUpdate(Void... values) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					userAdapter.notifyDataSetChanged();

				}
			});
			super.onProgressUpdate(values);
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
				if (!isAdded()) return false;
				Dao<User, String> userDao = UserFragment.this.getDatabaseHelper().getUserDao();
				if (mSearchTerm == null){
					
					QueryBuilder<User, String> builder = userDao.queryBuilder();
					builder.where().ne(User.USER_NAME_FIELD, "Facebook User");
					List<User> loadedUsers = userDao.query(builder.prepare());
					
					if (loadedUsers.size() > 0){
						users.clear();
						users.addAll(loadedUsers);
					}
				}
				else {
					publishProgress(null);
					users.clear();
					QueryBuilder<User, String> builder = userDao.queryBuilder();
					builder.where().like(User.USER_NAME_FIELD, "%" + mSearchTerm + "%")
								.and().ne(User.USER_NAME_FIELD, "Facebook User");
					users.addAll(userDao.query(builder.prepare()));
				}
				Collections.sort(users, new Comparator<User>() {
					@Override
					public int compare(User lhs, User rhs) {
						float point1 = lhs.getPoint();
						float point2 = rhs.getPoint();
						if (point1 > point2)
							return -1;
						else if (point2 > point1)
							return 1;
						else return 0;
					}
				});
				
				
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			isLoading = false;
			if (!isAdded()) return;
			loadingScreenLayout.setVisibility(View.GONE);
			if (users.size() == 0){
				
				if (isSearching){
					userLoadMessageTv.setVisibility(View.VISIBLE);
					userLoadMessageTv.setText(R.string.no_result);
				}
					
			}
			else{
				userLoadMessageTv.setVisibility(View.GONE);
			}
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					userAdapter.notifyDataSetChanged();
					
				}
			});
			
			super.onPostExecute(result);
		}
	}
	
	
	public class GetSessionChatId extends AsyncTask<User, Void, Integer> {
		ChatSession thisSession;
		Activity activity;
		User friend;

		public GetSessionChatId(Activity context, User friend) {
			this.activity = context;
			this.friend = friend;
		}

		@Override
		protected Integer doInBackground(User... params) {
			
			
			List<ChatSession> listSessionsWithThisFriend = null;
			Dao<ChatSession, Integer> chatSessionDao = null;
			try {
				chatSessionDao = getDatabaseHelper().getChatSessionDao();
				listSessionsWithThisFriend = chatSessionDao
						.query(chatSessionDao.queryBuilder().where()
								.eq(ChatSession.USER_NAME_FIELD, friend)
								.prepare());
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			/*
			 * if there no session chat exists, this is the first time chat with
			 * this contact We create new one
			 */
			if (listSessionsWithThisFriend == null
					|| listSessionsWithThisFriend.size() == 0) {
				thisSession = new ChatSession(friend);
				try {
					if (chatSessionDao == null)
						chatSessionDao = getDatabaseHelper().getChatSessionDao();
					chatSessionDao.create(thisSession);
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}// else take the first session found with this friend
			else {
				thisSession = listSessionsWithThisFriend.get(0);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Intent intent = new Intent(activity, ChatActivity.class);
			intent.putExtra(ChatSession.SESSION_CHAT_ID_KEY,
					thisSession.getLocalId());
			intent.setData(friend.toUri());
			/*intent.putExtra(ChatSessionAdapter.SESSION_NAME_KEY, thisSession
					.getSessionPartner().getName());*/
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getActivity().startActivityForResult(intent,
					FbTextMainActivity.START_CHAT_REQUEST_CODE);
			super.onPostExecute(result);
		}
	}
	
	
	
	
	public void onFriendEntryAdded (List<User> users){
		users.addAll(users);
		CreateNewlyAddedContactTask createUserTask = new CreateNewlyAddedContactTask(users);
		Utils.executeAsyncTask(createUserTask);
	}
	
	
	
	public void onFriendEntryDeleted(List<User> deletedUsers) {
		//this modify users which is not thread-safe
		/*for (User user : users) {
			int index = users.indexOf(user);
			if (index > -1) {
				users.remove(index);
				
			} else {
				Log.e(LOGTAG, "deleted user not found in the list");
			}
		}*/
		
		DeleteContactTask deleteTask = new DeleteContactTask(users);
		Utils.executeAsyncTask(deleteTask);
	}
	
	
	
	
	 public class CreateNewlyAddedContactTask extends AsyncTask<Void, Void, Void>{
	    	private List<User> users;
	    	public CreateNewlyAddedContactTask (List<User> users){
	    		this.users = users;
	    	}
			@Override
			protected Void doInBackground(Void... params) {
				
				
				
				return null;
			}
	    	
			
			@Override
			protected void onPostExecute(Void result) {
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						
					}
				});
				super.onPostExecute(result);
			}
	    }
	    
	    public class DeleteContactTask extends AsyncTask<Void, Void, Void>{
	    	private List<User> users;
	    	public DeleteContactTask (List<User> users){
	    		this.users = users;
	    	}
			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					if (userDao == null)
						userDao = getDatabaseHelper().getUserDao();
					userDao.delete(users);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}
	    	
			
			@Override
			protected void onPostExecute(Void result) {
				reloadListUser();
				super.onPostExecute(result);
			}
	    }
	    
	    public void diplayProgressBar (){
	    	loadingScreenLayout.setVisibility(View.VISIBLE);
	    }
	    
	    public void removeProgressBar (){
	    	loadingScreenLayout.setVisibility(View.GONE);
	    }
	    
	    
	    
	    
	    
	    public void onDisconnected(){/*
	    	try{
	    		mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						connectionAreaLayout.setVisibility(View.VISIBLE);
				    	connectionProgressBar.setVisibility(View.VISIBLE);
				    	connectionAreaLayout.bringToFront();
				    	connectionMessageTv.setText(R.string.not_connect);
					}
				}, 6000);
		    	
	    	}catch (Exception e){
	    		e.printStackTrace();
	    	}*/
	    }
	    
	    public void onConnected () {
	    	mHandler.removeCallbacksAndMessages(null);
	    	connectionAreaLayout.setVisibility(View.VISIBLE);
	    	connectionAreaLayout.setBackgroundColor(getActivity()
	    						.getResources().getColor(R.color
	    						.connection_message_background_connected));
	    	connectionProgressBar.setVisibility(View.VISIBLE);
	    	connectionAreaLayout.bringToFront();
	    	connectionMessageTv.setText(R.string.connected);
	    	connectionProgressBar.setVisibility(View.GONE);
	    	connectionAreaLayout.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					connectionAreaLayout.setBackgroundColor(getActivity()
    						.getResources().getColor(R.color
    						.connection_message_background));
					connectionAreaLayout.setVisibility(View.GONE);
					
				}
			}, 100);
	    }
	    
	    public void onConnecting (){/*
	    	
	    	
	    	
	    	try{
	    		mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						connectionAreaLayout.setVisibility(View.VISIBLE);
				    	connectionProgressBar.setVisibility(View.VISIBLE);
				    	connectionAreaLayout.bringToFront();
				    	connectionMessageTv.setText(R.string.connecting);
					}
				}, 6000);
		    	
	    	}catch (Exception e){
	    		e.printStackTrace();
	    	}
	    	*/
	    }
	    
	    
	    public void onNoInternetConnection (){
	    	try{
	    		mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						connectionAreaLayout.setVisibility(View.VISIBLE);
				    	connectionAreaLayout.bringToFront();
				    	connectionMessageTv.setText(R.string.no_internet_connection);
					}
				}, 10);
		    	
	    	}catch (Exception e){
	    		e.printStackTrace();
	    	}
	    	
	    }
	    
	    
	    
	    public void setConnectionMessage (String text) {
	    	connectionMessageTv.setText(text);
	    	
	    }
	    
}
