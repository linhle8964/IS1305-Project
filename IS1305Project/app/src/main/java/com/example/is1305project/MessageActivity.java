package com.example.is1305project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.is1305project.adapter.MessageAdapter;
import com.example.is1305project.function.Status;
import com.example.is1305project.model.Chat;
import com.example.is1305project.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private TextView username;

    private FirebaseUser currentUser; // user cua minh
    private DatabaseReference reference;
    private Intent intent;
    private Toolbar toolbar;
    private EditText textSend;
    private ImageButton btnSend;

    private String userid; // user id cua nguoi minh dang chat
    private MessageAdapter messageAdapter;
    private List<Chat> listChat;
    private RecyclerView recyclerView;

    private ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //set tool bar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        // show message box
        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        textSend = findViewById(R.id.text_send);
        btnSend = findViewById(R.id.btn_send);

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textSend.getText().toString();
                if (!message.trim().equals("")) {
                    sendMessage(currentUser.getUid(), userid, message);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                    ;
                }

                textSend.setText("");
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                username.setText(user.getUsername());
                Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);

                readMessage(currentUser.getUid(), userid, user.getImageURL());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // show message
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        seenMessage(userid);
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        message = message.trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final long currentTime = System.currentTimeMillis();
        String key = reference.child("Chats").push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", key);
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("time", currentTime);
        hashMap.put("isSeen", false);
        hashMap.put("senderRemove", false);
        hashMap.put("receiverRemove", false);

        reference.child("Chats").child(key).setValue(hashMap);

        // add user to chat fragment
        addToChatList(currentUser.getUid(), userid, currentTime);
        addToChatList(userid, currentUser.getUid(), currentTime);
    }

    private void addToChatList(String sender, final String receiver, final Long currentTime) {
        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(sender)
                .child(receiver);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    HashMap<String, Object> chatListHashMap = new HashMap<>();
                    chatListHashMap.put("id", receiver);
                    chatListHashMap.put("time", currentTime);
                    chatRef.setValue(chatListHashMap);
                } else {
                    chatRef.child("time").setValue(currentTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(final String myid, final String userid, final String imageURL) {
        listChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listChat.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid)
                            || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        listChat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, listChat, imageURL);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Status.changeStatus("online", currentUser);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (seenListener != null && reference != null) {
            reference.removeEventListener(seenListener);
        }
        Status.changeStatus("offline", currentUser);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_friend_profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra("userid", userid);
                System.out.println(userid);
                startActivity(profileIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}