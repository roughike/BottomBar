package com.roughike.bottombar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

/**
 * Created by crugnola on 3/25/16.
 * BottomBar
 */
public class MenuParser {
    private MenuItem item;

    public MenuParser() { }

    static class MenuItem {
        private int itemId;
        private CharSequence itemTitle;
        private CharSequence itemTitleCondensed;
        private int itemIconResId;
        private boolean itemEnabled;
        private int itemColor;

        public int getItemId() {
            return itemId;
        }

        public CharSequence getItemTitle() {
            return itemTitle;
        }

        public CharSequence getItemTitleCondensed() {
            return itemTitleCondensed;
        }

        public int getItemIconResId() {
            return itemIconResId;
        }

        public boolean isItemEnabled() {
            return itemEnabled;
        }

        public int getItemColor() {
            return itemColor;
        }
    }

    public MenuItem pull() {
        MenuItem current = item;
        item = null;
        return current;
    }

    public boolean hasItem() {
        return null != item;
    }

    /**
     * Called when the parser is pointing to an item tag.
     */
    public void readItem(Context mContext, AttributeSet attrs) {
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.BB_MenuItem);
        item = new MenuItem();
        item.itemId = a.getResourceId(R.styleable.BB_MenuItem_android_id, 0);
        item.itemTitle = a.getText(R.styleable.BB_MenuItem_android_title);
        item.itemTitleCondensed = a.getText(R.styleable.BB_MenuItem_android_titleCondensed);
        item.itemIconResId = a.getResourceId(R.styleable.BB_MenuItem_android_icon, 0);
        item.itemEnabled = a.getBoolean(R.styleable.BB_MenuItem_android_enabled, true);
        item.itemColor = a.getColor(R.styleable.BB_MenuItem_android_color, 0);
        a.recycle();
    }
}
