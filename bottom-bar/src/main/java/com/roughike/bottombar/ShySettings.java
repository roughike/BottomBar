package com.roughike.bottombar;

/**
 * Settings specific for a shy BottomBar.
 */
public class ShySettings {
    private BottomBar bottomBar;
    private Boolean pendingIsVisibleInShyMode;

    ShySettings(BottomBar bottomBar) {
        this.bottomBar = bottomBar;
    }

    void shyHeightCalculated() {
        updatePendingShyVisibility();
    }

    /**
     * Shows the BottomBar if it was hidden, with a translate animation.
     */
    public void showBar() {
        toggleIsVisibleInShyMode(true);
    }

    /**
     * Hides the BottomBar in if it was visible, with a translate animation.
     */
    public void hideBar() {
        toggleIsVisibleInShyMode(false);
    }

    private void toggleIsVisibleInShyMode(boolean visible) {
        if (!bottomBar.isShy()) {
            return;
        }

        if (bottomBar.isShyHeightAlreadyCalculated()) {
            BottomNavigationBehavior<BottomBar> behavior = BottomNavigationBehavior.from(bottomBar);

            if (behavior != null) {
                boolean isHidden = !visible;
                behavior.setHidden(bottomBar, isHidden);
            }
        } else {
            pendingIsVisibleInShyMode = true;
        }
    }

    private void updatePendingShyVisibility() {
        if (pendingIsVisibleInShyMode != null) {
            toggleIsVisibleInShyMode(pendingIsVisibleInShyMode);
            pendingIsVisibleInShyMode = null;
        }
    }
}
