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
    private boolean isOneTime;

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
        int layoutResource;

        layoutResource = getLayoutResource();

        inflate(getContext(), layoutResource, this);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        iconView = (AppCompatImageView) findViewById(R.id.bb_bottom_bar_icon);
        iconView.setImageResource(iconResId);

        if (type != Type.TABLET) {
            titleView = (TextView) findViewById(R.id.bb_bottom_bar_title);
            titleView.setText(title);
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

    String getTitle() {
        return title;
    }

    TextView getTitleView() {
        return titleView;
    }

    void setTitle(String title) {
        this.title = title;
    }

    float getInActiveAlpha() {
        return inActiveAlpha;
    }

    void setInActiveAlpha(float inActiveAlpha) {
        this.inActiveAlpha = inActiveAlpha;

        if (!isActive) {
            setAlphas(inActiveAlpha);
        }
    }

    float getActiveAlpha() {
        return activeAlpha;
    }

    void setActiveAlpha(float activeAlpha) {
        this.activeAlpha = activeAlpha;

        if (isActive) {
            setAlphas(activeAlpha);
        }
    }

    int getInActiveColor() {
        return inActiveColor;
    }

    void setInActiveColor(int inActiveColor) {
        this.inActiveColor = inActiveColor;

        if (!isActive) {
            setColors(inActiveColor);
        }
    }

    int getActiveColor() {
        return activeColor;
    }

    void setActiveColor(int activeIconColor) {
        this.activeColor = activeIconColor;

        if (isActive) {
            setColors(activeColor);
        }
    }

    int getBarColorWhenSelected() {
        return barColorWhenSelected;
    }

    void setBarColorWhenSelected(int barColorWhenSelected) {
        this.barColorWhenSelected = barColorWhenSelected;
    }

    int getBadgeBackgroundColor() {
        return badgeBackgroundColor;
    }

    void setBadgeBackgroundColor(int badgeBackgroundColor) {
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

    public void setOneTime() {
        isOneTime = true;
    }

    public void setBadgeDot() {
        removeBadge();
        if (badge == null) {
            badge = new BottomBarBadge(getContext());
            badge.attachToTab(this, badgeBackgroundColor);
        }
    }

    public void setBadgeCount(int count) {
        if (count <= 0) {
            removeBadge();
            return;
        }

        if (badge == null) {
            badge = new BottomBarBadge(getContext());
            badge.attachToTab(this, badgeBackgroundColor);
        }

        badge.setCount(count);
    }

    public void removeBadge() {
        if (badge != null) {
            badge.removeFromTab(this);
            badge = null;
        }
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

    void setTitleTypeface(Typeface typeface) {
        this.titleTypeFace = typeface;
        updateCustomTypeface();
    }

    Typeface getTitleTypeFace() {
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
            if (isOneTime) {
                removeBadge();
                return;
            }
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
            Bundle bundle = badge.saveState(indexInContainer);
            bundle.putParcelable("superstate", super.onSaveInstanceState());
            return bundle;
        }

        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (badge != null && state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            badge.restoreState(bundle, indexInContainer);

            state = bundle.getParcelable("superstate");
        }
        super.onRestoreInstanceState(state);
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
