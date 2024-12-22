package de.dotwee.micropinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;
import de.dotwee.micropinner.tools.PreferencesHandler;

public class FragEditor
 extends Frag
{
public final static String EXTRA_PIN_SPEC = "IAMAPIN";
private static final String DBG = "FragEditor";

public static FragEditor getNewCreatingInstance()
{
   return new FragEditor();
}

public static FragEditor getNewEditingInstance(PinSpec editing)
{
   FragEditor ret = new FragEditor();
   ret.editing = editing;
   return ret;
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

private FragEditor()
{
}

private PinSpec editing = null;
//private int order;
private String channelID;

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
      editing = (PinSpec) savedInstanceState.getSerializable(EXTRA_PIN_SPEC);
   }
   
   View root = inflater.inflate(R.layout.frag_editor, container, false);
   
   // init fields
   chkShowActions = root.findViewById(R.id.chkShowActions);
   spinPriority = root.findViewById(R.id.spinPriority);
   txtTitleAndContent = root.findViewById(R.id.txtTitleAndContent);
   
   // on change, store default value for checkbox
   chkShowActions.setOnCheckedChangeListener(
    (buttonView, isChecked) -> PreferencesHandler.getInstance(requireContext())
                                .setNotificationActionsEnabled(isChecked)
   );
   
   // set choices for spinPriority
   ArrayAdapter<String> adapter = MainActivity.getPriorityLocalizedStrings(getContext());
   adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
   spinPriority.setAdapter(adapter);
   
   if(editing != null) {
      // edit priority
      spinPriority.setSelection(editing.getPriorityIndex(), false);
      
      // edit title and content
      if(editing.getContent().isEmpty()) {
         txtTitleAndContent.setText(editing.getTitle());
      }
      else {
         txtTitleAndContent.setText(editing.getTitle() + "\n" + editing.getContent());
      }
   } // editing != null
   else { // editing == null
      
      // load default value for checkbox
      chkShowActions.setChecked(
       PreferencesHandler.getInstance(requireContext()).isNotificationActionsEnabled()
      );
      
      // default priority for new pin
      int priorityIndex = PinSpec.priorityToIndex(Notification.PRIORITY_DEFAULT);
      spinPriority.setSelection(priorityIndex, false);
      
      // create channel ID for new pin
      int newOrderForPriority =
       PinDatabase.getInstance(requireContext()).getNewOrderForPriority(priorityIndex);
      channelID = PinSpec.getChannelID(priorityIndex, newOrderForPriority);
      spinPriority.setOnItemSelectedListener(new OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
         {
            int newOrderForPriority =
             PinDatabase.getInstance(requireContext()).getNewOrderForPriority(position);
            channelID = PinSpec.getChannelID(position, newOrderForPriority);
         }
         
         @Override
         public void onNothingSelected(AdapterView<?> parent)
         {
         }
      });
   } // editing == null
   
   // button takes user to channel settings
   Button btnMoreSettings = root.findViewById(R.id.btnMoreSettings);
   btnMoreSettings.setOnClickListener(v -> {
      if(editing == null)
         NotificationTools.openSettings(requireContext(), channelID);
      else
         NotificationTools.openSettings(requireContext(), editing.getNotificationChannelID());
   });
   
   return root;
}

@Override
public boolean onUpMayFinish(boolean cancel)
{
   if(cancel)
      return true;
   
   // get title and optional content from EditText
   String titleAndContent = txtTitleAndContent.getText().toString().trim();
   if(titleAndContent.isEmpty()) {
      Toast.makeText(requireContext(), R.string.message_empty_title, Toast.LENGTH_SHORT).show();
      Log.d(DBG, "user entered no title, can't finish pin.");
      return false;
   }
   String title;
   String content;
   int split = titleAndContent.indexOf('\n');
   if(split == -1) {
      // only one row
      title = titleAndContent;
      content = "";
   }
   else if(split == titleAndContent.length() - 1) {
      // only one row, ends with newline
      title = titleAndContent.substring(0, split);
      content = "";
   }
   else {
      // more than one row
      title = titleAndContent.substring(0, split);
      content = titleAndContent.substring(split + 1);
   }
   if(title.isEmpty()) {
      Toast.makeText(requireContext(), R.string.message_empty_title, Toast.LENGTH_SHORT).show();
      Log.d(DBG, "user entered no title, can't finish pin.");
      return false;
   }
   
   // update database
   PinDatabase pinDatabase = PinDatabase.getInstance(requireContext());
   PinSpec written = pinDatabase.writePin(editing, title, content,
    spinPriority.getSelectedItemPosition(), chkShowActions.isChecked());
   // check for database being full
   if(written == null) {
      Toast.makeText(requireContext(), R.string.message_too_many, Toast.LENGTH_LONG).show();
      return false;
   }
   
   // show pin
   NotificationTools.notify(requireContext(), written);
   return true;
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

public void deletePin()
{
   PinDatabase.getInstance(requireContext()).deletePin(editing.getId());
}
}
