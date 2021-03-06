package com.example.is1305project.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.is1305project.MessageActivity;
import com.example.is1305project.R;
import com.example.is1305project.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> listUser;

    public UserAdapter(Context mContext, List<User> listUser){
        this.mContext = mContext;
        this.listUser = listUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        final User user = listUser.get(position);
        holder.username.setText(user.getUsername());
        Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);

        holder.lastMessageInfo.setVisibility(View.GONE);
        holder.imageOn.setVisibility(View.GONE);
        holder.imageOff.setVisibility(View.GONE);


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
        private ImageView imageOn;
        private ImageView imageOff;
        private LinearLayout lastMessageInfo;

        public ViewHolder(View itemView){
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            imageOn = itemView.findViewById(R.id.image_on);
            imageOff = itemView.findViewById(R.id.image_off);
            lastMessageInfo = itemView.findViewById(R.id.last_message_info);
        }
    }
}
