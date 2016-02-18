package cz.muni.fi.anglictina.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            results = new ArrayList<>(((Results) getArguments().getParcelable("results")).res);
        }
        getActivity().setTitle("Results");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_words_list, container, false);
        mList = (ExpandableListView) view.findViewById(R.id.results);
        Set<String> cats = new HashSet<>();
        for (Pair<Word, Boolean> w : results) {
//            cats.addAll(Arrays.asList(w.first.getHumanCategories()));
            for (String s : w.first.getHumanCategories()) {
                cats.add(Categories.categoriesForHuman[Categories.categoriesForHumanAscii.indexOf(s)]);
            }
        }
        mList.setAdapter(new ResultsAdapter(getActivity(), results));

        mFilter = (AutoCompleteTextView) view.findViewById(R.id.filter);
        mFilter.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                cats.toArray()));
        mFilter.clearFocus();

//        mFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                filter(mFilter.getText().toString());
//                Toast.makeText(getActivity(), mFilter.getText().toString(), Toast.LENGTH_SHORT).show();
//                mFilter.dismissDropDown();
//                return true;
//            }
//        });
        mFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilter.showDropDown();
            }
        });
        mFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mFilter.getText().length() == 0) {
                    hideSoftKeyboard();
                }
            }
        });
        mFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                filter(mFilter.getText().toString());
                mFilter.clearFocus();
                hideSoftKeyboard();
                mFilter.dismissDropDown();
                return true;
            }
        });
        mFilter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mFilter.getRight() - mFilter.getCompoundDrawables()[2].getBounds().width())) {
                        mFilter.setText("");
                        mFilter.clearFocus();
                        hideSoftKeyboard();
                        new SelectLearned().execute("");
                        return true;
                    } else {
                        mFilter.showDropDown();
                    }
                }
                return false;
            }
        });
        mFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filter(mFilter.getText().toString());
            }
        });

        return view;


    }

    public void filter(String cat) {
        hideSoftKeyboard();
        new SelectLearned().execute(cat);
    }

    public void refresh() {
        ((ResultsAdapter) mList.getExpandableListAdapter()).notifyDataSetChanged();
    }

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFilter.getWindowToken(), 0);
    }

    public class SelectLearned extends AsyncTask<String, Void, List<Pair<Word, Boolean>>> {
        @Override
        protected List<Pair<Word, Boolean>> doInBackground(String... params) {
            String what = Normalizer.normalize(params[0], Normalizer.Form.NFD)
                    .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "");
            List<Pair<Word, Boolean>> res = new ArrayList<>();
            for (Pair<Word, Boolean> w : results) {
                if (params[0] == null || params[0].equals("") || contains(what, w.first.getHumanCategories())) {
                    res.add(w);
                }
            }
            return res;
        }

        @Override
        protected void onPostExecute(List<Pair<Word, Boolean>> list) {
            mList.setAdapter(new ResultsAdapter(getActivity(), list));
        }

        public boolean contains(String what, String[] where) {
            boolean result = false;
            for (String s : where) {
                if (s.toLowerCase().equals(what.toLowerCase()) || s.toLowerCase().startsWith(what.toLowerCase())) {
                    result = true;
                    break;
                }
            }
            return result;
        }
    }
}
