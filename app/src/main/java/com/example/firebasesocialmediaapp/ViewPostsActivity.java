package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView postsListView;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;

    private ImageView sentPostImageView;
    private TextView txtDescription;
    private ArrayList<DataSnapshot> dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        sentPostImageView = findViewById(R.id.sentPostImageView);
        txtDescription = findViewById(R.id.txtDescription);


        firebaseAuth = FirebaseAuth.getInstance();

        postsListView = findViewById(R.id.postsListView);
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        postsListView.setAdapter(adapter);
        dataSnapshots = new ArrayList<>();
        postsListView.setOnItemClickListener(this);
        postsListView.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference()
                .child("my_users")
                .child(firebaseAuth.getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //object of received shots is the parameter datasnapshots so creating an instance datasnapshots
                dataSnapshots.add(dataSnapshot);

                //we r going to access the child of received posts child and will access the value
                String fromWhomUsername = (String) dataSnapshot.child("fromWhom").getValue();
                usernames.add(fromWhomUsername);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {


                int i = 0;
                for (DataSnapshot snapshot : dataSnapshots) {

                    //if snapshot we get from array list is equal to the data snapshot we get from onchild removed
                    if (snapshot.getKey().equals(dataSnapshot.getKey())){

                        //deleting snapshot and username from array list
                        dataSnapshots.remove(i);
                        usernames.remove(i);

                    }
                        i++;
                }
                //will uppdate th elist view accordingly
                adapter.notifyDataSetChanged();
                //also deleting image and showimg the placehold4r
                sentPostImageView.setImageResource(R.drawable.placeholder);
                //also updating text view which was the description
                txtDescription.setText("");


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        //getting the item related to "fromWhom" user. getting the user's pic
        DataSnapshot myDataSnapshot = dataSnapshots.get(position);
        //access to the download link that is sent by the user
        //mydatasnapshot referring to received post child
        String downloadLink = (String) myDataSnapshot.child("imageLink").getValue();

        Picasso.get().load(downloadLink).into(sentPostImageView);
        txtDescription.setText((String) myDataSnapshot.child("des").getValue());

    }



    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                        //continue to delete

                        //its better to first delete image from storage
                        //deleting image from storage firebase
                        FirebaseStorage.getInstance().getReference()
                                .child("my_images").child((String) dataSnapshots.get(position)
                                .child("imageIdentifier").getValue()).delete();

                        //deleteing posts from server database
                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users").child(firebaseAuth.getCurrentUser()
                                .getUid()).child("received_posts")
                                .child(dataSnapshots.get(position).getKey()).removeValue();
                    }
                });
//        if(hasNegativeAction || negativeAction!=null || negativeButtonText!=null)
//        {
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
//                    switch (negativeAction)
////                    {
////                        case 1:
////                            //TODO:Do your negative action here
////                            break;
////                        //TODO: add cases when needed
////                    }
                }
            });
     //   }
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();

        return false;
    }
}
