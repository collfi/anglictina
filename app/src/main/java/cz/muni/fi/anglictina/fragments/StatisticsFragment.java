package cz.muni.fi.anglictina.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.utils.adapters.StatisticsAdapter;

/**
 * Created by collfi on 30. 4. 2016.
 */
public class StatisticsFragment extends Fragment {

    private ExpandableListView list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        list = (ExpandableListView) view.findViewById(R.id.stats_list);
        list.setAdapter(new StatisticsAdapter(getActivity()));
        return view;
    }
}
