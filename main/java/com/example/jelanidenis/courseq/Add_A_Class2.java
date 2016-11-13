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

public class Add_A_Class2 extends AppCompatActivity {

    private static final String TAG = "Add_A_Class2";

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

        setContentView(R.layout.add_a_class2);

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
                            Toast.makeText(Add_A_Class2.this, R.string.auth_failed,
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
        banner = (TextView) findViewById(R.id.banner);
        year = (EditText) findViewById(R.id.yr);

        bar1 = (RatingBar) findViewById(R.id.ratingBar1);
        bar2 = (RatingBar) findViewById(R.id.ratingBar2);
        bar3 = (RatingBar) findViewById(R.id.ratingBar3);
        bar4 = (RatingBar) findViewById(R.id.ratingBar4);
        bar5 = (RatingBar) findViewById(R.id.ratingBar5);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

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

        //Initalize add button
        Button AddButton = (Button) findViewById(R.id.add_button);
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Reset errors.
                year.setError(null);
                yr = year.getText().toString();

                // Log.i(TAG, netid.toString());
                if (yr.equals("")) {
                    year.setError(getString(R.string.error_field_required));
                    year.requestFocus();
                }
                else if (Double.parseDouble(yr) > 4.0 || Double.parseDouble(yr) < 1.0) {
                    year.setError(getString(R.string.yr_integer));
                    year.requestFocus();
                }
                else if (rating1 == 6.0 || rating2 == 6.0 || rating3 == 6.0) {
                    Toast.makeText(Add_A_Class2.this, R.string.rating_required,
                            Toast.LENGTH_SHORT).show();
                }
                else if (rating4 == 6.0 || rating5 == 6.0) {
                    Toast.makeText(Add_A_Class2.this, R.string.rating_required,
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
        getNeighbors();

    }


    private void getNeighbors() {

        //Reference to neighbor informaion
        rNeighbors = rDatabase.child("user_neighbors");

        //Set up and add listener
        ValueEventListener NeighborListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                neighbor_map = (Map<String, Object>) dataSnapshot.getValue();

                HashMap<String, Object> newbies = new HashMap<String, Object>();

                HashMap<String, Object> Neighbor;
                ArrayList<String> Similarity;

                HashMap<String, Object> user;
                HashMap<String, Object> history;
                HashMap<String, Object> info;
                ArrayList<String> n_rating;
                Double year;

                if (neighbor_map.containsKey(netid)) {
                    for (String neighbor : neighbor_map.keySet()) {
                        if (neighbor.equals(netid)) continue;
                        user = (HashMap<String, Object>) user_map.get(neighbor);
                        history = (HashMap<String, Object>) user.get("history");

                        Log.i(TAG, "We are here once");
                        if (history.containsKey(course)) {
                            info = (HashMap<String, Object>) history.get(course);
                            n_rating = (ArrayList<String>) info.get("rating");
                            year = Double.parseDouble(info.get("yr") + "");

                            //Calculate Distance
                            Double distance = 0.0;
                            Double difference;
                            for (int i = 0; i < 5; i++) {
                                difference = (Double.parseDouble(n_rating.get(i)) - Double.parseDouble(rating.get(i)));
                                distance += difference*difference;
                            }
                            distance = Math.sqrt(distance);

                            //Calculate Similarity
                            Double similarity = (Math.sqrt(125.0) - distance) / Math.sqrt(125.0);
                            //apply penalty for year difference, proportional to similarity size
                            //we can play with punishment percent parameter 0.15 if we want
                            similarity *= (1.0 - Math.abs(year - Double.parseDouble(yr)) * (0.15));

                            //get previous similarity
                            Neighbor = (HashMap<String, Object>) neighbor_map.get(neighbor);
                            Similarity = (ArrayList<String>) Neighbor.get(netid);

                            //update
                            Double strength = Double.parseDouble(Similarity.get(1));
                            Similarity.set(0, (Double.parseDouble(Similarity.get(0)) * strength / (strength + 1.0) + similarity / (strength + 1.0)) + "");
                            Similarity.set(1, (strength + 1.0) + "");

                            //update
                            rDatabase.child("user_neighbors").child(neighbor).child(netid).setValue(Similarity);
                            newbies.put(neighbor, Similarity);

                            Log.i(TAG, "put one in newbies");
                        }
                    }
                } else { //first time we are adding netid to neighbors_map
                    for (String neighbor : neighbor_map.keySet()) {
                        user = (HashMap<String, Object>) user_map.get(neighbor);
                        history = (HashMap<String, Object>) user.get("history");
                        if (history.containsKey(course)) {
                            info = (HashMap<String, Object>) user.get(course);
                            n_rating = (ArrayList<String>) info.get("rating");
                            year = Double.parseDouble(info.get("yr") + "");

                            //Calculate Distance
                            Double distance = 0.0;
                            Double difference;
                            for (int i = 0; i < 5; i++) {
                                difference = (Double.parseDouble(n_rating.get(i)) - Double.parseDouble(rating.get(i)));
                                distance += difference*difference;
                            }
                            distance = Math.sqrt(distance);

                            //Calculate Similarity
                            Double similarity = (Math.sqrt(125.0) - distance) / Math.sqrt(125.0);
                            //apply penalty for year difference, proportional to similarity size
                            //we can play with punishment percent parameter 0.15 if we want
                            similarity *= (1.0 - Math.abs(year - Double.parseDouble(yr)) * (0.15));

                            //define new similarity
                            Similarity = new ArrayList<String>();
                            Similarity.set(0, similarity + "");
                            Similarity.set(1, 1.0 + "");

                            //update
                            rDatabase.child("user_neighbors").child(neighbor).child(netid).setValue(Similarity);
                            newbies.put(neighbor, Similarity);

                        }
                    }
                }

                //it is possible that none of the users with course histories (i.e. can be found
                //in neighbor_map, have actually taken the class that is being added.  In this case,
                //work is done and we simply return to Add a Class page
                if (newbies.size() == 0) Return();
                else {

                    //update all similiarities for current netid in neighbor_map
                    rDatabase.child("user_neighbors").child(netid).setValue(newbies);

                    //sort candidates by strength to find median threshold
                    Double strength_median;
                    Double similarity_threshold = 0.6;

                    HashMap<String, Double> strengths = new HashMap<>();
                    //HashMap<String, Double> similarities = new HashMap<>();

                    for (String neighbor : newbies.keySet()) {
                        ArrayList<String> store = (ArrayList<String>) newbies.get(neighbor);
                        strengths.put(neighbor, Double.parseDouble(store.get(1)));
                        //similarities.put(neighbor, Double.parseDouble(store.get(0)));
                        Log.i(TAG, "put one in");
                    }

                    //Log.i(TAG, "" + strengths.size());

                    //find strength threshold aka median
                    Collection<Double> str_solo = strengths.values();

                    //Log.i(TAG, "" + str_solo.size());

                    Double[] arg_array = new Double[]{};
                    Double[] str_array = str_solo.toArray(arg_array);
                    Arrays.sort(str_array);

                    if (str_array.length % 2 == 0) {
                        int high = str_array.length / 2;
                        int low = high - 1;
                        strength_median = (str_array[high] + str_array[low]) / 2.0;
                    } else {
                        int mid = str_array.length / 2;
                        strength_median = str_array[mid];
                    }

                    //find and categorize candidates as above(reliable) or below(less reliable)
                    HashMap<String, Object> above = new HashMap<>();
                    HashMap<String, Object> below = new HashMap<>();
                    ArrayList<String> store;

                    for (String neighbor : newbies.keySet()) {
                        store = (ArrayList<String>) newbies.get(neighbor);
                        if (Double.parseDouble(store.get(0)) >= similarity_threshold) {
                            if (Double.parseDouble(store.get(1)) >= strength_median) {
                                above.put(neighbor, store);

                                //add to other user's nearest neighbor list
                                rDatabase.child("users").child(neighbor).child("neighbors")
                                        .child("above").child(netid).setValue(store);
                            } else {
                                below.put(neighbor, store);

                                //add to other user's nearest neighbor list
                                rDatabase.child("users").child(neighbor).child("neighbors")
                                        .child("below").child(netid).setValue(store);
                            }
                        }
                    }

                    //update nearest neighbors for netid of interest
                    rDatabase.child("users").child(netid).child("neighbors").child("above").setValue(above);
                    rDatabase.child("users").child(netid).child("neighbors").child("below").setValue(below);

                    //Return to Add_A_Class.java
                    showProgress(false);
                    Return();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "syncCourses:onCancelled", databaseError.toException());
            }
        };

        //initialize neighbor map
        rNeighbors.addListenerForSingleValueEvent(NeighborListener);

    };

    public void Return() {
        //return to add_a_class2 activity
        Log.i(TAG, "In Return()");
        Intent intent = new Intent(this, Add_A_Class.class);
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
