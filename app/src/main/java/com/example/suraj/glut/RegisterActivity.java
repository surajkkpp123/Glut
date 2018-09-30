package com.example.suraj.glut;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail,UserPassword,UserConfermPassword;
    private Button CreateAccountButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //casting all things

        mAuth=FirebaseAuth.getInstance(); // authentication

        UserEmail=(EditText)findViewById(R.id.register_email);
        UserPassword=(EditText)findViewById(R.id.register_password);
        UserConfermPassword=(EditText)findViewById(R.id.register_conferm_password);
        CreateAccountButton=(Button)findViewById(R.id.register_create_account);

        loadingbar=new ProgressDialog(this);//progressBar likes syncing


        //Create new account onclick
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });


    }//ending of oncreate method


    //creating an account
    private void createNewAccount(){

        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        String confermpassword=UserConfermPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please Enter Your Email ", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please Enter Password ", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confermpassword))
        {
            Toast.makeText(this, "Please Conferm password ", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confermpassword))
        {
            Toast.makeText(this, "Please Enter same password to conferm! ", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // new account creating and data are entering in firebase (database);

            loadingbar.setTitle("Creating New Account");
            loadingbar.setMessage("Please Wait,Account is creating....");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful())
                    {
                        sendUserTosetupActivity();
                        Toast.makeText(RegisterActivity.this, "You are successfully authenticate.", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                    else
                    {
                        String message=task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error occurs: "+message, Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }

    }//ending of create new account


    //redirecting to setupactivity from registration activity
    private void sendUserTosetupActivity(){

        Intent setup=new Intent(RegisterActivity.this,SetupActivity.class);
        setup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setup);
        finish();
    }

    //check user is already logged in no need to register
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if (currentUser!=null)
        {
            sentUserToMainActivity();
        }
    }

    //redirecting to mainactivity when user already loggedin
    private void sentUserToMainActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }



}
