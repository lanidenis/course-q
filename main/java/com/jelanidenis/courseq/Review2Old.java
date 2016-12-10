package com.jelanidenis.courseq;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.jelanidenis.courseq.MainActivity;
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
import java.util.Set;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class Review2Old extends AppCompatActivity {

    private static final String TAG = "Review2Old";

    //Firebase References
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference rDatabase;
    private DatabaseReference rUsers;

    //Current Course Info
    private String yr = "";
    private String netid;
    private String course;

    //User Profiles (to extract ratings)
    private Map<String, Object> user_map;

    //Information to generate review
    HashMap<String, Object> course_map;

    //retrieve NN list, course NN, majors, supermajors, average
    private HashMap<String, Object> user_nn;
    private HashMap<String, Object> course_nn;
    private HashMap<String, Boolean> majors = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> supermajors = new HashMap<String, Boolean>();
    private ArrayList<String> average;

    // UI references.
    private RatingBar bar1, bar2, bar3, bar4, bar5;

    private View mLoginFormView;
    private View mProgressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In onCreate()");
        super.onCreate(savedInstanceState);

        Intent previous = getIntent();
        netid = previous.getStringExtra("Netid");
        course = previous.getStringExtra("Course");

        setContentView(com.jelanidenis.courseq.R.layout.review2);

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
                            Toast.makeText(Review2Old.this, com.jelanidenis.courseq.R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });


        //Get Database Instance
        rDatabase = FirebaseDatabase.getInstance().getReference();


            //UI STUFF//

        //Initalize UI References
        bar1 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar1);
        bar2 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar2);
        bar3 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar3);
        bar4 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar4);
        bar5 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar5);

        mLoginFormView = findViewById(com.jelanidenis.courseq.R.id.login_form);
        mProgressView = findViewById(com.jelanidenis.courseq.R.id.login_progress);

        //set up buttons
        Button BackToMain = (Button) findViewById(com.jelanidenis.courseq.R.id.back_button);
        BackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackToMain();
            }
        });

        Button SignOutButton = (Button) findViewById(com.jelanidenis.courseq.R.id.sign_out_button);
        SignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });

        //showProgress(true);

        //get user profiles
        getUsers();

    };

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

        //retrieve NN list
        getUserNeighbors();

    }

    private void getUserNeighbors() {
        //Reference to neighbor map information
        DatabaseReference rNeighbors = rDatabase.child("user_neighbors").child(netid);

        //Set up and add listener
        ValueEventListener CurrentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot == null) { //no user NN's
                    getCourseNeighbors();
                }

                user_nn = (HashMap<String, Object>) dataSnapshot.getValue();
                getCourseNeighbors();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getUserNeighbors:onCancelled", databaseError.toException());
            }
        };

        //get user neighbors
        rNeighbors.addListenerForSingleValueEvent(CurrentListener);
    }

    private void getCourseNeighbors() {
        //Reference to course neighbor map
        DatabaseReference rMajor = rDatabase.child("course_neighbors").child(course);

        //Set up and add listener
        ValueEventListener CurrentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //no course NN's
                if (dataSnapshot == null) getMajor();

                course_nn = (HashMap<String, Object>) dataSnapshot.getValue();
                getMajor();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getCourseNeighbors:onCancelled", databaseError.toException());
            }
        };

        //get course neighbors
        rMajor.addListenerForSingleValueEvent(CurrentListener);
    }


    private void getMajor() {

        HashMap<String, Object> user = (HashMap<String, Object>) user_map.get(netid);
        final String major = (String) user.get("major");

        //create reference to majors
        DatabaseReference mMajor = rDatabase.child("majors").child(major);

        //Create Major Listener
        ValueEventListener MajorListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> major_info = (HashMap<String, Object>) dataSnapshot.getValue();
                String sector = (String) major_info.get("Sector");
                String type = (String) major_info.get("type");
                getSuperMajors(sector, type);
            };

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getMajor:onCancelled", databaseError.toException());
            }
        };

        //add Listener
        mMajor.addListenerForSingleValueEvent(MajorListener);

    }


    private void getSuperMajors(String sector, String type) {

        final String type2 = type;
        final String sector2 = sector;

        //reference
        DatabaseReference rSuper = rDatabase.child("groups").child(type);

        //Create Listener
        ValueEventListener CurrentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) getAverage(); //no supers or major

                if (type2.equals("BSE")) {
                    HashMap<String, Object> engineers = (HashMap<String, Object>) dataSnapshot.getValue();
                    for (String egr_major : engineers.keySet()) {
                        HashMap<String, Object> netids = (HashMap<String, Object>) engineers.get(egr_major);

                        if (egr_major.equals(sector2)) {
                            for (String curr_id : netids.keySet()) {
                                majors.put(curr_id, true);
                            }
                        }
                        else {
                            for (String curr_id : netids.keySet()) {
                                supermajors.put(curr_id, true);
                            }
                        }
                    }
                }
                else {
                    HashMap<String, Object> arts = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (sector2.equals("Humanities") || sector2.equals("Art and Music") || sector2.equals("Language")) {
                        for (String art_major : arts.keySet()) {
                            HashMap<String, Object> netids = (HashMap<String, Object>) arts.get(art_major);

                            if (art_major.equals(sector2)) {
                                for (String curr_id : netids.keySet()) {
                                    majors.put(curr_id, true);
                                }
                            } else if (!art_major.equals("Science") && !art_major.equals("Economics")) {
                                for (String curr_id : netids.keySet()) {
                                    supermajors.put(curr_id, true);
                                }
                            }
                        }
                    }
                    else {
                        for (String art_major : arts.keySet()) {
                            HashMap<String, Object> netids = (HashMap<String, Object>) arts.get(art_major);

                            if (art_major.equals(sector2)) {
                                for (String curr_id : netids.keySet()) {
                                    majors.put(curr_id, true);
                                }
                            } else if (art_major.equals("Science") || art_major.equals("Economics")) {
                                for (String curr_id : netids.keySet()) {
                                    supermajors.put(curr_id, true);
                                }
                            }
                        }
                    }
                }

                getAverage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getSuperMajors:onCancelled", databaseError.toException());
            }
        };

        //add Listener
        rSuper.addListenerForSingleValueEvent(CurrentListener);
    }

    private void getAverage() {

        DatabaseReference rCourse = rDatabase.child("courses");

        ValueEventListener courseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                course_map = (HashMap<String, Object>) dataSnapshot.getValue();
                HashMap<String, Object> my_course = (HashMap<String, Object>) course_map.get(course);
                double num_students = Double.parseDouble(my_course.get("students") + "");
                if (num_students == 0.0) computePrediction(); //nobody has taken this class yet

                average = (ArrayList<String>) my_course.get("average");
                average.add(num_students + "");
                computePrediction();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "courseListener:onCancelled", databaseError.toException());
            }
        };

        rCourse.addListenerForSingleValueEvent(courseListener);
    }

    ArrayList<Float> ratings = new ArrayList<Float>();
    ArrayList<Boolean> found = new ArrayList<Boolean>();


    private Float[] getRatings(Set<String> user_ids, ArrayList<String> courses, int I) {

        float sum = 0.0f;
        float count = 0.0f;
        for (String user_id : user_ids) {
            HashMap<String, Object> user = (HashMap<String, Object>) user_map.get(user_id);
            if (!user.containsKey("history")) continue;
            else {
                HashMap<String, Object> history = (HashMap<String, Object>) user.get("history");
                //iterate over all course NN's
                for (int j = 0; j < courses.size(); j++) {
                    if (!history.containsKey(courses.get(j))) continue;
                    else {
                        HashMap<String, Object> course_info = (HashMap<String, Object>) history.get(courses.get(j));
                        ArrayList<String> rating = (ArrayList<String>) course_info.get("rating");
                        sum += Float.parseFloat(rating.get(I));
                        count += 1.0f;
                    }
                }
            }
        }

        Float[] result = new Float[2];
        result[0] = sum;
        result[1] = count;
        return result;

    }

    private void computePrediction() {

        ArrayList<ArrayList<String>> similar = new ArrayList<ArrayList<String>>();
        //intiliaze 5 empty array lists for each category
        for (int i = 0; i < 5; i++) {
            similar.add(new ArrayList<String>());
        }
        /*ArrayList<ArrayList<String>> dissimilar = new ArrayList<ArrayList<String>>();*/


        //THIS PART DECIDES WHETHER TO LOOK AT COURSE NN OR LOOK DIRECTLY AT COURSE (WITH POSSIBILITY OF RETURNING
        //AN IMPERSONAL AVERAGE

        //SIDENOTE THIS IMPLEMENTATION DOES NOT YET CONSIDER THE STRENGTH OF RATINGS
        if (average == null) { //no one has taken the course, consult all course_nn's hoping one of them has review

            //consult nearest neighbors of course (THIS IMPLEMENTATION ONLY EXTRACTS SIMILAR COURSE NN's)
            for (String course_n : course_nn.keySet()) {
                ArrayList<String> similarity = (ArrayList<String>) course_nn.get(course_n);
                for (int i = 0; i < 5; i++) {
                    if (Double.parseDouble(similarity.get(i)) >= 0.7) {
                        similar.get(i).add(course_n);
                    }
                    /*else dissimilar.get(i).add(course_n);*/
                }
            }

        }

        else { //someone has taken the course, let's hope one of the NN/supers/majors did / and reviewed it

            for (int i = 0; i < 5; i++) { //similar arraylist of arraylist will only contain course
                similar.get(i).add(course);             //of interest for each category iteration
            }
        }

        //for each of the 5 categories, average over available ratings from NN/major/supermajor histories
        for (int i = 0; i < 5; i++) {
            if (user_nn == null) {//consult majors

                if(majors.size() == 0) {//consult supermajors

                    if(supermajors.size() == 0) { //report averages

                        float sum = 0.0f;
                        float count = 0.0f;
                        for (int j = 0; j < similar.get(i).size(); j++) {
                            HashMap<String, Object> my_course = (HashMap<String, Object>) course_map.get(similar.get(i).get(j));
                            if (!my_course.containsKey("average")) continue;
                            else {
                                ArrayList<String> average = (ArrayList<String>) my_course.get("average");
                                sum += Float.parseFloat(average.get(i));
                                count += 1.0f;
                            }
                        }
                        if (count == 0.0f) {
                            found.add(false);
                            ratings.add(0.0f);
                        }
                        else {
                            found.add(true);
                            ratings.add(sum / count);
                        }
                    }

                    else {//consult supermajors

                        Float[] result = getRatings(supermajors.keySet(), similar.get(i), i);

                        if (result[1] == 0.0f) {
                            found.add(false);
                            ratings.add(0.0f);
                        }
                        else {
                            found.add(true);
                            ratings.add(result[0] / result[1]);
                        }
                    }
                }
                else {//consult majors

                    Float[] result = getRatings(majors.keySet(), similar.get(i), i);

                    if (result[1] == 0.0f) {
                        found.add(false);
                        ratings.add(0.0f);
                    }
                    else {
                        found.add(true);
                        ratings.add(result[0] / result[1]);
                    }
                }
            }

            else {// consult user NN's

                HashMap<String, Float> sim_users = new HashMap<String, Float>();
                HashMap<String, Float> diff_users = new HashMap<String, Float>();

                //separate diff and sim NN's
                for (String user_id : user_nn.keySet()) {
                    ArrayList<String> similarity = (ArrayList<String>) user_nn.get(user_id);
                    Float sim = Float.parseFloat(similarity.get(i));
                    if (sim >= 0.7f) {
                        sim_users.put(user_id, sim);
                    }
                    else {
                        diff_users.put(user_id, sim);
                    }
                }

                //THIS IMPlEMENATION DOES NOT DO WEIGHTED AVERAGE OF RATINGS BASED ON
                //MAGNITUDE OF SIMILARITY SCORE , IT JUST DOES SIMPLE AVERAGE OF ALL SIM NN's / DIFF NN's

                Float[] result = getRatings(sim_users.keySet(), similar.get(i), i);
                float sim_sum = result[0];
                float sim_count = result[1];

                Float[] result2 = getRatings(diff_users.keySet(), similar.get(i), i);
                float diff_sum = result2[0];
                float diff_count = result2[1];

                //Based on whether any opposite NNs took the class, or any sim ones did, or both
                //for each case set current rating appropriately
                if (sim_count == 0.0f) {

                    if(diff_count == 0.0f) {
                        found.add(false);
                        ratings.add(0.0f);
                    }

                    else {//do the opposite as diff NN's
                        diff_sum = diff_sum / diff_count;
                        found.add(true);
                        ratings.add(5.0f - diff_sum);
                    }
                }

                else {
                    sim_sum = sim_sum / sim_count;

                    if (diff_count == 0.0f) { //do the same as sim NN's
                        found.add(true);
                        ratings.add(sim_sum);
                    }

                    else { //do average of sim and (opp of diff)
                        diff_sum = diff_sum / diff_count;
                        found.add(true);
                        ratings.add((sim_sum + 5.0f - diff_sum) / 2.0f);
                    }
                }
            }
        }

        setRatings(ratings);

    }

    private void setRatings(ArrayList<Float> ratings) {
        //showProgress(false);

        if (!found.get(0)) {
            Toast.makeText(Review2Old.this, "We're Sorry. No information can be found for category 1",
                    Toast.LENGTH_LONG).show();
        }
        else bar1.setRating(ratings.get(0));

        if (!found.get(1)) {
            Toast.makeText(Review2Old.this, "We're Sorry. No information can be found for category 2",
                    Toast.LENGTH_LONG).show();
        }
        else bar2.setRating(ratings.get(1));

        if (!found.get(2)) {
            Toast.makeText(Review2Old.this, "We're Sorry. No information can be found for category 3",
                    Toast.LENGTH_LONG).show();
        }
        else bar3.setRating(ratings.get(2));

        if (!found.get(3)) {
            Toast.makeText(Review2Old.this, "We're Sorry. No information can be found for category 4",
                    Toast.LENGTH_LONG).show();
        }
        else bar4.setRating(ratings.get(3));

        if (!found.get(4)) {
            Toast.makeText(Review2Old.this, "We're Sorry. No information can be found for category 5",
                    Toast.LENGTH_LONG).show();
        }
        else bar5.setRating(ratings.get(4));
    }

    public void BackToMain() {
        //go back to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    public void SignOut() {
        //return to SignIn activity
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
