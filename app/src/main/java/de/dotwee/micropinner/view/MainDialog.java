package de.dotwee.micropinner.view;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import de.dotwee.micropinner.R;
import de.dotwee.micropinner.database.PinSpec.Data;
import de.dotwee.micropinner.presenter.MainPresenter;
import de.dotwee.micropinner.receiver.OnBootReceiver;
import de.dotwee.micropinner.view.custom.DialogContentView;
import de.dotwee.micropinner.view.custom.DialogFooterView;
import de.dotwee.micropinner.view.custom.DialogHeaderView;

/**
 * Created by Lukas Wolfsteiner on 29.10.2015.
 */
public class MainDialog
 extends AppCompatActivity
 implements Data
{

static {
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
   
   // simulate device-boot by sending a new intent to class OnBootReceiver
   sendBroadcast(new Intent(this, OnBootReceiver.class));
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
@Override
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
@Override
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
@Override
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
@Override
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
@Override
public boolean isPersistent()
{
   CheckBox checkBox = findViewById(R.id.checkBoxPersistentPin);
   return checkBox != null && checkBox.isChecked();
}

/**
 * This method reads the state of the show-actions checkbox widget.
 * @return State of the show-actions checkbox.
 */
@Override
public boolean showActions()
{
   CheckBox checkBox = findViewById(R.id.checkBoxShowActions);
   return checkBox != null && checkBox.isChecked();
}
}


