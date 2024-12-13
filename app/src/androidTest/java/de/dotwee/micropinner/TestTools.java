package de.dotwee.micropinner;

import java.util.Iterator;
import java.util.List;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.tools.PreferencesHandler;

/**
 * Created by lukas on 20.07.2016.
 */
public final class TestTools
{

public static final PinSpec[] testPins = {
 new PinSpec(0, "First", "zero, high priority, order 0",
  PinSpec.priorityToIndex(Notification.PRIORITY_HIGH), 0, true),
 new PinSpec(1, "Third", "one, default priority",
  PinSpec.priorityToIndex(Notification.PRIORITY_DEFAULT), 0, true),
 new PinSpec(2, "Second", "two, high priority, order 1",
  PinSpec.priorityToIndex(Notification.PRIORITY_HIGH), 1, true),
 new PinSpec(3, "Fourth", "three, low priority",
  PinSpec.priorityToIndex(Notification.PRIORITY_LOW), 0, true),
};

public static void testDatabase(Context ctx)
{
   PinDatabase db = PinDatabase.getInstance(ctx);
   for(PinSpec pin : testPins) {
      db.writePin(null, pin.getTitle(), pin.getContent(),
       pin.getPriorityIndex(), pin.isShowActions());
   }
   List<PinSpec> allPins = db.getAllPins();
   Iterator<PinSpec> iterator = allPins.iterator();
   for(PinSpec pin : testPins) {
      pin.test(iterator.next());
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

public static Intent launchEditPin(PinSpec pin)
{
   Intents.init();
   Intent launchIntent = new Intent(Intent.ACTION_VIEW);
   launchIntent.setClassName("de.dotwee.micropinner", "de.dotwee.micropinner.MainActivity");
   launchIntent.putExtra(FragEditor.EXTRA_PIN_SPEC, pin);
   return launchIntent;
}
}
