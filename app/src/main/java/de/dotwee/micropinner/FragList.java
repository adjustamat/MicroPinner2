package de.dotwee.micropinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

enum Mode
{
   NORMAL(
    true, true, false
   ),
   ORDER(
    false, false, false
   ),
   DELETE(
    false, false, true
   );
   private final boolean showOrderMode;
   private final boolean showDeleteMode;
   private final boolean showDelete1;
   
   Mode(boolean showOrderMode, boolean showDeleteMode, boolean showDelete1)
   {
      this.showOrderMode = showOrderMode;
      this.showDeleteMode = showDeleteMode;
      this.showDelete1 = showDelete1;
   }
}

private Mode mode = Mode.NORMAL;

@Override
public boolean onUp()
{
   if(mode != Mode.NORMAL) {
      setMode(Mode.NORMAL);
      return true;
   }
   return false;
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
   item.setVisible(mode == Mode.NORMAL);
   
   // TODO: enabled if there are pins!
   item.setEnabled(mode == Mode.NORMAL);
   
   item = menu.findItem(R.id.btnOrderMode);
   item.setVisible(mode == Mode.NORMAL);
   
   // TODO: enabled if there are pins that can be rearranged!
   item.setEnabled(mode == Mode.NORMAL);
   
   item = menu.findItem(R.id.btnNew);
   item.setVisible(mode == Mode.NORMAL);
   item.setEnabled(mode == Mode.NORMAL);
   
   
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
   return inflater.inflate(R.layout.frag_list, container, false);
}

public void onClick(PinSpec pin)
{
   MainActivity activity = (MainActivity) requireActivity();
   activity.showEditPin(pin);
}
}
