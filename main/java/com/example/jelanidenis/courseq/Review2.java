package com.example.jelanidenis.courseq;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.api.model.StringList;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class Review2 extends AppCompatActivity {

    private static final String TAG = "Review2";

    //Firebase References
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference rDatabase;
    private DatabaseReference rNeighbors;
    private DatabaseReference rUsers;

    //Useful Storage Objects
    private String yr = "";
    private String netid;
    private String course;
    private Map<String, Object> neighbor_map_2;
    private Map<String, Object> major_map;
    private Map<String, Object> neighbor_map;
    private Map<String, Object> user_map;
    ArrayList<String> rating;

    private float rating1 = 0.0f;
    private float rating2 = 0.0f;
    private float rating3 = 0.0f;
    private float rating4 = 0.0f;
    private float rating5 = 0.0f;

    // UI references.
    private TextView banner;
    private EditText year;

    private RatingBar bar1;
    private RatingBar bar2;
    private RatingBar bar3;
    private RatingBar bar4;
    private RatingBar bar5;

    private View mLoginFormView;
    private View mProgressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In onCreate()");
        super.onCreate(savedInstanceState);

        Intent previous = getIntent();
        netid = previous.getStringExtra("Netid");
        course = previous.getStringExtra("Course");

        setContentView(R.layout.review2);

        //Get Auth Instance
        mAuth = FirebaseAuth.getInstance();

        //Create Auth Listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.i(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.i(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        //Add Listener to Auth Instance
        mAuth.addAuthStateListener(mAuthListener);

        //In order to read/write data we sign in to a universal, pre-made dummy account
        String email = "jdenis@princeton.edu";
        String password = "Bando657Yo";
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.i(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(Review2.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });


        //Get Database Instance
        rDatabase = FirebaseDatabase.getInstance().getReference();


            //UI STUFF//

        //Initalize UI References
        bar1 = (RatingBar) findViewById(R.id.ratingBar1);
        bar2 = (RatingBar) findViewById(R.id.ratingBar2);
        bar3 = (RatingBar) findViewById(R.id.ratingBar3);
        bar4 = (RatingBar) findViewById(R.id.ratingBar4);
        bar5 = (RatingBar) findViewById(R.id.ratingBar5);

        bar1.setIsIndicator(true);
        bar2.setIsIndicator(true);
        bar3.setIsIndicator(true);
        bar4.setIsIndicator(true);
        bar5.setIsIndicator(true);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //set up buttons
        Button BackButton = (Button) findViewById(R.id.back_button);
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Back();
            }
        });

        Button SignOutButton = (Button) findViewById(R.id.sign_out_button);
        SignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });

        //showProgress(true);
        getUsers();

    };

    private void setRatings() {
        //showProgress(false);

        //Set ratings
        bar1.setRating(rating1);
        bar2.setRating(rating1);
        bar3.setRating(rating1);
        bar4.setRating(rating1);
        bar5.setRating(rating1);
    }

    private void addCourse() {
        Log.i(TAG, "in addCourse()");
        //Add course info to user history
        rating = new ArrayList<String>(5);
        rating.add(0, rating1 + "");
        rating.add(1, rating2 + "");
        rating.add(2, rating3 + "");
        rating.add(3, rating4 + "");
        rating.add(4, rating5 + "");

        HashMap<String, Object> my_map = new HashMap<>();
        my_map.put("rating", rating);
        my_map.put("yr", yr);

        rDatabase.child("users").child(netid).child("history").child(course).setValue(my_map);

        //update Course statistics
        DatabaseReference rCourse = rDatabase.child("courses").child(course);

        ValueEventListener courseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> my_map = (Map<String, Object>) dataSnapshot.getValue();
                double num_students = Double.parseDouble(my_map.get("students") + "");
                rDatabase.child("courses").child(course).child("students").setValue((num_students + 1.0) + "");

                if (num_students == 0) {
                    rDatabase.child("courses").child(course).child("average").setValue(rating);
                }
                else {
                    ArrayList<String> average = (ArrayList<String>) my_map.get("average");
                    for (int i = 0; i < 5; i++) {
                        average.set(i, ((Double.parseDouble(rating.get(i))/(num_students + 1.0)) +
                                Double.parseDouble(average.get(i))*(num_students / (num_students + 1.0))) + "");
                    }
                    rDatabase.child("courses").child(course).child("average").setValue(average);

                }


                //force finish before neighbor update
                getUsers();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "studentListener:onCancelled", databaseError.toException());
                Toast.makeText(Review2.this, "Failed to load course statistics.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        rCourse.addListenerForSingleValueEvent(courseListener);

    }

    private void getUsers() {
        Log.i(TAG, "in getUsers()");

        //Reference to user informaion
        rUsers = rDatabase.child("users");

        //Set up and add listener
        ValueEventListener UserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_map = (Map<String, Object>) dataSnapshot.getValue();
                syncUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "syncCourses:onCancelled", databaseError.toException());
            }
        };

        //initialize user map
        rUsers.addListenerForSingleValueEvent(UserListener);

    }

    private void syncUsers() {

        //Reference to user informaion
        rUsers = rDatabase.child("users");

        //Set up and add listener
        ChildEventListener UserListenerTwo = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!user_map.containsKey(dataSnapshot.getKey())) {
                    user_map.put(dataSnapshot.getKey(), dataSnapshot.getValue());
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                user_map.put(dataSnapshot.getKey(), dataSnapshot.getValue());
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                user_map.remove(dataSnapshot.getKey());
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //sync user data continuously
        rUsers.addChildEventListener(UserListenerTwo);

        //getNeighbors
        kernel();

    }

    private void kernel() {

        HashMap<String, Object> user = (HashMap<String, Object>) user_map.get(netid);
        if (user.containsKey("neighbors")) {
            HashMap<String, Object> neighbors = (HashMap<String, Object>) user.get("neighbors");
            HashMap<String, Object> above = new HashMap<>();
            HashMap<String, Object> below = new HashMap<>();

            if (neighbors.containsKey("above")) {
                above = (HashMap<String, Object>) neighbors.get("above");
            }

            if (neighbors.containsKey("below")) {
                below = (HashMap<String, Object>) neighbors.get("below");
            }

            if (above.size() + below.size() < 5) {
                general();
            } else {
                HashMap<String, Object> n_user;
                HashMap<String, Object> history;
                HashMap<String, Object> info;

                int below_count = 0;
                int above_count = 0;

                for (String neighbor : above.keySet()) {
                    n_user = (HashMap<String, Object>) user_map.get(neighbor);
                    history = (HashMap<String, Object>) n_user.get("history");

                    if(!history.containsKey(course)) continue;

                    above_count++;

                    info = (HashMap<String, Object>) history.get(course);
                    ArrayList<String> rating = (ArrayList<String>) info.get("rating");

                    rating1 += Float.parseFloat(rating.get(0));
                    rating2 += Float.parseFloat(rating.get(1));
                    rating3 += Float.parseFloat(rating.get(2));
                    rating4 += Float.parseFloat(rating.get(3));
                    rating5 += Float.parseFloat(rating.get(4));
                }

                for (String neighbor : below.keySet()) {
                    n_user = (HashMap<String, Object>) user_map.get(neighbor);
                    history = (HashMap<String, Object>) n_user.get("history");

                    if(!history.containsKey(course)) continue;

                    below_count++;

                    info = (HashMap<String, Object>) history.get(course);
                    ArrayList<String> rating = (ArrayList<String>) info.get("rating");

                    rating1 += Float.parseFloat(rating.get(0));
                    rating2 += Float.parseFloat(rating.get(1));
                    rating3 += Float.parseFloat(rating.get(2));
                    rating4 += Float.parseFloat(rating.get(3));
                    rating5 += Float.parseFloat(rating.get(4));
                }

                rating1 = rating1 / ((float) (above_count + below_count));
                rating2 = rating2 / ((float) (above_count + below_count));
                rating3 = rating3 / ((float) (above_count + below_count));
                rating4 = rating4 / ((float) (above_count + below_count));
                rating5 = rating5 / ((float) (above_count + below_count));

                setRatings();
            }
        }


        else {
            general();
        }

    }

    private void general() {

        HashMap<String, Object> user = (HashMap<String, Object>) user_map.get(netid);

        final String major = (String) user.get("major");

        //Create Majors Listener
        ValueEventListener MajorListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                major_map = (Map<String, Object>) dataSnapshot.getValue();
                Log.i(TAG, "majors was just set");

                //Force a return to ensure Listeners UI gets initialized before onStart() called
                next(major);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        //references for Listener Data Queries
        DatabaseReference mMajors = rDatabase.child("majors");
        /*Query mMajors = mDatabase.child("majors").orderByKey();*/

        //Retrieve the Data we need
        mMajors.addListenerForSingleValueEvent(MajorListener);

    }


    private void next(String major) {
        //find major sector and type for grouping
        Map<String, String> both = (Map<String, String>) major_map.get(major);
        String sector = both.get("Sector");
        String type = both.get("type");

        //Retrieve Proper Data
        DatabaseReference retrieve = rDatabase.child("groups").child(type).child(sector);

        //Create Listener
        ValueEventListener nListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                neighbor_map_2 = (Map<String, Object>) dataSnapshot.getValue();
                Log.i(TAG, "neighbor_map_2 was just set");

                //Force a return to ensure Listeners UI gets initialized before onStart() called
                next2();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        retrieve.addListenerForSingleValueEvent(nListener);
    }

    private void next2() {

        HashMap<String, Object> n_user;
        HashMap<String, Object> history;
        HashMap<String, Object> info;

        int count = 0;

        for (String neighbor : neighbor_map_2.keySet()) {
            if (user_map.containsKey(neighbor)) {
                n_user = (HashMap<String, Object>) user_map.get(neighbor);
                history = (HashMap<String, Object>) n_user.get("history");

                if(history == null) continue;
                if(!history.containsKey(course)) continue;

                count++;

                info = (HashMap<String, Object>) history.get(course);
                ArrayList<String> rating = (ArrayList<String>) info.get("rating");

                rating1 += Float.parseFloat(rating.get(0));
                rating2 += Float.parseFloat(rating.get(1));
                rating3 += Float.parseFloat(rating.get(2));
                rating4 += Float.parseFloat(rating.get(3));
                rating5 += Float.parseFloat(rating.get(4));
            }
        }

        rating1 = rating1 / ((float) (count));
        rating2 = rating2 / ((float) (count));
        rating3 = rating3 / ((float) (count));
        rating4 = rating4 / ((float) (count));
        rating5 = rating5 / ((float) (count));

        setRatings();
    }

    public void Back() {
        //return to add_a_class2 activity
        Log.i(TAG, "In Back()");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void SignOut() {
        //return to add_a_class2 activity
        Log.i(TAG, "In SignOut()");
        Intent intent = new Intent(this, SimpleSignIn.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "In onStart()");
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

        //Add Auth Listener Becuase we know it was removed during onStop()
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "In onStop()");

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
