package com.roughike.bottombar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by iiro on 13.8.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BottomBarTest {
    private static final int THREE_TABS = com.roughike.bottombar.test.R.xml.dummy_tabs_three;

    private static final float INACTIVE_TAB_ALPHA = 0.69f;
    private static final float ACTIVE_TAB_ALPHA = 0.96f;
    private static final int INACTIVE_TAB_COLOR = Color.parseColor("#111111");
    private static final int ACTIVE_TAB_COLOR = Color.parseColor("#222222");
    private static final int BACKGROUND_COLOR = Color.parseColor("#333333");
    private static final int BADGE_BACKGROUND_COLOR = Color.parseColor("#444444");
    private static final boolean DEFAULT_BADGE_HIDES_WHEN_SELECTED_VALUE = true;
    private static final int TITLE_TEXT_APPEARANCE = com.roughike.bottombar.test.R.style.dummy_text_appearance;
    private static final Typeface TYPEFACE = Typeface.DEFAULT_BOLD;

    private static final BottomBarTab.Config DEFAULT_CONFIG = new BottomBarTab.Config.Builder()
            .inActiveTabAlpha(INACTIVE_TAB_ALPHA)
            .activeTabAlpha(ACTIVE_TAB_ALPHA)
            .inActiveTabColor(INACTIVE_TAB_COLOR)
            .activeTabColor(ACTIVE_TAB_COLOR)
            .barColorWhenSelected(BACKGROUND_COLOR)
            .badgeBackgroundColor(BADGE_BACKGROUND_COLOR)
            .hideBadgeWhenSelected(DEFAULT_BADGE_HIDES_WHEN_SELECTED_VALUE)
            .titleTextAppearance(TITLE_TEXT_APPEARANCE)
            .titleTypeFace(TYPEFACE)
            .build();

    private Context context;

    private TabSelectionInterceptor tabSelectionInterceptor;
    private OnTabSelectListener selectListener;
    private OnTabReselectListener reselectListener;

    private BottomBar bottomBar;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();

        tabSelectionInterceptor = mock(TabSelectionInterceptor.class);
        selectListener = mock(OnTabSelectListener.class);
        reselectListener = mock(OnTabReselectListener.class);

        bottomBar = new BottomBar(context);
        bottomBar.setItems(THREE_TABS, DEFAULT_CONFIG);
        bottomBar.setOnTabSelectListener(selectListener);
        bottomBar.setOnTabReselectListener(reselectListener);
    }

    @Test
    public void canCreateNewInstanceFromXml_WithoutXmlMenuResource() {
        new BottomBar(context, null);
    }

    @Test(expected = RuntimeException.class)
    public void setItems_ThrowsExceptionWithNoResource() {
        BottomBar secondBar = new BottomBar(context);
        secondBar.setItems(0);
    }

    @Test
    public void setItems_AfterAlreadySet_ReplacesPreviousWithNewOnes() {
        int previousItemCount = bottomBar.getTabCount();
        assertThat(previousItemCount, is(3));

        bottomBar.setItems(com.roughike.bottombar.test.R.xml.dummy_tabs_five);
        assertThat(bottomBar.getTabCount(), is(5));
    }

    @Test
    public void setItemsWithCustomConfig_OverridesPreviousValues() {
        BottomBar newBar = new BottomBar(context);
        newBar.setItems(THREE_TABS, DEFAULT_CONFIG);

        BottomBarTab first = newBar.getTabAtPosition(0);
        assertEquals(INACTIVE_TAB_ALPHA, first.getInActiveAlpha(), 0);
        assertEquals(ACTIVE_TAB_ALPHA, first.getActiveAlpha(), 0);
        assertEquals(INACTIVE_TAB_COLOR, first.getInActiveColor());
        assertEquals(ACTIVE_TAB_COLOR, first.getActiveColor());
        assertEquals(BACKGROUND_COLOR, first.getBarColorWhenSelected());
        assertEquals(BADGE_BACKGROUND_COLOR, first.getBadgeBackgroundColor());
        assertEquals(DEFAULT_BADGE_HIDES_WHEN_SELECTED_VALUE, first.getBadgeHidesWhenActive());
        assertEquals(TITLE_TEXT_APPEARANCE, first.getTitleTextAppearance());
        assertEquals(TYPEFACE, first.getTitleTypeFace());
    }

    @Test
    public void setOverrideTabSelectionListener_preventSelection() {
        bottomBar.setTabSelectionInterceptor(tabSelectionInterceptor);

        when(tabSelectionInterceptor.shouldInterceptTabSelection(anyInt(), anyInt())).thenReturn(true);

        BottomBarTab oldTab = bottomBar.getCurrentTab();
        BottomBarTab newTab = bottomBar.getTabAtPosition(2);
        newTab.performClick();

        verify(tabSelectionInterceptor, times(1)).shouldInterceptTabSelection(oldTab.getId(), newTab.getId());
        assertNotSame(bottomBar.getCurrentTab(), newTab);
    }

    @Test
    @UiThreadTest
    public void setOverrideTabSelectionListener_allowingSelection() {
        bottomBar.setTabSelectionInterceptor(tabSelectionInterceptor);

        when(tabSelectionInterceptor.shouldInterceptTabSelection(anyInt(), anyInt())).thenReturn(false);

        BottomBarTab oldTab = bottomBar.getCurrentTab();
        final BottomBarTab newTab = bottomBar.getTabAtPosition(2);
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                newTab.performClick();
            }
        });


        verify(tabSelectionInterceptor, times(1)).shouldInterceptTabSelection(oldTab.getId(), newTab.getId());
        assertSame(bottomBar.getCurrentTab(), newTab);
    }

    @Test
    @UiThreadTest
    public void setOverrideTabSelectionListener_whenNoListenerSet() {
        bottomBar.removeOverrideTabSelectionListener();

        BottomBarTab oldTab = bottomBar.getCurrentTab();
        final BottomBarTab newTab = bottomBar.getTabAtPosition(2);
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                newTab.performClick();
            }
        });

        verify(tabSelectionInterceptor, times(0)).shouldInterceptTabSelection(oldTab.getId(), newTab.getId());
        assertSame(bottomBar.getCurrentTab(), newTab);
    }

    @Test
    public void setOnTabSelectListener_WhenShouldFireInitiallySetToTrue_FiresWhenSet() {
        OnTabSelectListener listener = mock(OnTabSelectListener.class);

        bottomBar.setOnTabSelectListener(listener);
        bottomBar.setOnTabSelectListener(listener, true);

        verify(listener, times(2)).onTabSelected(com.roughike.bottombar.test.R.id.tab_favorites);
    }

    @Test
    public void setOnTabSelectListener_WhenShouldFireInitiallySetToFalse_DoesNotFireWhenSet() {
        OnTabSelectListener listener = mock(OnTabSelectListener.class);

        bottomBar.setOnTabSelectListener(listener, false);
        verifyZeroInteractions(listener);
    }

    @Test
    @UiThreadTest
    public void tabCount_IsCorrect() {
        assertEquals(3, bottomBar.getTabCount());
    }

    @Test
    @UiThreadTest
    public void findingPositionForTabs_ReturnsCorrectPositions() {
        assertEquals(0, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_favorites));
        assertEquals(1, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_nearby));
        assertEquals(2, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_friends));
    }

    @Test
    @UiThreadTest
    public void whenTabIsSelected_SelectionListenerIsFired() {
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_friends);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        InOrder inOrder = inOrder(selectListener);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_friends);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_nearby);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_favorites);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @UiThreadTest
    public void afterConfigurationChanged_SavedStateRestored_AndSelectedTabPersists() {
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        Bundle savedState = bottomBar.saveState();
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.restoreState(savedState);

        assertEquals(com.roughike.bottombar.test.R.id.tab_favorites, bottomBar.getCurrentTabId());
    }

    @Test
    @UiThreadTest
    public void whenTabIsReselected_ReselectionListenerIsFired() {
        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        bottomBar.selectTabWithId(firstTabId);
        verify(reselectListener, times(1)).onTabReSelected(firstTabId);

        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        bottomBar.selectTabWithId(secondTabId);
        bottomBar.selectTabWithId(secondTabId);
        verify(reselectListener, times(1)).onTabReSelected(secondTabId);

        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;
        bottomBar.selectTabWithId(thirdTabId);
        bottomBar.selectTabWithId(thirdTabId);
        verify(reselectListener, times(1)).onTabReSelected(thirdTabId);
    }

    @Test
    @UiThreadTest
    public void whenDefaultTabIsSet_ItsSelectedAtFirst() {
        int defaultTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.setDefaultTab(defaultTabId);
        verify(selectListener).onTabSelected(defaultTabId);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void settingTooLowDefaultPosition_Throws() {
        bottomBar.setDefaultTabPosition(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void settingTooHighDefaultPosition_Throws() {
        bottomBar.setDefaultTabPosition(bottomBar.getTabCount());
    }

    @Test
    @UiThreadTest
    public void afterConfigurationChanged_UserSelectedTabPersistsWhenResettingDefaultTab() {
        int defaultTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.setDefaultTab(defaultTabId);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);

        Bundle savedState = bottomBar.saveState();
        bottomBar.restoreState(savedState);
        bottomBar.setDefaultTab(defaultTabId);

        assertNotSame(defaultTabId, bottomBar.getCurrentTabId());
        assertEquals(com.roughike.bottombar.test.R.id.tab_nearby, bottomBar.getCurrentTabId());
    }

    @Test
    @UiThreadTest
    public void whenGettingCurrentTab_ReturnsCorrectOne() {
        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        bottomBar.selectTabWithId(firstTabId);

        assertEquals(firstTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(firstTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(firstTabId), bottomBar.getCurrentTab());

        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        bottomBar.selectTabWithId(secondTabId);

        assertEquals(secondTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(secondTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(secondTabId), bottomBar.getCurrentTab());

        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;
        bottomBar.selectTabWithId(thirdTabId);

        assertEquals(thirdTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(thirdTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(thirdTabId), bottomBar.getCurrentTab());
    }

    @Test
    @UiThreadTest
    public void whenSelectionChanges_AndHasNoListeners_onlyOneTabIsSelectedAtATime() {
        bottomBar.removeOnTabSelectListener();
        bottomBar.removeOnTabReselectListener();

        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.selectTabWithId(secondTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(secondTabId);

        bottomBar.selectTabWithId(thirdTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(thirdTabId);

        bottomBar.selectTabWithId(firstTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(firstTabId);
    }

    private void assertOnlyHasOnlyOneSelectedTabWithId(int tabId) {
        for (int i = 0; i < bottomBar.getTabCount(); i++) {
            BottomBarTab tab = bottomBar.getTabAtPosition(i);

            if (tab.getId() == tabId) {
                assertTrue(tab.isActive());
            } else {
                assertFalse(tab.isActive());
            }
        }
    }

    @Test
    @UiThreadTest
    public void whenTabIsSelectedOnce_AndNoSelectionListenerSet_ReselectionListenerIsNotFired() {
        bottomBar.removeOnTabSelectListener();
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_friends);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        verifyZeroInteractions(reselectListener);
    }

    @Test
    @UiThreadTest
    public void setInActiveAlpha_UpdatesAlpha() {
        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        assertNotEquals(bottomBar.getCurrentTab(), inActiveTab);

        float previousAlpha = inActiveTab.getInActiveAlpha();
        float testAlpha = 0.1f;

        assertNotEquals(testAlpha, previousAlpha);
        assertNotEquals(testAlpha, inActiveTab.getIconView().getAlpha());
        assertNotEquals(testAlpha, inActiveTab.getTitleView().getAlpha());

        bottomBar.setInActiveTabAlpha(testAlpha);
        assertEquals(testAlpha, inActiveTab.getInActiveAlpha(), 0);
        assertEquals(testAlpha, inActiveTab.getIconView().getAlpha(), 0);
        assertEquals(testAlpha, inActiveTab.getTitleView().getAlpha(), 0);
    }

    @Test
    public void setInactiveTabAlpha_LeavesOtherValuesIntact() {
        bottomBar.setInActiveTabAlpha(0.2f);

        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        assertNotEquals(inActiveTab, bottomBar.getCurrentTab());

        assertEquals(0.2f, inActiveTab.getInActiveAlpha(), 0);
        assertEquals(ACTIVE_TAB_ALPHA, inActiveTab.getActiveAlpha(), 0);
        assertEquals(INACTIVE_TAB_COLOR, inActiveTab.getInActiveColor());
        assertEquals(ACTIVE_TAB_COLOR, inActiveTab.getActiveColor());
        assertEquals(BACKGROUND_COLOR, inActiveTab.getBarColorWhenSelected());
        assertEquals(BADGE_BACKGROUND_COLOR, inActiveTab.getBadgeBackgroundColor());
        assertEquals(TITLE_TEXT_APPEARANCE, inActiveTab.getTitleTextAppearance());
        assertEquals(TYPEFACE, inActiveTab.getTitleTypeFace());
    }

    @Test
    @UiThreadTest
    public void setActiveAlpha_UpdatesAlpha() {
        BottomBarTab activeTab = bottomBar.getCurrentTab();

        float previousAlpha = activeTab.getActiveAlpha();
        float testAlpha = 0.69f;

        assertNotEquals(testAlpha, previousAlpha);
        assertNotEquals(testAlpha, activeTab.getIconView().getAlpha());
        assertNotEquals(testAlpha, activeTab.getTitleView().getAlpha());

        bottomBar.setActiveTabAlpha(testAlpha);
        assertEquals(testAlpha, activeTab.getActiveAlpha(), 0);
        assertEquals(testAlpha, activeTab.getIconView().getAlpha(), 0);
        assertEquals(testAlpha, activeTab.getTitleView().getAlpha(), 0);
    }

    @Test
    public void setActiveTabAlpha_LeavesOtherValuesIntact() {
        bottomBar.setActiveTabAlpha(0.2f);

        BottomBarTab activeTab = bottomBar.getCurrentTab();
        assertEquals(INACTIVE_TAB_ALPHA, activeTab.getInActiveAlpha(), 0);
        assertEquals(0.2f, activeTab.getActiveAlpha(), 0);
        assertEquals(INACTIVE_TAB_COLOR, activeTab.getInActiveColor());
        assertEquals(ACTIVE_TAB_COLOR, activeTab.getActiveColor());
        assertEquals(BACKGROUND_COLOR, activeTab.getBarColorWhenSelected());
        assertEquals(BADGE_BACKGROUND_COLOR, activeTab.getBadgeBackgroundColor());
        assertEquals(TITLE_TEXT_APPEARANCE, activeTab.getTitleTextAppearance());
        assertEquals(TYPEFACE, activeTab.getTitleTypeFace());
    }

    @Test
    @UiThreadTest
    public void setInActiveColor_UpdatesColor() {
        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        assertNotEquals(bottomBar.getCurrentTab(), inActiveTab);

        int previousInActiveColor = inActiveTab.getInActiveColor();
        int previousIconColor = inActiveTab.getCurrentDisplayedIconColor();
        int previousTitleColor = inActiveTab.getCurrentDisplayedTitleColor();

        int testColor = Color.GREEN;
        assertNotEquals(testColor, previousInActiveColor);
        assertNotEquals(testColor, previousIconColor);
        assertNotEquals(testColor, previousTitleColor);

        bottomBar.setInActiveTabColor(testColor);
        assertEquals(testColor, inActiveTab.getInActiveColor());
        assertEquals(testColor, inActiveTab.getCurrentDisplayedIconColor());
        assertEquals(testColor, inActiveTab.getCurrentDisplayedTitleColor());
    }

    @Test
    public void setInactiveColor_LeavesOtherValuesIntact() {
        bottomBar.setInActiveTabColor(Color.BLUE);

        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        assertNotEquals(inActiveTab, bottomBar.getCurrentTab());

        assertEquals(INACTIVE_TAB_ALPHA, inActiveTab.getInActiveAlpha(), 0);
        assertEquals(ACTIVE_TAB_ALPHA, inActiveTab.getActiveAlpha(), 0);
        assertEquals(Color.BLUE, inActiveTab.getInActiveColor());
        assertEquals(ACTIVE_TAB_COLOR, inActiveTab.getActiveColor());
        assertEquals(BACKGROUND_COLOR, inActiveTab.getBarColorWhenSelected());
        assertEquals(BADGE_BACKGROUND_COLOR, inActiveTab.getBadgeBackgroundColor());
        assertEquals(TITLE_TEXT_APPEARANCE, inActiveTab.getTitleTextAppearance());
        assertEquals(TYPEFACE, inActiveTab.getTitleTypeFace());
    }

    @Test
    @UiThreadTest
    public void setActiveColor_UpdatesColor() {
        BottomBarTab activeTab = bottomBar.getCurrentTab();
        int previousActiveColor = activeTab.getActiveColor();
        int previousIconColor = activeTab.getCurrentDisplayedIconColor();
        int previousTitleColor = activeTab.getCurrentDisplayedTitleColor();

        int testColor = Color.GRAY;
        assertNotEquals(testColor, previousActiveColor);
        assertNotEquals(testColor, previousIconColor);
        assertNotEquals(testColor, previousTitleColor);

        bottomBar.setActiveTabColor(testColor);
        assertEquals(testColor, activeTab.getActiveColor());
        assertEquals(testColor, activeTab.getCurrentDisplayedIconColor());
        assertEquals(testColor, activeTab.getCurrentDisplayedTitleColor());
    }

    @Test
    public void setActiveColor_LeavesOtherValuesIntact() {
        bottomBar.setActiveTabColor(Color.BLUE);

        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        assertNotEquals(inActiveTab, bottomBar.getCurrentTab());

        assertEquals(INACTIVE_TAB_ALPHA, inActiveTab.getInActiveAlpha(), 0);
        assertEquals(ACTIVE_TAB_ALPHA, inActiveTab.getActiveAlpha(), 0);
        assertEquals(INACTIVE_TAB_COLOR, inActiveTab.getInActiveColor());
        assertEquals(Color.BLUE, inActiveTab.getActiveColor());
        assertEquals(BACKGROUND_COLOR, inActiveTab.getBarColorWhenSelected());
        assertEquals(BADGE_BACKGROUND_COLOR, inActiveTab.getBadgeBackgroundColor());
        assertEquals(TITLE_TEXT_APPEARANCE, inActiveTab.getTitleTextAppearance());
        assertEquals(TYPEFACE, inActiveTab.getTitleTypeFace());
    }

    @Test
    @UiThreadTest
    public void setBadgeBackgroundColor_UpdatesColor() {
        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        inActiveTab.setBadgeCount(3);

        int previousBadgeColor = inActiveTab.getBadgeBackgroundColor();
        int testColor = Color.GREEN;
        assertNotEquals(testColor, previousBadgeColor);

        bottomBar.setBadgeBackgroundColor(testColor);
        assertEquals(testColor, inActiveTab.getBadgeBackgroundColor());
    }

    @Test
    public void setBadgeBackgroundColor_LeavesOtherValuesIntact() {
        bottomBar.setBadgeBackgroundColor(Color.BLUE);

        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        assertNotEquals(inActiveTab, bottomBar.getCurrentTab());

        assertEquals(INACTIVE_TAB_ALPHA, inActiveTab.getInActiveAlpha(), 0);
        assertEquals(ACTIVE_TAB_ALPHA, inActiveTab.getActiveAlpha(), 0);
        assertEquals(INACTIVE_TAB_COLOR, inActiveTab.getInActiveColor());
        assertEquals(ACTIVE_TAB_COLOR, inActiveTab.getActiveColor());
        assertEquals(BACKGROUND_COLOR, inActiveTab.getBarColorWhenSelected());
        assertEquals(Color.BLUE, inActiveTab.getBadgeBackgroundColor());
        assertEquals(TITLE_TEXT_APPEARANCE, inActiveTab.getTitleTextAppearance());
        assertEquals(TYPEFACE, inActiveTab.getTitleTypeFace());
    }

    @Test
    @UiThreadTest
    public void setBadgeHidesWhenSelected_UpdatesBadgeHidesWhenSelected() {
        BottomBarTab tab = bottomBar.getCurrentTab();

        boolean previousBadgeHidesValue = tab.getBadgeHidesWhenActive();
        assertTrue(previousBadgeHidesValue);

        bottomBar.setBadgesHideWhenActive(false);
        assertFalse(tab.getBadgeHidesWhenActive());
    }

    @Test
    public void setBadgeHidesWhenSelected_LeavesOtherValuesIntact() {
        bottomBar.setBadgesHideWhenActive(true);

        BottomBarTab tab = bottomBar.getCurrentTab();

        assertEquals(INACTIVE_TAB_ALPHA, tab.getInActiveAlpha(), 0);
        assertEquals(ACTIVE_TAB_ALPHA, tab.getActiveAlpha(), 0);
        assertEquals(INACTIVE_TAB_COLOR, tab.getInActiveColor());
        assertEquals(ACTIVE_TAB_COLOR, tab.getActiveColor());
        assertEquals(BACKGROUND_COLOR, tab.getBarColorWhenSelected());
        assertEquals(BADGE_BACKGROUND_COLOR, tab.getBadgeBackgroundColor());
        assertTrue(tab.getBadgeHidesWhenActive());
        assertEquals(TITLE_TEXT_APPEARANCE, tab.getTitleTextAppearance());
        assertEquals(TYPEFACE, tab.getTitleTypeFace());
    }

    @Test
    @UiThreadTest
    public void setTitleTextAppearance_UpdatesAppearance() {
        BottomBarTab tab = bottomBar.getCurrentTab();

        int testTextApperance = -666;
        assertNotEquals(testTextApperance, tab.getTitleTextAppearance());
        assertNotEquals(testTextApperance, tab.getCurrentDisplayedTextAppearance());

        bottomBar.setTabTitleTextAppearance(testTextApperance);
        assertEquals(testTextApperance, tab.getTitleTextAppearance());
        assertEquals(testTextApperance, tab.getCurrentDisplayedTextAppearance());
    }

    @Test
    public void setTitleTextAppearance_LeavesOtherValuesIntact() {
        bottomBar.setTabTitleTextAppearance(-666);

        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        assertNotEquals(inActiveTab, bottomBar.getCurrentTab());

        assertEquals(INACTIVE_TAB_ALPHA, inActiveTab.getInActiveAlpha(), 0);
        assertEquals(ACTIVE_TAB_ALPHA, inActiveTab.getActiveAlpha(), 0);
        assertEquals(INACTIVE_TAB_COLOR, inActiveTab.getInActiveColor());
        assertEquals(ACTIVE_TAB_COLOR, inActiveTab.getActiveColor());
        assertEquals(BACKGROUND_COLOR, inActiveTab.getBarColorWhenSelected());
        assertEquals(BADGE_BACKGROUND_COLOR, inActiveTab.getBadgeBackgroundColor());
        assertEquals(-666, inActiveTab.getTitleTextAppearance());
        assertEquals(TYPEFACE, inActiveTab.getTitleTypeFace());
    }

    @Test
    @UiThreadTest
    public void setTitleTypeface_UpdatesTypeface() {
        BottomBarTab tab = bottomBar.getCurrentTab();
        Typeface testTypeface = Typeface.createFromAsset(
                bottomBar.getContext().getAssets(), "fonts/GreatVibes-Regular.otf");

        assertNotEquals(testTypeface, tab.getTitleTypeFace());
        assertNotEquals(testTypeface, tab.getTitleView().getTypeface());

        bottomBar.setTabTitleTypeface(testTypeface);
        assertEquals(testTypeface, tab.getTitleTypeFace());
        assertEquals(testTypeface, tab.getTitleView().getTypeface());
    }

    @Test
    public void setTitleTypeface_LeavesOtherValuesIntact() {
        bottomBar.setTabTitleTypeface(Typeface.DEFAULT);

        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        assertNotEquals(inActiveTab, bottomBar.getCurrentTab());

        assertEquals(INACTIVE_TAB_ALPHA, inActiveTab.getInActiveAlpha(), 0);
        assertEquals(ACTIVE_TAB_ALPHA, inActiveTab.getActiveAlpha(), 0);
        assertEquals(INACTIVE_TAB_COLOR, inActiveTab.getInActiveColor());
        assertEquals(ACTIVE_TAB_COLOR, inActiveTab.getActiveColor());
        assertEquals(BACKGROUND_COLOR, inActiveTab.getBarColorWhenSelected());
        assertEquals(BADGE_BACKGROUND_COLOR, inActiveTab.getBadgeBackgroundColor());
        assertEquals(TITLE_TEXT_APPEARANCE, inActiveTab.getTitleTextAppearance());
        assertEquals(Typeface.DEFAULT, inActiveTab.getTitleTypeFace());
    }
}
