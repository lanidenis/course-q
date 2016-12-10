package com.jelanidenis.courseq;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class Add_A_Class2 extends AppCompatActivity {

    private static final String TAG = "Add_A_Class2";

    //Firebase References
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference rDatabase;
    private DatabaseReference rUsers;

    //Useful Storage Objects
    private String yr = "";
    private String netid;
    private String course;
    private Map<String, Object> history;
    private Map<String, Object> neighbor_map;
    private Map<String, Object> user_map;
    ArrayList<String> rating;

    private double rating1 = 6.0;
    private double rating2 = 6.0;
    private double rating3 = 6.0;
    private double rating4 = 6.0;
    private double rating5 = 6.0;


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

        setContentView(com.jelanidenis.courseq.R.layout.add_a_class2);

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
                            Log.i(TAG, "signInWithEmail:failed");
                            Toast.makeText(Add_A_Class2.this, com.jelanidenis.courseq.R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });


        //Get Database Instance
        rDatabase = FirebaseDatabase.getInstance().getReference();


        /*
        //double set everything so I can move the hell on
        ArrayList<Double> temp = new ArrayList<Double>(2);
        temp.add(0, (Double) 0.4); temp.add(1, (Double) 1.0);
        rDatabase.child("user_neighbors/cdenis/jdenis").setValue(temp);
        rDatabase.child("user_neighbors/jdenis/cdenis").setValue(temp);

        temp = new ArrayList<Double>(5);
        temp.add(0, 2.0); temp.add(1, 4.0); temp.add(2, 5.0); temp.add(3, 1.0); temp.add(4, 3.0);
        rDatabase.child("users/jdenis/history/COS 226/rating").setValue(temp);
        rDatabase.child("users/jdenis/history/COS 226/yr").setValue((double) 1.0);

        temp.add(0, 1.0); temp.add(1, 2.0); temp.add(2, 3.0); temp.add(3, 3.0); temp.add(4, 3.0);
        rDatabase.child("users/cdenis/history/COS 226/rating").setValue(temp);
        rDatabase.child("users/cdenis/history/COS 226/yr").setValue((double) 1.0);

        temp.add(0, 1.5); temp.add(1, 3.0); temp.add(2, 4.0); temp.add(3, 2.0); temp.add(4, 3.0);
        rDatabase.child("courses/COS 226/average").setValue(temp);

        rDatabase.child("courses/COS 217/students").setValue((double) 0.0);
        rDatabase.child("courses/COS 126/students").setValue((double) 0.0);
        rDatabase.child("courses/COS 226/students").setValue((double) 2.0);
        */

                    //UI STUFF//

        //Initalize UI References
        banner = (TextView) findViewById(com.jelanidenis.courseq.R.id.banner);
        year = (EditText) findViewById(com.jelanidenis.courseq.R.id.yr);

        bar1 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar1);
        bar2 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar2);
        bar3 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar3);
        bar4 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar4);
        bar5 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar5);

        mLoginFormView = findViewById(com.jelanidenis.courseq.R.id.login_form);
        mProgressView = findViewById(com.jelanidenis.courseq.R.id.login_progress);

        //Add banner
        String composite = "Add " + course;
        banner.setText(composite);

        //Listen for Ratings
        bar1.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating1 = (double) rating;
            }
        });

        bar2.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating2 = (double) rating;
            }
        });

        bar3.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating3 = (double) rating;
            }
        });

        bar4.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating4 = (double) rating;
            }
        });

        bar5.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating5 = (double) rating;
            }
        });

        //Initalize sign out button
        Button SignOut = (Button) findViewById(com.jelanidenis.courseq.R.id.sign_out_button);
        SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });

        //Initalize back to main button
        Button BackToMain = (Button) findViewById(com.jelanidenis.courseq.R.id.back_button);
        BackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackToMain();
            }
        });



        //Initalize add button
        Button AddButton = (Button) findViewById(com.jelanidenis.courseq.R.id.add_button);
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Reset errors.
                year.setError(null);
                yr = year.getText().toString();

                // Log.i(TAG, netid.toString());
                if (yr.equals("")) {
                    year.setError(getString(com.jelanidenis.courseq.R.string.error_field_required));
                    year.requestFocus();
                }
                else if (Double.parseDouble(yr) > 4.0 || Double.parseDouble(yr) < 1.0) {
                    year.setError(getString(com.jelanidenis.courseq.R.string.yr_integer));
                    year.requestFocus();
                }
                else if (rating1 == 6.0 || rating2 == 6.0 || rating3 == 6.0) {
                    Toast.makeText(Add_A_Class2.this, com.jelanidenis.courseq.R.string.rating_required,
                            Toast.LENGTH_SHORT).show();
                }
                else if (rating4 == 6.0 || rating5 == 6.0) {
                    Toast.makeText(Add_A_Class2.this, com.jelanidenis.courseq.R.string.rating_required,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    showProgress(true);
                    addCourse();
                }
            }
        });

    };

    private void addCourse() {
        Log.i(TAG, "in addCourse()");
        //Add course info to user history
        rating = new ArrayList<String>(5);
        rating.add(rating1 + "");
        rating.add(rating2 + "");
        rating.add(rating3 + "");
        rating.add(rating4 + "");
        rating.add(rating5 + "");

        HashMap<String, Object> my_map = new HashMap<>();
        my_map.put("rating", rating);
        my_map.put("yr", yr);

        rDatabase.child("users").child(netid).child("history").child(course).setValue(my_map);

        //update Course statistics
        DatabaseReference rCourse = rDatabase.child("courses").child(course);

        ValueEventListener courseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot == null) { //first time someone added this course
                    rDatabase.child("courses").child(course).child("average").setValue(rating);
                    rDatabase.child("courses").child(course).child("students").setValue(1.0 + "");
                }

                else {
                    Map<String, Object> my_map = (Map<String, Object>) dataSnapshot.getValue();
                    double num_students = Double.parseDouble(my_map.get("students") + "");
                    rDatabase.child("courses").child(course).child("students").setValue((num_students + 1.0) + "");

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
                Log.i(TAG, "studentListener:onCancelled");
                Toast.makeText(Add_A_Class2.this, "Failed to load course statistics.",
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
                Log.i(TAG, "syncCourses:onCancelled");
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
        userNeighbors();

    }


    private void userNeighbors() {

        //Reference to neighbor informaion
        DatabaseReference rNeighbors = rDatabase.child("user_neighbors");

        //Set up and add listener
        ValueEventListener NeighborListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                neighbor_map = (Map<String, Object>) dataSnapshot.getValue();

                HashMap<String, Object> updates = new HashMap<String, Object>();

                HashMap<String, Object> Neighbor;
                ArrayList<String> Sim_Strg;
                ArrayList<Float> Sim_Calc;

                for (String current : user_map.keySet()) {
                    if (current.equals(netid)) continue;
                    HashMap<String, Object> user = (HashMap<String, Object>) user_map.get(current);

                    if (!user.containsKey("history")) continue;
                    HashMap<String, Object> history = (HashMap<String, Object>) user.get("history");

                    if (history.containsKey(course)) {
                        HashMap<String, Object> info = (HashMap<String, Object>) history.get(course);
                        ArrayList<String> n_rating = (ArrayList<String>) info.get("rating");
                        float year = Float.parseFloat(info.get("yr") + "");

                        //Calculate Similarity for each Category
                        Sim_Calc = new ArrayList<Float>();
                        float similarity = 0.0f;
                        float difference = 0.0f;

                        for (int i = 0; i < 5; i++) {
                            difference = Math.abs(Float.parseFloat(n_rating.get(i)) - Float.parseFloat(rating.get(i)));
                            similarity = (5.0f - difference) / 5.0f; //scaled from 0 to 1

                            //apply penalty of 10% for every difference in year - i can play around with this parameter though
                            similarity *= (1.0f - Math.abs(year - Float.parseFloat(yr)) * (0.10f));
                            Sim_Calc.add(i, similarity);
                        }

                        //include new similiarity in both users' NN list in neighbor_map
                        if (neighbor_map.containsKey(current))
                        {//current already has a NN list

                            Neighbor = (HashMap<String, Object>) neighbor_map.get(current);
                            if (!Neighbor.containsKey(netid)) {//first time neighbor and netid have a class in common

                                    Sim_Strg = new ArrayList<String>();
                                    for (int i = 0; i < 5; i++) {
                                        Sim_Strg.add(i, Sim_Calc.get(i) + "");
                                    }
                                    Sim_Strg.add(5, 1.0 + "");
                            }

                            else {//update previous similarity - aka include current sim in running average

                                Sim_Strg = (ArrayList<String>) Neighbor.get(netid);
                                float strength = Float.parseFloat(Sim_Strg.get(5));
                                for (int i = 0; i < 5; i++) {
                                    Sim_Strg.set(i, Float.parseFloat(Sim_Strg.get(i)) * strength / (strength + 1.0f) + Sim_Calc.get(i) / (strength + 1.0f) + "");
                                }
                                Sim_Strg.set(5, (strength + 1.0f) + "");

                            }

                        }

                        else { //current does not yet have a NN list
                               //aka first time neighbor and netid have a class in common

                            Sim_Strg = new ArrayList<String>();
                            for (int i = 0; i < 5; i++) {
                                Sim_Strg.add(i, Sim_Calc.get(i) + "");
                            }
                            Sim_Strg.add(5, 1.0 + "");

                        }

                        //update current's NN list
                        rDatabase.child("user_neighbors").child(current).child(netid).setValue(Sim_Strg);

                        //store similarity to update netid's NN list later on
                        updates.put("/user_neighbors/" + netid + "/" + current, Sim_Strg);

                        Log.i(TAG, "put one in updates");
                    }
                }

                //it is possible that none of the users with course histories have actually taken
                // the class that is being added.  In this case, there are no NN candidates
                if (updates.size() == 0) {
                    //proceed to next step
                    courseNeighbors();
                }
                else {

                    //update NN list for netid, only those NN with whom sim has changed
                    rDatabase.updateChildren(updates);

                    //proceed to next step
                    courseNeighbors();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "syncCourses:onCancelled");
            }
        };

        //get and update the user neighbor map
        rNeighbors.addListenerForSingleValueEvent(NeighborListener);

    }

    private void courseNeighbors() {

        //Reference to neighbor informaion
        DatabaseReference mNeighbors = rDatabase.child("course_neighbors");

        //Set up and add listener
        ValueEventListener MovieListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> course_map = (HashMap<String, Object>) dataSnapshot.getValue();
                HashMap<String, Object> user = (HashMap<String, Object>) user_map.get(netid);
                HashMap<String, Object> history = (HashMap<String, Object>) user.get("history");
                HashMap<String, Object> updates = new HashMap<String, Object>();

                if (history.size() == 1) {
                    //no pairs to check, Return to Add_A_Class.java
                    showProgress(false);
                    Return();
                }

                ArrayList<String> Sim_Strg;
                ArrayList<Float> Sim_Calc = new ArrayList<Float>();
                HashMap<String, Object> Partner;

                //for every other class user has rated, update course-course similarities
                for (String partner : history.keySet()) {
                    if (partner.equals(course)) continue;

                    HashMap<String, Object> info = (HashMap<String, Object>) history.get(partner);
                    ArrayList<String> n_rating = (ArrayList<String>) info.get("rating");
                    float year = Float.parseFloat(info.get("yr") + "");

                    //Calculate Similarity for each Category
                    float similarity = 0.0f;
                    float difference = 0.0f;

                    for (int i = 0; i < 5; i++) {
                        difference = Math.abs(Float.parseFloat(n_rating.get(i)) - Float.parseFloat(rating.get(i)));
                        similarity = (5.0f - difference) / 5.0f; //scaled from 0 to 1

                        //apply penalty of 10% for every difference in year - i can play around with this parameter though
                        similarity *= (1.0f - Math.abs(year - Float.parseFloat(yr)) * (0.10f));
                        Sim_Calc.add(i, similarity);
                    }

                    //include new similiarity in both courses' NN list in course_map
                    if (course_map.containsKey(partner))
                    {//partner already has a NN list

                        Partner = (HashMap<String, Object>) course_map.get(partner);
                        if (!Partner.containsKey(course)) {//first time partner and course have a user in common

                            Sim_Strg = new ArrayList<String>();
                            for (int i = 0; i < 5; i++) {
                                Sim_Strg.add(i, Sim_Calc.get(i) + "");
                            }
                            Sim_Strg.add(5, 1.0 + "");
                        }

                        else {//update previous similarity - aka include current sim in running average

                            Sim_Strg = (ArrayList<String>) Partner.get(course);
                            float strength = Float.parseFloat(Sim_Strg.get(5));
                            for (int i = 0; i < 5; i++) {
                                Sim_Strg.set(i, Float.parseFloat(Sim_Strg.get(i)) * strength / (strength + 1.0f) + Sim_Calc.get(i) / (strength + 1.0f) + "");
                            }
                            Sim_Strg.set(5, strength + 1.0 + "");

                        }

                    }

                    else { //partner does not yet have a NN list
                        //aka also first time partner and course have a user in common

                        Sim_Strg = new ArrayList<String>();
                        for (int i = 0; i < 5; i++) {
                            Sim_Strg.add(i, Sim_Calc.get(i) + "");
                        }
                        Sim_Strg.add(5, 1.0 + "");

                    }

                    //update partner's NN list
                    rDatabase.child("course_neighbors").child(partner).child(course).setValue(Sim_Strg);

                    //store similarity to update netid's NN list later on
                    updates.put("/course_neighbors/" + course + "/" + partner, Sim_Strg);

                }


                //update NN list for course, only those NN with whom sim has changed
                rDatabase.updateChildren(updates);

                //return to add a class page
                showProgress(false);
                Return();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "syncCourses:onCancelled");
            }
        };


        //get and update the course neighbor map
        mNeighbors.addListenerForSingleValueEvent(MovieListener);

    }

    public void Return() {
        //return to add_a_class1 activity
        Log.i(TAG, "In Return()");
        Intent intent = new Intent(this, Add_A_Class.class);
        startActivity(intent);
    }

    public void SignOut() {
        //proceed to sign in activity
        Intent intent = new Intent(this, SimpleSignIn.class);
        startActivity(intent);
    }

    public void BackToMain() {
        //go back to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Netid", netid);
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
