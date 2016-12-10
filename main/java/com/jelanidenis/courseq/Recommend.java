package com.jelanidenis.courseq;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;

/**
 * Created by jelanidenis on 11/4/16.
 */

public class Recommend extends AppCompatActivity {

    private static final String TAG = "Recommend";
    private Intent previous;

    private boolean like = false;
    private boolean use = false;
    private boolean well = false;
    private boolean easy = true;
    private boolean prep = false;

    // UI references.
    private Switch like_switch, use_switch, well_switch, prep_switch;
    private RadioGroup easyGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.jelanidenis.courseq.R.layout.recommend);
        Log.i(TAG, "In onCreate()");
        previous = getIntent();

        like_switch = (Switch) findViewById(com.jelanidenis.courseq.R.id.switch_1);
        use_switch = (Switch) findViewById(com.jelanidenis.courseq.R.id.switch_2);
        well_switch = (Switch) findViewById(com.jelanidenis.courseq.R.id.switch_3);
        prep_switch = (Switch) findViewById(com.jelanidenis.courseq.R.id.switch_5);
        easyGroup = (RadioGroup) findViewById(com.jelanidenis.courseq.R.id.easy_Group);

        like_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // isChecked will be true if the switch is in the On position
                if (isChecked) like = true;
            }
        });

        use_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // isChecked will be true if the switch is in the On position
                if (isChecked) use = true;
            }
        });

        well_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // isChecked will be true if the switch is in the On position
                if (isChecked) well = true;
            }
        });

        prep_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // isChecked will be true if the switch is in the On position
                if (isChecked) prep = true;
            }
        });

        easyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == com.jelanidenis.courseq.R.id.hard_button) easy = false;
            }
        });

        //Initalize Next button
        Button NextButton = (Button) findViewById(com.jelanidenis.courseq.R.id.next_button);
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Proceed();
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

    };

    public void Proceed() {
        //proceed to recommend2 activity
        Intent intent = new Intent(this, Recommend2.class);

        //pass all booleans to make ArrayList<boolean> in next activity
        intent.putExtra("easy", easy);
        intent.putExtra("like", like);
        intent.putExtra("use", use);
        intent.putExtra("well", well);
        intent.putExtra("prep", prep);

        String netid = previous.getStringExtra("Netid");
        intent.putExtra("Netid", netid);
        startActivity(intent);
    }

    public void BackToMain() {
        //go back to main activity
        String netid = previous.getStringExtra("Netid");
        Intent intent = new Intent(this, MainActivity.class);
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
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "In onStop()");
    }


}


