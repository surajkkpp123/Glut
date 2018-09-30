package com.example.suraj.glut;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName,FullName,CountryName;
    private Button saveInformationButton;
    private CircleImageView ProfileImage;

    private FirebaseAuth mAuth;//current user authentication
    private DatabaseReference UsersRef;//database reference

    String currentUserId;
    private ProgressDialog loadingbar;//for loadingar display

    final static int gallerypick=1;//only select one image

    private StorageReference UserProfileImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        //Casting All things
        UserName=(EditText)findViewById(R.id.setup_username);
        FullName=(EditText)findViewById(R.id.setup_fullname);
        CountryName=(EditText)findViewById(R.id.setup_country);
        saveInformationButton=(Button)findViewById(R.id.setup_information_button);
        ProfileImage=(CircleImageView)findViewById(R.id.setup_profile_image);
        loadingbar=new ProgressDialog(this);


        //finding references

        mAuth=FirebaseAuth.getInstance();

        currentUserId=mAuth.getCurrentUser().getUid();//user cuurent unique id

        //here some problem then data is not saving in database
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


        //for image storing in firebase storage

        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");



        //when user click profile picture(circular iamges view)
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //redirect to User's phone Gallary to select image
                Intent galleryIntent=new Intent();

                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallerypick);
            }
        });


        //setting profilw picture which in selecting from Gallery;
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image=dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);

                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Please Select Profile Image!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
         }); //ending of image work




        //action on  save button clicked
        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveAccountSetupInformation();
            }
        });


    }//ending of oncreate method


    //to select image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==gallerypick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingbar.setTitle("Profile Image");
                loadingbar.setMessage("Please wait, while we updating your profile image...");
                loadingbar.show();
                loadingbar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                            //here we are getting the link of image from firebase database
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UsersRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingbar.dismiss();
            }
        }
    }


    //saving account informations into firebase database
    private void saveAccountSetupInformation()
    {
        String username=UserName.getText().toString();
        String fullname=FullName.getText().toString();
        String country=CountryName.getText().toString();

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Enter your Username", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Enter your Fullname", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Enter your Country", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //adding all thigs in database
            loadingbar.setTitle("Saving Information");
            loadingbar.setMessage("Please Wait,Saving all informations...");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            HashMap userMap=new HashMap();

            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("status","Hey!I am using Glut");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("relationshipstatus","none");

            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        sentUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Account is Updated successfully", Toast.LENGTH_LONG).show();
                        loadingbar.dismiss();
                    }
                    else
                    {
                        String msg=task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occurs: "+msg, Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });

        }
    }


    private void sentUserToMainActivity() {
        Intent mainIntent=new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
