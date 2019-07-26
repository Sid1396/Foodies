package com.example.foodies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {


    private EditText SetupUserName, SetupFullName, SetupEmailId;
    private Button SaveInformationbuttion;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    String currentUserID;
    private String downloadUrl;


    private Uri ImageUri;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        SetupUserName = (EditText) findViewById(R.id.setup_username);
        SetupFullName = (EditText) findViewById(R.id.setup_full_name);
        SetupEmailId = (EditText) findViewById(R.id.setup_email_id);
        SaveInformationbuttion = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference();


        SaveInformationbuttion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();
            }
        });


        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();


            }
        });


    }



    private void openGallery()
    {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick &&  resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            ProfileImage.setImageURI(ImageUri);

            loadingBar.setTitle("Uploading Profile Image");
            loadingBar.setMessage("Please wait");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            ProgressBar progressbar=(ProgressBar)loadingBar.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#C60000"), android.graphics.PorterDuff.Mode.SRC_IN);

            saveImagetoFirebase();

        }
    }


    private void saveImagetoFirebase() {

        final StorageReference filePath = UserProfileImageRef.child("Profile Images").child(currentUserID + ".jpg");
        filePath.putFile(ImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downUri = task.getResult();
                    //            Toast.makeText(SetupActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();


                    downloadUrl = downUri.toString();
                    SavingProfileInformationToDatabase();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Error occured while uploading profile picture: " + message, Toast.LENGTH_SHORT).show();

                }
            }
        });


    }


    private void SavingProfileInformationToDatabase() {
        UsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){


                    Toast.makeText(SetupActivity.this, "Profile Picture Uploaded Successfully ", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();


                }else{
                    String message = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Error "+message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });

    }





    private void SaveAccountSetupInformation()
    {

        String username = SetupUserName.getText().toString();
        String fullname = SetupFullName.getText().toString();
        String email = SetupEmailId.getText().toString();


        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show();
        }
        else {

            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait ");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            ProgressBar progressbar=(ProgressBar)loadingBar.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#C60000"), android.graphics.PorterDuff.Mode.SRC_IN);


            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("Email", email);



            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Account created Successfully.", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String message =  task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }


    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }







}
