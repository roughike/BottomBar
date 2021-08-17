package com.roughike.bottombar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class BottomBarTabContainer extends LinearLayout {

    public BottomBarTabContainer(Context context) {
        super(context);
    }

    public BottomBarTabContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomBarTabContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("NewApi")
    public BottomBarTabContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        return super.addViewInLayout(child, index, params);
    }
}