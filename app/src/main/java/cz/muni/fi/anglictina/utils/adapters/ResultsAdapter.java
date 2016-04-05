package cz.muni.fi.anglictina.utils.adapters;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.fragments.LearnedWordsFragment;
import cz.muni.fi.anglictina.fragments.LearningFragment;
import cz.muni.fi.anglictina.fragments.ResultsFragment;
import cz.muni.fi.anglictina.utils.Categories;
import cz.muni.fi.anglictina.utils.Results;

/**
 * Created by collfi on 31. 1. 2016.
 */
public class ResultsAdapter extends BaseExpandableListAdapter {

    private List<Pair<Word, Boolean>> mResults;
    private Context mContext;
    private SharedPreferences resultsPref;

    public ResultsAdapter(Context context, List<Pair<Word, Boolean>> list) {
        mContext = context;
        mResults = list;
        resultsPref = mContext.getSharedPreferences("results", Context.MODE_PRIVATE);

    }

    @Override
    public int getGroupCount() {
        return mResults.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.results_item, parent, false);
            convertView.setId(groupPosition);
            holder = new GroupViewHolder();
            holder.word = (TextView) convertView.findViewById(R.id.word);
            holder.translation = (TextView) convertView.findViewById(R.id.translation);
            holder.correct = (TextView) convertView.findViewById(R.id.correct);
            holder.incorrect = (TextView) convertView.findViewById(R.id.incorrect);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }
        holder.word.setText(mResults.get(groupPosition).first.getWord());
        holder.translation.setText(mResults.get(groupPosition).first.getTranslations()[0]);
        holder.correct.setText(String.valueOf(mResults.get(groupPosition).first.getCorrectAnswers()));
        holder.incorrect.setText(String.valueOf(mResults.get(groupPosition).first.getIncorrectAnswers()));
        if (mResults.get(groupPosition).second == null) {
            holder.word.setTextColor(Color.parseColor("#000000"));
            holder.translation.setTextColor(Color.parseColor("#000000"));
        } else {
            if (mResults.get(groupPosition).second) {
                holder.word.setTextColor(mContext.getResources().getColor(R.color.greenCorrect));
                holder.translation.setTextColor(mContext.getResources().getColor(R.color.greenCorrect));
            } else {
                holder.word.setTextColor(mContext.getResources().getColor(R.color.redIncorrect));
                holder.translation.setTextColor(mContext.getResources().getColor(R.color.redIncorrect));
            }
        }
        holder.correct.setTextColor(mContext.getResources().getColor(R.color.greenCorrect));
        holder.incorrect.setTextColor(mContext.getResources().getColor(R.color.redIncorrect));
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.results_item_expand, parent, false);
            convertView.setId(groupPosition);
            holder = new ChildViewHolder();
            holder.categories = (TextView) convertView.findViewById(R.id.categories);
            holder.change = (Button) convertView.findViewById(R.id.change_button);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : mResults.get(groupPosition).first.getHumanCategories()) {
            String resultCategory = "";
//            for (String cat : Categories.categoriesForHuman) {
//                if (Normalizer.normalize(cat, Normalizer.Form.NFD)
//                        .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "").equals(s)) {
//                    resultCategory = cat;
//                    break;
//                }
//            }
            resultCategory = Categories.categoriesForHuman[Categories.categoriesForHumanAscii.indexOf(s)];
            sb.append(delim).append(resultCategory).append(" <font color=#8BC34A>").append(resultsPref.getInt(s + "_correct", 0))
                    .append("</font> <font color=#F44336>").append(resultsPref.getInt(s + "_incorrect", 0)).append("</font>");
            delim = "<br>";
        }
//        holder.categories.setText(Html.fromHtml(sb.toString()));
        holder.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TranslationsDialogFragment.newInstance(mResults.get(groupPosition).first).
                        show(((AppCompatActivity) mContext).getSupportFragmentManager(), "translations");
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private static class GroupViewHolder {
        TextView word;
        TextView translation;
        TextView correct;
        TextView incorrect;
    }

    private static class ChildViewHolder {
        TextView categories;
        Button change;
    }

    public static class TranslationsDialogFragment extends DialogFragment {

        private ArrayList<String> translations;
        private Word word;

        public static TranslationsDialogFragment newInstance(String[] t, Results r) {
            TranslationsDialogFragment fragment = new TranslationsDialogFragment();
            Bundle args = new Bundle();
            args.putStringArray("translations", t);
            args.putParcelable("results", r);
            fragment.setArguments(args);
            return fragment;
        }

        public static TranslationsDialogFragment newInstance(Word w) {
            TranslationsDialogFragment fragment = new TranslationsDialogFragment();
            Bundle args = new Bundle();
            args.putParcelable("word", w);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                String[] t = getArguments().getStringArray("translations");
                word = getArguments().getParcelable("word");
//                translations = new ArrayList<>();
//                Collections.addAll(translations, t);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_translations, null);

            builder.setView(view);


            final Dialog d = builder.create();
            ListView list = (ListView) view.findViewById(R.id.translations);
            list.setAdapter(new TranslationsAdapter(getActivity(), Arrays.asList(word.getTranslations())));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getCount() == 1 || position == 0) {
                        dismiss();
                        return;
                    }


                    String wordString = null;
                    String[] trans = null;

//                    if (buttonPosition < 4) {
//                        ((Button) longClicked).setText(translations.get(position));
                    LearningFragment.swap(word.getTranslations(), 0, position);
                    trans = word.getTranslations();
                    wordString = word.getWord();
//                    } else {
//                        fragment.getWord().setText(translations.get(position));
//                        LearningFragment.swap(fragment.getCurrentWord().getTranslations(), 0, position);
//                        trans = fragment.getCurrentWord().getTranslations();
//                        word = fragment.getCurrentWord().getWord();
//                        longClicked.postInvalidate();
//                    }
                    SQLiteDatabase db = new WordDbHelper(getActivity()).getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    cv.put(WordContract.LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, LearningFragment.arrayToString(
                            trans));
                    db.update(WordContract.LearnedWordEntry.TABLE_NAME, cv, WordContract.LearnedWordEntry.COLUMN_NAME_WORD
                            + " = ?", new String[]{wordString});
                    cv.clear();
                    cv.put(WordContract.WordEntry.COLUMN_NAME_TRANSLATIONS, LearningFragment.arrayToString(
                            trans));
                    db.update(WordContract.WordEntry.TABLE_NAME, cv, WordContract.WordEntry.COLUMN_NAME_WORD
                            + " = ?", new String[]{wordString});


                    Fragment fragment = getFragmentManager().
                            findFragmentByTag("results");
                    if (fragment == null) {
                        fragment = getFragmentManager().
                                findFragmentById(R.id.revise_fragment);
                        ((LearnedWordsFragment) fragment).refresh();
                    } else {
                        ((ResultsFragment) fragment).refresh();
                    }

                    db.close();
                    dismiss();
                }
            });
            return d;
        }

    }
}
