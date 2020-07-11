package com.example.is1305project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.is1305project.R;
import com.example.is1305project.model.User;

import java.util.ArrayList;
import java.util.List;

public class AddUserAdapter extends RecyclerView.Adapter<AddUserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> listUser;

    public interface OnItemCheckListener {
        void onItemCheck(User user);
        void onItemUncheck(User user);
    }


    public List<User> addedUser = new ArrayList<>();

    @NonNull
    private OnItemCheckListener onItemClick;

    public AddUserAdapter(Context mContext, List<User> listUser, @NonNull OnItemCheckListener onItemCheckListener){
        this.mContext = mContext;
        this.listUser = listUser;
        this.onItemClick = onItemCheckListener;
    }

    @NonNull
    @Override
    public AddUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(mContext).inflate(R.layout.add_user_item, parent, false);
        return new AddUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final User user = listUser.get(position);

        // set user image and name
        holder.username.setText(user.getUsername());
        Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);

        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked( !holder.checkBox.isChecked());
                if(holder.checkBox.isChecked()){
                    onItemClick.onItemCheck(user);
                }else{
                    onItemClick.onItemUncheck(user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView profile_image;
        public CheckBox checkBox;
        public View itemView;

        public ViewHolder(View itemView){
            super(itemView);
            this.itemView = itemView;
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            checkBox = itemView.findViewById(R.id.checkedAdd);
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }
    }
}
