package hoahong.facebook.messenger.data;

public class FbAvatar {
	private Picture picture;
	public class Picture {
		private Data data;
		public class Data {
			public String url;
		}
		public Data getData() {
			return data;
		}
		public void setData(Data data) {
			this.data = data;
		}
	}
	public Picture getPicture() {
		return picture;
	}
	public void setPicture(Picture picture) {
		this.picture = picture;
	}
}

