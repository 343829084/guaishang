package com.houwei.guaishang.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.github.jdsjlzx.recyclerview.LRecyclerView;

/**
 * create by lei on 2018/11/15/015
 * desc:
 */
public class ScrollViewRecycleView extends LRecyclerView {
    public ScrollViewRecycleView(Context context) {
        super(context);
    }

    public ScrollViewRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //核心在此
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec+50);
    }
}
