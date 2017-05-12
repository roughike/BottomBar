package com.roughike.bottombar;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
public class BottomBarTab extends LinearLayout {
    @VisibleForTesting
    static final String STATE_BADGE_COUNT = "STATE_BADGE_COUNT_FOR_TAB_";

    private static final long ANIMATION_DURATION = 150;
    private static final float ACTIVE_TITLE_SCALE = 1;
    private static final float INACTIVE_FIXED_TITLE_SCALE = 0.86f;
    private static final float ACTIVE_SHIFTING_TITLELESS_ICON_SCALE = 1.24f;
    private static final float INACTIVE_SHIFTING_TITLELESS_ICON_SCALE = 1f;

    private final int sixDps;
    private final int eightDps;
    private final int sixteenDps;

    @VisibleForTesting
    BottomBarBadge badge;

    private Type type = Type.FIXED;
    private boolean isTitleless;
    private int iconResId;
    private String title;
    private float inActiveAlpha;
    private float activeAlpha;
    private int inActiveColor;
    private int activeColor;
    private int barColorWhenSelected;
    private int badgeBackgroundColor;
    private boolean badgeHidesWhenActive;
    private AppCompatImageView iconView;
    private TextView titleView;
    private boolean isActive;
    private int indexInContainer;
    private int titleTextAppearanceResId;
    private Typeface titleTypeFace;

    BottomBarTab(Context context) {
        super(context);

        sixDps = MiscUtils.dpToPixel(context, 6);
        eightDps = MiscUtils.dpToPixel(context, 8);
        sixteenDps = MiscUtils.dpToPixel(context, 16);
    }

    void setConfig(@NonNull Config config) {
        setInActiveAlpha(config.inActiveTabAlpha);
        setActiveAlpha(config.activeTabAlpha);
        setInActiveColor(config.inActiveTabColor);
        setActiveColor(config.activeTabColor);
        setBarColorWhenSelected(config.barColorWhenSelected);
        setBadgeBackgroundColor(config.badgeBackgroundColor);
        setBadgeHidesWhenActive(config.badgeHidesWhenSelected);
        setTitleTextAppearance(config.titleTextAppearance);
        setTitleTypeface(config.titleTypeFace);
    }

    void prepareLayout() {
        inflate(getContext(), getLayoutResource(), this);
        setOrientation(VERTICAL);
        setGravity(isTitleless? Gravity.CENTER : Gravity.CENTER_HORIZONTAL);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setBackgroundResource(MiscUtils.getDrawableRes(getContext(), R.attr.selectableItemBackgroundBorderless));

        iconView = (AppCompatImageView) findViewById(R.id.bb_bottom_bar_icon);
        iconView.setImageResource(iconResId);

        if (type != Type.TABLET && !isTitleless) {
            titleView = (TextView) findViewById(R.id.bb_bottom_bar_title);
            titleView.setVisibility(VISIBLE);

            if (type == Type.SHIFTING) {
                findViewById(R.id.spacer).setVisibility(VISIBLE);
            }

            updateTitle();
        }

        updateCustomTextAppearance();
        updateCustomTypeface();
    }

    @VisibleForTesting
    int getLayoutResource() {
        int layoutResource;
        switch (type) {
            case FIXED:
                layoutResource = R.layout.bb_bottom_bar_item_fixed;
                break;
            case SHIFTING:
                layoutResource = R.layout.bb_bottom_bar_item_shifting;
                break;
            case TABLET:
                layoutResource = R.layout.bb_bottom_bar_item_fixed_tablet;
                break;
            default:
                // should never happen
                throw new RuntimeException("Unknown BottomBarTab type.");
        }
        return layoutResource;
    }

    private void updateTitle() {
        if (titleView != null) {
            titleView.setText(title);
        }
    }

    @SuppressWarnings("deprecation")
    private void updateCustomTextAppearance() {
        if (titleView == null || titleTextAppearanceResId == 0) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleView.setTextAppearance(titleTextAppearanceResId);
        } else {
            titleView.setTextAppearance(getContext(), titleTextAppearanceResId);
        }

        titleView.setTag(R.id.bb_bottom_bar_appearance_id, titleTextAppearanceResId);
    }

    private void updateCustomTypeface() {
        if (titleTypeFace != null && titleView != null) {
            titleView.setTypeface(titleTypeFace);
        }
    }

    Type getType() {
        return type;
    }

    void setType(Type type) {
        this.type = type;
    }

    boolean isTitleless() {
        return isTitleless;
    }

    void setIsTitleless(boolean isTitleless) {
        if (isTitleless && getIconResId() == 0) {
            throw new IllegalStateException("This tab is supposed to be " +
                    "icon only, yet it has no icon specified. Index in " +
                    "container: " + getIndexInTabContainer());
        }

        this.isTitleless = isTitleless;
    }

    public ViewGroup getOuterView() {
        return (ViewGroup) getParent();
    }

    AppCompatImageView getIconView() {
        return iconView;
    }

    int getIconResId() {
        return iconResId;
    }

    void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    TextView getTitleView() {
        return titleView;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateTitle();
    }

    public float getInActiveAlpha() {
        return inActiveAlpha;
    }

    public void setInActiveAlpha(float inActiveAlpha) {
        this.inActiveAlpha = inActiveAlpha;

        if (!isActive) {
            setAlphas(inActiveAlpha);
        }
    }

    public float getActiveAlpha() {
        return activeAlpha;
    }

    public void setActiveAlpha(float activeAlpha) {
        this.activeAlpha = activeAlpha;

        if (isActive) {
            setAlphas(activeAlpha);
        }
    }

    public int getInActiveColor() {
        return inActiveColor;
    }

    public void setInActiveColor(int inActiveColor) {
        this.inActiveColor = inActiveColor;

        if (!isActive) {
            setColors(inActiveColor);
        }
    }

    public int getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(int activeIconColor) {
        this.activeColor = activeIconColor;

        if (isActive) {
            setColors(activeColor);
        }
    }

    public int getBarColorWhenSelected() {
        return barColorWhenSelected;
    }

    public void setBarColorWhenSelected(int barColorWhenSelected) {
        this.barColorWhenSelected = barColorWhenSelected;
    }

    public int getBadgeBackgroundColor() {
        return badgeBackgroundColor;
    }

    public void setBadgeBackgroundColor(int badgeBackgroundColor) {
        this.badgeBackgroundColor = badgeBackgroundColor;

        if (badge != null) {
            badge.setColoredCircleBackground(badgeBackgroundColor);
        }
    }

    public boolean getBadgeHidesWhenActive() {
        return badgeHidesWhenActive;
    }

    public void setBadgeHidesWhenActive(boolean hideWhenActive) {
        this.badgeHidesWhenActive = hideWhenActive;
    }

    int getCurrentDisplayedIconColor() {
        Object tag = iconView.getTag(R.id.bb_bottom_bar_color_id);

        if (tag instanceof Integer) {
            return (int) tag;
        }

        return 0;
    }

    int getCurrentDisplayedTitleColor() {
        if (titleView != null) {
            return titleView.getCurrentTextColor();
        }

        return 0;
    }

    int getCurrentDisplayedTextAppearance() {
        Object tag = titleView.getTag(R.id.bb_bottom_bar_appearance_id);

        if (titleView != null && tag instanceof Integer) {
            return (int) tag;
        }

        return 0;
    }

    public void setBadgeCount(int count) {
        if (count <= 0) {
            if (badge != null) {
                badge.removeFromTab(this);
                badge = null;
            }

            return;
        }

        if (badge == null) {
            badge = new BottomBarBadge(getContext());
            badge.attachToTab(this, badgeBackgroundColor);
        }

        badge.setCount(count);

        if (isActive && badgeHidesWhenActive) {
            badge.hide();
        }
    }

    public void removeBadge() {
        setBadgeCount(0);
    }

    boolean isActive() {
        return isActive;
    }

    boolean hasActiveBadge() {
        return badge != null;
    }

    int getIndexInTabContainer() {
        return indexInContainer;
    }

    void setIndexInContainer(int indexInContainer) {
        this.indexInContainer = indexInContainer;
    }

    void setIconTint(int tint) {
        iconView.setColorFilter(tint);
    }

    public int getTitleTextAppearance() {
        return titleTextAppearanceResId;
    }

    @SuppressWarnings("deprecation")
    void setTitleTextAppearance(int resId) {
        this.titleTextAppearanceResId = resId;
        updateCustomTextAppearance();
    }

    public void setTitleTypeface(Typeface typeface) {
        this.titleTypeFace = typeface;
        updateCustomTypeface();
    }

    public Typeface getTitleTypeFace() {
        return titleTypeFace;
    }

    void select(boolean animate) {
        isActive = true;

        if (animate) {
            animateIcon(activeAlpha, ACTIVE_SHIFTING_TITLELESS_ICON_SCALE);
            animateTitle(sixDps, ACTIVE_TITLE_SCALE, activeAlpha);
            animateColors(inActiveColor, activeColor);
        } else {
            setTitleScale(ACTIVE_TITLE_SCALE);
            setTopPadding(sixDps);
            setIconScale(ACTIVE_SHIFTING_TITLELESS_ICON_SCALE);
            setColors(activeColor);
            setAlphas(activeAlpha);
        }

        setSelected(true);

        if (badge != null && badgeHidesWhenActive) {
            badge.hide();
        }
    }

    void deselect(boolean animate) {
        isActive = false;

        boolean isShifting = type == Type.SHIFTING;

        float titleScale = isShifting ? 0 : INACTIVE_FIXED_TITLE_SCALE;
        int iconPaddingTop = isShifting ? sixteenDps : eightDps;

        if (animate) {
            animateTitle(iconPaddingTop, titleScale, inActiveAlpha);
            animateIcon(inActiveAlpha, INACTIVE_SHIFTING_TITLELESS_ICON_SCALE);
            animateColors(activeColor, inActiveColor);
        } else {
            setTitleScale(titleScale);
            setTopPadding(iconPaddingTop);
            setIconScale(INACTIVE_SHIFTING_TITLELESS_ICON_SCALE);
            setColors(inActiveColor);
            setAlphas(inActiveAlpha);
        }

        setSelected(false);

        if (!isShifting && badge != null && !badge.isVisible()) {
            badge.show();
        }
    }

    private void animateColors(int previousColor, int color) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(previousColor, color);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setColors((Integer) valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(150);
        anim.start();
    }

    private void setColors(int color) {
        if (iconView != null) {
            iconView.setColorFilter(color);
            iconView.setTag(R.id.bb_bottom_bar_color_id, color);
        }

        if (titleView != null) {
            titleView.setTextColor(color);
        }
    }

    private void setAlphas(float alpha) {
        if (iconView != null) {
            ViewCompat.setAlpha(iconView, alpha);
        }

        if (titleView != null) {
            ViewCompat.setAlpha(titleView, alpha);
        }
    }

    void updateWidth(float endWidth, boolean animated) {
        if (!animated) {
            getLayoutParams().width = (int) endWidth;

            if (!isActive && badge != null) {
                badge.adjustPositionAndSize(this);
                badge.show();
            }
            return;
        }

        float start = getWidth();

        ValueAnimator animator = ValueAnimator.ofFloat(start, endWidth);
        animator.setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ViewGroup.LayoutParams params = getLayoutParams();
                if (params == null) return;

                params.width = Math.round((float) animator.getAnimatedValue());
                setLayoutParams(params);
            }
        });

        // Workaround to avoid using faulty onAnimationEnd() listener
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isActive && badge != null) {
                    clearAnimation();
                    badge.adjustPositionAndSize(BottomBarTab.this);
                    badge.show();
                }
            }
        }, animator.getDuration());

        animator.start();
    }

    private void updateBadgePosition() {
        if (badge != null) {
            badge.adjustPositionAndSize(this);
        }
    }

    private void setTopPaddingAnimated(int start, int end) {
        if (type == Type.TABLET || isTitleless) {
            return;
        }

        ValueAnimator paddingAnimator = ValueAnimator.ofInt(start, end);
        paddingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iconView.setPadding(
                        iconView.getPaddingLeft(),
                        (Integer) animation.getAnimatedValue(),
                        iconView.getPaddingRight(),
                        iconView.getPaddingBottom()
                );
            }
        });

        paddingAnimator.setDuration(ANIMATION_DURATION);
        paddingAnimator.start();
    }

    private void animateTitle(int padding, float scale, float alpha) {
        if (type == Type.TABLET && isTitleless) {
            return;
        }

        setTopPaddingAnimated(iconView.getPaddingTop(), padding);

        ViewPropertyAnimatorCompat titleAnimator = ViewCompat.animate(titleView)
                .setDuration(ANIMATION_DURATION)
                .scaleX(scale)
                .scaleY(scale);
        titleAnimator.alpha(alpha);
        titleAnimator.start();
    }

    private void animateIconScale(float scale) {
        ViewCompat.animate(iconView)
                .setDuration(ANIMATION_DURATION)
                .scaleX(scale)
                .scaleY(scale)
                .start();
    }

    private void animateIcon(float alpha, float scale) {
        ViewCompat.animate(iconView)
                .setDuration(ANIMATION_DURATION)
                .alpha(alpha)
                .start();

        if (isTitleless && type == Type.SHIFTING) {
            animateIconScale(scale);
        }
    }

    private void setTopPadding(int topPadding) {
        if (type == Type.TABLET || isTitleless) {
            return;
        }

        iconView.setPadding(
                iconView.getPaddingLeft(),
                topPadding,
                iconView.getPaddingRight(),
                iconView.getPaddingBottom()
        );
    }

    private void setTitleScale(float scale) {
        if (type == Type.TABLET || isTitleless) {
            return;
        }

        ViewCompat.setScaleX(titleView, scale);
        ViewCompat.setScaleY(titleView, scale);
    }

    private void setIconScale(float scale) {
        if (isTitleless && type == Type.SHIFTING) {
            ViewCompat.setScaleX(iconView, scale);
            ViewCompat.setScaleY(iconView, scale);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (badge != null) {
            Bundle bundle = saveState();
            bundle.putParcelable("superstate", super.onSaveInstanceState());

            return bundle;
        }

        return super.onSaveInstanceState();
    }

    @VisibleForTesting
    Bundle saveState() {
        Bundle outState = new Bundle();
        outState.putInt(STATE_BADGE_COUNT + getIndexInTabContainer(), badge.getCount());

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
        int previousBadgeCount = savedInstanceState.getInt(STATE_BADGE_COUNT + getIndexInTabContainer());
        setBadgeCount(previousBadgeCount);
    }

    enum Type {
        FIXED, SHIFTING, TABLET
    }

    public static class Config {
        private final float inActiveTabAlpha;
        private final float activeTabAlpha;
        private final int inActiveTabColor;
        private final int activeTabColor;
        private final int barColorWhenSelected;
        private final int badgeBackgroundColor;
        private final int titleTextAppearance;
        private final Typeface titleTypeFace;
        private boolean badgeHidesWhenSelected = true;

        private Config(Builder builder) {
            this.inActiveTabAlpha = builder.inActiveTabAlpha;
            this.activeTabAlpha = builder.activeTabAlpha;
            this.inActiveTabColor = builder.inActiveTabColor;
            this.activeTabColor = builder.activeTabColor;
            this.barColorWhenSelected = builder.barColorWhenSelected;
            this.badgeBackgroundColor = builder.badgeBackgroundColor;
            this.badgeHidesWhenSelected = builder.hidesBadgeWhenSelected;
            this.titleTextAppearance = builder.titleTextAppearance;
            this.titleTypeFace = builder.titleTypeFace;
        }

        public static class Builder {
            private float inActiveTabAlpha;
            private float activeTabAlpha;
            private int inActiveTabColor;
            private int activeTabColor;
            private int barColorWhenSelected;
            private int badgeBackgroundColor;
            private boolean hidesBadgeWhenSelected = true;
            private int titleTextAppearance;
            private Typeface titleTypeFace;

            public Builder inActiveTabAlpha(float alpha) {
                this.inActiveTabAlpha = alpha;
                return this;
            }

            public Builder activeTabAlpha(float alpha) {
                this.activeTabAlpha = alpha;
                return this;
            }

            public Builder inActiveTabColor(@ColorInt int color) {
                this.inActiveTabColor = color;
                return this;
            }

            public Builder activeTabColor(@ColorInt int color) {
                this.activeTabColor = color;
                return this;
            }

            public Builder barColorWhenSelected(@ColorInt int color) {
                this.barColorWhenSelected = color;
                return this;
            }

            public Builder badgeBackgroundColor(@ColorInt int color) {
                this.badgeBackgroundColor = color;
                return this;
            }

            public Builder hideBadgeWhenSelected(boolean hide) {
                this.hidesBadgeWhenSelected = hide;
                return this;
            }

            public Builder titleTextAppearance(int titleTextAppearance) {
                this.titleTextAppearance = titleTextAppearance;
                return this;
            }

            public Builder titleTypeFace(Typeface titleTypeFace) {
                this.titleTypeFace = titleTypeFace;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }
}
