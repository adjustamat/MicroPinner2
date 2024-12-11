package de.dotwee.micropinner;

import java.util.HashSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;

class FragList
 extends Frag
{
private static final String DBG = "FragList";

public static FragList getNewInstance()
{
   return new FragList();
}

private FragList()
{
}

private ArrayAdapter<PinSpec> pins;
private HashSet<PinSpec> selected;
private MenuItem btnDelete1;
private MenuItem btnDeleteMode;
private MenuItem btnOrderMode;
private MenuItem btnNew;
ActionMode actionMode;

enum Mode
{
   NORMAL, ORDER, DELETE
}

private Mode mode = Mode.NORMAL;

@Override
public boolean onUpMayFinish(boolean cancel)
{
   if(mode != Mode.NORMAL) {
      setMode(Mode.NORMAL);
      return false;
   }
   return true;
}

void setMode(Mode mode)
{
   if(mode != this.mode) {
      this.mode = mode;
      MainActivity activity = (MainActivity) requireActivity();
      activity.invalidateActionBar(this);
   }
}

@Override
public void onPrepareActionBar(ActionBar bar)
{
   switch(mode) {
   case NORMAL:
      // show logo as up, show title
      bar.setHomeActionContentDescription(R.string.app_name);
      bar.setTitle(R.string.app_name);
      break;
   case ORDER:
      // show done as up
      bar.setHomeActionContentDescription(android.R.string.ok);
      bar.setHomeAsUpIndicator(R.drawable.ic_done);
      break;
   case DELETE:
      // show cancel as up
      bar.setHomeActionContentDescription(R.string.action_cancel);
      bar.setHomeAsUpIndicator(R.drawable.ic_cancel);
      break;
   }
   bar.setDisplayHomeAsUpEnabled(mode != Mode.NORMAL);
   bar.setDisplayUseLogoEnabled(mode == Mode.NORMAL);
   bar.setDisplayShowTitleEnabled(mode == Mode.NORMAL);
}

@Override
public void onPrepareMenu(Menu menu)
{
   boolean normal = mode == Mode.NORMAL;
   // TODO: see my plan. (order)
   MenuItem item;
   item = menu.findItem(R.id.btnCancel);
   item.setVisible(false);
   item.setEnabled(false);
   
   item = menu.findItem(R.id.btnDelete1);
   item.setVisible(mode == Mode.DELETE);
   
   // TODO: enabled if there are selected pins!
   item.setEnabled(mode == Mode.DELETE);
   
   item = menu.findItem(R.id.btnDeleteMode);
   item.setVisible(normal);
   
   // TODO: enabled if there are pins!
   item.setEnabled(normal);
   
   item = menu.findItem(R.id.btnOrderMode);
   item.setVisible(normal);
   
   // TODO: enabled if there are pins that can be rearranged!
   item.setEnabled(normal);
   
   // hide btnNew if MAX_NOTIFICATIONS
   int count = PinDatabase.getInstance(getContext()).getCount();
   if(count >= PinDatabase.MAX_NOTIFICATIONS){
      Toast.makeText(requireContext(), R.string.message_too_many, Toast.LENGTH_SHORT).show();
   }
   normal = normal && count >= PinDatabase.MAX_NOTIFICATIONS;
   item = menu.findItem(R.id.btnNew);
   item.setVisible(normal);
   item.setEnabled(normal);
   
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
   // TODO: setup clicking on recyclerview, and selecting for DELETE mode
   return inflater.inflate(R.layout.frag_list, container, false);
}

public void onClick(PinSpec pin)
{/*
TODO selection:
build your KeyProvider (selection key type: Long)

Implement ItemDetailsLookup (This will likely depend on RecyclerView.ViewHolder)

In Adapter#onBindViewHolder, set the "activated" status on view.
 Note that the status should be "activated" not "selected". See View.html#setActivated for details.
Update the styling of the view to represent the activated status with a color state list.

Use ActionMode when there is a selection
Register a androidx.recyclerview.selection.SelectionTracker.SelectionObserver to be notified
when selection changes. When a selection is first created, start ActionMode to represent this
to the user, and provide selection specific actions.

Assemble everything with SelectionTracker.Builder

In order to preserve state, See SelectionTracker#onSaveInstanceState
and SelectionTracker#onRestoreInstanceState
*/
   switch(mode) {
   case NORMAL:
      MainActivity activity = (MainActivity) requireActivity();
      activity.showEditPin(pin);
      break;
   case DELETE:
      if(pin.selected == null)
         pin.selected = true;
      else
         pin.selected = null;
      
      break;
   // case ORDER: // do nothing.
   }
   
}

public void deleteSelected()
{
   // TODO: loop through adapter. reset pin.selected to null, and send those to PinDatabase.
   
   // PinDatabase.getInstance(context).deletePin(pin.getId());
}
}
