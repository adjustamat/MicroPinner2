package de.dotwee.micropinner.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import de.dotwee.micropinner.R;

/**
 * Created by lukas on 25.07.2016.
 */
public class DialogContentView
 extends AbstractDialogView
 implements CheckBox.OnCheckedChangeListener
{

private static ArrayAdapter<String> visibilityLocaleStrs;
private static ArrayAdapter<String> priorityLocaleStrs;

public DialogContentView(Context context)
{
   super(context);
}

public DialogContentView(Context context, AttributeSet attrs)
{
   super(context, attrs);
}

public DialogContentView(Context context, AttributeSet attrs, int defStyleAttr)
{
   super(context, attrs, defStyleAttr);
}

@Override
public void init()
{
   super.init();
   
   inflate(getContext(), R.layout.dialog_main_content, this);
   
   Spinner spinnerVisibility = findViewById(R.id.spinnerVisibility);
   getVisibilityAdapter(getContext(), spinnerVisibility);
   
   Spinner spinnerPriority = findViewById(R.id.spinnerPriority);
   getPriorityAdapter(getContext(), spinnerPriority);
}

public static ArrayAdapter<String> getVisibilityAdapter(Context ctx, Spinner spinnerVisibility)
{
   if(visibilityLocaleStrs == null) {
      visibilityLocaleStrs =
       new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item,
        ctx.getResources().getStringArray(R.array.array_visibilities));
      visibilityLocaleStrs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
   }
   if(spinnerVisibility != null) {
      spinnerVisibility.setAdapter(visibilityLocaleStrs);
   }
   return visibilityLocaleStrs;
}

public static ArrayAdapter<String> getPriorityAdapter(Context ctx, Spinner spinnerPriority)
{
   if(priorityLocaleStrs == null) {
      priorityLocaleStrs =
       new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item,
        ctx.getResources().getStringArray(R.array.array_priorities));
      priorityLocaleStrs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
   }
   if(spinnerPriority != null) {
      spinnerPriority.setAdapter(priorityLocaleStrs);
   }
   return priorityLocaleStrs;
}

@Override
public void onCheckedChanged(CompoundButton compoundButton, boolean b)
{
   checkIfPresenterNull();
   
   if(compoundButton.getId() == R.id.checkBoxShowActions) {
      mainPresenter.onShowActions();
   }
}
}
