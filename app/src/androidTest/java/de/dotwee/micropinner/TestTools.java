package de.dotwee.micropinner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.Pin;
import de.dotwee.micropinner.ui.FragEditor;

/**
 * Created by lukas on 20.07.2016.
 */
public final class TestTools
{
private static final String DBG = "TestTools";
public static final Pin[] testPins = {
 new Pin(0, "Second (high)", "high priority order 1, ID=zero",
  Pin.priorityToIndex(Notification.PRIORITY_HIGH), 1, true),
 new Pin(1, "Third (default)", "default priority, ID=one",
  Pin.priorityToIndex(Notification.PRIORITY_DEFAULT), 0, true),
 new Pin(2, "First (high)", "high priority order 0, ID=two",
  Pin.priorityToIndex(Notification.PRIORITY_HIGH), 0, false),
 new Pin(3, "Fourth (low)", "low priority, ID=three",
  Pin.priorityToIndex(Notification.PRIORITY_LOW), 0, true),
};

public static void testDatabase(Context ctx)
{
   PinDatabase db = PinDatabase.getInstance(ctx);
   for(Pin pin : testPins) {
      db.writePin(null, pin.getTitle(), pin.getContent(),
       pin.getPriorityIndex(), pin.isShowActions());
   }
   List<Pin> allPins = db.getAllPins(); // ordered by PRIORITY then ORDER
   Iterator<Pin> iterator = allPins.iterator();
   
   // order by PRIORITY then ORDER:
   Arrays.sort(testPins, new Comparator<Pin>()
   {
      @Override
      public int compare(Pin o1, Pin o2)
      {
         if(o1.getPriorityIndex() == o2.getPriorityIndex()) {
            if(o1.getOrder() == o2.getOrder())
               return 0;
            else if(o1.getOrder() > o2.getOrder())
               return 1;
            return -1;
         }
         else if(o1.getPriorityIndex() > o2.getPriorityIndex())
            return 1;
         return -1;
      }
   });
   
   for(Pin testPin : testPins) {
      Log.d(DBG, "testDatabase() - " + testPin.test(iterator.next()));
   }
   assert !(iterator.hasNext());
   
}

/**
 * This method returns an instance of {@link PreferencesHandler}
 * for an activity test rule.
 * @param activityTestRule
 *  Source to get the PreferenceHandler.
 * @return An instance of {@link PreferencesHandler}
 */
public static PreferencesHandler getPreferencesHandler(
 ActivityTestRule<MainActivity> activityTestRule)
{
   return PreferencesHandler.getInstance(activityTestRule.getActivity());
}

/**
 * This method recreates the main activity in order to apply
 * themes or reload the preference cache.
 * @param activityTestRule
 *  Source to get access to the activity.
 */
public static void recreateActivity(final ActivityTestRule<MainActivity> activityTestRule)
{
   activityTestRule.getActivity().runOnUiThread(() -> activityTestRule.getActivity().recreate());
}

public static Intent launchList()
{
   Intents.init();
   Intent launchIntent = new Intent(Intent.ACTION_MAIN);
   launchIntent.setClassName("de.dotwee.micropinner", "de.dotwee.micropinner.MainActivity");
   return launchIntent;
}

public static Intent launchNewPin()
{
   Intents.init();
   Intent launchIntent = new Intent(Intent.ACTION_CREATE_NOTE);
   launchIntent.setClassName("de.dotwee.micropinner", "de.dotwee.micropinner.MainActivity");
   return launchIntent;
}

public static Intent launchEditPin(Pin pin)
{
   Intents.init();
   Intent launchIntent = new Intent(Intent.ACTION_VIEW);
   launchIntent.setClassName("de.dotwee.micropinner", "de.dotwee.micropinner.MainActivity");
   launchIntent.putExtra(FragEditor.EXTRA_SERIALIZABLE_PIN, pin);
   return launchIntent;
}
}
