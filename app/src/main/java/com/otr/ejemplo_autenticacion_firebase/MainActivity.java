package com.otr.ejemplo_autenticacion_firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
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
        barraProgreso = (ProgressBar) findViewById(R.id.barraprogresomain);
    }

    public void cambiarActividad(View v){
        switch(v.getId()){
            case R.id.textoRegistro:{
                Intent i = new Intent(this, SignupActivity.class);
                startActivity(i);
            }break;
            case R.id.textoVolver:{
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            }break;
        }


    }//..

    public void userLogin(View v){
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
                    Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                    getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                else{
                    barraProgreso.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
