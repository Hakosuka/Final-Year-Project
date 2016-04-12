package com.example.se415017.maynoothskyradar.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.fragments.EnterURLFragment;

/**
 * This activity is launched if the user starts the app for the first time, or if they've deleted
 * the app's saved data.
 */
public class SetUpActivity extends AppCompatActivity implements EnterURLFragment.OnFragmentInteractionListener{

    final String PREFS = "UserPreferences";
    final String SERVER_PREF = "serverAddress";
    final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //The user shouldn't need to use the action bar at this time.
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_setup, new EnterURLFragment())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_setup, new EnterURLFragment())
                    .commit();
            Log.d(TAG, "Did the user navigate here themselves? " +
                    Boolean.toString(savedInstanceState.getBoolean(MainActivity.USER_BEGINS_SETUP)));
        }
    }
    // Needed when implementing Fragments
    public void onFragmentInteraction(Uri uri){
        //TODO: Add code here - it still works if you leave it blank, but just research
        //onFragmentInteraction() anyway.
    }
}
