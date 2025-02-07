package de.dotwee.micropinner.database;

import java.util.LinkedList;
import java.util.List;
import androidx.annotation.NonNull;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lukas on 10.08.2016.
 */
public class PinDatabase
 extends SQLiteOpenHelper
{
private static final String TAG = PinDatabase.class.getSimpleName();

// maximums
public static final int MAX_NOTIFICATIONS = 20;
public static final int MAX_PER_PRIO = 5;

// string columns
private static final String COL_TITLE = "txt1";
private static final String COL_CONTENT = "txt2";

// integer columns
private static final String COL_ID = "_id";
//static final String COLUMN_VISIBILITY = "visibility";
private static final String COL_PRIORITY = "pri";
private static final String COL_ORDER = "pos";

// boolean (flag) columns
//static final String COLUMN_PERSISTENT = "persistent";
private static final String COL_SHOW_ACTIONS = "flags";

// table name
private static final String TABLE = "pins";
private static final String[] ALL_COLUMNS = {
 COL_ID,
 COL_TITLE, COL_CONTENT,
 COL_PRIORITY, COL_ORDER,
 COL_SHOW_ACTIONS
};

// database info
private static final String DATABASE_NAME = "comments.db";
private static final int DATABASE_VERSION = 300;// BuildConfig.VERSION_CODE

private static PinDatabase instance = null;

private PinDatabase(@NonNull Context context)
{
   super(context, DATABASE_NAME, null, DATABASE_VERSION);
}

public static synchronized PinDatabase getInstance(@NonNull Context context)
{
   if(instance == null) {
      instance = new PinDatabase(context.getApplicationContext());
   }
   return instance;
}

private static final String SQL_ORDER_BY = COL_PRIORITY + "," + COL_ORDER;

@Override
public void onCreate(SQLiteDatabase sdb)
{
   sdb.execSQL("CREATE TABLE "
                + TABLE + "("
                + COL_ID + " int primary key, "
                + COL_TITLE + " text, "
                + COL_CONTENT + " text, "
                + COL_PRIORITY + " int, "
                + COL_ORDER + " int, "
                + COL_SHOW_ACTIONS + " int);");
   sdb.execSQL("CREATE INDEX pins_prio_order on "
                + TABLE + "(" + SQL_ORDER_BY + ");");
}

@Override
public void onUpgrade(SQLiteDatabase sdb, int oldVersion, int newVersion)
{
   Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion +
               ", which destroys all old data");
   
   sdb.execSQL("DROP TABLE IF EXISTS " + TABLE);
   onCreate(sdb);
}

/**
 * Rearranges two pins with the same priority.
 */
public void changeOrderForPins(long id1, int order1, long id2, int order2)
{
   SQLiteDatabase sdb = getWritableDatabase();
   ContentValues contentValues = new ContentValues();
   
   contentValues.put(COL_ORDER, order1);
   sdb.update(TABLE, contentValues,
    COL_ID + "=" + id1, null);
   
   contentValues.put(COL_ORDER, order2);
   sdb.update(TABLE, contentValues,
    COL_ID + "=" + id2, null);
   
   sdb.close();
}

private int getNewOrderForPriority(SQLiteDatabase sdb, int priorityIndex)
{
   return (int) DatabaseUtils.queryNumEntries(sdb, TABLE,
    COL_PRIORITY + "=" + priorityIndex);
}

public int getNewOrderForPriority(int priority)
{
   SQLiteDatabase sdb = getReadableDatabase();
   int ret = getNewOrderForPriority(sdb, priority);
   sdb.close();
   return ret;
}

/**
 * This method decides whether a new pin should be created or updated in the database.
 * Either creates a pin within the database and gives it a unique id,
 * or updates a pin in the database without changing its id.
 * @param editing
 *  The pin that was edited, or null to create a new pin.
 */
public PinSpec writePin(PinSpec editing,
 String title, String content, int priorityIndex, boolean showActions)
{
   SQLiteDatabase sdb = getWritableDatabase();
   PinSpec ret;
   ContentValues contentValues = new ContentValues();
   contentValues.put(COL_TITLE, title);
   contentValues.put(COL_CONTENT, content);
   contentValues.put(COL_PRIORITY, priorityIndex);
   contentValues.put(COL_SHOW_ACTIONS, showActions ? 1 : 0);
   
   if(editing == null) {
      
      // check if we can create new pin
      int orderWithinPrio = getNewOrderForPriority(sdb, priorityIndex);
      int total = (int) DatabaseUtils.queryNumEntries(sdb, TABLE);
      if(total >= MAX_NOTIFICATIONS || orderWithinPrio >= MAX_PER_PRIO) {
         Log.i(TAG, "Cannot create new pin because there are " + orderWithinPrio +
                     " pins with priority " + priorityIndex + ", and the total is " + total +
                     ". MAX: " + MAX_PER_PRIO + ", total " + MAX_NOTIFICATIONS);
         sdb.close();
         return null;
      }
      
      // create new pin and get new ID from the database
      contentValues.put(COL_ORDER, orderWithinPrio);
      long id = sdb.insert(TABLE, null, contentValues);
      Log.i(TAG, "Created new pin with id " + id);
      logRowCount(sdb);
      ret = new PinSpec(id, title, content, priorityIndex, orderWithinPrio, showActions);
   }
   else {
      
      // update edited pin
      sdb.update(TABLE, contentValues,
       COL_ID + "=" + editing.getId(), null);
      ret = editing;
      ret.setData(title, content, priorityIndex, showActions);
   }
   
   sdb.close();
   return ret;
}

/**
 * This method deletes a pin from the database
 * @param id
 *  the pin to delete
 */
public void deletePin(long id)
{
   SQLiteDatabase sdb = getWritableDatabase();
   boolean success = sdb.delete(TABLE, COL_ID + "=" + id, null) > 0;
   Log.i(TAG, "Deleted pin with id " + id + "; success = " + success);
   logRowCount(sdb);
   sdb.close();
}

public void deleteAll()
{
   SQLiteDatabase sdb = getWritableDatabase();
   Log.i(TAG, "Deleting all rows");
   sdb.delete(TABLE, null, null);
   sdb.close();
}

/**
 * Returns all pins in the database, ordered by PRIORITY, then ORDER.
 * @return A LinkedList with all the pins
 */
@NonNull
public List<PinSpec> getAllPins()
{
   SQLiteDatabase sdb = getReadableDatabase();
   List<PinSpec> list = new LinkedList<>();
   Cursor cursor = sdb.query(TABLE, ALL_COLUMNS,
    null, null,
    null, null, SQL_ORDER_BY);
   while(cursor.moveToNext()) {
//      ContentValues contentValues = new ContentValues();
//      DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
      long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
      String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
      String content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT));
      int priority = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRIORITY));
      int order = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER));
      boolean showActions = (cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_ACTIONS)) != 0);
//      long id = contentValues.getAsLong(COL_ID);
//      String title = contentValues.getAsString(COL_TITLE);
//      String content = contentValues.getAsString(COL_CONTENT);
//      int priority = contentValues.getAsInteger(COL_PRIORITY);
//      int order = contentValues.getAsInteger(COL_ORDER);
//      boolean showActions = (contentValues.getAsInteger(COL_SHOW_ACTIONS) != 0);
      
      PinSpec pinSpec = new PinSpec(id, title, content, priority, order, showActions);
      list.add(pinSpec);
   }
   cursor.close();
   sdb.close();
   return list;
}

public int getCount()
{
   SQLiteDatabase sdb = getReadableDatabase();
   int count = (int) DatabaseUtils.queryNumEntries(sdb, TABLE);
   sdb.close();
   return count;
}

/**
 * Logs the amount of entries in the database.
 * This method gets called on INSERT and DELETE.
 */
private void logRowCount(SQLiteDatabase sdb)
{
   Log.i(TAG, "row count = " + DatabaseUtils.queryNumEntries(sdb, TABLE));
}
}
