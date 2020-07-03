package com.example.is1305project.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.is1305project.R;
import com.example.is1305project.adapter.ChatAdapter;
import com.example.is1305project.adapter.UserAdapter;
import com.example.is1305project.function.OnBackPressed;
import com.example.is1305project.model.Chat;
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


public class ChatFragment extends Fragment implements OnBackPressed {
    private RecyclerView recyclerView;
    private EditText search_conversation;
    private ChatAdapter chatAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private ArrayList<User> listUser = new ArrayList<>();
    private ArrayList<User> listUserSave = new ArrayList<>();
    private ArrayList<ChatList> userList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);

        search_conversation = view.findViewById(R.id.search_conversation);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
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
        search_conversation.addTextChangedListener(new TextWatcher() {
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
                chatAdapter = new ChatAdapter(getContext(), listUser);
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
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
                chatAdapter = new ChatAdapter(getContext(), listUser);
                chatAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        chatAdapter = new ChatAdapter(getContext(), listUser);
        chatAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(chatAdapter);
    }

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().popBackStack();
        // exit app
        Intent exit = new Intent(Intent.ACTION_MAIN);
        exit.addCategory(Intent.CATEGORY_HOME);
        exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(exit);
    }
}