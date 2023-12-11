package com.example.objtranslator;

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

//import com.example.objtranslator.main.GuestActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText mName, mEmail, mPassword, mPassword2;
    Button mSignupBtn;
    TextView mGotoLogin, guest;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPassword2 = findViewById(R.id.password2);
        mSignupBtn = findViewById(R.id.signupBtn);
        mGotoLogin = findViewById(R.id.haveAccount);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String password2 = mPassword2.getText().toString().trim();

                if (!validateInputs(mName, mEmail, mPassword, mPassword2, name, email, password, password2)) return;
                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE); // Hide progress bar
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "User Created.", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();
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
        });

        mGotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }


    private boolean validateInputs(EditText mName, EditText mEmail, EditText mPassword, EditText mPassword2,
                                String name, String email, String password, String password2) {
        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            mName.setError("Name is Required.");
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email is Required.");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password is Required.");
            valid = false;
        }
        if (password.length() < 6) {
            mPassword.setError("Password Must be >= 6 Characters");
            valid = false;
        }
        if (TextUtils.isEmpty(password2)) {
            mPassword2.setError("Confirm your password");
            valid = false;
        }
        if (!password.equals(password2)) {
            mPassword2.setError("Passwords don't match");
            valid = false;
        }
        return valid;
    }
}