package de.dotwee.micropinner.receiver;

import java.util.Map;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.tools.NotificationTools;

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
      Log.w(TAG, "OnBootReceiver's intent actions is not " + Intent.ACTION_BOOT_COMPLETED +
                  ", returning without work");
      return;
   }
   
   // get all pins
   final Map<Integer, PinSpec> pinMap = PinDatabase.getInstance(context).getAllPinsMap();
   
   // foreach through them all
   for(Map.Entry<Integer, PinSpec> entry : pinMap.entrySet()) {
      PinSpec pin = entry.getValue();
      
      // create a notification from the object and finally restore it
      NotificationTools.notify(context, pin);
   }
}
}
