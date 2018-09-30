package com.example.suraj.glut;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private static final int gallerypick=1;

    private ProgressDialog loadingbar;

    private Uri ImageUri;
    private StorageReference PostImagesRefrence;
    private DatabaseReference UsersRef,PostsRef;
    private FirebaseAuth mAuth;

    private String description,downloadUrl,current_user_id;
    private  String saveCurrentDate,saveCurrentTime,postRandomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();

        //storage reference
        PostImagesRefrence= FirebaseStorage.getInstance().getReference();

        //database refrence
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        //casting

        SelectPostImage=(ImageButton)findViewById(R.id.select_post_image);
        UpdatePostButton=(Button)findViewById(R.id.update_post_button);
        PostDescription=(EditText)findViewById(R.id.post_description);

        loadingbar=new ProgressDialog(this);

        mToolbar=(Toolbar)findViewById(R.id.update_post_page_toolbar);

        //here creating a back arrow to go to mainactivity
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        //if image select and click

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        //when update utton is clicked
        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });


    }//end of oncreate method



    // here opening the user's mobile phone gallery
    private void openGallery() {

        //redirect to User's phone Gallary to select image
        Intent galleryIntent=new Intent();

        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,gallerypick);
    }

    //selecting the image inside gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallerypick&&resultCode==RESULT_OK&&data!=null)
        {
            ImageUri=data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }

    }


    //Validateing post information
    private void ValidatePostInfo() {

         description=PostDescription.getText().toString();

        if(ImageUri==null)
        {
            Toast.makeText(this, "Please select the Image before posting!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Please say something about your image!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Add New Post");
            loadingbar.setMessage("Please wait, updating your new post...");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);
            storingImageToFirebaseStorage();
        }

    }

    //method to storing image into firebase database
    private void storingImageToFirebaseStorage() {

        //to create unique name of images
        Calendar calForDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(calForDate.getTime());


        Calendar calForTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calForDate.getTime());

        postRandomName=saveCurrentDate+saveCurrentTime;


        StorageReference filepath=PostImagesRefrence.child("Post Images").child(ImageUri.getLastPathSegment()+postRandomName+".jpg");

        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful())
                {
                    downloadUrl=task.getResult().getDownloadUrl().toString();

                    Toast.makeText(PostActivity.this, "image uploaded successfully!", Toast.LENGTH_SHORT).show();

                    savingPostInformationToDatabase();
                    loadingbar.dismiss();
                }
                else
                {
                    String msg=task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error Occers: "+msg, Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }

            }
        });
    }

    //here we are saving post information in firabese database
    private void savingPostInformationToDatabase() {

      UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {

              if (dataSnapshot.exists())
              {

                      String userFullName = dataSnapshot.child("fullname").getValue().toString();
                      String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                      HashMap postMap=new HashMap();

                      postMap.put("uid",current_user_id);
                      postMap.put("date",saveCurrentDate);
                      postMap.put("time",saveCurrentTime);
                      postMap.put("description",description);
                      postMap.put("postimage",downloadUrl);
                      postMap.put("profileimage",userProfileImage);
                      postMap.put("fullname",userFullName);

                      PostsRef.child(current_user_id+postRandomName).updateChildren(postMap)
                              .addOnCompleteListener(new OnCompleteListener() {
                                  @Override
                                  public void onComplete(@NonNull Task task)
                                  {

                                      if (task.isSuccessful())
                                      {
                                          sendUserToMainActivity();
                                          Toast.makeText(PostActivity.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                          loadingbar.dismiss();
                                      }
                                      else
                                      {
                                          String msg=task.getException().getMessage();
                                          Toast.makeText(PostActivity.this, "Error Occers: "+msg, Toast.LENGTH_SHORT).show();
                                          loadingbar.dismiss();
                                      }
                                  }
                              });
              }

          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });

    }


    //here creating option to go to mainactivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id =item.getItemId();
        //if clicking arrow
        if (id==android.R.id.home){

            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    //redirect to mainactivity
    private void sendUserToMainActivity() {

        Intent mainIntent=new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}
