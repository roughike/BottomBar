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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.XmlRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
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
    private static final int BEHAVIOR_ICONS_ONLY = 8;

    private BatchTabPropertyApplier batchPropertyApplier;
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
    private boolean hideBadgeWhenActive;
    private boolean longPressHintsEnabled;
    private int titleTextAppearance;
    private Typeface titleTypeFace;
    private boolean showShadow;
    private float shadowElevation;
    private View shadowView;

    private View backgroundOverlay;
    private ViewGroup outerContainer;
    private ViewGroup tabContainer;

    private int defaultBackgroundColor = Color.WHITE;
    private int currentBackgroundColor;
    private int currentTabPosition;

    private int inActiveShiftingItemWidth;
    private int activeShiftingItemWidth;

    @Nullable
    private TabSelectionInterceptor tabSelectionInterceptor;

    @Nullable
    private OnTabSelectListener onTabSelectListener;

    @Nullable
    private OnTabReselectListener onTabReselectListener;

    private boolean isComingFromRestoredState;
    private boolean ignoreTabReselectionListener;

    private ShySettings shySettings;
    private boolean shyHeightAlreadyCalculated;
    private boolean navBarAccountedHeightCalculated;

    private BottomBarTab[] currentTabs;

    public BottomBar(Context context) {
        this(context, null);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        batchPropertyApplier = new BatchTabPropertyApplier(this);

        populateAttributes(context, attrs, defStyleAttr, defStyleRes);
        initializeViews();
        determineInitialBackgroundColor();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            init21(context);
        }

        if (tabXmlResource != 0) {
            setItems(tabXmlResource);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // This is so that in Pre-Lollipop devices there is a shadow BUT without pushing the content
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && showShadow && shadowView != null) {
            shadowView.setVisibility(VISIBLE);
            ViewGroup.LayoutParams params = getLayoutParams();
            if (params instanceof MarginLayoutParams) {
                MarginLayoutParams layoutParams = (MarginLayoutParams) params;
                final int shadowHeight = getResources().getDimensionPixelSize(R.dimen.bb_fake_shadow_height);

                layoutParams.setMargins(layoutParams.leftMargin,
                        layoutParams.topMargin - shadowHeight,
                        layoutParams.rightMargin,
                        layoutParams.bottomMargin);
                setLayoutParams(params);
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void init21(Context context) {
        if (showShadow) {
            shadowElevation = getElevation();
            shadowElevation = shadowElevation > 0
                    ? shadowElevation
                    : getResources().getDimensionPixelSize(R.dimen.bb_default_elevation);
            setElevation(MiscUtils.dpToPixel(context, shadowElevation));
            setOutlineProvider(ViewOutlineProvider.BOUNDS);
        }
    }

    private void populateAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        primaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);
        screenWidth = MiscUtils.getScreenWidth(getContext());
        tenDp = MiscUtils.dpToPixel(getContext(), 10);
        maxFixedItemWidth = MiscUtils.dpToPixel(getContext(), 168);

        TypedArray ta = context.getTheme()
                               .obtainStyledAttributes(attrs, R.styleable.BottomBar, defStyleAttr, defStyleRes);

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

            longPressHintsEnabled = ta.getBoolean(R.styleable.BottomBar_bb_longPressHintsEnabled, true);
            inActiveTabColor = ta.getColor(R.styleable.BottomBar_bb_inActiveTabColor, defaultInActiveColor);
            activeTabColor = ta.getColor(R.styleable.BottomBar_bb_activeTabColor, defaultActiveColor);
            badgeBackgroundColor = ta.getColor(R.styleable.BottomBar_bb_badgeBackgroundColor, Color.RED);
            hideBadgeWhenActive = ta.getBoolean(R.styleable.BottomBar_bb_badgesHideWhenActive, true);
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

    boolean isShy() {
        return !isTabletMode && hasBehavior(BEHAVIOR_SHY);
    }

    boolean isShyHeightAlreadyCalculated() {
        return shyHeightAlreadyCalculated;
    }

    private boolean isIconsOnlyMode() {
        return !isTabletMode && hasBehavior(BEHAVIOR_ICONS_ONLY);
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

        View rootView = inflate(getContext(),
                isTabletMode ? R.layout.bb_bottom_bar_item_container_tablet : R.layout.bb_bottom_bar_item_container, this);
        rootView.setLayoutParams(params);

        backgroundOverlay = rootView.findViewById(R.id.bb_bottom_bar_background_overlay);
        outerContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_outer_container);
        tabContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_item_container);
        shadowView = findViewById(R.id.bb_bottom_bar_shadow);
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
        updateItems(parser.parseTabs());
    }

    private BottomBarTab.Config getTabConfig() {
        return new BottomBarTab.Config.Builder()
                .inActiveTabAlpha(inActiveTabAlpha)
                .activeTabAlpha(activeTabAlpha)
                .inActiveTabColor(inActiveTabColor)
                .activeTabColor(activeTabColor)
                .barColorWhenSelected(defaultBackgroundColor)
                .badgeBackgroundColor(badgeBackgroundColor)
                .hideBadgeWhenSelected(hideBadgeWhenActive)
                .titleTextAppearance(titleTextAppearance)
                .titleTypeFace(titleTypeFace)
                .build();
    }

    private void updateItems(final List<BottomBarTab> bottomBarItems) {
        tabContainer.removeAllViews();

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

            if (isIconsOnlyMode()) {
                bottomBarTab.setIsTitleless(true);
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

        currentTabs = viewsToAdd;

        if (!isTabletMode) {
            resizeTabsToCorrectSizes(viewsToAdd);
        }
    }

    private void resizeTabsToCorrectSizes(BottomBarTab[] tabsToAdd) {
        int viewWidth = MiscUtils.pixelToDp(getContext(), getWidth());

        if (viewWidth <= 0 || viewWidth > screenWidth) {
            viewWidth = screenWidth;
        }

        int proposedItemWidth = Math.min(
                MiscUtils.dpToPixel(getContext(), viewWidth / tabsToAdd.length),
                maxFixedItemWidth
        );

        inActiveShiftingItemWidth = (int) (proposedItemWidth * 0.9);
        activeShiftingItemWidth = (int) (proposedItemWidth + (proposedItemWidth * ((tabsToAdd.length - 1) * 0.1)));
        int height = Math.round(getContext().getResources()
                                            .getDimension(R.dimen.bb_height));

        for (BottomBarTab tabView : tabsToAdd) {
            ViewGroup.LayoutParams params = tabView.getLayoutParams();
            params.height = height;

            if (isShiftingMode()) {
                if (tabView.isActive()) {
                    params.width = activeShiftingItemWidth;
                } else {
                    params.width = inActiveShiftingItemWidth;
                }
            } else {
                params.width = proposedItemWidth;
            }

            if (tabView.getParent() == null) {
                tabContainer.addView(tabView);
            }

            tabView.setLayoutParams(params);
        }
    }

    /**
     * Returns the settings specific for a shy BottomBar.
     *
     * @throws UnsupportedOperationException, if this BottomBar is not shy.
     */
    public ShySettings getShySettings() {
        if (!isShy()) {
            Log.e("BottomBar", "Tried to get shy settings for a BottomBar " +
                    "that is not shy.");
        }

        if (shySettings == null) {
            shySettings = new ShySettings(this);
        }

        return shySettings;
    }

    /**
     * Set a listener that gets fired when the selected {@link BottomBarTab} is about to change.
     *
     * @param interceptor a listener for potentially interrupting changes in tab selection.
     */
    public void setTabSelectionInterceptor(@NonNull TabSelectionInterceptor interceptor) {
        tabSelectionInterceptor = interceptor;
    }

    /**
     * Removes the current {@link TabSelectionInterceptor} listener
     */
    public void removeOverrideTabSelectionListener() {
        tabSelectionInterceptor = null;
    }

    /**
     * Set a listener that gets fired when the selected {@link BottomBarTab} changes.
     * <p>
     * Note: Will be immediately called for the currently selected tab
     * once when set.
     *
     * @param listener a listener for monitoring changes in tab selection.
     */
    public void setOnTabSelectListener(@NonNull OnTabSelectListener listener) {
        setOnTabSelectListener(listener, true);
    }

    /**
     * Set a listener that gets fired when the selected {@link BottomBarTab} changes.
     * <p>
     * If {@code shouldFireInitially} is set to false, this listener isn't fired straight away
     * it's set, but you'll get all events normally for consecutive tab selection changes.
     *
     * @param listener            a listener for monitoring changes in tab selection.
     * @param shouldFireInitially whether the listener should be fired the first time it's set.
     */
    public void setOnTabSelectListener(@NonNull OnTabSelectListener listener, boolean shouldFireInitially) {
        onTabSelectListener = listener;

        if (shouldFireInitially && getTabCount() > 0) {
            listener.onTabSelected(getCurrentTabId());
        }
    }

    /**
     * Removes the current {@link OnTabSelectListener} listener
     */
    public void removeOnTabSelectListener() {
        onTabSelectListener = null;
    }

    /**
     * Set a listener that gets fired when a currently selected {@link BottomBarTab} is clicked.
     *
     * @param listener a listener for handling tab reselections.
     */
    public void setOnTabReselectListener(@NonNull OnTabReselectListener listener) {
        onTabReselectListener = listener;
    }

    /**
     * Removes the current {@link OnTabReselectListener} listener
     */
    public void removeOnTabReselectListener() {
        onTabReselectListener = null;
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
        selectTabAtPosition(position, false);
    }

    /**
     * Select a tab at the specified position.
     *
     * @param position the position to select.
     * @param animate  should the tab change be animated or not.
     */
    public void selectTabAtPosition(int position, boolean animate) {
        if (position > getTabCount() - 1 || position < 0) {
            throw new IndexOutOfBoundsException("Can't select tab at position " +
                    position + ". This BottomBar has no items at that position.");
        }

        BottomBarTab oldTab = getCurrentTab();
        BottomBarTab newTab = getTabAtPosition(position);

        oldTab.deselect(animate);
        newTab.select(animate);

        updateSelectedTab(position);
        shiftingMagic(oldTab, newTab, animate);
        handleBackgroundColorChange(newTab, animate);
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

        if (child instanceof BadgeContainer) {
            return findTabInLayout((BadgeContainer) child);
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
     * Controls whether the long pressed tab title should be displayed in
     * a helpful Toast if the title is not currently visible.
     *
     * @param enabled true if toasts should be shown to indicate the title
     *                of a long pressed tab, false otherwise.
     */
    public void setLongPressHintsEnabled(boolean enabled) {
        longPressHintsEnabled = enabled;
    }

    /**
     * Set alpha value used for inactive BottomBarTabs.
     */
    public void setInActiveTabAlpha(float alpha) {
        inActiveTabAlpha = alpha;

        batchPropertyApplier.applyToAllTabs(new BatchTabPropertyApplier.TabPropertyUpdater() {
            @Override
            public void update(BottomBarTab tab) {
                tab.setInActiveAlpha(inActiveTabAlpha);
            }
        });
    }

    /**
     * Set alpha value used for active BottomBarTabs.
     */
    public void setActiveTabAlpha(float alpha) {
        activeTabAlpha = alpha;

        batchPropertyApplier.applyToAllTabs(new BatchTabPropertyApplier.TabPropertyUpdater() {
            @Override
            public void update(BottomBarTab tab) {
                tab.setActiveAlpha(activeTabAlpha);
            }
        });
    }

    public void setInActiveTabColor(@ColorInt int color) {
        inActiveTabColor = color;

        batchPropertyApplier.applyToAllTabs(new BatchTabPropertyApplier.TabPropertyUpdater() {
            @Override
            public void update(BottomBarTab tab) {
                tab.setInActiveColor(inActiveTabColor);
            }
        });
    }

    /**
     * Set active color used for selected BottomBarTabs.
     */
    public void setActiveTabColor(@ColorInt int color) {
        activeTabColor = color;

        batchPropertyApplier.applyToAllTabs(new BatchTabPropertyApplier.TabPropertyUpdater() {
            @Override
            public void update(BottomBarTab tab) {
                tab.setActiveColor(activeTabColor);
            }
        });
    }

    /**
     * Set background color for the badge.
     */
    public void setBadgeBackgroundColor(@ColorInt int color) {
        badgeBackgroundColor = color;

        batchPropertyApplier.applyToAllTabs(new BatchTabPropertyApplier.TabPropertyUpdater() {
            @Override
            public void update(BottomBarTab tab) {
                tab.setBadgeBackgroundColor(badgeBackgroundColor);
            }
        });
    }

    /**
     * Controls whether the badge (if any) for active tabs
     * should be hidden or not.
     */
    public void setBadgesHideWhenActive(final boolean hideWhenSelected) {
        hideBadgeWhenActive = hideWhenSelected;
        batchPropertyApplier.applyToAllTabs(new BatchTabPropertyApplier.TabPropertyUpdater() {
            @Override
            public void update(BottomBarTab tab) {
                tab.setBadgeHidesWhenActive(hideWhenSelected);
            }
        });
    }

    /**
     * Set custom text apperance for all BottomBarTabs.
     */
    public void setTabTitleTextAppearance(int textAppearance) {
        titleTextAppearance = textAppearance;

        batchPropertyApplier.applyToAllTabs(new BatchTabPropertyApplier.TabPropertyUpdater() {
            @Override
            public void update(BottomBarTab tab) {
                tab.setTitleTextAppearance(titleTextAppearance);
            }
        });
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

        batchPropertyApplier.applyToAllTabs(new BatchTabPropertyApplier.TabPropertyUpdater() {
            @Override
            public void update(BottomBarTab tab) {
                tab.setTitleTypeface(titleTypeFace);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            if (!isTabletMode) {
                resizeTabsToCorrectSizes(currentTabs);
            }

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
        if (isIconsOnlyMode()) {
            return;
        }

        int tabCount = getTabCount();

        if (tabContainer == null || tabCount == 0 || !isShiftingMode()) {
            return;
        }

        for (int i = 0; i < tabCount; i++) {
            BottomBarTab tab = getTabAtPosition(i);
            TextView title = tab.getTitleView();

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
                getShySettings().shyHeightCalculated();
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
    public void onClick(View target) {
        if (!(target instanceof BottomBarTab)) return;
        handleClick((BottomBarTab) target);
    }

    @Override
    public boolean onLongClick(View target) {
        return !(target instanceof BottomBarTab) || handleLongClick((BottomBarTab) target);
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

    private void handleClick(BottomBarTab newTab) {
        BottomBarTab oldTab = getCurrentTab();

        if (tabSelectionInterceptor != null
                && tabSelectionInterceptor.shouldInterceptTabSelection(oldTab.getId(), newTab.getId())) {
            return;
        }

        oldTab.deselect(true);
        newTab.select(true);

        shiftingMagic(oldTab, newTab, true);
        handleBackgroundColorChange(newTab, true);
        updateSelectedTab(newTab.getIndexInTabContainer());
    }

    private boolean handleLongClick(BottomBarTab longClickedTab) {
        boolean areInactiveTitlesHidden = isShiftingMode() || isTabletMode;
        boolean isClickedTitleHidden = !longClickedTab.isActive();
        boolean shouldShowHint = areInactiveTitlesHidden
                && isClickedTitleHidden
                && longPressHintsEnabled;

        if (shouldShowHint) {
            Toast.makeText(getContext(), longClickedTab.getTitle(), Toast.LENGTH_SHORT)
                 .show();
        }

        return true;
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
                  })
                  .start();
    }
}
