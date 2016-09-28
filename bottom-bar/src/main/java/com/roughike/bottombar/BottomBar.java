package com.roughike.bottombar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.XmlRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

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
public class BottomBar extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {
    private static final String STATE_CURRENT_SELECTED_TAB = "STATE_CURRENT_SELECTED_TAB";

    private static final float DEFAULT_INACTIVE_SHIFTING_TAB_ALPHA = 0.6f;

    // Behaviors
    private static final int BEHAVIOR_NONE = 0;
    private static final int BEHAVIOR_SHIFTING = 1;
    private static final int BEHAVIOR_SHY = 2;
    private static final int BEHAVIOR_DRAW_UNDER_NAV = 4;

    private int primaryColor;
    private int screenWidth;
    private int tenDp;
    private int maxFixedItemWidth;

    // XML Attributes
    private int tabXmlResource;
    private boolean isTabletMode;
    private int behaviors;
    private float inActiveTabAlpha;
    private float activeTabAlpha;
    private int inActiveTabColor;
    private int activeTabColor;
    private int badgeBackgroundColor;
    private int titleTextAppearance;
    private Typeface titleTypeFace;
    private boolean showShadow;

    private View backgroundOverlay;
    private ViewGroup outerContainer;
    private ViewGroup tabContainer;
    private View shadowView;

    private int defaultBackgroundColor = Color.WHITE;
    private int currentBackgroundColor;
    private int currentTabPosition;

    private int inActiveShiftingItemWidth;
    private int activeShiftingItemWidth;

    private OnTabSelectListener onTabSelectListener;
    private OnTabReselectListener onTabReselectListener;

    private boolean isComingFromRestoredState;
    private boolean ignoreTabReselectionListener;

    private boolean shyHeightAlreadyCalculated;
    private boolean navBarAccountedHeightCalculated;

    public BottomBar(Context context) {
        super(context);
        init(context, null);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        setItems(tabXmlResource);
    }

    private void init(Context context, AttributeSet attrs) {
        populateAttributes(context, attrs);
        initializeViews();
        determineInitialBackgroundColor();
    }

    private void populateAttributes(Context context, AttributeSet attrs) {
        primaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);
        screenWidth = MiscUtils.getScreenWidth(getContext());
        tenDp = MiscUtils.dpToPixel(getContext(), 10);
        maxFixedItemWidth = MiscUtils.dpToPixel(getContext(), 168);

        TypedArray ta = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.BottomBar, 0, 0);

        try {
            tabXmlResource = ta.getResourceId(R.styleable.BottomBar_bb_tabXmlResource, 0);
            isTabletMode = ta.getBoolean(R.styleable.BottomBar_bb_tabletMode, false);
            behaviors = ta.getInteger(R.styleable.BottomBar_bb_behavior, BEHAVIOR_NONE);
            inActiveTabAlpha = ta.getFloat(R.styleable.BottomBar_bb_inActiveTabAlpha,
                    isShiftingMode() ? DEFAULT_INACTIVE_SHIFTING_TAB_ALPHA : 1);
            activeTabAlpha = ta.getFloat(R.styleable.BottomBar_bb_activeTabAlpha, 1);

            @ColorInt
            int defaultInActiveColor = isShiftingMode() ?
                    Color.WHITE : ContextCompat.getColor(context, R.color.bb_inActiveBottomBarItemColor);
            int defaultActiveColor = isShiftingMode() ? Color.WHITE : primaryColor;

            inActiveTabColor = ta.getColor(R.styleable.BottomBar_bb_inActiveTabColor, defaultInActiveColor);
            activeTabColor = ta.getColor(R.styleable.BottomBar_bb_activeTabColor, defaultActiveColor);
            badgeBackgroundColor = ta.getColor(R.styleable.BottomBar_bb_badgeBackgroundColor, Color.RED);
            titleTextAppearance = ta.getResourceId(R.styleable.BottomBar_bb_titleTextAppearance, 0);
            titleTypeFace = getTypeFaceFromAsset(ta.getString(R.styleable.BottomBar_bb_titleTypeFace));
            showShadow = ta.getBoolean(R.styleable.BottomBar_bb_showShadow, true);
        } finally {
            ta.recycle();
        }
    }

    private boolean isShiftingMode() {
        return !isTabletMode && hasBehavior(BEHAVIOR_SHIFTING);
    }

    private boolean drawUnderNav() {
        return !isTabletMode
                && hasBehavior(BEHAVIOR_DRAW_UNDER_NAV)
                && NavbarUtils.shouldDrawBehindNavbar(getContext());
    }

    private boolean isShy() {
        return !isTabletMode && hasBehavior(BEHAVIOR_SHY);
    }

    private boolean hasBehavior(int behavior) {
        return (behaviors | behavior) == behaviors;
    }

    private Typeface getTypeFaceFromAsset(String fontPath) {
        if (fontPath != null) {
            return Typeface.createFromAsset(
                    getContext().getAssets(), fontPath);
        }

        return null;
    }

    private void initializeViews() {
        int width = isTabletMode ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT;
        int height = isTabletMode ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
        LayoutParams params = new LayoutParams(width, height);

        setLayoutParams(params);
        setOrientation(isTabletMode ? HORIZONTAL : VERTICAL);
        ViewCompat.setElevation(this, MiscUtils.dpToPixel(getContext(), 8));

        View rootView = inflate(getContext(),
                isTabletMode ? R.layout.bb_bottom_bar_item_container_tablet : R.layout.bb_bottom_bar_item_container, this);
        rootView.setLayoutParams(params);

        backgroundOverlay = rootView.findViewById(R.id.bb_bottom_bar_background_overlay);
        outerContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_outer_container);
        tabContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_item_container);
        shadowView = rootView.findViewById(R.id.bb_bottom_bar_shadow);

        if (!showShadow) {
            shadowView.setVisibility(GONE);
        }
    }

    private void determineInitialBackgroundColor() {
        if (isShiftingMode()) {
            defaultBackgroundColor = primaryColor;
        }

        Drawable userDefinedBackground = getBackground();

        boolean userHasDefinedBackgroundColor = userDefinedBackground != null
                && userDefinedBackground instanceof ColorDrawable;

        if (userHasDefinedBackgroundColor) {
            defaultBackgroundColor = ((ColorDrawable) userDefinedBackground).getColor();
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * Set the items for the BottomBar from XML Resource.
     */
    public void setItems(@XmlRes int xmlRes) {
        setItems(xmlRes, null);
    }

    /**
     * Set the item for the BottomBar from XML Resource with a default configuration
     * for each tab.
     */
    public void setItems(@XmlRes int xmlRes, BottomBarTab.Config defaultTabConfig) {
        if (xmlRes == 0) {
            throw new RuntimeException("No items specified for the BottomBar!");
        }

        if (defaultTabConfig == null) {
            defaultTabConfig = getTabConfig();
        }

        TabParser parser = new TabParser(getContext(), defaultTabConfig, xmlRes);
        updateItems(parser.getTabs());
    }

    private BottomBarTab.Config getTabConfig() {
        return new BottomBarTab.Config.Builder()
                .inActiveTabAlpha(inActiveTabAlpha)
                .activeTabAlpha(activeTabAlpha)
                .inActiveTabColor(inActiveTabColor)
                .activeTabColor(activeTabColor)
                .barColorWhenSelected(defaultBackgroundColor)
                .badgeBackgroundColor(badgeBackgroundColor)
                .titleTextAppearance(titleTextAppearance)
                .titleTypeFace(titleTypeFace)
                .build();
    }

    private void updateItems(final List<BottomBarTab> bottomBarItems) {
        int index = 0;
        int biggestWidth = 0;

        BottomBarTab[] viewsToAdd = new BottomBarTab[bottomBarItems.size()];

        for (BottomBarTab bottomBarTab : bottomBarItems) {
            BottomBarTab.Type type;

            if (isShiftingMode()) {
                type = BottomBarTab.Type.SHIFTING;
            } else if (isTabletMode) {
                type = BottomBarTab.Type.TABLET;
            } else {
                type = BottomBarTab.Type.FIXED;
            }

            bottomBarTab.setType(type);
            bottomBarTab.prepareLayout();

            if (index == currentTabPosition) {
                bottomBarTab.select(false);

                handleBackgroundColorChange(bottomBarTab, false);
            } else {
                bottomBarTab.deselect(false);
            }

            if (!isTabletMode) {
                if (bottomBarTab.getWidth() > biggestWidth) {
                    biggestWidth = bottomBarTab.getWidth();
                }

                viewsToAdd[index] = bottomBarTab;
            } else {
                tabContainer.addView(bottomBarTab);
            }

            bottomBarTab.setOnClickListener(this);
            bottomBarTab.setOnLongClickListener(this);
            index++;
        }

        if (!isTabletMode) {
            resizeTabsToCorrectSizes(bottomBarItems, viewsToAdd);
        }
    }

    private void resizeTabsToCorrectSizes(List<BottomBarTab> bottomBarItems, BottomBarTab[] viewsToAdd) {
        int proposedItemWidth = Math.min(
                MiscUtils.dpToPixel(getContext(), screenWidth / bottomBarItems.size()),
                maxFixedItemWidth
        );

        inActiveShiftingItemWidth = (int) (proposedItemWidth * 0.9);
        activeShiftingItemWidth = (int) (proposedItemWidth + (proposedItemWidth * (bottomBarItems.size() * 0.1)));
        int height = Math.round(getContext().getResources().getDimension(R.dimen.bb_height));

        for (BottomBarTab bottomBarView : viewsToAdd) {
            LayoutParams params;

            if (isShiftingMode()) {
                if (bottomBarView.isActive()) {
                    params = new LayoutParams(activeShiftingItemWidth, height);
                } else {
                    params = new LayoutParams(inActiveShiftingItemWidth, height);
                }
            } else {
                params = new LayoutParams(proposedItemWidth, height);
            }

            bottomBarView.setLayoutParams(params);
            tabContainer.addView(bottomBarView);
        }
    }

    /**
     * Set a listener that gets fired when the selected tab changes.
     *
     * Note: Will be immediately called for the currently selected tab
     * once when set.
     *
     * @param listener a listener for monitoring changes in tab selection.
     */
    public void setOnTabSelectListener(@Nullable OnTabSelectListener listener) {
        onTabSelectListener = listener;

        if (onTabSelectListener != null && getTabCount() > 0) {
            listener.onTabSelected(getCurrentTabId());
        }
    }

    /**
     * Set a listener that gets fired when a currently selected tab is clicked.
     *
     * @param listener a listener for handling tab reselections.
     */
    public void setOnTabReselectListener(@Nullable OnTabReselectListener listener) {
        onTabReselectListener = listener;
    }

    /**
     * Set the default selected to be the tab with the corresponding tab id.
     * By default, the first tab in the container is the default tab.
     */
    public void setDefaultTab(@IdRes int defaultTabId) {
        int defaultTabPosition = findPositionForTabWithId(defaultTabId);
        setDefaultTabPosition(defaultTabPosition);
    }

    /**
     * Sets the default tab for this BottomBar that is shown until the user changes
     * the selection.
     *
     * @param defaultTabPosition the default tab position.
     */
    public void setDefaultTabPosition(int defaultTabPosition) {
        if (isComingFromRestoredState) return;

        selectTabAtPosition(defaultTabPosition);
    }

    /**
     * Select the tab with the corresponding id.
     */
    public void selectTabWithId(@IdRes int tabResId) {
        int tabPosition = findPositionForTabWithId(tabResId);
        selectTabAtPosition(tabPosition);
    }

    /**
     * Select a tab at the specified position.
     *
     * @param position the position to select.
     */
    public void selectTabAtPosition(int position) {
        if (position > getTabCount() - 1 || position < 0) {
            throw new IndexOutOfBoundsException("Can't select tab at position " +
                    position + ". This BottomBar has no items at that position.");
        }

        selectTabAtPosition(position, false);
    }

    public int getTabCount() {
        return tabContainer.getChildCount();
    }

    /**
     * Get the currently selected tab.
     */
    public BottomBarTab getCurrentTab() {
        return getTabAtPosition(getCurrentTabPosition());
    }

    /**
     * Get the tab at the specified position.
     */
    public BottomBarTab getTabAtPosition(int position) {
        View child = tabContainer.getChildAt(position);

        if (child instanceof FrameLayout) {
            return findTabInLayout((FrameLayout) child);
        }

        return (BottomBarTab) child;
    }

    /**
     * Get the resource id for the currently selected tab.
     */
    @IdRes
    public int getCurrentTabId() {
        return getCurrentTab().getId();
    }

    /**
     * Get the currently selected tab position.
     */
    public int getCurrentTabPosition() {
        return currentTabPosition;
    }

    /**
     * Find the tabs' position in the container by id.
     */
    public int findPositionForTabWithId(@IdRes int tabId) {
        return getTabWithId(tabId).getIndexInTabContainer();
    }

    /**
     * Find a BottomBarTab with the corresponding id.
     */
    public BottomBarTab getTabWithId(@IdRes int tabId) {
        return (BottomBarTab) tabContainer.findViewById(tabId);
    }

    /**
     * Set alpha value used for inactive BottomBarTabs.
     */
    public void setInActiveTabAlpha(float alpha) {
        inActiveTabAlpha = alpha;
        refreshTabs();
    }

    /**
     * Set alpha value used for active BottomBarTabs.
     */
    public void setActiveTabAlpha(float alpha) {
        activeTabAlpha = alpha;
        refreshTabs();
    }

    public void setInActiveTabColor(@ColorInt int color) {
        inActiveTabColor = color;
        refreshTabs();
    }

    /**
     * Set active color used for selected BottomBarTabs.
     */
    public void setActiveTabColor(@ColorInt int color) {
        activeTabColor = color;
        refreshTabs();
    }

    public void setBadgeBackgroundColor(@ColorInt int color) {
        badgeBackgroundColor = color;
        refreshTabs();
    }

    /**
     * Set custom text apperance for all BottomBarTabs.
     */
    public void setTabTitleTextAppearance(int textAppearance) {
        titleTextAppearance = textAppearance;
        refreshTabs();
    }

    /**
     * Set a custom typeface for all tab's titles.
     *
     * @param fontPath path for your custom font file, such as fonts/MySuperDuperFont.ttf.
     *                 In that case your font path would look like src/main/assets/fonts/MySuperDuperFont.ttf,
     *                 but you only need to provide fonts/MySuperDuperFont.ttf, as the asset folder
     *                 will be auto-filled for you.
     */
    public void setTabTitleTypeface(String fontPath) {
        Typeface actualTypeface = getTypeFaceFromAsset(fontPath);
        setTabTitleTypeface(actualTypeface);
    }

    /**
     * Set a custom typeface for all tab's titles.
     */
    public void setTabTitleTypeface(Typeface typeface) {
        titleTypeFace = typeface;
        refreshTabs();
    }

    private void refreshTabs() {
        int tabCount = getTabCount();

        if (tabCount > 0) {
            BottomBarTab.Config newConfig = getTabConfig();

            for (int i = 0; i < getTabCount(); i++) {
                BottomBarTab tab = getTabAtPosition(i);
                tab.setConfig(newConfig);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            updateTitleBottomPadding();

            if (isShy()) {
                initializeShyBehavior();
            }

            if (drawUnderNav()) {
                resizeForDrawingUnderNavbar();
            }
        }
    }

    private void updateTitleBottomPadding() {
        if (tabContainer == null) {
            return;
        }

        int childCount = getTabCount();

        for (int i = 0; i < childCount; i++) {
            View tab = tabContainer.getChildAt(i);
            TextView title = (TextView) tab.findViewById(R.id.bb_bottom_bar_title);

            if (title == null) {
                continue;
            }

            int baseline = title.getBaseline();
            int height = title.getHeight();
            int paddingInsideTitle = height - baseline;
            int missingPadding = tenDp - paddingInsideTitle;

            if (missingPadding > 0) {
                title.setPadding(title.getPaddingLeft(), title.getPaddingTop(),
                        title.getPaddingRight(), missingPadding + title.getPaddingBottom());
            }
        }
    }

    private void initializeShyBehavior() {
        ViewParent parent = getParent();

        boolean hasAbusiveParent = parent != null
                && parent instanceof CoordinatorLayout;

        if (!hasAbusiveParent) {
            throw new RuntimeException("In order to have shy behavior, the " +
                    "BottomBar must be a direct child of a CoordinatorLayout.");
        }

        if (!shyHeightAlreadyCalculated) {
            int height = getHeight();

            if (height != 0) {
                updateShyHeight(height);
                shyHeightAlreadyCalculated = true;
            }
        }
    }

    private void updateShyHeight(int height) {
        ((CoordinatorLayout.LayoutParams) getLayoutParams())
                .setBehavior(new BottomNavigationBehavior(height, 0, false));
    }

    private void resizeForDrawingUnderNavbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int currentHeight = getHeight();

            if (currentHeight != 0 && !navBarAccountedHeightCalculated) {
                navBarAccountedHeightCalculated = true;
                tabContainer.getLayoutParams().height = currentHeight;

                int navbarHeight = NavbarUtils.getNavbarHeight(getContext());
                int finalHeight = currentHeight + navbarHeight;
                getLayoutParams().height = finalHeight;

                if (isShy()) {
                    updateShyHeight(finalHeight);
                }
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = saveState();
        bundle.putParcelable("superstate", super.onSaveInstanceState());
        return bundle;
    }

    @VisibleForTesting
    Bundle saveState() {
        Bundle outState = new Bundle();
        outState.putInt(STATE_CURRENT_SELECTED_TAB, currentTabPosition);

        return outState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            restoreState(bundle);

            state = bundle.getParcelable("superstate");
        }
        super.onRestoreInstanceState(state);
    }

    @VisibleForTesting
    void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isComingFromRestoredState = true;
            ignoreTabReselectionListener = true;

            int restoredPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_TAB, currentTabPosition);
            selectTabAtPosition(restoredPosition, false);
        }
    }

    @Override
    public void onClick(View v) {
        handleClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        return handleLongClick(v);
    }

    private BottomBarTab findTabInLayout(ViewGroup child) {
        for (int i = 0; i < child.getChildCount(); i++) {
            View candidate = child.getChildAt(i);

            if (candidate instanceof BottomBarTab) {
                return (BottomBarTab) candidate;
            }
        }

        return null;
    }

    private void handleClick(View v) {
        BottomBarTab oldTab = getCurrentTab();
        BottomBarTab newTab = (BottomBarTab) v;

        oldTab.deselect(true);
        newTab.select(true);

        shiftingMagic(oldTab, newTab, true);
        handleBackgroundColorChange(newTab, true);
        updateSelectedTab(newTab.getIndexInTabContainer());
    }

    private boolean handleLongClick(View v) {
        if (v instanceof BottomBarTab) {
            BottomBarTab longClickedTab = (BottomBarTab) v;

            if ((isShiftingMode() || isTabletMode) && !longClickedTab.isActive()) {
                Toast.makeText(getContext(), longClickedTab.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    private void selectTabAtPosition(int position, boolean animate) {
        BottomBarTab oldTab = getCurrentTab();
        BottomBarTab newTab = getTabAtPosition(position);

        oldTab.deselect(animate);
        newTab.select(animate);

        updateSelectedTab(position);
        shiftingMagic(oldTab, newTab, animate);
        handleBackgroundColorChange(newTab, false);
    }

    private void updateSelectedTab(int newPosition) {
        int newTabId = getTabAtPosition(newPosition).getId();

        if (newPosition != currentTabPosition) {
            if (onTabSelectListener != null) {
                onTabSelectListener.onTabSelected(newTabId);
            }
        } else if (onTabReselectListener != null && !ignoreTabReselectionListener) {
            onTabReselectListener.onTabReSelected(newTabId);
        }

        currentTabPosition = newPosition;

        if (ignoreTabReselectionListener) {
            ignoreTabReselectionListener = false;
        }
    }

    private void shiftingMagic(BottomBarTab oldTab, BottomBarTab newTab, boolean animate) {
        if (isShiftingMode()) {
            oldTab.updateWidth(inActiveShiftingItemWidth, animate);
            newTab.updateWidth(activeShiftingItemWidth, animate);
        }
    }

    private void handleBackgroundColorChange(BottomBarTab tab, boolean animate) {
        int newColor = tab.getBarColorWhenSelected();

        if (currentBackgroundColor == newColor) {
            return;
        }

        if (!animate) {
            outerContainer.setBackgroundColor(newColor);
            return;
        }

        View clickedView = tab;

        if (tab.hasActiveBadge()) {
            clickedView = tab.getOuterView();
        }

        animateBGColorChange(clickedView, newColor);
        currentBackgroundColor = newColor;
    }

    private void animateBGColorChange(View clickedView, final int newColor) {
        prepareForBackgroundColorAnimation(newColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!outerContainer.isAttachedToWindow()) {
                return;
            }

            backgroundCircularRevealAnimation(clickedView, newColor);
        } else {
            backgroundCrossfadeAnimation(newColor);
        }
    }

    private void prepareForBackgroundColorAnimation(int newColor) {
        outerContainer.clearAnimation();
        backgroundOverlay.clearAnimation();

        backgroundOverlay.setBackgroundColor(newColor);
        backgroundOverlay.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void backgroundCircularRevealAnimation(View clickedView, final int newColor) {
        int centerX = (int) (ViewCompat.getX(clickedView) + (clickedView.getMeasuredWidth() / 2));
        int yOffset = isTabletMode ? (int) ViewCompat.getY(clickedView) : 0;
        int centerY = yOffset + clickedView.getMeasuredHeight() / 2;
        int startRadius = 0;
        int finalRadius = isTabletMode ? outerContainer.getHeight() : outerContainer.getWidth();

        Animator animator = ViewAnimationUtils.createCircularReveal(
                backgroundOverlay,
                centerX,
                centerY,
                startRadius,
                finalRadius
        );

        if (isTabletMode) {
            animator.setDuration(500);
        }

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onEnd();
            }

            private void onEnd() {
                outerContainer.setBackgroundColor(newColor);
                backgroundOverlay.setVisibility(View.INVISIBLE);
                ViewCompat.setAlpha(backgroundOverlay, 1);
            }
        });

        animator.start();
    }

    private void backgroundCrossfadeAnimation(final int newColor) {
        ViewCompat.setAlpha(backgroundOverlay, 0);
        ViewCompat.animate(backgroundOverlay)
                .alpha(1)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        onEnd();
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        onEnd();
                    }

                    private void onEnd() {
                        outerContainer.setBackgroundColor(newColor);
                        backgroundOverlay.setVisibility(View.INVISIBLE);
                        ViewCompat.setAlpha(backgroundOverlay, 1);
                    }
                }).start();
    }

    /**
     * Toggle translation of BottomBar to hidden and visible in a CoordinatorLayout.
     *
     * @param visible true resets translation to 0, false translates view to hidden
     */
    private void toggleShyVisibility(boolean visible) {
        BottomNavigationBehavior<BottomBar> from = BottomNavigationBehavior.from(this);
        if (from != null) {
            from.setHidden(this, visible);
        }
    }
}