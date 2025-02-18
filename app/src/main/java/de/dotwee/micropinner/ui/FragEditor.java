package de.dotwee.micropinner.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import de.dotwee.micropinner.MainActivity;
import de.dotwee.micropinner.NotificationTools;
import de.dotwee.micropinner.PreferencesHandler;
import de.dotwee.micropinner.R;
import de.dotwee.micropinner.database.Pin;
import de.dotwee.micropinner.database.PinDatabase;

public class FragEditor
 extends Frag
{
private static final String DBG = "FragEditor";

public static final String ACTION_SETTINGS;
public static final String EXTRA_SETTINGS_PKG;

static {
   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // OREO == 26
      ACTION_SETTINGS = Settings.ACTION_APP_NOTIFICATION_SETTINGS;
      EXTRA_SETTINGS_PKG = Settings.EXTRA_APP_PACKAGE;
   }
   else {
      ACTION_SETTINGS = "android.settings.APP_NOTIFICATION_SETTINGS";
      EXTRA_SETTINGS_PKG = "android.provider.extra.APP_PACKAGE";
   }
}

public final static String EXTRA_SERIALIZABLE_PIN = "adjustamat.pin";

public static FragEditor getNewCreatingInstance()
{
   return new FragEditor();
}

public static FragEditor getNewEditingInstance(Pin editing)
{
   FragEditor ret = new FragEditor();
   Log.d(DBG, "getNewEditingInstance() - ID = " + editing.getID());
   ret.editing = editing;
   return ret;
}

private FragEditor()
{
}

private Pin editing = null;

ArrayAdapter<String> priorityLocalizedStrings;
private Spinner spinPriority;
private EditText txtTitleAndContent;
private CheckBox chkShowActions;

/**
 * Called to have the fragment instantiate its user interface view.
 *
 * <p>It is recommended to <strong>only</strong> inflate the layout in this method and move
 * logic that operates on the returned View to {@link #onViewCreated(View, Bundle)}.
 *
 * <p>If you return a View from here, you will later be called in
 * {@link #onDestroyView} when the view is being released.
 * @param inflater
 *  The LayoutInflater object that can be used to inflate
 *  any views in the fragment,
 * @param container
 *  If non-null, this is the parent view that the fragment's
 *  UI should be attached to.  The fragment should not add the view itself,
 *  but this can be used to generate the LayoutParams of the view.
 * @param savedInstanceState
 *  If non-null, this fragment is being re-constructed
 *  from a previous saved state as given here.
 * @return Return the View for the fragment's UI, or null.
 */
@SuppressLint("SetTextI18n")
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
 @Nullable Bundle savedInstanceState)
{
   if(savedInstanceState != null && editing == null) {
      editing = (Pin) savedInstanceState.getSerializable(EXTRA_SERIALIZABLE_PIN);
   }
   
   View root = inflater.inflate(R.layout.frag_editor, container, false);
   Context ctx = requireContext();
   
   // initialize View fields
   chkShowActions = root.findViewById(R.id.chkShowActions);
   spinPriority = root.findViewById(R.id.spinPriority);
   txtTitleAndContent = root.findViewById(R.id.txtTitleAndContent);
   
   // store the set value for chkShowActions as default for the future
   chkShowActions.setOnCheckedChangeListener(
    (buttonView, isChecked) -> PreferencesHandler.getInstance(getContext())
                                .setShowActionsEnabled(isChecked)
   );
   
   // load choices for spinPriority
   priorityLocalizedStrings = MainActivity.getPriorityLocalizedStrings(ctx);
   priorityLocalizedStrings.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
   spinPriority.setAdapter(priorityLocalizedStrings);
   
   if(editing != null) { // Editing existing pin
      // set priority
      spinPriority.setSelection(editing.getPriorityIndex(), false);
      
      // set title and content
      if(editing.getContent().isEmpty()) {
         txtTitleAndContent.setText(editing.getTitle());
      }
      else {
         txtTitleAndContent.setText(editing.getTitle() + "\n" + editing.getContent());
      }
   } // Editing existing pin (editing != null)
   
   else { // Creating new pin (editing == null)
      // load default value for chkShowActions
      chkShowActions.setChecked(
       PreferencesHandler.getInstance(ctx).isNotificationActionsEnabled()
      );
      
      // default priority for new pin
      int defaultPrioIndex = Pin.priorityToIndex(Notification.PRIORITY_DEFAULT);
      spinPriority.setSelection(defaultPrioIndex, false);
   } // Creating new pin (editing == null)
   
   // btnMoreSettings takes user to Notification settings
   Button btnMoreSettings = root.findViewById(R.id.btnMoreSettings);
   btnMoreSettings.setOnClickListener(v -> {
      // first, create new pin if needed
      if(editing == null) {
         editing = savePin();
         
         // if unable to create pin, do not navigate away from this fragment
         if(editing == null)
            return;
      }
      
      // show android's notification settings for this app (if running on OREO or later,
      // create and show settings for the Pin's NotificationChannel)
      Intent intent;
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // OREO == 26
         // OREO or later: first, create or update channel
         String channelID = editing.getNotificationChannelID();
         NotificationTools.createChannel(
          (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE),
          channelID,
          editing.getNotificationChannelName(priorityLocalizedStrings),
          editing.getImportance()
         );
         // OREO or later: open notification settings for the specified channel
         intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
         intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelID);
      }
      else { // pre-OREO: open notification settings for this app
         intent = new Intent(ACTION_SETTINGS);
      }
      // specify this app, then navigate to Android's settings
      intent.putExtra(EXTRA_SETTINGS_PKG, ctx.getPackageName());
      intent.putExtra("app_package", ctx.getPackageName());
      intent.putExtra("app_uid", ctx.getApplicationInfo().uid);
      ctx.startActivity(intent);
   });
   
   return root;
}

/**
 * Called to ask the fragment to save its current dynamic state, so it
 * can later be reconstructed in a new instance if its process is
 * restarted.  If a new instance of the fragment later needs to be
 * created, the data you place in the Bundle here will be available
 * in the Bundle given to {@link #onCreate(Bundle)},
 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
 * {@link #onViewCreated(View, Bundle)}.
 * @param outState
 *  Bundle in which to place your saved state.
 */
@Override
public void onSaveInstanceState(@NonNull Bundle outState)
{
   super.onSaveInstanceState(outState);
   outState.putSerializable(EXTRA_SERIALIZABLE_PIN, editing);
}

@Override
public boolean mayCloseFragment(boolean cancel)
{
   // When cancelling: do not save, just close this fragment.
   if(cancel)
      return true;
   
   // try saving the pin. if unable to create pin, do not close.
   editing = savePin();
   if(editing == null)
      return false;
   
   // show the edited Notification, then close this fragment
   NotificationTools.showPin(requireContext(), editing);
   return true;
}

private Pin savePin()
{
   // get title and optional text content from EditText
   String titleAndContent = txtTitleAndContent.getText().toString().trim();
   if(titleAndContent.isEmpty()) {
      Toast.makeText(requireContext(), R.string.message_empty_title, Toast.LENGTH_SHORT).show();
      Log.d(DBG, "user entered no title, can't close FragEditor.");
      return null;
   }
   String title;
   String content;
   int split = titleAndContent.indexOf('\n');
   if(split == -1) {
      // only one row, no newline - no content
      title = titleAndContent;
      content = "";
   }
   else if(split == titleAndContent.length() - 1) {
      // only one row, ends with newline - no content
      title = titleAndContent.substring(0, split);
      content = "";
   }
   else {
      // more than one row - content is non-empty.
      title = titleAndContent.substring(0, split);
      content = titleAndContent.substring(split + 1);
   }
   
   // title is required!
   if(title.isEmpty()) {
      Toast.makeText(requireContext(), R.string.message_empty_title, Toast.LENGTH_SHORT).show();
      Log.d(DBG, "user entered no title, can't close FragEditor.");
      return null;
   }
   
   // update database
   PinDatabase pinDatabase = PinDatabase.getInstance(requireContext());
   Pin written = pinDatabase.writePin(editing, title, content,
    spinPriority.getSelectedItemPosition(), chkShowActions.isChecked());
   // check for database being full
   if(written == null) {
      // user has to change priority or cancel - pin cannot be saved
      Toast.makeText(requireContext(), R.string.message_too_many, Toast.LENGTH_LONG).show();
      return null;
   }
   return written;
}

@Override
public void onPrepareActionBar(ActionBar bar)
{
   bar.setHomeActionContentDescription(R.string.action_pin);
   bar.setHomeAsUpIndicator(R.drawable.ic_done);
   bar.setTitle(editing != null ? R.string.title_edit_pin : R.string.title_new_pin);
   bar.setDisplayHomeAsUpEnabled(true);
   bar.setDisplayUseLogoEnabled(false);
   bar.setDisplayShowTitleEnabled(true);
}

@Override
public void onPrepareMenu(Menu menu)
{
   // show cancel
   menu.findItem(R.id.btnCancel).setVisible(true);
   
   // If editing existing pin, show delete
   MenuItem btnDelete = menu.findItem(R.id.btnDelete);
   btnDelete.setVisible(editing != null);
   btnDelete.setEnabled(editing != null);
   
   menu.findItem(R.id.btnDeleteMode).setVisible(false);
   menu.findItem(R.id.btnOrderMode).setVisible(false);
   menu.findItem(R.id.btnNew).setVisible(false);
}

/**
 * Delete the pin being edited.
 * @param db
 *  the PinDatabase
 */
public void deleteFromDatabase(PinDatabase db)
{
   db.deletePin(editing.getID());
}
}
