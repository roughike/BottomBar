package com.example.bottombar.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

/**
 * Created by iiro on 7.6.2016.
 */
public class ThreeTabsActivity extends Activity {
    private BottomBar mBottomBar;
    private TextView mMessageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        mMessageView = (TextView) findViewById(R.id.messageView);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottombar_menu_three_items);
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public boolean onAttemptSelectMenuTab(@IdRes int menuItemId) {
                return true;
            }

            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                mMessageView.setText(TabMessage.get(menuItemId, false));
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                Toast.makeText(getApplicationContext(), TabMessage.get(menuItemId, true), Toast.LENGTH_SHORT).show();
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