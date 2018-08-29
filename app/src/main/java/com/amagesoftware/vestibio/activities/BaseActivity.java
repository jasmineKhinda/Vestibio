package com.amagesoftware.vestibio.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.amagesoftware.vestibio.R;
import com.amagesoftware.vestibio.tools.OnBackPressedListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andrey Aleev on 26.09.2015.
 */
public class BaseActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    ArrayList<OnBackPressedListener> backPressedListeners;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_base);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        initToolbar();
    }

    protected void initToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public void setTitle(CharSequence text) {
        super.setTitle(text);
        if (getToolbar() != null) {
            getToolbar().setTitle(text);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(text);
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void showBackArrow() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void hideBackArrow() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if (backPressedListeners != null) {
            for (OnBackPressedListener listener : backPressedListeners) {
                listener.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void superBack() {
        super.onBackPressed();
    }

    public void addBackPressedListenr(OnBackPressedListener listener) {
        if (backPressedListeners == null) {
            backPressedListeners = new ArrayList<>();
        }
        backPressedListeners.add(listener);
    }
}
