package cz.muni.fi.anglictina.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.List;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.utils.Categories;
import cz.muni.fi.anglictina.utils.adapters.ResultsAdapter;

/**
 * Created by collfi on 3. 2. 2016.
 */
public class LearnedWordsFragment extends Fragment {
    private ExpandableListView mList;
    private AutoCompleteTextView mFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_words_list, container, false);
        mList = (ExpandableListView) view.findViewById(R.id.results);
        mList.setEmptyView(view.findViewById(android.R.id.empty));
        mFilter = (AutoCompleteTextView) view.findViewById(R.id.filter);

        mFilter.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                Categories.categoriesForHuman));
        mFilter.clearFocus();
//        mFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
////                    mFilter.showDropDown();
//                }
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
        mFilter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mFilter.getRight() - mFilter.getCompoundDrawables()[2].getBounds().width())) {
                        mFilter.setText("");
                        mFilter.clearFocus();
                        hideSoftKeyboard();
                        new SelectLearned().execute();
                        return true;
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
        new SelectLearned().execute();
        return view;
    }

    public void refresh() {
        ((ResultsAdapter) mList.getExpandableListAdapter()).notifyDataSetChanged();
    }

    public void filter(String cat) {
        hideSoftKeyboard();
        new SelectLearned().execute(cat);
    }

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFilter.getWindowToken(), 0);
    }


    public class SelectLearned extends AsyncTask<String, Void, List<Pair<Word, Boolean>>> {
        @Override
        protected List<Pair<Word, Boolean>> doInBackground(String... params) {
            SQLiteDatabase db = new WordDbHelper(getActivity()).getWritableDatabase();

            Cursor cursor = null;
            if (params.length == 0) {
                cursor = db.rawQuery("SELECT * FROM " + WordContract.LearnedWordEntry.TABLE_NAME, null);
            } else {
                String query = Normalizer.normalize(params[0], Normalizer.Form.NFD)
                        .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "");
                cursor = db.rawQuery("SELECT * FROM " + WordContract.LearnedWordEntry.TABLE_NAME
                        + " WHERE " + WordContract.LearnedWordEntry.COLUMN_NAME_HUMAN_CATEGORIES + " LIKE '%"
                        + query + "%'", null);
            }
            List<Pair<Word, Boolean>> results = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    Word w = new Word();
                    w.setWord(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.LearnedWordEntry.COLUMN_NAME_WORD)));
                    w.setTranslations(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.LearnedWordEntry.COLUMN_NAME_TRANSLATIONS)).split(";"));
                    w.setPronunciation(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.LearnedWordEntry.COLUMN_NAME_PRONUNCIATION)));
                    w.setCategories(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.LearnedWordEntry.COLUMN_NAME_CATEGORIES)).split(";"));
                    w.setHumanCategories(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.LearnedWordEntry.COLUMN_NAME_HUMAN_CATEGORIES)).split(";"));
                    w.setCorrectAnswers(cursor.getInt(cursor.getColumnIndexOrThrow(WordContract.LearnedWordEntry.COLUMN_NAME_CORRECT_COUNT)));
                    w.setIncorrectAnswers(cursor.getInt(cursor.getColumnIndexOrThrow(WordContract.LearnedWordEntry.COLUMN_NAME_INCORRECT_COUNT)));
                    results.add(new Pair<Word, Boolean>(w, null));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return results;
        }

        @Override
        protected void onPostExecute(List<Pair<Word, Boolean>> list) {
            mList.setAdapter(new ResultsAdapter(getActivity(), list));
        }
    }
}
