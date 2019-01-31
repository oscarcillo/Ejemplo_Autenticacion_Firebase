package com.otr.ejemplo_autenticacion_firebase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatosActivity extends AppCompatActivity {

    EditText editTextName;
    Button buttonAdd;
    Spinner spinnerGenres;

    DatabaseReference databaseArtists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);
        //
        editTextName = findViewById(R.id.editTextName);
        buttonAdd = findViewById(R.id.buttonAddArtist);
        spinnerGenres = findViewById(R.id.spinnerGenres);
        //
        databaseArtists = FirebaseDatabase.getInstance().getReference("artists");
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

}
