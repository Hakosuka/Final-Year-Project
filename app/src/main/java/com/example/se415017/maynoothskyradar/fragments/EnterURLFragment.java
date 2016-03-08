package com.example.se415017.maynoothskyradar.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.activities.MainActivity;
import com.example.se415017.maynoothskyradar.helpers.DecimalDigitsInputFilter;
import com.example.se415017.maynoothskyradar.helpers.InputHelper;
import com.example.se415017.maynoothskyradar.helpers.WebAddressValidator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EnterURLFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EnterURLFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * This Fragment appears if the user is using the app for the first time. I thought it would
 * improve the UI over using an AlertDialog to accept the user's server's address, especially
 * since the EditText box inside the AlertDialog would get cleared when the screen configuration
 * changes, and that would suck.
 */
public class EnterURLFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    final String PREFS = "UserPreferences";
    final String SERVER_PREF = "serverAddress";
    final String LAT_PREF = "latitude";
    final String LON_PREF = "longitude";
    final String TAG = getClass().getSimpleName();
    
    InputHelper inputHelper;

    boolean validLatitude = false;
    boolean validLongitude = false;
    boolean validServerAddress = false;

    String enteredLat = "";
    String enteredLon = "";
    String enteredAddress = "";

    @Bind(R.id.edit_url_fragment_title)
    TextView editUrlFragmentTitle;
    @Bind(R.id.edit_url_fragment_content)
    TextView editUrlFragmentContent;
    @Bind(R.id.edit_lat_lon_content)
    TextView editLatLonContent;

    @Bind(R.id.server_address_edit_field)
    EditText serverAddressEditor;
    @OnTextChanged(R.id.server_address_edit_field)
    void onServerTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG, "Entered URL = " + s.toString());
        enteredAddress = s.toString();
        clearServerAddressEditorButton.setEnabled(enteredAddress.length() > 0
                || enteredLat.length() > 0 || enteredLon.length() > 0);
        //Checks that the user has entered a valid web address
        submitServerAddressButton.setEnabled(inputHelper.checkForValidLat(enteredLat)
                && inputHelper.checkForValidLon(enteredLon)
                && inputHelper.checkForValidURLOrIP(enteredAddress));
    }

    @Bind(R.id.latitude_edit_field)
    EditText latitudeEditor;
    @OnTextChanged(value=R.id.latitude_edit_field)
    void onLatitudeTextChanged(CharSequence s, int start, int before, int count){
        Log.d(TAG, "Entered latitude = " + s);
        enteredLat = s.toString();
        clearServerAddressEditorButton.setEnabled(enteredAddress.length() > 0
            || enteredLat.length() > 0 || enteredLon.length() > 0);
        submitServerAddressButton.setEnabled(inputHelper.checkForValidLat(enteredLat)
            && inputHelper.checkForValidLon(enteredLon)
            && inputHelper.checkForValidURLOrIP(enteredAddress));
    }

    @Bind(R.id.longitude_edit_field)
    EditText longitudeEditor;
    @OnTextChanged(R.id.longitude_edit_field)
    void onLongitudeTextChanged(CharSequence s, int start, int before, int count){
        Log.d(TAG, "Entered longitude = " + s);
        enteredLon = s.toString();
        clearServerAddressEditorButton.setEnabled(enteredAddress.length() > 0
                || enteredLat.length() > 0 || enteredLon.length() > 0);
        submitServerAddressButton.setEnabled(inputHelper.checkForValidLat(enteredLat)
                && inputHelper.checkForValidLon(enteredLon)
                && inputHelper.checkForValidURLOrIP(enteredAddress));
    }

    @Bind(R.id.button_submit_server_address)
    Button submitServerAddressButton;
    @Bind(R.id.button_clear)
    Button clearServerAddressEditorButton;

    private OnFragmentInteractionListener mListener;

    public EnterURLFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment EnterURLFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EnterURLFragment newInstance() {
        return new EnterURLFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_enter_url, container, false);
        ButterKnife.bind(this, v);

        if(inputHelper == null)
            inputHelper = new InputHelper();

        Log.d(TAG, "Is savedInstanceState empty? " +
                Boolean.toString(savedInstanceState == null));

        // Limit the user to two decimal places
        latitudeEditor.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});
        longitudeEditor.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});

        // If the user has changed their device's orientation, load their inputs from before they did that
        if(savedInstanceState != null){
            Log.d(TAG, "Entering saved user inputs into EditText fields");
            serverAddressEditor.setText(savedInstanceState.getString(SERVER_PREF, "")); //TODO: NullPointerException!
            latitudeEditor.setText(savedInstanceState.getString(LAT_PREF, ""));
            longitudeEditor.setText(savedInstanceState.getString(LON_PREF, ""));
        } else {
            Log.d(TAG, "No saved user inputs to enter");
        }

        return v;
    }
    //The following two methods determine whether I enable the "submit" and "reset" buttons.
    protected void enableSubmitButton(boolean doIEnableThis){
        submitServerAddressButton.setEnabled(doIEnableThis);
    }

    protected void enableResetButton(boolean doIEnableThis){
        clearServerAddressEditorButton.setEnabled(doIEnableThis);
    }

    @OnClick(R.id.button_submit_server_address)
    protected void submitServerAddress(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String editorText = serverAddressEditor.getText().toString();
        // Android doesn't have a "putDouble()" method - thanks for being lazy, Google!
        long latFromEditor = Double.doubleToRawLongBits(
                Double.parseDouble(latitudeEditor.getText().toString()));
        long lonFromEditor = Double.doubleToRawLongBits(
                Double.parseDouble(longitudeEditor.getText().toString()));
        editor.putString(SERVER_PREF, editorText);
        editor.putLong(LAT_PREF, latFromEditor);
        editor.putLong(LON_PREF, lonFromEditor);
        editor.apply();
        Intent returnToMainActivityIntent = new Intent(getActivity(), MainActivity.class);
        startActivity(returnToMainActivityIntent);
    }

    @OnClick(R.id.button_clear)
    protected void resetURLField(){
        serverAddressEditor.setText("");
        latitudeEditor.setText("");
        longitudeEditor.setText("");
        enableResetButton(false);
        enableSubmitButton(false);
    }

    /**
     * Checks that the string entered by the user is a valid latitude value.
     * @param lat the string to be checked to ensure that it's a number between -90 and 90 (inclusive)
     * @return whether or not the entered number is between -90 and 90
     */
    public boolean checkForValidLat(String lat){
        return lat.matches("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)");
    }

    /**
     * Checks that the string entered by the user is a valid longitude value.
     * @param lon the string to be entered to ensure that it's a number between -180 and 180 (inclusive)
     * @return whether or not the entered number is between -180 and 180
     */
    public boolean checkForValidLon(String lon){
        return lon.matches("[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)");
    }

    /**
     * Checks
     * @param urlStr The URL (or IP address) of the server entered by the user
     * @return whether or not the entered string represents a valid URL/IP address
     */
    public boolean checkForValidURLOrIP(String urlStr){
        return (Patterns.WEB_URL.matcher(urlStr).matches()
                || Patterns.IP_ADDRESS.matcher(urlStr).matches());
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() //TODO: implement OnFragmentInteractionListner
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

    @Override
    public void onDestroy(){
        Bundle savedInstanceState = new Bundle();
        enteredLat = latitudeEditor.getText().toString();
        enteredLon = longitudeEditor.getText().toString();
        enteredAddress = serverAddressEditor.getText().toString();
        Log.d(TAG, "Editor entries = " + enteredLat + ", " + enteredLon + ", " + enteredAddress);
        Log.d(TAG, "Fragment destroyed");
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Preparing to save state");
        super.onSaveInstanceState(outState);
        if(!serverAddressEditor.getText().toString().equals(""))
            outState.putString(SERVER_PREF, serverAddressEditor.getText().toString());
        if(!latitudeEditor.getText().toString().equals(""))
            outState.putString(LAT_PREF, latitudeEditor.getText().toString());
        if(!longitudeEditor.getText().toString().equals(""))
            outState.putString(LON_PREF, longitudeEditor.getText().toString());
    }
}
