package hoahong.facebook.messenger.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName="chat_sessions")
public class ChatSession {
	
	public static final String SESSION_CHAT_ID_KEY = "chat_session_id";
	public static final String USER_NAME_FIELD = "user_id";
	public static final String LAST_MESSAGE_FIELD = "last_messsage_id";
	
	@DatabaseField(generatedId=true)
	private Integer localId;
	
	@ForeignCollectionField(eager=false)
	private Collection<ChatMessage> messages;
	
	@DatabaseField(foreign=true, columnName=USER_NAME_FIELD, index=true, foreignAutoRefresh=true)
	private User user;
	
	@DatabaseField(foreign=true, foreignAutoRefresh=true, columnName=LAST_MESSAGE_FIELD)
	private ChatMessage lastMessage;
	
	@DatabaseField
	private int numberOfUnreadMessages;
	

	public ChatSession (){
		super();
	}
	// constructor for private chat
	public ChatSession(User sessionPartner) {
		super();
		this.user = sessionPartner;
	}

	public ChatMessage getLastMessage() {
		return lastMessage;
	}


	public void setLastMessage(ChatMessage lastMessage) {
		this.lastMessage = lastMessage;
	}
	
	
	

	public Integer getLocalId() {
		return localId;
	}


	public void setLocalId(Integer localId) {
		this.localId = localId;
	}
	


	public Collection<ChatMessage> getMessages() {
		return messages;
	}


	public void setMessages(Collection<ChatMessage> messages) {
		this.messages = messages;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	
	/*this method returns list of events with just ID*/
	public List<ChatMessage> getEventsIDAsSortedList(){
		List<ChatMessage> listEvents = new ArrayList<ChatMessage>(messages);
		Collections.sort(listEvents, new Comparator<ChatMessage>() {
			@Override
			public int compare(ChatMessage lhs, ChatMessage rhs) {
				if(lhs.getLocalId() > rhs.getLocalId())
					return 1;
				else
				return -1;
			}
		});
		System.out.println("onlyID: " + listEvents.size());
		return listEvents;
	}


	public int getNumberOfUnreadMessages() {
		return numberOfUnreadMessages;
	}


	public void setNumberOfUnreadMessages(int numberOfUnreadMessages) {
		this.numberOfUnreadMessages = numberOfUnreadMessages;
	}
	public void increaseNumberOfUnreadMEssages (){
		this.numberOfUnreadMessages ++;
	}
}
