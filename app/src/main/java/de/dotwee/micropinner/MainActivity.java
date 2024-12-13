package de.dotwee.micropinner;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import de.dotwee.micropinner.FragList.Mode;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.tools.PreferencesHandler;

/**
 * Created by Lukas Wolfsteiner on 29.10.2015.
 */
public class MainActivity
 extends AppCompatActivity
{
//private static final String DBG = "MainActivity";
public static final String PERMISSION_POST_NOTI;
public static boolean hasPermission = false;

static {
   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU == 33
      PERMISSION_POST_NOTI = permission.POST_NOTIFICATIONS;
   }
   else {
      PERMISSION_POST_NOTI = "android.permission.POST_NOTIFICATIONS";
   }
   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
}

private final List<Frag> fragBackstack = new LinkedList<>();

@Override
public void onBackPressed()
{
   if(onUpMayFinish(true))
      finish();
}

@Override
public boolean onSupportNavigateUp()
{
   if(!onUpMayFinish(false))
      return false;
   return super.onSupportNavigateUp();
}

public boolean onUpMayFinish(boolean cancel)
{
   if(!fragBackstack.get(0).onUpMayFinish(cancel))
      return false;
   return fragPopMayFinish();
}

private boolean fragPopMayFinish()
{
   if(fragBackstack.size() <= 1) {
      return true;
      // finish();
   }
   
   // Frag removed =
   fragBackstack.remove(0);
   Frag current = fragBackstack.get(0);
   getSupportFragmentManager().beginTransaction()
    .replace(R.id.fragment, current)
    .commit();
   
   invalidateActionBar(current);
   return false;
}

void invalidateActionBar(Frag currentFrag)
{
   currentFrag.onPrepareActionBar(getSupportActionBar());
   invalidateMenu();
}

private void fragCommit(Frag frag)
{
   fragBackstack.add(0, frag);
   getSupportFragmentManager().beginTransaction()
    .replace(R.id.fragment, frag)
    .commit();
   
   invalidateActionBar(frag);
}

void showNewPin()
{
   fragCommit(FragEditor.getNewCreatingInstance());
}

void showEditPin(PinSpec pin)
{
   fragCommit(FragEditor.getNewEditingInstance(pin));
}

void showList()
{
   fragCommit(FragList.getNewInstance());
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
   getMenuInflater().inflate(R.menu.app_bar_buttons, menu);
   return true;
}

@Override
public boolean onPrepareOptionsMenu(Menu menu)
{
   fragBackstack.get(0).onPrepareMenu(menu);
   return true;
}

@Override
protected void onCreate(@Nullable Bundle savedInstanceState)
{
   super.onCreate(savedInstanceState);
   
   PreferencesHandler preferencesHandler = PreferencesHandler.getInstance(this);
   preferencesHandler.isFirstUse();
   
   setContentView(R.layout.activity_main);
   
   ActionBar bar = Objects.requireNonNull(getSupportActionBar());
   bar.setHideOnContentScrollEnabled(true);
   bar.setDisplayShowTitleEnabled(true);
   
   // restore state
   Intent intent = getIntent();
   switch(intent.getAction()) {
   case Intent.ACTION_CREATE_NOTE:
      showNewPin();
      break;
   case Intent.ACTION_MAIN:
      showList();
      break;
   default: // case Intent.ACTION_DEFAULT: // ACTION_VIEW
      // deserialize our pin from the intent
      PinSpec pin = (PinSpec) intent.getSerializableExtra(FragEditor.EXTRA_PIN_SPEC);
      if(pin == null) {
         // fallback if ACTION_DEFAULT instead of ACTION_MAIN was used to launch app
         showList();
      }
      else {
         showEditPin(pin);
      }
   }
   
   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && // MARSHMALLOW == 23 // TIRAMISU == 33
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
   int id = item.getItemId();
   
   if(id == R.id.btnCancel) {
      if(onUpMayFinish(true))
         finish();
   }
   else if(id == R.id.btnNew) {
      showNewPin();
   }
   else if(id == R.id.btnDelete1) {
      Frag frag = fragBackstack.get(0);
      if(frag instanceof FragList) {
         FragList fragList = (FragList) frag;
         // delete all selected pins from database
         fragList.deleteSelectedPins();
      }
      else {
         FragEditor fragEditor = (FragEditor) frag;
         // delete frag.editing from database
         fragEditor.deletePin();
      }
   }
   else if(id == R.id.btnDeleteMode) {
      FragList frag = (FragList) fragBackstack.get(0);
      frag.setMode(Mode.DELETE);
   }
   else if(id == R.id.btnOrderMode) {
      FragList frag = (FragList) fragBackstack.get(0);
      frag.setMode(Mode.ORDER);
   }
   else {
      return super.onOptionsItemSelected(item);
   }
   return true;
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

public static ArrayAdapter<String> getPriorityLocalizedStrings(Context ctx)
{
   return new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item,
    ctx.getResources().getStringArray(R.array.array_priorities));
}
}