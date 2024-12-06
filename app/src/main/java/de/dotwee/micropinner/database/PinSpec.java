package de.dotwee.micropinner.database;

import java.io.Serializable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build.VERSION_CODES;

/**
 * Created by lukas on 10.08.2016.
 */
public class PinSpec
 implements Serializable
{
private long id;
private final String title;
private final String content;

private final int visibility;
private final int priority;

private final boolean persistent;
private final boolean showActions;

public PinSpec(@NonNull String title, @NonNull String content, int visibility, int priority,
 boolean persistent, boolean showActions)
{
   this.id = -1;
   this.title = title;
   this.content = content;
   this.visibility = visibility;
   this.priority = priority;
   this.persistent = persistent;
   this.showActions = showActions;
}

PinSpec(@NonNull Cursor cursor)
{
   ContentValues contentValues = new ContentValues();
   DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
   
   this.id = contentValues.getAsLong(PinDatabase.COLUMN_ID);
   this.title = contentValues.getAsString(PinDatabase.COLUMN_TITLE);
   this.content = contentValues.getAsString(PinDatabase.COLUMN_CONTENT);
   this.visibility = contentValues.getAsInteger(PinDatabase.COLUMN_VISIBILITY);
   this.priority = contentValues.getAsInteger(PinDatabase.COLUMN_PRIORITY);
   this.persistent = (contentValues.getAsInteger(PinDatabase.COLUMN_PERSISTENT) != 0);
   this.showActions = (contentValues.getAsInteger(PinDatabase.COLUMN_SHOW_ACTIONS) != 0);
}

@NonNull
ContentValues toContentValues()
{
   ContentValues contentValues = new ContentValues();
   contentValues.put(PinDatabase.COLUMN_TITLE, getTitle());
   contentValues.put(PinDatabase.COLUMN_CONTENT, getContent());
   contentValues.put(PinDatabase.COLUMN_VISIBILITY, getVisibilityIndex());
   contentValues.put(PinDatabase.COLUMN_PRIORITY, getPriorityIndex());
   contentValues.put(PinDatabase.COLUMN_PERSISTENT, isPersistent() ? 1 : 0);
   contentValues.put(PinDatabase.COLUMN_SHOW_ACTIONS, isShowActions() ? 1 : 0);
   return contentValues;
}

public long getId()
{
   return id;
}

public void setId(long id)
{
   this.id = id;
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

public int getVisibilityIndex()
{
   return visibility;
}

public int getVisibility()
{
   switch(visibility) {
   case 0:
      return Notification.VISIBILITY_PUBLIC;
   case 1:
      return Notification.VISIBILITY_PRIVATE;
   case 2:
      return Notification.VISIBILITY_SECRET;
   default:
      throw new RuntimeException("illegal visibility value");
   }
}

public int getPriorityIndex()
{
   return priority;
}

public int getPreOreoPriority()
{
   switch(priority) {
   case 0:
      return Notification.PRIORITY_DEFAULT;
   case 1:
      return Notification.PRIORITY_MIN;
   case 2:
      return Notification.PRIORITY_LOW;
   case 3:
      return Notification.PRIORITY_HIGH;
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
      return NotificationManager.IMPORTANCE_DEFAULT;
   case 1:
      return NotificationManager.IMPORTANCE_MIN;
   case 2:
      return NotificationManager.IMPORTANCE_LOW;
   case 3:
      return NotificationManager.IMPORTANCE_HIGH;
   default:
      throw new RuntimeException("illegal priority value");
   }
}

public boolean isPersistent()
{
   return persistent;
}

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
           ", visibility=" + visibility +
           ", priority=" + priority +
           ", persistent=" + persistent +
           ", showActions=" + showActions +
           '}';
}
}
