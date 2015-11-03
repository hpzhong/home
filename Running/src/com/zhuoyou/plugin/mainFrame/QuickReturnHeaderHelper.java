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


import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.zhuoyou.plugin.custom.XListView;
import com.zhuoyou.plugin.custom.XListView.OnXListHeaderViewListener;
import com.zhuoyou.plugin.mainFrame.ListViewScrollObserver.OnListViewScrollListener;
import com.zhuoyou.plugin.running.R;

public class QuickReturnHeaderHelper implements OnGlobalLayoutListener, OnXListHeaderViewListener{
    protected static final String TAG = "QuickReturnHeaderHelper";
    private View realHeader;
    private FrameLayout.LayoutParams realHeaderLayoutParams;
    private int headerHeight;
    private int headerTop;
    private View dummyHeader;
    private int contentResId;
    private int headerResId;
    private boolean waitingForExactHeaderHeight = true;
    private Context context;
    private XListView listView;
    private LayoutInflater inflater;
    private View content;
    private ViewGroup listParentContainer;
    private ViewGroup root;
    private static int scroll_state = 0;
    private boolean snapped = true;
    private OnSnappedChangeListener onSnappedChangeListener;
    private OnListScrollForFragment mFragment_listener;
    private Animation animation,head_scroll_up_animation;
    private int lastFirstVisibleItem;
    private int lastTop;
    private int scrollPosition;
    private int lastHeight;
    private int drag_down_delta = 0;;
        
    /**
     * True if the last scroll movement was in the "up" direction.
     */
    private boolean scrollingUp;
    /**
     * Maximum time it takes the show/hide animation to complete. Maximum because it will take much less time if the
     * header is already partially hidden or shown.
     * <p>
     * In milliseconds.
     */
    private static final long ANIMATION_DURATION = 400;

    public interface OnSnappedChangeListener {
        void onSnappedChange(boolean snapped);
    }

    public interface OnListScrollForFragment {
        void onScrollStateChange(int state);
        void onScrollListDispatch(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount,int delta);
        
    }
  
    public QuickReturnHeaderHelper(Context context, int contentResId, int headerResId) {
        this.context = context;
        this.contentResId = contentResId;
        this.headerResId = headerResId;
    }
    public QuickReturnHeaderHelper(Context context, int contentResId, int headerResId,ViewGroup listparent) {
        this.context = context;
        this.contentResId = contentResId;
        this.headerResId = headerResId;
        this.listParentContainer = listparent;
    }
    public View createView() {
        inflater = LayoutInflater.from(context);
        content = inflater.inflate(contentResId, listParentContainer,false);

        realHeader = inflater.inflate(headerResId, listParentContainer,false);
        realHeaderLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        realHeaderLayoutParams.gravity = Gravity.TOP;

        // Use measured height here as an estimate of the header height, later on after the layout is complete 
        // we'll use the actual height
        //before layout,we get real size of view
        realHeader.measure(0, 0);
        headerHeight = realHeader.getMeasuredHeight();
        listView = (XListView) content.findViewById(R.id.listview_activity);
        if (listView != null) {
            createListView();
        }
        listView.SetXlistHeaderLisetener(this);
        return root;
    }

    public void setOnSnappedChangeListener(OnSnappedChangeListener onSnapListener) {
        this.onSnappedChangeListener = onSnapListener;
    }

    public void setOnListScrollForFragmentListener(OnListScrollForFragment fragmentlistlistener) {
        this.mFragment_listener = fragmentlistlistener;
    }

    private void createListView() {
        root = (FrameLayout) inflater.inflate(R.layout.qrh_listview_container, listParentContainer,false);
        root.addView(content);

        listView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        ListViewScrollObserver observer = new ListViewScrollObserver(listView);
        observer.setOnScrollUpAndDownListener(new OnListViewScrollListener() {
			@Override
			public void onScrollStateChange(int state) {
				// TODO Auto-generated method stub
				scroll_state = state;
				if(mFragment_listener!= null){
					mFragment_listener.onScrollStateChange(state);
				}
			}

			@Override
			public void onScrollListDispatch(AbsListView view,
					int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				
			        View firstChild = view.getChildAt(0);
			        if (firstChild == null) {
			            return;
			        }
			        int top = firstChild.getTop();// view top which is instead of headerview
			        int height = firstChild.getHeight();
			        int delta;
			        int skipped = 0;
			        if (lastFirstVisibleItem == firstVisibleItem) {
			            delta = lastTop - top;
			        } else if (firstVisibleItem > lastFirstVisibleItem) {
			            skipped = firstVisibleItem - lastFirstVisibleItem - 1;
			            delta = skipped * height + lastHeight + lastTop - top;
			        } else {
			            skipped = lastFirstVisibleItem - firstVisibleItem - 1;
			            delta = skipped * -height + lastTop - (height + top);
			        }

//			        boolean exact = skipped > 0;
//			        scrollPosition += -delta;
			        
					if(mFragment_listener!= null){
						mFragment_listener.onScrollListDispatch(view, firstVisibleItem, visibleItemCount, totalItemCount,delta);
					}
//					Log.d("zzb","top ="+top+"  ||delta ="+delta+"  ||headerHeight = "+headerHeight);
					
					if(firstVisibleItem == 1){
						if (delta < 0) {//scroll down 
							if(top+delta<headerHeight&&headerTop != 0){//headview half show
								headerTop = top;
							}else{
								headerTop = 0;
							}
				        } else if (delta > 0){//scroll up 
				            if (top + delta < 0) {
				            	headerTop = top;
				            }else{
				            	headerTop = 0;
				            }
				        }
					}else if(firstVisibleItem == 0){//roll back show
//						Log.d("zzb","drag_down delta in OnHeaderSyncShowDown = "+drag_down_delta);
						if(delta == 0&&top ==0){
							headerTop = drag_down_delta;							
						}else{
							if(delta>0)
								headerTop = drag_down_delta;							
						}
						
					}else{
//						Log.d("zzb","lastFirstVisibleItem ="+lastFirstVisibleItem+"firstVisibleItem =" +firstVisibleItem);
						if (delta < 0) {//scroll down
								headerTop = 0;
				        } else if (delta > 0){//scroll up 
				            	headerTop = -headerHeight;
				        }						
					}
					
			        // I'm aware that offsetTopAndBottom is more efficient, but it gave me trouble when scrolling to the bottom of the list
			        if (realHeaderLayoutParams.topMargin != headerTop) {
			            realHeaderLayoutParams.topMargin = headerTop;
			            Log.v(TAG, "topMargin=" + headerTop);
			            realHeader.setLayoutParams(realHeaderLayoutParams);
			        }
			        lastFirstVisibleItem = firstVisibleItem;
			        lastTop = top;
			        lastHeight = firstChild.getHeight();
			        
			}
        });
        root.addView(realHeader, realHeaderLayoutParams);
        dummyHeader = new View(context);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, headerHeight);
        dummyHeader.setLayoutParams(params);
        listView.addHeaderView(dummyHeader);
    }

    private void snap(boolean newValue) {
        if (snapped == newValue) {
            return;
        }
        snapped = newValue;
        if (onSnappedChangeListener != null) {
            onSnappedChangeListener.onSnappedChange(snapped);
        }
        Log.v(TAG, "snapped=" + snapped);
    }

    @Override
    public void onGlobalLayout() {
        if (waitingForExactHeaderHeight && dummyHeader.getHeight() > 0) {
            headerHeight = dummyHeader.getHeight();
            waitingForExactHeaderHeight = false;
            LayoutParams params = dummyHeader.getLayoutParams();
            params.height = headerHeight;
            dummyHeader.setLayoutParams(params);
        }
    }
	@Override
	public void OnHeaderSyncShowDown(int delta) {
		drag_down_delta = delta;
	}
	@Override
	public void OnHeaderSyncScrollBack(int position, int time) {
		drag_down_delta = position;
		headerTop = drag_down_delta;
		
	}
	
	public void reSetHeaderMargin(){
		if(	headerTop != 0){
			headerTop = 0;
			realHeaderLayoutParams.topMargin = headerTop;
			realHeader.setLayoutParams(realHeaderLayoutParams);
		}
	}
	
}