package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivityLoginBinding;
import com.example.chatapplication.databinding.ActivityResetPasswordMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPasswordMainActivity extends AppCompatActivity {


    FirebaseDatabase database;  //save to fb realtime
    DatabaseReference databaseReference;
    FirebaseAuth auth;

    private ActivityResetPasswordMainBinding pwBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pwBinding = ActivityResetPasswordMainBinding.inflate(getLayoutInflater());
        setContentView(pwBinding.getRoot());
        //setContentView(R.layout.activity_reset_password_main);

        auth = FirebaseAuth.getInstance();
        pwBinding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp = pwBinding.editTextForget.getText().toString();
                if(!tmp.equals("")){
                    passwordReset(tmp);
                }
            }
        });

    }
    public void passwordReset(String email){
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ResetPasswordMainActivity.this,"Check your email",Toast.LENGTH_LONG).show();
                }
                else {
                    Log.d("failed :","Send failed");
                }
            }
        });
    }
}