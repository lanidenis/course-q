package com.example.jelanidenis.courseq;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class Add_A_Class extends AppCompatActivity {

    private static final String TAG = "Add_A_Class";

    //Firebase References
    private FirebaseAuth mAuth;

    private DatabaseReference rDatabase;
    private DatabaseReference rCourses;

    private FirebaseAuth.AuthStateListener mAuthListener;

    //Useful Storage Objects
    private Map<String, Object> course_map;
    private String[] courses;
    private String[] arg_for_to_Array = new String[] {};
    private ArrayAdapter<String> c_adapter;

    private String course = "";
    private Intent previous;

    // UI references.
    private EditText search;
    private ListView search_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_a_class);
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
                            Log.i(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(Add_A_Class.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });


        //Get Database Instance
        rDatabase = FirebaseDatabase.getInstance().getReference();

        //Set courses reference
        rCourses = rDatabase.child("courses");

        //Set up and add listener
        ValueEventListener CoursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                course_map = (Map<String, Object>) dataSnapshot.getValue();
                Set<String> key_set = course_map.keySet();
                courses = key_set.toArray(arg_for_to_Array);
                Log.i(TAG, "course array and map were just set");

                //Force a return to ensure UI gets initialized before onStart() called
                boolean returned = setUpUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "loadCourses:onCancelled", databaseError.toException());
                Toast.makeText(Add_A_Class.this, "Failed to load courses.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        //get courses data
        rCourses.addListenerForSingleValueEvent(CoursesListener);

    };

    private boolean setUpUI() {

        // Initialize UI References
        search = (EditText) findViewById(R.id.search);
        search_list = (ListView) findViewById(R.id.search_list);

        //populate listview with search array
        c_adapter = new ArrayAdapter<String>(this, R.layout.checkedtextview, courses);
        search_list.setAdapter(c_adapter);
        search_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

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

                //method with checkedtextview
                SparseBooleanArray checked = search_list.getCheckedItemPositions();
                for (int i = 0; i < search_list.getCount(); i++) {
                    if (checked.get(i)) {
                        course = courses[i];
                    }
                }
                Log.i(TAG, "course has been made");
            }
        });

        //Initalize signup button
        Button NextButton = (Button) findViewById(R.id.next_button);
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (course.equals("")) {
                    Toast.makeText(Add_A_Class.this, R.string.course_required,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Proceed();
                }
            }
        });

        //Initalize sign out button
        Button SignOut = (Button) findViewById(R.id.sign_out_button);
        SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               SignOut();
            }
        });

        return true;

    };

    public void Proceed() {
        //proceed to add_a_class2 activity
        String netid = previous.getStringExtra("Netid");
        Intent intent = new Intent(this, Add_A_Class2.class);
        intent.putExtra("Course", course);
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    public void SignOut() {
        //proceed to sign in activity
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


}


