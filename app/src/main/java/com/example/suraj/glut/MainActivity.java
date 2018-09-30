package com.example.suraj.glut;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private RecyclerView postList;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;  //main tool bar

    private FirebaseAuth mAuth; //user authontication
    private DatabaseReference UsersRef,PostRef; //it is checking using existinging in database

    private CircleImageView navProfileImage;//to set navprofile image of user
    private TextView navProfileUserName;//to set navprofile name of user displayed

    private ImageButton AddNewPostButton;

    private FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter;

    String currentUserId;

    //Initial method Oncreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //authontication
          mAuth=FirebaseAuth.getInstance();


        // here it is checking null pointer and given currentuserid to help to set navigation profile image and username
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendUserToLoginActivity();
        }
        else{
            //  checkUserExistence();
            currentUserId=mAuth.getCurrentUser().getUid();
        }

        //   currentUserId=mAuth.getCurrentUser().getUid();//here is problem
          UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");//user refrence
          PostRef= FirebaseDatabase.getInstance().getReference().child("Posts");//user refrence


        //NavigationBar  section
        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Glut");

        AddNewPostButton=(ImageButton)findViewById(R.id.add_new_post);//post button casting

        //this drawer section
        drawerLayout= findViewById(R.id.drawer_layout);
        actionBarDrawerToggle=new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //navigation View casting
        navigationView= findViewById(R.id.navigation_view);


        //casting of postlist which is recyclerview and displaying latest post on top

        postList=(RecyclerView)findViewById(R.id.all_users_post_list);

        //postList.setHasFixedSize(true);//it is not opening recycler view
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);

        //display the new post at top
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        //casting
        //navigation header
        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);
        //setting profile image and user name in navbar
        navProfileImage=(CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileUserName=(TextView) navView.findViewById(R.id.nav_user_full_name);

        //navigation selector menu must be in side oncreate method
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });


        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        navProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(navProfileImage);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

      //when user click on post button
        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });


        //displaying all user's post
        DisplayAllUsersPosts();


    }//ending of oncreate method


    //this is for firebase-ui 3.2.2
    private void DisplayAllUsersPosts()
    {

       // Query query = PostRef.child("Posts").limitToLast(100);

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(PostRef, Posts.class)
                        .build();

         //FirebaseRecyclerAdapter<Posts,PostsViewHolder>
         firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                        final String PostKey=getRef(position).getKey();

                        holder.setFullname(model.getFullname());
                        holder.setTime(model.getTime());
                        holder.setDate(model.getDate());
                        holder.setDescription(model.getDescription());
                        holder.setProfileimage(getApplicationContext(), model.getProfileimage());
                        holder.setPostimage(getApplicationContext(), model.getPostimage());

                        //if click upon the post the it rediect to click activty
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //redirecting to ClickPostActivty with Post Key
                                Intent clickPostIntent=new Intent(MainActivity.this,ClickPostActivity.class);
                               clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);
                            }
                        });
               }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.all_post_layout, parent, false);

                        return new PostsViewHolder(view);

                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }


    //onstop method calling
    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();

    }


    //populate class
    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        //setting all values to Posts class
        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);

        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time)
        {

            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText(time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText(date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1,  String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }

    }//end of PostsViewHolder clasd




    // if onstart method  is called
    @Override
    protected void onStart(){
        super.onStart();


        //Firebase user existence checking
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if (currentUser==null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            checkUserExistence();
        }
        //it is basically for  recycler view means all post
        firebaseRecyclerAdapter.startListening();

    }


    //checking user existence in firebase
    private void checkUserExistence()
    {
        FirebaseUser User=mAuth.getCurrentUser();

        if (User!=null)
         {
            final String current_user_id = mAuth.getCurrentUser().getUid();


            UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //If user is authenticate but not in database then we have to send user to fill all required details
                    if (!dataSnapshot.hasChild(current_user_id)) {

                        sendUserToSetupActivity();
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Toast.makeText(this, "Current user is null", Toast.LENGTH_SHORT).show();
        }

    }




    //for click Home buttion action happened
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }


    //for navigation item is selected
    private void UserMenuSelector(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.nav_post:
                      sendUserToPostActivity();
                     break;

            case R.id.nav_profile:
                Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "friend", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Toast.makeText(this, "find_friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_Logout:

                mAuth.signOut();
                sendUserToLoginActivity();
                break;
        }
      }

    //redirecting to setupactivity
    private void sendUserToSetupActivity()
     {
        Intent setupIntent=new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
     }

    //redirecting to LoginActivity from MainActivity
    private void sendUserToLoginActivity()
    {
        Intent logIntent=new Intent(MainActivity.this,LoginActivity.class);
        logIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logIntent);
        finish();
    }

    //redirecting to PostActivity
    private void sendUserToPostActivity() {
        Intent postIntent=new Intent(MainActivity.this,PostActivity.class);
         startActivity(postIntent);
    }


}//end of MainActivity
