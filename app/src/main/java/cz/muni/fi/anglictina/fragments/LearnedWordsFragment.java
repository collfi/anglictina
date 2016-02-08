package cz.muni.fi.anglictina.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.utils.adapters.ResultsAdapter;

/**
 * Created by collfi on 3. 2. 2016.
 */
public class LearnedWordsFragment extends Fragment {
    private ExpandableListView mList;

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
        new SelectLearned().execute();
        return view;
    }

    public void refresh() {
        ((ResultsAdapter) mList.getExpandableListAdapter()).notifyDataSetChanged();
    }

    public class SelectLearned extends AsyncTask<Void, Void, List<Pair<Word, Boolean>>> {
        @Override
        protected List<Pair<Word, Boolean>> doInBackground(Void... params) {
            SQLiteDatabase db = new WordDbHelper(getActivity()).getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + WordContract.LearnedWordEntry.TABLE_NAME, null);
            List<Pair<Word, Boolean>> results = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    Word w = new Word();
                    w.setWord(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_WORD)));
                    w.setTranslations(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_TRANSLATIONS)).split(";"));
                    w.setPronunciation(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_PRONUNCIATION)));
                    w.setCategories(cursor.getString(cursor.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_CATEGORIES)).split(";"));
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
