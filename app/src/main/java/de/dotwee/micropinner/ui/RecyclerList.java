package de.dotwee.micropinner.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Simple RecyclerView subclass that supports providing an empty view (which
 * is displayed when the adapter has no data and hidden otherwise).
 */
class RecyclerList
 extends RecyclerView
{
private View mEmptyView;
private final AdapterDataObserver mDataObserver = new AdapterDataObserver()
{
   @Override
   public void onChanged()
   {
      super.onChanged();
      updateEmptyView();
   }
};

public RecyclerList(@NonNull Context context)
{
   super(context);
}

public RecyclerList(@NonNull Context context,
 @Nullable AttributeSet attrs)
{
   super(context, attrs);
}

public RecyclerList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
{
   super(context, attrs, defStyleAttr);
}

/**
 * Designate a view as the empty view. When the backing adapter has no
 * data this view will be made visible and the recycler view hidden.
 */
public void setEmptyView(View emptyView)
{
   mEmptyView = emptyView;
   updateEmptyView();
}

@Override
public void setAdapter(RecyclerView.Adapter adapter)
{
   if(getAdapter() != null) {
      getAdapter().unregisterAdapterDataObserver(mDataObserver);
   }
   if(adapter != null) {
      adapter.registerAdapterDataObserver(mDataObserver);
   }
   super.setAdapter(adapter);
   updateEmptyView();
}

private void updateEmptyView()
{
   if(mEmptyView != null && getAdapter() != null) {
      boolean showEmptyView = getAdapter().getItemCount() == 0;
      mEmptyView.setVisibility(showEmptyView ? VISIBLE : GONE);
      setVisibility(showEmptyView ? GONE : VISIBLE);
   }
}
}
