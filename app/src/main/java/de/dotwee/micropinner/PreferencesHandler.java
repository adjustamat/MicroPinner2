package de.dotwee.micropinner;

import androidx.annotation.NonNull;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesHandler
{
private static final String PREF_FIRST_USE = "pref_firstuse";
private static final String PREF_SHOW_NOTIFICATION_ACTIONS = "pref_shownotificationactions";

private static PreferencesHandler instance;
private final SharedPreferences preferences;

private PreferencesHandler(@NonNull Context context)
{
   this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
}

public static synchronized PreferencesHandler getInstance(Context context)
{
   if(instance == null) {
      instance = new PreferencesHandler(context.getApplicationContext());
   }
   return instance;
}

public boolean isFirstUse()
{
   boolean ret = false;
   
   if(!preferences.contains(PREF_FIRST_USE)) {
      preferences.edit().putBoolean(PREF_FIRST_USE, true).apply();
      ret = true;
   }
   
   return ret;
}

public boolean isNotificationActionsEnabled()
{
   return preferences.getBoolean(PREF_SHOW_NOTIFICATION_ACTIONS, false);
}

public void setShowActionsEnabled(boolean b)
{
   preferences.edit().putBoolean(PREF_SHOW_NOTIFICATION_ACTIONS, b).apply();
}
}
