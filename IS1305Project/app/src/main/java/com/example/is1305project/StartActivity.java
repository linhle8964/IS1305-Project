package com.example.is1305project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.is1305project.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    private SignInButton btnLogin;
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "MyActivity";
    private DatabaseReference reference;
    private List<User> listUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnLogin = (SignInButton) findViewById(R.id.sign_in_button);
        listUser = new ArrayList<>();
        // set toolbar text
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Log In");
        //getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signInGoogle() {
        Intent signIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(StartActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            final boolean[] checkExist = {false};
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            final List<User> listUser = new ArrayList<>();
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    listUser.clear();
                                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                        User myUser = dataSnapshot.getValue(User.class);
                                        if(user.getUid().equals(myUser.getId())){
                                            checkExist[0] = true;
                                        }
                                    }

                                    if(user != null && checkExist[0] == false){
                                        String username = user.getDisplayName();
                                        Uri profileUri = user.getPhotoUrl();
                                        String phoneNumber = user.getPhoneNumber();

                                        // If the above were null, iterate the provider data
                                        // and set with the first non null data
                                        for (UserInfo userInfo : user.getProviderData()) {
                                            if (username == null && userInfo.getDisplayName() != null) {
                                                username = userInfo.getDisplayName();
                                            }
                                            if (profileUri == null && userInfo.getPhotoUrl() != null) {
                                                profileUri = userInfo.getPhotoUrl();
                                            }

                                            if(phoneNumber == null && userInfo.getPhoneNumber() != null){
                                                phoneNumber = userInfo.getPhoneNumber();
                                            }
                                        }

                                        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("id", user.getUid());
                                        hashMap.put("username", username);
                                        hashMap.put("email", user.getEmail());
                                        hashMap.put("phoneNumber", phoneNumber);
                                        hashMap.put("imageURL", profileUri.toString());
                                        reference.setValue(hashMap);
                                        updateUI(user);
                                    }else{
                                        System.out.println("Check: " + checkExist[0]);
                                        updateUI(user);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}