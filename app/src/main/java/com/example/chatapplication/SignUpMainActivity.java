package com.example.chatapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivitySignUpMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

public class SignUpMainActivity extends AppCompatActivity {

    private ActivitySignUpMainBinding signUpMainBinding;
    boolean imageControl = false;

    Uri imageUri;
    FirebaseDatabase database;  //save to fb realtime
    DatabaseReference databaseReference;
    FirebaseAuth auth;

    boolean galleryOpen =false;
    FirebaseStorage firebaseStorage; //for storage db
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signUpMainBinding = ActivitySignUpMainBinding.inflate(getLayoutInflater());
        setContentView(signUpMainBinding.getRoot());
        //setContentView(R.layout.activity_sign_up_main);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() != null) {
                            if (galleryOpen) {
                                Intent galleryIntent = result.getData();
                                imageUri = galleryIntent.getData();
                                //set to imageview
                                Picasso.get().load(imageUri).fit().into(signUpMainBinding.circleImageView);
                                imageControl = true;
                                galleryOpen = false;
                            }
                        }
                    }
                });
        signUpMainBinding.circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryOpen = true;
                Intent iGetImage = new Intent();
                iGetImage.setType("image/*");
                iGetImage.setAction(Intent.ACTION_GET_CONTENT);
                launcher.launch(iGetImage);
            }
        });
        signUpMainBinding.btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signUpMainBinding.editTextSignUpE.getText().toString();
                String pw = signUpMainBinding.editTextSignUpPw.getText().toString();
                String un = signUpMainBinding.editTextSignUpUN.getText().toString();
                if(!email.equals("")&&!pw.equals("")&&!un.equals("")){
                    signUp(email,pw,un);
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && requestCode == RESULT_OK && data.getData() !=null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(signUpMainBinding.circleImageView);
            imageControl = true;
        }
        else {
            imageControl = false;
        }
    }
    public void signUp(String email, String pw, String un){
        auth.createUserWithEmailAndPassword(email,pw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // parent : users => userid => username and set with un value
                            databaseReference.child("Users").child(auth.getUid()).child("userName")
                                    .setValue(un);
                            //save image to fb
                            if(imageControl){
                                //save to firebase storage
                                UUID randomID = UUID.randomUUID(); //for uniqueness of image of each user
                                String imageName = "images/"+randomID+".jpg";
                                storageReference.child(imageName).putFile(imageUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                //link us to the storage path
                                                StorageReference myStorageRef = firebaseStorage.getReference(imageName);
                                                myStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Log.d("success :","true");
                                                        String filePath = uri.toString();
                                                        //save it to real time db
                                                        databaseReference.child("Users").child(auth.getUid()).child("image").setValue(filePath)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(SignUpMainActivity.this,"Success upload image to real time db",Toast.LENGTH_LONG).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(SignUpMainActivity.this,"Failed upload image to real time db",Toast.LENGTH_LONG).show();
                                                                        e.printStackTrace();
                                                                    }
                                                                });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                                            }
                                        });
                            }
                            else {
                                databaseReference.child("Users").child(auth.getUid()).child("image").setValue("null");
                            }
                            Intent iMain = new Intent(SignUpMainActivity.this,MainActivity.class);
                            startActivity(iMain);
                            finish();
                        }
                        else {
                            Toast.makeText(SignUpMainActivity.this,"Account has created!",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}