package com.example.bottombar.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;
import com.roughike.bottombar.ToggleVisibilityCallback;

/**
 * Created by iiro on 7.6.2016.
 */
public class StripedTabsActivity extends Activity {
    private TextView messageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_striped_tabs);

        messageView = (TextView) findViewById(R.id.messageView);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                messageView.setText(TabMessage.get(tabId, false));
            }
        });

        final ToggleVisibilityCallback callback = new StripeAnimation();
        final int tabCount = bottomBar.getTabCount();
        for (int i = 0; i < tabCount; i++) {

            BottomBarTab tab = bottomBar.getTabAtPosition(i);
            tab.setStripeViewEnabled(true);
            tab.setCustomStripeAnimation(callback);
            //all colors can be defined by xml 'stripeColor' attribute
            //you can also setup custom color per tab programmatically:
//            int color = ContextCompat.getColor(this,R.color.colorAccent);
//            tab.setStripeViewColor(color);
        }

        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                Toast.makeText(getApplicationContext(), TabMessage.get(tabId, true), Toast.LENGTH_LONG)
                     .show();
            }
        });
    }

    private static class StripeAnimation implements ToggleVisibilityCallback {

        @Override
        public void onVisibleStart(View view) {
            view.setScaleX(0);
            ViewCompat.animate(view)
                      .setDuration(200)
                      .scaleX(1f)
                      .setInterpolator(new AccelerateInterpolator())
                      .start();
        }

        @Override
        public void onVisibleEnd(View stripeView) {

        }
    }
}