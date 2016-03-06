package co.beem.project.beem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	
	public static final String IS_FROM_BOOT = "is_from_boot";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
        	Log.e(IS_FROM_BOOT, "receive intent on boot");
            Intent serviceIntent = new Intent(context, FbTextService.class);
            serviceIntent.putExtra(IS_FROM_BOOT, true);
            context.startService(serviceIntent);
        }
    }
}