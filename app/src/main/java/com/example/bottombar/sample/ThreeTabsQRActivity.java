package com.example.bottombar.sample;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

/**
 * Created by iiro on 7.6.2016.
 */
public class ThreeTabsQRActivity extends AppCompatActivity {
    private BottomBar mBottomBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_tabs_quick_return);

        mBottomBar = BottomBar.attachShy((CoordinatorLayout) findViewById(R.id.myCoordinator),
                findViewById(R.id.myScrollingContent), savedInstanceState);
        mBottomBar.setItems(R.menu.bottombar_menu_three_items);

        // We're doing nothing with this listener here this time. Check example usage
        // from ThreeTabsActivity on how to use it.
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public boolean onAttemptSelectMenuTab(@IdRes int menuItemId) {
                return true;
            }

            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {

            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }
}
