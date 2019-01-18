package com.otr.ejemplo_autenticacion_firebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;

    ImageView imagen;
    EditText nombreUsuario;

    Uri uriProfileImage;
    ProgressBar progreso;
    String profileImageUrl;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //
        imagen = findViewById(R.id.imagenCamara);
        progreso = findViewById(R.id.barraprogresoimagen);
        nombreUsuario = findViewById(R.id.textoNombreUsuario);
        //
        mAuth = FirebaseAuth.getInstance();
        //cargar informacion del usuario
        loadUserInformation();
    }

    //
    @Override
    public void onStart(){
        super.onStart();
        //volver a la actividad principal si el usuario no está logueado
        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }
    //

    /**
     * Metodo que carga la información del usuario al iniciar la actividad si
     * la imagen de perfil o el nombre de usuario están configurados.
     */
    public void loadUserInformation(){
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            if(user.getPhotoUrl() != null){
                Glide.with(this)
                        .load(user.getPhotoUrl().toString())
                        .into(imagen);
            }
            if(user.getDisplayName() != null){

            }
        }

        //String photoUrl = user.getPhotoUrl().toString();
       // String displayName = user.getDisplayName();
    }

    /**
     * Mostrar la imagen seleccionada en la actividad
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Comprobar que la imagen se ha seleccionado correctamente
        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null
                && data.getData()!=null){
            //almacenar la imagen en una variable
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        uriProfileImage);
                imagen.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mostrar la pantalla de seleccion de imagenes
     * @param v
     */
    public void showImageChooser(View v){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Elige una imagen"), CHOOSE_IMAGE);
    }

    /**
     * Método que sube imagenes de usuario al Storage de Firebase
     */
    public void uploadImageToFirebaseStorage(){
        final StorageReference profileImageRef = FirebaseStorage.getInstance().
                getReference("profilepics/"+System.currentTimeMillis() + ".jpg");

        if(uriProfileImage != null){
            progreso.setVisibility(View.VISIBLE);
            profileImageRef.putFile(uriProfileImage).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progreso.setVisibility(View.INVISIBLE);
                    profileImageUrl = profileImageRef.getDownloadUrl().toString();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progreso.setVisibility(View.INVISIBLE);
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            progreso.setVisibility(View.INVISIBLE);
            profileImageUrl = profileImageRef.getDownloadUrl().toString();
        }
    }

    /**
     * Método que sube la información del usuario (imagen, nombre de usuario) al Storage de Firebase
     */
    public void saveUserInformation(View v){
        String displayName = nombreUsuario.getText().toString();

        if(displayName.isEmpty()) {
            nombreUsuario.setError("El nombre de usuario no puede estar vacío");
            nombreUsuario.requestFocus();
            return;
        }

        if(nombreUsuario.length()<6){
            nombreUsuario.setError("El nombre debe tener al menos 6 caracteres");
            nombreUsuario.requestFocus();
            return;
        }

        //subir imagen a Firebase
        uploadImageToFirebaseStorage();
        //

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null && profileImageUrl != null){
                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().
                        setDisplayName(displayName).setPhotoUri(Uri.parse(profileImageUrl)).build();
                user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ProfileActivity.this,
                                    "El perfil ha sido actualizado",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(ProfileActivity.this,
                                    "El perfil no ha sido actualizado correctamente",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }
}
