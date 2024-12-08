package de.dotwee.micropinner.database;

import java.io.Serializable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build.VERSION_CODES;
import android.widget.ArrayAdapter;

/**
 * Created by lukas on 10.08.2016.
 */
public class PinSpec
 implements Serializable
{
private final long id;
private String title;
private String content;

private int priority;
private int order;

private boolean showActions;

public PinSpec(long id,
 @NonNull String title, @NonNull String content,
 int priority, int order, boolean showActions)
{
   this.id = id;
   this.title = title;
   this.content = content;
//   this.visibility = visibility;
   this.priority = priority;
   this.order = order;
//   this.persistent = persistent;
   this.showActions = showActions;
}

public void setData(@NonNull String title, @NonNull String content, int priority,
 boolean showActions)
{
   this.title = title;
   this.content = content;
   this.priority = priority;
   this.showActions = showActions;
}

public long getId()
{
   return id;
}

public int getIdAsInt()
{
   return (int) id;
}

@NonNull
public String getTitle()
{
   return title;
}

@NonNull
public String getContent()
{
   return content;
}

//public int getVisibilityIndex()
//{
//   return visibility;
//}
//
//public int getVisibility()
//{
//   switch(visibility) {
//   case 0:
//      return Notification.VISIBILITY_PUBLIC;
//   case 1:
//      return Notification.VISIBILITY_PRIVATE;
//   case 2:
//      return Notification.VISIBILITY_SECRET;
//   default:
//      throw new RuntimeException("illegal visibility value");
//   }
//}

public int getOrder()
{
   return order;
}

public String getOrderKey()
{
   return String.valueOf(order);
}

public void setOrder(int order)
{
   this.order = order;
}

public String getNotificationChannelID()
{
   return priority + "_" + order;
}

public String getNotificationChannelName(ArrayAdapter<String> priorityAdapter)
{
   if(order == 0)
      return priorityAdapter.getItem(priority);
   else
      return priorityAdapter.getItem(priority) + " " + (1 + order);
}

public int getPriorityIndex()
{
   return priority;
}

public static int priorityToIndex(int notificationPriority)
{
   switch(notificationPriority) {
   case Notification.PRIORITY_HIGH:
      return 0;
   case Notification.PRIORITY_DEFAULT:
      return 1;
   case Notification.PRIORITY_LOW:
      return 2;
   case Notification.PRIORITY_MIN:
      return 3;
   default:
      throw new RuntimeException("illegal priority value");
   }
}

public int getPreOreoPriority()
{
   switch(priority) {
   case 0:
      return Notification.PRIORITY_HIGH;
   case 1:
      return Notification.PRIORITY_DEFAULT;
   case 2:
      return Notification.PRIORITY_LOW;
   case 3:
      return Notification.PRIORITY_MIN;
   default:
      throw new RuntimeException("illegal priority value");
   }
}

@RequiresApi(VERSION_CODES.O) // OREO == 26
public int getImportance()
{
   // these constants actually only need VERSION_CODES.N // NOUGAT == 24
   switch(priority) {
   case 0:
      return NotificationManager.IMPORTANCE_HIGH;
   case 1:
      return NotificationManager.IMPORTANCE_DEFAULT;
   case 2:
      return NotificationManager.IMPORTANCE_LOW;
   case 3:
      return NotificationManager.IMPORTANCE_MIN;
   default:
      throw new RuntimeException("illegal priority value");
   }
}

//public boolean isPersistent()
//{
//   return persistent;
//}

public boolean isShowActions()
{
   return showActions;
}

@NonNull
public String toClipString()
{
   if(content != null && !content.isEmpty()) {
      return title + " - " + content;
   }
   else {
      return title;
   }
}

@NonNull
@Override
public String toString()
{
   return "PinSpec{" +
           "id=" + id +
           ", title='" + title + '\'' +
           ", content='" + content + '\'' +
//           ", visibility=" + visibility +
           ", priority=" + priority +
           ", order=" + order +
           ", showActions=" + showActions +
           '}';
}
}
