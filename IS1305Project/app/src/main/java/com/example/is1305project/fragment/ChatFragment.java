package com.example.is1305project.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.is1305project.R;
import com.example.is1305project.adapter.ChatAdapter;
import com.example.is1305project.adapter.UserAdapter;
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
import java.util.List;


public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;

    private ChatAdapter chatAdapter;
    private List<User> listUser;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private List<String> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        userList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    if(chat.getSender().equals(firebaseUser.getUid())){
                        userList.add(chat.getReceiver());
                    }

                    if(chat.getReceiver().equals(firebaseUser.getUid())){
                        userList.add(chat.getSender());
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    private void readChats(){
        listUser = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listUser.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    /*for(String id : userList){
                        if(user.getId().equals(id)){
                            if(listUser.size() != 0){
                                for(User myUser : listUser){
                                    if(!myUser.getId().equals(user.getId())){
                                        listUser.add(user);
                                    }
                                }
                            }else{
                                listUser.add(user);
                            }
                        }
                    }*/
                }
                chatAdapter = new ChatAdapter(getContext(), listUser);
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}