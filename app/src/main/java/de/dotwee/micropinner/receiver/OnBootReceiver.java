package de.dotwee.micropinner.receiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import de.dotwee.micropinner.tools.NotificationTools;
import de.dotwee.micropinner.view.MainDialog;

public class OnBootReceiver
 extends BroadcastReceiver
{
private final static String TAG = OnBootReceiver.class.getSimpleName();

@Override
public void onReceive(@NonNull Context context, @Nullable Intent intent)
{
   if(intent == null || intent.getAction() == null) {
      Log.w(TAG,
       "Intent (and its action) must be not null to work with it, returning without work");
      return;
   }
   if(!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
      Log.w(TAG, "OnBootReceiver's intent action is not BOOT_COMPLETED, returning without work");
      return;
   }
   
   if(VERSION.SDK_INT < VERSION_CODES.M || // MARSHMALLOW == 23
       PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
        context, MainDialog.POST_NOTIFICATIONS)) {
      MainDialog.hasPermission = true;
      NotificationTools.restoreAllPins(context);
   }
}
}
