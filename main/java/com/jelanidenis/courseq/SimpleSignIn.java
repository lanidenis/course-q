package com.jelanidenis.courseq;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jelanidenis.courseq.Log;
import com.jelanidenis.courseq.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class SimpleSignIn extends AppCompatActivity {

    private static final String TAG = "SimpleSignIn";

    //Firebase References
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private DatabaseReference mUserIndex;
    private ValueEventListener mIndexListener;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Map<String, Object> netids;

    // UI references.
    private EditText mNetView;
    private View mLoginFormView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.jelanidenis.courseq.R.layout.simple_signin);

        Log.i(TAG, "In onCreate()");

        //Get Database Instance
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Get Auth Instance
        mAuth = FirebaseAuth.getInstance();

        //Create Auth Listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        //create IndexListener
        Log.d(TAG, "right before listener");

        //load all netids for signin() method
        mUserIndex = mDatabase.child("user_index");

        // Add value event listener to the netid query
        ValueEventListener IndexListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get netid index and check that its not null
                netids = (Map<String, Object>) dataSnapshot.getValue();
                Log.d(TAG, "onDataChange executed");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting netid failed, log a message
                Log.w(TAG, "loadNetID:onCancelled");
                Toast.makeText(SimpleSignIn.this, "Failed to load netids.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        mIndexListener = IndexListener;
        Log.d(TAG, "after listener");

        //Add Listeners
        //to Firebase Auth instance (for state changes) and UserIndex reference to get netids
        mAuth.addAuthStateListener(mAuthListener);
        mUserIndex.addListenerForSingleValueEvent(mIndexListener);

        // Set up the login form.
        mNetView = (EditText) findViewById(com.jelanidenis.courseq.R.id.netid);

        //setOnEditorActionListener simply allows you to perform action if user presses Enter Key
        //instead of explicitly selecting login button (lazy user)
        mNetView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == com.jelanidenis.courseq.R.id.login || id == EditorInfo.IME_NULL) {
                    // Reset error.
                    mNetView.setError(null);
                    String netid = mNetView.getText().toString();
                    Log.w(TAG, netid);
                    if (netid.equals("")) { //no netid was provided
                        mNetView.setError(getString(com.jelanidenis.courseq.R.string.error_field_required));
                        mNetView.requestFocus();
                    }
                    else {
                        SignIn(netid);
                    }
                    return true;
                }
                return false;
            }
        });

        //set up signin/signup buttons
        Button mSignInButton = (Button) findViewById(com.jelanidenis.courseq.R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset error.
                mNetView.setError(null);
                CharSequence netid = mNetView.getText();
                Log.w(TAG, netid.toString());
                if (netid == null) { //no netid was provided
                    mNetView.setError(getString(com.jelanidenis.courseq.R.string.error_field_required));
                    mNetView.requestFocus();
                }
                else {
                    SignIn(netid.toString());
                }
            }
        });

        Button mSignUpButton = (Button) findViewById(com.jelanidenis.courseq.R.id.sign_up_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { SignUp();
            }
        });

        mLoginFormView = findViewById(com.jelanidenis.courseq.R.id.login_form);
        mProgressView = findViewById(com.jelanidenis.courseq.R.id.login_progress);
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "In onStart()");

        //In order to read/write data we sign in to a universal, pre-made dummy account
        String email = "jdenis@princeton.edu";
        String password = "Bando657Yo";
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed");
                            Toast.makeText(SimpleSignIn.this, com.jelanidenis.courseq.R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });


        //Log.d(TAG, "Check that sign in was successful");
    }

    private void SignUp() {
        Intent intent = new Intent(this, SimpleSignUp.class);
        startActivity(intent);
    }

    /*
     * Attempts to sign in account specified by the login form.
     * If netid is not yet registered, user taken to sign up activity
     */
    private void SignIn(String netid) {

        Log.i(TAG, "In SignIn()");

        // Reset error.
        mNetView.setError(null);

        //showProgress(true);
        //showProgress(false);

        // If netid is registered, proceed to Add A Class Page
        // else prompt user to sign up
        if (netids.containsKey(netid)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("Netid", netid);
            startActivity(intent);
        }

        else {
            mNetView.setError(getString(com.jelanidenis.courseq.R.string.error_netid_not_found));
            mNetView.requestFocus();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "In onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "In onPause()");
    }

    @Override
    public void onRestart() {
        super.onResume();
        Log.i(TAG, "In onRestart()");

        //Add Listeners Becuase we know they we removed during onStop()
        //to Firebase Auth instance (for state changes) and UserIndex reference to get netids
        mAuth.addAuthStateListener(mAuthListener);
        mUserIndex.addListenerForSingleValueEvent(mIndexListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "In onStop()");

        // Remove event listener before pausing
        if (mIndexListener != null) {
            mUserIndex.removeEventListener(mIndexListener);
        }

        // Remove previously added state listener for auth
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}
