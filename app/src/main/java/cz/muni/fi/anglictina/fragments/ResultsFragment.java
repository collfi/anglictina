package cz.muni.fi.anglictina.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.utils.Categories;
import cz.muni.fi.anglictina.utils.Results;
import cz.muni.fi.anglictina.utils.adapters.ResultsAdapter;

/**
 * Created by collfi on 31. 1. 2016.
 */
public class ResultsFragment extends Fragment {
    private List<Pair<Word, Boolean>> results;
    private ExpandableListView mList;
    private AutoCompleteTextView mFilter;
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
        mFilter = (AutoCompleteTextView) view.findViewById(R.id.filter);
        mFilter.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                Categories.categories));
        mFilter.clearFocus();
        mFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filter(mFilter.getText().toString());
                Toast.makeText(getActivity(), mFilter.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        mFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                filter(mFilter.getText().toString());
                Toast.makeText(getActivity(), mFilter.getText().toString(), Toast.LENGTH_SHORT).show();
                mFilter.dismissDropDown();
                return true;
            }
        });
        return view;
    }

    public void filter(String cat) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFilter.getWindowToken(), 0);
    }

    public void refresh() {
        ((ResultsAdapter) mList.getExpandableListAdapter()).notifyDataSetChanged();
    }
}
