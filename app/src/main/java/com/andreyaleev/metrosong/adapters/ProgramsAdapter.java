package com.andreyaleev.metrosong.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.metronome.Song;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andrey on 25.10.2016.
 */

public class ProgramsAdapter extends RecyclerView.Adapter<ProgramsAdapter.ViewHolder> {

    private ArrayList<Song> songs;
    private Context mContext;
    private ProgramsItemsListener programsItemsListener;

    public ProgramsAdapter(Context context, ArrayList<Song> songs, ProgramsItemsListener programsItemsListener) {
        this.songs = songs;
        this.mContext = context;
        this.programsItemsListener = programsItemsListener;
    }

    public Song getItem(int position) {
        return songs.get(position);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public ProgramsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_program, parent, false);
        ProgramsAdapter.ViewHolder vh = new ProgramsAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ProgramsAdapter.ViewHolder holder, final int position) {
        final Song song = getItem(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvDescription.setText(String.format(mContext.getString(R.string.snippets), song.getSnippets().size()));
        holder.ivPlayCard.setOnClickListener(view -> {
            if(programsItemsListener!=null){
                programsItemsListener.onPlayClicked(song);
            }
        });
        holder.tvTitle.setOnClickListener(view -> editCard(song));
        holder.tvDescription.setOnClickListener(view -> editCard(song));
        holder.ivEditCard.setOnClickListener(view -> editCard(song));
    }

    private void editCard(Song song) {
        if(programsItemsListener!=null){
            programsItemsListener.onEditClicked(song);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvDescription) TextView tvDescription;
        @BindView(R.id.ivPlayCard)
        ImageView ivPlayCard;
        @BindView(R.id.ivEditCard) ImageView ivEditCard;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public interface ProgramsItemsListener{
        void onPlayClicked(Song song);
        void onEditClicked(Song song);
    }
}
