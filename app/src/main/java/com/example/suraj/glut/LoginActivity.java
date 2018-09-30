package com.example.suraj.glut;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
    private EditText UserEmail,UserPassword;
    private TextView NeedNewAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;

    private ImageView GoogleSignInButton;
    private  static final int RC_SIGN_IN=1;
    private GoogleSignInClient mGoogleSignInClient;

    private static final String TAG="LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Casting all things
        NeedNewAccountLink=(TextView)findViewById(R.id.register_account_link);
        UserEmail=(EditText)findViewById(R.id.login_email);
        UserPassword=(EditText)findViewById(R.id.login_password);
        LoginButton=(Button)findViewById(R.id.login_button);
        mAuth=FirebaseAuth.getInstance();
        loadingbar=new ProgressDialog(this);

        //login by google account casting
        GoogleSignInButton=(ImageView)findViewById(R.id.google_signin_button);
        //If new user comming then first click on New Users textView
        //New Users Action Happend
        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendUserToRegisterActivity();
            }
        });


        //if login button clicked
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //if user click on the Google Imageview
        GoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                signIn();
            }
        });


    }//oncreate ending



    //method for google signin

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            loadingbar.setTitle("Google SignIn");
            loadingbar.setMessage("Please Wait,you are login into your Google account....");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess())
            {
                GoogleSignInAccount account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "Please wait we are getting to your result...", Toast.LENGTH_SHORT).show();
                loadingbar.dismiss();
            }
            else
            {
                Toast.makeText(this, "Can;t get Auth result", Toast.LENGTH_SHORT).show();
                 loadingbar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            sentUserToMainActivity();
                        }
                        else
                            {
                            sendUserToLoginActivity();
                            String msg=task.getException().getMessage();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Error: "+msg, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }






    //check user is already logged in
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if (currentUser!=null)
        {
            sentUserToMainActivity();
        }

    }



    //allowing user to login
    private void AllowUserToLogin() {

        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please Enter Email!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please Enter Password!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(email)&&TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please Enter Email And Password!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Going to login
            loadingbar.setTitle("Login");
            loadingbar.setMessage("Please Wait,you are login into your account....");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {
                                sentUserToMainActivity();//after login redirect to MainActivity
                                Toast.makeText(LoginActivity.this, "You are Loggedin Successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                            else
                            {
                                String msg=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error Ocuurs:"+msg, Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }

    }


    //redirecting to mainactivity
    private void sentUserToMainActivity() {
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent mainIntent=new Intent(LoginActivity.this,LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);

    }

    //redirecting to RegisterActivity to creact new account
    private void sendUserToRegisterActivity(){
        Intent reg=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(reg);
        // finish();
    }
}
