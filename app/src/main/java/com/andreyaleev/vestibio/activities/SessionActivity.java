package com.andreyaleev.vestibio.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreyaleev.vestibio.R;
import com.andreyaleev.vestibio.db.SessionsDataSource;

import com.andreyaleev.vestibio.fragments.MetronomeFragment;
import com.andreyaleev.vestibio.metronome.Session;

import com.andreyaleev.vestibio.tools.Utils;

import java.text.SimpleDateFormat;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jkhinda on 27/06/18.
 */

public class SessionActivity extends BaseActivity {

        public static String SESSION_TAG = "SESSION_TAG";


        @BindView(R.id.edtTitle)
        EditText edtTitle;
        @BindView(R.id.beatsNumber)
        TextView bpm;
        @BindView(R.id.reps)
        TextView reps;
        @BindView(R.id.sets)
        TextView sets;
        @BindView(R.id.rest)
        TextView rest;
        @BindView(R.id.durationSession)
        TextView totalDuration;
        @BindView(R.id.date)
        TextView timestamp;

        @BindView(R.id.edtNotes)
        TextView notes;
        @BindView(R.id.dizzy_level)
        Spinner dizzyLevel;
        int sessionId=0;



    private RecyclerView.LayoutManager mLayoutManager;


        private Session session;
        private SessionsDataSource dataSource;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        ButterKnife.bind(this);
        setTitle(getString(R.string.addsession));
        showBackArrow();

        mLayoutManager = new LinearLayoutManager(this);
        Utils.setKeyboardAutoHide(this, edtTitle);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.dizzyness, R.layout.spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            dizzyLevel.setAdapter(adapter);


            //ONCLICK
            dizzyLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });




            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                this.session = (Session) bundle.getSerializable(SESSION_TAG);
                edtTitle.setText(session.getTitle());
                edtTitle.setSelection(edtTitle.getText().length());
                bpm.setText(String.valueOf(session.getBpm()));
                reps.setText(String.valueOf(session.getDuration()));
                sets.setText(String.valueOf(session.getSets()));
                rest.setText(String.valueOf(session.getRest()));

                if(session.getDizzynesslevel()==0){
                    dizzyLevel.setSelection(0);
                }else{
                    dizzyLevel.setSelection(session.getDizzynesslevel());
                }
                totalDuration.setText( getString(R.string.duration_tag)+"  " +MetronomeFragment.timeConversion(session.getTotalDuration()));

                long timeStampMillis = session.getTimeStamp();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy 'at' h:mma");
                Date resultdate = new Date(timeStampMillis);
                timestamp.setText(getString(R.string.date_tag) +"  "+sdf.format(resultdate));
                notes.setText(session.getNotes());



            }

        dataSource = new SessionsDataSource(this);
        dataSource.open();








    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.session != null) {
            getMenuInflater().inflate(R.menu.menu_song, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_apply, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_apply) {
            saveSong();
            return true;
        }
        if (id == R.id.action_remove) {
            removeSong();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }

    private void saveSong() {
        if (!edtTitle.getText().toString().isEmpty()) {
            long songId = -1;
            if (this.session == null) {
                this.session = new Session();
            } else {
                songId = this.session.getId();
            }
            this.session.setTitle(edtTitle.getText().toString());
            this.session.setNotes(notes.getText().toString());
            Log.d("Vestibio", "dizzy level "+ dizzyLevel.getSelectedItem().toString());
            if(dizzyLevel.getSelectedItem().toString().trim().equals("N/A")){
                this.session.setDizzynesslevel(0);
            }else{
                this.session.setDizzynesslevel(Integer.parseInt(dizzyLevel.getSelectedItem().toString().trim()));
            }

            if (songId != -1) {
                dataSource.updateSession(this.session);
                Log.d("Vestibio", "dizzy level -1 "+ songId);
                Log.d("Vestibio", "dizzy level -1 title "+ this.session.getTitle());
            } else {
                Log.d("Vestibio", "dizzy level not -1 "+ dizzyLevel.getSelectedItem().toString());
                long id = dataSource.insertSession(this.session);
            }
            onBackPressed();
        } else {
            edtTitle.setError(getString(R.string.empty_field));
        }
    }

    private void removeSong() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    dataSource.removeSession(session);
                    finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_delete)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();

    }


}
