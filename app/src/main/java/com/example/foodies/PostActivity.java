package com.example.foodies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class PostActivity extends AppCompatActivity {

    private Button UpdatePostButton;
    private ImageView postimage;
    private EditText PostDescription;
    private  String Description;
    private Uri ImageUri;
    private ProgressDialog loadingBar;
    private String dir;
    private static final int Camera_Pick = 1;
    private String pathToFile;
    private static final int Gallery_Pick = 2;
    private Uri fileUri = null;
    private FirebaseAuth mAuth;
    private String pictureFilePath;
    private String deviceIdentifier;
    private Spinner railway_station;
    private ImageView postbackbtn;
    private long countPosts = 0;
    private DatabaseReference UsersRef, PostsRef,NewPostRef;
    private StorageReference PostsImagesRefrence;
    public  long time;
    public String finalTime;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        if(Build.VERSION.SDK_INT >= 23){
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }


        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("ddMMyyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());
        finalTime = saveCurrentDate+System.currentTimeMillis();


        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        PostsImagesRefrence = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");



        UpdatePostButton = (Button) findViewById(R.id.btn_post);
        PostDescription =(EditText) findViewById(R.id.et_discreption);
        postimage=(ImageView)findViewById(R.id.post_image);
        loadingBar = new ProgressDialog(this);



        railway_station = (Spinner)findViewById(R.id.spinner_stations);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.RailwayStations,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        railway_station.setAdapter(adapter);

        postimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setTitle("Add Photo!");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo"))
                        {

                            dispatchPictureTakerAction();

                        }
                        else if (options[item].equals("Choose from Gallery"))
                        {
                            openGallery();
                        }
                        else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();



        }
        });



        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatePostInfo();
            }
        });




    }

    private void dispatchPictureTakerAction() {

        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getPackageManager()) != null )
        {
            File photofile = null ;
            photofile = createPhotoFile();


            if (photofile != null)
            {
                pathToFile = photofile.getAbsolutePath();
                ImageUri= FileProvider.getUriForFile(PostActivity.this,"com.example.foodies.fileprovider",photofile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT,ImageUri);
                startActivityForResult(takePic,Camera_Pick);

            }

        }



    }

    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

       File image = null;
        try {
            image = File.createTempFile(name,".jpg",storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

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


        if (requestCode == Camera_Pick && resultCode == RESULT_OK) {

            File imgFile = new  File(pathToFile);
            if(imgFile.exists())            {
                postimage.setImageURI(Uri.fromFile(imgFile));
            }


        loadingBar.setTitle("Uploading Your Post");
        loadingBar.setMessage("Please wait");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
        ProgressBar progressbar = (ProgressBar) loadingBar.findViewById(android.R.id.progress);
        progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#C60000"), android.graphics.PorterDuff.Mode.SRC_IN);


        saveImagetoFirebase();


        }


        else if (requestCode==Gallery_Pick &&  resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            postimage.setImageURI(ImageUri);

            loadingBar.setTitle("Uploading Your Post");
            loadingBar.setMessage("Please wait");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            ProgressBar progressbar=(ProgressBar)loadingBar.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#C60000"), android.graphics.PorterDuff.Mode.SRC_IN);


            saveImagetoFirebase();

        }
    }

    private void saveImagetoFirebase() {


        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;
        final StorageReference filePath = PostsImagesRefrence.child("Post Images").child(current_user_id + postRandomName + ".jpg");






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
                    // Toast.makeText(SetupActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();


                    downloadUrl = downUri.toString();
                    SavingProfileInformationToDatabase();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured while uploading profile picture: " + message, Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    private void SavingProfileInformationToDatabase() {
        UsersRef.child(current_user_id).child("postimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){


                    Toast.makeText(PostActivity.this, "Post is Successfully Uploaded", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();


                }else{
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error "+message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });

    }


    private void ValidatePostInfo()
    {
        Description = PostDescription.getText().toString().trim();
        String selected_category = railway_station.getSelectedItem().toString();


        if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
        }else  if(selected_category.equals("Select Station")){
            Toast.makeText(this, "Please Select Station !!", Toast.LENGTH_SHORT).show();
        }
        else if(ImageUri == null)
        {
            Toast.makeText(this, "Please Select Photo to upload", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Adding Your Post");
            loadingBar.setMessage("Please wait");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            ProgressBar progressbar=(ProgressBar)loadingBar.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#C60000"), android.graphics.PorterDuff.Mode.SRC_IN);

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calFordTime.getTime());

            postRandomName = saveCurrentDate + saveCurrentTime;

            SavingPostInformationToDatabase();
        }
    }


    private void SavingPostInformationToDatabase() {


        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String userFullName = dataSnapshot.child("fullname").getValue().toString();
                        String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        String selected_category = railway_station.getSelectedItem().toString();


                        HashMap postsMap = new HashMap();
                        postsMap.put("uid", current_user_id);
                        postsMap.put("date", saveCurrentDate);
                        postsMap.put("time", saveCurrentTime);
                        postsMap.put("description", Description);
                        postsMap.put("profileimage", userProfileImage);
                        postsMap.put("fullname", userFullName);
                     //   postsMap.put("station", selected_category);
                        postsMap.put("counter", finalTime);
                        postsMap.put("postimage", downloadUrl);


                        PostsRef.child(selected_category).child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                if (task.isSuccessful()) {

                                    SendUserToMainActivity();

                                    Toast.makeText(PostActivity.this, "Post Updated ", Toast.LENGTH_SHORT).show();

                                    loadingBar.dismiss();
                                } else {

                                    Toast.makeText(PostActivity.this, "Error Occured +", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                    else {
                        String userFullName = dataSnapshot.child("fullname").getValue().toString();
                        String selected_category = railway_station.getSelectedItem().toString().toLowerCase();


                        HashMap postsMap = new HashMap();
                        postsMap.put("uid", current_user_id);
                        postsMap.put("date", saveCurrentDate);
                        postsMap.put("time", saveCurrentTime);
                        postsMap.put("description", Description);
                        postsMap.put("fullname", userFullName);
                        postsMap.put("station", selected_category);
                        postsMap.put("counter", countPosts);
                        PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                if (task.isSuccessful()) {

                                    SendUserToMainActivity();

                                    Toast.makeText(PostActivity.this, "Post Updated ", Toast.LENGTH_SHORT).show();

                                    loadingBar.dismiss();
                                } else {

                                    Toast.makeText(PostActivity.this, "Error Occured +", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

}
