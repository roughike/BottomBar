package com.roughike.bottombar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

/*
 * BottomBar library for Android
 * Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BottomBar extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    private static final long ANIMATION_DURATION = 150;
    private static final int MAX_FIXED_TAB_COUNT = 3;

    private static final String STATE_CURRENT_SELECTED_TAB = "STATE_CURRENT_SELECTED_TAB";
    private static final String TAG_BOTTOM_BAR_VIEW_INACTIVE = "BOTTOM_BAR_VIEW_INACTIVE";
    private static final String TAG_BOTTOM_BAR_VIEW_ACTIVE = "BOTTOM_BAR_VIEW_ACTIVE";

    private Context mContext;
    private boolean mIsTabletMode;

    private ViewGroup mUserContentContainer;
    private View mOuterContainer;
    private ViewGroup mItemContainer;

    private View mBackgroundView;
    private View mBackgroundOverlay;
    private View mShadowView;

    private int mPrimaryColor;
    private int mInActiveColor;
    private int mWhiteColor;

    private int mScreenWidth;
    private int mTwoDp;
    private int mTenDp;
    private int mMaxFixedItemWidth;

    private OnTabSelectedListener mListener;
    private OnMenuTabSelectedListener mMenuListener;

    private int mCurrentTabPosition;
    private boolean mIsShiftingMode;

    private FragmentManager mFragmentManager;
    private int mFragmentContainer;

    private BottomBarItemBase[] mItems;
    private HashMap<Integer, Integer> mColorMap;

    private int mCurrentBackgroundColor;
    private int mDefaultBackgroundColor;

    private boolean mDrawBehindNavBar = true;
    private boolean mUseTopOffset = true;

    private boolean mUseOnlyStatusBarOffset;

    /**
     * Bind the BottomBar to your Activity, and inflate your layout here.
     * <p/>
     * Remember to also call {@link #onRestoreInstanceState(Bundle)} inside
     * of your {@link Activity#onSaveInstanceState(Bundle)} to restore the state.
     *
     * @param activity           an Activity to attach to.
     * @param savedInstanceState a Bundle for restoring the state on configuration change.
     * @return a BottomBar at the bottom of the screen.
     */
    public static BottomBar attach(Activity activity, Bundle savedInstanceState) {
        BottomBar bottomBar = new BottomBar(activity);
        bottomBar.onRestoreInstanceState(savedInstanceState);

        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View oldLayout = contentView.getChildAt(0);
        contentView.removeView(oldLayout);

        bottomBar.getUserContainer()
                .addView(oldLayout, 0, oldLayout.getLayoutParams());
        contentView.addView(bottomBar, 0);

        return bottomBar;
    }

    /**
     * Bind the BottomBar to the specified View's parent, and inflate
     * your layout there. Useful when the BottomBar overlaps some content
     * that shouldn't be overlapped.
     * <p/>
     * Remember to also call {@link #onRestoreInstanceState(Bundle)} inside
     * of your {@link Activity#onSaveInstanceState(Bundle)} to restore the state.
     *
     * @param view               a View, which parent we're going to attach to.
     * @param savedInstanceState a Bundle for restoring the state on configuration change.
     * @return a BottomBar at the bottom of the screen.
     */
    public static BottomBar attach(View view, Bundle savedInstanceState) {
        BottomBar bottomBar = new BottomBar(view.getContext());
        bottomBar.onRestoreInstanceState(savedInstanceState);

        ViewGroup contentView = (ViewGroup) view.getParent();

        if (contentView != null) {
            View oldLayout = contentView.getChildAt(0);
            contentView.removeView(oldLayout);

            bottomBar.getUserContainer()
                    .addView(oldLayout, oldLayout.getLayoutParams());
            contentView.addView(bottomBar, 0);
        } else {
            if (view.getLayoutParams() == null) {
                bottomBar.getUserContainer()
                        .addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                bottomBar.getUserContainer()
                        .addView(view, view.getLayoutParams());
            }
        }

        return bottomBar;
    }

    /**
     * Set tabs and fragments for this BottomBar. When setting more than 3 items,
     * only the icons will show by default, but the selected item
     * will have the text visible.
     *
     * @param fragmentManager   a FragmentManager for managing the Fragments.
     * @param containerResource id for the layout to inflate Fragments to.
     * @param fragmentItems     an array of {@link BottomBarFragment} objects.
     */
    public void setFragmentItems(FragmentManager fragmentManager, @IdRes int containerResource,
                                 BottomBarFragment... fragmentItems) {
        clearItems();
        mFragmentManager = fragmentManager;
        mFragmentContainer = containerResource;
        mItems = fragmentItems;
        updateItems(mItems);
    }

    /**
     * Set tabs for this BottomBar. When setting more than 3 items,
     * only the icons will show by default, but the selected item
     * will have the text visible.
     *
     * @param bottomBarTabs an array of {@link BottomBarTab} objects.
     */
    public void setItems(BottomBarTab... bottomBarTabs) {
        clearItems();
        mItems = bottomBarTabs;
        updateItems(mItems);
    }

    /**
     * Set items from an XML menu resource file.
     *
     * @param menuRes  the menu resource to inflate items from.
     * @param listener listener for tab change events.
     */
    public void setItemsFromMenu(@MenuRes int menuRes, OnMenuTabSelectedListener listener) {
        clearItems();
        mItems = MiscUtils.inflateMenuFromResource((Activity) getContext(), menuRes);
        mMenuListener = listener;
        updateItems(mItems);
    }

    /**
     * Set a listener that gets fired when the selected tab changes.
     *
     * @param listener a listener for monitoring changes in tab selection.
     */
    public void setOnItemSelectedListener(OnTabSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Select a tab at the specified position.
     *
     * @param position the position to select.
     */
    public void selectTabAtPosition(int position, boolean animate) {
        unselectTab(mItemContainer.findViewWithTag(TAG_BOTTOM_BAR_VIEW_ACTIVE), animate);
        selectTab(mItemContainer.getChildAt(position), animate);

        if (mListener != null) {
            mListener.onItemSelected(position);
        }
    }

    /**
     * Call this method in your Activity's onSaveInstanceState
     * to keep the BottomBar's state on configuration change.
     *
     * @param outState the Bundle to save data to.
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_SELECTED_TAB, mCurrentTabPosition);
    }

    /**
     * Map a background color for a Tab, that changes the whole BottomBar
     * background color when the Tab is selected.
     *
     * @param tabPosition zero-based index for the tab.
     * @param color       a hex color for the tab, such as 0xFF00FF00.
     */
    public void mapColorForTab(int tabPosition, int color) {
        if (mItems == null || mItems.length == 0) {
            throw new UnsupportedOperationException("You have no BottomBar Tabs set yet. " +
                    "Please set them first before calling the mapColorForTab method.");
        } else if (tabPosition > mItems.length || tabPosition < 0) {
            throw new IndexOutOfBoundsException("Cant map color for Tab " +
                    "index " + tabPosition + ". You have no BottomBar Tabs at that position.");
        }

        if (!mIsShiftingMode || mIsTabletMode) return;

        if (mColorMap == null) {
            mColorMap = new HashMap<>();
        }

        if (tabPosition == mCurrentTabPosition
                && mCurrentBackgroundColor != color) {
            mCurrentBackgroundColor = color;
            mBackgroundView.setBackgroundColor(color);
        }

        mColorMap.put(tabPosition, color);
    }

    /**
     * Map a background color for a Tab, that changes the whole BottomBar
     * background color when the Tab is selected.
     *
     * @param tabPosition zero-based index for the tab.
     * @param color       a hex color for the tab, such as "#00FF000".
     */
    public void mapColorForTab(int tabPosition, String color) {
        mapColorForTab(tabPosition, Color.parseColor(color));
    }

    /**
     * Hid the shadow that's normally above the BottomBar.
     */
    public void hideShadow() {
        if (mShadowView != null) {
            mShadowView.setVisibility(GONE);
        }
    }

    /**
     * Prevent the BottomBar drawing behind the Navigation Bar and making
     * it transparent. Must be called before setting items.
     */
    public void noNavBarGoodness() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call noNavBarGoodness() before setting the items, preferably " +
                    "right after attaching it to your layout.");
        }

        mDrawBehindNavBar = false;
    }

    /**
     * Super ugly hacks
     * ----------------------------/
     */

    /**
     * If you get some unwanted extra padding in the top (such as
     * when using CoordinatorLayout), this fixes it.
     */
    public void noTopOffset() {
        mUseTopOffset = false;
    }

    /**
     * If your ActionBar gets inside the status bar for some reason,
     * this fixes it.
     */
    public void useOnlyStatusBarTopOffset() {
        mUseOnlyStatusBarOffset = true;
    }

    /**
     * ------------------------------------------- //
     */
    public BottomBar(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        mPrimaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);
        mInActiveColor = ContextCompat.getColor(getContext(), R.color.bb_inActiveBottomBarItemColor);
        mWhiteColor = ContextCompat.getColor(getContext(), R.color.white);

        mScreenWidth = MiscUtils.getScreenWidth(mContext);
        mTwoDp = MiscUtils.dpToPixel(mContext, 2);
        mTenDp = MiscUtils.dpToPixel(mContext, 10);
        mMaxFixedItemWidth = MiscUtils.dpToPixel(mContext, 168);

        initializeViews();
    }


    private void initializeViews() {
        View rootView = View.inflate(mContext,
                R.layout.bb_bottom_bar_item_container, null);

        mIsTabletMode = rootView.findViewById(R.id.bb_tablet_right_border) != null;
        mUserContentContainer = (ViewGroup) rootView.findViewById(R.id.bb_user_content_container);
        mOuterContainer = rootView.findViewById(R.id.bb_bottom_bar_outer_container);
        mItemContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_item_container);

        mBackgroundView = rootView.findViewById(R.id.bb_bottom_bar_background_view);
        mBackgroundOverlay = rootView.findViewById(R.id.bb_bottom_bar_background_overlay);

        mShadowView = rootView.findViewById(R.id.bb_bottom_bar_shadow);

        addView(rootView);
    }

    protected ViewGroup getUserContainer() {
        return mUserContentContainer;
    }

    protected View getOuterContainer() {
        return mOuterContainer;
    }

    protected boolean drawBehindNavBar() {
        return mDrawBehindNavBar;
    }

    protected boolean useTopOffset() {
        return mUseTopOffset;
    }

    protected boolean useOnlyStatusbarOffset() {
        return mUseOnlyStatusBarOffset;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag().equals(TAG_BOTTOM_BAR_VIEW_INACTIVE)) {
            unselectTab(findViewWithTag(TAG_BOTTOM_BAR_VIEW_ACTIVE), true);
            selectTab(v, true);
            updateSelectedTab(findItemPosition(v));
        }
    }

    private void updateSelectedTab(int newPosition) {
        if (newPosition != mCurrentTabPosition) {
            mCurrentTabPosition = newPosition;

            if (mListener != null) {
                mListener.onItemSelected(mCurrentTabPosition);
            }

            if (mMenuListener != null && mItems instanceof BottomBarTab[]) {
                mMenuListener.onMenuItemSelected(((BottomBarTab) mItems[mCurrentTabPosition]).id);
            }

            updateCurrentFragment();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if ((mIsShiftingMode || mIsTabletMode) && v.getTag().equals(TAG_BOTTOM_BAR_VIEW_INACTIVE)) {
            Toast.makeText(mContext, mItems[findItemPosition(v)].getTitle(mContext), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void updateItems(BottomBarItemBase[] bottomBarItems) {
        int index = 0;
        int biggestWidth = 0;
        mIsShiftingMode = MAX_FIXED_TAB_COUNT < bottomBarItems.length;

        if (!mIsTabletMode && mIsShiftingMode) {
            mDefaultBackgroundColor = mCurrentBackgroundColor = mPrimaryColor;
            mBackgroundView.setBackgroundColor(mDefaultBackgroundColor);

            if (mContext instanceof Activity) {
                navBarMagic((Activity) mContext, this);
            }
        }

        View[] viewsToAdd = new View[bottomBarItems.length];

        for (BottomBarItemBase bottomBarItemBase : bottomBarItems) {
            int layoutResource;

            if (mIsShiftingMode && !mIsTabletMode) {
                layoutResource = R.layout.bb_bottom_bar_item_shifting;
            } else {
                layoutResource = R.layout.bb_bottom_bar_item_fixed;
            }

            View bottomBarView = View.inflate(mContext, layoutResource, null);
            ImageView icon = (ImageView) bottomBarView.findViewById(R.id.bb_bottom_bar_icon);

            icon.setImageDrawable(bottomBarItemBase.getIcon(mContext));

            if (!mIsTabletMode) {
                TextView title = (TextView) bottomBarView.findViewById(R.id.bb_bottom_bar_title);
                title.setText(bottomBarItemBase.getTitle(mContext));
            }

            if (!mIsTabletMode && mIsShiftingMode) {
                icon.setColorFilter(mWhiteColor);
            }

            if (bottomBarItemBase instanceof BottomBarTab) {
                bottomBarView.setId(((BottomBarTab) bottomBarItemBase).id);
            }

            if (index == mCurrentTabPosition) {
                selectTab(bottomBarView, false);
            } else {
                unselectTab(bottomBarView, false);
            }

            if (!mIsTabletMode) {
                if (bottomBarView.getWidth() > biggestWidth) {
                    biggestWidth = bottomBarView.getWidth();
                }

                viewsToAdd[index] = bottomBarView;
            } else {
                mItemContainer.addView(bottomBarView);
            }

            bottomBarView.setOnClickListener(this);
            bottomBarView.setOnLongClickListener(this);
            index++;
        }

        if (!mIsTabletMode) {
            int proposedItemWidth = Math.min(
                    MiscUtils.dpToPixel(mContext, mScreenWidth / bottomBarItems.length),
                    mMaxFixedItemWidth
            );

            LinearLayout.LayoutParams params = new LinearLayout
                    .LayoutParams(proposedItemWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

            for (View bottomBarView : viewsToAdd) {
                bottomBarView.setLayoutParams(params);
                mItemContainer.addView(bottomBarView);
            }
        }

        updateCurrentFragment();
    }

    private void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentTabPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_TAB, -1);

            if (mCurrentTabPosition == -1) {
                mCurrentTabPosition = 0;
                Log.e("BottomBar", "You must override the Activity's onSave" +
                        "InstanceState(Bundle outState) and call BottomBar.onSaveInstanc" +
                        "eState(outState) there to restore the state properly.");
            }
        }
    }

    private void selectTab(View tab, boolean animate) {
        tab.setTag(TAG_BOTTOM_BAR_VIEW_ACTIVE);
        ImageView icon = (ImageView) tab.findViewById(R.id.bb_bottom_bar_icon);
        TextView title = (TextView) tab.findViewById(R.id.bb_bottom_bar_title);

        int tabPosition = findItemPosition(tab);

        if (!mIsShiftingMode || mIsTabletMode) {
            icon.setColorFilter(mPrimaryColor);

            if (title != null) {
                title.setTextColor(mPrimaryColor);
            }
        }

        if (title == null) {
            return;
        }

        int translationY = mIsShiftingMode ? mTenDp : mTwoDp;

        if (animate) {
            title.animate()
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(1)
                    .scaleY(1)
                    .start();
            tab.animate()
                    .setDuration(ANIMATION_DURATION)
                    .translationY(-translationY)
                    .start();

            if (mIsShiftingMode) {
                icon.animate()
                        .setDuration(ANIMATION_DURATION)
                        .alpha(1.0f)
                        .start();
            }

            handleBackgroundColorChange(tabPosition, tab);
        } else {
            title.setScaleX(1);
            title.setScaleY(1);
            tab.setTranslationY(-translationY);

            if (mIsShiftingMode) {
                icon.setAlpha(1.0f);
            }
        }
    }

    private void handleBackgroundColorChange(int tabPosition, View tab) {
        if (!mIsShiftingMode || mIsTabletMode) return;

        if (mColorMap != null && mColorMap.containsKey(tabPosition)) {
            handleBackgroundColorChange(
                    tab, mColorMap.get(tabPosition));
        } else {
            handleBackgroundColorChange(tab, mDefaultBackgroundColor);
        }
    }

    private void handleBackgroundColorChange(View tab, int color) {
        MiscUtils.animateBGColorChange(tab,
                mBackgroundView,
                mBackgroundOverlay,
                color);
        mCurrentBackgroundColor = color;
    }

    private void unselectTab(View tab, boolean animate) {
        tab.setTag(TAG_BOTTOM_BAR_VIEW_INACTIVE);

        ImageView icon = (ImageView) tab.findViewById(R.id.bb_bottom_bar_icon);
        TextView title = (TextView) tab.findViewById(R.id.bb_bottom_bar_title);

        if (!mIsShiftingMode || mIsTabletMode) {
            icon.setColorFilter(mInActiveColor);

            if (title != null) {
                title.setTextColor(mInActiveColor);
            }
        }

        if (title == null) {
            return;
        }

        float scale = mIsShiftingMode ? 0 : 0.86f;

        if (animate) {
            title.animate()
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(scale)
                    .scaleY(scale)
                    .start();
            tab.animate()
                    .setDuration(ANIMATION_DURATION)
                    .translationY(0)
                    .start();

            if (mIsShiftingMode && !mIsTabletMode) {
                icon.animate()
                        .setDuration(ANIMATION_DURATION)
                        .alpha(0.6f)
                        .start();
            }
        } else {
            title.setScaleX(scale);
            title.setScaleY(scale);
            tab.setTranslationY(0);

            if (mIsShiftingMode && !mIsTabletMode) {
                icon.setAlpha(0.6f);
            }
        }
    }

    private int findItemPosition(View viewToFind) {
        int position = 0;

        for (int i = 0; i < mItemContainer.getChildCount(); i++) {
            View candidate = mItemContainer.getChildAt(i);

            if (candidate.equals(viewToFind)) {
                position = i;
                break;
            }
        }

        return position;
    }

    private void updateCurrentFragment() {
        if (mFragmentManager != null
                && mFragmentContainer != 0
                && mItems != null
                && mItems instanceof BottomBarFragment[]) {
            mFragmentManager.beginTransaction()
                    .replace(mFragmentContainer, ((BottomBarFragment) mItems[mCurrentTabPosition]).getFragment())
                    .commit();
        }
    }

    private void clearItems() {
        int childCount = mItemContainer.getChildCount();

        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                mItemContainer.removeView(mItemContainer.getChildAt(i));
            }
        }

        if (mFragmentManager != null) {
            mFragmentManager = null;
        }

        if (mFragmentContainer != 0) {
            mFragmentContainer = 0;
        }

        if (mItems != null) {
            mItems = null;
        }
    }

    private static void navBarMagic(Activity activity, BottomBar bottomBar) {
        Resources res = activity.getResources();
        int softMenuIdentifier = res
                .getIdentifier("config_showNavigationBar", "bool", "android");

        if (!bottomBar.drawBehindNavBar()
                || !(softMenuIdentifier > 0 && res.getBoolean(softMenuIdentifier))) {
            return;
        }

        if (ViewConfiguration.get(activity).hasPermanentMenuKey() && KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.getWindow().getAttributes().flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;

            int navBarIdentifier = res.getIdentifier("navigation_bar_height",
                    "dimen", "android");
            final int navBarHeight;

            if (navBarIdentifier > 0) {
                navBarHeight = res.getDimensionPixelSize(navBarIdentifier);
            } else {
                navBarHeight = MiscUtils.dpToPixel(activity, 48);
            }

            if (bottomBar.useTopOffset()) {
                int offset;
                int statusBarResource = res
                        .getIdentifier("status_bar_height", "dimen", "android");

                if (statusBarResource > 0) {
                    offset = res.getDimensionPixelSize(statusBarResource);
                } else {
                    offset = MiscUtils.dpToPixel(activity, 25);
                }

                if (!bottomBar.useOnlyStatusbarOffset()) {
                    TypedValue tv = new TypedValue();
                    if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                        offset += TypedValue.complexToDimensionPixelSize(tv.data,
                                res.getDisplayMetrics());
                    } else {
                        offset += MiscUtils.dpToPixel(activity, 56);
                    }
                }

                bottomBar.getUserContainer().setPadding(0, offset, 0, 0);
            }

            final View outerContainer = bottomBar.getOuterContainer();
            bottomBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    outerContainer.getLayoutParams().height =
                            outerContainer.getHeight() + navBarHeight;
                    ViewTreeObserver obs = outerContainer.getViewTreeObserver();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        obs.removeOnGlobalLayoutListener(this);
                    } else {
                        obs.removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }
}
