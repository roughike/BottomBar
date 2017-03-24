package com.roughike.bottombar;

import android.support.annotation.NonNull;

class BatchTabPropertyApplier {
    private final BottomBar bottomBar;

    interface TabPropertyUpdater {
        void update(BottomBarTab tab);
    }

    BatchTabPropertyApplier(@NonNull BottomBar bottomBar) {
        this.bottomBar = bottomBar;
    }

    void applyToAllTabs(@NonNull TabPropertyUpdater propertyUpdater) {
        int tabCount = bottomBar.getTabCount();

        if (tabCount > 0) {
            for (int i = 0; i < tabCount; i++) {
                BottomBarTab tab = bottomBar.getTabAtPosition(i);
                propertyUpdater.update(tab);
            }
        }
    }
}
