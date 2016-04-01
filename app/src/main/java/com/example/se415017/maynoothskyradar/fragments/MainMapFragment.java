package com.example.se415017.maynoothskyradar.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.objects.Aircraft;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

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
public class MainMapFragment extends Fragment implements OnMapReadyCallback {
    private String TAG = getClass().getSimpleName();

    public static final String PREFS = "UserPreferences";
    public static final String SERVER_PREF = "serverAddress";
    public static final String LAT_PREF = "latitude";
    public static final String LON_PREF = "longitude";

    private boolean mapSetUp;

    public static final String AIR_KEY = "aircraftKey";
    public static final String HASHMAP_KEY = "markers";

    static View view;

//    @Bind(R.id.map_container)
//    RelativeLayout mapContainer;

//    @Bind(R.id.main_mapview)
//    MapView mapView;

    @Bind(R.id.map_bSheet) FrameLayout bottomSheet;
    @Bind(R.id.map_bSheet_content) ViewGroup bottomSheetContent;
    @Bind(R.id.bSheet_hex_title) TextView sheetHexTitle;
    @Bind(R.id.bSheet_hex_content) TextView sheetHexContent;
    @Bind(R.id.bSheet_altitude_title) TextView sheetAltTitle;
    @Bind(R.id.bSheet_altitude_content) TextView sheetAltContent;
    @Bind(R.id.bSheet_callsign_content) TextView sheetCallsign;
    @Bind(R.id.bSheet_latitude_title) TextView sheetLatTitle;
    @Bind(R.id.bSheet_latitude_content) TextView sheetLatContent;
    @Bind(R.id.bSheet_longitude_title) TextView sheetLonTitle;
    @Bind(R.id.bSheet_longitude_content) TextView sheetLonContent;
    @Bind(R.id.bSheet_gs_title) TextView sheetGSpeedTitle;
    @Bind(R.id.bSheet_gs_content) TextView sheetGSpeedContent;
    @Bind(R.id.bSheet_track_title) TextView sheetTrackTitle;
    @Bind(R.id.bSheet_track_content) TextView sheetTrackContent;
    //DONE: Test if ButterKnife can work on MapFragments - IT CAN'T, @Bind fields must extend from View or be an interface
    SupportMapFragment mainMapFrag;

    private GoogleMap googleMap;
    private static Double latitude, longitude;
    private CameraPosition cameraPosition;

    ArrayList<Aircraft> aircrafts;
    ArrayList<Marker> aircraftMarkers;

    //A WeakHashMap allowed for an object associated with a Marker to get garbage-collected with it
    //The key is a String so that I can use the Marker's ID. However...
    //23 Mar: Changed to a HashMap, as during testing with the WeakHashMap, when I clicked on the
    //Markers there was no Aircraft associated with it.
    //29 Mar: Switched the HashMap around
    //29 Mar (v2): Switched the key to Strings to represent the Aircraft's Mode-S hex code
    HashMap<String, Marker> aircraftAndMarkers = new HashMap<>();
    HashMap<String, Polyline> aircraftAndPaths = new HashMap<>();
    private boolean markersAdded; //Stops Markers being added to the map repeatedly
    private boolean activityResuming;

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
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d(TAG, "Bottom sheet state changed");
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d(TAG, "Bottom sheet is sliding");
            }
        });
        behavior.setPeekHeight(24);
        mainMapFrag = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.main_map);
        mainMapFrag.getMapAsync(this);

        for(Aircraft a : aircrafts){
            Log.d(TAG, "Loading from bundle " + a.toString());
        }

        setUpMapIfNeeded(activityResuming);

        Log.d(TAG, "Lat & Lon: " + Double.toString(latitude) + ", " + Double.toString(longitude));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        if(googleMap != null) {
            setUpMap(googleMap, activityResuming);
        } else { //It's null anyway
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map)).getMapAsync(this);
            mainMapFrag.getMapAsync(this);
            if (googleMap != null) {
                setUpMap(googleMap, activityResuming);
            }
        }
    }

    //For handling configuration changes - otherwise the map zooms out to 0.0N, 0.0W, and all markers are lost
    @Override
    public void onPause() {
        Log.d(TAG, "Pausing");
        super.onPause();
        if(googleMap != null)
            cameraPosition = googleMap.getCameraPosition();
        googleMap = null;
        //Set these to false, otherwise the map is blank and centred @0.0N, 0.0W when resuming
        markersAdded = false;
        mapSetUp = false;
    }
    @Override
    public void onResume() {
        Log.d(TAG, "Resuming");
        super.onResume();
        setUpMapIfNeeded(activityResuming);
        //Wait until googleMap is re-initialised
        if(cameraPosition != null & googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            cameraPosition = null;
        }
    }

    //Sets up the map if it hasn't been set up already
    protected void setUpMapIfNeeded(boolean activityResumed) {
        if (googleMap == null){
            Log.d(TAG, "Map was null");
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map))
                    .getMapAsync(this);
            //Check if the map was obtained successfully
            if (googleMap != null) {
                setUpMap(googleMap, activityResumed);
            }
        }
    }

    /** This is where markers, lines and listeners are added, and where the camera is moved.
     *  @param googleMap The GoogleMap object to be set up.
     */
    private void setUpMap(GoogleMap googleMap, boolean activityResumed) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)))
                .setTitle("My server is here");
        //DONE: Add custom markers for the planes - see onCreateView()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 7.0f));
        Log.d(TAG, "Map has been set up");
        Log.d(TAG, "Number of planes to set up: " + aircrafts.size());
        Log.d(TAG, "Have the markers been added? " + Boolean.toString(markersAdded));
        if(!markersAdded) {
            addMarkers(activityResumed);
            markersAdded = true;
        }
    }

    /**
     * Updates the ArrayList of Aircraft objects held by the Fragment
     * @param aircrafts - the updated ArrayList of Aircraft objects
     * @param activityResumed - if the MainActivity is resuming rather than being created again,
     *                        this will be true to stop Markers being added again.
     */
    public void updateAircrafts(ArrayList<Aircraft> aircrafts, boolean activityResumed) {
        Log.d(TAG, "Updating AircraftMarkers, " + aircrafts.size() +  " to update.");
        this.aircrafts = aircrafts;
        this.activityResuming = activityResumed;
        if(googleMap != null)
            addMarkers(activityResumed);
    }

    //This is what's calling setUpMap
    @Override
    public void onMapReady(final GoogleMap gMap) {
        if(!mapSetUp){
            googleMap = gMap;
            setUpMap(googleMap, activityResuming);
            mapSetUp = true;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Configuration changed");
    }

    /**
     * Adds markers and polylines to the map for each Aircraft.
     * @param activityResumed - when returning the app, but the app hasn't killed the MainActivity,
     *                        additional unnecessary markers were laid on top of the existing map
     *                        Markers.
     */
    public void addMarkers(boolean activityResumed){
        for (Aircraft a : aircrafts) {
            //Check if the Aircraft object has latitude and longitude values yet
            //If not, don't add them to the map, there'd be no point adding a Marker for them
            if (a.latitude != null && a.longitude != null) {
                //New Aircraft found
                //29 Mar - I'll have to use a different data type for the HashMap entries' values
                //as if I were to use an Aircraft object as the value, the new Aircraft would
                //be added to the map as a new marker.
                //Also, "markersAddedAfterResume" is here so that the Markers get added again after
                //a configuration change.
                if (!aircraftAndMarkers.containsKey(a.icaoHexAddr) || !markersAdded) { //AfterResume) {
                    Marker m = googleMap.addMarker(new MarkerOptions().position(a.getPosition()).title("Mode-S: " + a.icaoHexAddr)
                            .snippet("Coordinates: " + a.getPosString())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane_north))
                            .flat(true)
                            .rotation(Float.parseFloat(a.track))); //rotate the marker by the track of the aircraft
                    Polyline p = googleMap.addPolyline(new PolylineOptions()
                            .width(5.0f)
                            .color(Color.rgb(253, 95, 0)) //"Neon orange" colour - stands out easily on the map
                            .geodesic(true) //Draws lines on the map assuming it's a globe, not a flat map
                            .addAll(a.path));
                    aircraftAndMarkers.put(a.icaoHexAddr, m);
                    aircraftAndPaths.put(a.icaoHexAddr, p);
                    Log.d(TAG, "Marker ID for " + a.icaoHexAddr + " = " + m.getId());
                } else {
                    Marker marker = aircraftAndMarkers.get(a.icaoHexAddr);
                    Log.d(TAG, "Marker to update: " + marker.getId());
                    marker.setPosition(a.getPosition());
                    marker.setTitle("Mode-S: " + a.icaoHexAddr);
                    if(a.nearestNeighbour != null){
                        marker.setSnippet("Coordinates: " + a.getPosString() + "\nNearest aircraft: " + a.nearestNeighbour.icaoHexAddr);
                    } else {
                        marker.setSnippet("Coordinates: " + a.getPosString());
                    }
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.airplane_north));
                    marker.setFlat(true);
                    marker.setRotation(Float.parseFloat(a.track)); //rotate the marker by the track of the aircraft;
                    //Update the entry in aircraftAndMarkers for the updated Aircraft
                    aircraftAndMarkers.put(a.icaoHexAddr, marker);
                    Polyline path = aircraftAndPaths.get(a.icaoHexAddr);
                    path.setPoints(a.path);
                    path.setWidth(5.0f);
                    path.setColor(Color.rgb(253, 95, 0));
                    path.setGeodesic(true);
                    //Update the entry in aircraftAndPaths for the updated Aircraft
                    aircraftAndPaths.put(a.icaoHexAddr, path);
                }
            } else {
                Log.d(TAG, "No position found for " + a.icaoHexAddr);
            }
        }
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
            Aircraft selected = null;
            //Search the HashMap for the Aircraft which corresponds to the clicked Marker
            for (String aHexCode : aircraftAndMarkers.keySet()) {
                if (aircraftAndMarkers.get(aHexCode).equals(marker)) {
                    for (Aircraft a : aircrafts) {
                        if (a.icaoHexAddr.equalsIgnoreCase(aHexCode)) {
                            selected = a;
                            //We've found the right Aircraft, now break out of the loop
                            break;
                        }
                    }
                    break;
                }
            }
            final Aircraft aircraftToBeShown = selected;
            //Sometimes some Markers weren't mapped to an Aircraft object, causing a
            //NullPointerException - such as the Marker for the server's location.
            if (selected != null) {
                Log.d(TAG, "Marker ID = " + marker.getId() + " selected, corresponds to " + selected.icaoHexAddr);
                //Show the Aircraft's location if its Marker is clicked
                Snackbar.make(view, "Do you want to see more details on this aircraft?", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO: Show a BottomSheet
                                Log.d(TAG, "Snackbar button clicked.");
                                sheetHexContent.setText(aircraftToBeShown.icaoHexAddr);
                                sheetCallsign.setText(aircraftToBeShown.callsign);
                                sheetAltContent.setText(aircraftToBeShown.altitude);
                                sheetLatContent.setText(aircraftToBeShown.latitude);
                                sheetLonContent.setText(aircraftToBeShown.longitude);
                                sheetGSpeedContent.setText(aircraftToBeShown.gSpeed);
                                //Add degree symbol
                                sheetTrackContent.setText(aircraftToBeShown.track + "\u00B0");
                                bottomSheet.setVisibility(View.VISIBLE);
                            }
                        })
                        .show();
                //I need to add this too, otherwise the map won't centre on the Marker's location
                googleMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(selected.getPosition(), 8.0f));
            } else {
                Log.d(TAG, "Marker doesn't correspond to any plane");
            }
            //I need to add this, otherwise the InfoWindow won't show
            marker.showInfoWindow();

            return true;
            }
        });
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
            Aircraft selected = null;
            for (String aHexCode : aircraftAndMarkers.keySet()) {
                if (aircraftAndMarkers.get(aHexCode).equals(marker)) {
                    for (Aircraft a : aircrafts) {
                        if (a.icaoHexAddr.equalsIgnoreCase(aHexCode)) {
                            selected = a;
                            //We've found the right Aircraft, now break out of the loop
                            break;
                        }
                    }
                    break;
                }
            }
            //Necessary for the SnackBar code below
            final Aircraft aircraftToBeShown = selected;
            //Just in case it's the server's Marker
            if (selected != null) {
                Log.d(TAG, "Marker ID = " + marker.getId() + " selected, corresponds to " + selected.icaoHexAddr);
                //Show the Aircraft's location if its Marker is clicked
                Snackbar.make(view, "Do you want to see more details on this aircraft?", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO: Show the BottomSheet
                            Log.d(TAG, "Snackbar button clicked.");
                            sheetHexContent.setText(aircraftToBeShown.icaoHexAddr);
                            sheetCallsign.setText(aircraftToBeShown.callsign);
                            sheetAltContent.setText(aircraftToBeShown.altitude);
                            sheetLatContent.setText(aircraftToBeShown.latitude);
                            sheetLonContent.setText(aircraftToBeShown.longitude);
                            sheetGSpeedContent.setText(aircraftToBeShown.gSpeed);
                            //Add degree symbol
                            sheetTrackContent.setText(aircraftToBeShown.track + "\u00B0");
                            bottomSheet.setVisibility(View.VISIBLE);
                        }
                    })
                    .show();
            }
            }
        });

        Log.d(TAG, "Finished adding markers");
    }

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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(AIR_KEY, aircrafts);
        //Markers aren't Serializable.
        //outState.putSerializable("hashMap", aircraftAndMarkers);
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
    }
}
