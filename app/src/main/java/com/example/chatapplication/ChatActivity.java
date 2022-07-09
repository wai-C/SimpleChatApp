package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ImageView imageViewBack;
    EditText editTextTextMultiLineMsg;
    TextView textViewChat;
    FloatingActionButton fab;
    RecyclerView recycleViewChat;

    String userNameComing,  otherNameComing;
    MessageAdapter msgAdapter;
    List<ModelClass> list;


    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        imageViewBack = findViewById(R.id.imageViewBack);
        editTextTextMultiLineMsg = findViewById(R.id.editTextTextMultiLineMsg);
        textViewChat = findViewById(R.id.textViewChat);
        fab = findViewById(R.id.fab);

        recycleViewChat = findViewById(R.id.recycleViewChat);
        recycleViewChat.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();

        //will use two design , receive and send msg
        userNameComing = getIntent().getStringExtra("user_name");
        otherNameComing = getIntent().getStringExtra("other_name");

        textViewChat.setText(otherNameComing);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iBack = new Intent(ChatActivity.this,MainActivity.class);
                startActivity(iBack);
            }
        });

        //send
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputMsg = editTextTextMultiLineMsg.getText().toString();
                if(!inputMsg.equals(""))
                {
                    //send out
                    sendMsg(inputMsg);
                    editTextTextMultiLineMsg.setText(""); //clear after sent
                }
            }
        });
        getMsg();
    }

    private void sendMsg(String msg){
        //different key for each msg, avoid delete other person msg
        //so using push , get
        final String key = reference.child("Messages").child(userNameComing).child(otherNameComing).push().getKey().toString();
        //msg it self, and sender of the msg, using Map array
        final Map<String,Object> msgMap = new HashMap<>();
        msgMap.put("message",msg);
        msgMap.put("from",userNameComing);

        //saving msg for the other side also
        reference.child("Messages").child(userNameComing).child(otherNameComing).child(key)
                .setValue(msgMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    reference.child("Messages").child(otherNameComing).child(userNameComing)
                            .child(key).setValue(msgMap);
                }
            }
        });
    }
    public void getMsg(){
        reference.child("Messages").child(userNameComing).child(otherNameComing).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ModelClass modelClass = snapshot.getValue(ModelClass.class); //get into this model obj
                list.add(modelClass);
                msgAdapter.notifyDataSetChanged();
                //last ms sent, will always on the screen
                recycleViewChat.scrollToPosition(list.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        msgAdapter = new MessageAdapter(list,userNameComing);
        recycleViewChat.setAdapter(msgAdapter);
    }

    //or add  <uses-permission android:name="android.permission.READ_PHONE_STATE"/>


    //help use to flight with (greylist-max-q,core-platform-api, reflection, denied)
    /*
    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            int res = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);
            if (res != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, 123);
            }

        }
    }

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1002;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "READ_PHONE_STATE Denied", Toast.LENGTH_SHORT)
                            .show();
                } else {
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }*/
}