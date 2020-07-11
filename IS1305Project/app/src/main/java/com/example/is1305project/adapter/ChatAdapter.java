package com.example.is1305project.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private Context mContext;
    private List<User> listUser;

    String userLastMessage;
    long userLastMessageTime;
    boolean isNotSeen;
    public ChatAdapter(Context mContext, List<User> listUser){
        this.mContext = mContext;
        this.listUser = listUser;
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
        holder.linearLayout.setVisibility(View.VISIBLE);

        // set active icon
        if(user.getStatus() != null){
            if(user.getStatus().equals("online")){
                holder.imageOn.setVisibility(View.VISIBLE);
                holder.imageOff.setVisibility(View.GONE);
            }else{
                holder.imageOn.setVisibility(View.GONE);
                holder.imageOff.setVisibility(View.VISIBLE);
            }
        }

        // set user image and name
        holder.username.setText(user.getUsername());
        Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        displayLastMessage(user.getId(), holder.lastMessage, holder.lastMessageTime, holder.username);


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

    private void displayLastMessage(final String userid, final TextView lastMessage, final TextView lastMessageTime,
                                    final TextView username){
        userLastMessage = "";
        userLastMessageTime = 0;
        isNotSeen = false;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        if(firebaseUser != null){
            reference.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if( chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                            if(chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid()) ){
                                userLastMessage = "You: " + chat.getMessage();
                            }else{
                                userLastMessage = chat.getMessage();
                                // check if chat have any unseen message. If not change color to black
                                if(!chat.isIsSeen()){
                                    username.setTextColor(Color.BLACK);
                                    username.setTypeface(null, Typeface.BOLD);
                                    lastMessage.setTextColor(Color.BLACK);
                                    lastMessage.setTypeface(null, Typeface.BOLD);
                                }
                            }
                            userLastMessageTime = chat.getTime();
                        }
                    }

                    lastMessage.setText(fixLastMessage(userLastMessage));
                    lastMessageTime.setText("-  " + convertTime(userLastMessageTime));

                    userLastMessage = "";
                    userLastMessageTime = 0;
                    isNotSeen = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}
