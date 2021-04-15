package com.example.researchproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.researchproject.database.AppDatabase;
import com.example.researchproject.database.History;
import com.example.researchproject.database.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    int RC_SIGN_IN = 1;
    AppDatabase db;

    SignInButton btnSignIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create Google Sign In Options
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // declare client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                }
        });

        // create instance of database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "project_db_v4").build();

    }
    @Override
    protected void onStart() {
        super.onStart();
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //updateUI(account);

        mGoogleSignInClient.signOut();
        mGoogleSignInClient.revokeAccess();
    }

    private void signIn() {
        // Switch to login activity
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // get email as userId
            String userId = account.getEmail();

            // if userId is not registered in db, registerUser
            // initialize ifArtist = false
            User user = new User(userId, account.getDisplayName(), false);
            new registerUser(MainActivity.this, user).execute();

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("tag", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    public void updateUI(GoogleSignInAccount account) {
        // if user is signed in, navigate to main page
        if (account != null) {
            Intent intent = new Intent(MainActivity.this, Welcome.class);
            startActivity(intent);
        }
    }

    private static class registerUser extends AsyncTask<Void,Void,Boolean> {

        private WeakReference<MainActivity> activityReference;
        private User user;

        // only retain a weak reference to the activity
        registerUser(MainActivity context, User user) {
            activityReference = new WeakReference<>(context);
            this.user = user;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            // if user doesn't exist, create new user and history
            if (activityReference.get().db.userDao().findByUserId(user.getUserId()) == null) {
                activityReference.get().db.userDao().insert(user);
                activityReference.get().db.historyDao().insert(new History(user.getUserId()));
            }
            return true;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(Boolean bool) {
        }

    }

}