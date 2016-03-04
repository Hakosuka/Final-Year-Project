package com.example.se415017.maynoothskyradar.fragments;

import android.content.Context;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.se415017.maynoothskyradar.R;
//TODO: Implement THIS
import com.example.se415017.maynoothskyradar.fragments.AircraftListItem.OnListFragmentInteractionListener;
import com.example.se415017.maynoothskyradar.fragments.dummy.DummyContent.DummyItem;
import com.example.se415017.maynoothskyradar.objects.Aircraft;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AircraftRecyclerViewAdapter extends
        RecyclerView.Adapter<AircraftRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Aircraft> aircraftList;
    private final AircraftListFragment.OnListFragmentInteractionListener mListener;

    private Context context;
    private boolean useList = true;

    public AircraftRecyclerViewAdapter(Context context, List<Aircraft> items) {
        this.context = context;
        this.aircraftList = (ArrayList) items;
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
        holder.mIdView.setText(aircraftList.get(position).icaoHexAddr);
        holder.mContentView.setText(aircraftList.get(position).getPosString());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return aircraftList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Aircraft mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
