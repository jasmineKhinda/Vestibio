package com.andreyaleev.metrosong;

import android.app.Application;

import com.andreyaleev.metrosong.tools.MainThreadBus;
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
