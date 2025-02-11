package de.dotwee.micropinner.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.dotwee.micropinner.MainActivity;
import de.dotwee.micropinner.NotificationTools;
import de.dotwee.micropinner.R;
import de.dotwee.micropinner.database.Pin;
import de.dotwee.micropinner.database.PinDatabase;

public class FragList
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

private final ListAdapter listAdapter = new ListAdapter();
private HashSet<Pin> selected;
ArrayAdapter<String> priorityLocalizedStrings;

@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
 @Nullable Bundle savedInstanceState)
{
   View root = inflater.inflate(R.layout.frag_list, container, false);
   
   priorityLocalizedStrings = MainActivity.getPriorityLocalizedStrings(requireContext());
   RecyclerList lstList = root.findViewById(R.id.lstList);
   lstList.setEmptyView(root.findViewById(R.id.lblEmptyList));
   lstList.setAdapter(listAdapter);
   updateList();
   
   return root;
}

public enum Mode
{
   NORMAL, ORDER, DELETE
}

private Mode mode = Mode.NORMAL;
private MenuItem btnDelete;
private MenuItem btnDeleteMode;
private MenuItem btnOrderMode;
private MenuItem btnNew;

public void setMode(Mode mode)
{
   if(mode != this.mode) {
      this.mode = mode;
      MainActivity activity = (MainActivity) requireActivity();
      activity.updateActionBar(this);
      if(mode != Mode.DELETE)
         selected = null;
      listAdapter.notifyItemRangeChanged(0, listAdapter.getItemCount());
   }
}

@Override
public boolean mayCloseFragment(boolean cancel)
{
   // If not NORMAL mode, switch back to NORMAL mode.
   if(mode != Mode.NORMAL) {
      setMode(Mode.NORMAL);
      return false;
   }
   
   // Fragment may close when in NORMAL mode
   return true;
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
      // show done as up (no cancel button needed)
      bar.setHomeActionContentDescription(R.string.action_cancel);
      bar.setHomeAsUpIndicator(R.drawable.ic_done);
      break;
   case DELETE:
      // show cancel as up (btnDelete1 is used for deleting)
      bar.setHomeActionContentDescription(R.string.action_cancel);
      bar.setHomeAsUpIndicator(R.drawable.ic_cancel);
      break;
   }
   bar.setDisplayHomeAsUpEnabled(mode != Mode.NORMAL);
   bar.setDisplayUseLogoEnabled(mode == Mode.NORMAL);
   // no title in special modes
   bar.setDisplayShowTitleEnabled(mode == Mode.NORMAL);
}

@Override
public void onPrepareMenu(Menu menu)
{
   boolean normal = mode == Mode.NORMAL;
   
   menu.findItem(R.id.btnCancel).setVisible(false);
   
   btnDelete = menu.findItem(R.id.btnDelete);
   btnDelete.setVisible(mode == Mode.DELETE);
   
   btnDeleteMode = menu.findItem(R.id.btnDeleteMode);
   btnDeleteMode.setVisible(normal);
   
   btnOrderMode = menu.findItem(R.id.btnOrderMode);
   btnOrderMode.setVisible(normal);
   
   btnNew = menu.findItem(R.id.btnNew);
   btnNew.setVisible(normal);
   
   updateMenuButtons();
}

private void updateMenuButtons()
{
   if(btnNew == null) {
      Log.d(DBG, "updateMenuButtons() - called before onPrepareMenu");
      return;
   }
   if(getContext() == null) {
      Log.d(DBG, "updateMenuButtons() - no context");
      return;
   }
   Log.d(DBG, "updateMenuButtons() - OK - mode is: " + mode);
   
   boolean normal = mode == Mode.NORMAL;
   
   // btnDelete1 enabled if there are selected pins
   btnDelete.setEnabled(selected != null && !selected.isEmpty());
   
   // btnDeleteMode enabled if there are pins to delete
   btnDeleteMode.setEnabled(normal && listAdapter.getItemCount() > 0);
   
   // btnOrderMode enabled if there are pins that can be rearranged
   btnOrderMode.setEnabled(normal && listAdapter.canOrder);
   
   // btnNew enabled unless there are too many pins
   int count = PinDatabase.getInstance(requireContext()).getCount();
//   if(count >= PinDatabase.MAX_NOTIFICATIONS) {
//      Toast.makeText(requireContext(), R.string.message_too_many, Toast.LENGTH_SHORT).show();
//   }
   btnNew.setEnabled(normal && count < PinDatabase.MAX_NOTIFICATIONS);
}

private void updateList()
{
   Context ctx = getContext();
   if(ctx == null) {
      Log.d(DBG, "updateList() - no context");
      return;
   }
   List<Pin> allPins = PinDatabase.getInstance(ctx).getAllPins();
   listAdapter.update(allPins);
   updateMenuButtons();
}

public void onClick(int position)
{
   switch(mode) {
   // edit pin on click in NORMAL mode
   case NORMAL:
      MainActivity activity = (MainActivity) requireActivity();
      activity.showEditPin(listAdapter.pins.get(position));
      break;
   
   // select (or deselect) pin on click in DELETE mode
   case DELETE:
      Pin pin = listAdapter.pins.get(position);
      if(selected == null) {
         selected = new HashSet<>(2);
         selected.add(pin);
      }
      else if(selected.contains(pin))
         selected.remove(pin);
      else
         selected.add(pin);
      listAdapter.notifyItemChanged(position);
      updateMenuButtons();
      break;
   
   // do nothing on click in ORDER mode
   }
}

static class Holder
 extends RecyclerView.ViewHolder
{
   final CheckBox chkItemSelected;
   final TextView lblItemTitle;
   final TextView lblItemOrder;
   final ImageButton ibtnItemMoveUp;
   final ImageButton ibtnItemMoveDown;
   final LinearLayout llvLabels;
   
   public Holder(@NonNull View itemView)
   {
      super(itemView);
      chkItemSelected = itemView.findViewById(R.id.chkItemSelected);
      llvLabels = itemView.findViewById(R.id.llvLabels);
      lblItemTitle = itemView.findViewById(R.id.lblItemTitle);
      lblItemOrder = itemView.findViewById(R.id.lblItemOrder);
      ibtnItemMoveUp = itemView.findViewById(R.id.ibtnItemMoveUp);
      ibtnItemMoveDown = itemView.findViewById(R.id.ibtnItemMoveDown);
   }
} // Holder

class ListAdapter
 extends RecyclerView.Adapter<Holder>
{
   final ArrayList<Pin> pins = new ArrayList<>();
   Integer[] maxOrder;
   boolean canOrder;
   
   ListAdapter()
   {
   }
   
   @SuppressLint("NotifyDataSetChanged")
   void update(List<Pin> allPins)
   {
      maxOrder = new Integer[allPins.size()];
      canOrder = false;
      pins.clear();
      pins.addAll(allPins);
      int i = 0;
      int prevPrio = -1;
      for(Pin pin : allPins) {
         if(pin.getPriorityIndex() == prevPrio) {
            // there are two pins with the same priority.
            canOrder = true;
            int newMax = pin.getOrder();
            for(int back = 0; back <= newMax; back++) {
               maxOrder[i - back] = newMax;
            }
         }
         prevPrio = pin.getPriorityIndex();
         i++;
      }
      
      // show changes in RecyclerView
      notifyDataSetChanged();
   }
   
   @Override
   public void onBindViewHolder(@NonNull Holder holder, final int position)
   {
      final Pin pin = pins.get(position);
      Integer max = maxOrder[position];
      
      boolean select = false;
      boolean up = false;
      boolean down = false;
      switch(mode) {
      case ORDER:
         up = max != null && pin.getOrder() > 0;
         down = max != null && pin.getOrder() < max;
         if(up)
            holder.ibtnItemMoveUp.setOnClickListener(v -> {
               // change order in list
               Pin pin2 = pins.set(position - 1, pin);
               pins.set(position, pin2);
               int order = pin.getOrder();
               pin.setOrder(pin2.getOrder());
               pin2.setOrder(order);
               
               // update database
               PinDatabase.getInstance(requireContext()).changeOrderForPins(
                pin.getID(), pin.getOrder(),
                pin2.getID(), order
               );
               
               // show changes in RecyclerView
               notifyItemRangeChanged(position - 1, 2);
               
               // update order of system notifications
               NotificationTools.notify(requireContext(), pin);
               NotificationTools.notify(requireContext(), pin2);
            });
         if(down)
            holder.ibtnItemMoveDown.setOnClickListener(v -> {
               // change order in list
               Pin pin2 = pins.set(position + 1, pin);
               pins.set(position, pin2);
               int order = pin.getOrder();
               pin.setOrder(pin2.getOrder());
               pin2.setOrder(order);
               
               // update database
               PinDatabase.getInstance(requireContext()).changeOrderForPins(
                pin.getID(), pin.getOrder(),
                pin2.getID(), order
               );
               
               // show changes in RecyclerView
               notifyItemRangeChanged(position, 2);
               
               // update order of system notifications
               NotificationTools.notify(requireContext(), pin2);
               NotificationTools.notify(requireContext(), pin);
            });
         break;
      case DELETE:
         select = selected != null && selected.contains(pin);
         break;
      }
      
      holder.lblItemTitle.setText(pin.getTitle());
      holder.lblItemOrder.setText(getString(
       R.string.lblItemOrder,
       pin.getPrioOrderDisplayString(priorityLocalizedStrings, max)
      ));
      holder.ibtnItemMoveUp.setVisibility(up ? View.VISIBLE : View.INVISIBLE);
      holder.ibtnItemMoveDown.setVisibility(down ? View.VISIBLE : View.INVISIBLE);
      holder.chkItemSelected.setVisibility(mode == Mode.DELETE ? View.VISIBLE : View.GONE);
      holder.chkItemSelected.setChecked(select);
      holder.itemView.setActivated(select);
      View.OnClickListener clickListener = v -> onClick(position);
      holder.llvLabels.setOnClickListener(clickListener);
      holder.lblItemTitle.setOnClickListener(clickListener);
      holder.lblItemOrder.setOnClickListener(clickListener);
      holder.chkItemSelected.setOnClickListener(clickListener);
   }
   
   @NonNull
   @Override
   public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
   {
      View itemView = getLayoutInflater().inflate(
       R.layout.item_pin_listitem, parent, false);
      return new Holder(itemView);
   }
   
   /**
    * Returns the total number of items in the data set held by the adapter.
    * @return The total number of items in this adapter.
    */
   @Override
   public int getItemCount()
   {
      return pins.size();
   }
} // ListAdapter

/**
 * Delete selected pins from database, if any. Also go back to NORMAL mode.
 * @param db
 *  the PinDatabase
 */
public void deleteFromDatabase(PinDatabase db)
{
   if(selected != null && !selected.isEmpty()) {
      for(Pin pin : selected) {
         db.deletePin(pin.getID());
      }
   }
   setMode(Mode.NORMAL);
   updateList();
}
}
