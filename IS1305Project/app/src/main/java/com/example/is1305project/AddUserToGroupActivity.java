package com.example.is1305project;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.example.is1305project.adapter.AddUserAdapter;
import com.example.is1305project.adapter.ChatAdapter;
import com.example.is1305project.model.ChatList;
import com.example.is1305project.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class AddUserToGroupActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText searchUserGroup;
    private AddUserAdapter addUserAdapter;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private ArrayList<User> listUser = new ArrayList<>();
    private ArrayList<User> listUserSave = new ArrayList<>();
    private ArrayList<ChatList> userList = new ArrayList<>();
    private ArrayList<User> listUserAdd = new ArrayList<>();
    private Intent intent;
    private String groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_to_group);

        intent = getIntent();
        groupName = intent.getStringExtra("group_name");
        recyclerView = findViewById(R.id.recycler_view_group_add_user);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchUserGroup = findViewById(R.id.search_user_group);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                    userList.add(chatList);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        searchUserGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listUser.clear();
                if(TextUtils.isEmpty(s)){
                    listUser.addAll(listUserSave);
                }else {
                    for (User user : listUserSave){
                        if (user.getUsername().toLowerCase().contains(s.toString().toLowerCase())){
                            listUser.add(user);
                        }
                    }
                }
                addUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private void chatList(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listUser.clear();
                Collections.sort(userList, new Comparator<ChatList>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public int compare(ChatList o1, ChatList o2) {
                        return Long.compare(o2.getTime(), o1.getTime());
                    }
                });
                for(ChatList chatList : userList){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);
                        assert user != null;
                        assert firebaseUser != null;
                        assert chatList != null;
                        if(user.getId().equals(chatList.getId()) && !firebaseUser.getUid().equals(user.getId())){
                            listUser.add(user);
                        }
                    }
                }
                listUserSave.addAll(listUser);
                addUserAdapter = new AddUserAdapter(AddUserToGroupActivity.this, listUser, new AddUserAdapter.OnItemCheckListener() {
                    @Override
                    public void onItemCheck(User user) {
                        listUserAdd.add(user);
                    }

                    @Override
                    public void onItemUncheck(User user) {
                        listUserAdd.remove(user);
                    }

                });
                recyclerView.setAdapter(addUserAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        System.out.println(listUserAdd.size());
        for(User myUser : listUserAdd){
            System.out.println(myUser.getUsername());
        }
    }

    public void createGroup(View view){
        System.out.println(listUserAdd.size());
        for(User myUser : listUserAdd){
            System.out.println(myUser.getUsername());
        }
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String key = databaseReference.child("Group").push().getKey();
        HashMap<String, Object> groupHashMap = new HashMap<>();
        groupHashMap.put("id", key);
        groupHashMap.put("group_name", groupName);
        groupHashMap.put("imageURL", "default");
        databaseReference.child("Groups").child(key).setValue(groupHashMap);

        final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("Groups").child(key).child("member");
        for(User user : listUser){
            groupReference.push().setValue(user.getId());
            System.out.println(user.getUsername());
        }
    }
}