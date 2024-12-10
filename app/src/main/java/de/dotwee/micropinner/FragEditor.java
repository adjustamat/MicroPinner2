package de.dotwee.micropinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import de.dotwee.micropinner.database.PinSpec;

class FragEditor
 extends Frag
{
public final static String EXTRA_PIN_SPEC = "IAMAPIN";
private static final String DBG = "FragEditor";

// TODO: is this field needed?
private ArrayAdapter<String> priorityLocalizedStrings;

// TODO: does this object have to be saved to instancestate somehow?
private PinSpec editing = null;

private Spinner spinPriority;
private EditText txtTitleAndContent;
private CheckBox chkShowActions;

public static FragEditor getNewEditingInstance(PinSpec editing)
{
   FragEditor ret = new FragEditor();
   ret.editing = editing;
   return ret;
}

public static FragEditor getNewCreatingInstance()
{
   return new FragEditor();
}

private FragEditor()
{
}

@Override
public boolean onUp()
{
   String titleAndContent = txtTitleAndContent.getText().toString();
   
   if(titleAndContent.isEmpty()) {
      Toast.makeText(getContext(), R.string.message_empty_title, Toast.LENGTH_SHORT).show();
      Log.d(DBG, "user entered no title, can't finish pin.");
      return false;
   }
   String title;
   String content;
   int split = titleAndContent.indexOf('\n');
   if(split < 0) {
      title = titleAndContent;
      content = "";
   }
   else {
      title = titleAndContent.substring(0, split);
      content = titleAndContent.substring(split + 1);
   }
//   if(getParentPin() != null) {
//
//   }
//   else {
//
//   }
   
   
   PinSpec newPin = pinDatabase.writePin(editing,
    title, content,
    spinPriority.getSelectedItemPosition(), chkShowActions.isChecked());
   
   NotificationTools.notify(activity, newPin);
   
   activity.finish();
//   catch(Exception e) {
//      Log.d(DBG, "onButtonPositive()", e);
//   }
   return false;
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
   // TODO: see my plan. (order)
   MenuItem item;
   item = menu.findItem(R.id.btnCancel);
   item.setVisible(editing == null);
   item.setEnabled(editing == null);
   item = menu.findItem(R.id.btnDelete1);
   item.setVisible(editing != null);
   item.setEnabled(editing != null);
   item = menu.findItem(R.id.btnDeleteMode);
   item.setVisible(false);
   item.setEnabled(false);
   item = menu.findItem(R.id.btnOrderMode);
   item.setVisible(false);
   item.setEnabled(false);
   item = menu.findItem(R.id.btnNew);
   item.setVisible(false);
   item.setEnabled(false);
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
   outState.putSerializable(EXTRA_PIN_SPEC, editing);
}

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
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
 @Nullable Bundle savedInstanceState)
{
   if(savedInstanceState != null && editing == null) {
      editing = (PinSpec) savedInstanceState.getSerializable(EXTRA_PIN_SPEC);
   }
   // TODO: store chk and txt and spinner in fields!
   // TODO: make sure onShowActions() is called.
   // TODO: init() ?_
   return inflater.inflate(R.layout.frag_editor, container, false);
}

void init()
{
   boolean hasPin = getParentPin() != null;
   EditText editText = findViewById(R.id.txtTitleAndContent);
   
   Spinner spinPriority = findViewById(R.id.spinPriority);
   ArrayAdapter<String> adapter = getPriorityAdapter();
   if(spinPriority != null) {
      spinPriority.setAdapter(adapter);
   }
   
   
   if(hasPin) {
      spinPriority.setSelection(editing.getPriorityIndex(), true);
      
      if(editText != null) {
         if(editing.getContent().isEmpty()) {
            editText.setText(editing.getTitle());
         }
         else {
            editText.setText(editing.getTitle() + "\n" + editing.getContent());
         }
      }
   } // if(hasPin)
   
}

/**
 * This method handles the click on the show-actions checkbox.
 */
public void onShowActions()
{
   preferencesHandler.setNotificationActionsEnabled(chkShowActions.isChecked());
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
