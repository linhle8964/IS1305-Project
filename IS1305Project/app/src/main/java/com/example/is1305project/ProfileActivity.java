package com.example.is1305project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.security.Key;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ImageView avatar;
    private TextView nameTV, EmailTV;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storage;
    String path = "Users_Profile_Imgs";
    private FloatingActionButton fab;
    private ProgressDialog pd;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;
    private String camerapermission[];
    private String storagepermission[];
    private Uri image_uri;
    private String userid;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // set toolbar text
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        database = FirebaseDatabase.getInstance();
        intent = getIntent();
        userid = intent.getStringExtra("userid");
        reference = database.getReference("Users");
        storage = FirebaseStorage.getInstance().getReference();
        camerapermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        avatar = findViewById(R.id.avatar);
        nameTV = findViewById(R.id.NameTV);
        EmailTV = findViewById(R.id.EmailTV);
        fab = findViewById(R.id.fab);

        pd = new ProgressDialog(getApplicationContext());

        System.out.println(user.getUid());
        System.out.println(userid);

        if (user.getUid() .equalsIgnoreCase( userid)==false) {
fab.setVisibility(View.GONE);
            Query query1 = reference.orderByChild("id").equalTo(userid);
            query1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String name = "" + ds.child("username").getValue();
                        String email = "" + ds.child("email").getValue();
                        String image = "" + ds.child("imageURL").getValue();
                        nameTV.setText(name);
                        EmailTV.setText(email);

                        try {
                            Picasso.get().load(image).into(avatar);
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_add_image).into(avatar);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



        } else if (userid .equalsIgnoreCase( user.getUid())==true) {
            fab.setVisibility(View.VISIBLE);
            Query query = reference.orderByChild("email").equalTo(user.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String name = "" + ds.child("username").getValue();
                        String email = "" + ds.child("email").getValue();
                        String image = "" + ds.child("imageURL").getValue();
                        nameTV.setText(name);
                        EmailTV.setText(email);

                        try {
                            Picasso.get().load(image).into(avatar);
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_add_image).into(avatar);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowEditprofile();

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(userid.equalsIgnoreCase(user.getUid())==false){
            System.out.println(user.getUid());
            System.out.println(userid);
        Intent intent= new Intent(this,MessageActivity.class);
        intent.putExtra("userid",userid);
        startActivity(intent);
        }
        else if(userid.equalsIgnoreCase(user.getUid())==true){
            Intent intent= new Intent(this,MainActivity.class);
            startActivity(intent);

        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Please Enable Permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGal();
                    } else {
                        Toast.makeText(this, "Please Enable Permission", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_REQUEST_CODE) {
                image_uri = data.getData();
                uploadProfile(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                uploadProfile(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfile(Uri image_uri) {

        String filepath = path + "" + "imageURL" + "_" + user.getUid();
        StorageReference storageReference = storage.child(filepath);
        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("imageURL", downloadUri.toString());
                            reference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(),"Update Image Successfully",Toast.LENGTH_SHORT).show();
                                }

                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getApplicationContext(),"Update Image Failed",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            pd.dismiss();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"Update Image Failed",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromGal() {
        Intent galleryintent = new Intent(Intent.ACTION_PICK);
        galleryintent.setType("image/*");
        startActivityForResult(galleryintent, IMAGE_PICK_REQUEST_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp DES");
        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    private void ShowImagePicDialog() {
        String[] option = {"Camera ", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCamera();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStorage();
                    } else {
                        pickFromGal();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void ShowEditprofile() {
        String[] option = {"Edit Profile Picture ", "Edit Name"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pd.setMessage("Updating Profile Picture");
                    ShowImagePicDialog();
                } else if (which == 1) {
                    pd.setMessage("Updating Name");
                    ShowNameUpdateDialog("username");

                }
            }
        });
        builder.create().show();
    }

    private void ShowNameUpdateDialog(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(this);
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {

                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                    reference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(),"Update Name Successfully",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(),"Update Name Failed",Toast.LENGTH_SHORT).show();
                                }
                            });
                    {

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please Enter " + key, Toast.LENGTH_SHORT).show();
                }
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStorage() {
        ActivityCompat.requestPermissions(this, storagepermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCamera() {
        ActivityCompat.requestPermissions(this, camerapermission, CAMERA_REQUEST_CODE);
    }


}