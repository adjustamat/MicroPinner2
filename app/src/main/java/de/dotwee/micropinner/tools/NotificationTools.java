package de.dotwee.micropinner.tools;

import java.util.List;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import de.dotwee.micropinner.R;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.receiver.OnCancelReceiver;
import de.dotwee.micropinner.receiver.OnClipReceiver;
import de.dotwee.micropinner.view.MainDialog;
import de.dotwee.micropinner.view.custom.DialogContentView;

/**
 * Created by lukas on 10.08.2016.
 */
public class NotificationTools
{
public final static String EXTRA_INTENT = "IAMAPIN";

private static final String CHANNEL_ID = "chan_id_";
private static final String TAG = NotificationTools.class.getSimpleName();

@NonNull
private static PendingIntent getEditorIntent(@NonNull Context context, @NonNull PinSpec pin)
{
   // intent for starting Activity MainDialog (edit the pin)
   Intent resultIntent = new Intent(context, MainDialog.class);
   resultIntent.putExtra(EXTRA_INTENT, pin);
   resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   return PendingIntent.getActivity(context, pin.getIdAsInt(), resultIntent,
    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
}

public static void notify(@NonNull Context context, @NonNull PinSpec pin)
{
   NotificationManager notificationManager =
    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
   
   if(!MainDialog.hasPermission) {
      Toast.makeText(context, R.string.requires_your_permission, Toast.LENGTH_LONG).show();
      return;
   }
   
   if(notificationManager == null) {
      Log.w(TAG, "NotificationManager is null! Couldn't send notification!");
      return;
   }
   
   @SuppressLint("WrongConstant") NotificationCompat.Builder builder =
    new NotificationCompat.Builder(context,
     CHANNEL_ID + pin.getPriorityIndex() + pin.getVisibilityIndex())
     
     .setShowWhen(false)
     .setSilent(true)
     .setOnlyAlertOnce(true)
     .setAutoCancel(false)
     .setCategory(Notification.CATEGORY_STATUS)
     .setPriority(pin.getPreOreoPriority())
     .setVisibility(pin.getVisibility())
     // .setPublicVersion()
     .setOngoing(pin.isPersistent())
     
     .setContentTitle(pin.getTitle())
     .setSmallIcon(R.drawable.ic_notif_star)
     
     .setContentText(pin.getContent())
     .setStyle(new NotificationCompat.BigTextStyle().bigText(pin.getContent()))
     
     .setContentIntent(getEditorIntent(context, pin))
     
     .setDeleteIntent(PendingIntent.getBroadcast(context, pin.getIdAsInt(),
      new Intent(context, OnCancelReceiver.class)
       .setAction("notification_cancelled")
       .putExtra(EXTRA_INTENT, pin),
      PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE))
    //
    ;
   
   if(pin.isShowActions()) {
      builder.addAction(R.drawable.ic_action_clip,
       context.getString(R.string.message_save_to_clipboard),
       PendingIntent.getBroadcast(context, pin.getIdAsInt(),
        new Intent(context, OnClipReceiver.class)
         .putExtra(EXTRA_INTENT, pin),
        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE));
   }
   
   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // OREO == 26
      // Create or update NotificationChannel
      String channelName = context.getString(R.string.noti_chan_name,
       DialogContentView.getPriorityAdapter(context, null)
        .getItem(pin.getPriorityIndex()),
       DialogContentView.getVisibilityAdapter(context, null)
        .getItem(pin.getVisibilityIndex())
      );
      NotificationChannel channel = new NotificationChannel(
       CHANNEL_ID + pin.getPriorityIndex() + pin.getVisibilityIndex(),
       channelName,
       pin.getImportance()
      );
      notificationManager.createNotificationChannel(channel);
   }
   
   Log.i(TAG, "Send notification with pin id " + pin.getIdAsInt() + " to system");
   
   Notification notification = builder.build();
   // only delete noti with delete button in MainDialog!
   /*
    * Bit to be bitswised-ored into the {@link #flags} field that should be
    * set by the system if this notification is not dismissible.
    *
    * This flag is for internal use only; applications cannot set this flag directly.
    * @hide
   public static final int FLAG_NO_DISMISS = 0x00002000;
    */
//   notification.flags |= 0x00002000;
   notificationManager.notify(pin.getIdAsInt(), notification);
   
}

public static void restoreAllPins(Context context)
{
   final List<PinSpec> pins = PinDatabase.getInstance(context).getAllPins();
   for(PinSpec pin : pins) {
      
      // create a notification from the object
      notify(context, pin);
   }
}
}
