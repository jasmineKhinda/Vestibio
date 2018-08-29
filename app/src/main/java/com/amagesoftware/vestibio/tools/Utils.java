package com.amagesoftware.vestibio.tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amagesoftware.vestibio.R;
import com.amagesoftware.vestibio.services.MetronomeService;

/**
 * Created by Andrey Aleev on 10.10.2015.
 */
public class Utils {

    public static void setKeyboardAutoHide(final Activity activity, View view) {

        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                Utils.hideKeyboard(activity);
                return false;
            });
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                Utils.setKeyboardAutoHide(activity, innerView);
            }
        }
    }


    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static boolean isHigherVersion(int version) {
        if (android.os.Build.VERSION.SDK_INT >= version) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMetronomeServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MetronomeService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static void checkAndStopService(Context context) {
        boolean serviceRunning = isMetronomeServiceRunning(context);
        Log.d(Utils.class.getSimpleName(), "" + serviceRunning);
        if (serviceRunning) {
            Intent myIntent = new Intent(context.getApplicationContext(), MetronomeService.class);
            context.stopService(myIntent);
//            String ns = Context.NOTIFICATION_SERVICE;
//            NotificationManager nMgr = (NotificationManager) context.getApplicationContext().getSystemService(ns);
//            nMgr.cancel(Constants.SERVICE_NOTIFICATION_ID);
//            Utils.showCustomThemeToast(context, context.getString(R.string.service_stopped), Toast.LENGTH_SHORT, 18);
        }
    }

    public static void showCustomThemeToast(Context context, String message, int length, Integer textSize) {
        Toast toast = Toast.makeText(context, message, length);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
        messageTextView.setTypeface(type);
        if (textSize != null) {
            messageTextView.setTextSize(textSize);
        }
        messageTextView.setTextColor(context.getResources().getColor(R.color.theme50));
        toast.show();
    }
}
