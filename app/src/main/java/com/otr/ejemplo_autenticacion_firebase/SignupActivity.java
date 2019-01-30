package com.otr.ejemplo_autenticacion_firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //
        editTextEmail = findViewById(R.id.textoCorreoRegistro);
        editTextPassword = findViewById(R.id.textoContrasenaRegistro);
        //
        mAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.barraprogreso);
    }

    public void registrarUsuario(View v){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "El usuario se ha registrado",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                    //enviar email de verificacion
                    FirebaseUser user = mAuth.getCurrentUser();
                    user.sendEmailVerification();
                    Toast.makeText(getApplicationContext(), "Se ha enviado un correo para verificar tu cuenta",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    progressBar.setVisibility(View.INVISIBLE);
                    if(task.getException() instanceof FirebaseAuthUserCollisionException)
                        Toast.makeText(getApplicationContext(), "El usuario ya está registrado",
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void cambiarActividad(View v) {
        switch (v.getId()) {
            case R.id.textoVolver: {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
            }
            break;
        }
    }
}
