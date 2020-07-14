package com.example.is1305project.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.is1305project.MessageActivity;
import com.example.is1305project.R;
import com.example.is1305project.model.Chat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Chat> listChat;
    private String imageURL;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> listChat, String imageURL) {
        this.mContext = mContext;
        this.listChat = listChat;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        final Chat chat = listChat.get(position);
        holder.showMessage.setText(chat.getMessage());

        holder.showMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showEditMyMessage(chat);
                return false;
            }
        });
        Glide.with(mContext).load(imageURL).into(holder.profileImage);

        boolean check = false;
        for (int i = position; i <= listChat.size() - 1; i++) {
            if (listChat.get(i).getReceiver().equals(firebaseUser.getUid()) && !listChat.get(i).isReceiverRemove()) {
                check = true;
                break;
            }
        }
        if (position == getLastPosition() && chat.getSender().equals(firebaseUser.getUid()) && check == false) {
            if (chat.isIsSeen() == true) {
                holder.txtSeen.setText("Seen");
            } else {
                holder.txtSeen.setText("Delivered");
            }
        } else {
            holder.txtSeen.setVisibility(View.GONE);
        }


        if (chat.isSenderRemove() == true && holder.getItemViewType() == MSG_TYPE_RIGHT) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

        if (chat.isReceiverRemove() == true && holder.getItemViewType() == MSG_TYPE_LEFT) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

    }

    @Override
    public int getItemCount() {
        return listChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView showMessage;
        public ImageView profileImage;
        public TextView txtSeen;

        public ViewHolder(View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.show_message);
            profileImage = itemView.findViewById(R.id.profile_image);
            txtSeen = itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (listChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    private void showEditMyMessage(final Chat chat) {
        String[] option = {"Copy", "Remove"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Choose Action");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    copyToClipBoard(chat.getMessage());
                    Toast.makeText(mContext, "Copy to clipboard", Toast.LENGTH_SHORT).show();
                } else if (which == 1) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("Remove For You?")
                            .setMessage("This message will be removed for you. Other chat members will still be able to see it")
                            .setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    deleteMessage(chat);
                                }
                            })
                            .setNegativeButton("CANCEL", null).show();

                }
            }
        });
        builder.create().show();
    }

    private void copyToClipBoard(String text) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message_text", text);
        clipboard.setPrimaryClip(clip);
    }

    private void deleteMessage(final Chat chat) {
        final DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference().child("Chats").child(chat.getId());
        messageReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (chat.getSender().equals(firebaseUser.getUid())) {
                    messageReference.child("senderRemove").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(mContext, "Removed", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (chat.getReceiver().equals(firebaseUser.getUid())) {
                    messageReference.child("receiverRemove").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(mContext, "Removed", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private int getLastPosition() {
        int position = 0;
        for (int i = listChat.size() - 1; i >= 0; i--) {
            if (listChat.get(i).isSenderRemove() == false && listChat.get(i).getSender().equals(firebaseUser.getUid())) {
                position = i;
                break;
            }

        }
        return position;
    }
}
