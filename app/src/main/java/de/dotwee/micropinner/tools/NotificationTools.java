package de.dotwee.micropinner.tools;

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
import de.dotwee.micropinner.R;
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
private static PendingIntent getPinIntent(@NonNull Context context, @NonNull PinSpec pin)
{
   Intent resultIntent = new Intent(context, MainDialog.class);
   resultIntent.putExtra(EXTRA_INTENT, pin);
   resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   return PendingIntent.getActivity(context, pin.getIdAsInt(), resultIntent,
    PendingIntent.FLAG_UPDATE_CURRENT);
}

public static void notify(@NonNull Context context, @NonNull PinSpec pin)
{
   NotificationManager notificationManager =
    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
   
   @SuppressLint("WrongConstant") NotificationCompat.Builder builder =
    new NotificationCompat.Builder(context,
     CHANNEL_ID + pin.getPriorityIndex() + pin.getVisibilityIndex())
     .setContentTitle(pin.getTitle())
     .setContentText(pin.getContent())
     .setSmallIcon(R.drawable.ic_notif_star)
     .setPriority(pin.getPreOreoPriority())
     .setVisibility(pin.getVisibility())
     .setStyle(new NotificationCompat.BigTextStyle().bigText(pin.getContent()))
     .setContentIntent(getPinIntent(context, pin))
     
     .setDeleteIntent(PendingIntent.getBroadcast(context, pin.getIdAsInt(),
      new Intent(context, OnCancelReceiver.class)
       .setAction("notification_cancelled")
       .putExtra(EXTRA_INTENT, pin),
      PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE))
     .setOngoing(pin.isPersistent());
   
   if(pin.isShowActions()) {
      builder.addAction(R.drawable.ic_action_clip,
       context.getString(R.string.message_save_to_clipboard),
       PendingIntent.getBroadcast(context, pin.getIdAsInt(),
        new Intent(context, OnClipReceiver.class)
         .putExtra(EXTRA_INTENT, pin),
        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE));
   }
   
   if(notificationManager != null) {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // OREO == 26
         
         // Create or update channel
         NotificationChannel channel = new NotificationChannel(
          CHANNEL_ID + pin.getPriorityIndex() + pin.getVisibilityIndex(),
          context.getString(R.string.noti_chan_name,
           DialogContentView.priorityLocaleStrs.getItem(pin.getPriorityIndex()),
           DialogContentView.visibilityLocaleStrs.getItem(pin.getVisibilityIndex())
          ),
          pin.getImportance()
         );
         notificationManager.createNotificationChannel(channel);
      }
      
      Log.i(TAG, "Send notification with pin id " + pin.getIdAsInt() + " to system");
      Notification notification = builder.build();
      
      notificationManager.notify(pin.getIdAsInt(), notification);
   }
   else {
      Log.w(TAG, "NotificationManager is null! Couldn't send notification!");
   }
}
}
