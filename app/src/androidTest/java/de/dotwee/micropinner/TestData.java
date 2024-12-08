package de.dotwee.micropinner;

import android.app.Notification;

import de.dotwee.micropinner.database.PinSpec;

/**
 * Created by lukas on 20.07.2016.
 */
public final class TestData
{
public static final PinSpec[] testPins = {
 new PinSpec("testPinTitle", "testPinContent",
  PinSpec.priorityToIndex(Notification.PRIORITY_HIGH), true),
 new PinSpec("testPinTitle", "testPinContent",
  PinSpec.priorityToIndex(Notification.PRIORITY_HIGH), true),
 new PinSpec("testPinTitle", "testPinContent",
  PinSpec.priorityToIndex(Notification.PRIORITY_DEFAULT), true),
 new PinSpec("testPinTitle", "testPinContent",
  PinSpec.priorityToIndex(Notification.PRIORITY_LOW), true),
};
/*
switch(priority) {
   case 0:
      return Notification.PRIORITY_HIGH;
   case 1:
      return Notification.PRIORITY_DEFAULT;
   case 2:
      return Notification.PRIORITY_LOW;
   case 3:
      return Notification.PRIORITY_MIN;
 */

}
