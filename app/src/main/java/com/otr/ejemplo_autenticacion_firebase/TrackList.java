package com.otr.ejemplo_autenticacion_firebase;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TrackList extends ArrayAdapter<Track> {

    private Activity context;
    private List<Track> tracks;

    public TrackList(Activity context, List<Track> tracks){
        super(context, R.layout.layout_track_list, tracks);
        this.context = context;
        this.tracks = tracks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.layout_track_list, null, true);

        TextView textViewName = listViewItem.findViewById(R.id.textViewNameTrack);
        TextView textViewRating = listViewItem.findViewById(R.id.textViewRating);

        Track track = tracks.get(position);

        textViewName.setText(track.getTrackName());
        textViewRating.setText("Puntuación: " + String.valueOf(track.getTrackRating()));

        return listViewItem;
    }
}
