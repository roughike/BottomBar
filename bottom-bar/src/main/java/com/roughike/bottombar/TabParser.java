package com.roughike.bottombar;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.support.annotation.XmlRes;
import android.support.v4.content.ContextCompat;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iiro on 21.7.2016.
 */
class TabParser {
    private final Context context;
    private final BottomBarTab.Config defaultTabConfig;
    private final XmlResourceParser parser;

    private ArrayList<BottomBarTab> tabs;
    private BottomBarTab workingTab;

    TabParser(Context context, BottomBarTab.Config defaultTabConfig, @XmlRes int tabsXmlResId) {
        this.context = context;
        this.defaultTabConfig = defaultTabConfig;

        parser = context.getResources().getXml(tabsXmlResId);
        tabs = new ArrayList<>();

        parse();
    }

    private void parse() {
        try {
            parser.next();
            int eventType = parser.getEventType();

            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if(eventType == XmlResourceParser.START_TAG) {
                    parseNewTab(parser);
                } else if(eventType == XmlResourceParser.END_TAG) {
                    if (parser.getName().equals("tab")) {
                        if (workingTab != null) {
                            tabs.add(workingTab);
                            workingTab = null;
                        }
                    }
                }

                eventType = parser.next();
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            throw new TabParserException();
        }
    }

    private void parseNewTab(XmlResourceParser parser) {
        if (workingTab == null) {
            workingTab = tabWithDefaults();
        }

        workingTab.setIndexInContainer(tabs.size());

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attrName = parser.getAttributeName(i);

            switch (attrName) {
                case "id":
                    workingTab.setId(parser.getIdAttributeResourceValue(i));
                    break;
                case "icon":
                    workingTab.setIconResId(parser.getAttributeResourceValue(i, 0));
                    break;
                case "title":
                    workingTab.setTitle(getTitleValue(i, parser));
                    break;
                case "inActiveColor":
                    Integer inActiveColor = getColorValue(i, parser);

                    if (inActiveColor != null) {
                        workingTab.setInActiveColor(inActiveColor);
                    }
                    break;
                case "activeColor":
                    Integer activeColor = getColorValue(i, parser);

                    if (activeColor != null) {
                        workingTab.setActiveColor(activeColor);
                    }
                    break;
                case "barColorWhenSelected":
                    Integer barColorWhenSelected = getColorValue(i, parser);

                    if (barColorWhenSelected != null) {
                        workingTab.setBarColorWhenSelected(barColorWhenSelected);
                    }
                    break;
                case "badgeBackgroundColor":
                    Integer badgeBackgroundColor = getColorValue(i, parser);

                    if (badgeBackgroundColor != null) {
                        workingTab.setBadgeBackgroundColor(badgeBackgroundColor);
                    }
                    break;
            }
        }
    }

    private BottomBarTab tabWithDefaults() {
        BottomBarTab tab = new BottomBarTab(context);
        tab.setConfig(defaultTabConfig);

        return tab;
    }

    private String getTitleValue(int attrIndex, XmlResourceParser parser) {
        int titleResource = parser.getAttributeResourceValue(attrIndex, 0);

        if (titleResource != 0) {
            return context.getString(titleResource);
        }

        return parser.getAttributeValue(attrIndex);
    }

    private Integer getColorValue(int attrIndex, XmlResourceParser parser) {
        int colorResource = parser.getAttributeResourceValue(attrIndex, 0);

        if (colorResource != 0) {
            return ContextCompat.getColor(context, colorResource);
        }

        try {
            return Color.parseColor(parser.getAttributeValue(attrIndex));
        } catch (Exception ignored) {
            return null;
        }
    }

    List<BottomBarTab> getTabs() {
        return tabs;
    }

    private class TabParserException extends RuntimeException {
    }
}
