package com.example.is1305project.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.is1305project.R;
import com.example.is1305project.adapter.UserAdapter;
import com.example.is1305project.model.ChatList;
import com.example.is1305project.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ContactFragment extends Fragment {
    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> listUser;
    private List<ChatList> userList;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    private EditText searchUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userList = new ArrayList<>();

        // view list
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                    userList.add(chatList);
                }
                loadUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // search
        searchUser = view.findViewById(R.id.search_user);
        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsersByEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    private void searchUsersByEmail(String input){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("email")
                .startAt(input)
                .endAt(input+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!searchUser.getText().toString().isEmpty()){
                    listUser.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);

                        assert user != null;
                        assert firebaseUser != null;
                        if(!user.getId().equals(firebaseUser.getUid())){
                            listUser.add(user);
                        }
                    }

                    userAdapter = new UserAdapter(getContext(), listUser, false);
                    recyclerView.setAdapter(userAdapter);
                }else{
                    listUser.clear();
                    userAdapter = new UserAdapter(getContext(), listUser, false);
                    recyclerView.setAdapter(userAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void loadUsers(){
        listUser = new ArrayList<>();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(searchUser.getText().toString().equals("")){
                    listUser.clear();
                    for(ChatList chatList : userList){
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            User user = dataSnapshot.getValue(User.class);
                            assert user != null;
                            assert firebaseUser != null;
                            if(user.getId().equals(chatList.getId()) && !firebaseUser.getUid().equals(user.getId())){
                                listUser.add(user);
                            }
                        }
                    }

                    Collections.sort(listUser, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            return o1.getUsername().compareTo(o2.getUsername());
                        }
                    });

                    userAdapter = new UserAdapter(getContext(), listUser, false);
                    recyclerView.setAdapter(userAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}