package hoahong.facebook.messenger.data;

public interface Contactable {
	public UserState getState();
	public String getJabberId();
	public String getName();
	public void setState (UserState state);
}
