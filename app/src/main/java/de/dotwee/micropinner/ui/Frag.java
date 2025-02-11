package de.dotwee.micropinner.ui;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import de.dotwee.micropinner.database.PinDatabase;

public abstract class Frag
 extends Fragment
{
public abstract void onPrepareMenu(Menu menu);
public abstract void onPrepareActionBar(ActionBar bar);
public abstract boolean mayCloseFragment(boolean cancel);
public abstract void deleteFromDatabase(PinDatabase db);
}
