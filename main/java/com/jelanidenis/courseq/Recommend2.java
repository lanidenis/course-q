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
import java.util.NavigableSet;
import java.util.TreeMap;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class Recommend2 extends AppCompatActivity {

    private static final String TAG = "Recommend2";

    //Firebase References
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference rDatabase;
    private DatabaseReference rUsers;

    //firebase maps
    private Map<String, Object> user_map;
    HashMap<String, Object> course_map;
    HashMap<String, Object> course_nnmap;
    private HashMap<String, Object> user_nn;

    //store solution (best course recommendations)
    TreeMap<Float[], String> courseNN_solution = new TreeMap<Float[], String>(By_Float_Array);
    TreeMap<Float[], String> userNN_solution = new TreeMap<Float[], String>(By_Float_Array);
    TreeMap<Float, String> avg_solution = new TreeMap<Float, String>();
    TreeMap<Float, String> maj_solution = new TreeMap<Float, String>();
    private HashMap<String, Boolean> majors;

    // UI references.
    private TextView course1,course2,course3,course4,course5;
    private View mLoginFormView;
    private View mProgressView;

    private TextView course11, course22, course33, course44, course55;
    private TextView[] course_labels = new TextView[5];
    private boolean prep, like, ease, use, well;
    ArrayList<Boolean> categories = new ArrayList<Boolean>();
    private String netid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In onCreate()");
        super.onCreate(savedInstanceState);

        Intent previous = getIntent();
        netid = previous.getStringExtra("Netid");

        ease = previous.getExtras().getBoolean("easy");
        like = previous.getExtras().getBoolean("like");
        use =  previous.getExtras().getBoolean("use");
        well = previous.getExtras().getBoolean("well");
        prep = previous.getExtras().getBoolean("prep");

        categories.add(prep);
        categories.add(like);
        categories.add(ease);
        categories.add(use);
        categories.add(well);

        setContentView(com.jelanidenis.courseq.R.layout.recommend2);

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
                            Toast.makeText(Recommend2.this, com.jelanidenis.courseq.R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });


        //Get Database Instance
        rDatabase = FirebaseDatabase.getInstance().getReference();


            //UI STUFF//

        course11 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course1);
        course22 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course2);
        course33 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course3);
        course44 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course4);
        course55 = (TextView) findViewById(com.jelanidenis.courseq.R.id.course5);

        course_labels[0] = course11;
        course_labels[1] = course22;
        course_labels[2] = course33;
        course_labels[3] = course44;
        course_labels[4] = course55;

        mLoginFormView = findViewById(com.jelanidenis.courseq.R.id.login_form);
        mProgressView = findViewById(com.jelanidenis.courseq.R.id.login_progress);

        //set up buttons
        Button BackButton = (Button) findViewById(com.jelanidenis.courseq.R.id.back_button);
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackToMain();
            }
        });

        Button TryButton = (Button) findViewById(com.jelanidenis.courseq.R.id.try_button);
        TryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TryAgain();
            }
        });

        Button SignOutButton = (Button) findViewById(com.jelanidenis.courseq.R.id.sign_out_button);
        SignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });

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
                Log.i(TAG, "syncCourses:onCancelled" );
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
                Log.i(TAG, "getUserNeighbors:onCancelled" );
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
                getMajors(sector, type);
            };

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getMajor:onCancelled" );
            }
        };

        //add Listener
        mMajor.addListenerForSingleValueEvent(MajorListener);

    }

    private void getMajors(String sector, String type) {
        final String type2 = type;
        final String sector2 = sector;

        //reference
        DatabaseReference rMajor = rDatabase.child("groups").child(type).child(sector);

        //Create Listener
        ValueEventListener CurrentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) getCourseNeighbors(); //no majors
                else {
                    majors = (HashMap<String, Boolean>) dataSnapshot.getValue();
                    getCourseNeighbors();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "getMajors:onCancelled" );
            }
        };

        //add Listener
        rMajor.addListenerForSingleValueEvent(CurrentListener);
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
                Log.i(TAG, "getCourseNeighbors:onCancelled" );
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
                courseNN();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "courseListener:onCancelled" );
            }
        };

        rCourse.addListenerForSingleValueEvent(courseListener);
    }

    private void courseNN() {

        HashMap<String, Float> blueprint = new HashMap<String, Float>();

        HashMap<String, Object> userProfile = (HashMap<String, Object>) user_map.get(netid);
        if (!userProfile.containsKey("history")) majors(null); //no history = no course or user nn

        HashMap<String, Object> userHistory = (HashMap<String, Object>) userProfile.get("history");

        //find qualified course nn averaged over categories (needs to be above 3.5)
        ArrayList<String> rating = new ArrayList<String>();
        HashMap<String, Object> both = new HashMap<String, Object>();

        for (String course : userHistory.keySet()) {
            both = (HashMap<String, Object>) userHistory.get(course);
            rating = (ArrayList<String>) both.get("rating");

            float average = histAvg(rating);
            if (average >= 3.5) blueprint.put(course, average);
        }

        if (blueprint.size() == 0) { //none of user's history satisfies query
            userNN(userHistory);
        }

        //find all courseNN for each blueprint wrt avg across categories

        HashMap<String, Object> nn = new HashMap<String, Object>();

        for (String course : blueprint.keySet()) {
            if (!course_nnmap.containsKey(course)) continue;

            nn = (HashMap<String, Object>) course_nnmap.get(course);

            for (String c_nn : nn.keySet()) {
                rating = (ArrayList<String>) nn.get(c_nn);
                float average = simAvg(rating);

                if (average >= 0.7) {//threshold for course nn
                    Float[] myArray = new Float[3];
                    myArray[0] = average;
                    myArray[1] = blueprint.get(course); //we adopt histAvg of blueprint for courseNN,
                    // (likely rating user would give class) since we do not want to run Review (exponential complexity)
                    // exact analog to "nn" hashmap that stores simAvg as well as strenghs, since histAvg can
                    // be computed from each nn's history since they took class

                    myArray[2] = Float.parseFloat(rating.get(5));
                    courseNN_solution.put(myArray, c_nn);
                }
            }
        }

        userNN(userHistory); //proceed to next method of collection
    }

    private float simAvg(ArrayList<String> rating) {
        float sum = 0.0f;
        float counter = 0.0f;

        for (int i = 0; i < 5; i++) {
            if (i == 2) { //dealing with easiness category
                if (categories.get(i)) sum += (5.0f - Float.parseFloat(rating.get(i)));
                else sum += Float.parseFloat(rating.get(i));

                counter += 1.0f;
            }
            else {
                if (categories.get(i)) {
                    sum += Float.parseFloat(rating.get(i));
                    counter += 1.0f;
                }
            }
        }

        if (counter == 0.0f) return 0.0f; //imposible
        else return sum / counter;
    }

    private float histAvg(ArrayList<String> rating) {
        float sum = 0.0f;
        float counter = 0.0f;

        for (int i = 0; i < 5; i++) {
            if (categories.get(i) || i == 2) {
                sum += Float.parseFloat(rating.get(i));
                counter += 1.0f;
            }
        }

        if (counter == 0.0f) return 0.0f; //impossible
        else return (sum / counter);
    }

    private void userNN(HashMap<String,Object> userHistory) {

        if (user_nn == null) majors(userHistory); //current user has no nn's

        HashMap<String, Float[]> nn = new HashMap<String, Float[]>();
        ArrayList<String> rating = new ArrayList<String>();

        for (String candidate : user_nn.keySet()) {

            rating = (ArrayList<String>) user_nn.get(candidate);
            float average = simAvg(rating);

            if (average >= 0.7) {//threshold for course nn

                Float[] store = new Float[2];
                store[0] = average;                         //store the sim avg
                store[1] = Float.parseFloat(rating.get(5));  //store the strength
                nn.put(candidate, store);
            }
        }

        //now that we have user_nn based on the categories specified, let's go through all their courses
        if (nn.size() == 0) majors(userHistory);

        HashMap<String, Object> next_user = new HashMap<String, Object>();
        HashMap<String, Object> next_history = new HashMap<String, Object>();
        HashMap<String, Object> both = new HashMap<>();

        for (String nearest : nn.keySet()) {

            next_user = (HashMap<String, Object>) user_map.get(nearest);
            next_history = (HashMap<String, Object>) next_user.get("history");

            for (String course : next_history.keySet()) {

                //only proceed is current netid hasnt taken the course
                if (userHistory.containsKey(course)) continue;
                else if (courseNN_solution.containsValue(course)) continue;
                else {
                    both = (HashMap<String, Object>) next_history.get(course);
                    rating =  (ArrayList<String>) both.get("rating");
                    float average = histAvg(rating);

                    if (average >= 3.5) {// we can recommend this class!
                        Float[] myArray = new Float[3];
                        myArray[0] = nn.get(nearest)[0]; //simAvg
                        myArray[1] = average;            //histAvg  (3.5 to 5)
                        myArray[2] = nn.get(nearest)[1]; //strength
                        userNN_solution.put(myArray, course);
                    }

                }

            }
        }

        //check if we have 5 recommendations already , i.e. ignore duplicate courses
        int user_count = 0;
        for (String course : userNN_solution.values()) {
            if (!courseNN_solution.containsValue(course))
                ++user_count;
        }

        if (courseNN_solution.size() + user_count < 5) majors(userHistory);
        else displayRec();
    }

    //quick REMINDER - userNN_solution sorted by histAvg (3.5 to 5.0), and strength
    //               - courseNN_solution sorted by simAvg (0 to 1.0), and strength

    private void majors(HashMap<String, Object> rootUserHistory) {
        if (majors == null) scrapAverages();

        for (String netids : majors.keySet()) {

            if (user_nn.containsKey(netids)) continue; //we do not reconsider nn's

            HashMap<String, Object> userProfile = (HashMap<String, Object>) user_map.get(netids);
            if (!userProfile.containsKey("history")) continue; //no history = no course or user nn

            HashMap<String, Object> userHistory = (HashMap<String, Object>) userProfile.get("history");

            //find qualified courses averaged over categories (needs to be above 3.5)
            ArrayList<String> rating = new ArrayList<String>();
            HashMap<String, Object> both = new HashMap<String, Object>();

            for (String course : userHistory.keySet()) {
                if (rootUserHistory != null) {
                    if (rootUserHistory.containsKey(course)) continue;
                }

                if (userNN_solution.containsValue(course) || courseNN_solution.containsValue(course))
                    continue; //no redundancy, we already have the best recommendations scores for these courses
                both = (HashMap<String, Object>) userHistory.get(course);
                rating = (ArrayList<String>) both.get("rating");

                float average = histAvg(rating);
                if (average >= 3.5) maj_solution.put(average, course);
            }

        }

        //check if we have 5 recommendations already , i.e. ignore duplicate courses
        int user_count = 0;
        for (String course : userNN_solution.values()) {
            if (!courseNN_solution.containsValue(course))
                ++user_count;
        }

        if (maj_solution.size() + user_count + courseNN_solution.size() < 5) scrapAverages();
        else displayRec();

    }

    private void scrapAverages() {
        ArrayList<String> average = new ArrayList<String>();
        HashMap<String, Object> temp_map = new HashMap<String, Object>();
        for (String course : course_map.keySet()) {

            if (userNN_solution.containsValue(course) || courseNN_solution.containsValue(course))
                continue;
            if (maj_solution.containsValue(course)) continue;

            temp_map = (HashMap<String, Object>) course_map.get(course);
            average = (ArrayList<String>) temp_map.get("average");

            float histAvg = histAvg(average);
            if (histAvg >= 3.5)
                avg_solution.put(histAvg, course);
        }
        displayRec();
    }

    public static final java.util.Comparator<Float[]> By_Float_Array = new ByFloatArray();

    private static class ByFloatArray implements java.util.Comparator<Float[]>
    {
        public int compare(Float[] one, Float[] two) {
            if (one[0] > two[0]) return 1;
            else if (one[0] < two[0]) return -1;
            else if (one[1] > two[1]) return 1;
            else if (one[1] < two[1]) return -1;
            else if (one[2] > two[2]) return 1;
            else if (one[2] < two[2]) return -1;
            else return 0;
        }
    }

    private void displayRec() {

        ArrayList<String> resultOrder = new ArrayList<String>();

        //combine to get overall sorted
        for (Float[] key : userNN_solution.keySet()) {
            courseNN_solution.put(key, userNN_solution.get(key));
        }

        NavigableSet<Float[]> descending = courseNN_solution.descendingKeySet();
        for (Float[] key : descending) {
            resultOrder.add(courseNN_solution.get(key));
            if (resultOrder.size() == 5) displayRec2(resultOrder);
        }

        NavigableSet<Float> descending2 = maj_solution.descendingKeySet();
        for (Float key : descending2) {
            resultOrder.add(maj_solution.get(key));
            if (resultOrder.size() == 5) displayRec2(resultOrder);
        }

        NavigableSet<Float> descending3 = avg_solution.descendingKeySet();
        for (Float key : descending3) {
            resultOrder.add(avg_solution.get(key));
            if (resultOrder.size() == 5) displayRec2(resultOrder);
        }

        displayRec2(resultOrder);

    }

    private void displayRec2(ArrayList<String> resultOrder) {
        showProgress(false);

        for(int i = 0; i < resultOrder.size(); i++) {
            course_labels[i].setText(resultOrder.get(i));
        }
    }


    public void BackToMain() {
        //go back to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    public void TryAgain() {
        //go back to main activity
        Intent intent = new Intent(this, Recommend.class);
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
