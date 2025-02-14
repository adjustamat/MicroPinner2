package de.dotwee.micropinner;

import java.util.LinkedList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import de.dotwee.micropinner.database.Pin;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.ui.Frag;
import de.dotwee.micropinner.ui.FragEditor;
import de.dotwee.micropinner.ui.FragList;
import de.dotwee.micropinner.ui.FragList.Mode;

/**
 * Created by Lukas Wolfsteiner on 29.10.2015.
 */
public class MainActivity
 extends AppCompatActivity
{
//private static final String DBG = "MainActivity";
public static final String PERMISSION_POST_NOTI;

static {
   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU == 33
      PERMISSION_POST_NOTI = permission.POST_NOTIFICATIONS;
   }
   else {
      PERMISSION_POST_NOTI = "android.permission.POST_NOTIFICATIONS";
   }
   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
}

private final LinkedList<Frag> backStack = new LinkedList<>();

@Override
public void onBackPressed()
{
   if(onUpMayFinish(false))
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
   // first ask the Fragment if it may be closed
   if(!backStack.getFirst().mayCloseFragment(cancel))
      return false;
   // go back to the previous fragment
   return popBackMayFinish();
}

private boolean popBackMayFinish()
{
   if(backStack.size() <= 1) {
      return true; // nothing to pop, may finish()
   }
   
   // pop a fragment from the stack
   backStack.removeFirst();
   
   // return to previous fragment
   Frag current = backStack.getFirst();
   getSupportFragmentManager().beginTransaction()
    .replace(R.id.fragment, current)
    .commit();
   
   // invalidate UI
   updateActionBar(current);
   return false;
}

private void fragCommit(Frag frag)
{
   // push the fragment onto the stack
   backStack.addFirst(frag);
   getSupportFragmentManager().beginTransaction()
    .replace(R.id.fragment, frag)
    .commit();
   
   // invalidate UI
   updateActionBar(frag);
}

public void updateActionBar(Frag currentFrag)
{
   currentFrag.onPrepareActionBar(getSupportActionBar());
   invalidateMenu();
}

public void showNewPin()
{
   fragCommit(FragEditor.getNewCreatingInstance());
}

public void showEditPin(Pin pin)
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
   backStack.getFirst().onPrepareMenu(menu);
   return true;
}

@Override
protected void onCreate(@Nullable Bundle savedInstanceState)
{
   super.onCreate(savedInstanceState);
   
   PreferencesHandler preferencesHandler = PreferencesHandler.getInstance(this);
   preferencesHandler.isFirstUse();
   
   setContentView(R.layout.activity_main);
   
   // set or restore state
   Intent intent = getIntent();
   switch(intent.getAction()) {
   case Intent.ACTION_CREATE_NOTE:
      showNewPin();
      break;
      
   case Intent.ACTION_MAIN:
      showList();
      break;
      
   default: // case Intent.ACTION_DEFAULT: // ACTION_DEFAULT == ACTION_VIEW
      // deserialize our pin from the intent
      Pin pin = (Pin) intent.getSerializableExtra(FragEditor.EXTRA_SERIALIZABLE_PIN);
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
         Toast.makeText(this, R.string.message_require_permission, Toast.LENGTH_SHORT)
          .show();
      }
      requestPermissions(new String[] {PERMISSION_POST_NOTI}, 1);
   }
   else {
      NotificationTools.hasPermission = true;
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
   else if(id == R.id.btnDelete) {
      Frag frag = backStack.getFirst();
      // Delete from database:
      // FragEditor deletes the pin being edited.
      // FragList deletes all selected pins.
      frag.deleteFromDatabase(PinDatabase.getInstance(this));
   }
   else if(id == R.id.btnDeleteMode) {
      FragList fragList = (FragList) backStack.getFirst();
      fragList.setMode(Mode.DELETE);
   }
   else if(id == R.id.btnOrderMode) {
      FragList fragList = (FragList) backStack.getFirst();
      fragList.setMode(Mode.ORDER);
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
         NotificationTools.hasPermission = true;
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