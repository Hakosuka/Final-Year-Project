package com.example.se415017.maynoothskyradar.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.activities.MainActivity;
import com.example.se415017.maynoothskyradar.helpers.TextValidator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    final String TAG = getClass().getSimpleName();

    @Bind(R.id.edit_url_fragment_title)
    TextView editUrlFragmentTitle;
    @Bind(R.id.edit_url_fragment_content)
    TextView editUrlFragmentContent;

    @Bind(R.id.server_address_edit_field)
    EditText serverAddressEditor;
    //TODO: Maybe it would be a good idea to ask the user to enter their latitude and longitude

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
        EnterURLFragment fragment = new EnterURLFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_enter_url, container, false);
        ButterKnife.bind(this, v);

        serverAddressEditor.addTextChangedListener(new TextValidator(serverAddressEditor) {
            @Override
            public void validate(TextView textView, String text) {
                //TODO: Add validation code
                Log.d(TAG, "Text to validate: " + text);
                if(text.length() > 0) {
                    enableResetButton(true);
                    //TODO: Implement a proper RegEx check
                    if(text.length() > 7) {
                        enableSubmitButton(true);
                    }
                }
            }
        });

        return inflater.inflate(R.layout.fragment_enter_url, container, false);
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
        editor.putString(SERVER_PREF, editorText);
        editor.apply();
        Intent returnToMainActivityIntent = new Intent(getActivity(), MainActivity.class);
        startActivity(returnToMainActivityIntent);
    }

    @OnClick(R.id.button_clear)
    protected void resetURLField(){
        serverAddressEditor.setText("");
        enableResetButton(false);
        enableSubmitButton(false);
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
