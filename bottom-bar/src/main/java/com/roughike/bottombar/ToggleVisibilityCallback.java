package com.roughike.bottombar;

import android.view.View;

/**
 * Created by lukasz.marczak on 25/10/2017.
 */

public interface ToggleVisibilityCallback {

    void onVisibleStart(View stripeView);

    void onVisibleEnd(View stripeView);
}
