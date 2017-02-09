package com.roughike.bottombar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
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

    private final int sixDps;
    private final int eightDps;
    private final int sixteenDps;

    private Type type = Type.FIXED;
    private int iconResId;
    private String title;

    private float inActiveAlpha;
    private float activeAlpha;
    private int inActiveColor;
    private int activeColor;
    private int barColorWhenSelected;
    private int badgeBackgroundColor;

    private AppCompatImageView iconView;
    private TextView titleView;
    private boolean isActive;

    private int indexInContainer;

    @VisibleForTesting
    BottomBarBadge badge;
    private int titleTextAppearanceResId;
    private Typeface titleTypeFace;

    enum Type {
        FIXED, SHIFTING, TABLET
    }

    BottomBarTab(Context context) {
        super(context);

        sixDps = MiscUtils.dpToPixel(context, 6);
        eightDps = MiscUtils.dpToPixel(context, 8);
        sixteenDps = MiscUtils.dpToPixel(context, 16);
    }

    void setConfig(Config config) {
        setInActiveAlpha(config.inActiveTabAlpha);
        setActiveAlpha(config.activeTabAlpha);
        setInActiveColor(config.inActiveTabColor);
        setActiveColor(config.activeTabColor);
        setBarColorWhenSelected(config.barColorWhenSelected);
        setBadgeBackgroundColor(config.badgeBackgroundColor);
        setTitleTextAppearance(config.titleTextAppearance);
        setTitleTypeface(config.titleTypeFace);
    }

    void prepareLayout() {
        inflate(getContext(), getLayoutResource(), this);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        iconView = (AppCompatImageView) findViewById(R.id.bb_bottom_bar_icon);
        iconView.setImageResource(iconResId);

        if (type != Type.TABLET) {
            titleView = (TextView) findViewById(R.id.bb_bottom_bar_title);
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

        titleView.setTag(titleTextAppearanceResId);
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

    int getCurrentDisplayedIconColor() {
        Object tag = iconView.getTag();

        if (tag instanceof Integer) {
            return (int) iconView.getTag();
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
        Object tag = titleView.getTag();

        if (titleView != null && tag instanceof Integer) {
            return (int) titleView.getTag();
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

    @SuppressWarnings("deprecation")
    void setTitleTextAppearance(int resId) {
        this.titleTextAppearanceResId = resId;
        updateCustomTextAppearance();
    }

    public int getTitleTextAppearance() {
        return titleTextAppearanceResId;
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
            setTopPaddingAnimated(iconView.getPaddingTop(), sixDps);
            animateIcon(activeAlpha);
            animateTitle(ACTIVE_TITLE_SCALE, activeAlpha);
            animateColors(inActiveColor, activeColor);
        } else {
            setTitleScale(ACTIVE_TITLE_SCALE);
            setTopPadding(sixDps);
            setColors(activeColor);
            setAlphas(activeAlpha);
        }

        if (badge != null) {
            badge.hide();
        }
    }

    void deselect(boolean animate) {
        isActive = false;

        boolean isShifting = type == Type.SHIFTING;

        float scale = isShifting ? 0 : INACTIVE_FIXED_TITLE_SCALE;
        int iconPaddingTop = isShifting ? sixteenDps : eightDps;

        if (animate) {
            setTopPaddingAnimated(iconView.getPaddingTop(), iconPaddingTop);
            animateTitle(scale, inActiveAlpha);
            animateIcon(inActiveAlpha);
            animateColors(activeColor, inActiveColor);
        } else {
            setTitleScale(scale);
            setTopPadding(iconPaddingTop);
            setColors(inActiveColor);
            setAlphas(inActiveAlpha);
        }

        if (!isShifting && badge != null) {
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
            iconView.setTag(color);
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
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isActive && badge != null) {
                    badge.adjustPositionAndSize(BottomBarTab.this);
                    badge.show();
                }
            }
        });
        animator.start();
    }

    private void updateBadgePosition() {
        if (badge != null) {
            badge.adjustPositionAndSize(this);
        }
    }

    private void setTopPaddingAnimated(int start, int end) {
        if (type == Type.TABLET) {
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

    private void animateTitle(float finalScale, float finalAlpha) {
        if (type == Type.TABLET) {
            return;
        }

        ViewPropertyAnimatorCompat titleAnimator = ViewCompat.animate(titleView)
                .setDuration(ANIMATION_DURATION)
                .scaleX(finalScale)
                .scaleY(finalScale);
        titleAnimator.alpha(finalAlpha);
        titleAnimator.start();
    }

    private void animateIcon(float finalAlpha) {
        ViewCompat.animate(iconView)
                .setDuration(ANIMATION_DURATION)
                .alpha(finalAlpha)
                .start();
    }

    private void setTopPadding(int topPadding) {
        if (type == Type.TABLET) {
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
        if (type == Type.TABLET) {
            return;
        }

        ViewCompat.setScaleX(titleView, scale);
        ViewCompat.setScaleY(titleView, scale);
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

    public static class Config {
        private final float inActiveTabAlpha;
        private final float activeTabAlpha;
        private final int inActiveTabColor;
        private final int activeTabColor;
        private final int barColorWhenSelected;
        private final int badgeBackgroundColor;
        private final int titleTextAppearance;
        private final Typeface titleTypeFace;

        private Config(Builder builder) {
            this.inActiveTabAlpha = builder.inActiveTabAlpha;
            this.activeTabAlpha = builder.activeTabAlpha;
            this.inActiveTabColor = builder.inActiveTabColor;
            this.activeTabColor = builder.activeTabColor;
            this.barColorWhenSelected = builder.barColorWhenSelected;
            this.badgeBackgroundColor = builder.badgeBackgroundColor;
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
