package com.example.se415017.maynoothskyradar.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.activities.MainActivity;
import com.example.se415017.maynoothskyradar.helpers.NetHelper;
import com.example.se415017.maynoothskyradar.presenters.GPSTestPresenter;

import butterknife.Bind;
import butterknife.OnClick;
import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GPSTestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GPSTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

@RequiresPresenter(GPSTestPresenter.class)
public class GPSTestFragment extends NucleusFragment<GPSTestPresenter> {
    // TODO: Rename parameter arguments, choose names that match
    @Bind(R.id.button_gps_activation)
    Button GpsActivationButton;
    @Bind(R.id.check_server_button)
    Button CheckServerButton;

    //These are the GPS coordinates of the server
    @Bind(R.id.gps_latitude)
    TextView GpsLat;
    @Bind(R.id.gps_longitude)
    TextView GpsLon;

    @Bind(R.id.server_status)
    TextView ServerStat;

    private static GPSTestPresenter gpsTestPresenter;

    @OnClick(R.id.button_gps_activation)
    public void activateGPS(View view){
        String[] decodedNMEA;
        Log.d("activateGPS", "Button pressed");
        for(int i = 0; i < dummyData.length; i++){
            decodedNMEA = gpsTestPresenter.decodeNMEA(dummyData[i]);
            if(decodedNMEA[0] == "Success"){
                GpsLat.setText(decodedNMEA[1]); // latitude and N/S strings in decodedNMEA
                GpsLon.setText(decodedNMEA[3]); // longitude and E/W strings in decodedNMEA
            }
        }
    }

    @OnClick(R.id.check_server_button)
    public void checkServerHandler(View view){
        Log.d("checkServer", "Button pressed");
        NetHelper netHelper = new NetHelper(getActivity());
        if(netHelper.isConnected()) {
            Log.d("checkServer", "Connection available");
            ServerStat.setText("Server is available");
        }
        else {
            Log.d("checkServer", "No connection available");
            new MaterialDialog.Builder(getActivity()).title("No Internet connection available")
                    .content("Please activate your mobile data or connect to wi-fi.")
                    .cancelable(false)
                    .positiveText("Open settings")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(settingsIntent);
                        }
                    });

        }
    }

    boolean netStatus = false;
    Boolean serverStatus = false;
    String[] dummyData = {
            "$GPGGA,103102.557,5323.0900,N,00636.1283,W,1,08,1.0,49.1,M,56.5,M,,0000*7E",
            "$GPGSA,A,3,01,11,08,19,28,32,03,18,,,,,1.7,1.0,1.3*37",
            "$GPGSV,3,1,10,08,70,154,34,11,61,270,26,01,47,260,48,22,40,062,*7E",
            "$GPGSV,3,2,10,19,40,297,46,32,39,184,32,28,28,314,43,03,11,205,41*7C",
            "$GPGSV,3,3,10,18,07,044,35,30,03,276,42*75",
            "$GPRMC,103102.557,A,5323.0900,N,00636.1283,W,000.0,308.8,101115,,,A*79",
            "$GPVTG,308.8,T,,M,000.0,N,000.0,K,A*0E"
    }; // using the data supplied in Joe's email from 10 November

    private OnFragmentInteractionListener mListener;

    public GPSTestFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static GPSTestFragment newInstance(String param1, String param2) {
        GPSTestFragment fragment = new GPSTestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //TODO: something
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gpstest, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
