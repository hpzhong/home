package com.zhuoyou.plugin.custom;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.Toast;

import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.running.MainService;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;

public class XListView extends ListView implements OnScrollListener {

	private Context mContext;
	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private IXListViewListener mListViewListener;

	// add by zhouzhongbo@20150108 start
	private OnXListHeaderViewListener mXlistHearListener;
	// add by zhouzhongbo@20150108 end

	// -- header view
	private XListViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent;
	private LinearLayout mSleepHeader;

	private int mHeaderViewHeight; // header view's height
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false; // is refreashing.

	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;

	private final static int SCROLL_DURATION = 400; // scroll back duration
	private final static float OFFSET_RADIO = 2.5f; // 阻尼系数

	/**
	 * @param context
	 */
	public XListView(Context context) {
		super(context);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	private void initWithContext(Context context) {
		mContext = context;
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new XListViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView
				.findViewById(R.id.xlistview_header_content);
		mSleepHeader = (LinearLayout) mHeaderView.findViewById(R.id.sleep_header);
		addHeaderView(mHeaderView);
		// init header height
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderViewContent.getHeight();
						getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
					}
				});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		Log.i("2222", "mPullRefreshing = " + mPullRefreshing);
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta
				+ mHeaderView.getVisiableHeight());
		// mofedfy by zhouzhongbo@20150108 start
		if (mXlistHearListener != null) {
			int dd = getScrollY() >= 0 ? 0 : Math.abs(getScrollY());
			mXlistHearListener.OnHeaderSyncShowDown((int) delta
					+ mHeaderView.getVisiableHeight() + dd);
		}
		// mofedfy by zhouzhongbo@20150108 end
		if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(XListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(XListViewHeader.STATE_NORMAL);
			}
		}
		setSelection(0); // scroll to top each time
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);

		// mofedfy by zhouzhongbo@20150108 start
		if (mXlistHearListener != null) {
			mXlistHearListener.OnHeaderSyncScrollBack(finalHeight,
					SCROLL_DURATION);
		}
		// mofedfy by zhouzhongbo@20150108 end

		// trigger computeScroll
		invalidate();
	}

	private void startRefreshing() {
		if (mPullRefreshing)
			return;

		if (CloudSync.isSync) {
			resetHeaderHeight();
			Toast.makeText(mContext, R.string.ps_cloud_sync_wait,
					Toast.LENGTH_SHORT).show();
		} else if (BluetoothService.getInstance().isConnected()
				|| (RunningApp.isBLESupport && BleManagerService.getInstance()
						.GetBleConnectState())) {
			mPullRefreshing = true;
			mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
			if (mListViewListener != null) {
				mListViewListener.onRefresh();
			}
		} else {
			List<BluetoothDevice> bondList = Util.getBondDevice();
			if (bondList != null && bondList.size() > 0) {
				mPullRefreshing = true;
				mHeaderView.setState(XListViewHeader.STATE_CONNECTING);
				IntentFilter intent = new IntentFilter();
				intent.addAction("com.zhuoyou.running.connect.success");
				intent.addAction("com.zhuoyou.running.connect.failed");
				mContext.registerReceiver(mBTConnectReceiver, intent);
				BluetoothDevice currentDevice = bondList.get(0);
				Util.connect(currentDevice);
			} else {
				resetHeaderHeight();
				Toast.makeText(mContext, R.string.ps_connect_device,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private BroadcastReceiver mBTConnectReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("com.zhuoyou.running.connect.success")) {
				mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
				if (mListViewListener != null) {
					mListViewListener.onRefresh();
				}
			} else if (action.equals("com.zhuoyou.running.connect.failed")) {
				mPullRefreshing = false;
				resetHeaderHeight();
				Toast.makeText(mContext,
						R.string.xlistview_header_hint_connect_fail,
						Toast.LENGTH_SHORT).show();
			}
			mContext.unregisterReceiver(mBTConnectReceiver);
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
     		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (getFirstVisiblePosition() == 0
					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)
					&& !MainService.syncnow) {
				// the first item is showing, header has shown or pull down.
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
				

			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mLastY = -1; // reset
			if (getFirstVisiblePosition() == 0 && !MainService.syncnow) {
				// invoke refresh
				if (mEnablePullRefresh
						&& mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
					startRefreshing();
				}
				resetHeaderHeight();
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	public void setXListViewListener(IXListViewListener l) {
		mListViewListener = l;
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back.
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}

	public interface OnXListHeaderViewListener {
		public void OnHeaderSyncShowDown(int height);

		public void OnHeaderSyncScrollBack(int position, int time);

	}

	public void SetXlistHeaderLisetener(OnXListHeaderViewListener mlistener) {
		mXlistHearListener = mlistener;
	}

	/**
	 * implements this interface to get refresh/load more event.
	 */
	public interface IXListViewListener {
		public void onRefresh();
	}

	public void setSleepHeaderBackground() {
		mSleepHeader.setBackgroundColor(Color.rgb(65, 54, 111));
	}
}
