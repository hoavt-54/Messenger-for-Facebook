package hoahong.facebook.messenger.ui.android;

import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.data.ChatMessage;
import hoahong.facebook.messenger.data.ChatSession;
import hoahong.facebook.messenger.data.User;
import hoahong.facebook.messenger.database.DatabaseHelper;
import hoahong.facebook.messenger.ui.Utils;
import hoahong.facebook.messenger.ui.android.ChatFragment.ChatSessionAdapter.SessionViewHolder.HolderClickListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

public class ChatFragment extends Fragment {

	
	private RecyclerView chatsListView;
	private List<ChatSession> listChatSessions;
	private ChatSessionAdapter sessionAdapter;
	private String mSearchTerm;
	private boolean isSearching;
	private LinearLayout connectionAreaLayout;
	private TextView connectionMessageTv;
	private ProgressBar connectionProgressBar;
	private LinearLayout loadingScreenLayout;
	private TextView listChatMessage;
	private Handler mHandler;
	
	public ChatFragment() {
		
	}
	
	public static ChatFragment newInstance (){
		ChatFragment fFragment = new ChatFragment();
		return fFragment;
	}
	
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// setup connection first
		mHandler = new Handler();
		
		View view = inflater.inflate(R.layout.fragment_chat_lists_layout, null);
		connectionAreaLayout = (LinearLayout) view.findViewById(R.id.connection_area);
		connectionProgressBar = (ProgressBar) view.findViewById(R.id.connection_progressbar);
		connectionMessageTv = (TextView) view.findViewById(R.id.connection_message_tv);
		loadingScreenLayout = (LinearLayout) view.findViewById(R.id.user_fragment_loading_screen);
		listChatMessage = (TextView) view.findViewById(R.id.chats_fragment_welcome_message);
		
		chatsListView = (android.support.v7.widget.RecyclerView) view.findViewById(R.id.chats_listview);
		chatsListView.setLayoutManager(new LinearLayoutManager(getActivity()));
		return view;
	}
	
	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (listChatSessions == null ){
			listChatSessions = new ArrayList<ChatSession>();
			LoadChatSessionTask loadTask = new LoadChatSessionTask();
			Utils.executeAsyncTask(loadTask);
		}
		sessionAdapter = new ChatSessionAdapter(listChatSessions, getActivity(), new HolderClickListener() {
			
			@Override
			public void onClick(View v, int position) {
				Intent intent = new Intent(getActivity(), ChatActivity.class);
				ChatSession session = listChatSessions.get(position);
				intent.putExtra(ChatSession.SESSION_CHAT_ID_KEY,
						session.getLocalId());
				User user = session.getUser();
				if (user != null)
				intent.setData(user.toUri());
				getActivity().startActivityForResult(intent,
						FbTextMainActivity.START_CHAT_REQUEST_CODE);
			}
		});
		chatsListView.setAdapter(sessionAdapter);
		setRetainInstance(true);
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
	
	
	/*
	 * method to refresh chat session list
	 * */
	public void refreshChatList (){
		LoadChatSessionTask loadTask = new LoadChatSessionTask();
		Utils.executeAsyncTask(loadTask);
	}
	
	
	
	
	
	public static class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.SessionViewHolder> {

		private List<ChatSession> listSessions;
		private LayoutInflater inflater;
		private Activity activity;
		private HolderClickListener mListener;
		private boolean isLoadAvatar;
		
		public ChatSessionAdapter (List<ChatSession> listSessions, Activity activity, HolderClickListener listener){
			this.listSessions = listSessions;
			this.activity = activity;
			this.mListener = listener;
			isLoadAvatar = PreferenceManager.getDefaultSharedPreferences(activity)
				 	.getBoolean(FbTextApplication.LOAD_AVATAR_KEY, true);
			inflater = LayoutInflater.from(activity.getApplicationContext());
		}
		
		public int getCount() {
			
			return listSessions.size();
		}

		public ChatSession getItem(int position) {
			
			return listSessions.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}
		
		
		public static class SessionViewHolder  extends RecyclerView.ViewHolder implements OnClickListener{
			public SessionViewHolder(View view, HolderClickListener listener) {
				super(view);
				view.setOnClickListener(this);
				this.sessionAvatar = (RoundedImageView) view.findViewById(R.id.chat_session_avatar);
				this.sessionName = (TextView) view.findViewById(R.id.chat_session_friend_name);
				this.lastMessageStatus = (ImageView) view.findViewById(R.id.chat_session_last_message_status_imgview);
				this.sessionSnipet = (TextView) view.findViewById(R.id.chat_session_last_message_content_tv);
				this.timeTxtview = (TextView) view.findViewById(R.id.chat_sessiontimeTxtView);
				this.unreadMessageTv = (TextView) view.findViewById(R.id.chat_session_unread_message);
				this.mListener = listener;
			}
			TextView sessionSnipet;
			RoundedImageView sessionAvatar;
			TextView sessionName;
			TextView timeTxtview;
			ImageView lastMessageStatus;
			TextView unreadMessageTv;
			HolderClickListener mListener;
			
			public static interface HolderClickListener {
				public void onClick(View v, int position);
			}

			@Override
			public void onClick(View v) {
				mListener.onClick(v, getPosition());
				
			}
		}


		@Override
		public int getItemCount() {
			return this.listSessions == null ? 0 : this.listSessions.size(); 
		}

		@Override
		public void onBindViewHolder(SessionViewHolder holder, int position) {
			// set data
						ChatSession data = getItem(position);
						User user = data.getUser();
						ChatMessage lastMessage = data.getLastMessage();
						
						//set name
						if (user != null)
						holder.sessionName.setText(user.getName()+"");
						
							 int avatarSize = (int) activity
									 .getApplicationContext().getResources()
									 .getDimension(R.dimen.avatar_size);
							 if(isLoadAvatar && user.getJabberId() != null)
							 Picasso.with(activity)
						 		.load(Utils.getImageURLForIdLarge(user.getJabberId().split("@")[0].substring(1)) )
						 		.resize(avatarSize, avatarSize)
						 		.centerCrop()
						 		.placeholder(R.drawable.user_placeholder_fb)
						 		.error(R.drawable.user_placeholder_fb)
						 		.into(holder.sessionAvatar);
						if (lastMessage != null) {
							holder.sessionSnipet.setText(lastMessage.getShortText() + "");
							holder.timeTxtview.setText(lastMessage.getTimeOfLastMessage() + "");
							if (lastMessage.isFromMe()){
								holder.lastMessageStatus.setVisibility(View.VISIBLE);
								if (lastMessage.isSent())
									holder.lastMessageStatus.setImageResource(R.drawable.msg_check);
								else
									holder.lastMessageStatus.setImageResource(R.drawable.msg_clock);
							}
							else
								holder.lastMessageStatus.setImageResource(R.drawable.abc_ic_go_search_api_mtrl_alpha);
						}
						else{ 
							holder.sessionSnipet.setText(R.string.no_message);
							holder.timeTxtview.setText(R.string.no_message);
							holder.lastMessageStatus.setVisibility(View.INVISIBLE);
						}
						
						// set number of unread message
						if (data.getNumberOfUnreadMessages() > 0){
							holder.unreadMessageTv.setVisibility(View.VISIBLE);
							holder.unreadMessageTv.setText(data.getNumberOfUnreadMessages() + "");
						}
						else 
							holder.unreadMessageTv.setVisibility(View.GONE);
						
		}

		@Override
		public SessionViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
			View v = inflater.inflate(R.layout.chatsession_itemt_in_listview, arg0, false);
			return new SessionViewHolder(v, mListener);
		} 
		
	} 
	
	
	
	// Load chat session task
	public class LoadChatSessionTask extends AsyncTask<Void, Void, List<ChatSession>>{

		@Override
		protected void onPreExecute() {
			try {
				listChatMessage.setVisibility(View.GONE);
				loadingScreenLayout.setVisibility(View.VISIBLE);
			}catch (Exception w){
				w.printStackTrace();
			}		
			super.onPreExecute();
			
			if (listChatSessions != null && listChatSessions.size() > 0)
				listChatSessions.clear();
		}
		
		@Override
		protected  List<ChatSession> doInBackground(Void... params) {
			
			List<ChatSession> tmpList = null;
			
			try {
				QueryBuilder<ChatSession, Integer> builder = ((FbTextApplication)
						getActivity().getApplicationContext()).getHelper().getChatSessionDao().queryBuilder();
				builder.where()
						.isNotNull(ChatSession.LAST_MESSAGE_FIELD)
						.and().isNotNull(ChatSession.USER_NAME_FIELD);
				 tmpList = ((FbTextApplication)getActivity().getApplicationContext())
						 .getHelper().getChatSessionDao().query(builder.prepare());
				// count number of unread messages
				//for ()
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			Collections.sort(tmpList, new Comparator<ChatSession>() {

				@Override
				public int compare(ChatSession lhs, ChatSession rhs) {
					long dateDiff = lhs.getLastMessage().getSentTime() - rhs.getLastMessage().getSentTime();
					if (dateDiff == 0)
						return 0;
					else if (dateDiff > 0)
						return -1;
					else return 1;
				}
			});
			return tmpList;
		}
		
		@Override
		protected void onPostExecute(List<ChatSession> result) {
			try{
			loadingScreenLayout.setVisibility(View.GONE);
			if (result != null && result.size() > 0){
				listChatSessions.addAll(result);
				sessionAdapter.notifyDataSetChanged();
				
			}else {
				listChatMessage.setVisibility(View.VISIBLE);
				
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
			
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
		}, 0);
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
    
    
    public void onNoInternetConnection (){/*
    	
    	try{
    		mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					connectionAreaLayout.setVisibility(View.VISIBLE);
			    	connectionAreaLayout.bringToFront();
			    	connectionMessageTv.setText(R.string.no_internet_connection);
				}
			}, 30);
	    	
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    	
    	*/
    	
    	
    }
    
	
	
}
