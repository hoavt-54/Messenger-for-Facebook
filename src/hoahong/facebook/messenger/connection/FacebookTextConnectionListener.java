package hoahong.facebook.messenger.connection;

public interface FacebookTextConnectionListener {
public void onConnected();
	
	public void onDisconnect();
	
	public void onConnecting();
	public void onConnectingIn(int arg0);
	public void onNoInternetConnection ();
	
	public void onConnectionError ();
}
