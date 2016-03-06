package hoahong.facebook.messenger.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;


public class Image implements Parcelable {
	
	@SerializedName("width")
	private float width;
	@SerializedName("height")
	private float height;
	@SerializedName("src")
	private String src;
	
	@SerializedName("source")
	private String source;
	
	public Image()
	{

	}

	public float getWidth()
	{
		return width;
	}

	public void setWidth(float width)
	{
		this.width = width;
	}

	public float getHeight()
	{
		return height;
	}

	public void setHeight(float height)
	{
		this.height = height;
	}

	public String getSource()
	{
		return src == null ? source : src;
	}

	public void setSource(String source)
	{
		this.src = source;
	}

    protected Image(Parcel in) {
        width = in.readFloat();
        height = in.readFloat();
        src = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(width);
        dest.writeFloat(height);
        dest.writeString(src);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}