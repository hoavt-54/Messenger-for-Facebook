package hoahong.facebook.messenger.data;

public class ImageMessageInQueue {
	private String localPath;
	private int localId;
	private String jIdWithRes;
	private String senderName;
	
	public ImageMessageInQueue () {}
	public ImageMessageInQueue (String localPath){
		super();
		this.localPath = localPath;
	}	
	
	public String getjIdWithRes() {
		return jIdWithRes;
	}
	public void setjIdWithRes(String jIdWithRes) {
		this.jIdWithRes = jIdWithRes;
	}
	public String getLocalPath() {
		return localPath;
	}
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	public int getLocalId() {
		return localId;
	}
	public void setLocalId(int localId) {
		this.localId = localId;
	}
	public ImageMessageInQueue(String localPath, int localId, String jIdWithRes, String senderName) {
		super();
		this.localPath = localPath;
		this.localId = localId;
		this.jIdWithRes = jIdWithRes;
		this.senderName = senderName;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	
	
	
	
}
