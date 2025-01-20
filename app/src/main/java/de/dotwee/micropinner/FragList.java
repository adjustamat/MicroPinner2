package de.dotwee.micropinner;

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
import android.widget.TextView;
import de.dotwee.micropinner.database.PinDatabase;
import de.dotwee.micropinner.database.PinSpec;

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
private boolean canOrder;
private HashSet<PinSpec> selected;
ArrayAdapter<String> priorityLocalizedStrings;

@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
 @Nullable Bundle savedInstanceState)
{
   View root = inflater.inflate(R.layout.frag_list, container, false);
   
   priorityLocalizedStrings = MainActivity.getPriorityLocalizedStrings(requireContext());
   
   RecyclerView lstList = (RecyclerView) root;
   lstList.setAdapter(listAdapter);
   updateList();
   
   return root;
}

static class Holder
 extends RecyclerView.ViewHolder
{
   final CheckBox chkItemSelected;
   final TextView lblItemTitle;
   final TextView lblItemChannelName;
   final ImageButton ibtnItemMoveUp;
   final ImageButton ibtnItemMoveDown;
   
   public Holder(@NonNull View itemView)
   {
      super(itemView);
      chkItemSelected = itemView.findViewById(R.id.chkItemSelected);
      lblItemTitle = itemView.findViewById(R.id.lblItemTitle);
      lblItemChannelName = itemView.findViewById(R.id.lblItemChannelName);
      ibtnItemMoveUp = itemView.findViewById(R.id.ibtnItemMoveUp);
      ibtnItemMoveDown = itemView.findViewById(R.id.ibtnItemMoveDown);
   }
}

class ListAdapter
 extends RecyclerView.Adapter<Holder>
{
   final ArrayList<PinSpec> pins = new ArrayList<>();
   Integer[] maxOrder;
   
   ListAdapter()
   {
   }
   
   @SuppressLint("NotifyDataSetChanged")
   void update(List<PinSpec> allPins)
   {
      maxOrder = new Integer[allPins.size()];
      canOrder = false;
      pins.clear();
      pins.addAll(allPins);
      int i = 0;
      int prevPrio = -1;
      for(PinSpec pin : allPins) {
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
      final PinSpec pin = pins.get(position);
      
      boolean select = false;
      boolean up = false;
      boolean down = false;
      switch(mode) {
      case ORDER:
         Integer max = maxOrder[position];
         up = max != null && pin.getOrder() > 0;
         down = max != null && pin.getOrder() < max;
         if(up)
            holder.ibtnItemMoveUp.setOnClickListener(v -> {
               // change order in list
               PinSpec pin2 = pins.set(position - 1, pin);
               pins.set(position, pin2);
               int order = pin.getOrder();
               pin.setOrder(pin2.getOrder());
               pin2.setOrder(order);
               
               // update database
               PinDatabase.getInstance(requireContext()).changeOrderForPins(
                pin.getId(), pin.getOrder(),
                pin2.getId(), order
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
               PinSpec pin2 = pins.set(position + 1, pin);
               pins.set(position, pin2);
               int order = pin.getOrder();
               pin.setOrder(pin2.getOrder());
               pin2.setOrder(order);
               
               // update database
               PinDatabase.getInstance(requireContext()).changeOrderForPins(
                pin.getId(), pin.getOrder(),
                pin2.getId(), order
               );
               
               // show changes in RecyclerView
               notifyItemRangeChanged(position, 2);
               
               // update order of system notifications
               NotificationTools.notify(requireContext(), pin);
               NotificationTools.notify(requireContext(), pin2);
            });
         break;
      case DELETE:
         select = selected.contains(pin);
         break;
      }
      
      holder.lblItemTitle.setText(pin.getTitle());
      holder.lblItemChannelName.setText(pin.getNotificationChannelName(priorityLocalizedStrings));
      holder.ibtnItemMoveUp.setVisibility(up ? View.VISIBLE : View.INVISIBLE);
      holder.ibtnItemMoveDown.setVisibility(down ? View.VISIBLE : View.INVISIBLE);
      holder.chkItemSelected.setVisibility(mode == Mode.DELETE ? View.VISIBLE : View.GONE);
      holder.chkItemSelected.setChecked(select);
      holder.itemView.setActivated(select);
      holder.itemView.setOnClickListener(v -> onClick(position));
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
}

enum Mode
{
   NORMAL, ORDER, DELETE
}

private Mode mode = Mode.NORMAL;

void setMode(Mode mode)
{
   if(mode != this.mode) {
      this.mode = mode;
      MainActivity activity = (MainActivity) requireActivity();
      activity.invalidateActionBar(this);
      if(mode != Mode.DELETE)
         selected = null;
      listAdapter.notifyItemRangeChanged(0, listAdapter.getItemCount());
   }
}

@Override
public boolean onUpMayFinish(boolean cancel)
{
   if(mode != Mode.NORMAL) {
      setMode(Mode.NORMAL);
      return false;
   }
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
      bar.setHomeActionContentDescription(android.R.string.ok);
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

private void updateButtons()
{
   if(btnNew == null) {
      Log.d(DBG, "updateButtons() - called before onPrepareMenu");
      return;
   }
   if(getContext() == null) {
      Log.d(DBG, "updateButtons() - no context");
      return;
   }
   boolean normal = mode == Mode.NORMAL;
   
   // btnDelete1 enabled if there are selected pins
   btnDelete1.setEnabled(selected != null && !selected.isEmpty());
   
   // btnDeleteMode enabled if there are pins to delete
   btnDeleteMode.setEnabled(normal && listAdapter.getItemCount() > 0);
   
   // btnOrderMode enabled if there are pins that can be rearranged
   btnOrderMode.setEnabled(normal && canOrder);
   
   // btnNew enabled unless there are too many pins
   int count = PinDatabase.getInstance(requireContext()).getCount();
//   if(count >= PinDatabase.MAX_NOTIFICATIONS) {
//      Toast.makeText(requireContext(), R.string.message_too_many, Toast.LENGTH_SHORT).show();
//   }
   btnNew.setEnabled(normal && count < PinDatabase.MAX_NOTIFICATIONS);
}

@Override
public void onPrepareMenu(Menu menu)
{
   boolean normal = mode == Mode.NORMAL;
   MenuItem item;
   item = menu.findItem(R.id.btnCancel);
   item.setVisible(false);
   item.setEnabled(false);
   
   btnDelete1 = menu.findItem(R.id.btnDelete1);
   btnDelete1.setVisible(mode == Mode.DELETE);
   
   btnDeleteMode = menu.findItem(R.id.btnDeleteMode);
   btnDeleteMode.setVisible(normal);
   
   btnOrderMode = menu.findItem(R.id.btnOrderMode);
   btnOrderMode.setVisible(normal);
   
   btnNew = menu.findItem(R.id.btnNew);
   btnNew.setVisible(normal);
   
   updateButtons();
}

private MenuItem btnDelete1;
private MenuItem btnDeleteMode;
private MenuItem btnOrderMode;
private MenuItem btnNew;

private void updateList()
{
   Context ctx = getContext();
   if(ctx == null) {
      Log.d(DBG, "updateList() - no context");
      return;
   }
   List<PinSpec> allPins = PinDatabase.getInstance(ctx).getAllPins();
   listAdapter.update(allPins);
   updateButtons();
}

public void onClick(int position)
{
   switch(mode) {
   case NORMAL:
      MainActivity activity = (MainActivity) requireActivity();
      activity.showEditPin(listAdapter.pins.get(position));
      break;
   case DELETE:
      PinSpec pin = listAdapter.pins.get(position);
      if(selected == null) {
         selected = new HashSet<>(2);
         selected.add(pin);
      }
      else if(selected.contains(pin))
         selected.remove(pin);
      else
         selected.add(pin);
      listAdapter.notifyItemChanged(position);
      break;
   }
}

public void deleteSelectedPins()
{
   PinDatabase db = PinDatabase.getInstance(requireContext());
   for(PinSpec pin : selected) {
      db.deletePin(pin.getId());
   }
   setMode(Mode.NORMAL);
   updateList();
}
}
