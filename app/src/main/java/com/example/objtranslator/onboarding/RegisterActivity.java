package com.example.objtranslator.onboarding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.objtranslator.R;
import com.example.objtranslator.main.MainActivity;
import com.example.objtranslator.main.ObjectTranslatorActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText mName, mEmail, mPassword, mPassword2;
    Button mSignupBtn;
    TextView mGotoLogin;
    FirebaseAuth fAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Set variables
        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPassword2 = findViewById(R.id.password2);
        mSignupBtn = findViewById(R.id.signupBtn);
        mGotoLogin = findViewById(R.id.haveAccount);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String name = mName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String password2 = mPassword2.getText().toString().trim();

                //Check if inputs are valid
                if(TextUtils.isEmpty(name)) {
                    mName.setError("Name is Required.");
                    return;
                }
                if(TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required.");
                    return;
                }
                if(TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required.");
                    return;
                }
                if(password.length() < 6) {
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }
                if(TextUtils.isEmpty(password2)) {
                    mPassword2.setError("Confirm your password");
                    return;
                }
                if(!password.equals(password2)) {
                    mPassword2.setError("Passwords don't match");
                    return;
                }

                //Input is valid, register user in firebase
                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "User Created.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    Toast.makeText(RegisterActivity.this,
                                                   "Error! " + task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                }
                            }
                    });
                }
            });
        }
}