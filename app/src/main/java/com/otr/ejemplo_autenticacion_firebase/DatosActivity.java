package com.otr.ejemplo_autenticacion_firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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

public class DatosActivity extends AppCompatActivity {

    public static final String ARTIST_NAME = "artistname";
    public static final String ARTIST_ID = "artistid";

    EditText editTextName;
    Button buttonAdd;
    Spinner spinnerGenres;
    ListView listViewArtists;
    ProgressBar progreso;

    DatabaseReference databaseArtists;

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

        //
        spinnerGenres = findViewById(R.id.spinnerGenres);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        cargarGeneros()); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinnerGenres.setAdapter(spinnerArrayAdapter);
        //

        listViewArtists = findViewById(R.id.listViewArtists);
        progreso = findViewById(R.id.progressBar);
        //
        databaseArtists = FirebaseDatabase.getInstance().getReference("artists").child(mAuth.getUid());
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

        //mostrar dialogo al hacer una pulsacion larga en el spinner
        listViewArtists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = artistList.get(position);
                int posGenre = 0;

                if(artist.getArtistGenre().equals("Rock"))
                    posGenre = 0;
                else if(artist.getArtistGenre().equals("Punk"))
                    posGenre = 1;
                else if(artist.getArtistGenre().equals("Blues"))
                    posGenre = 2;
                else if(artist.getArtistGenre().equals("Electronica"))
                    posGenre = 3;


                showUpdateDialog(artist.getArtistId(), artist.getArtistName(), posGenre);
                return true;
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

    private void showUpdateDialog(final String artistId, String artistName, int genre){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);
        //
        final EditText editTextName = dialogView.findViewById(R.id.editTextNameDialog);
            editTextName.setText(artistName);
        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdate);
        final Spinner spinner = dialogView.findViewById(R.id.spinnerGenresDialog);
            spinner.setSelection(genre);
            final Button buttonDelete = dialogView.findViewById(R.id.buttonDelete);
        //
        dialogBuilder.setTitle("Actualizar artista " + artistName);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = editTextName.getText().toString().trim();
                if(!TextUtils.isEmpty(editTextName.toString())){
                    updateArtist(artistId, nombre, spinner.getSelectedItem().toString());
                            alertDialog.dismiss();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "El nombre del artista no puede estar vacio",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteArtist(artistId);
                Toast.makeText(getApplicationContext(),
                        "El artista y sus canciones han sido eliminadas",
                        Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });
    }

    private boolean updateArtist(String id, String name, String genre){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("artists").child(mAuth.getUid()).child(id);

        Artist artist = new Artist(id, name, genre);
        Log.e("1", artist.getArtistName() + " - " + artist.getArtistGenre());
        db.setValue(artist);
        Toast.makeText(this, "Artista actualizado", Toast.LENGTH_SHORT).show();

        return true;
    }

    private boolean deleteArtist(String id){
        //borrar artista
        DatabaseReference dbArtists = FirebaseDatabase.getInstance().getReference("artists").child(mAuth.getUid()).child(id);
        dbArtists.removeValue();
        //borrar canciones del artista
        DatabaseReference dbTracks = FirebaseDatabase.getInstance().getReference("tracks").child(id);
        dbTracks.removeValue();
        return true;
    }

    private String[] cargarGeneros(){
        return new String[4];
    }
}
