package com.otr.ejemplo_autenticacion_firebase;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;
    ProgressBar barraProgreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        mAuth = FirebaseAuth.getInstance();
        //
        editTextEmail = findViewById(R.id.textoCorreo);
        editTextPassword = findViewById(R.id.textoContrasena);
        //
        barraProgreso = findViewById(R.id.barraprogresomain);
    }

    @Override
    public void onStart(){
        super.onStart();

        if(mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
    }

    /**
     * Método para cambiar entre actividades
     * @param v Vista que activa este método
     */
    public void cambiarActividad(View v){
        switch(v.getId()){
            case R.id.textoRegistro:{
                Intent i = new Intent(this, SignupActivity.class);
                startActivity(i);
                finish();
            }break;
            case R.id.textoVolver:{
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            }break;
        }


    }//..

    /**
     * Método para loguear al usuario
     * @param v Vista que activa este método
     */
    public void userLogin(View v){
        hideKeyboard(this);
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        //comprueba que el email y la contraseña no estan vacias
        if(email.isEmpty()){
            editTextEmail.setError("Es necesario introducir un email");
            editTextEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            editTextPassword.setError("Es necesario introducir una contraseña");
            editTextPassword.requestFocus();
            return;
        }
        //comprueba que el email introducido tiene una estructura correcta
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("El email introducido no es valido");
            editTextEmail.requestFocus();
            return;
        }
        //comprueba que la contraseña tiene al menos 6 caracteres
        if(password.length()<6){
            editTextPassword.setError("Introduce una contraseña con al menos 6 caracteres");
            editTextPassword.requestFocus();
            return;
        }

        barraProgreso.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    barraProgreso.setVisibility(View.INVISIBLE);

                    //comprobar que el usuario tiene el email verificado
                    if(mAuth.getCurrentUser().isEmailVerified()){
                        Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                        getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                    else
                        Toast.makeText(getApplicationContext(),
                                "Esta cuenta aun no ha sido verificada",
                                Toast.LENGTH_SHORT).show();
                }
                else{
                    barraProgreso.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),
                            "El usuario no existe o datos mal introducidos",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Método para ocultar el teclado
     * @param activity Contexto
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
