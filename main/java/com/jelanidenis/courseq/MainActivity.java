package com.jelanidenis.courseq;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Intent previous;
    private String netid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.jelanidenis.courseq.R.layout.activity_main);

        Log.i(TAG, "In onCreate()");

        previous = getIntent();
        netid = previous.getStringExtra("Netid");

        //set up buttons
        Button AddButton = (Button) findViewById(com.jelanidenis.courseq.R.id.add_button);
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Add();
            }
        });

        Button ReviewButton = (Button) findViewById(com.jelanidenis.courseq.R.id.review_button);
        ReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Review();
            }
        });

        Button CompareButton = (Button) findViewById(com.jelanidenis.courseq.R.id.compare_button);
        CompareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Compare();
            }
        });

        Button RecommendButton = (Button) findViewById(com.jelanidenis.courseq.R.id.recommend_button);
        RecommendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Recommend();
            }
        });
    }

    private void Add() {
        Intent intent = new Intent(this, Add_A_Class.class);
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    private void Review() {
        Intent intent = new Intent(this, Review.class);
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    private void Compare() {
        Intent intent = new Intent(this, Compare.class);
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    private void Recommend() {
        Intent intent = new Intent(this, Recommend.class);
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "In onResume()");
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
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "In onStop()");
    }




}
