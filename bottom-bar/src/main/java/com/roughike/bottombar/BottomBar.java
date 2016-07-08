package com.roughike.bottombar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.scrollsweetness.BottomNavigationBehavior;

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
public class BottomBar extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {
    private static final long ANIMATION_DURATION = 150;

    private static final String STATE_CURRENT_SELECTED_TAB = "STATE_CURRENT_SELECTED_TAB";
    private static final String STATE_BADGE_STATES_BUNDLE = "STATE_BADGE_STATES_BUNDLE";
    private static final String TAG_BOTTOM_BAR_VIEW_INACTIVE = "BOTTOM_BAR_VIEW_INACTIVE";
    private static final String TAG_BOTTOM_BAR_VIEW_ACTIVE = "BOTTOM_BAR_VIEW_ACTIVE";
    private static final String TAG_BADGE = "BOTTOMBAR_BADGE_";

    private Context mContext;
    private boolean mIsComingFromRestoredState;
    private boolean mIgnoreTabletLayout;
    private boolean mIsTabletMode;
    private boolean mIsShy;
    private boolean mShyHeightAlreadyCalculated;
    private boolean mUseExtraOffset;

    private ViewGroup mUserContentContainer;
    private ViewGroup mOuterContainer;
    private ViewGroup mItemContainer;

    private View mBackgroundView;
    private View mBackgroundOverlay;
    private View mShadowView;
    private View mTabletRightBorder;
    private View mPendingUserContentView;

    private Integer mPrimaryColor;
    private Integer mInActiveColor;
    private Integer mDarkBackgroundColor;
    private Integer mWhiteColor;
    private float mTabAlpha = 0.6f;

    private int mScreenWidth;
    private int mTenDp;
    private int mSixDp;
    private int mSixteenDp;
    private int mEightDp;
    private int mMaxFixedItemWidth;
    private int mMaxInActiveShiftingItemWidth;
    private int mInActiveShiftingItemWidth;
    private int mActiveShiftingItemWidth;

    private Object mListener;
    private Object mMenuListener;

    private int mCurrentTabPosition;
    private boolean mIsShiftingMode;

    private Object mFragmentManager;
    private int mFragmentContainer;

    private BottomBarItemBase[] mItems;
    private HashMap<Integer, Integer> mColorMap;
    private HashMap<Integer, Object> mBadgeMap;
    private HashMap<Integer, Boolean> mBadgeStateMap;

    private int mCurrentBackgroundColor;
    private int mDefaultBackgroundColor;

    private boolean mIsDarkTheme;
    private boolean mIgnoreNightMode;
    private boolean mIgnoreShiftingResize;
    private boolean mIgnoreScalingResize;

    private int mCustomActiveTabColor;

    private boolean mDrawBehindNavBar = true;
    private boolean mUseTopOffset = true;
    private boolean mUseOnlyStatusBarOffset;

    private int mPendingTextAppearance = -1;
    private Typeface mPendingTypeface;

    // For fragment state restoration
    private boolean mShouldUpdateFragmentInitially;

    private int mMaxFixedTabCount = 3;

    /**
     * Bind the BottomBar to your Activity, and inflate your layout here.
     * <p/>
     * Remember to also call {@link #onSaveInstanceState(Bundle)} inside
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

        bottomBar.setPendingUserContentView(oldLayout);
        contentView.addView(bottomBar, 0);

        return bottomBar;
    }

    public static BottomBar attach(Activity activity, Bundle savedInstanceState, int backgroundColor, int activeIconColor, float alpha) {
        BottomBar bottomBar = new BottomBar(activity, backgroundColor, activeIconColor, alpha);
        bottomBar.onRestoreInstanceState(savedInstanceState);

        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View oldLayout = contentView.getChildAt(0);
        contentView.removeView(oldLayout);

        bottomBar.setPendingUserContentView(oldLayout);
        contentView.addView(bottomBar, 0);

        return bottomBar;
    }

    private void setPendingUserContentView(View oldLayout) {
        mPendingUserContentView = oldLayout;
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

            bottomBar.setPendingUserContentView(oldLayout);
            contentView.addView(bottomBar, 0);
        } else {
            bottomBar.setPendingUserContentView(view);
        }

        return bottomBar;
    }

    /**
     * Deprecated. Breaks support for tablets.
     * Use {@link #attachShy(CoordinatorLayout, View, Bundle)} instead.
     */
    @Deprecated
    public static BottomBar attachShy(CoordinatorLayout coordinatorLayout, Bundle savedInstanceState) {
        return attachShy(coordinatorLayout, null, savedInstanceState);
    }

    /**
     * Adds the BottomBar inside of your CoordinatorLayout and shows / hides
     * it according to scroll state changes.
     * <p/>
     * Remember to also call {@link #onRestoreInstanceState(Bundle)} inside
     * of your {@link Activity#onSaveInstanceState(Bundle)} to restore the state.
     *
     * @param coordinatorLayout  a CoordinatorLayout for the BottomBar to add itself into
     * @param userContentView    the view (usually a NestedScrollView) that has your scrolling content.
     *                           Needed for tablet support.
     * @param savedInstanceState a Bundle for restoring the state on configuration change.
     * @return a BottomBar at the bottom of the screen.
     */
    public static BottomBar attachShy(CoordinatorLayout coordinatorLayout, View userContentView, Bundle savedInstanceState) {
        final BottomBar bottomBar = new BottomBar(coordinatorLayout.getContext());
        bottomBar.onRestoreInstanceState(savedInstanceState);
        bottomBar.toughChildHood(ViewCompat.getFitsSystemWindows(coordinatorLayout));

        if (userContentView != null && coordinatorLayout.getContext()
                .getResources().getBoolean(R.bool.bb_bottom_bar_is_tablet_mode)) {
            bottomBar.setPendingUserContentView(userContentView);
        }

        coordinatorLayout.addView(bottomBar);
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
    public void setFragmentItems(android.app.FragmentManager fragmentManager, @IdRes int containerResource,
                                 BottomBarFragment... fragmentItems) {
        if (fragmentItems.length > 0) {
            int index = 0;

            for (BottomBarFragment fragmentItem : fragmentItems) {
                if (fragmentItem.getFragment() == null
                        && fragmentItem.getSupportFragment() != null) {
                    throw new IllegalArgumentException("Conflict: cannot use android.app.FragmentManager " +
                            "to handle a android.support.v4.app.Fragment object at position " + index +
                            ". If you want BottomBar to handle support Fragments, use getSupportFragment" +
                            "Manager() instead of getFragmentManager().");
                }

                index++;
            }
        }

        clearItems();
        mFragmentManager = fragmentManager;
        mFragmentContainer = containerResource;
        mItems = fragmentItems;
        updateItems(mItems);
    }

    /**
     * Deprecated.
     * <p/>
     * Use either {@link #setItems(BottomBarTab...)} or
     * {@link #setItemsFromMenu(int, OnMenuTabClickListener)} and add a listener using
     * {@link #setOnTabClickListener(OnTabClickListener)} to handle tab changes by yourself.
     * <p/>
     * Set tabs and fragments for this BottomBar. When setting more than 3 items,
     * only the icons will show by default, but the selected item
     * will have the text visible.
     *
     * @param fragmentManager   a FragmentManager for managing the Fragments.
     * @param containerResource id for the layout to inflate Fragments to.
     * @param fragmentItems     an array of {@link BottomBarFragment} objects.
     */
    @Deprecated
    public void setFragmentItems(android.support.v4.app.FragmentManager fragmentManager, @IdRes int containerResource,
                                 BottomBarFragment... fragmentItems) {
        if (fragmentItems.length > 0) {
            int index = 0;

            for (BottomBarFragment fragmentItem : fragmentItems) {
                if (fragmentItem.getSupportFragment() == null
                        && fragmentItem.getFragment() != null) {
                    throw new IllegalArgumentException("Conflict: cannot use android.support.v4.app.FragmentManager " +
                            "to handle a android.app.Fragment object at position " + index +
                            ". If you want BottomBar to handle normal Fragments, use getFragment" +
                            "Manager() instead of getSupportFragmentManager().");
                }

                index++;
            }
        }
        clearItems();
        mFragmentManager = fragmentManager;
        mFragmentContainer = containerResource;
        mItems = fragmentItems;
        updateItems(mItems);
    }

    /**
     * Set items for this BottomBar.
     *
     * When setting more than 3 items, only the icons will show by
     * default, but the selected item will have the text visible.
     *
     * @param bottomBarTabs an array of {@link BottomBarTab} objects.
     */
    public void setItems(BottomBarTab... bottomBarTabs) {
        clearItems();
        mItems = bottomBarTabs;
        updateItems(mItems);
    }
    /**
     * Set items for this BottomBar from an XML menu resource file.
     *
     * When setting more than 3 items, only the icons will show by
     * default, but the selected item will have the text visible.
     *
     * @param menuRes  the menu resource to inflate items from.
     */
    public void setItems(@MenuRes int menuRes) {
        clearItems();
        mItems = MiscUtils.inflateMenuFromResource((Activity) getContext(), menuRes);
        updateItems(mItems);
    }

    /**
     * Deprecated. Use {@link #setItems(int)} and
     * {@link #setOnMenuTabClickListener(OnMenuTabClickListener)}instead.
     */
    @Deprecated
    public void setItemsFromMenu(@MenuRes int menuRes, OnMenuTabSelectedListener listener) {
        clearItems();
        mItems = MiscUtils.inflateMenuFromResource((Activity) getContext(), menuRes);
        mMenuListener = listener;
        updateItems(mItems);
    }

    /**
     * Deprecated. Use {@link #setItems(int)} and
     * {@link #setOnMenuTabClickListener(OnMenuTabClickListener)}instead.
     */
    @Deprecated
    public void setItemsFromMenu(@MenuRes int menuRes, OnMenuTabClickListener listener) {
        clearItems();
        mItems = MiscUtils.inflateMenuFromResource((Activity) getContext(), menuRes);
        mMenuListener = listener;
        updateItems(mItems);

        if (mItems != null && mItems.length > 0
                && mItems instanceof BottomBarTab[]) {
            listener.onMenuTabSelected(((BottomBarTab) mItems[mCurrentTabPosition]).id);
        }
    }

    /**
     * Deprecated. Use {@link #setOnTabClickListener(OnTabClickListener)} instead.
     */
    @Deprecated
    public void setOnItemSelectedListener(OnTabSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Set a listener that gets fired when the selected tab changes.
     *
     * Note: If listener is set after items are added to the BottomBar, onTabSelected
     * will be immediately called for the currently selected tab
     *
     * @param listener a listener for monitoring changes in tab selection.
     */
    public void setOnTabClickListener(@Nullable OnTabClickListener listener) {
        mListener = listener;

        if (mListener != null && mItems != null && mItems.length > 0) {
            listener.onTabSelected(mCurrentTabPosition);
        }
    }

    /**
     * Set a listener that gets fired when the selected tab changes, when the
     * tabs are created from an XML menu resource file.
     *
     * Note: If listener is set after items are added to the BottomBar, onMenuTabSelected
     * will be immediately called for the currently selected tab
     *
     * @param listener a listener for monitoring changes in tab selection.
     */
    public void setOnMenuTabClickListener(@Nullable OnMenuTabClickListener listener) {
        mMenuListener = listener;

        if (mMenuListener != null && mItems != null && mItems.length > 0
                && mItems instanceof BottomBarTab[]) {
            listener.onMenuTabSelected(((BottomBarTab) mItems[mCurrentTabPosition]).id);
        }
    }

    /**
     * Select a tab at the specified position.
     *
     * @param position the position to select.
     */
    public void selectTabAtPosition(int position, boolean animate) {
        if (mItems == null || mItems.length == 0) {
            throw new UnsupportedOperationException("Can't select tab at " +
                    "position " + position + ". This BottomBar has no items set yet.");
        } else if (position > mItems.length - 1 || position < 0) {
            throw new IndexOutOfBoundsException("Can't select tab at position " +
                    position + ". This BottomBar has no items at that position.");
        }

        View oldTab = mItemContainer.findViewWithTag(TAG_BOTTOM_BAR_VIEW_ACTIVE);
        View newTab = mItemContainer.getChildAt(position);

        unselectTab(oldTab, animate);
        selectTab(newTab, animate);

        updateSelectedTab(position);
        shiftingMagic(oldTab, newTab, animate);
    }

    /**
     * Sets the default tab for this BottomBar that is shown until the user changes
     * the selection.
     *
     * @param defaultTabPosition the default tab position.
     */
    public void setDefaultTabPosition(int defaultTabPosition) {
        if (mIsComingFromRestoredState) return;

        if (mItems == null) {
            mCurrentTabPosition = defaultTabPosition;
            return;
        } else if (mItems.length == 0 || defaultTabPosition > mItems.length - 1
                || defaultTabPosition < 0) {
            throw new IndexOutOfBoundsException("Can't set default tab at position " +
                    defaultTabPosition + ". This BottomBar has no items at that position.");
        }

        selectTabAtPosition(defaultTabPosition, false);
    }

    /**
     * Get the current selected tab position.
     *
     * @return the position of currently selected tab.
     */
    public int getCurrentTabPosition() {
        return mCurrentTabPosition;
    }

    /**
     * Hide the BottomBar.
     */
    public void hide() {
        setBarVisibility(GONE);
    }

    /**
     * Show the BottomBar.
     */
    public void show() {
        setBarVisibility(VISIBLE);
    }

    /**
     * Set the maximum number of tabs, after which the tabs should be shifting
     * ones with a background color.
     * <p/>
     * NOTE: You must call this method before setting any items.
     *
     * @param count maximum number of fixed tabs.
     */
    public void setMaxFixedTabs(int count) {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call the setMaxFixedTabs() method before specifying any items.");
        }

        mMaxFixedTabCount = count;
    }

    /**
     * Always show the titles and icons also on inactive tabs, even if there's more
     * than three of them.
     */
    public void useFixedMode() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call the useFixedMode() method before specifying any items.");
        }

        mMaxFixedTabCount = -1;
    }

    /**
     * Call this method in your Activity's onSaveInstanceState
     * to keep the BottomBar's state on configuration change.
     *
     * @param outState the Bundle to save data to.
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_SELECTED_TAB, mCurrentTabPosition);

        if (mBadgeMap != null && mBadgeMap.size() > 0) {
            if (mBadgeStateMap == null) {
                mBadgeStateMap = new HashMap<>();
            }

            for (Integer key : mBadgeMap.keySet()) {
                BottomBarBadge badgeCandidate = (BottomBarBadge) mOuterContainer
                        .findViewWithTag(mBadgeMap.get(key));

                if (badgeCandidate != null) {
                    mBadgeStateMap.put(key, badgeCandidate.isVisible());
                }
            }

            outState.putSerializable(STATE_BADGE_STATES_BUNDLE, mBadgeStateMap);
        }

        if (mFragmentManager != null
                && mFragmentContainer != 0
                && mItems != null
                && mItems instanceof BottomBarFragment[]) {
            BottomBarFragment bottomBarFragment = (BottomBarFragment) mItems[mCurrentTabPosition];

            if (bottomBarFragment.getFragment() != null) {
                bottomBarFragment.getFragment().onSaveInstanceState(outState);
            } else if (bottomBarFragment.getSupportFragment() != null) {
                bottomBarFragment.getSupportFragment().onSaveInstanceState(outState);
            }
        }
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
        } else if (tabPosition > mItems.length - 1 || tabPosition < 0) {
            throw new IndexOutOfBoundsException("Cant map color for Tab " +
                    "index " + tabPosition + ". You have no BottomBar Tabs at that position.");
        }

        if (mIsDarkTheme || !mIsShiftingMode || mIsTabletMode) return;

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
     * Deprecated. Use {@link #useDarkTheme()} instead.
     */
    @Deprecated
    public void useDarkTheme(boolean darkThemeEnabled) {
        mIsDarkTheme = darkThemeEnabled;
        useDarkTheme();
    }

    /**
     * Use dark theme instead of the light one.
     * <p/>
     * NOTE: You might want to change your active tab color to something else
     * using {@link #setActiveTabColor(int)}, as the default primary color might
     * not have enough contrast for the dark background.
     */
    public void useDarkTheme() {
        if (!mIsDarkTheme && mItems != null && mItems.length > 0) {
            darkThemeMagic();

            for (int i = 0; i < mItemContainer.getChildCount(); i++) {
                View bottomBarTab = mItemContainer.getChildAt(i);
                ((AppCompatImageView) bottomBarTab.findViewById(R.id.bb_bottom_bar_icon))
                        .setColorFilter(mWhiteColor);

                if (i == mCurrentTabPosition) {
                    selectTab(bottomBarTab, false);
                } else {
                    unselectTab(bottomBarTab, false);
                }
            }
        }

        mIsDarkTheme = true;
    }

    /**
     * Ignore the automatic Night Mode detection and use a light theme by default,
     * even if the Night Mode is on.
     */
    public void ignoreNightMode() {
        if (mItems != null && mItems.length > 0) {
            throw new UnsupportedOperationException("This BottomBar " +
                    "already has items! You must call ignoreNightMode() " +
                    "before setting any items.");
        }

        mIgnoreNightMode = true;
    }

    /**
     * Set a custom color for an active tab when there's three
     * or less items.
     * <p/>
     * NOTE: This value is ignored on mobile devices if you have more than
     * three items.
     *
     * @param activeTabColor a hex color used for active tabs, such as "#00FF000".
     */
    public void setActiveTabColor(String activeTabColor) {
        setActiveTabColor(Color.parseColor(activeTabColor));
    }

    /**
     * Set a custom color for an active tab when there's three
     * or less items.
     * <p/>
     * NOTE: This value is ignored if you have more than three items.
     *
     * @param activeTabColor a hex color used for active tabs, such as 0xFF00FF00.
     */
    public void setActiveTabColor(int activeTabColor) {
        mCustomActiveTabColor = activeTabColor;

        if (mItems != null && mItems.length > 0) {
            selectTabAtPosition(mCurrentTabPosition, false);
        }
    }

    /**
     * Set a custom color for inactive icons in fixed mode.
     * <p/>
     * NOTE: This value is ignored if not in fixed mode.
     *
     * @param iconColor a hex color used for icons, such as 0xFF00FF00.
     */
    public void setFixedInactiveIconColor(int iconColor) {
        mInActiveColor = iconColor;

        if (mItems != null && mItems.length > 0) {
            throw new UnsupportedOperationException("This BottomBar " +
                    "already has items! You must call setFixedInactiveIconColor() " +
                    "before setting any items.");
        }
    }

    /**
     * Set a custom color for icons in shifting mode.
     * <p/>
     * NOTE: This value is ignored in fixed mode.
     *
     * @param iconColor a hex color used for icons, such as 0xFF00FF00.
     */
    public void setShiftingIconColor(int iconColor) {
        mWhiteColor = iconColor;

        if (mItems != null && mItems.length > 0) {
            throw new UnsupportedOperationException("This BottomBar " +
                    "already has items! You must call setShiftingIconColor() " +
                    "before setting any items.");
        }
    }

    /**
     * Creates a new Badge (for example, an indicator for unread messages) for a Tab at
     * the specified position.
     *
     * @param tabPosition     zero-based index for the tab.
     * @param backgroundColor a color for this badge, such as "#FF0000".
     * @param initialCount    text displayed initially for this Badge.
     * @return a {@link BottomBarBadge} object.
     */
    public BottomBarBadge makeBadgeForTabAt(int tabPosition, String backgroundColor, int initialCount) {
        return makeBadgeForTabAt(tabPosition, Color.parseColor(backgroundColor), initialCount);
    }

    /**
     * Creates a new Badge (for example, an indicator for unread messages) for a Tab at
     * the specified position.
     *
     * @param tabPosition     zero-based index for the tab.
     * @param backgroundColor a color for this badge, such as 0xFFFF0000.
     * @param initialCount    text displayed initially for this Badge.
     * @return a {@link BottomBarBadge} object.
     */
    public BottomBarBadge makeBadgeForTabAt(int tabPosition, int backgroundColor, int initialCount) {
        if (mItems == null || mItems.length == 0) {
            throw new UnsupportedOperationException("You have no BottomBar Tabs set yet. " +
                    "Please set them first before calling the makeBadgeForTabAt() method.");
        } else if (tabPosition > mItems.length - 1 || tabPosition < 0) {
            throw new IndexOutOfBoundsException("Cant make a Badge for Tab " +
                    "index " + tabPosition + ". You have no BottomBar Tabs at that position.");
        }

        final View tab = mItemContainer.getChildAt(tabPosition);

        BottomBarBadge badge = new BottomBarBadge(mContext, tabPosition,
                tab, backgroundColor);
        badge.setTag(TAG_BADGE + tabPosition);
        badge.setCount(initialCount);

        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick((View) tab.getParent());
            }
        });

        tab.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return handleLongClick((View) tab.getParent());
            }
        });

        if (mBadgeMap == null) {
            mBadgeMap = new HashMap<>();
        }

        mBadgeMap.put(tabPosition, badge.getTag());

        boolean canShow = true;

        if (mIsComingFromRestoredState && mBadgeStateMap != null
                && mBadgeStateMap.containsKey(tabPosition)) {
            canShow = mBadgeStateMap.get(tabPosition);
        }

        if (canShow && mCurrentTabPosition != tabPosition
                && initialCount != 0) {
            badge.show();
        } else {
            badge.hide();
        }

        return badge;
    }

    /**
     * Set a custom TypeFace for the tab titles.
     * The .ttf file should be located at "/src/main/assets".
     *
     * @param typeFacePath path for the custom typeface in the assets directory.
     */
    public void setTypeFace(String typeFacePath) {
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),
                typeFacePath);

        if (mItemContainer != null && mItemContainer.getChildCount() > 0) {
            for (int i = 0; i < mItemContainer.getChildCount(); i++) {
                View bottomBarTab = mItemContainer.getChildAt(i);
                TextView title = (TextView) bottomBarTab.findViewById(R.id.bb_bottom_bar_title);
                title.setTypeface(typeface);
            }
        } else {
            mPendingTypeface = typeface;
        }
    }

    /**
     * Set a custom text appearance for the tab title.
     *
     * @param resId path to the custom text appearance.
     */
    public void setTextAppearance(@StyleRes int resId) {
        if (mItemContainer != null && mItemContainer.getChildCount() > 0) {
            for (int i = 0; i < mItemContainer.getChildCount(); i++) {
                View bottomBarTab = mItemContainer.getChildAt(i);
                TextView title = (TextView) bottomBarTab.findViewById(R.id.bb_bottom_bar_title);
                MiscUtils.setTextAppearance(title, resId);
            }
        } else {
            mPendingTextAppearance = resId;
        }
    }

    /**
     * Hide the shadow that's normally above the BottomBar.
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
     * Force the BottomBar to behave exactly same on tablets and phones,
     * instead of showing a left menu on tablets.
     */
    public void noTabletGoodness() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call noTabletGoodness() before setting the items, preferably " +
                    "right after attaching it to your layout.");
        }

        mIgnoreTabletLayout = true;
    }

    /**
     * Don't resize the tabs when selecting a new one, so every tab is the same if you have more than three
     * tabs. The text still displays the scale animation and the icon moves up, but the badass width animation
     * is ignored.
     */
    public void noResizeGoodness() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call noResizeGoodness() before setting the items, preferably " +
                    "right after attaching it to your layout.");
        }

        mIgnoreShiftingResize = true;
    }

    /**
     * Don't animate the scaling of the text when selecting a new tab. The text still displays the badass width animation,
     * but the scale animation is ignored.
     */
    public void noScalingGoodness() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call noScalingGoodness() before setting the items, preferably " +
                    "right after attaching it to your layout.");
        }

        mIgnoreScalingResize = true;
    }

    /**
     * Get this BottomBar's height (or width), depending if the BottomBar
     * is on the bottom (phones) or the left (tablets) of the screen.
     *
     * @param listener {@link OnSizeDeterminedListener} to get the size when it's ready.
     */
    public void getBarSize(final OnSizeDeterminedListener listener) {
        final int sizeCandidate = mIsTabletMode ?
                mOuterContainer.getWidth() : mOuterContainer.getHeight();

        if (sizeCandidate == 0) {
            mOuterContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void onGlobalLayout() {
                            listener.onSizeReady(mIsTabletMode ?
                                    mOuterContainer.getWidth() : mOuterContainer.getHeight());
                            ViewTreeObserver obs = mOuterContainer.getViewTreeObserver();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                obs.removeOnGlobalLayoutListener(this);
                            } else {
                                obs.removeGlobalOnLayoutListener(this);
                            }
                        }
                    });
            return;
        }

        listener.onSizeReady(sizeCandidate);
    }

    /**
     * Get the actual BottomBar that has the tabs inside it for whatever what you may want
     * to do with it.
     *
     * @return the BottomBar.
     */
    public View getBar() {
        return mOuterContainer;
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

    public BottomBar(Context context, int backgroundColor, int activeColor, float alpha) {
        super(context);
        mTabAlpha = alpha;
        mWhiteColor = activeColor;
        mPrimaryColor = backgroundColor;
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

        mDarkBackgroundColor = ContextCompat.getColor(getContext(), R.color.bb_darkBackgroundColor);


        if (mWhiteColor == null) {
            mWhiteColor = ContextCompat.getColor(getContext(), R.color.white);
            mPrimaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);
            mInActiveColor = ContextCompat.getColor(getContext(), R.color.bb_inActiveBottomBarItemColor);
        }

        //mWhiteColor = ContextCompat.getColor(getContext(), R.color.white);
        //mPrimaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);
        //mInActiveColor = ContextCompat.getColor(getContext(), R.color.bb_inActiveBottomBarItemColor);


        //mWhiteColor = Color.parseColor("#000000");
        //mPrimaryColor = Color.parseColor("#555555");
        //mInActiveColor = Color.parseColor("#ffffff");

        mScreenWidth = MiscUtils.getScreenWidth(mContext);
        mTenDp = MiscUtils.dpToPixel(mContext, 10);
        mSixteenDp = MiscUtils.dpToPixel(mContext, 16);
        mSixDp = MiscUtils.dpToPixel(mContext, 6);
        mEightDp = MiscUtils.dpToPixel(mContext, 8);
        mMaxFixedItemWidth = MiscUtils.dpToPixel(mContext, 168);
        mMaxInActiveShiftingItemWidth = MiscUtils.dpToPixel(mContext, 96);
    }

    private void initializeViews() {
        mIsTabletMode = !mIgnoreTabletLayout &&
                mContext.getResources().getBoolean(R.bool.bb_bottom_bar_is_tablet_mode);
        ViewCompat.setElevation(this, MiscUtils.dpToPixel(mContext, 8));
        View rootView = inflate(mContext, mIsTabletMode ?
                        R.layout.bb_bottom_bar_item_container_tablet : R.layout.bb_bottom_bar_item_container,
                this);
        mTabletRightBorder = rootView.findViewById(R.id.bb_tablet_right_border);

        mUserContentContainer = (ViewGroup) rootView.findViewById(R.id.bb_user_content_container);

        mShadowView = rootView.findViewById(R.id.bb_bottom_bar_shadow);

        mOuterContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_outer_container);
        mItemContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_item_container);

        mBackgroundView = rootView.findViewById(R.id.bb_bottom_bar_background_view);
        mBackgroundOverlay = rootView.findViewById(R.id.bb_bottom_bar_background_overlay);

        if (mIsShy && mIgnoreTabletLayout) {
            mPendingUserContentView = null;
        }

        if (mPendingUserContentView != null) {
            ViewGroup.LayoutParams params = mPendingUserContentView.getLayoutParams();

            if (params == null) {
                params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }

            if (mIsTabletMode && mIsShy) {
                ((ViewGroup) mPendingUserContentView.getParent()).removeView(mPendingUserContentView);
            }

            mUserContentContainer.addView(mPendingUserContentView, 0, params);
            mPendingUserContentView = null;
        }

        if (mIsShy && !mIsTabletMode) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (!mShyHeightAlreadyCalculated) {
                        ((CoordinatorLayout.LayoutParams) getLayoutParams())
                                .setBehavior(new BottomNavigationBehavior(getOuterContainer().getHeight(), 0, isShy(), mIsTabletMode));
                    }

                    ViewTreeObserver obs = getViewTreeObserver();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        obs.removeOnGlobalLayoutListener(this);
                    } else {
                        obs.removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }

    /**
     * Makes this BottomBar "shy". In other words, it hides on scroll.
     */
    private void toughChildHood(boolean useExtraOffset) {
        mIsShy = true;
        mUseExtraOffset = useExtraOffset;
    }

    protected boolean isShy() {
        return mIsShy;
    }

    protected void shyHeightAlreadyCalculated() {
        mShyHeightAlreadyCalculated = true;
    }

    protected boolean useExtraOffset() {
        return mUseExtraOffset;
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

    protected void setBarVisibility(int visibility) {
        if (mIsShy) {
            toggleShyVisibility(visibility == VISIBLE);
            return;
        }

        if (mOuterContainer != null) {
            mOuterContainer.setVisibility(visibility);
        }

        if (mBackgroundView != null) {
            mBackgroundView.setVisibility(visibility);
        }

        if (mBackgroundOverlay != null) {
            mBackgroundOverlay.setVisibility(visibility);
        }
    }

    /**
     * Toggle translation of BottomBar to hidden and visible in a CoordinatorLayout.
     *
     * @param visible true resets translation to 0, false translates view to hidden
     */
    protected void toggleShyVisibility(boolean visible) {
        BottomNavigationBehavior<BottomBar> from = BottomNavigationBehavior.from(this);
        if (from != null) {
            from.setHidden(this, visible);
        }
    }

    @Override
    public void onClick(View v) {
        handleClick(v);
    }

    private void handleClick(View v) {
        if (v.getTag().equals(TAG_BOTTOM_BAR_VIEW_INACTIVE)) {
            View oldTab = findViewWithTag(TAG_BOTTOM_BAR_VIEW_ACTIVE);

            unselectTab(oldTab, !mIgnoreScalingResize);
            selectTab(v, !mIgnoreScalingResize);

            shiftingMagic(oldTab, v, true);
        }
        updateSelectedTab(findItemPosition(v));
    }

    private void shiftingMagic(View oldTab, View newTab, boolean animate) {
        if (!mIsTabletMode && mIsShiftingMode && !mIgnoreShiftingResize) {
            if (oldTab instanceof FrameLayout) {
                // It's a badge, goddammit!
                oldTab = ((FrameLayout) oldTab).getChildAt(0);
            }

            if (newTab instanceof FrameLayout) {
                // It's a badge, goddammit!
                newTab = ((FrameLayout) newTab).getChildAt(0);
            }

            if (animate) {
                MiscUtils.resizeTab(oldTab, oldTab.getWidth(), mInActiveShiftingItemWidth);
                MiscUtils.resizeTab(newTab, newTab.getWidth(), mActiveShiftingItemWidth);
            } else {
                oldTab.getLayoutParams().width = mInActiveShiftingItemWidth;
                newTab.getLayoutParams().width = mActiveShiftingItemWidth;
            }
        }
    }

    private void updateSelectedTab(int newPosition) {
        final boolean notifyMenuListener = mMenuListener != null && mItems instanceof BottomBarTab[];
        final boolean notifyRegularListener = mListener != null;

        if (newPosition != mCurrentTabPosition) {
            handleBadgeVisibility(mCurrentTabPosition, newPosition);
            mCurrentTabPosition = newPosition;

            if (notifyRegularListener) {
                notifyRegularListener(mListener, false, mCurrentTabPosition);
            }

            if (notifyMenuListener) {
                notifyMenuListener(mMenuListener, false, ((BottomBarTab) mItems[mCurrentTabPosition]).id);
            }

            updateCurrentFragment();
        } else {
            if (notifyRegularListener) {
                notifyRegularListener(mListener, true, mCurrentTabPosition);
            }

            if (notifyMenuListener) {
                notifyMenuListener(mMenuListener, true, ((BottomBarTab) mItems[mCurrentTabPosition]).id);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void notifyRegularListener(Object listener, boolean isReselection, int position) {
        if (listener instanceof OnTabClickListener) {
            OnTabClickListener onTabClickListener = (OnTabClickListener) listener;

            if (!isReselection) {
                onTabClickListener.onTabSelected(position);
            } else {
                onTabClickListener.onTabReSelected(position);
            }
        } else if (listener instanceof OnTabSelectedListener) {
            OnTabSelectedListener onTabSelectedListener = (OnTabSelectedListener) listener;

            if (!isReselection) {
                onTabSelectedListener.onItemSelected(position);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void notifyMenuListener(Object listener, boolean isReselection, @IdRes int menuItemId) {
        if (listener instanceof OnMenuTabClickListener) {
            OnMenuTabClickListener onMenuTabClickListener = (OnMenuTabClickListener) listener;

            if (!isReselection) {
                onMenuTabClickListener.onMenuTabSelected(menuItemId);
            } else {
                onMenuTabClickListener.onMenuTabReSelected(menuItemId);
            }
        } else if (listener instanceof OnMenuTabSelectedListener) {
            OnMenuTabSelectedListener onMenuTabSelectedListener = (OnMenuTabSelectedListener) listener;

            if (!isReselection) {
                onMenuTabSelectedListener.onMenuItemSelected(menuItemId);
            }
        }
    }

    private void handleBadgeVisibility(int oldPosition, int newPosition) {
        if (mBadgeMap == null) {
            return;
        }

        if (mBadgeMap.containsKey(oldPosition)) {
            BottomBarBadge oldBadge = (BottomBarBadge) mOuterContainer
                    .findViewWithTag(mBadgeMap.get(oldPosition));

            if (oldBadge.getAutoShowAfterUnSelection()) {
                oldBadge.show();
            } else {
                oldBadge.hide();
            }
        }

        if (mBadgeMap.containsKey(newPosition)) {
            BottomBarBadge newBadge = (BottomBarBadge) mOuterContainer
                    .findViewWithTag(mBadgeMap.get(newPosition));

            if (newBadge.getAutoHideOnSelection()) {
                newBadge.hide();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return handleLongClick(v);
    }

    private boolean handleLongClick(View v) {
        if ((mIsShiftingMode || mIsTabletMode) && v.getTag().equals(TAG_BOTTOM_BAR_VIEW_INACTIVE)) {
            Toast.makeText(mContext, mItems[findItemPosition(v)].getTitle(mContext), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void updateItems(final BottomBarItemBase[] bottomBarItems) {
        if (mItemContainer == null) {
            initializeViews();
        }

        int index = 0;
        int biggestWidth = 0;

        mIsShiftingMode = mMaxFixedTabCount >= 0 && mMaxFixedTabCount < bottomBarItems.length;

        if (!mIsDarkTheme && !mIgnoreNightMode
                && MiscUtils.isNightMode(mContext)) {
            mIsDarkTheme = true;
        }

        if (mIsDarkTheme) {
            darkThemeMagic();
        } else if (!mIsTabletMode && mIsShiftingMode) {
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
                layoutResource = mIsTabletMode ?
                        R.layout.bb_bottom_bar_item_fixed_tablet : R.layout.bb_bottom_bar_item_fixed;
            }

            View bottomBarTab = View.inflate(mContext, layoutResource, null);
            AppCompatImageView icon = (AppCompatImageView) bottomBarTab.findViewById(R.id.bb_bottom_bar_icon);

            icon.setImageDrawable(bottomBarItemBase.getIcon(mContext));

            if (!mIsTabletMode) {
                TextView title = (TextView) bottomBarTab.findViewById(R.id.bb_bottom_bar_title);
                title.setText(bottomBarItemBase.getTitle(mContext));

                if (mPendingTextAppearance != -1) {
                    MiscUtils.setTextAppearance(title, mPendingTextAppearance);
                }

                if (mPendingTypeface != null) {
                    title.setTypeface(mPendingTypeface);
                }
            }

            if (mIsDarkTheme || (!mIsTabletMode && mIsShiftingMode)) {
                icon.setColorFilter(mWhiteColor);
            }

            if (bottomBarItemBase instanceof BottomBarTab) {
                bottomBarTab.setId(((BottomBarTab) bottomBarItemBase).id);
            }

            if (index == mCurrentTabPosition) {
                selectTab(bottomBarTab, false);
            } else {
                unselectTab(bottomBarTab, false);
            }

            if (!mIsTabletMode) {
                if (bottomBarTab.getWidth() > biggestWidth) {
                    biggestWidth = bottomBarTab.getWidth();
                }

                viewsToAdd[index] = bottomBarTab;
            } else {
                mItemContainer.addView(bottomBarTab);
            }

            bottomBarTab.setOnClickListener(this);
            bottomBarTab.setOnLongClickListener(this);
            index++;
        }

        if (!mIsTabletMode) {
            int proposedItemWidth = Math.min(
                    MiscUtils.dpToPixel(mContext, mScreenWidth / bottomBarItems.length),
                    mMaxFixedItemWidth
            );

            mInActiveShiftingItemWidth = (int) (proposedItemWidth * 0.9);
            mActiveShiftingItemWidth = (int) (proposedItemWidth + (proposedItemWidth * (bottomBarItems.length * 0.1)));

            int height = Math.round(mContext.getResources().getDimension(R.dimen.bb_height));
            for (View bottomBarView : viewsToAdd) {
                LinearLayout.LayoutParams params;

                if (mIsShiftingMode && !mIgnoreShiftingResize) {
                    if (TAG_BOTTOM_BAR_VIEW_ACTIVE.equals(bottomBarView.getTag())) {
                        params = new LinearLayout.LayoutParams(mActiveShiftingItemWidth, height);
                    } else {
                        params = new LinearLayout.LayoutParams(mInActiveShiftingItemWidth, height);
                    }
                } else {
                    params = new LinearLayout.LayoutParams(proposedItemWidth, height);
                }

                bottomBarView.setLayoutParams(params);
                mItemContainer.addView(bottomBarView);
            }
        }

        if (mPendingTextAppearance != -1) {
            mPendingTextAppearance = -1;
        }

        if (mPendingTypeface != null) {
            mPendingTypeface = null;
        }
    }

    private void darkThemeMagic() {
        if (!mIsTabletMode) {
            mBackgroundView.setBackgroundColor(mDarkBackgroundColor);
        } else {
            mItemContainer.setBackgroundColor(mDarkBackgroundColor);
            mTabletRightBorder.setBackgroundColor(ContextCompat.getColor(mContext, R.color.bb_tabletRightBorderDark));
        }
    }

    private void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentTabPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_TAB, -1);
            mBadgeStateMap = (HashMap<Integer, Boolean>) savedInstanceState
                    .getSerializable(STATE_BADGE_STATES_BUNDLE);

            if (mCurrentTabPosition == -1) {
                mCurrentTabPosition = 0;
                Log.e("BottomBar", "You must override the Activity's onSave" +
                        "InstanceState(Bundle outState) and call BottomBar.onSaveInstanc" +
                        "eState(outState) there to restore the state properly.");
            }

            mIsComingFromRestoredState = true;
            mShouldUpdateFragmentInitially = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateTitleBottomPadding();
        }
    }

    /**
     * Material Design specify that there should be a 10dp padding under the text, it seems that
     * it means 10dp starting from the text baseline.
     * This method takes care of calculating the amount of padding that needs to be added to the
     * Title TextView in order to comply with the Material Design specifications.
     */
    private void updateTitleBottomPadding() {
        if (mItemContainer == null) {
            return;
        }

        int childCount = mItemContainer.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View tab = mItemContainer.getChildAt(i);
            TextView title = (TextView) tab.findViewById(R.id.bb_bottom_bar_title);
            if (title == null) {
                continue;
            }
            int baseline = title.getBaseline();
            // Height already includes any possible top/bottom padding
            int height = title.getHeight();
            int paddingInsideTitle = height - baseline;
            int missingPadding = mTenDp - paddingInsideTitle;
            if (missingPadding > 0) {
                // Only update the padding if really needed
                title.setPadding(title.getPaddingLeft(), title.getPaddingTop(),
                    title.getPaddingRight(), missingPadding + title.getPaddingBottom());
            }
        }
    }

    private void selectTab(View tab, boolean animate) {
        tab.setTag(TAG_BOTTOM_BAR_VIEW_ACTIVE);
        AppCompatImageView icon = (AppCompatImageView) tab.findViewById(R.id.bb_bottom_bar_icon);
        TextView title = (TextView) tab.findViewById(R.id.bb_bottom_bar_title);

        int tabPosition = findItemPosition(tab);

        if (!mIsShiftingMode || mIsTabletMode) {
            int activeColor = mCustomActiveTabColor != 0 ?
                    mCustomActiveTabColor : mPrimaryColor;
            icon.setColorFilter(activeColor);

            if (title != null) {
                title.setTextColor(activeColor);
            }
        } else {
            title.setTextColor(mWhiteColor);
        }

        if (mIsDarkTheme) {
            if (title != null) {
                ViewCompat.setAlpha(title, 1.0f);
            }

            ViewCompat.setAlpha(icon, 1.0f);
        }

        if (title == null) {
            return;
        }

        if (animate) {
            ViewPropertyAnimatorCompat titleAnimator = ViewCompat.animate(title)
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(1)
                    .scaleY(1);

            if (mIsShiftingMode) {
                titleAnimator.alpha(1.0f);
            }

            titleAnimator.start();

            // We only want to animate the icon to avoid moving the title
            // Shifting or fixed the padding above icon is always 6dp
            MiscUtils.resizePaddingTop(icon, icon.getPaddingTop(), mSixDp, ANIMATION_DURATION);

            if (mIsShiftingMode) {
                ViewCompat.animate(icon)
                        .setDuration(ANIMATION_DURATION)
                        .alpha(1.0f)
                        .start();
            }

            handleBackgroundColorChange(tabPosition, tab);
        } else {
            ViewCompat.setScaleX(title, 1);
            ViewCompat.setScaleY(title, 1);
            icon.setPadding(icon.getPaddingLeft(), mSixDp, icon.getPaddingRight(),
                icon.getPaddingBottom());

            if (mIsShiftingMode) {
                ViewCompat.setAlpha(icon, 1.0f);
                ViewCompat.setAlpha(title, 1.0f);
            }
        }
    }

    private void unselectTab(View tab, boolean animate) {
        tab.setTag(TAG_BOTTOM_BAR_VIEW_INACTIVE);

        AppCompatImageView icon = (AppCompatImageView) tab.findViewById(R.id.bb_bottom_bar_icon);
        TextView title = (TextView) tab.findViewById(R.id.bb_bottom_bar_title);

        if (!mIsShiftingMode || mIsTabletMode) {
            int inActiveColor = mIsDarkTheme ? mWhiteColor : mInActiveColor;
            icon.setColorFilter(inActiveColor);

            if (title != null) {
                title.setTextColor(inActiveColor);
            }
        }

        if (mIsDarkTheme) {
            if (title != null) {
                ViewCompat.setAlpha(title, mTabAlpha);
            }

            ViewCompat.setAlpha(icon, mTabAlpha);
        }

        if (title == null) {
            return;
        }

        float scale = mIsShiftingMode ? 0 : 0.86f;
        int iconPaddingTop = mIsShiftingMode ? mSixteenDp : mEightDp;

        if (animate) {
            ViewPropertyAnimatorCompat titleAnimator = ViewCompat.animate(title)
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(scale)
                    .scaleY(scale);

            if (mIsShiftingMode) {
                titleAnimator.alpha(0);
            }

            titleAnimator.start();

            MiscUtils.resizePaddingTop(icon, icon.getPaddingTop(), iconPaddingTop, ANIMATION_DURATION);

            if (mIsShiftingMode) {
                ViewCompat.animate(icon)
                        .setDuration(ANIMATION_DURATION)
                        .alpha(mTabAlpha)
                        .start();
            }
        } else {
            ViewCompat.setScaleX(title, scale);
            ViewCompat.setScaleY(title, scale);
            icon.setPadding(icon.getPaddingLeft(), iconPaddingTop, icon.getPaddingRight(),
                icon.getPaddingBottom());

            if (mIsShiftingMode) {
                ViewCompat.setAlpha(icon, mTabAlpha);
                ViewCompat.setAlpha(title, 0);
            }
        }
    }

    private void handleBackgroundColorChange(int tabPosition, View tab) {
        if (mIsDarkTheme || !mIsShiftingMode || mIsTabletMode) return;

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
        if (!mShouldUpdateFragmentInitially && mFragmentManager != null
                && mFragmentContainer != 0
                && mItems != null
                && mItems instanceof BottomBarFragment[]) {
            BottomBarFragment newFragment = ((BottomBarFragment) mItems[mCurrentTabPosition]);

            if (mFragmentManager instanceof android.support.v4.app.FragmentManager
                    && newFragment.getSupportFragment() != null) {
                ((android.support.v4.app.FragmentManager) mFragmentManager).beginTransaction()
                        .replace(mFragmentContainer, newFragment.getSupportFragment())
                        .commit();
            } else if (mFragmentManager instanceof android.app.FragmentManager
                    && newFragment.getFragment() != null) {
                ((android.app.FragmentManager) mFragmentManager).beginTransaction()
                        .replace(mFragmentContainer, newFragment.getFragment())
                        .commit();
            }
        }

        mShouldUpdateFragmentInitially = false;
    }

    private void clearItems() {
        if (mItemContainer != null) {
            int childCount = mItemContainer.getChildCount();

            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    mItemContainer.removeView(mItemContainer.getChildAt(0));
                }
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

    private static void navBarMagic(Activity activity, final BottomBar bottomBar) {
        Resources res = activity.getResources();

        int softMenuIdentifier = res
                .getIdentifier("config_showNavigationBar", "bool", "android");
        int navBarIdentifier = res.getIdentifier("navigation_bar_height",
                "dimen", "android");
        int navBarHeight = 0;

        if (navBarIdentifier > 0) {
            navBarHeight = res.getDimensionPixelSize(navBarIdentifier);
        }

        if (!bottomBar.drawBehindNavBar()
                || navBarHeight == 0) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && ViewConfiguration.get(activity).hasPermanentMenuKey()) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                (!(softMenuIdentifier > 0 && res.getBoolean(softMenuIdentifier)))) {
            return;
        }

        /**
         * Copy-paste coding made possible by:
         * http://stackoverflow.com/a/14871974/940036
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display d = activity.getWindowManager().getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            boolean hasSoftwareKeys = (realWidth - displayWidth) > 0
                    || (realHeight - displayHeight) > 0;

            if (!hasSoftwareKeys) {
                return;
            }
        }
        /**
         * End of delicious copy-paste code
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.getWindow().getAttributes().flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;

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
            final int navBarHeightCopy = navBarHeight;
            bottomBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    bottomBar.shyHeightAlreadyCalculated();

                    int newHeight = outerContainer.getHeight() + navBarHeightCopy;
                    outerContainer.getLayoutParams().height = newHeight;

                    if (bottomBar.isShy()) {
                        int defaultOffset = bottomBar.useExtraOffset() ? navBarHeightCopy : 0;
                        bottomBar.setTranslationY(defaultOffset);
                        ((CoordinatorLayout.LayoutParams) bottomBar.getLayoutParams())
                                .setBehavior(new BottomNavigationBehavior(newHeight, defaultOffset, bottomBar.isShy(), bottomBar.mIsTabletMode));
                    }

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
