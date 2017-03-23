package com.roughike.bottombar;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Nikola D. on 11/22/2015.
 *
 * Credit goes to Nikola Despotoski:
 * https://github.com/NikolaDespotoski
 */
abstract class VerticalScrollingBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private int totalDyUnconsumed = 0;
    private int totalDy = 0;
    @ScrollDirection
    private int overScrollDirection = ScrollDirection.SCROLL_NONE;
    @ScrollDirection
    private int scrollDirection = ScrollDirection.SCROLL_NONE;

    VerticalScrollingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    VerticalScrollingBehavior() {
        super();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ScrollDirection.SCROLL_DIRECTION_UP, ScrollDirection.SCROLL_DIRECTION_DOWN, ScrollDirection.SCROLL_NONE})
    @interface ScrollDirection {
        int SCROLL_DIRECTION_UP = 1;
        int SCROLL_DIRECTION_DOWN = -1;
        int SCROLL_NONE = 0;
    }


    /*
       @return Overscroll direction: SCROLL_DIRECTION_UP, CROLL_DIRECTION_DOWN, SCROLL_NONE
   */
    @ScrollDirection
    int getOverScrollDirection() {
        return overScrollDirection;
    }


    /**
     * @return Scroll direction: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN, SCROLL_NONE
     */

    @ScrollDirection
    int getScrollDirection() {
        return scrollDirection;
    }


    /**
     * @param coordinatorLayout
     * @param child
     * @param direction         Direction of the overscroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
     * @param currentOverScroll Unconsumed value, negative or positive based on the direction;
     * @param totalOverScroll   Cumulative value for current direction
     */
    abstract void onNestedVerticalOverScroll(CoordinatorLayout coordinatorLayout, V child, @ScrollDirection int direction, int currentOverScroll, int totalOverScroll);

    /**
     * @param scrollDirection Direction of the overscroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
     */
    abstract void onDirectionNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed, @ScrollDirection int scrollDirection);

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & View.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyUnconsumed > 0 && totalDyUnconsumed < 0) {
            totalDyUnconsumed = 0;
            overScrollDirection = ScrollDirection.SCROLL_DIRECTION_UP;
        } else if (dyUnconsumed < 0 && totalDyUnconsumed > 0) {
            totalDyUnconsumed = 0;
            overScrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN;
        }
        totalDyUnconsumed += dyUnconsumed;
        onNestedVerticalOverScroll(coordinatorLayout, child, overScrollDirection, dyConsumed, totalDyUnconsumed);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        if (dy > 0 && totalDy < 0) {
            totalDy = 0;
            scrollDirection = ScrollDirection.SCROLL_DIRECTION_UP;
        } else if (dy < 0 && totalDy > 0) {
            totalDy = 0;
            scrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN;
        }
        totalDy += dy;
        onDirectionNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, scrollDirection);
    }


    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY, boolean consumed) {
        super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
        scrollDirection = velocityY > 0 ? ScrollDirection.SCROLL_DIRECTION_UP : ScrollDirection.SCROLL_DIRECTION_DOWN;
        return onNestedDirectionFling(coordinatorLayout, child, target, velocityX, velocityY, scrollDirection);
    }

    abstract boolean onNestedDirectionFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY, @ScrollDirection int scrollDirection);

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    @NonNull
    @Override
    public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, V child, WindowInsetsCompat insets) {
        return super.onApplyWindowInsets(coordinatorLayout, child, insets);
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
        return super.onSaveInstanceState(parent, child);
    }

}