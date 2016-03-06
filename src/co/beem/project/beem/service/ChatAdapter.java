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
import hoahong.facebook.messenger.data.ChatMessage;
import hoahong.facebook.messenger.ui.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import co.beem.project.beem.otr.BeemOtrManager;
import co.beem.project.beem.service.aidl.IChat;
import co.beem.project.beem.service.aidl.IMessageListener;
import net.java.otr4j.OtrException;
import net.java.otr4j.session.SessionID;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.ChatStateListener;

/**
 * An adapter for smack's Chat class.
 * @author darisk
 */
public class ChatAdapter extends IChat.Stub {
    private static final int HISTORY_MAX_SIZE = 200;
    private static final String TAG = "ChatAdapter_Beem";
    private static final String PROTOCOL = "XMPP";

    private final Chat mAdaptee;
    private final Contact mParticipant;
    private String mState;
    private boolean mIsOpen;
    private final List<BeemMessage> mMessages;
    private final RemoteCallbackList<IMessageListener> mRemoteListeners = new RemoteCallbackList<IMessageListener>();
    private final MsgListener mMsgListener = new MsgListener();
    private SessionID mOtrSessionId;
    private boolean mIsHistory;
    private File mHistoryPath;
    private String mAccountUser;
    private int mUnreadMsgCount;

    /**
     * Constructor.
     * @param chat The chat to adapt
     */
    public ChatAdapter(final Chat chat) {
		mAdaptee = chat;
		mParticipant = new Contact(chat.getParticipant());
		mMessages = new LinkedList<BeemMessage>();
		mAdaptee.addMessageListener(mMsgListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact getParticipant() throws RemoteException {
	return mParticipant;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public void sendMessage(co.beem.project.beem.service.BeemMessage message)
			throws RemoteException {
		co.beem.project.beem.service.BeemMessage encrypted = otrEncryptMessage(message);
		if (encrypted != null) {
			transferMessage(encrypted);
		} else {
			transferMessage(message);
		}
		addMessage(message);
	}

    /**
     * private method for sending message.
     * @param message the message to send
     */
	private void transferMessage(
			co.beem.project.beem.service.BeemMessage message) {
		org.jivesoftware.smack.packet.Message send = new org.jivesoftware.smack.packet.Message();
		String msgBody = message.getBody();
		send.setTo(message.getTo());
		if (FbTextApplication.isDebug)
			Log.w(TAG, "message to " + message.getTo());
		send.setBody(msgBody);

		send.setThread(message.getThread());
		send.setSubject(message.getSubject());
		send.setType(org.jivesoftware.smack.packet.Message.Type.chat);
		// TODO gerer les messages contenant des XMPPError
		// send.set
		try {
			mAdaptee.sendMessage(send);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

    /**
     * send message.
     * @param msg to send.
     */
    public void injectMessage(String msg) {
	BeemMessage msgToSend = new BeemMessage(mParticipant.getJIDWithRes(), BeemMessage.MSG_TYPE_CHAT);
	msgToSend.setBody(msg);
	transferMessage(msgToSend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMessageListener(IMessageListener listen) {
	if (listen != null)
	    mRemoteListeners.register(listen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMessageListener(IMessageListener listen) {
	if (listen != null) {
	    mRemoteListeners.unregister(listen);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getState() throws RemoteException {
	return mState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(String state) throws RemoteException {
	mState = state;
    }

    /**
     * Get the adaptee for the Chat.
     * @return The real chat object
     */
    public Chat getAdaptee() {
	return mAdaptee;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOpen(boolean isOpen) {
	this.mIsOpen = isOpen;
	if (isOpen)
		mUnreadMsgCount = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
	return mIsOpen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BeemMessage> getMessages() throws RemoteException {
	return Collections.unmodifiableList(mMessages);
    }

    /**
     * Add a message in the chat history.
     * @param msg the message to add
     */
    private void addMessage(BeemMessage msg) {
	if (mMessages.size() == HISTORY_MAX_SIZE)
	    mMessages.remove(0);
	mMessages.add(msg);
	if (!isOpen())
	    mUnreadMsgCount++;
	if (!"".equals(msg.getBody()) && msg.getBody() != null) {
	    logMessage(msg);
	}
    }

    /**
     * Save message in SDCard.
     * @param msg the message receive
     * @param contactName the name of the contact
     */
    public void saveHistory(BeemMessage msg, String contactName) {
	File path = getHistoryPath();
	File filepath;
	if (contactName.equals(msg.getFrom()))
	    filepath = new File(path, StringUtils.parseBareAddress(contactName));
	else
	    filepath = new File(path, StringUtils.parseBareAddress(msg.getTo()));
	path.mkdirs();
	try {
	    FileWriter file = new FileWriter(filepath, true);
	    String log = msg.getTimestamp() + " " + contactName + " " + msg.getBody()
		+ System.getProperty("line.separator");
	    file.write(log);
	    file.close();
	} catch (IOException e) {
		if (FbTextApplication.isDebug)
	    Log.e(TAG, "Error writing chat history", e);
	}
    }

    /**
     * set History enable/disable.
     * @param isHisory history state
     */
    public void setHistory(boolean isHisory) {
	this.mIsHistory = isHisory;
    }

    /**
     * get History state.
     * @return mIsHistory
     */
    public boolean getHistory() {
	return mIsHistory;
    }

    /**
     * Set Account user name.
     * @param accountUser user name
     */
    public void setAccountUser(String accountUser) {
	mAccountUser = accountUser;
    }

    /**
     * get Account user name.
     * @return mAccountUser
     */
    public String getAccountUser() {
	return mAccountUser;
    }

    /**
     * set History path.
     * @param historyPath history path
     */
    public void setHistoryPath(File historyPath) {
	this.mHistoryPath = historyPath;
    }

    /**
     * get History path.
     * @return mHistoryPath;
     */
    public File getHistoryPath() {
	return mHistoryPath;
    }

    /**
     * log a message.
     * @param message message to log
     */
    private void logMessage(co.beem.project.beem.service.BeemMessage message) {
	String state = Environment.getExternalStorageState();
	if (mIsHistory && Environment.MEDIA_MOUNTED.equals(state))
	    saveHistory(message, mAccountUser);

    }

    /**
     * encrypt a message with an otr session.
     * @param unencrypted message with cleartext body
     * @return message with encrypted body
     */
    private co.beem.project.beem.service.BeemMessage otrEncryptMessage(co.beem.project.beem.service.BeemMessage unencrypted) {

	if (mOtrSessionId != null && unencrypted != null && unencrypted.getBody() != null) {
	    try {
		String body = BeemOtrManager.getInstance().getOtrManager()
		    .transformSending(mOtrSessionId, unencrypted.getBody());
		BeemMessage result = new BeemMessage(unencrypted.getTo(), unencrypted.getType());
		result.setBody(body);
		return result;
	    } catch (OtrException e) {
	    	if (FbTextApplication.isDebug)
		Log.e(TAG, "OTR: Unable to encrypt message", e);
	    }
	}
	return null;
    }

    /**
     * This method is executed when the otr session status change.
     * @param otrState the new state of otr session.
     */
    public void otrStateChanged(final String otrState) {
	BeemMessage m = new BeemMessage(null, BeemMessage.MSG_TYPE_INFO);
	m.setBody(otrState);
	addMessage(m);
	final int n = mRemoteListeners.beginBroadcast();

	for (int i = 0; i < n; i++) {
	    IMessageListener listener = mRemoteListeners.getBroadcastItem(i);
	    try {
		listener.otrStateChanged(otrState);
	    } catch (RemoteException e) {
	    	if (FbTextApplication.isDebug)
		Log.w(TAG, e.getMessage());
	    }
	}
	mRemoteListeners.finishBroadcast();
    }

    @Override
    public void startOtrSession() throws RemoteException {
	if (mOtrSessionId == null) {
	    mOtrSessionId = new SessionID(mAccountUser, mParticipant.getJIDWithRes(), PROTOCOL);
	    BeemOtrManager.getInstance().addChat(mOtrSessionId, this);
	}

	try {
	    BeemOtrManager.getInstance().getOtrManager().startSession(mOtrSessionId);
	} catch (OtrException e) {
	    mOtrSessionId = null;
	    e.printStackTrace();
	    throw new RemoteException();
	}
    }

    @Override
    public void endOtrSession() throws RemoteException {
	try {
	    localEndOtrSession();
	} catch (OtrException e) {
	    e.printStackTrace();
	    throw new RemoteException();
	}
    }

    /**
     * end an Otr session.
     * @return false if something bad happened.
     * @throws OtrException an exception from otr
     */
    public boolean localEndOtrSession() throws OtrException {
	if (mOtrSessionId == null)
	    return true;

	BeemOtrManager.getInstance().getOtrManager().endSession(mOtrSessionId);
	BeemOtrManager.getInstance().removeChat(mOtrSessionId);
	mOtrSessionId = null;
	listenOtrSession();
	return true;
    }

    /**
     * Start listenning to an OTR session.
     */
    public void listenOtrSession() {
	if (mOtrSessionId != null)
	    return;

	mOtrSessionId = new SessionID(mAccountUser, mParticipant.getJIDWithRes(), PROTOCOL);
	BeemOtrManager.getInstance().addChat(mOtrSessionId, this);
	//OtrEngineImpl will make a call to "this.getSession(sessionID)" which will instantiate our session.
	BeemOtrManager.getInstance().getOtrManager().getSessionStatus(mOtrSessionId);
    }

    @Override
    public String getLocalOtrFingerprint() throws RemoteException {
	if (mOtrSessionId == null)
	    return null;

	return BeemOtrManager.getInstance().getLocalFingerprint(mOtrSessionId);
    }

    @Override
    public String getRemoteOtrFingerprint() throws RemoteException {
	if (mOtrSessionId == null)
	    return null;

	return BeemOtrManager.getInstance().getRemoteFingerprint(mOtrSessionId);
    }

    @Override
    public void verifyRemoteFingerprint(boolean ok) {
	if (mOtrSessionId != null) {
	    if (ok)
		BeemOtrManager.getInstance().verifyRemoteFingerprint(mOtrSessionId);
	    else
		BeemOtrManager.getInstance().unverifyRemoteFingerprint(mOtrSessionId);
	}
    }

    @Override
    public String getOtrStatus() throws RemoteException {
	if (mOtrSessionId == null)
	    return null;
	return BeemOtrManager.getInstance().getOtrManager().getSessionStatus(mOtrSessionId).toString();
    }

    @Override
    public int getUnreadMessageCount() throws RemoteException {
	return mUnreadMsgCount;
    }

    /**
     * Listener.
     */
    private class MsgListener implements ChatStateListener {
	/**
	 * Constructor.
	 */
	public MsgListener() {
	}

		@Override
		public void processMessage(Chat chat,
				org.jivesoftware.smack.packet.Message message) {
			BeemMessage msg = new BeemMessage(message);
			if (FbTextApplication.isDebug)
				Log.d(TAG, "new msg " + msg.getBody());
			if (!Utils.isEmpty(msg.getBody()) && msg.getBody().contains(ChatMessage.EMOTICON_MESSAGE) )
				return;
			String body;

			if (mOtrSessionId != null) {
				try {
					body = BeemOtrManager.getInstance().getOtrManager()
							.transformReceiving(mOtrSessionId, msg.getBody());
					msg.setBody(body);
				} catch (OtrException e) {
					if (FbTextApplication.isDebug)
						Log.w(TAG, "Unable to decrypt OTR message", e);
				}
			}
			// TODO add que les message pas de type errors
			ChatAdapter.this.addMessage(msg);
			final int n = mRemoteListeners.beginBroadcast();
			for (int i = 0; i < n; i++) {
				IMessageListener listener = mRemoteListeners
						.getBroadcastItem(i);
				try {
					if (listener != null)
						listener.processMessage(ChatAdapter.this, msg);
				} catch (RemoteException e) {
					if (FbTextApplication.isDebug)
						Log.w(TAG, "Error while diffusing message to listener",
								e);
				}
			}
			mRemoteListeners.finishBroadcast();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stateChanged(Chat chat, ChatState state) {
	    mState = state.name();
	    final int n = mRemoteListeners.beginBroadcast();

	    for (int i = 0; i < n; i++) {
		IMessageListener listener = mRemoteListeners.getBroadcastItem(i);
		try {
		    listener.stateChanged(ChatAdapter.this);
		} catch (RemoteException e) {
			if (FbTextApplication.isDebug)
		    Log.w(TAG, e.getMessage());
		}
	    }
	    mRemoteListeners.finishBroadcast();
	}

    }

}
