package de.dotwee.micropinner.view;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import de.dotwee.micropinner.R;
import de.dotwee.micropinner.presenter.MainPresenter;
import de.dotwee.micropinner.tools.NotificationTools;
import de.dotwee.micropinner.view.custom.DialogContentView;
import de.dotwee.micropinner.view.custom.DialogFooterView;
import de.dotwee.micropinner.view.custom.DialogHeaderView;

/**
 * Created by Lukas Wolfsteiner on 29.10.2015.
 */
public class MainDialog
 extends AppCompatActivity
{
public static final String POST_NOTIFICATIONS;
public static boolean hasPermission = false;

static {
   if(VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      POST_NOTIFICATIONS = permission.POST_NOTIFICATIONS;
   }
   else {
      POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
   }
   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
   
   this.setContentView(R.layout.dialog_main);
   
   MainPresenter mainPresenter = new MainPresenter(this, getIntent());
   
   DialogHeaderView headerView = findViewById(R.id.dialogHeaderView);
   headerView.setMainPresenter(mainPresenter);
   
   DialogContentView contentView = findViewById(R.id.dialogContentView);
   contentView.setMainPresenter(mainPresenter);
   
   DialogFooterView footerView = findViewById(R.id.dialogFooterView);
   footerView.setMainPresenter(mainPresenter);
   
   // restore previous state
   mainPresenter.restore();
   
   if(VERSION.SDK_INT >= VERSION_CODES.M && // MARSHMALLOW == 23 // TIRAMISU == 33
       PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(
        this, POST_NOTIFICATIONS)) {
      if(shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {
         Toast.makeText(this, R.string.requires_your_permission, Toast.LENGTH_SHORT)
          .show();
      }
      requestPermissions(new String[] {POST_NOTIFICATIONS}, 1);
   }
   else {
      hasPermission = true;
      NotificationTools.restoreAllPins(this);
//   // simulate device-boot by sending a fake BOOT_COMPLETED intent to OnBootReceiver
//   sendBroadcast(new Intent("fake." + Intent.ACTION_BOOT_COMPLETED, null,
//    this, OnBootReceiver.class));
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

@Override
public void setContentView(@LayoutRes int layoutResID)
{
   if(isTablet(this)) {
      
      DisplayMetrics metrics = getResources().getDisplayMetrics();
      int newWidth = Math.round(320 * (metrics.densityDpi / 160f));
      
      setContentView(View.inflate(this, layoutResID, null),
       new FrameLayout.LayoutParams(newWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
   }
   else {
      super.setContentView(layoutResID);
   }
}

/**
 * This method reads the value of the visibility spinner widget.
 * @return Value of the content visibility spinner widget.
 */
public Integer getVisibility()
{
   Spinner spinner = findViewById(R.id.spinnerVisibility);
   if(spinner != null) {
      return spinner.getSelectedItemPosition();
   }
   
   return null;
}

/**
 * This method reads the value of the priority spinner widget.
 * @return Value of the content priority spinner widget.
 */
public Integer getPriority()
{
   Spinner spinner = findViewById(R.id.spinnerPriority);
   if(spinner != null) {
      return spinner.getSelectedItemPosition();
   }
   
   return null;
}

/**
 * This method reads the value of the title editText widget.
 * @return Value of the content title widget.
 */
public String getPinTitle()
{
   EditText editText = findViewById(R.id.editTextTitle);
   if(editText != null) {
      return editText.getText().toString();
   }
   
   return null;
}

/**
 * This method reads the value of the content editText widget.
 * @return Value of the content editText widget.
 */
public String getPinContent()
{
   EditText editText = findViewById(R.id.editTextContent);
   if(editText != null) {
      return editText.getText().toString();
   }
   
   return null;
}

/**
 * This method reads the state of the persistent checkbox widget.
 * @return State of the persistent checkbox.
 */
public boolean isPersistent()
{
   CheckBox checkBox = findViewById(R.id.checkBoxPersistentPin);
   return checkBox != null && checkBox.isChecked();
}

/**
 * This method reads the state of the show-actions checkbox widget.
 * @return State of the show-actions checkbox.
 */
public boolean showActions()
{
   CheckBox checkBox = findViewById(R.id.checkBoxShowActions);
   return checkBox != null && checkBox.isChecked();
}
}


