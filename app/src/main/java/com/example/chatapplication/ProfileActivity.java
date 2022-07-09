package com.example.chatapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivityProfileBinding;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding profileBinding;
    boolean imageControl = false;
    boolean galleryOpen = false;
    Uri imageUri;
    String oldImage;

    FirebaseStorage firebaseStorage; //for storage db
    StorageReference storageReference;

    FirebaseDatabase database;
    DatabaseReference reference;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(profileBinding.getRoot());

        //for realtime db
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
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
                                Picasso.get().load(imageUri).fit().into(profileBinding.circleImageViewProfile);
                                imageControl = true;
                                galleryOpen = false;
                            }
                        }
                    }
                });
        getUserInfo();

        profileBinding.circleImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryOpen = true;
                Intent iGetImage = new Intent();
                iGetImage.setType("image/*");
                iGetImage.setAction(Intent.ACTION_GET_CONTENT);
                launcher.launch(iGetImage);
            }
        });
        profileBinding.btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
            }
        });


    }

    private void updateUserInfo() {
        //new infon and save to realtime db and storage
        //update userName
        String userName = profileBinding.editTextProfileName.getText().toString();
        reference.child("Users").child(user.getUid()).child("userName").setValue(userName);
        //update userImage
        if (imageControl) {
            //save to firebase storage
            UUID randomID = UUID.randomUUID(); //for uniqueness of image of each user
            String imageName = "images/" + randomID + ".jpg";
            storageReference.child(imageName).putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //link us to the storage path
                            StorageReference myStorageRef = firebaseStorage.getReference(imageName);
                            myStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d("success :", "true");
                                    String imageLink = uri.toString();
                                    //save it to real time db
                                    reference.child("Users").child(auth.getUid()).child("image").setValue(imageLink)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(ProfileActivity.this, "Success upload image to real time db", Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ProfileActivity.this, "Failed upload image to real time db", Toast.LENGTH_LONG).show();
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
        } else {
            //here is different, if user image null, then print use image
            reference.child("Users").child(auth.getUid()).child("image").setValue(oldImage);
        }
        Intent iMain = new Intent(ProfileActivity.this, MainActivity.class);
        iMain.putExtra("user_name", userName);
        startActivity(iMain);
        finish();
    }

    private void getUserInfo() {


        reference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tmp = snapshot.child("userName").getValue().toString(); // oldone
                oldImage = snapshot.child("image").getValue().toString();
                profileBinding.editTextProfileName.setText(tmp); // load to the UI

                if (oldImage.equals("null")) {
                    //change to default
                    profileBinding.circleImageViewProfile.setImageResource(R.drawable.account);
                } else {
                    Picasso.get().load(oldImage).fit().into(profileBinding.circleImageViewProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}