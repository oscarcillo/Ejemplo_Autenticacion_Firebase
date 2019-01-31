package com.otr.ejemplo_autenticacion_firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddTrackActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    TextView textViewArtistName;
    EditText editTextTrackname;
    SeekBar seekBarRating;
    Button buttonAddTrack;
    ProgressBar progeso;
    TextView numeroRating;

    ListView listViewTracks;

    DatabaseReference databaseTracks;

    List<Track> tracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_track);
        //
        Intent i = getIntent();
        textViewArtistName = findViewById(R.id.textViewArtistName);
        editTextTrackname = findViewById(R.id.editTextTrackName);
        seekBarRating = findViewById(R.id.seekBarRating);
        buttonAddTrack = findViewById(R.id.buttonAddTrack);
        progeso = findViewById(R.id.progressBarTrack);
        listViewTracks = findViewById(R.id.listViewTracks);
        numeroRating = findViewById(R.id.textNumberRating);
        //establecer nombre del artista
        textViewArtistName.setText(i.getStringExtra(DatosActivity.ARTIST_NAME));
        String id = i.getStringExtra(DatosActivity.ARTIST_ID);
        //
        databaseTracks = FirebaseDatabase.getInstance().getReference("tracks").child(id);
        //
        tracks = new ArrayList<>();

        //cambiar numero del rating
        seekBarRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numeroRating.setText(""+progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //
        databaseTracks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tracks.clear();
                for(DataSnapshot trackSnapshot : dataSnapshot.getChildren()){
                    Track track = trackSnapshot.getValue(Track.class);
                    tracks.add(track);
                }
                TrackList adapter = new TrackList(AddTrackActivity.this, tracks);
                listViewTracks.setAdapter(adapter);
                //
                progeso.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progeso.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void addTrack(View v){
        String trackName = editTextTrackname.getText().toString().trim();
        int rating = seekBarRating.getProgress();

        if(!TextUtils.isEmpty(trackName)){
            String id = databaseTracks.push().getKey();
            Track track = new Track(id, trackName, rating);
            databaseTracks.child(id).setValue(track);
            Toast.makeText(this, "Canción añadida", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Debes introducir un nombre de canción",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //Barra superior con opcion de cerrar sesion
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mAuth.signOut();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
        return true;
    }

    public void volver(View v){
        Intent i = new Intent(this, DatosActivity.class);
        startActivity(i);
        finish();
    }
}
