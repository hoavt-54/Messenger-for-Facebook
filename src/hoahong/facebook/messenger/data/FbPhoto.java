package hoahong.facebook.messenger.data;

import java.util.List;

public class FbPhoto {
	private String id;
	private List<Image> images;
	private float height;
	private float width;
	private String picture;
	private String source;
	
	
	public FbPhoto () {}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public List<Image> getImages() {
		return images;
	}


	public void setImages(List<Image> images) {
		this.images = images;
	}


	public float getHeight() {
		return height;
	}


	public void setHeight(float height) {
		this.height = height;
	}


	public float getWidth() {
		return width;
	}


	public void setWidth(float width) {
		this.width = width;
	}


	public String getPicture() {
		return picture;
	}


	public void setPicture(String picture) {
		this.picture = picture;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}
	
	
}
