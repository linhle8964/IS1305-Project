package com.example.is1305project.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.is1305project.MessageActivity;
import com.example.is1305project.R;
import com.example.is1305project.model.Chat;
import com.example.is1305project.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private Context mContext;
    private List<User> listUser;
    private boolean isChat;

    public ChatAdapter(Context mContext, List<User> listUser, boolean isChat){
        this.mContext = mContext;
        this.listUser = listUser;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position){
        final User user = listUser.get(position);
        holder.username.setText(user.getUsername());
        Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        final List<Chat> userChatHistory = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(user.getId().equals(chat.getSender()) || user.getId().equals(chat.getReceiver())){
                        userChatHistory.add(chat);
                    }
                }
                holder.linearLayout.setVisibility(View.VISIBLE);
                Collections.sort(userChatHistory);
                if(userChatHistory.size() == 0){
                    holder.lastMessage.setText("");
                    holder.lastMessageTime.setText("");
                }else{
                    holder.lastMessage.setText(fixLastMessage(userChatHistory.get(userChatHistory.size() - 1).getMessage()));
                    holder.lastMessageTime.setText(convertTime(userChatHistory.get(userChatHistory.size() - 1).getTime()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(user.getStatus() != null){
            if(isChat){
                if(user.getStatus().equals("online")){
                    holder.imageOn.setVisibility(View.VISIBLE);
                    holder.imageOff.setVisibility(View.GONE);
                }else{
                    holder.imageOn.setVisibility(View.GONE);
                    holder.imageOff.setVisibility(View.VISIBLE);
                }
            }else{
                holder.imageOn.setVisibility(View.GONE);
                holder.imageOff.setVisibility(View.GONE);
            }
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount(){
        return listUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView profile_image;
        public TextView lastMessage;
        public ImageView imageOn;
        public ImageView imageOff;
        public LinearLayout linearLayout;
        public TextView lastMessageTime;

        public ViewHolder(View itemView){
            super(itemView);
            lastMessage = itemView.findViewById(R.id.last_message);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            imageOn = itemView.findViewById(R.id.image_on);
            imageOff = itemView.findViewById(R.id.image_off);
            linearLayout = itemView.findViewById(R.id.last_message_info);
            lastMessageTime = itemView.findViewById(R.id.last_message_time);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String convertTime(long time){
        Date date = new Date(time);
        LocalDateTime currentDate = LocalDateTime.now();
        if(date.getDate() == currentDate.getDayOfMonth()){
            Format format = new SimpleDateFormat("HH:mm");
            return format.format(date);
        }else{
            if(date.getYear() - currentDate.getYear() > 0){
                Format format = new SimpleDateFormat("yyyy/MM/dd ");
                return format.format(date);
            }else{
                Format format = new SimpleDateFormat("MM/dd ");
                return format.format(date);
            }
        }
    }

    private String fixLastMessage(String message){
        message = message.replaceAll("\n+", " ");
        if(message.length() >= 25){
            message = message.substring(0, 24) + "...";
        }
        return message;
    }
}
