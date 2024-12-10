package de.dotwee.micropinner;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import android.view.Menu;

abstract class Frag
 extends Fragment
{
public abstract void onPrepareMenu(Menu menu);
public abstract void onPrepareActionBar(ActionBar bar);
public abstract boolean onUp();
}
