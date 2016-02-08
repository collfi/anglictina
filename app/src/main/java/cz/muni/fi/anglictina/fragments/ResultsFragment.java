package cz.muni.fi.anglictina.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.utils.Results;
import cz.muni.fi.anglictina.utils.adapters.ResultsAdapter;

/**
 * Created by collfi on 31. 1. 2016.
 */
public class ResultsFragment extends Fragment {
    private List<Pair<Word, Boolean>> results;
    private ExpandableListView mList;
    public static ResultsFragment newInstance(Results r) {
        ResultsFragment fragment = new ResultsFragment();
        Bundle args = new Bundle();
        args.putParcelable("results", r);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.i("results",  "som v if " + results);
            results = new ArrayList<>(((Results) getArguments().getParcelable("results")).res);
            Log.i("results",  "som v if " + results);

        }
        getActivity().setTitle("Results");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_words_list, container, false);
        mList = (ExpandableListView) view.findViewById(R.id.results);
        Log.i("results", "create view " + results);

        mList.setAdapter(new ResultsAdapter(getActivity(), results));
        return view;
    }

    public void refresh() {
        ((ResultsAdapter) mList.getExpandableListAdapter()).notifyDataSetChanged();
    }
}
