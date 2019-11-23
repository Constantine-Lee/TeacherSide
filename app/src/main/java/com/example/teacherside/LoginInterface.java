package com.example.teacherside;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginInterface extends AppCompatActivity {

    Toolbar mLoginInterface_toolbar;
    ProgressBar mLoginInterface_progressBar;

    EditText mLoginInterface_editText_userEmail;
    EditText mLoginInterface_editText_userPassword;
    Button mLoginInterface_button_userLogin;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_interface);

        mLoginInterface_toolbar = findViewById(R.id.LoginInterface_toolbar);
        mLoginInterface_progressBar = findViewById(R.id.LoginInterface_progressBar);

        mLoginInterface_editText_userEmail = (EditText) findViewById(R.id.LoginInterface_editTest_userEmail);
        mLoginInterface_editText_userPassword = (EditText) findViewById(R.id.LoginInterface_editText_userPassword);
        mLoginInterface_button_userLogin = (Button) findViewById(R.id.LoginInterface_button_userLogin);

        mLoginInterface_toolbar.setTitle("Login");

        firebaseAuth = FirebaseAuth.getInstance();

        mLoginInterface_button_userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginInterface_progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.signInWithEmailAndPassword(mLoginInterface_editText_userEmail.getText().toString(),
                        mLoginInterface_editText_userPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mLoginInterface_progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                startActivity(new Intent(LoginInterface.this, UnitsInterface.class));
                            }else{
                                Toast.makeText(LoginInterface.this, "Please verify your email address"
                                        , Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(LoginInterface.this, task.getException().getMessage()
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
