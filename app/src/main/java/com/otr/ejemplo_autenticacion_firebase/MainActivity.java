package com.otr.ejemplo_autenticacion_firebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
