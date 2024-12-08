package de.dotwee.micropinner.receiver;

import androidx.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.tools.NotificationTools;

public class OnCancelReceiver
 extends BroadcastReceiver
{
private static final String DBG = "AutocloseReceiver";

@Override
public void onReceive(@NonNull Context context, @NonNull Intent intent)
{
   // deserialize our pin from the intent
   PinSpec pin = (PinSpec) intent.getSerializableExtra(NotificationTools.EXTRA_PIN_SPEC);
   if(pin == null) {
      Log.d(DBG, "Intent did not contain a pin as serialized extra! " + intent);
      return;
   }
   
   // show pin again
   NotificationTools.notify(context, pin);
}
}
