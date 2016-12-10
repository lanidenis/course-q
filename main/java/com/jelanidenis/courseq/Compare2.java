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
import java.util.Set;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class Compare2 extends AppCompatActivity {

    private static final String TAG = "Compare2";

    //Firebase References
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference rDatabase;
    private DatabaseReference rUsers;

    //Current Course Info
    private String netid;
    private String course1;
    private String course2;

    //User Profiles (to extract ratings)
    private Map<String, Object> user_map;

    //Information to generate reviews
    HashMap<String, Object> course_map;
    HashMap<String, Object> course_nnmap;

    //retrieve NN list, course NN, majors, supermajors, average
    private HashMap<String, Object> user_nn;
    private HashMap<String, Boolean> majors = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> supermajors = new HashMap<String, Boolean>();

    // UI references.
    private RatingBar bar11, bar21, bar31, bar41, bar51;
    private RatingBar bar12, bar22, bar32, bar42, bar52;
    private TextView label11, label21, label31, label41, label51;
    private TextView label12, label22, label32, label42, label52;
    private TextView question11, question21, question31, question41, question51;
    private TextView question12, question22, question32, question42, question52;

    private View mLoginFormView;
    private View mProgressView;

    //Store a ton of ref's
    TextView[] label1;
    TextView[] label2;
    TextView[] questions1;
    TextView[] questions2;
    RatingBar[] bar1;
    RatingBar[] bar2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In onCreate()");
        super.onCreate(savedInstanceState);

        Intent previous = getIntent();
        netid = previous.getStringExtra("Netid");
        course1 = previous.getStringExtra("Course1");
        course2  = previous.getStringExtra("Course2");

        setContentView(com.jelanidenis.courseq.R.layout.compare2);

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
                            Log.i(TAG, "signInWithEmail:failed" );
                            Toast.makeText(Compare2.this, com.jelanidenis.courseq.R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });


        //Get Database Instance
        rDatabase = FirebaseDatabase.getInstance().getReference();


            //UI STUFF//

        //Store the shit ton of references
        label1 = new TextView[5];
        label2 = new TextView[5];
        questions1 = new TextView[5];
        questions2 = new TextView[5];
        bar1 = new RatingBar[5];
        bar2 = new RatingBar[5];

        //Initalize UI References
        bar11 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar1_1);
        bar12 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar1_2);
        bar21 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar2_1);
        bar22 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar2_2);
        bar31 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar3_1);
        bar32 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar3_2);
        bar41 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar4_1);
        bar42 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar4_2);
        bar51 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar5_1);
        bar52 = (RatingBar) findViewById(com.jelanidenis.courseq.R.id.ratingBar5_2);

        bar1[0] = bar11; bar1[1] = bar21; bar1[2] = bar31; bar1[3] = bar41; bar1[4] = bar51;
        bar2[0] = bar12; bar2[1] = bar22; bar2[2] = bar32; bar2[3] = bar42; bar2[4] = bar52;

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

        //add course labels to left of ratings bars
        label11 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course1_1);
        label12 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course1_2);
        label21 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course2_1);
        label22 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course2_2);
        label31 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course3_1);
        label32 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course3_2);
        label41 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course4_1);
        label42 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course4_2);
        label51 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course5_1);
        label52 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course5_2);

        label1[0] = label11; label1[1] = label21; label1[2] = label31; label1[3] = label41; label1[4] = label51;
        label2[0] = label12; label2[1] = label22; label2[2] = label32; label2[3] = label42; label2[4] = label52;

        //add course labels to left of ratings bars
        for (int i = 0; i < 5; i++) {
            label1[i].setText(course1);
            label2[i].setText(course2);
        }

        //intialize question mark blocks
        question11 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question1_1);
        question12 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question1_2);
        question21 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question2_1);
        question22 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question2_2);
        question31 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question3_1);
        question32 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question3_2);
        question41 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question4_1);
        question42 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question4_2);
        question51 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question5_1);
        question52 = (TextView) findViewById(com.jelanidenis.courseq.R.id.question5_2);

        questions1[0] = question11; questions1[1] = question21; questions1[2] = question31; questions1[3] = question41; questions1[4] = question51;
        questions2[0] = question12; questions2[1] = question22; questions2[2] = question32; questions2[3] = question42; questions2[4] = question52;

        showProgress(true);

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
                Log.i(TAG, "syncCourses:onCancelled"  );
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
                    getMajor();
                }

                user_nn = (HashMap<String, Object>) dataSnapshot.getValue();
                getMajor();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getUserNeighbors:onCancelled"  );
            }
        };

        //get user neighbors
        rNeighbors.addListenerForSingleValueEvent(CurrentListener);
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
                Log.i(TAG, "getMajor:onCancelled"  );
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
                if (dataSnapshot == null) getCourseNeighbors(); //no supers or major

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

                getCourseNeighbors();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getSuperMajors:onCancelled"  );
            }
        };

        //add Listener
        rSuper.addListenerForSingleValueEvent(CurrentListener);
    }

    private void getCourseNeighbors() {
        //Reference to course neighbor map
        DatabaseReference rCourseNN = rDatabase.child("course_neighbors");

        //Set up and add listener
        ValueEventListener CurrentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //never null becuase i'm keeping some dummy start data inside firebase
                //if (dataSnapshot == null) getAllCoursesTaken();
                course_nnmap = (HashMap<String, Object>) dataSnapshot.getValue();
                getAllCoursesTaken();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getCourseNeighbors:onCancelled"  );
            }
        };

        //get course neighbors
        rCourseNN.addListenerForSingleValueEvent(CurrentListener);
    }

    private void getAllCoursesTaken() {

        DatabaseReference rCourse = rDatabase.child("courses");

        ValueEventListener courseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                course_map = (HashMap<String, Object>) dataSnapshot.getValue();
                Triage();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "courseListener:onCancelled"  );
            }
        };

        rCourse.addListenerForSingleValueEvent(courseListener);
    }

    private void Triage() { //determine best way to retrive ratings for each course

        //Store ratings results for both courses
        ArrayList<Float> ratings1, ratings2;
        ArrayList<Boolean> found1, found2;

        HashMap<String, Object> retrieve;

        retrieve = Triage2(course1);
        ratings1 = (ArrayList<Float>) retrieve.get("rating");
        found1 = (ArrayList<Boolean>) retrieve.get("found");

        retrieve = Triage2(course2);
        ratings2 = (ArrayList<Float>) retrieve.get("rating");
        found2 = (ArrayList<Boolean>) retrieve.get("found");

        setRatings(found1, ratings1, found2, ratings2);
    }

    private HashMap<String, Object> Triage2(String curr_course) { //execute triage

        HashMap<String, Object> toReturn = new HashMap<String, Object>();
        ArrayList<Float> rating = new ArrayList<Float>();
        ArrayList<Boolean> found = new ArrayList<Boolean>();
        ArrayList<ArrayList<String>> similar = new ArrayList<ArrayList<String>>();

        //intiliaze 5 empty array lists for each category
        for (int i = 0; i < 5; i++) {
            similar.add(new ArrayList<String>());
        }

        HashMap<String, Object> departmentals = new HashMap<String, Object>();

        if (!course_map.containsKey(curr_course)) { //no one has taken the course

            // try to get departmentals
            int j = -1;
            for (int i = 0; i < curr_course.length(); i++) {
                if (Character.isWhitespace(curr_course.charAt(i))) {
                    j = i;
                    break;
                }
            }
            String dept = curr_course.substring(0, j);

            for (String candidate : course_map.keySet()) {
                int k = -1;
                for (int i = 0; i < candidate.length(); i++) {
                    if (Character.isWhitespace(curr_course.charAt(i))) {
                        k = i;
                        break;
                    }
                }
                String cand = candidate.substring(0, j);

                if (cand.equals(dept)) { //add to departmentals's
                    departmentals.put(candidate, (Object) true);
                }
            }

            if (departmentals.size() == 0) { //abosolutely no rating info
                for (int i = 0; i < 5; i++) {
                    found.add(false);
                    rating.add(0.0f);
                }
            } else { //compute if any user_nn, majors, or supermajors have taken departmentals

                //reset to 5 empty array lists for each category
                for (int i = 0; i < 5; i++) {
                    similar.set(i, new ArrayList<String>());
                }

                for (String course_n : departmentals.keySet()) { //assume each dept course good estimator for each category
                    for (int i = 0; i < 5; i++) {
                        similar.get(i).add(course_n);
                    }
                    /*else dissimilar.get(i).add(course_n);*/
                }

                float[][] result = computePrediction(similar);

                for (int i = 0; i < 5; i++) {
                    if (result[0][i] == -1.0f) {
                        found.add(false);
                        rating.add(0.0f);
                    } else {
                        found.add(true);
                        rating.add(result[0][i]);
                    }
                }
            }


        } else { //someone has taken the course, possibly course_nn exist,
                // only use if strength of user_nn, or majors, or supermajors really small

            float[][] result_withNN = null;
            if (course_nnmap.containsKey(curr_course)) { //get nearest neighbors

                //reset to 5 empty array lists for each category
                for (int i = 0; i < 5; i++) {
                    similar.set(i, new ArrayList<String>());
                }

                HashMap<String, Object> course_nn = (HashMap<String, Object>) course_nnmap.get(curr_course);
                for (String course_n : course_nn.keySet()) {
                    ArrayList<String> similarity = (ArrayList<String>) course_nn.get(course_n);
                    for (int i = 0; i < 5; i++) {
                        if (Float.parseFloat(similarity.get(i)) >= 0.7) {
                            //add neighbor to similar arraylist for that category if its a good neighbor
                            similar.get(i).add(course_n);
                        }
                    /*else dissimilar.get(i).add(course_n);*/
                    }
                }

                result_withNN = computePrediction(similar);
            }

            //reset to 5 empty array lists for each category
            for (int i = 0; i < 5; i++) {
                similar.set(i, new ArrayList<String>());
            }

            //do computation over just the course at hand -
            // this is the normal case we hope for each time
            for (int i = 0; i < 5; i++) {
                similar.get(i).add(curr_course);
            }

            float[][] result_withoutNN = computePrediction(similar);

            if (result_withNN == null) {  //course had no nearest neighbors, proceed as best as possible
                for (int i = 0; i < 5; i++) {
                    if (result_withoutNN[0][i] == -1.0f) {
                        found.add(false);
                        rating.add(0.0f);
                    } else {
                        found.add(true);
                        rating.add(result_withNN[0][i]);
                    }
                }
            }

            else {

                for (int i = 0; i < 5; i++) {
                    if (result_withoutNN[0][i] == -1.0f) {
                        //now that i think of it we will never get inside here
                        //based on code logic only here if course was taken by somebody at very least
                        //course avg will populate all 5 categories
                        if(result_withNN[0][i] == -1.0f) {
                            found.add(false);
                            rating.add(0.0f);
                        }
                        else { //user nn rating since its all we got
                            found.add(true);
                            rating.add(result_withNN[0][i]);
                        }

                    } else { //we have the true rating
                        if(result_withNN[0][i] == -1.0f) { //user true rating its all we got
                            found.add(true);
                            rating.add(result_withoutNN[0][i]);
                            Log.i(TAG, (result_withoutNN[0][i]) + "user rating all we got");
                        }
                        else { //we have both ratings, check strength and apply average if necessary
                            found.add(true);
                            if (result_withNN[1][i] < 2)
                                rating.add((result_withoutNN[0][i] + result_withNN[0][i])/ 2.0f);
                            else
                                rating.add(result_withoutNN[0][i]);

                            Log.i(TAG, ((result_withoutNN[0][i] + result_withNN[0][i])/ 2.0f) + "");
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 5; i++)
            Log.i(TAG, found.get(i) + " " + i + " " + rating.get(i)) ;

        toReturn.put("rating", rating);
        toReturn.put("found", found);
        return toReturn;

    }


    private float[] getRatings(Set<String> user_ids, ArrayList<String> courses, int I) {

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

        float[] result = new float[2];
        result[0] = sum;
        result[1] = count;
        return result;

    }

    private float[][] computePrediction(ArrayList<ArrayList<String>> similar) {

        //second row saves the strenth of the prediciton i.e. number of separate ratings that went into average
        float[][] result = new float[2][5];

        //try to get predication through each group, using next group if none in previous took classes
        boolean continue_flag = false;

        for (int i = 0; i < 5; i++) {
            continue_flag = false; //reset continue flag for this category

            if (user_nn != null) {
                HashMap<String, Float> sim_users = new HashMap<String, Float>();
                HashMap<String, Float> diff_users = new HashMap<String, Float>();

                //separate diff and sim NN's
                for (String user_id : user_nn.keySet()) {
                    ArrayList<String> similarity = (ArrayList<String>) user_nn.get(user_id);
                    Float sim = Float.parseFloat(similarity.get(i));
                    if (sim >= 0.7f) {
                        sim_users.put(user_id, sim);
                    } else {
                        diff_users.put(user_id, sim);
                    }
                }

                //THIS IMPlEMENATION DOES NOT DO WEIGHTED AVERAGE OF RATINGS BASED ON
                //MAGNITUDE OF SIMILARITY SCORE , IT JUST DOES SIMPLE AVERAGE OF ALL SIM NN's / DIFF NN's

                float[] returned = getRatings(sim_users.keySet(), similar.get(i), i);
                float sim_sum = returned[0];
                float sim_count = returned[1];

                float[] returned2 = getRatings(diff_users.keySet(), similar.get(i), i);
                float diff_sum = returned2[0];
                float diff_count = returned2[1];

                //Based on whether any opposite NNs took the class, or any sim ones did, or both
                //for each case set current rating appropriately
                if (sim_count == 0.0f) {

                    if (diff_count == 0.0f) {//no user_nn took class so try majors
                        continue_flag = true;
                    } else {//do the opposite as diff NN's
                        float diff_avg = diff_sum / diff_count;
                        result[0][i] = 5.0f - diff_avg;
                        result[1][i] = diff_count;
                    }
                } else {
                    float sim_avg = sim_sum / sim_count;

                    if (diff_count == 0.0f) { //do the same as sim NN's
                        result[0][i] = sim_avg;
                        result[1][i] = sim_count;
                    } else { //do average of sim and (opp of diff)
                        float diff_avg = diff_sum / diff_count;
                        result[0][i] = (sim_avg + 5.0f - diff_avg) / 2.0f;
                        result[1][i] = sim_count + diff_count; //number of ratings that went into avg
                    }
                }
            } else {
                continue_flag = true;
            }

            if (majors.size() != 0 && continue_flag) {
                float[] returned = getRatings(majors.keySet(), similar.get(i), i);

                if (returned[1] == 0.0f) {
                    //no neighbor ratings could be found
                    //do nothing i.e. do not set continue_flag to false
                }
                else {
                    result[0][i] = returned[0] / returned[1]; // sum/count
                    result[1][i] = returned[1];
                    continue_flag = false;
                }
            }

            if (supermajors.size() != 0 && continue_flag) {
                float[] returned = getRatings(supermajors.keySet(), similar.get(i), i);

                if (returned[1] == 0.0f) {
                    //no neighbor ratings could be found
                    //do nothing i.e. do not set continue_flag to false
                }
                else {
                    result[0][i] = returned[0] / returned[1]; // sum/count
                    result[1][i] = returned[1];
                    continue_flag = false;
                }
            }

            if (continue_flag) {
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
                if (count == 0.0f) {//the course has never been taken, no more groups to check
                                    //I am fairly certain this is an unreachbale piece of code
                                    // ie comptuePrediction will alays return some rating, at worst the average of
                                    //the class in question, since course_nn always have averages
                    result[0][i] = -1.0f;
                    result[1][i] = count;
                }
                else {
                    result[0][i] = sum / count;
                    result[1][i] = count;
                }
            }
        }
        return result;
    }


    private void setRatings(ArrayList<Boolean> found1, ArrayList<Float> ratings1,
                            ArrayList<Boolean> found2, ArrayList<Float> ratings2) {
        showProgress(false);

        for (int i = 0; i < 5; i++) {//display correct ratings format for each category/course

            if (found1.get(i) && found2.get(i)) { //both ratings available

                if (ratings1.get(i) > ratings2.get(i)) {
                    //bar1[i].setBackgroundColor(Color.GREEN);
                    bar1[i].setRating(ratings1.get(i));
                    //bar2[i].setBackgroundColor(Color.RED);
                    bar2[i].setRating(ratings2.get(i));
                } else if (ratings1.get(i) < ratings2.get(i)) {
                    //bar1[i].setBackgroundColor(Color.RED);
                    bar1[i].setRating(ratings1.get(i));
                    //bar2[i].setBackgroundColor(Color.GREEN);
                    bar2[i].setRating(ratings2.get(i));
                } else {
                    //bar1[i].setBackgroundColor(Color.YELLOW);
                    bar1[i].setRating(ratings1.get(i));
                    //bar2[i].setBackgroundColor(Color.YELLOW);
                    bar2[i].setRating(ratings2.get(i));
                }
            } else {

                if (found1.get(i)) { //rating 1 available
                    bar1[i].setRating(ratings1.get(i));
                    //bar1[i].setBackgroundColor(Color.GREEN);
                } else {
                    bar1[i].setVisibility(View.GONE);
                    questions1[i].setVisibility(View.VISIBLE);
                }

                if (found2.get(i)) { //rating 2 available
                    bar2[i].setRating(ratings2.get(i));
                    //bar2[i].setBackgroundColor(Color.GREEN);
                } else {
                    bar2[i].setVisibility(View.GONE);
                    questions2[i].setVisibility(View.VISIBLE);
                }

            }

            //incorporate error message later??
             /*Toast.makeText(Compare2.this, "We're Sorry. No information can be found for category 1",
                    Toast.LENGTH_LONG).show(); */
        }
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
