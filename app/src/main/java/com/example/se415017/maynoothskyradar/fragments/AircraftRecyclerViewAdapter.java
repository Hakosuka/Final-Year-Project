package com.example.se415017.maynoothskyradar.fragments;

import android.content.Context;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.se415017.maynoothskyradar.R;
import com.example.se415017.maynoothskyradar.fragments.AircraftListFragment.OnListFragmentInteractionListener;
import com.example.se415017.maynoothskyradar.objects.Aircraft;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AircraftListItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class AircraftRecyclerViewAdapter extends
        RecyclerView.Adapter<AircraftRecyclerViewAdapter.ViewHolder> {

    public ArrayList<Aircraft> aircraftList;
    private List<AircraftListItem> aircraftListItems;
    private final AircraftListFragment.OnListFragmentInteractionListener mListener;

    private Context context;
    private boolean useList = true;

    public AircraftRecyclerViewAdapter(Context context, List<Aircraft> items,
                                       AircraftListFragment.OnListFragmentInteractionListener listener) {
        this.context = context;
        if (aircraftList == null){
            aircraftList = new ArrayList<Aircraft>();
        }
        if (aircraftListItems == null){
            aircraftListItems = new ArrayList<AircraftListItem>();
        }
        if (items != null) {
            if(items.size() > 0) {
                for (int i = 0; i < items.size(); i++) {
                    Object unknownTypeObject = items.get(i);
                    if(unknownTypeObject != null){
                        Aircraft aircraftToShow = (Aircraft) unknownTypeObject;
                        aircraftList.add(aircraftToShow);
                        aircraftListItems.add(new AircraftListItem(aircraftToShow));
                    }
                }
            }
        }
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_aircraft, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = aircraftList.get(position);
        holder.hexContent.setText(aircraftList.get(position).icaoHexAddr);
        holder.altitudeContent.setText(aircraftList.get(position).altitude);
        holder.latitudeContent.setText(aircraftList.get(position).latitude);
        holder.longitudeContent.setText(aircraftList.get(position).longitude);
        holder.callsignContent.setText(aircraftList.get(position).callsign);
    }

    @Override
    public int getItemCount() {
        return aircraftList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.hex_title) TextView hexTitle;
        @Bind(R.id.hex_content) TextView hexContent;
        @Bind(R.id.callsign_content) TextView callsignContent;
        @Bind(R.id.altitude_title) TextView altitudeTitle;
        @Bind(R.id.altitude_content) TextView altitudeContent;
        @Bind(R.id.latitude_title) TextView latitudeTitle;
        @Bind(R.id.latitude_content) TextView latitudeContent;
        @Bind(R.id.longitude_title) TextView longitudeTitle;
        @Bind(R.id.longitude_content) TextView longitudeContent;
        public Aircraft mItem;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + hexContent.getText() + "'";
        }

        //TODO: Take the user to the AircraftDetailFragment
        @Override
        public void onClick(View view){
            Log.d("AircraftRecycler", view.toString());
            mListener.onListItemSelection(view, getLayoutPosition());
        }
    }
}
