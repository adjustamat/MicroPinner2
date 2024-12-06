package de.dotwee.micropinner.presenter;

import java.io.Serializable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.dotwee.micropinner.R;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.receiver.OnDeleteReceiver;
import de.dotwee.micropinner.tools.NotificationTools;
import de.dotwee.micropinner.tools.PreferencesHandler;
import de.dotwee.micropinner.view.MainDialog;

/**
 * Created by Lukas Wolfsteiner on 29.10.2015.
 */
public class MainPresenter
{
private static final String TAG = MainPresenter.class.getSimpleName();
private final PreferencesHandler preferencesHandler;
private final NotificationManager notificationManager;
private final MainDialog activity;

private final PinDatabase pinDatabase;
private final Intent intent;
private PinSpec parentPin;

public MainPresenter(@NonNull MainDialog activity, @NonNull Intent intent)
{
   this.preferencesHandler = PreferencesHandler.getInstance(activity);
   this.activity = activity;
   this.intent = intent;
   
   pinDatabase = PinDatabase.getInstance(activity.getApplicationContext());
   
   notificationManager =
    (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
   
   // check if first use
   preferencesHandler.isFirstUse();

//   if(preferencesHandler.isFirstUse()) {
   // friendly notification that visibility is broken for SDK < 21
//      if(Build.VERSION.SDK_INT < 21) {
//         Toast.makeText(activity,
//          activity.getResources().getText(R.string.message_visibility_unsupported),
//          Toast.LENGTH_LONG).show();
//      }
//   }
}

/**
 * This method handles the click on the positive dialog button.
 */
public void onButtonPositive()
{
   try {
      PinSpec newPin = toPin(); // toPin() throws Exception if user entered no title
      
      if(hasParentPin()) {
         newPin.setId(parentPin.getId());
      }
      
      pinDatabase.writePin(newPin);
      NotificationTools.notify(activity, newPin);
      
      activity.finish();
   }
   catch(Exception e) {
      Log.d(TAG, "onButtonPositive()", e);
   }
}

/**
 * This method handles the click on the negative dialog button.
 */
public void onButtonNegative()
{
   if(hasParentPin()) {
      notificationManager.cancel(parentPin.getIdAsInt());
      
      Intent intent = new Intent(activity, OnDeleteReceiver.class);
      intent.putExtra(NotificationTools.EXTRA_INTENT, parentPin);
      activity.sendBroadcast(intent);
   }
   
   activity.finish();
}

public void restore()
{
   
   // restore the switch's state if advanced is enabled
   if(preferencesHandler.isAdvancedUsed()) {
      SwitchCompat advancedSwitch = activity.findViewById(R.id.switchAdvanced);
      if(advancedSwitch != null) {
         advancedSwitch.setChecked(true);
      }
   }
   
   // restore show-actions checkbox
   if(preferencesHandler.isNotificationActionsEnabled()) {
      CheckBox checkBox = activity.findViewById(R.id.checkBoxShowActions);
      checkBox.setChecked(true);
   }
   
   // restore advanced layout
   this.onViewExpand(preferencesHandler.isAdvancedUsed());
   
   // notify about provided intent
   notifyAboutParentPin();
}

/**
 * This method handles the click on the show-actions checkbox.
 */
public void onShowActions()
{
   CheckBox checkBox = activity.findViewById(R.id.checkBoxShowActions);
   preferencesHandler.setNotificationActionsEnabled(checkBox.isChecked());
}

/**
 * This method handles the expand action.
 * @param expand
 *  If view should expand or not.
 */
public void onViewExpand(boolean expand)
{
   int[] expandedIds = new int[] {R.id.checkBoxPersistentPin, R.id.checkBoxShowActions};
   
   for(int id : expandedIds) {
      View view = activity.findViewById(id);
      
      if(view != null) {
         view.setVisibility(expand ? View.VISIBLE : View.GONE);
      }
   }
   
   preferencesHandler.setAdvancedUse(expand);
}

/**
 * This method checks if a parent pin exists.
 */
public boolean hasParentPin()
{
   if(intent != null) {
      Serializable extra = intent.getSerializableExtra(NotificationTools.EXTRA_INTENT);
      
      if(extra instanceof PinSpec) {
         this.parentPin = (PinSpec) extra;
         return true;
      }
   }
   
   return false;
}

/**
 * This method creates a {@link PinSpec} from the view.
 * @return A new {@link PinSpec}
 * @throws Exception
 *  if pin could not be created
 */
@NonNull
public PinSpec toPin()
 throws Exception
{
   if(activity.getPinTitle().isEmpty()) {
      Toast.makeText(activity, R.string.message_empty_title, Toast.LENGTH_SHORT).show();
      throw new Exception("user entered no title, can't create pin.");
   }
   else {
      return new PinSpec(activity.getPinTitle(), activity.getPinContent(),
       activity.getVisibility(), activity.getPriority(),
       activity.isPersistent(), activity.showActions());
   }
}

/**
 * This method returns the corresponding view of the presenter.
 * @return A non null {@link Activity} activity.
 */
@NonNull
public Activity getView()
{
   return this.activity;
}

/**
 * This method notifies all layouts about the parent pin.
 */
public void notifyAboutParentPin()
{
   boolean hasPin = hasParentPin();
   
   TextView textViewTitle = activity.findViewById(R.id.dialogTitle);
   if(textViewTitle != null) {
      textViewTitle.setText(hasPin ? R.string.edit_name : R.string.app_name);
   }
   
   Button buttonNegative = activity.findViewById(R.id.buttonCancel);
   if(buttonNegative != null) {
      buttonNegative.setText(
       hasPin ? R.string.dialog_action_delete : R.string.dialog_action_cancel);
   }
   
   if(hasPin) {
      
      Spinner spinnerVisibility = activity.findViewById(R.id.spinnerVisibility);
      if(spinnerVisibility != null) {
         spinnerVisibility.setSelection(parentPin.getVisibilityIndex(), true);
      }
      
      Spinner spinnerPriority = activity.findViewById(R.id.spinnerPriority);
      if(spinnerPriority != null) {
         spinnerPriority.setSelection(parentPin.getPriorityIndex(), true);
      }
      
      EditText editTextTitle = activity.findViewById(R.id.editTextTitle);
      if(editTextTitle != null) {
         editTextTitle.setText(parentPin.getTitle());
      }
      
      EditText editTextContent = activity.findViewById(R.id.editTextContent);
      if(editTextContent != null) {
         editTextContent.setText(parentPin.getContent());
      }
      
      CheckBox checkBoxPersistent = activity.findViewById(R.id.checkBoxPersistentPin);
      if(checkBoxPersistent != null) {
         checkBoxPersistent.setChecked(parentPin.isPersistent());
      }
   }
}
}
