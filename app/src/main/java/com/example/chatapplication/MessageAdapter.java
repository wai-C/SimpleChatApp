package com.example.chatapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MsgViewHolder> {

    List<ModelClass> list;
    String userName;
    boolean status;
    int send;
    int receive;


    public MessageAdapter(List<ModelClass> list, String userNameFrom) {
        this.list = list;
        this.userName = userNameFrom;
        status = false;
        send = 1;
        receive = 2;
    }

    @NonNull
    @Override  // we have two design , so use view type
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == send) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_send_design, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_received_design, parent, false);
        }
        return new MsgViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        String tmp = list.get(position).getMessage();
        Log.d("test :", tmp);
        holder.textViewMsg.setText(tmp);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder {

        TextView textViewMsg;

        public MsgViewHolder(@NonNull View itemView) {
            super(itemView);
            if (status) {
                textViewMsg = itemView.findViewById(R.id.textViewSend);
            } else {
                textViewMsg = itemView.findViewById(R.id.textViewReceived);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        //if user send by it self
        if (list.get(position).getFrom().equals(userName)) {
            status = true;
            return send;
        } else {
            status = false;
            return receive;
        }
    }
}
