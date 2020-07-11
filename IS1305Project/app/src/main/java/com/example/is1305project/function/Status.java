package com.example.is1305project.function;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Status {

    public static void changeStatus(String status, FirebaseUser currentUser) {
        if(currentUser != null){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            reference.updateChildren(hashMap);
        }
    }

}
