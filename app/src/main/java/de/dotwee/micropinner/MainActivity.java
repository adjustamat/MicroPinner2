package de.dotwee.micropinner;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.tools.NotificationTools;
import de.dotwee.micropinner.tools.PreferencesHandler;

/**
 * Created by Lukas Wolfsteiner on 29.10.2015.
 */
public class MainActivity
 extends AppCompatActivity
{
public static final String PERMISSION_POST_NOTI;
public static boolean hasPermission = false;

private static final int displayLogo =
 ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE;
private static final int displayDone =
 ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE;

static {
   if(VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) { // TIRAMISU == 33
      PERMISSION_POST_NOTI = permission.POST_NOTIFICATIONS;
   }
   else {
      PERMISSION_POST_NOTI = "android.permission.POST_NOTIFICATIONS";
   }
   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
}

private Fragment currentFrag;
protected final List<Fragment> fragBackstack = new LinkedList<>();

private  PreferencesHandler preferencesHandler;

private  PinDatabase pinDatabase;


@Override
public boolean onSupportNavigateUp()
{
   if(fragPopBack())
      return true;
   return super.onSupportNavigateUp();
}

/**
 * Initialize the contents of the Activity's standard options menu.  You
 * should place your menu items in to <var>menu</var>.
 *
 * <p>This is only called once, the first time the options menu is
 * displayed.  To update the menu every time it is displayed, see
 * {@link #onPrepareOptionsMenu}.
 *
 * <p>The default implementation populates the menu with standard system
 * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
 * they will be correctly ordered with application-defined menu items.
 * Deriving classes should always call through to the base implementation.
 *
 * <p>You can safely hold on to <var>menu</var> (and any items created
 * from it), making modifications to it as desired, until the next
 * time onCreateOptionsMenu() is called.
 *
 * <p>When you add items to the menu, you can implement the Activity's
 * {@link #onOptionsItemSelected} method to handle them there.
 * @param menu
 *  The options menu in which you place your items.
 * @return You must return true for the menu to be displayed;
 *  if you return false it will not be shown.
 * @see #onPrepareOptionsMenu
 * @see #onOptionsItemSelected
 */
@Override
public boolean onCreateOptionsMenu(Menu menu)
{
   
   boolean result = super.onCreateOptionsMenu(menu);
//   // Using findViewById because NavigationView exists in different layout files
//   // between w600dp and w1240dp
//   NavigationView navView = findViewById(R.id.nav_view);
//   if(navView == null) {
//      // The navigation drawer already has the items including the items in the overflow menu
//      // We only inflate the overflow menu if the navigation drawer isn't visible
   getMenuInflater().inflate(R.menu.menu_navigation/*overflow*/, menu);
//   }
   return result;
//}
}

/**
 * This hook is called whenever an item in your options menu is selected.
 * The default implementation simply returns false to have the normal
 * processing happen (calling the item's Runnable or sending a message to
 * its Handler as appropriate).  You can use this method for any items
 * for which you would like to do processing without those other
 * facilities.
 *
 * <p>Derived classes should call through to the base class for it to
 * perform the default menu handling.</p>
 * @param item
 *  The menu item that was selected.
 * @return boolean Return false to allow normal menu processing to
 *  proceed, true to consume it here.
 * @see #onCreateOptionsMenu
 */
@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item)
{
   //public boolean onOptionsItemSelected(@NonNull MenuItem item)
//{
//   if(item.getItemId() == R.id.nav_settings) {
//      NavController navController =
//       Navigation.findNavController(this, R.id.nav_host_fragment_content_responsive);
//      navController.navigate(R.id.nav_settings);
//   }
//   return super.onOptionsItemSelected(item);
//}
   
   return super.onOptionsItemSelected(item);
}

private void fragCommit(Fragment frag)
{
   fragBackstack.add(0, frag);
   getSupportFragmentManager().beginTransaction()
    .replace(R.id.fragment, frag)
    .commit();
   ActionBar bar = Objects.requireNonNull(getSupportActionBar());
   bar.setDisplayOptions(displayDone);
   
   
/*
TODO:
android:id="@+id/buttonCancel"
    android:text="@string/dialog_action_cancel"
android:id="@+id/buttonPin"
    android:text="@string/dialog_action_pin"
    
    icon = @mipmap/ic_launcher or checkmark
    label = title_new_pin, title_edit_pin, app_name

 */
 
}

private boolean fragPopBack()
{
//      Log.d(DBG, "fragPopBack - backstack.size: " + fragBackstack.size());
   if(fragBackstack.size() <= 1) {
      return false; // Activity.finish()
   }
   
   Fragment removed = fragBackstack.remove(0);
   getSupportFragmentManager().beginTransaction()
    .replace(R.id.fragment, fragBackstack.get(0))
    .commit();
   
   if(fragBackstack.size() == 1) {
      ActionBar bar = Objects.requireNonNull(getSupportActionBar());
      bar.setDisplayOptions(displayLogo);
   }
   
   
/*
TODO:
android:id="@+id/buttonCancel"
    android:text="@string/dialog_action_cancel"
android:id="@+id/buttonPin"
    android:text="@string/dialog_action_pin"
    
    icon = @mipmap/ic_launcher or checkmark
    label = title_new_pin, title_edit_pin, app_name

 */
   
   
   return true;
}

/**
 * This method checks if the user's device is a tablet, depending on the official resource {@link
 * Configuration}.
 * @param context
 *  needed to get resources
 * @return true if device screen size is greater than 6 inches
 */
private static boolean isTablet(@NonNull Context context)
{
   return (context.getResources().getConfiguration().screenLayout &
            Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
}

@Override
protected void onCreate(@Nullable Bundle savedInstanceState)
{
   super.onCreate(savedInstanceState);
   
   preferencesHandler = PreferencesHandler.getInstance(this);
   preferencesHandler.isFirstUse();
   pinDatabase = PinDatabase.getInstance(getApplicationContext());
   
   this.setContentView(R.layout.activity_main);
   
   ActionBar bar = Objects.requireNonNull(getSupportActionBar());
   bar.setHideOnContentScrollEnabled(true);
   bar.setHomeAsUpIndicator(R.drawable.ic_done);
   
   fragCommit(new FragEditor());
   
   
   
   // restore state
   // TODO: getIntent();
   
   if(preferencesHandler.isNotificationActionsEnabled()) {
      CheckBox checkBox = activity.findViewById(R.id.chkShowActions);
      checkBox.setChecked(true);
   }
   
   
   
//   TextView textViewTitle = activity.findViewById(R.id.dialogTitle);
//   if(textViewTitle != null) {
//      textViewTitle.setText(hasPin ? R.string.title_edit_pin : R.string.app_name);
//   }
   
   Button buttonNegative = activity.findViewById(R.id.buttonCancel);
   if(buttonNegative != null) {
      buttonNegative.setText(
       hasPin ? R.string.dialog_action_delete : R.string.dialog_action_cancel);
   }
   
   
   
   
   if(VERSION.SDK_INT >= VERSION_CODES.M && // MARSHMALLOW == 23 // TIRAMISU == 33
       PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(
        this, PERMISSION_POST_NOTI)) {
      if(shouldShowRequestPermissionRationale(PERMISSION_POST_NOTI)) {
         Toast.makeText(this, R.string.requires_your_permission, Toast.LENGTH_SHORT)
          .show();
      }
      requestPermissions(new String[] {PERMISSION_POST_NOTI}, 1);
   }
   else {
      hasPermission = true;
      NotificationTools.restoreAllPins(this);
   }
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
 @NonNull int[] grantResults)
{
   super.onRequestPermissionsResult(requestCode, permissions, grantResults);
   
   for(int i = 0; i < permissions.length; i++) {
      if(grantResults[i] == PackageManager.PERMISSION_GRANTED &&
          permissions[i].equals(permission.POST_NOTIFICATIONS)) {
         hasPermission = true;
         NotificationTools.restoreAllPins(this);
      }
   }
}


///**
// * This method handles the click on the negative dialog button.
// */
//public void onButtonNegative()
//{
//   if(hasParentPin()) {
//      notificationManager.cancel(intentPin.getIdAsInt());
//
//      Intent intent = new Intent(activity, OnDeleteReceiver.class);
//      intent.putExtra(NotificationTools.EXTRA_INTENT, intentPin);
//      activity.sendBroadcast(intent);
//   }
//
//   activity.finish();
//}

/**
 * This method handles the click on the positive dialog button.
 */
public void onButtonPositive()
{
   if(activity.getPinTitle().isEmpty()) {
      Toast.makeText(activity, R.string.message_empty_title, Toast.LENGTH_SHORT).show();
      Log.d(TAG, "user entered no title, can't finish pin.");
      return;
   }
//   if(getParentPin() != null) {
//
//   }
//   else {
//
//   }
   PinSpec newPin = pinDatabase.writePin(getParentPin(),
    activity.getPinTitle(), activity.getPinContent(),
    activity.getPriority(), activity.showActions());
   
   NotificationTools.notify(activity, newPin);
   
   activity.finish();
//   catch(Exception e) {
//      Log.d(TAG, "onButtonPositive()", e);
//   }
}


//@Override
//public void setContentView(@LayoutRes int layoutResID)
//{
//   if(isTablet(this)) {
//
//      DisplayMetrics metrics = getResources().getDisplayMetrics();
//      int newWidth = Math.round(320 * (metrics.densityDpi / 160f));
//
//      setContentView(View.inflate(this, layoutResID, null),
//       new FrameLayout.LayoutParams(newWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
//   }
//   else {
//      super.setContentView(layoutResID);
//   }
//}

public static ArrayAdapter<String> getPriorityLocalizedStrings(Context ctx)
{
   return new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item,
    ctx.getResources().getStringArray(R.array.array_priorities));
}

}


