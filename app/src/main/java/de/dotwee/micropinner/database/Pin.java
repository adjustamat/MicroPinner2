package de.dotwee.micropinner.database;

import java.io.Serializable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.ArrayAdapter;

public class Pin
 implements Serializable
{
private final int id;
private @NonNull String title;
private @NonNull String content;

private boolean showActions;

private int priority;
private int order;

public Pin(int id,
 @NonNull String title, @NonNull String content,
 int priority, int order, boolean showActions)
{
   this.id = id;
   this.title = title;
   this.content = content;
   this.priority = priority;
   this.order = order;
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

public void setOrder(int order)
{
   this.order = order;
}

public long getID()
{
   return id;
}

public int getIDAsInt()
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

public String getSortKey()
{
   return Integer.toHexString(order);
}

public int getOrder()
{
   return order;
}

@NonNull
public String getNotificationChannelID()
{
   return getChannelID(priority, order);
}

public String getNotificationChannelName(ArrayAdapter<String> priorityLocalizedStrings)
{
   return getChannelName(priority, order, priorityLocalizedStrings);
}

@NonNull
public static String getChannelID(int priority, int order)
{
   return "p" + priority + "_" + order;
}

@SuppressLint("DefaultLocale")
public static String getChannelName(int priorityIndex, int order,
 ArrayAdapter<String> priorityLocalizedStrings)
{
   if(order == 0)
      return priorityLocalizedStrings.getItem(priorityIndex);
   else
      return priorityLocalizedStrings.getItem(priorityIndex) + String.format(" %d", 1 + order);
}

@SuppressLint("DefaultLocale")
public String getPrioOrderDisplayString(ArrayAdapter<String> priorityLocalizedStrings, Integer max)
{
   if(max == null)
      return priorityLocalizedStrings.getItem(priority);
   else
      return priorityLocalizedStrings.getItem(priority) +
              String.format(" (%d/%d)", 1 + order, 1 + max);
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

@RequiresApi(Build.VERSION_CODES.N) // NOUGAT == 24
public static int getImportance(int priorityIndex)
{
   switch(priorityIndex) {
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

@RequiresApi(Build.VERSION_CODES.N) // NOUGAT == 24
public int getImportance()
{
   // this method actually only used when Build.VERSION.SDK_INT >= VERSION_CODES.O // OREO == 26
   return getImportance(priority);
}

public boolean isShowActions()
{
   return showActions;
}

@NonNull
public String toClipString()
{
   if(!content.isEmpty()) {
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
   return "Pin{" + "id=" + id +
           ", title='" + title + "', content='" + content + "', priority=" + priority +
           ", order=" + order + ", showActions=" + showActions + '}';
}

@SuppressLint("DefaultLocale")
public final String test(Object o)
{
   Pin other = (Pin) o;
   if(!equals(o))
      return String.format("test ID different: %d != %d", id, other.id);
   if(!title.equals(other.title))
      return String.format("test %d title different: %s != %s", id, title, other.title);
   if(!content.equals(other.content))
      return String.format("test %d content different: %s != %s", id, content, other.content);
   if(priority != other.priority)
      return String.format("test %d priority different: %d != %d", id, priority, other.priority);
   if(order != other.order)
      return String.format("test %d order different: %d != %d", id, order, other.order);
   if(showActions != other.showActions)
      return String.format("test %d showActions different: %b != %b", id,
       showActions, other.showActions);
   return String.format("test %d exactly equal :)", id);
}

@Override
public final boolean equals(Object o)
{
   if(this == o) return true;
   if(!(o instanceof Pin)) return false;
   Pin other = (Pin) o;
   return id == other.id;
}

@Override
public int hashCode()
{
   return Long.hashCode(id);
}
}
