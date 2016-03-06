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
package co.beem.project.beem.service;

import hoahong.facebook.messenger.FbTextApplication;
import hoahong.facebook.messenger.R;
import hoahong.facebook.messenger.data.User;
import hoahong.facebook.messenger.ui.Utils;
import hoahong.facebook.messenger.ui.android.ChatActivity;
import hoahong.facebook.messenger.ui.android.FbTextMainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.java.otr4j.OtrException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import co.beem.project.beem.FbTextService;
import co.beem.project.beem.service.aidl.IChat;
import co.beem.project.beem.service.aidl.IChatManager;
import co.beem.project.beem.service.aidl.IChatManagerListener;
import co.beem.project.beem.service.aidl.IMessageListener;
import co.beem.project.beem.service.aidl.IRoster;
import co.beem.project.beem.utils.BeemBroadcastReceiver;
import co.beem.project.beem.utils.Status;

import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;

/**
 * An adapter for smack's ChatManager. This class provides functionnality to
 * handle chats.
 * 
 * @author darisk
 */
public class BeemChatManager extends IChatManager.Stub {

	private static final String TAG = "BeemChatManager";
	private final ChatManager mAdaptee;
	private final Map<String, ChatAdapter> mChats = new HashMap<String, ChatAdapter>();
	private final ChatListener mChatListener = new ChatListener();
	private final RemoteCallbackList<IChatManagerListener> mRemoteChatCreationListeners = new RemoteCallbackList<IChatManagerListener>();
	private final FbTextService mService;
	private final Roster mRoster;
	private final ChatRosterListener mChatRosterListn = new ChatRosterListener();

	/**
	 * Constructor.
	 * 
	 * @param chatManager
	 *            the smack ChatManager to adapt
	 * @param service
	 *            the service which runs the chat manager
	 * @param roster
	 *            roster used to get presences changes
	 */
	public BeemChatManager(final ChatManager chatManager,
			final FbTextService service, final Roster roster) {
		mService = service;
		mAdaptee = chatManager;
		mRoster = roster;
		mRoster.addRosterListener(mChatRosterListn);
		mAdaptee.addChatListener(mChatListener);
		mService.setChatManagerReference(BeemChatManager.this);
	}

	@Override
	public void addChatCreationListener(IChatManagerListener listener)
			throws RemoteException {
		if (listener != null)
			mRemoteChatCreationListeners.register(listener);
	}
	
	/**
	 * Create a chat session.
	 * 
	 * @param contact
	 *            the contact you want to chat with
	 * @param listener
	 *            listener to use for chat events on this chat session
	 * @return the chat session
	 */
	@Override
	public IChat createChat(Contact contact, IMessageListener listener) {
		String jid = contact.getJIDWithRes();
		return createChat(jid, listener);
	}

	/**
	 * Create a chat session.
	 * 
	 * @param jid
	 *            the jid of the contact you want to chat with
	 * @param listener
	 *            listener to use for chat events on this chat session
	 * @return the chat session
	 */
	public IChat createChat(String jid, IMessageListener listener) {
		String key = jid;
		ChatAdapter result;
		if (mChats.containsKey(key)) {
			result = mChats.get(key);
			result.addMessageListener(listener);
			return result;
		}
		Chat c = mAdaptee.createChat(key, null);
		// maybe a little probleme of thread synchronization
		// if so use an HashTable instead of a HashMap for mChats
		result = getChat(c);
		result.addMessageListener(listener);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroyChat(IChat chat) throws RemoteException {
		// Can't remove it. otherwise we will lose all futur message in this
		// chat
		// chat.removeMessageListener(mChatListener);
		if (chat == null)
			return;
		deleteChatNotification(chat);
		mChats.remove(chat.getParticipant().getJID());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteChatNotification(IChat chat) {
		try {
			mService.deleteNotification(chat.getParticipant().getJID()
					.hashCode());
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
			Log.v(TAG, "Remote exception ", e);
		}
	}

	/**
	 * Get an existing ChatAdapter or create it if necessary.
	 * 
	 * @param chat
	 *            The real instance of smack chat
	 * @return a chat adapter register in the manager
	 */
	private ChatAdapter getChat(Chat chat) {
		String key = chat.getParticipant();
		if (mChats.containsKey(key)) {
			return mChats.get(key);
		}
		ChatAdapter res = new ChatAdapter(chat);
		boolean history = PreferenceManager.getDefaultSharedPreferences(
				mService.getBaseContext()).getBoolean("settings_key_history",
				false);
		String accountUser = PreferenceManager.getDefaultSharedPreferences(
				mService.getBaseContext()).getString(
				FbTextApplication.ACCOUNT_USERNAME_KEY, "");
		String historyPath = PreferenceManager.getDefaultSharedPreferences(
				mService.getBaseContext()).getString(
				FbTextApplication.CHAT_HISTORY_KEY, "");
		if ("".equals(historyPath))
			historyPath = "/Android/data/co.beem.project.beem/chat/";
		res.setHistory(history);
		res.setAccountUser(accountUser);
		res.listenOtrSession();
		res.setHistoryPath(new File(Environment.getExternalStorageDirectory(),
				historyPath));
		if (FbTextApplication.isDebug)
		Log.d(TAG, "getChat put " + key);
		mChats.put(key, res);
		return res;
	}

	@Override
	public ChatAdapter getChat(Contact contact) {
		String key = contact.getJIDWithRes();
		return mChats.get(key);
	}
	
	public ChatAdapter getChat (String jidWithRes) {
		return mChats.get(jidWithRes);
	}

	/**
	 * This methods permits to retrieve the list of contacts who have an opened
	 * chat session with us.
	 * 
	 * @return An List containing Contact instances.
	 * @throws RemoteException
	 *             If a Binder remote-invocation error occurred.
	 */
	public List<Contact> getOpenedChatList() throws RemoteException {
		List<Contact> openedChats = new ArrayList<Contact>();
		IRoster r = mService.getBind().getRoster();
		for (ChatAdapter chat : mChats.values()) {
			if (chat.getMessages().size() > 0) {
				Contact t = r.getContact(chat.getParticipant().getJID());
				if (t == null)
					t = new Contact(chat.getParticipant().getJID());
				openedChats.add(t);
			}
		}
		return openedChats;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeChatCreationListener(IChatManagerListener listener)
			throws RemoteException {
		if (listener != null)
			mRemoteChatCreationListeners.unregister(listener);
	}

	/**
	 * A listener for all the chat creation event that happens on the
	 * connection.
	 * 
	 * @author darisk
	 */
	public class ChatListener extends IMessageListener.Stub implements
			ChatManagerListener {

		/**
		 * Constructor.
		 */
		public ChatListener() {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void chatCreated(Chat chat, boolean locally) {
			IChat newchat = getChat(chat);
			if (FbTextApplication.isDebug)
			Log.d(TAG, "Chat" + chat.toString() + " created locally " + locally
					+ " with " + chat.getParticipant());
			try {
				newchat.addMessageListener(mChatListener);
				final int n = mRemoteChatCreationListeners.beginBroadcast();

				for (int i = 0; i < n; i++) {
					IChatManagerListener listener = mRemoteChatCreationListeners
							.getBroadcastItem(i);
					listener.chatCreated(newchat, locally);
				}
				mRemoteChatCreationListeners.finishBroadcast();
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing the
				// dead listeners.
				if (FbTextApplication.isDebug)
				Log.w(TAG,
						" Error while triggering remote connection listeners in chat creation",
						e);
			}
		}

		/**
		 * Create the PendingIntent to launch our activity if the user select
		 * this chat notification.
		 * 
		 * @param chat
		 *            A ChatAdapter instance
		 * @return A Chat activity PendingIntent
		 */
		private Builder makeChatIntent(IChat chat) {
			Intent chatIntent = new Intent(mService, ChatActivity.class);
			chatIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			try {
				chatIntent.setData(chat.getParticipant().toUri());
			} catch (RemoteException e) {
				if (FbTextApplication.isDebug)
				Log.e(TAG, e.getMessage());
			}
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(mService);
			stackBuilder.addParentStack(FbTextMainActivity.class);
			stackBuilder.addNextIntent(chatIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mService);
			builder.setContentIntent(resultPendingIntent);
			
			return builder;
			
			/*PendingIntent contentIntent = PendingIntent.getActivity(mService,
					0, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			return contentIntent;*/
		}

		/**
		 * Set a notification of a new chat.
		 * 
		 * @param chat
		 *            The chat to access by the notification
		 * @param msgBody
		 *            the body of the new message
		 */
		private void notifyNewChat(IChat chat, String msgBody) {
			NotificationCompat.Builder notif = makeChatIntent(chat);
			try {
				String contactJid = chat.getParticipant().getJID();
				Contact c = mService.getBind().getRoster()
						.getContact(contactJid);
				String contactName = contactJid;
				if (c != null) {
					contactName = c.getName();
					//Bitmap avatar = getAvatar(c);
					try {
						notif.setLargeIcon(Picasso.with(mService).load(Utils.getImageURLForIdLarge(c.getJID().substring(1).split("@")[0])).get());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				notif.setTicker(contactName).setContentTitle(contactName);
				notif.setContentText(msgBody);
				notif.setSmallIcon(R.drawable.ic_stat_ic_launcher_fbtext);
				notif.setNumber(chat.getUnreadMessageCount());
				notif.setAutoCancel(true).setWhen(System.currentTimeMillis());
				String ringtoneStr = PreferenceManager.getDefaultSharedPreferences(
						mService.getBaseContext()).getString(
						FbTextApplication.CHANGE_RINGTONE_PREF_KEY,
						Settings.System.DEFAULT_NOTIFICATION_URI.toString());
				notif.setSound(Uri.parse(ringtoneStr));
				mService.sendNotification(chat.getParticipant().getJID()
						.hashCode(), notif.build());
			} catch (RemoteException e) {
				if (FbTextApplication.isDebug)
				Log.e(TAG, e.getMessage());
			}
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
			
			File internalFile = mService.getFileStreamPath(c.getJID().substring(1).split("@")[0] + ".png");
	         Uri uri = Uri.fromFile(internalFile);
			
			try {
				InputStream in = mService.getContentResolver().openInputStream(
						uri);
				return BitmapFactory.decodeStream(in);
			} catch (FileNotFoundException e) {
				if (FbTextApplication.isDebug)
				Log.d(TAG, "Error loading avatar id: " + id, e);
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void processMessage(final IChat chat, BeemMessage message) {
			
			//boolean isNotifcaion = PreferenceManager.getDefaultSharedPreferences(mService).getBoolean("notifications_new_message", true);
			String currentId = PreferenceManager.getDefaultSharedPreferences(mService).getString(FbTextApplication.CURRENT_CHAT_ID, "");
			
			
			try {
				boolean isBlock = false;
				try {
					//DatabaseHelper databaseHelper = (DatabaseHelper) OpenHelperManager.getHelper(mService, DatabaseHelper.class);
					Dao<User, String> userDao = ((FbTextApplication)mService.getApplicationContext()).getHelper().getUserDao();
					User thisUser = userDao.queryForId(message.getFrom());
					isBlock = thisUser.isBlocked();
					userDao = null;
					//OpenHelperManager.releaseHelper();
				}catch (Exception e){
					e.printStackTrace();
				}
				String body = message.getBody();
				System.out.println("from: " + message.getFrom());
				if (!chat.isOpen() && body != null) {
					if (chat instanceof ChatAdapter) {
						mChats.put(chat.getParticipant().getJID(),
								(ChatAdapter) chat);
					}
					if (!isBlock && !message.getFrom().equals(currentId)){
						notifyNewChat(chat, body);
						Intent intent = new Intent(BeemBroadcastReceiver.BEEM_NEW_MESSAGE_ARRIVED);
					    intent.putExtra("normally", true);
					    if (mService != null)
					    	mService.sendBroadcast(intent);
					}
					if (mService != null)
						mService.putMessageToSave(message);
					// TODO
				}
			} catch (RemoteException e) {
				if (FbTextApplication.isDebug)
				Log.e(TAG, e.getMessage());
			}
		}

		@Override
		public void stateChanged(final IChat chat) {
		}

		@Override
		public void otrStateChanged(String otrState) throws RemoteException {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * implement a roster listener, is used to detect and close otr chats.
	 * 
	 * @author nikita
	 * 
	 */
	private class ChatRosterListener implements RosterListener {

		@Override
		public void entriesAdded(Collection<String> addresses) {
			 
		}

		@Override
		public void entriesDeleted(Collection<String> arg0) {
		}

		@Override
		public void entriesUpdated(Collection<String> arg0) {
		}

		@Override
		public void presenceChanged(Presence presence) {
			String key = StringUtils.parseBareAddress(presence.getFrom());
			if (!mChats.containsKey(key)) {
				return;
			}

			if (Status.getStatusFromPresence(presence) >= Status.CONTACT_STATUS_DISCONNECT) {
				try {
					mChats.get(key).localEndOtrSession();
				} catch (OtrException e) {
					e.printStackTrace(); 
				}
			}
		}

	}
}
