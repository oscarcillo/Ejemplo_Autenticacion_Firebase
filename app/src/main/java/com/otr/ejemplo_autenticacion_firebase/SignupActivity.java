package com.otr.ejemplo_autenticacion_firebase;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //
        editTextEmail = findViewById(R.id.textoCorreoRegistro);
        editTextPassword = findViewById(R.id.textoContrasenaRegistro);
        //
        mAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(View v){
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

        mAuth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "El usuario se ha registrado",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
