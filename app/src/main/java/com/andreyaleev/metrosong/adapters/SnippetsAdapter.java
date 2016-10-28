package com.andreyaleev.metrosong.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.metronome.SongSnippet;
import com.andreyaleev.metrosong.tools.SnippetRemoveListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import pntanasis.android.metronome.Beats;
import pntanasis.android.metronome.NoteValues;

/**
 * Created by Andrey Aleev on 15.09.2015.
 */
public class SnippetsAdapter extends RecyclerView.Adapter<SnippetsAdapter.ViewHolder> {

    private ArrayList<SongSnippet> snippets;
    private Context mContext;
    private SnippetRemoveListener snippetRemoveListener;

    public SnippetsAdapter(Context mContext, ArrayList<SongSnippet> snippets, SnippetRemoveListener listener) {
        this.mContext = mContext;
        this.snippets = snippets;
        this.snippetRemoveListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_snippet, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        ArrayAdapter<Beats> arrayBeats = new ArrayAdapter<>(mContext, R.layout.spinner_item_white, Beats.values());
        vh.spinnerBeat.setAdapter(arrayBeats);
        arrayBeats.setDropDownViewResource(R.layout.spinner_dropdown);
        ArrayAdapter<NoteValues> noteValues = new ArrayAdapter<>(mContext, R.layout.spinner_item_white, NoteValues.values());
        vh.spinnerNote.setAdapter(noteValues);
        noteValues.setDropDownViewResource(R.layout.spinner_dropdown);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SongSnippet snippet = getItem(position);
        holder.edtBPM.setText(String.valueOf(snippet.getBpm()));
        holder.edtBPM.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    snippets.get(position).setBpm(Integer.parseInt(charSequence.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        holder.edtBarsCount.setText("" + snippet.getBarsCount());
        holder.edtBarsCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    snippets.get(position).setBarCount(Integer.parseInt(charSequence.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        holder.spinnerBeat.setSelection((snippet.getBeatsPerBar() - 1));
        holder.spinnerBeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Beats beat = (Beats) adapterView.getItemAtPosition(i);
                short beats = beat.getNum();
                if (beats > snippet.getNoteValue()) {
                    beats = (short) snippet.getNoteValue(); // TODO fixme =(
                }
                getItem(position).setBeatsPerBar(beats);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        holder.ibTrash.setOnClickListener(view -> {
            snippets.remove(position);
            if (snippetRemoveListener != null) {
                snippetRemoveListener.onSnippetRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return snippets.size();
    }

    public SongSnippet getItem(int position) {
        return snippets.get(position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.spinnerBeat)
        Spinner spinnerBeat;
        @BindView(R.id.spinnerNote)
        Spinner spinnerNote;
        @BindView(R.id.edtBPM)
        EditText edtBPM;
        @BindView(R.id.edtBarsCount)
        EditText edtBarsCount;
        @BindView(R.id.ibTrash)
        ImageButton ibTrash;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

}
