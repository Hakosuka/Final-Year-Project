package com.example.se415017.maynoothskyradar.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.activities.MainActivity;
import com.example.se415017.maynoothskyradar.helpers.MainTabPagerAdapter;
import com.example.se415017.maynoothskyradar.objects.Aircraft;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * This Fragment is meant to show all of the Aircraft that have been detected on a map.
 */
public class MainMapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private String TAG = getClass().getSimpleName();

    public static final String PREFS = "UserPreferences";
    public static final String SERVER_PREF = "serverAddress";
    public static final String LAT_PREF = "latitude";
    public static final String LON_PREF = "longitude";

    public static final String AIR_KEY = "aircraftKey";

    private static View view;

//    @Bind(R.id.map_container)
//    RelativeLayout mapContainer;

//    @Bind(R.id.main_mapview)
//    MapView mapView;

//TODO: Test if ButterKnife can work on MapFragments
//    @Bind(R.id.main_map)
//    SupportMapFragment mainMapFrag;

    private static GoogleMap googleMap;
    private static Double latitude, longitude;
    private CameraPosition camPos;

    ArrayList<Aircraft> aircrafts;
    ArrayList<Marker> aircraftMarkers;
    HashMap<Marker, Aircraft> anyHash;

    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;

    public MainMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param aircraftArrayList The list of aircraft detected by the app so far.
     * @return A new instance of fragment MainMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainMapFragment newInstance(ArrayList<Aircraft> aircraftArrayList) {
        MainMapFragment fragment = new MainMapFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(AIR_KEY, aircraftArrayList);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //Just in case aircrafts hasn't been initialised yet
        if (aircrafts == null) {
            aircrafts = new ArrayList<Aircraft>();
        }
        if (aircraftMarkers == null) {
            aircraftMarkers = new ArrayList<Marker>();
        }
        if (getArguments() != null) {
            //Need to populate the ArrayList of Aircraft somehow
            if(getArguments().getSerializable(AIR_KEY) instanceof ArrayList<?>) {
                ArrayList<?> unknownTypeList = (ArrayList<?>) getArguments().getSerializable(AIR_KEY);
                if(unknownTypeList != null && unknownTypeList.size() > 0) {
                    for (int i = 0; i < unknownTypeList.size(); i++) {
                        Object unknownTypeObject = unknownTypeList.get(i);
                        if(unknownTypeObject instanceof Aircraft){
                            aircrafts.add((Aircraft) unknownTypeObject);
                        }
                    }
                }
            }
        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        latitude = Double.longBitsToDouble(sharedPreferences.getLong(LAT_PREF, 0));
        Log.d(TAG, "Latitude from sharedPreferences = " + Double.toString(latitude));
        longitude = Double.longBitsToDouble(sharedPreferences.getLong(LON_PREF, 0));
        Log.d(TAG, "Longitude from sharedPreferences = " + Double.toString(longitude));
        Log.d(TAG, "Is the map initialised? " + Boolean.toString(googleMap != null));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(container == null)
            return null;
        view = inflater.inflate(R.layout.fragment_main_map, container, false);
        ButterKnife.bind(this, view);

        for(Aircraft a : aircrafts){
            Log.d(TAG, "Loading from bundle " + a.toString());
        }
        setUpMapIfNeeded();
        Log.d(TAG, "Lat & Lon: " + Double.toString(latitude) + ", " + Double.toString(longitude));
        if(googleMap != null) {
            for(Aircraft a : aircrafts) {
                //Check if the Aircraft object has latitude and longitude values yet
                //If not, don't add them to the map, there'd be no point adding them in
                if(a.latitude != null && a.longitude != null)
                    googleMap.addMarker(new MarkerOptions().position(a.getPosition()).title("Mode-S: " + a.icaoHexAddr)
                        .snippet("Coordinates: " + a.getPosString())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane_north))
                        .flat(true)
                        .rotation(Float.parseFloat(a.track))); //rotate the marker by the track of the aircraft
            }
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    //Show the Aircraft's location if its Marker is clicked
                    if(marker.getTitle().startsWith("Mode-S")){
                        Toast.makeText(getContext(), marker.getSnippet(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        if(googleMap != null)
            setUpMap();
        else { //It's null anyway
            googleMap = ((SupportMapFragment) MainActivity.fragManager.findFragmentById(R.id.main_map)).getMap();
            if (googleMap != null)
                setUpMap();
        }
    }

    //For handling configuration changes - otherwise the map zooms out to 0.0N, 0.0W, and all markers are lost
    @Override
    public void onPause() {
        super.onPause();
        camPos = googleMap.getCameraPosition();
        googleMap = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if(camPos != null) {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
            camPos = null;
        }
    }

    //Sets up the map if it hasn't been set up already
    //Somehow it works just fine if I get rid of "static"
    public void setUpMapIfNeeded() {
        if (googleMap == null){
            googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map)) //TODO: FIX THIS
                    .getMap();
            //Check if the map was obtained successfully
            if (googleMap != null)
                setUpMap();
        }
    }

    //This is where markers, lines and listeners are added, and where the camera is moved.
    private static void setUpMap() {
//        Log.d("MainMapFragment", "Latitude to pass into map being set up: " + Double.toString(latitude));
//        Log.d("MainMapFragment", "Longitude to pass into map being set up: " + Double.toString(longitude));
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //googleMap.setMyLocationEnabled(true); //TODO: Maybe wait until I have the pointing function worked out
        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)))
                .setTitle("My server is here");
        //TODO: Add custom markers for the planes
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 8.0f));

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (googleMap != null) { //TODO: CANNOT PERFORM THIS ACTION AFTER ONSAVEINSTANCESTATE
//            MainActivity.fragManager.beginTransaction()
//                    .remove(MainActivity.fragManager.findFragmentById(R.id.main_map)).commit();
//            googleMap = null;
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(AIR_KEY, aircrafts);
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
        public void onFragmentInteraction(Uri uri);
        //public void onFragmentSetAircraft(ArrayList<Aircraft> aircraftArrayList);
    }
}
