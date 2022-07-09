package com.example.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    Context context;
    String userName;
    List<String> userList;
    //access to db
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    public UsersAdapter(Context context, String userName, List<String> userList) {
        this.context = context;
        this.userName = userName;
        this.userList = userList;

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_users_design,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                                         //using the users id list to get each user data
        databaseReference.child("Users").child(userList.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String otherName = snapshot.child("userName").getValue().toString();
                String userImageUri = snapshot.child("image").getValue().toString();
                holder.textViewUserName.setText(otherName);
                if(userImageUri.equals("")){
                    holder.circleImageView.setImageResource(R.drawable.account);
                }
                else {
                    Picasso.get().load(userImageUri).fit().into(holder.circleImageView);
                }
                //pass value to the chat room
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iOpenChatAct = new Intent(context,ChatActivity.class);
                        iOpenChatAct.putExtra("user_name",userName);
                        iOpenChatAct.putExtra("other_name",otherName);
                        context.startActivity(iOpenChatAct);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Picasso.get().load().fit().into(holder.circleImageView);
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private CardView cardView;
        private TextView textViewUserName;
        private CircleImageView circleImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textViewUserName = itemView.findViewById(R.id.textViewProfileUsersName);
            circleImageView = itemView.findViewById(R.id.circleImageViewProfileUsers);
        }
    }
}
