package com.andreyaleev.metrosong.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.adapters.SnippetsAdapter;
import com.andreyaleev.metrosong.db.SongsDataSource;
import com.andreyaleev.metrosong.metronome.Song;
import com.andreyaleev.metrosong.metronome.SongSnippet;
import com.andreyaleev.metrosong.tools.SnippetRemoveListener;
import com.andreyaleev.metrosong.tools.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andrey Aleev on 14.09.2015.
 */
public class SongActivity extends BaseActivity implements SnippetRemoveListener {

    public static String SONG_TAG = "SONG_TAG";

    @BindView(R.id.rvSnippets)
    RecyclerView rvSnippets;
    @BindView(R.id.edtTitle)
    EditText edtTitle;
    @BindView(R.id.fabAddSnippet)
    com.github.clans.fab.FloatingActionButton fabAddSnippet;

    private RecyclerView.LayoutManager mLayoutManager;

    private SnippetsAdapter mAdapter;
    private ArrayList<SongSnippet> snippets;
    private Song song;
    private SongsDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        ButterKnife.bind(this);
        setTitle(getString(R.string.addsession));
        showBackArrow();

        mLayoutManager = new LinearLayoutManager(this);
        rvSnippets.setLayoutManager(mLayoutManager);
        Utils.setKeyboardAutoHide(this, edtTitle);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            this.song = (Song) bundle.getSerializable(SONG_TAG);
            snippets = song.getSnippets();
            edtTitle.setText(song.getTitle());
            edtTitle.setSelection(edtTitle.getText().length());
        } else {
            snippets = new ArrayList<>();
            addNewSnippet(); // adds one default snippet
        }

        dataSource = new SongsDataSource(this);
        dataSource.open();

        mAdapter = new SnippetsAdapter(this, snippets, this);
        rvSnippets.setAdapter(mAdapter);
        fabAddSnippet.setOnClickListener(view -> addNewSnippet());
    }

    private void addNewSnippet() {
        SongSnippet snippet = new SongSnippet(120, 10, 4, 4);
        snippets.add(snippet);
        mAdapter = new SnippetsAdapter(SongActivity.this, snippets, this);
        rvSnippets.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.song != null) {
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
            int songId = -1;
            if (this.song == null) {
                this.song = new Song();
            } else {
                songId = this.song.getId();
            }
            this.song.setTitle(edtTitle.getText().toString());
            this.song.setSnippets(this.snippets);
            if (songId != -1) {
                dataSource.updateSong(this.song);
            } else {
                long id = dataSource.insertSong(this.song);
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
                    dataSource.removeSong(song);
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

    @Override
    public void onSnippetRemoved(int id) {

        mAdapter = new SnippetsAdapter(SongActivity.this, snippets, this);
        rvSnippets.setAdapter(mAdapter);
    }
}
