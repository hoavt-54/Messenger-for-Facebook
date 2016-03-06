package hoahong.facebook.messenger.data;

public class FbPost {
//06-27 15:51:09.797: V/FacebookTextService(17956): response entity: {"id":"1604812059771713","post_id":"1417094931918673_1604812059771713"}
	private String id;
	private String post_id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPost_id() {
		return post_id;
	}
	public void setPost_id(String post_id) {
		this.post_id = post_id;
	}
	public FbPost(String id, String post_id) {
		super();
		this.id = id;
		this.post_id = post_id;
	}
	
	public FbPost() {
		super();
	}

}
