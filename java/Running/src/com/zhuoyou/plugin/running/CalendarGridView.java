package com.zhuoyou.plugin.running;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;

/**
 * 用于生成日历展示的GridView布局
 *
 * @author zhouxin@easier.cn
 */
public class CalendarGridView extends GridView {

    /**
     * 当前操作的上下文对象
     */
    private Context mContext;
    private  Display display;

    /**
     * CalendarGridView 构造器
     *
     * @param context 当前操作的上下文对象
     */
    public CalendarGridView(Context context) {
        super(context);
        mContext = context;

        setGirdView();
    }

    /**
     * 初始化gridView 控件的布局
     */
    private void setGirdView() {
        WindowManager windowManager = ((Activity) mContext).getWindowManager();
        display = windowManager.getDefaultDisplay();
        int i = display.getWidth() / 7;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        setNumColumns(7);// 设置每行列数
        setGravity(Gravity.CENTER_VERTICAL);// 位置居中
		setSelector(new ColorDrawable(Color.TRANSPARENT));
		setVerticalSpacing(1);
		setBackgroundColor(getResources().getColor(R.color.cal_bg));
		int j = display.getWidth() - (i * 7);
		int x = j / 2;
		setPadding(x, 0, 0, 0);// In the middle
    }

}
