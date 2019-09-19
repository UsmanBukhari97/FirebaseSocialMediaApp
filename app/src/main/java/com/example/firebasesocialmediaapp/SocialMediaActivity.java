package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseAuth mAuth;

    private ImageView postImageView;
    private Button btnCreatePost;
    private EditText edtDescription;
    private ListView usersListView;
    private Bitmap bitmap;
    private String imageIdentifier;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private ArrayList<String> uids;
    private String imageDownloadLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        postImageView = findViewById(R.id.postImageView);
        btnCreatePost = findViewById(R.id.btnCreatePost);
        edtDescription = findViewById(R.id.edtDescription);
        usersListView = findViewById(R.id.usersListView);

        //seleccting each user
        usersListView.setOnItemClickListener(this);

        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);

        uids = new ArrayList<>();

        usersListView.setAdapter(adapter);

        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

        uploadImageToServer();

            }
        });

        //when users taps on the image it should access our gallery to select image
        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectImage();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.my_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.logOutItem:

               logout();

                break;

            case R.id.viewPostsItem:
                Intent intent = new Intent(this, ViewPostsActivity.class);
                startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    //logging ot when pressing back button
    @Override
    public void onBackPressed() {
        logout();

        super.onBackPressed();
    }

    private void logout(){
        mAuth.signOut();
        finish();

    }

    private void selectImage() {
        if (Build.VERSION.SDK_INT < 23) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1000);
        } else if (Build.VERSION.SDK_INT >= 23)
            //if user havent given us the permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=  PackageManager.PERMISSION_GRANTED) {
                //then request permission
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);

            } else {

                //when user have already give us the permission
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            selectImage();

        }
        //after reuest permission result we hav ethe on activity result

    }
    //after reuest permission result we hav ethe on activity result
//when user has selected an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if the user has selected an image and data is not null
        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            Uri chosenImageData = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageData);
                postImageView.setImageBitmap(bitmap);


            } catch (Exception e) {

                e.printStackTrace();
            }

        }
    }

    //copied from firebase docs
    //android > storage > store data from memory
    //called in button listener
    private void uploadImageToServer(){

        //bitmap is initialized in class so if no image is selected this method will compress bitmap causing the app to crash
        if (bitmap != null) {

            // Get the data from an ImageView as bytes
            postImageView.setDrawingCacheEnabled(true);
            postImageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) postImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            //specifying id to evry image
            //will generate a unique id of type png
            imageIdentifier = UUID.randomUUID() + ".png";

            // UploadTask uploadTask = mountainsRef.putBytes(data);
            UploadTask uploadTask = FirebaseStorage.getInstance().getReference()
                    .child("my_images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(SocialMediaActivity.this, exception.toString(), Toast.LENGTH_LONG).show();

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(SocialMediaActivity.this, "Uploading process was successful", Toast.LENGTH_LONG).show();

                    edtDescription.setVisibility(View.VISIBLE);

                    //getting username sin list view
                    //getting username from my_users as username is a child of my_users
                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            //we getting data in this method

                            //this will give s the uid of we have under my_users child
                            uids.add(dataSnapshot.getKey());




                            //when a child is added this will execute
                            String username = (String) dataSnapshot.child("username").getValue();
                            usernames.add(username);
                            adapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    //we want to get download url
                    //tasksnapshot is parameter of onSuccess method
                    //getting url is a heavy method. cant be done on ui thread. so going to background
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()){

                                //get result will give us the url but converting it to string
                                imageDownloadLink = task.getResult().toString();

                            }

                        }
                    });
                }
            });
        }

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        //key String, value string
        HashMap<String, String> dataMap = new HashMap<>();
        //from whom the created posts are sent
        dataMap.put("fromWhom", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        dataMap.put("imageIdentifier", imageIdentifier);
        dataMap.put("imageLink", imageDownloadLink);
        dataMap.put("des", edtDescription.getText().toString());
        //sending data to server  or target user
        //push method that we called on the received posts will set a value
        FirebaseDatabase.getInstance().getReference()
                .child("my_users")
                .child(uids.get(position))
                .child("received_posts").push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
           //adding oncomplete listener after setvalue to know that the process is finished
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                //saving firebase process when fininshed
                Toast.makeText(SocialMediaActivity.this, "Data is sent", Toast.LENGTH_SHORT).show();

            }
        });




    }
}
