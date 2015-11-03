/*
 * Copyright (C) 2013 Manuel Peinado
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
package com.zhuoyou.plugin.mainFrame;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class ListViewScrollObserver implements OnScrollListener {
    private OnListViewScrollListener listener;


    public interface OnListViewScrollListener {
        void onScrollStateChange(int state);
        void onScrollListDispatch(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
        
    }

    public ListViewScrollObserver(ListView listView) {
        listView.setOnScrollListener(this);
    }

    public void setOnScrollUpAndDownListener(OnListViewScrollListener listener) {
        this.listener = listener;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (listener != null) {
            listener.onScrollListDispatch(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(listener != null)
        	listener.onScrollStateChange(scrollState);
    }
}
