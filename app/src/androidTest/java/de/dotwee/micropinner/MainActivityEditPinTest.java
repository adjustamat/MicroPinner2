package de.dotwee.micropinner;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;

/**
 * Created by Lukas Wolfsteiner on 06.11.2015.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class MainActivityEditPinTest
{
/**
 * Preferred JUnit 4 mechanism of specifying the
 * activity to be launched before each test
 */
@Rule
public ActivityTestRule<MainActivity> activityTestRule =
 new ActivityTestRule<>(MainActivity.class);

@Before
public void setUp()
{

//   final Intent testIntent =
//    new Intent(activityTestRule.getActivity(), MainActivity.class).putExtra(
//     FragEditor.EXTRA_PIN_SPEC, TestData.testPin);
//
   Intents.init();
   activityTestRule.launchActivity(TestTools.launchEditPin(TestTools.testPins[2]));
}

@Test
public void testPinTitle()
{
   
   // verify pin title
//   onView(withId(R.id.editTextTitle)).check(matches(withText(TestData.testPinTitle)));
}

@Test
public void testPinContent()
{
   
   // verify pin content
//   onView(withId(R.id.txtTitleAndContent)).check(matches(withText(TestData.testPinContent)));
}

/**
 * This method verifies the pin's priority.
 */
@Test
public void testPinPriority()
{
   
   // verify selected priority
   onView(withId(R.id.spinPriority)).check(matches(withSpinnerText(R.string.priority_high)));
}

@After
public void tearDown()
{
   Intents.release();
}
}
