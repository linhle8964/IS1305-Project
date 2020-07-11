package com.example.is1305project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateGroupNameActivity extends AppCompatActivity {
    private Intent intent;
    private EditText groupName;
    private Button btnNext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_name);
        intent = new Intent(this, AddUserToGroupActivity.class);
        groupName = findViewById(R.id.group_name);
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupName.getText().toString().trim().isEmpty()){
                    Toast.makeText(CreateGroupNameActivity.this, "Group Name cannot be empty", Toast.LENGTH_SHORT).show();
                }else{
                    intent.putExtra("group_name", groupName.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }
}