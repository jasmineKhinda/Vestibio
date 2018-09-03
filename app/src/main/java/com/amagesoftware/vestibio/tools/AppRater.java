package com.amagesoftware.vestibio.tools;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amagesoftware.vestibio.R;

/**
 * Created by jkhinda on 24/08/18.
 */

public class AppRater {
    private  static String APP_TITLE = "";// App Name
    private  static String APP_PNAME = "";// Package Name

    private final static int DAYS_UNTIL_PROMPT = 0;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 0;//Min number of launches

    public static void app_launched(Context mContext) {
        APP_TITLE = mContext.getString(R.string.app_name);
        APP_PNAME = mContext.getPackageName();
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        Log.d("vestibio", "dontshowagain"+ prefs.getBoolean("dontshowagain", false) );
        ///TODO: UNCOMMENT FOLLOWING LINE BEFORE RELEASE
        // if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    public static void launchMarket(Context mcontext) {
        Uri uri = Uri.parse("market://details?id=" + mcontext.getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            mcontext.startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mcontext, " unable to find market app", Toast.LENGTH_LONG).show();

        }}

    private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext,R.style.dialog);
         dialog.setTitle("Rate " + APP_TITLE);




        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(mContext.getResources().getColor(R.color.background));
        ll.setHorizontalGravity(Gravity.CENTER);

        ll.setPadding(0,5,0,50);



        TextView tv = new TextView(mContext);
        tv.setText("Is " + APP_TITLE + " useful so far?");

        //tv.setText("Thanks for using " + APP_TITLE + "! If this app has been useful in your recovery so far, we'd love to hear. Thanks for your support!");
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextSize(20);
        //tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setWidth(800);

        tv.setPadding(80, 60, 80, 25);
        tv.setTextColor(mContext.getResources().getColor(R.color.black));
        ll.addView(tv);

        TextView tv2 = new TextView(mContext);

        tv2.setText("If yes, we'd love to hear! Please take a moment to rate us. Thanks so much!");

        //tv.setText("Thanks for using " + APP_TITLE + "! If this app has been useful in your recovery so far, we'd love to hear. Thanks for your support!");
        tv2.setGravity(Gravity.CENTER);

        tv2.setTextSize(16);
        //tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv2.setWidth(800);

        tv2.setPadding(80, 0, 80, 80);
        tv2.setTextColor(mContext.getResources().getColor(R.color.black));
        ll.addView(tv2);


        Button b1 = new Button(mContext);
        b1.setText("Rate " + APP_TITLE);
        b1.setTypeface(tv.getTypeface(), Typeface.BOLD);
        b1.setTextColor(mContext.getResources().getColor(R.color.themeGradientPrimary));
        b1.setBackgroundColor(mContext.getResources().getColor(R.color.background));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            b1.setStateListAnimator(null);
        }


        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                launchMarket(mContext);
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setTextColor(mContext.getResources().getColor(R.color.themeGradientPrimary));
        b2.setTypeface(tv.getTypeface(), Typeface.BOLD);
        b2.setBackgroundColor(mContext.getResources().getColor(R.color.background));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            b2.setStateListAnimator(null);
        }

        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                editor.putLong("launch_count", 0);
                editor.commit();
                }

                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(mContext);
        b3.setText("No, thanks");
        b3.setBackgroundColor(mContext.getResources().getColor(R.color.background));
        b3.setTextColor(mContext.getResources().getColor(R.color.themeGradientPrimary));
        b3.setTypeface(tv.getTypeface(), Typeface.BOLD);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            b3.setStateListAnimator(null);
        }

        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);
        dialog.show();
    }
}