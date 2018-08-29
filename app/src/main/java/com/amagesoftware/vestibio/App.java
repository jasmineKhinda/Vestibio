package com.amagesoftware.vestibio;

import android.app.Application;

import com.amagesoftware.vestibio.tools.MainThreadBus;
import com.squareup.otto.Bus;

/**
 * Created by Andrey on 19.10.2016.
 */

public class App extends Application {

    private static final MainThreadBus BUS = new MainThreadBus();

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public static Bus getBus() {
        return BUS;
    }

}
