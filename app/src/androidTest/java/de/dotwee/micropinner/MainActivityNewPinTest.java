package de.dotwee.micropinner;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import de.dotwee.micropinner.database.PinDatabase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static de.dotwee.micropinner.TestTools.recreateActivity;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityNewPinTest
{
private static final String LOG_TAG = "MainActivityNewPinTest";
/**
 * Preferred JUnit 4 mechanism of specifying the
 * activity to be launched before each test
 */
@Rule
public ActivityTestRule<MainActivity> activityTestRule =
 new ActivityTestRule<>(MainActivity.class, false, false);

@Before
public void setUp()
{
   activityTestRule.launchActivity(TestTools.launchList());
   TestTools.testDatabase(activityTestRule.getActivity());
   activityTestRule.finishActivity();
}

/**
 * This method performs some input and verifies the EditText's entered value.
 */
@Test
public void testEditTextTitle()
{
   activityTestRule.launchActivity(TestTools.launchNewPin());
   onView(withId(R.id.txtTitleAndContent)).check(matches(hasFocus()));
   final String value = "MicroPinner title input";
   onView(withId(R.id.txtTitleAndContent)).perform(typeText(value)).check(matches(withText(value)));
}

/**
 * This method performs an empty input on the title EditText and
 * clicks on the pin-button. Verifies if a Toast appears.
 */
@Test
public void testEmptyTitleToast()
{
   recreateActivity(activityTestRule);
   
   // perform empty input
   onView(withId(R.id.txtTitleAndContent)).perform(typeText(""));
   
   // click pin button
   onView(withContentDescription(R.string.action_pin)).perform(click());
   
   // verify toast existence
   onView(withText(R.string.message_empty_title)).inRoot(
     withDecorView(not(activityTestRule.getActivity().getWindow().getDecorView())))
    .check(matches(isDisplayed()));
}

/**
 * This method verifies the persist mechanism for user-created pins.
 */
@Test
public void testUserCreateNewPin()
{
   recreateActivity(activityTestRule);
   
   PinDatabase pinDatabase =
    PinDatabase.getInstance(activityTestRule.getActivity().getApplicationContext());
   pinDatabase.deleteAll();
   
   long previousPinAmount = pinDatabase.getCount();
   
   // enter a title
   onView(withId(R.id.txtTitleAndContent)).perform(typeText(LOG_TAG));
   
   // mark with high priority
   String highPriority = activityTestRule.getActivity().getString(R.string.priority_high);
   onView(withId(R.id.spinPriority)).perform(click());
   onData(allOf(is(instanceOf(String.class)), is(highPriority))).perform(click());
   onView(withId(R.id.spinPriority)).check(matches(withSpinnerText(R.string.priority_high)));
   
   // select pin button
   onView(withContentDescription(R.string.action_pin)).perform(click());
   
   // make sure pin exists
   long newPinAmount = pinDatabase.getCount();
   assertEquals(previousPinAmount + 1, newPinAmount);
}
}