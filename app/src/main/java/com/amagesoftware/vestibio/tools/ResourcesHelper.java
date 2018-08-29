package com.amagesoftware.vestibio.tools;

import android.os.Build;

import com.amagesoftware.vestibio.R;


/**
 * Created by Andrey Aleev on 12.09.2016.
 */

public class ResourcesHelper {

    public static int getStopIconResId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return R.drawable.ic_stop_24dp;
        } else {
            return R.mipmap.ic_stop;
        }
    }
}
