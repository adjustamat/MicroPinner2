package de.dotwee.micropinner.receiver;

import androidx.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.dotwee.micropinner.FragEditor;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;

/**
 * Created by Lukas on 26.06.2015.
 * <p>
 * This class is a broadcast receiver for {@link android.app.Notification}
 * DeleteIntents.
 * <p>
 * Intents should contain a serialized pin as extra.
 * If yes, tell the {@link PinDatabase} to delete the pin
 */
public class OnDeleteReceiver
 extends BroadcastReceiver
{
private final static String TAG = OnDeleteReceiver.class.getSimpleName();

@Override
public void onReceive(@NonNull Context context, @NonNull Intent intent)
{
   
   // deserialize our pin from the intent
   PinSpec pin = (PinSpec) intent.getSerializableExtra(FragEditor.EXTRA_PIN_SPEC);
   
   if(pin != null) {
      Log.i(TAG, "Received deleteIntent from pin " + pin.getId());
      
      // delete it from the database
      PinDatabase.getInstance(context).deletePin(pin.getId());
   }
   else {
      throw new IllegalArgumentException(
       "Intent did not contain a pin as serialized extra! " + intent);
   }
}
}
