package com.example.se415017.maynoothskyradar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.activities.MainActivity;
import com.example.se415017.maynoothskyradar.objects.Aircraft;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AircraftListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String AIR_KEY = "aircraftKey";
    private static final String TAG = "AircraftListFragment";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private ArrayList<Aircraft> aircraftArrayList;
    List<AircraftListItem> aircraftListItems;
    private AircraftRecyclerViewAdapter mAdapter;
    @Bind(android.R.id.list)
    RecyclerView aircraftListView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AircraftListFragment() {
    }

    @SuppressWarnings("unused")
    public static AircraftListFragment newInstance(int columnCount, ArrayList<Aircraft> aircraftArrayList) {
        AircraftListFragment fragment = new AircraftListFragment();
        Bundle args = new Bundle();
        args.putSerializable(AIR_KEY, aircraftArrayList);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Just in case aircraftArrayList hasn't been initialised yet
        if (aircraftArrayList == null) {
            aircraftArrayList = new ArrayList<Aircraft>();
        }
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            //Need to populate the ArrayList of Aircraft somehow
            if(getArguments().getSerializable(AIR_KEY) instanceof ArrayList<?>) {
                ArrayList<?> unknownTypeList = (ArrayList<?>) getArguments().getSerializable(AIR_KEY);
                if(unknownTypeList != null && unknownTypeList.size() > 0) {
                    for (int i = 0; i < unknownTypeList.size(); i++) {
                        Object unknownTypeObject = unknownTypeList.get(i);
                        if(unknownTypeObject instanceof Aircraft){
                            aircraftArrayList.add((Aircraft) unknownTypeObject);
                        }
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aircraft_list, container, false);
        ButterKnife.bind(this, view);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new AircraftRecyclerViewAdapter(context, aircraftArrayList, mListener); //, mListener));
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    public void updateDataset(ArrayList<Aircraft> aircraftArrayList) {
        this.aircraftArrayList = aircraftArrayList;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

//    @Override
//    public void onListItemClick(ListView listView, View v, int position, long id){
//        super.onListItemClick(listView, v, position, id);
//    }

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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Aircraft item);
        //public void onListItemSelection(int position);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        Log.d(TAG, "Item #" + position + " clicked");
    }
}
