package com.example.se415017.maynoothskyradar.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.objects.Aircraft;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

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

    static View view;

//    @Bind(R.id.map_container)
//    RelativeLayout mapContainer;

//    @Bind(R.id.main_mapview)
//    MapView mapView;

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
    HashMap<String, Aircraft> markersAndAircraft = new HashMap<>();
    private boolean markersAdded; //Stops Markers being added to the map repeatedly

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
        //latitude = Double.parseDouble(sharedPreferences.getString(LAT_PREF, "0.0"));
        Log.d(TAG, "Latitude from sharedPreferences = " + Double.toString(latitude));
        //longitude = Double.parseDouble(sharedPreferences.getString(LON_PREF, "0.0"));
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

        mainMapFrag = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.main_map);
        mainMapFrag.getMapAsync(this);

        for(Aircraft a : aircrafts){
            Log.d(TAG, "Loading from bundle " + a.toString());
        }

        setUpMapIfNeeded();

        Log.d(TAG, "Lat & Lon: " + Double.toString(latitude) + ", " + Double.toString(longitude));
        //During testing, googleMap was always null at this point
//        if(googleMap != null) {
//            for(Aircraft a : aircrafts) {
//                //Check if the Aircraft object has latitude and longitude values yet
//                //If not, don't add them to the map, there'd be no point adding them in
//                if(a.latitude != null && a.longitude != null)
//                    Log.d(TAG, "Position (onCreateView) = " + a.getPosition());
//                    googleMap.addMarker(new MarkerOptions().position(a.getPosition()).title("Mode-S: " + a.icaoHexAddr)
//                        .snippet("Coordinates: " + a.getPosString())
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane_north))
//                        .flat(true)
//                        .rotation(Float.parseFloat(a.track))); //rotate the marker by the track of the aircraft
//            }
//            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                @Override
//                public void onInfoWindowClick(Marker marker) {
//                    //Show the Aircraft's location if its Marker is clicked
//                    if(marker.getTitle().startsWith("Mode-S")){
//                        Toast.makeText(getContext(), marker.getSnippet(), Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
//        } else {
//            Log.d(TAG, "onCreateView: Map is null");
//        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        if(googleMap != null) {
            setUpMap(googleMap);
        } else { //It's null anyway
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map)).getMapAsync(this);
            mainMapFrag.getMapAsync(this);
            if (googleMap != null) {
                setUpMap(googleMap);
            }
        }
    }

    //For handling configuration changes - otherwise the map zooms out to 0.0N, 0.0W, and all markers are lost
    @Override
    public void onPause() {
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
        super.onResume();
        setUpMapIfNeeded();
        //Wait until googleMap is re-initialised
        if(cameraPosition != null & googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            cameraPosition = null;
        }
    }

    //Sets up the map if it hasn't been set up already
    protected void setUpMapIfNeeded() {
        if (googleMap == null){
            Log.d(TAG, "Map was null");
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map))
                    .getMapAsync(this);
            //Check if the map was obtained successfully
            if (googleMap != null) {
                setUpMap(googleMap);
            }
        }
    }

    //TODO: This is being called 4 times!
    /** This is where markers, lines and listeners are added, and where the camera is moved.
     *  @param googleMap The GoogleMap object to be set up.
     */
    private void setUpMap(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //googleMap.setMyLocationEnabled(true); TODO: Maybe wait until I have the pointing function worked out
        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)))
                .setTitle("My server is here");
        //DONE: Add custom markers for the planes - see onCreateView()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 7.0f));
        Log.d(TAG, "Map has been set up");
        Log.d(TAG, "Number of planes to set up: " + aircrafts.size());
        Log.d(TAG, "Have the markers been added? " + Boolean.toString(markersAdded));
        if(!markersAdded) {
            addMarkers();
            markersAdded = true;
        }
    }
    /**
     * TODO:
     * Adds a new Aircraft marker when a new aircraft has been discovered.
     * @param newAircraft The newly discovered aircraft to be added to the map.
     */
    protected void onNewAircraftDiscovered(Aircraft newAircraft){
        aircrafts.add(newAircraft);
        if(googleMap != null) {
            if(newAircraft.latitude != null && newAircraft.longitude != null) {
                Marker m = googleMap.addMarker(new MarkerOptions().position(newAircraft.getPosition())
                        .title("Mode-S: " + newAircraft.icaoHexAddr)
                        .snippet("Coordinates: " + newAircraft.getPosString())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane_north))
                        .flat(true)
                        .rotation(Float.parseFloat(newAircraft.track)));
                Log.d(TAG, "Aircraft path = " + newAircraft.pathToString());
                googleMap.addPolyline(new PolylineOptions()
                        .width(3.0f)
                        .color(Color.rgb(253, 95, 0)) //"Neon orange" colour - stands out on the map
                        .geodesic(true)
                        .addAll(newAircraft.path));
                markersAndAircraft.put(m.getId(), newAircraft);
            }

        }
    }

    //This is what's calling setUpMap
    @Override
    public void onMapReady(final GoogleMap gMap) {
        if(!mapSetUp){
            Log.d(TAG, "Has map been set up? " + Boolean.toString(mapSetUp));
            googleMap = gMap;
            Log.d(TAG, "setUpMap - onMapReady");
            setUpMap(googleMap);
            mapSetUp = true;
        }
    }

    /**
     * TODO:
     * Adds markers and polylines to the map for each Aircraft.
     */
    public void addMarkers(){
        for(Aircraft a : aircrafts) {
            //Check if the Aircraft object has latitude and longitude values yet
            //If not, don't add them to the map, there'd be no point adding them in
            if(a.latitude != null && a.longitude != null) {
                Marker m = googleMap.addMarker(new MarkerOptions().position(a.getPosition()).title("Mode-S: " + a.icaoHexAddr)
                        .snippet("Coordinates: " + a.getPosString())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane_north))
                        .flat(true)
                        .rotation(Float.parseFloat(a.track))); //rotate the marker by the track of the aircraft
                googleMap.addPolyline(new PolylineOptions()
                        .width(5.0f)
                        .color(Color.rgb(253, 95, 0)) //"Neon orange" colour - stands out easily on the map
                        .geodesic(true) //Draws lines on the map assuming it's a globe, not a flat map
                        .addAll(a.path));
                markersAndAircraft.put(m.getId(), a);
                Log.d(TAG, "Marker ID for " + a.icaoHexAddr + " = " + m.getId());
            }
        }
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Aircraft selected = markersAndAircraft.get(marker.getId());
                //Sometimes some Markers weren't mapped to an Aircraft object, causing a NullPointerException.
                if(selected != null) {
                    Log.d(TAG, "Marker ID = " + marker.getId() + " selected, corresponds to " + selected.icaoHexAddr);
                    //Show the Aircraft's location if its Marker is clicked
                    if (marker.getTitle().startsWith("Mode-S")) {
                        Snackbar.make(view, "Do you want to see more details on this aircraft?", Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //TODO: Take the user to the AircraftDetailFragment
                                        Log.d(TAG, "Snackbar button clicked.");
                                    }
                                })
                                .show();
                    }
                    //I need to add this too, otherwise the map won't centre on the Marker's location
//                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
//                            selected.getPosition(), 8.0f, 0.0f, 0.0f)));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selected.getPosition(), 7.0f));
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
                Aircraft selected = markersAndAircraft.get(marker.getId());
                Log.d(TAG, "Marker ID = " + marker.getId() + " selected, corresponds to " + selected.icaoHexAddr);
                //Show the Aircraft's location if its Marker is clicked
                if (marker.getTitle().startsWith("Mode-S")) {
                    Snackbar.make(view, "Do you want to see more details on this aircraft?", Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //TODO: Take the user to the AircraftDetailFragment
                                    Log.d(TAG, "Snackbar button clicked.");
                                }
                            })
                            .show();
                }
            }
            //Such a method doesn't exist for the Google Maps API.
//            public void onLongInfoWindowClick(Marker marker) {
//                if (marker.getTitle().startsWith("Mode-S")) {
//                    Toast.makeText(getContext(), marker.getSnippet(), Toast.LENGTH_LONG).show();
//                }
//            }
        });
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
//        if (googleMap != null) { TODO: CANNOT PERFORM THIS ACTION AFTER ONSAVEINSTANCESTATE
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
