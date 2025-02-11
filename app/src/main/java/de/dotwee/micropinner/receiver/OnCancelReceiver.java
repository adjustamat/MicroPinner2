package de.dotwee.micropinner.receiver;

import androidx.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.dotwee.micropinner.ui.FragEditor;
import de.dotwee.micropinner.database.Pin;
import de.dotwee.micropinner.NotificationTools;

public class OnCancelReceiver
 extends BroadcastReceiver
{
private static final String DBG = "OnCancelReceiver";

@Override
public void onReceive(@NonNull Context context, @NonNull Intent intent)
{
   // deserialize our pin from the intent
   Pin pin = (Pin) intent.getSerializableExtra(FragEditor.EXTRA_SERIALIZABLE_PIN);
   if(pin == null) {
      Log.d(DBG, "Intent did not contain a pin as serialized extra! " + intent);
      return;
   }
   
   // show pin again
   NotificationTools.notify(context, pin);
}
}
