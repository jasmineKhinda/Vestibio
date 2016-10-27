package com.andreyaleev.metrosong.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.activities.SongActivity;
import com.andreyaleev.metrosong.adapters.ProgramsAdapter;
import com.andreyaleev.metrosong.bus.SongStateEvent;
import com.andreyaleev.metrosong.bus.SongTickEvent;
import com.andreyaleev.metrosong.db.SongsDataSource;
import com.andreyaleev.metrosong.metronome.Song;
import com.andreyaleev.metrosong.services.MetronomeService;
import com.andreyaleev.metrosong.tools.OnBackPressedListener;
import com.andreyaleev.metrosong.tools.Utils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andrey Aleev on 09.09.2015.
 */
public class SongsListFragment extends MetronomableFragment implements OnBackPressedListener,
        ProgramsAdapter.ProgramsItemsListener {

    @BindView(R.id.rvPrograms)
    RecyclerView rvPrograms;
    @BindView(R.id.viewPlayback)
    View viewPlayback;
    @BindView(R.id.btnStopCurrent)
    Button btnStopCurrent;
    @BindView(R.id.edtSnippet)
    EditText edtSnippet;
    @BindView(R.id.edtBPM)
    EditText edtBPM;
    @BindView(R.id.edtCurrentBeat)
    EditText edtCurrentBeat;
    @BindView(R.id.edtCurrentBar)
    EditText edtCurrentBar;
    @BindView(R.id.edtBarCount)
    EditText edtBarCount;
    @BindView(R.id.fabNew)
    com.github.clans.fab.FloatingActionButton fabNew;

    private ProgramsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Song> songs;

    private SongsDataSource dataSource;

    @Subscribe
    public void onSongStateChanged(SongStateEvent event) {
        if (!event.trackIsRunning) {
            stopSong();
        }
    }

    @Subscribe
    public void onTickEvent(SongTickEvent event) {
        updateUI(event.snippetNumber + 1, event.bpm, event.currentBeat, event.barsCount,
                event.noteValue, event.beatsPerBar, event.barsPassed + 1);
    }

    private void updateUI(Integer snippetNumber, Integer bpm, Integer currentBeat, Integer barCount,
                          Integer noteValue, Integer beatsPerBar, Integer barsPassed) {
        Resources res = getResources();
        String text = String.format(res.getString(R.string.snippet_number), snippetNumber);
        edtSnippet.setText(text);

        edtCurrentBeat.setText(String.valueOf(currentBeat));
        if (currentBeat != null && currentBeat == 1) {
            edtCurrentBeat.setTextColor(getResources().getColor(R.color.accent));
        } else {
            edtCurrentBeat.setTextColor(getResources().getColor(R.color.primary_text));
        }

        edtBPM.setText(String.valueOf(bpm));
        edtCurrentBar.setText((beatsPerBar) + "/" + noteValue);
        edtBarCount.setText(barsPassed + "/" + barCount);
    }

    public static SongsListFragment newInstance() {
        SongsListFragment frag = new SongsListFragment();
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_programs_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        btnStopCurrent.setOnClickListener(view1 -> stopSong());
    }

    private void stopSong() {
        Utils.checkAndStopService(getContext());
        Runtime.getRuntime().gc();
        viewPlayback.setVisibility(View.GONE);
        viewPlayback.setKeepScreenOn(false);
        activity.getSlidingTabLayout().setVisibility(View.VISIBLE);
        activity.getViewPager().setPagingEnabled(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.addBackPressedListenr(this);
        mLayoutManager = new LinearLayoutManager(activity);
        rvPrograms.setLayoutManager(mLayoutManager);
        rvPrograms.setOnLongClickListener(view -> {
            // TODO remove item ??? Possible ??
            return false;
        });
        fabNew.setOnClickListener(view -> startActivity(new Intent(activity, SongActivity.class)));
        dataSource = new SongsDataSource(activity);
        dataSource.open();
        getSongs();
    }

    @Override
    protected void onStopMetronome() {
        stopSong();
    }


    private void getSongs() {
        songs = new ArrayList<>();
        dataSource.open();

        //debug
        dataSource.showAllSnippets();

        songs = dataSource.getAllSongs();
        for (Song song : songs) {
            Log.d(this.getClass().getSimpleName(), song.toString());
        }
        mAdapter = new ProgramsAdapter(activity, songs, this);
        rvPrograms.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        if (viewPlayback.getVisibility() == View.VISIBLE) {
            stopSong();
        } else {
            activity.superBack();
        }
    }

    @Override
    public void onPlayClicked(Song song) {
        activity.getSlidingTabLayout().setVisibility(View.GONE);
        activity.getViewPager().setPagingEnabled(false);
        viewPlayback.setVisibility(View.VISIBLE);
        viewPlayback.setKeepScreenOn(true);

        if (!Utils.isMetronomeServiceRunning(getContext())) {
            Intent serviceIntent = new Intent(getContext(), MetronomeService.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(MetronomeService.SNIPPETS_KEY, song.getSnippets());
            serviceIntent.putExtras(bundle);
            getContext().startService(serviceIntent);
        }

        issueServiceNotification();
    }

    @Override
    public void onEditClicked(Song song) {
        Intent intent = new Intent(getContext(), SongActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SongActivity.SONG_TAG, song);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }

    @Override
    public void onResume() {
        getSongs();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }
}
