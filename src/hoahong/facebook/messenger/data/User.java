package hoahong.facebook.messenger.data;

import org.jivesoftware.smack.util.StringUtils;

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="users")
public class User implements Contactable{
	public static final String USER_NAME_FIELD = "user_name";
	public static final String USER_JABBER_ID = "jabber_id";
	public static final String USER_JID_WITH_RES_KEY = "jIdWithRes_key";
	public static final String USER_STATE_FIELD = "state_field";
	public static final String USER_CLICK_COUNT_FIELD = "click_count_field";
	public static final String USER_MESSAGE_COUNT_FIELD = "message_count_field";
	public static final String USER_FAVORITE_FIELD = "favorite_field";
	public static final String USER_BLOCK_FIELD = "block_field";
	
	public static final float ONLINE_WEIGHT = 0.2f;
	public static final float MESSAGE_COUNT_WEIGHT = 0.5f;
	public static final float CLICK_COUNT_WEIGHT = 0.6f;
	
	public static int MAX_CLICK_TIME = 30;
	public static int MAX_MESSAGE_COUNT= 50;
	
	
	@DatabaseField(id=true, index = true, columnName=USER_JABBER_ID)
	private String jabberId;
	
	
	@DatabaseField(columnName=USER_NAME_FIELD, index = true)
	private String name;
	
	@DatabaseField(columnName=USER_STATE_FIELD)
	private UserState state;
	
	@DatabaseField
	private String avatarImageFile;
	
	@DatabaseField
	private String status;
	
	@DatabaseField
	private int sessionChatId;
	
	@DatabaseField(columnName=USER_FAVORITE_FIELD)
	private boolean isFavorite;
	
	@DatabaseField(columnName=USER_BLOCK_FIELD)
	private boolean isBlocked;
	
	@DatabaseField(columnName=USER_CLICK_COUNT_FIELD)
	private int clickTime;
	
	@DatabaseField(columnName=USER_MESSAGE_COUNT_FIELD)
	private int  messageCount;

	public int getClickTime() {
		return clickTime;
	}

	public void setClickTime(int clickTime) {
		this.clickTime = clickTime;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}



	@DatabaseField(foreign=true)
	private UserGroup userGroup;
	
	@DatabaseField
	private float point;
	
	public float getPoint() {
		int onlinePoint = (state == UserState.available) ? 1 : 0;
		float favoritePoint = isFavorite() ? 30f: 0;
		return onlinePoint * ONLINE_WEIGHT + CLICK_COUNT_WEIGHT * 
				( (float)clickTime / MAX_CLICK_TIME) + MESSAGE_COUNT_WEIGHT 
					* ((float)messageCount / MAX_MESSAGE_COUNT) + favoritePoint;
	}

	public void setPoint(float point) {
		this.point = point;
	}

	// this one for ormlite
	public User(){
		this.state = UserState.unavailable;
	}

	public User(String id){
		this.jabberId = id;
		this.state = UserState.unavailable;
	}
	
	public User(String id, int state){
		this.jabberId = id;
		this.state = state == 1 ? UserState.available : UserState.unavailable;
	}
	
	public User(String jabberId, String name, UserGroup userGroup) {
		super();
		this.jabberId = jabberId;
		this.name = name;
		this.userGroup = userGroup;
		this.state = UserState.unavailable;
	}
	
	public User(String jabberId, String name, String userOwner) {
		super();
		this.jabberId = jabberId;
		this.name = name;
		this.state = UserState.unavailable;
	}
	
	

	public String getJabberId() {
		return jabberId;
	}

	public void setJabberId(String jabberId) {
		this.jabberId = jabberId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserState getState() {
		return state;
	}

	public boolean isFavorite() {
		return isFavorite;
	}

	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	public void setState(UserState state) {
		this.state = state;
	}

	public String getAvatarImage() {
		return avatarImageFile;
	}

	public void setAvatarImage(String avatarImage) {
		this.avatarImageFile = avatarImage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
	
	public void increaseMEssageCount (){
		messageCount++;
		if (MAX_MESSAGE_COUNT < messageCount)
			MAX_MESSAGE_COUNT = messageCount;
	}
	
	
	public void increaseClickCount (){
		clickTime ++;
		if (MAX_CLICK_TIME < clickTime)
			MAX_CLICK_TIME = clickTime;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof User){
			return this.jabberId.equals(((User) o).jabberId);
		}
		else return false;
	}

	public int getSessionChatId() {
		return sessionChatId;
	}

	public void setSessionChatId(int sessionChatId) {
		this.sessionChatId = sessionChatId;
	}
	
    /**
     * Make an xmpp uri for a spcific jid.
     *
     * @param jid the jid to represent as an uri
     * @return an uri representing this jid.
     */
    public static Uri makeXmppUri(String jid) {
	StringBuilder build = new StringBuilder("xmpp:");
	String name = StringUtils.parseName(jid);
	build.append(name);
	if (!"".equals(name))
	    build.append('@');
	build.append(StringUtils.parseServer(jid));
	String resource = StringUtils.parseResource(jid);
	if (!"".equals(resource)) {
	    build.append('/');
	    build.append(resource);
	}
	Uri u = Uri.parse(build.toString());
	System.out.println("Jid: " + jid);
	System.out.println("URI: "+u);
	return u;
    }

	
	
	 /**
     * Get a URI to access the contact.
     * @return the URI
     */
    public Uri toUri() {
    String mJID = StringUtils.parseBareAddress(jabberId);
	return makeXmppUri(mJID);
    }

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}
	
}
