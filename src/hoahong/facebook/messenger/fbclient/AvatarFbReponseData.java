package hoahong.facebook.messenger.fbclient;

public class AvatarFbReponseData {
	
	public Data data;
	public Error error;
	
	
	public class Data {
		public int height;
		public boolean is_silhouette;
		public String url;
		public int width;
	}
	
	public class Error {
		public String message;
	}
	
	
}
