package de.dotwee.micropinner;

import java.io.Serializable;
import androidx.fragment.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.tools.NotificationTools;

class FragEditor extends Fragment
{

private ArrayAdapter<String> priorityLocalizedStrings;
private PinSpec intentPin;

private FragEditor(){}

public static FragEditor getNewEditInstance(PinSpec editing){
FragEditor ret=new FragEditor();

return ret;
}

public static FragEditor getNewPinInstance(){
   FragEditor ret = new FragEditor();
   
   return ret;
}

/**
 * This method checks if a parent pin exists.
 */
public PinSpec getParentPin()
{
   if(intentPin != null)
      return intentPin;
   if(intent != null) {
      Serializable extra = intent.getSerializableExtra(NotificationTools.EXTRA_PIN_SPEC);
      if(extra instanceof PinSpec) {
         return this.intentPin = (PinSpec) extra;
      }
   }
   return this.intentPin = null;
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
   EditText editText = findViewById(R.id.txtTitleAndContent);
   if(editText != null) {
      return editText.getText().toString();
   }
   
   return null;
}

/**
 * This method reads the value of the priority spinner widget.
 * @return Value of the content priority spinner widget.
 */
public Integer getPriority()
{
   Spinner spinner = findViewById(R.id.spinPriority);
   if(spinner != null) {
      return spinner.getSelectedItemPosition();
   }
   
   return null;
}

/**
 * This method handles the click on the show-actions checkbox.
 */
public void onShowActions()
{
   CheckBox checkBox = activity.findViewById(R.id.chkShowActions);
   preferencesHandler.setNotificationActionsEnabled(checkBox.isChecked());
}

/**
 * This method reads the state of the show-actions checkbox widget.
 * @return State of the show-actions checkbox.
 */
public boolean getShowActions()
{
   CheckBox checkBox = findViewById(R.id.chkShowActions);
   return checkBox != null && checkBox.isChecked();
}

void init(){
   boolean hasPin = getParentPin() != null;
   EditText editText = findViewById(R.id.txtTitleAndContent);
   
   Spinner spinPriority = findViewById(R.id.spinPriority);
   ArrayAdapter<String> adapter = getPriorityAdapter();
   if(spinPriority != null) {
      spinPriority.setAdapter(adapter);
   }
   
   
   if(hasPin) {
      spinPriority.setSelection(intentPin.getPriorityIndex(), true);
      
      if(editText != null) {
         if(intentPin.getContent().isEmpty()) {
            editText.setText(intentPin.getTitle());
         }
         else {
            editText.setText(intentPin.getTitle() + "\n" + intentPin.getContent());
         }
      }
   } // if(hasPin)
   
}

public ArrayAdapter<String> getPriorityAdapter()
{
   if(priorityLocalizedStrings == null) {
      priorityLocalizedStrings = MainActivity.getPriorityLocalizedStrings(getContext());
      priorityLocalizedStrings.setDropDownViewResource(
       android.R.layout.simple_spinner_dropdown_item);
   }
   return priorityLocalizedStrings;
}

}
