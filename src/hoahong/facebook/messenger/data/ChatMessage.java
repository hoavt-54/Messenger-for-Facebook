package hoahong.facebook.messenger.data;

import hoahong.facebook.messenger.ui.Emoji;
import hoahong.facebook.messenger.ui.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.text.TextPaint;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="chat_messages")
public class ChatMessage {
	public static final String MESSAGE_CURRENT_UID = "message_current_uid";
	public static final String MESSAGE_SESSION_ID = "session_id";
	public static final String MESSAGE_IMAGE_PATH = "chat_message_path";
	public static final String MESSAGE_FB_PHOTO_PATH = "facebook_image_path";
	public static final String MESSAGE_ID_KEY = "message_local_id";
	public static final String MESSAGE_IS_SENT_KEY = "message_is_sent_key";
	public static final String MESSAGE_IS_SENT_FAIL = "message_is_sent_fail";
	public static final String EMOTICON_MESSAGE = "www.facebook.com/messages/";
	
	@DatabaseField(generatedId=true, index =true, columnName= MESSAGE_ID_KEY)
	private Integer localId;
	
	@DatabaseField
	private String messageContent;
	
	@DatabaseField
	private String imagePath;
	
	@DatabaseField
	private long sentTime;
	
	@DatabaseField(foreign=true)
	private User author;
	
	@DatabaseField (foreign=true, columnName=MESSAGE_SESSION_ID, index = true)
	private ChatSession session;
	
	@DatabaseField
	private ChatMessageType type;
	
	@DatabaseField
	private String to;
	
	@DatabaseField
	private boolean isLocallySeen;
	
	@DatabaseField(columnName= MESSAGE_IS_SENT_FAIL)
	private boolean isSentFail;
	
	private TextPaint textPaint;
	
	@DatabaseField(columnName = MESSAGE_IS_SENT_KEY)
	private boolean isSent;
	
	@DatabaseField
	public int compsitionLength;
     
	
	public ChatMessage (){
		super();
	}
	
	public ChatMessage (String messageContent, long sentTime,
				User user, ChatMessageType type, boolean isSent){
		
		if (messageContent != null && messageContent.length() > 0){
			if (textPaint == null) {
		         textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		         textPaint.setColor(0xff000000);
		         textPaint.linkColor = 0xff316f9f;
		     }
			textPaint.setTextSize(Utils.dp(Utils.fontSize));
			this.messageContent =  Emoji.
					replaceEmoji(messageContent,
							textPaint.getFontMetricsInt(), 
							Utils.dp(20))
							.toString();;
		}
		this.sentTime = sentTime;
		this.author = user;
		this.type = type;
		this.isSent = isSent;
	}
	

	public Integer getLocalId() {
		return localId;
	}

	public void setLocalId(Integer localId) {
		this.localId = localId;
	}

	public CharSequence getMessageContent() {
		return messageContent;
	}
	
	

	public boolean isSentFail() {
		return isSentFail;
	}

	public void setSentFail(boolean isSentFail) {
		this.isSentFail = isSentFail;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public long getSentTime() {
		return sentTime;
	}

	public void setSentTime(long sentTime) {
		this.sentTime = sentTime;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public ChatSession getSession() {
		return session;
	}

	public void setSession(ChatSession session) {
		this.session = session;
	}

	public ChatMessageType getType() {
		return type;
	}

	public void setType(ChatMessageType type) {
		this.type = type;
	}
	
	public boolean isFromMe(){
		return author == null;
	}

	public boolean isSent() {
		return isSent;
	}

	public void setSent(boolean isSent) {
		this.isSent = isSent;
	}
	public String getDate(){
		String newString = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(sentTime));
		return newString;
	}
	
	
	
	public String getShortTime(){
		String newString = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(sentTime));
		return newString;
	}
	@SuppressLint("SimpleDateFormat") 
	public String getTimeOfLastMessage(){
		Date timeOfLastMessage = null;
		Calendar currentTime = Calendar.getInstance();
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DATE, -1);

		timeOfLastMessage = new Date(sentTime);
		SimpleDateFormat dayFormat = new SimpleDateFormat("M/dd/yyyy", Locale.getDefault());
		SimpleDateFormat hourFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
		if(dayFormat.format(timeOfLastMessage).equalsIgnoreCase(dayFormat.format(currentTime.getTime())))
			return hourFormat.format(timeOfLastMessage);
		else if (dayFormat.format(timeOfLastMessage).equalsIgnoreCase(dayFormat.format(yesterday.getTime())))
			return "YESTERDAY";
		else
			return dayFormat.format(timeOfLastMessage);
	
	}
	
	public ChatMessage makeSeparator (){
		ChatMessage tmp = new ChatMessage();
		tmp.session = this.session;
		tmp.type = ChatMessageType.date_separator;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(sentTime);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		tmp.sentTime = cal.getTimeInMillis();
		tmp.isSent = true;
		return tmp;
		
	}
	
	public String getShortText (){
		
		
		String lastMessageContent = null;
		

		if (type == ChatMessageType.image)
			return "image";
		lastMessageContent = messageContent;
		
		if(lastMessageContent == null)
			return " ";
		// extract the first line of the message 
		lastMessageContent = lastMessageContent.split("\n")[0];
		// extrac first 20 characters of the message
		if(lastMessageContent.length() > 30){
			lastMessageContent = lastMessageContent.substring(0, 30);
			return lastMessageContent + "...";
		}
		return lastMessageContent;
	}

	public boolean isLocallySeen() {
		return isLocallySeen;
	}

	public void setLocallySeen(boolean isLocallySeen) {
		this.isLocallySeen = isLocallySeen;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@Override
	public boolean equals(Object o) {
		if (! (o instanceof ChatMessage))
			return false;
		ChatMessage other = (ChatMessage) o;
		if (this.localId > 0 && other.localId > 0)
			return this.localId == other.localId;
		else if (this.type != other.type)
			return false;
		else{
			if (this.messageContent == null || other.messageContent == null)
				return false;
			else return this.messageContent.equals(other.messageContent);
		}
	}
	
}
