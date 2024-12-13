package de.dotwee.micropinner;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import android.content.res.Configuration;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static de.dotwee.micropinner.TestTools.recreateActivity;

/**
 * Created by Lukas Wolfsteiner on 06.11.2015.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityThemeTest
{

/**
 * Preferred JUnit 4 mechanism of specifying the
 * activity to be launched before each test
 */
@Rule
public ActivityTestRule<MainActivity> activityTestRule =
 new ActivityTestRule<>(MainActivity.class);

//@RequiresApi(api=Build.VERSION_CODES.JELLY_BEAN_MR1)
@ColorInt
private static int getAccentColor(@NonNull ActivityTestRule<MainActivity> activityTestRule,
 boolean light)
{
   Configuration configuration = new Configuration();
   configuration.uiMode = light ? Configuration.UI_MODE_NIGHT_NO : Configuration.UI_MODE_NIGHT_YES;
   
   return ContextCompat.getColor(
    activityTestRule.getActivity().createConfigurationContext(configuration), R.color.accent);
}

//@RequiresApi(api=Build.VERSION_CODES.JELLY_BEAN_MR1)
@ColorInt
private static int getBackgroundColor(@NonNull ActivityTestRule<MainActivity> activityTestRule,
 boolean light)
{
   Configuration configuration = new Configuration();
   configuration.uiMode = light ? Configuration.UI_MODE_NIGHT_NO : Configuration.UI_MODE_NIGHT_YES;
   
   return ContextCompat.getColor(
    activityTestRule.getActivity().createConfigurationContext(configuration), R.color.background);
}

private static void changeUiMode(@NonNull ActivityTestRule<MainActivity> activityTestRule, int mode)
{
   activityTestRule.getActivity()
    .runOnUiThread(() -> activityTestRule.getActivity().getDelegate().setLocalNightMode(mode));
   
   // recreate activity to apply theme
   recreateActivity(activityTestRule);
}

@Before
public void setUp()
{
   recreateActivity(activityTestRule);
}

/**
 * This method verifies the light theme's accent.
 */
//@RequiresApi(api=Build.VERSION_CODES.JELLY_BEAN_MR1)
@Test
public void testThemeLightAccent()
{
   changeUiMode(activityTestRule, AppCompatDelegate.MODE_NIGHT_NO);
   
   // check color for all TextView descriptions
   for(int description : new int[] {
    R.string.txtTitleAndContent, R.string.spinPriority,
   }) {
      onView(withText(description)).check(
       matches(Matches.withTextColor(getAccentColor(activityTestRule, true))));
   }
}

/**
 * This method verifies the light theme's background.
 */
//@RequiresApi(api=Build.VERSION_CODES.JELLY_BEAN_MR1)
@Test
public void testThemeLightBackground()
{
   changeUiMode(activityTestRule, AppCompatDelegate.MODE_NIGHT_NO);
   
   onView(withId(android.R.id.content)).check(
    matches(Matches.withBackgroundColor(getBackgroundColor(activityTestRule, true))));
}

/**
 * This method verifies the light theme's accent.
 */
//@RequiresApi(api=Build.VERSION_CODES.JELLY_BEAN_MR1)
@Test
public void testThemeDarkAccent()
{
   changeUiMode(activityTestRule, AppCompatDelegate.MODE_NIGHT_YES);
   
   // check color for all TextView descriptions
   for(int description : new int[] {
    R.string.txtTitleAndContent, R.string.spinPriority
   }) {
      onView(withText(description)).check(matches(Matches.withTextColor(
       getAccentColor(activityTestRule, false)
      )));
   }
}

/**
 * This method verifies the dark theme's background.
 */
//@RequiresApi(api=Build.VERSION_CODES.JELLY_BEAN_MR1)
@Test
public void testThemeDarkBackground()
{
   changeUiMode(activityTestRule, AppCompatDelegate.MODE_NIGHT_YES);
   
   onView(withId(android.R.id.content)).check(matches(Matches.withBackgroundColor(
    getBackgroundColor(activityTestRule, false))));
}
}
