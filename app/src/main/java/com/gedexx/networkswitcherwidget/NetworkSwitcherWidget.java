package com.gedexx.networkswitcherwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implementation of App Widget functionality.
 */
public class NetworkSwitcherWidget extends AppWidgetProvider {

    public static String WIDGET_BUTTON = "com.gedexx.networkswitcherwidget.WIDGET_BUTTON";

    public static int NETWORK_MODE_GSM_ONLY = 1; /* 2G seulement */
    public static int NETWORK_MODE_WCDMA_ONLY = 2; /* 3G seulement */
    public static int NETWORK_MODE_GSM_UMTS = 3; /* 2G/3G */
    public static int NETWORK_MODE_LTE_GSM_WCDMA = 9; /* 4G, 2G/3G */

    public static TelephonyManager mTelephonyManager;
    public static RemoteViews views;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        views = new RemoteViews(context.getPackageName(), R.layout.network_switcher_widget);
        setWidgetLabel(context);

        Intent intent = new Intent(WIDGET_BUTTON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.fourGButton, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (WIDGET_BUTTON.equals(intent.getAction())) {
            if (is4gEnabled()) {
                setPreferredNetwork(NETWORK_MODE_GSM_UMTS);
            } else {
                setPreferredNetwork(NETWORK_MODE_LTE_GSM_WCDMA);
            }
            setWidgetLabel(context);
            AppWidgetManager.getInstance(context).updateAppWidget(AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, NetworkSwitcherWidget.class)), views);
        }
    }

    private static void setWidgetLabel(Context context) {
        views.setTextViewText(R.id.fourGButton, is4gEnabled() ? context.getString(R.string.lbl_4g) : context.getString(R.string.lbl_3g));
    }

    public static boolean is4gEnabled() {
        return NETWORK_MODE_LTE_GSM_WCDMA == getPreferredNetwork();
    }

    public static int getPreferredNetwork() {
        Method method = getHiddenMethod("getPreferredNetworkType", TelephonyManager.class, new Class[]{int.class});
        int preferredNetwork = -1000;
        try {
            preferredNetwork = (int) method.invoke(mTelephonyManager, (int) Math.random());
            Log.i(NetworkSwitcherWidget.class.getCanonicalName(), "Preferred Network is ::: " + preferredNetwork);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return preferredNetwork;
    }

    public void setPreferredNetwork(int networkType) {
        try {
            Method setPreferredNetwork = getHiddenMethod("setPreferredNetworkType",
                    TelephonyManager.class, new Class[]{int.class, int.class});
            Boolean success = (Boolean) setPreferredNetwork.invoke(mTelephonyManager, (int) Math.random(),
                    networkType);
            Log.i(NetworkSwitcherWidget.class.getCanonicalName(), "Could set Network Type ::: " + (success.booleanValue() ? "YES" : "NO"));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a hidden method instance from a class
     *
     * @param methodName The name of the method to be taken from the class
     * @param fromClass  The name of the class that has the method
     * @return A Method instance that can be invoked
     */
    public static Method getHiddenMethod(String methodName, Class fromClass, Class[] params) {
        Method method = null;
        try {
            Class clazz = Class.forName(fromClass.getName());
            /*for (Method meth : clazz.getMethods()) {
                Log.d(NetworkSwitcherWidget.class.getCanonicalName(), "Méthode : " + meth.getName());
            }*/

            if (params != null && params.length > 0) {
                method = clazz.getMethod(methodName, params);
            } else {
                method = clazz.getMethod(methodName);
            }
            method.setAccessible(true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return method;
    }
}

