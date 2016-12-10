package com.jelanidenis.courseq;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.Set;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class SimpleSignUp extends AppCompatActivity {

    private static final String TAG = "SimpleSignUp";

    //Firebase References
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private Map<String, Object> netids;
    private String year = "";
    private String major = "";

    //Built in Array
    private String[] majors;
    private Map<String, Object> major_map;
    private String[] arg_for_to_Array = new String[] {};
    private ArrayAdapter<String> c_adapter;
    /*
    private String[] majors = new String[] {
            "African American Studies",
            "Anthropology",
            "Architecture",
            "Art and Archaeology",
            "Astrophysical Sciences",
            "Chemical and Biological Engineering",
            "Chemistry",
            "Civil and Environmental Engineering",
            "Classics",
            "Comparative Literature",
            "Computer Science",
            "East Asian Studies",
            "Ecology and Evolutionary Biology",
            "Economics",
            "Electrical Engineering",
            "English",
            "Finance",
            "French and Italian",
            "Geosciences",
            "German",
            "History",
            "Mathematics",
            "Mechanical and Aerospace Engineering",
            "Molecular Biology",
            "Music",
            "Near Eastern Studies",
            "Neuroscience",
            "Operations Research and Financial Engineering",
            "Philosophy",
            "Physics",
            "Politics",
            "Psychology",
            "Religion",
            "Slavic Languages and Literatures",
            "Sociology",
            "Spanish and Portuguese",
            "Woodrow Wilson School of Public and International Affairs"
    };
    */

    private String[] years = new String[] {
            "2020", "2019", "2018", "2017"
    };

    // UI references.
    private EditText mNetView;
    private EditText mNameView;
    private ListView mMajorView;
    private EditText mSearchView;
    private ListView mYearView;


    private View mLoginFormView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(com.jelanidenis.courseq.R.layout.simple_signup);

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
                            Toast.makeText(SimpleSignUp.this, com.jelanidenis.courseq.R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });


        //Get Database Instance
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Create Netid Listener
        ValueEventListener NetidListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                netids = (Map<String, Object>) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadNetids:onCancelled");
                Toast.makeText(SimpleSignUp.this, "Failed to load netids.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        //Create Majors Listener
        ValueEventListener MajorListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                major_map = (Map<String, Object>) dataSnapshot.getValue();
                Set<String> key_set = major_map.keySet();
                majors = key_set.toArray(arg_for_to_Array);
                Log.i(TAG, "majors was just set");

                //Force a return to ensure Listeners UI gets initialized before onStart() called
                boolean returned = setUpUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadMajors:onCancelled");
                Toast.makeText(SimpleSignUp.this, "Failed to load majors.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        //references for Listener Data Queries
        DatabaseReference mNetids = mDatabase.child("user_index");
        DatabaseReference mMajors = mDatabase.child("majors");
        /*Query mMajors = mDatabase.child("majors").orderByKey();*/

        //Retrieve the Data we need
        mNetids.addListenerForSingleValueEvent(NetidListener);
        mMajors.addListenerForSingleValueEvent(MajorListener);

        Log.i(TAG, "all listeners created and added");

    };

    private boolean setUpUI() {

                    //UI STUFF//

        // Initialize UI References
        mNetView = (EditText) findViewById(com.jelanidenis.courseq.R.id.netid);
        mNameView = (EditText) findViewById(com.jelanidenis.courseq.R.id.name);
        mYearView = (ListView) findViewById(com.jelanidenis.courseq.R.id.year_list);
        mSearchView = (EditText) findViewById(com.jelanidenis.courseq.R.id.search);
        mMajorView = (ListView) findViewById(com.jelanidenis.courseq.R.id.major_list);

        mLoginFormView = findViewById(com.jelanidenis.courseq.R.id.login_form);
        mProgressView = findViewById(com.jelanidenis.courseq.R.id.login_progress);

        //set array adapters to populate listviews
        c_adapter = new ArrayAdapter<String>(this, com.jelanidenis.courseq.R.layout.checkedtextview, majors);
        mMajorView.setAdapter(c_adapter);
        mMajorView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<String> y_adapter = new ArrayAdapter<String>(this, com.jelanidenis.courseq.R.layout.checkedtextview, years);
        mYearView.setAdapter(y_adapter);
        mYearView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //add Listener to mYearView
        mYearView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //alternative method with textview.xml rather than checkedtextview
                //year = mYearView.getItemAtPosition(position).toString();

                //method with checkedtextview
                SparseBooleanArray checked = mYearView.getCheckedItemPositions();
                for (int i = 0; i < mYearView.getCount(); i++) {
                    if (checked.get(i)) {
                        year = years[i];
                    }
                }
                Log.i(TAG, "year has been made");
            }
        });

        //dyncamilly filter listview using EditText
        mSearchView.addTextChangedListener(new TextWatcher() {
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
        mMajorView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //alternative method with textview.xml rather than checkedtextview
                //year = mYearView.getItemAtPosition(position).toString();

                major = (String) parent.getAdapter().getItem(position);
                Log.i(TAG, "major has been made" + major);
            }
        });

        /* Old Implementation --

        mMajorView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                major = mMajorView.getItemAtPosition(position).toString();
                //Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

        */

        Button SignInButton = (Button) findViewById(com.jelanidenis.courseq.R.id.sign_in_button);
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });

        //Initalize signup button
        Button mSignUpButton = (Button) findViewById(com.jelanidenis.courseq.R.id.sign_up_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset errors.
                mNetView.setError(null);
                mNameView.setError(null);

                String netid = mNetView.getText().toString();
                String name = mNameView.getText().toString();

                // Log.i(TAG, netid.toString());
                if (netid.equals("")) {
                    mNetView.setError(getString(com.jelanidenis.courseq.R.string.error_field_required));
                    mNetView.requestFocus();
                }
                else if (netids.containsKey(netid)) {
                    Toast.makeText(SimpleSignUp.this, com.jelanidenis.courseq.R.string.already_registered,
                            Toast.LENGTH_SHORT).show();
                }
                else if (name.equals("")) {
                    mNameView.setError(getString(com.jelanidenis.courseq.R.string.error_field_required));
                    mNameView.requestFocus();
                }
                else if (year.equals("")) {
                    Toast.makeText(SimpleSignUp.this, com.jelanidenis.courseq.R.string.year_required,
                            Toast.LENGTH_SHORT).show();
                }
                else if (major.equals("")) {
                    Toast.makeText(SimpleSignUp.this, com.jelanidenis.courseq.R.string.major_required,
                            Toast.LENGTH_SHORT).show();
                }

                else {
                    //hopefully by forcing signup to return a value,
                    //the listener created within it will have to finish
                    SignUp(netid, name, year, major);
                }
            }
        });

        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "In onStart()");

    }

    public void SignIn() {
        //return to SignIn activity
        Log.i(TAG, "In SignIn()");
        Intent intent = new Intent(this, SimpleSignIn.class);
        startActivity(intent);
    }

    /*
     * Signs up account, then proceeds to add_a_class activity
     */
    private void SignUp(String Netid, String Name, String Year, String Major) {
        Log.i(TAG, "In SignUp()");

        User user = new User(Name, Year, Major);
        //final String Net_id = Netid;

        //add user to user profiles
        mDatabase.child("users").child(Netid).setValue(user);

        //add user to user_index
        mDatabase.child("user_index").child(Netid).setValue(true);

        //find major sector and type for grouping
        Map<String, String> both = (Map<String, String>) major_map.get(Major);
        String sector = both.get("Sector");
        String type = both.get("type");

        //add user to proper sector group
        mDatabase.child("groups").child(type).child(sector).child(Netid).setValue(true);

        /*
        mMajor = mDatabase.child("majors").child(Major);

        // Create and add value event listener to trigger once all data is retrieved
        mMajor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "inside onDataChanged");

                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                sector = map.get("Sector");
                type = map.get("type");

                //add user to proper sector group
                mDatabase.child("groups").child(type).child(sector).child(Net_id).setValue(true);

                //proceed to next activity
                Proceed(Net_id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "MajorValueEventListener:onCancelled");
            }
        });
        */

        //proceed to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Netid", Netid);
        startActivity(intent);

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
