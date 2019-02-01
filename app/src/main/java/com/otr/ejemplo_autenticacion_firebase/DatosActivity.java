package com.otr.ejemplo_autenticacion_firebase;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DatosActivity extends AppCompatActivity {

    public static final String ARTIST_NAME = "artistname";
    public static final String ARTIST_ID = "artistid";

    EditText editTextName;
    Button buttonAdd;
    Spinner spinnerGenres;
    ListView listViewArtists;
    ProgressBar progreso;

    DatabaseReference databaseArtists;
    DatabaseReference databaseTracks;

    List<Artist> artistList;
    List<Integer> numberSongList;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);
        //
        editTextName = findViewById(R.id.editTextName);
        buttonAdd = findViewById(R.id.buttonAddArtist);
        spinnerGenres = findViewById(R.id.spinnerGenres);
        listViewArtists = findViewById(R.id.listViewArtists);
        progreso = findViewById(R.id.progressBar);
        //
        databaseArtists = FirebaseDatabase.getInstance().getReference("artists");
        //
        artistList = new ArrayList<>();
        numberSongList = new ArrayList<>();

        //iniciar nueva actividad al seleccionar un artista
        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = artistList.get(position);
                Intent i = new Intent(getApplicationContext(), AddTrackActivity.class);
                i.putExtra(ARTIST_ID, artist.getArtistId());
                i.putExtra(ARTIST_NAME, artist.getArtistName());
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseArtists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                artistList.clear();
                progreso.setVisibility(View.VISIBLE);

                for(DataSnapshot artistSnapshot: dataSnapshot.getChildren()){
                    Artist artist = artistSnapshot.getValue(Artist.class);
                    artistList.add(artist);
                }
                ArtistList adapter = new ArtistList(DatosActivity.this, artistList);
                listViewArtists.setAdapter(adapter);
                progreso.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progreso.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void addArtist(View v){
        String name = editTextName.getText().toString().trim();
        String genre = spinnerGenres.getSelectedItem().toString();

        if(!TextUtils.isEmpty(name)) {
            String id = databaseArtists.push().getKey();
            Artist artist = new Artist(id, name, genre);
            databaseArtists.child(id).setValue(artist);
            Toast.makeText(this, "Artista añadido", Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(this, "Tienes que introducir un nombre",
                    Toast.LENGTH_SHORT).show();
    }

    public void volver(View v){
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
        finish();
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
}
