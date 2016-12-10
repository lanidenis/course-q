package com.jelanidenis.courseq;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Set;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class Compare extends AppCompatActivity {

    private static final String TAG = "Compare";

    //Firebase References
    private FirebaseAuth mAuth;

    private DatabaseReference rDatabase;
    private DatabaseReference rCourses;

    private FirebaseAuth.AuthStateListener mAuthListener;

    //Useful Storage Objects
    private Map<String, Object> course_map;
    private String[] courses;
    private String[] courses2; //two arrays for two separate arrayadapters (synchornization detail)
    private String[] arg_for_to_Array = new String[] {};

    private String course = "";
    private String course2 = "";
    private Intent previous;

    // UI references.
    private ArrayAdapter<String> c_adapter;
    private ArrayAdapter<String> c_adapter2;
    private EditText search;
    private ListView search_list;
    private EditText search2;
    private ListView search_list2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.jelanidenis.courseq.R.layout.compare);
        Log.i(TAG, "In onCreate()");
        previous = getIntent();

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
                            Toast.makeText(Compare.this, com.jelanidenis.courseq.R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });


        //Get Database Instance
        rDatabase = FirebaseDatabase.getInstance().getReference();

        //Set courses reference
        rCourses = rDatabase.child("course_list");

        //Set up and add listener
        ValueEventListener CoursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                course_map = (Map<String, Object>) dataSnapshot.getValue();
                Set<String> key_set = course_map.keySet();
                courses = key_set.toArray(arg_for_to_Array);
                courses2 = key_set.toArray(arg_for_to_Array);
                Log.i(TAG, "course array and map were just set");

                //Force a return to ensure UI gets initialized before onStart() called
                setUpUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "loadCourses:onCancelled");
                Toast.makeText(Compare.this, "Failed to load courses.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        //get courses data
        rCourses.addListenerForSingleValueEvent(CoursesListener);

    };

    private boolean setUpUI() {

        // Initialize UI References
        search = (EditText) findViewById(com.jelanidenis.courseq.R.id.search);
        search_list = (ListView) findViewById(com.jelanidenis.courseq.R.id.search_list);
        search2 = (EditText) findViewById(com.jelanidenis.courseq.R.id.search2);
        search_list2 = (ListView) findViewById(com.jelanidenis.courseq.R.id.search_list2);

        //populate listview with search array
        c_adapter = new ArrayAdapter<String>(this, com.jelanidenis.courseq.R.layout.checkedtextview, courses);
        search_list.setAdapter(c_adapter);
        search_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        c_adapter2 = new ArrayAdapter<String>(this, com.jelanidenis.courseq.R.layout.checkedtextview, courses2);
        if (c_adapter2 == null) Log.i(TAG, "wow");
        else if (search_list2 == null) Log.i(TAG, "wow2");
        search_list2.setAdapter(c_adapter2);
        search_list2.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //dyncamilly filter listview using EditText
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                c_adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) {}
        });

        //add Listener to mMajorView
        search_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //alternative method with textview.xml rather than checkedtextview
                //year = mYearView.getItemAtPosition(position).toString();

                course = (String) parent.getAdapter().getItem(position);
                Log.i(TAG, "course1 has been made :" + course);
            }
        });

        //dyncamilly filter listview using EditText
        search2.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                c_adapter2.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) {}
        });

        //add Listener to mMajorView
        search_list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //alternative method with textview.xml rather than checkedtextview
                //year = mYearView.getItemAtPosition(position).toString();

                course2 = (String) parent.getAdapter().getItem(position);
                Log.i(TAG, "course2 has been made :" + course2);
            }
        });

        //Initalize signup button
        Button NextButton = (Button) findViewById(com.jelanidenis.courseq.R.id.next_button);
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (course.equals("")) {
                    Toast.makeText(Compare.this, com.jelanidenis.courseq.R.string.course_required,
                            Toast.LENGTH_SHORT).show();
                }
                else if (course2.equals("")) {
                    Toast.makeText(Compare.this, com.jelanidenis.courseq.R.string.course_required,
                            Toast.LENGTH_SHORT).show();
                }
                else if (course.equals(course2)) {
                    Toast.makeText(Compare.this, "Do not select the same course twice",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Proceed();
                }
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

        return true;

    };

    public void Proceed() {
        //proceed to compare2 activity
        String netid = previous.getStringExtra("Netid");
        Intent intent = new Intent(this, Compare2.class);
        intent.putExtra("Course1", course);
        intent.putExtra("Course2", course2);
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    public void SignOut() {
        //proceed to sign in activity
        Intent intent = new Intent(this, SimpleSignIn.class);
        startActivity(intent);
    }

    public void BackToMain() {
        //go back to main activity
        String netid = previous.getStringExtra("Netid");
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


}


