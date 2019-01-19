package com.otr.ejemplo_autenticacion_firebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.Continuation;
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
    Button botonEnviar;

    Uri uriProfileImage;
    ProgressBar progreso;
    String profileImageUrl;

    FirebaseAuth mAuth;
    //
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //
        imagen = findViewById(R.id.imagenCamara);
        progreso = findViewById(R.id.barraprogresoimagen);
        nombreUsuario = findViewById(R.id.textoNombreUsuario);
        botonEnviar = findViewById(R.id.botonGuardar);
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            if(user.getPhotoUrl() != null){
                progreso.setVisibility(View.VISIBLE);
                //cargar de nuevo la url de la imagen en la variable para luego subirla a servidor
                profileImageUrl = user.getPhotoUrl().toString();
                //
                Glide.with(this).load(user.getPhotoUrl()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        progreso.setVisibility(View.INVISIBLE);
                        return false;
                    }
                }).into(imagen);
            }
            if(user.getDisplayName() != null){
                nombreUsuario.setText(user.getDisplayName());
            }
        }
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
        //subir la imagen
        uploadImageToFirebaseStorage();
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
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        url = "profilepics/"+System.currentTimeMillis() + ".jpg";
        final StorageReference riversRef = storageRef.child(url);
        UploadTask uploadTask = riversRef.putFile(uriProfileImage);

        botonEnviar.setEnabled(false);
        progreso.setVisibility(View.VISIBLE);
        imagen.setAlpha(0.3f);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("1", exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("1", "La imagen se ha subido a Firebase");
            }
        });
        //
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    profileImageUrl = task.getResult().toString();
                    botonEnviar.setEnabled(true);
                    progreso.setVisibility(View.INVISIBLE);
                    imagen.setAlpha(1f);
                }
            }
        });
    }

    /**
     * Método que sube la información del usuario (imagen, nombre de usuario) al Storage de Firebase
     */
    public void saveUserInformation(View v){
        String displayName = nombreUsuario.getText().toString();
        MainActivity.hideKeyboard(this);

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

    /**
     * Método que cierra la sesión de usuario actual y te lleva a la actividad principal
     * @param v Vista que activa este método
     */
    public void cerrarSesion(View v){
        mAuth.signOut();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
