package com.example.se415017.maynoothskyradar.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.se415017.maynoothskyradar.R;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GPSTestActivity extends AppCompatActivity {
    @Bind(R.id.test_latitude)
    TextView tLatitude;
    @Bind(R.id.test_longitude)
    TextView tLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstest);
        ButterKnife.bind(this);
    }
}
