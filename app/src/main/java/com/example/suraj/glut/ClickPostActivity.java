package com.example.suraj.glut;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView postImage;
    private TextView postDescription;
    private Button editPostButton,deletePostButton;

    private String getPostKey;

    private DatabaseReference ClickPostRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        //casting all things
        postImage = (ImageView) findViewById(R.id.click_post_image);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        editPostButton = (Button) findViewById(R.id.edit_post_button);
        deletePostButton = (Button) findViewById(R.id.delete_post_button);

        //Postkey which is identify the which post number
        getPostKey = getIntent().getExtras().get("PostKey").toString();

        //we are getting refrence of database from Posts child
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(getPostKey);

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String description=dataSnapshot.child("description").getValue().toString();
                String image=dataSnapshot.child("postimage").getValue().toString();


                postDescription.setText(description);
                Picasso.with(ClickPostActivity.this).load(image).into(postImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }//ending of oncreate method

}
