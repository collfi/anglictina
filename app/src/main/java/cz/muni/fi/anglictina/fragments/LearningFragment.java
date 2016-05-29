package cz.muni.fi.anglictina.fragments;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.activities.MainActivity;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordContract.LearnedWordEntry;
import cz.muni.fi.anglictina.db.WordContract.WordEntry;
import cz.muni.fi.anglictina.db.WordDbHelper;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.utils.IncorrectQuestion;
import cz.muni.fi.anglictina.utils.OnSwipeTouchListener;
import cz.muni.fi.anglictina.utils.Results;
import cz.muni.fi.anglictina.utils.WordLevenshteinComparator;

/**
 * Created by collfi on 27. 10. 2015.
 */
public class LearningFragment extends Fragment implements TextToSpeech.OnInitListener {
    //    public static final long INTERVAL = 43200;//12 hours
    public static final long INTERVAL = 86400;//24 hours
    public static final long DAY_MILISECONDS = 86400000;
    //        public static final long INTERVAL = 10;//10 sec
    public static final float DEFAULT_WORD_COEFFICIENT = 2.2f;
    private float mSkill;
    private Button a;
    private Button b;
    private Button c;
    private Button d;
    private Button dontKnow;
    private TextView mWord;
    private TextView mPron;
    private SQLiteDatabase mWordsDb;
    private TextToSpeech tts;
    //    private TextView mViewSkill;
//    private TextView mChance;
    private ProgressBar progress;
    private SharedPreferences mPreferences;
    private List<Word> mWords;
    boolean repeating;
    private Word mCurrentWord;
    private int direction;
    private Handler mHandler;
    private int count;
    private int currentCount;
    private List<Pair<Word, Boolean>> results;
    private int selectedButton;
    private ProgressDialog pd;
    private JSONArray incorrect;
    private List<IncorrectQuestion> incorrectList;
    private boolean repeatingIncorrect;
    private boolean fromNotification;
    private int reviseCount;

    String[] projection = {WordContract.WordEntry.COLUMN_NAME_ID, WordContract.WordEntry.COLUMN_NAME_WORD,
            WordContract.WordEntry.COLUMN_NAME_TRANSLATIONS, WordContract.WordEntry.COLUMN_NAME_PRONUNCIATION,
            WordContract.WordEntry.COLUMN_NAME_FREQUENCY, WordContract.WordEntry.COLUMN_NAME_PERCENTIL};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {//todo set max smooth?
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(getActivity(), this);
        mPreferences = getActivity().getSharedPreferences("stats", Context.MODE_PRIVATE);
        mSkill = mPreferences.getFloat("skill", 0);
        mWordsDb = new WordDbHelper(getActivity()).getWritableDatabase();
        mWords = new ArrayList<>();
        fromNotification = getActivity().getIntent().getBooleanExtra("notification", false);
        mHandler = new Handler();
        count = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_count", "10"));
        results = new ArrayList<>(count);
        incorrect = new JSONArray();
        incorrectList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_learning, container);

        a = (Button) v.findViewById(R.id.button_a);
        b = (Button) v.findViewById(R.id.button_b);
        c = (Button) v.findViewById(R.id.button_c);
        d = (Button) v.findViewById(R.id.button_d);
        dontKnow = (Button) v.findViewById(R.id.button_dont_know);
        mWord = (TextView) v.findViewById(R.id.word);
        mPron = (TextView) v.findViewById(R.id.pronunciation);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        progress.setMax(count * 100);
        progress.setProgress(0);

//        final RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.learning_layout);

//        a.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                final Spinner spinner = new Spinner(getActivity());
//                ArrayAdapter<CharSequence> ad = ArrayAdapter.createFromResource(getActivity(),
//                        R.array.pref_sync_frequency_titles, android.R.layout.simple_spinner_dropdown_item);
//                DisplayMetrics metrics = getResources().getDisplayMetrics();
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                        (int) (160 * metrics.density), ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.button_a);
//                params.setMargins(15, 0, 0, 0);
//                spinner.setLayoutParams(params);
//                spinner.setAdapter(ad);
//                spinner.performClick();
//                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        spinner.performClick();
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//                        spinner.performClick();
//                    }
//                });
//                layout.addView(spinner);
//                return true;
//            }
//        });


//        a.setOnClickListener(l);
//        b.setOnClickListener(l);
//        c.setOnClickListener(l);
//        d.setOnClickListener(l);

//        mViewSkill.setText(String.valueOf(mSkill));
        mWord.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (direction == 0) {
                    readWord(v);
                }
                return true;
            }
        });
        a.setFocusable(false);
        b.setFocusable(false);
        c.setFocusable(false);
        d.setFocusable(false);


        return v;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnSwipeTouchListener onSwipeTouchListener = new OnSwipeTouchListener(getActivity(), false) {
//            public void onSwipeTop() {
//                Toast.makeText(getActivity(), "top", Toast.LENGTH_SHORT).show();
//            }
//
//            public void onSwipeRight() {
//                Toast.makeText(getActivity(), "right", Toast.LENGTH_SHORT).show();
//            }

            @Override
            public boolean onViewDown(View v) {
                return view.equals(v);
            }

            @Override
            public void onClick(View v) {
                if (!(v instanceof Button)) return;
                if (v.equals(dontKnow)) {
                    onSwipe();
                    return;
                }
                answer(v);
            }

            @Override
            public void onLongClick(View v) {
                if (!(v instanceof Button)) return;
                if (v.equals(dontKnow)) return;
                if (direction == 1) {
                    readWord(v);
                }
            }

            @Override
            public void onSwipe() {
                highlightCorrect();
                makeButtonsUnclickable();

                if (repeatingIncorrect) {
                    answerIncorrect(null);
                    return;
                }

                boolean correct;
                correct = false;
                float newWordDiff = 0f;
                MainActivity.sIncorrect++;
                saveIncorrect();
                if (!repeating) {


                    float coefficientUser = 1 / (1 + 0.05f * (MainActivity.sCorrect + MainActivity.sIncorrect));
                    float coefficientWord = 1 / (1 + 0.05f * (mCurrentWord.getLearnedCount()));
                    float chanceUser = 1 / (1 + (float) Math.exp(-(mSkill - mCurrentWord.getDifficulty())));
                    float chanceWord = 1 / (1 + (float) Math.exp(-(mCurrentWord.getDifficulty() - mSkill)));

                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
                    updateDbNewWord(newWordDiff, correct);
//                    //update
//                    ContentValues cv = new ContentValues();
//                    cv.put(WordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);//++
//                    cv.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, mCurrentWord.getLearnedCount() + 1);//++
//                    cv.put(WordEntry.COLUMN_NAME_LEARNED, 1);
//                    mWordsDb.update(WordEntry.TABLE_NAME, cv, WordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});
//                    //todo save string for upload ↑
////            mPreferences.edit().putString(mWord.getText().toString(), mWord.getText().toString()).apply();
//
//                    //save to learned
//                    cv = new ContentValues();
//                    cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mCurrentWord.getWord());
////            cv.put(LearnedWordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);
//                    cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + INTERVAL);
//                    cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, INTERVAL);
//                    cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mCurrentWord.getPronunciation());
//                    cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, arrayToString(mCurrentWord.getTranslations()));
//                    cv.put(LearnedWordEntry.COLUMN_NAME_CATEGORIES, arrayToString(mCurrentWord.getCategories()));
//                    mWordsDb.insert(LearnedWordEntry.TABLE_NAME, null, cv);
                } else {
                    updateDbRepeating(correct);

//                    highlightCorrect();
//                    makeButtonsUnclickable();
//                    boolean correct;
//                    mWords.clear();
//                    correct = false;
//                    MainActivity.sIncorrect++;
//                    getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
//
//                    updateDbRepeating(correct);
//                    ContentValues cv = new ContentValues();
////                cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mWord.getText().toString());
//                    cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1));
//                    cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1) -
//                            getTimeSeconds());
////                cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mPron.getText().toString());
////                cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, ((Button) v).getText().toString());
//                    mWordsDb.update(LearnedWordEntry.TABLE_NAME, cv, LearnedWordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});
                }
                results.add(new Pair<>(mCurrentWord, false));
                SharedPreferences resultsPref = getActivity().getSharedPreferences("results", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = resultsPref.edit();
                for (String s : mCurrentWord.getHumanCategories()) {
                    ed.putInt(s + "_incorrect", resultsPref.getInt(s + "_incorrect", 0) + 1);
                    Log.i("resultscat learn", s + "_incorrect" + " " + resultsPref.getInt(s + "_incorrect", 0) + 1);
                }
                ed.commit();
                if (MainActivity.sIncorrect + MainActivity.sCorrect > 10) {
                    saveToPost(correct, selectedButton, repeating ? 0 : newWordDiff - mCurrentWord.getDifficulty());
                }

                mWords.clear();

                ClickToContinueDialog.newInstance(correct).show(getFragmentManager(), "incorrect_dialog");
            }

            public boolean onTouch(View v, MotionEvent event) {
                if (v instanceof Button) {
                    setView(v);
                } else {
                    setView(view);
                }
                return gestureDetector.onTouchEvent(event);
            }
        };
//        OnSwipeTouchListener forView = new OnSwipeTouchListener(getActivity(), true) {
//            @Override
//            public boolean onViewDown(View v) {
//                return false;
//            }
//
//            public void onSwipe() {
//                highlightCorrect();
//                makeButtonsUnclickable();
//                boolean correct;
//                mWords.clear();
//                correct = false;
//                getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
//                MainActivity.sIncorrect++;
//                if (!repeating) {
//                    float coefficientUser = 0.1f;//1 / (1 + 0.05f * (MainActivity.sCorrect + MainActivity.sIncorrect));
//                    float coefficientWord = 0.1f;// 1 / (1 + 0.05f * (pocetOdpovediNaTotoSlovoCelkovo));
//                    float chanceUser = 1 / (1 + (float) Math.exp(-(mSkill - mCurrentWord.getDifficulty())));
//                    float chanceWord = 1 / (1 + (float) Math.exp(-(mCurrentWord.getDifficulty() - mSkill)));
//                    float newWordDiff;
//
//
//                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
//                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
//
//                    //update
//                    ContentValues cv = new ContentValues();
//                    cv.put(WordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);//++
//                    cv.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, mCurrentWord.getLearnedCount() + 1);//++
//                    cv.put(WordEntry.COLUMN_NAME_LEARNED, 1);
//                    mWordsDb.update(WordEntry.TABLE_NAME, cv, WordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});
//                    //todo save string for upload ↑
////            mPreferences.edit().putString(mWord.getText().toString(), mWord.getText().toString()).apply();
//
//                    //save to learned
//                    cv = new ContentValues();
//                    cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mCurrentWord.getWord());
////            cv.put(LearnedWordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);
//                    cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + INTERVAL);
//                    cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, INTERVAL);
//                    cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mCurrentWord.getPronunciation());
//                    cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, arrayToString(mCurrentWord.getTranslations()));
//                    cv.put(LearnedWordEntry.COLUMN_NAME_CATEGORIES, arrayToString(mCurrentWord.getCategories()));
//                    mWordsDb.insert(LearnedWordEntry.TABLE_NAME, null, cv);
//                } else {
//
//
//                    ContentValues cv = new ContentValues();
////                cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mWord.getText().toString());
//                    cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1));
//                    cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1) -
//                            getTimeSeconds());
////                cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mPron.getText().toString());
////                cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, ((Button) v).getText().toString());
//                    mWordsDb.update(LearnedWordEntry.TABLE_NAME, cv, LearnedWordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});
//                }
//
////                mHandler.postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
////                        next();
////
////                    }
////                }, 1000);
//                new IncorrectDialogFragment().show(getFragmentManager(), "incorrect_dialog");
//            }
//
//            public boolean onTouch(View v, MotionEvent event) {
//                return gestureDetector.onTouchEvent(event);
//            }
//        };
        view.setOnTouchListener(onSwipeTouchListener);
        a.setOnTouchListener(onSwipeTouchListener);
        b.setOnTouchListener(onSwipeTouchListener);
        c.setOnTouchListener(onSwipeTouchListener);
        d.setOnTouchListener(onSwipeTouchListener);
        dontKnow.setOnTouchListener(onSwipeTouchListener);
        next();
    }

//    #F44336 red
//    #8BC34A green

    /**
     * Fill screen with new data. If there is word for repeating or using new word and distractors
     */
    public void next() {
        if (count == currentCount) {
            if (hasIncorrect()) {
                repeatingIncorrect = true;
                loadIncorrect();
            } else {
                endSession(true);
            }
            return;
        }
        a.setBackground(getResources().getDrawable(R.drawable.button));
        b.setBackground(getResources().getDrawable(R.drawable.button));
        c.setBackground(getResources().getDrawable(R.drawable.button));
        d.setBackground(getResources().getDrawable(R.drawable.button));
        a.setClickable(true);
        b.setClickable(true);
        c.setClickable(true);
        d.setClickable(true);
        dontKnow.setClickable(true);
        a.setPressed(false);
        b.setPressed(false);
        c.setPressed(false);
        d.setPressed(false);
        dontKnow.setPressed(false);
        mCurrentWord = new Word();

        if (!checkToRepeat()) {
            getNewWord();
        }

//        getDistractorsSimilarDifficulty();
//        getDistractorsSimilarDifficultySimilarCategory();
//        getDistractorsSimilarDifficultySameCategoryClosestLevenshtein();
//        getDistractorsSimilarDifficultyClosestLevenshtein();
//        getDistractorsSimilarDifficultySameCategory();
//        getDistractorsSameCategory();
        getDistractorsFinal();
        mWords.add(mCurrentWord);
        direction = getDirection();
        if (direction == 0) { //direction en-cz
            mWord.setText(mCurrentWord.getWord());
            mPron.setText(mCurrentWord.getPronunciation());
            Collections.shuffle(mWords);
            a.setText(mWords.get(0).getTranslations()[0]);
            b.setText(mWords.get(1).getTranslations()[0]);
            c.setText(mWords.get(2).getTranslations()[0]);
            d.setText(mWords.get(3).getTranslations()[0]);
        } else { // direction cz-en
            mWord.setText(mCurrentWord.getTranslations()[0]);
            mPron.setText("");
            Collections.shuffle(mWords);
            a.setText(mWords.get(0).getWord());
            b.setText(mWords.get(1).getWord());
            c.setText(mWords.get(2).getWord());
            d.setText(mWords.get(3).getWord());
        }
        if (direction == 0) {
            if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getBoolean("pref_sounds", true)) {
                readWord(mWord);
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor ed = mPreferences.edit();
        ed.putFloat("skill", mSkill);
        ed.putInt("correct", MainActivity.sCorrect);
        ed.putInt("incorrect", MainActivity.sIncorrect);
        ed.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWordsDb.close();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

    }

    /**
     * Listener for checking if user answered correctly and updating database
     */
  /*  private class OnButtonClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            highlightCorrect();
            makeButtonsUnclickable();
            boolean correct;
            mWords.clear();

            if (!repeating) {
                float coefficientUser = 0.1f;//1 / (1 + 0.05f * (MainActivity.sCorrect + MainActivity.sIncorrect));
                float coefficientWord = 0.1f;// 1 / (1 + 0.05f * (pocetOdpovediNaTotoSlovoCelkovo));
                float chanceUser = 1 / (1 + (float) Math.exp(-(mSkill - mCurrentWord.getDifficulty())));
                float chanceWord = 1 / (1 + (float) Math.exp(-(mCurrentWord.getDifficulty() - mSkill)));
                float newWordDiff;

                if (direction == 0) {
                    if (!((Button) v).getText().equals(mCurrentWord.getTranslations())) {
                        correct = false;
                        MainActivity.sIncorrect++;
                        getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                        mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
                        newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
                    } else {
                        MainActivity.sCorrect++;
                        correct = true;
                        getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));
                        mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (1 - chanceUser));
                        newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (0 - chanceWord));
                    }
                } else {
                    if (!((Button) v).getText().equals(mCurrentWord.getWord())) {
                        correct = false;
                        getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                        MainActivity.sIncorrect++;

                        mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
                        newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
                    } else {
                        MainActivity.sCorrect++;
                        correct = true;
                        getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));
                        mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (1 - chanceUser));
                        newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (0 - chanceWord));
                    }
                }
                //update
                ContentValues cv = new ContentValues();
                cv.put(WordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);//++
                cv.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, mCurrentWord.getLearnedCount() + 1);//++
                cv.put(WordEntry.COLUMN_NAME_LEARNED, 1);
                mWordsDb.update(WordEntry.TABLE_NAME, cv, WordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});
                //todo save string for upload ↑
//            mPreferences.edit().putString(mWord.getText().toString(), mWord.getText().toString()).apply();

                //save to learned
                cv = new ContentValues();
                cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mCurrentWord.getWord());
//            cv.put(LearnedWordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);
                cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + INTERVAL);
                cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, INTERVAL);
                cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mCurrentWord.getPronunciation());
                cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, mCurrentWord.getTranslations());
                mWordsDb.insert(LearnedWordEntry.TABLE_NAME, null, cv);
            } else {
                if (direction == 1) {
                    if (!((Button) v).getText().equals(mCurrentWord.getWord())) {
                        correct = false;
                        MainActivity.sIncorrect++;
                        getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                    } else {
                        MainActivity.sCorrect++;
                        correct = true;
                        getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));

                    }
                } else {
                    if (!((Button) v).getText().equals(mCurrentWord.getTranslations())) {
                        correct = false;
                        MainActivity.sIncorrect++;
                        getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                    } else {
                        MainActivity.sCorrect++;
                        correct = true;
                        getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));
                    }
                }
                ContentValues cv = new ContentValues();
//                cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mWord.getText().toString());
                cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1));
                cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1) -
                        getTimeSeconds());
//                cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mPron.getText().toString());
//                cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, ((Button) v).getText().toString());
                mWordsDb.update(LearnedWordEntry.TABLE_NAME, cv, LearnedWordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});


            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    next();

                }
            }, 1000);
        }

    }
    */
    public void answer(View v) {

        highlightCorrect();
        makeButtonsUnclickable();

        if (repeatingIncorrect) {
            answerIncorrect(v);
            return;
        }

        boolean correct;
        float newWordDiff = 0f;
        if (!repeating) {
            float coefficientUser = 1 / (1 + 0.05f * (MainActivity.sCorrect + MainActivity.sIncorrect));
            float coefficientWord = 1 / (1 + 0.05f * (mCurrentWord.getLearnedCount()));
            float chanceUser = 1 / (1 + (float) Math.exp(-(mSkill - mCurrentWord.getDifficulty())));
            float chanceWord = 1 / (1 + (float) Math.exp(-(mCurrentWord.getDifficulty() - mSkill)));


            if (direction == 0) {
                if (!((Button) v).getText().equals(mCurrentWord.getTranslations()[0])) {
                    correct = false;
                    MainActivity.sIncorrect++;
                    saveIncorrect();
                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
                } else {
                    MainActivity.sCorrect++;

                    ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", progress.getProgress() + 100);
                    animation.setDuration(500); // 0.5 second
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();


//                    progress.incrementProgressBy(1);
                    correct = true;
                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (1 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (0 - chanceWord));
                }
            } else {
                if (!((Button) v).getText().equals(mCurrentWord.getWord())) {
                    correct = false;
                    MainActivity.sIncorrect++;
                    saveIncorrect();
                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
                } else {
                    MainActivity.sCorrect++;
//                    progress.incrementProgressBy(1);

                    ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", progress.getProgress() + 100);
                    animation.setDuration(500); // 0.5 second
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();

                    correct = true;
                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (1 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (0 - chanceWord));
                }
            }
            updateDbNewWord(newWordDiff, correct);
            //update
//            ContentValues cv = new ContentValues();
//            cv.put(WordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);//++
//            cv.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, mCurrentWord.getLearnedCount() + 1);//++
//            cv.put(WordEntry.COLUMN_NAME_LEARNED, 1);
//            mWordsDb.update(WordEntry.TABLE_NAME, cv, WordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});
//            //todo save string for upload ↑
////            mPreferences.edit().putString(mWord.getText().toString(), mWord.getText().toString()).apply();
//
//            //save to learned
//            cv = new ContentValues();
//            cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mCurrentWord.getWord());
////            cv.put(LearnedWordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);
//            cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + INTERVAL);
//            cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, INTERVAL);
//            cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mCurrentWord.getPronunciation());
//            cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, arrayToString(mCurrentWord.getTranslations()));
//            cv.put(LearnedWordEntry.COLUMN_NAME_CATEGORIES, arrayToString(mCurrentWord.getCategories()));
//            mWordsDb.insert(LearnedWordEntry.TABLE_NAME, null, cv);
        } else {
            if (direction == 1) {
                if (!((Button) v).getText().equals(mCurrentWord.getWord())) {
                    correct = false;
                    MainActivity.sIncorrect++;
                    saveIncorrect();
//                    getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                } else {
                    MainActivity.sCorrect++;
                    correct = true;
//                    progress.incrementProgressBy(1);
                    ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", progress.getProgress() + 100);
                    animation.setDuration(500); // 0.5 second
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();

//                    getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));

                }
            } else {
                if (!((Button) v).getText().equals(mCurrentWord.getTranslations()[0])) {
                    correct = false;
                    MainActivity.sIncorrect++;
                    saveIncorrect();
//                    getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                } else {
                    MainActivity.sCorrect++;
//                    progress.incrementProgressBy(1);
                    ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", progress.getProgress() + 100);
                    animation.setDuration(500); // 0.5 second
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();

                    correct = true;
//                    getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));
                }
            }

            updateDbRepeating(correct);
//            ContentValues cv = new ContentValues();
////                cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mWord.getText().toString());
//            cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1));
//            cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1) -
//                    getTimeSeconds());
////                cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mPron.getText().toString());
////                cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, ((Button) v).getText().toString());
//            mWordsDb.update(LearnedWordEntry.TABLE_NAME, cv, LearnedWordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});


        }
        results.add(new Pair<>(mCurrentWord, correct));
        SharedPreferences resultsPref = getActivity().getSharedPreferences("results", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = resultsPref.edit();
        for (String s : mCurrentWord.getHumanCategories()) {
            if (correct) {
                ed.putInt(s + "_correct", resultsPref.getInt(s + "_correct", 0) + 1);
            } else {
                ed.putInt(s + "_incorrect", resultsPref.getInt(s + "_incorrect", 0) + 1);
            }
        }
//        if (direction == 1) {
//            if (PreferenceManager.getDefaultSharedPreferences(getActivity())
//                    .getBoolean("pref_sounds", true)) {
//                readWord(v);
//            }
//        }
        ed.apply();
        if (MainActivity.sIncorrect + MainActivity.sCorrect > 10) {
            saveToPost(correct, selectedButton, repeating ? 0 : newWordDiff - mCurrentWord.getDifficulty());
        }
        mWords.clear();
        if (correct) {
            final ClickToContinueDialog idf = ClickToContinueDialog.newInstance(correct);
            idf.show(getFragmentManager(), "incorrect_dialog");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    next();
                    idf.dismiss();
                }
            }, 1000);
        } else {
            v.setBackground(getResources().getDrawable(R.drawable.button_incorrect_clicked));
            ClickToContinueDialog.newInstance(correct).show(getFragmentManager(), "incorrect_dialog");
        }
    }

    public void updateDbNewWord(final float newWordDiff, final boolean correct) {
        try {

            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }

            //update
            ContentValues cv = new ContentValues();
            cv.put(WordEntry.COLUMN_NAME_LEARNED, 1);
            cv.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, mCurrentWord.getLearnedCount() + 1);//++


            cv.put(WordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);//++
            //todo save string for upload ↑
//            try {
//                SharedPreferences sp = getActivity().getSharedPreferences("post", Context.MODE_PRIVATE);
//                JSONArray ja = new JSONArray(sp.getString("post", "[]"));
//                JSONObject upload = new JSONObject();
//                upload.put("english", mCurrentWord.getWord());
//                upload.put("difficulty", newWordDiff - mCurrentWord.getDifficulty());
//                ja.put(upload);
//                Log.i("learning", ja.toString());
//                SharedPreferences.Editor ed = sp.edit();
//                ed.putString("post", ja.toString());
//                ed.apply();
//            } catch (JSONException e) {
//                Log.e("learning", "json exception saving string to post. " + e.getLocalizedMessage());
//                e.printStackTrace();
//            }

            mWordsDb.update(WordEntry.TABLE_NAME, cv, WordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});

//            mPreferences.edit().putString(mWord.getText().toString(), mWord.getText().toString()).apply();

            //save to learned
            cv = new ContentValues();
            cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mCurrentWord.getWord());
            Random r = new Random(System.nanoTime());
            long ttr = (int) (INTERVAL * (r.nextFloat() * (1.1 - 0.9) + 0.9));
            cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + ttr);
            cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, ttr);
            cv.put(LearnedWordEntry.COLUMN_NAME_COEFFICIENT_DIFF, DEFAULT_WORD_COEFFICIENT);
            cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mCurrentWord.getPronunciation());
            cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, arrayToString(mCurrentWord.getTranslations()));
            cv.put(LearnedWordEntry.COLUMN_NAME_CATEGORIES, arrayToString(mCurrentWord.getCategories()));
            cv.put(LearnedWordEntry.COLUMN_NAME_HUMAN_CATEGORIES, arrayToString(mCurrentWord.getHumanCategories()));
            cv.put(LearnedWordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff); //todo added diff to learned words
            if (correct) {
                mCurrentWord.incrementCorrect();
                cv.put(LearnedWordEntry.COLUMN_NAME_CORRECT_COUNT, mCurrentWord.getCorrectAnswers());
            } else {
                mCurrentWord.incrementIncorrect();
                cv.put(LearnedWordEntry.COLUMN_NAME_INCORRECT_COUNT, mCurrentWord.getIncorrectAnswers());
            }
            mWordsDb.insert(LearnedWordEntry.TABLE_NAME, null, cv);
        } catch (SQLiteDatabaseLockedException e) {
//            Log.i("qwer", "exception");
//            pd = new ProgressDialog(getActivity());
//            pd.setMessage("Prosím čekejte, probíha aktualizace databáze.");
//            pd.setCanceledOnTouchOutside(false);
//            pd.show();
//            Handler h = new Handler();
//            h.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    updateDbNewWord(newWordDiff, correct);
//                    Log.i("qwer", " 5 sekund ");
//                }
//            }, 5000);
        }
    }

    /**
     * Updates word when it is repeated.
     *
     * @param correct Whether answer was correct or not
     */
    public void updateDbRepeating(boolean correct) {
        try {
            ContentValues cv = new ContentValues();
            long ttr = computeRepeatTime(correct);
            cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, ttr);
            cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, ttr - getTimeSeconds());
            if (correct) {
                mCurrentWord.incrementCorrect();
                cv.put(LearnedWordEntry.COLUMN_NAME_CORRECT_COUNT, mCurrentWord.getCorrectAnswers());
                float newCoeff = mCurrentWord.getDiffCoefficient() + 0.1f; //changed from +0.59
                cv.put(LearnedWordEntry.COLUMN_NAME_COEFFICIENT_DIFF, newCoeff > DEFAULT_WORD_COEFFICIENT
                        ? DEFAULT_WORD_COEFFICIENT : newCoeff);

            } else {
                mCurrentWord.incrementIncorrect();
                cv.put(LearnedWordEntry.COLUMN_NAME_INCORRECT_COUNT, mCurrentWord.getIncorrectAnswers());
                float newCoeff = mCurrentWord.getDiffCoefficient() - 0.54f; //changed from -0.5204
                cv.put(LearnedWordEntry.COLUMN_NAME_COEFFICIENT_DIFF, newCoeff < 1.3 ? 1.3 : newCoeff);
            }
            mWordsDb.update(LearnedWordEntry.TABLE_NAME, cv, LearnedWordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});
        } catch (SQLiteDatabaseLockedException e) {

        }
    }


    /**
     * Computes when current word should be reviewed.
     *
     * @param correct true if user answered correctly
     * @return time in seconds when current word should be reviewed
     * For debug purposes returns current time + 10 seconds
     */
    public long computeRepeatTime(boolean correct/*, int count, int mozno*/) {
//        return getTimeSeconds() + 10;
        if (!correct) return getTimeSeconds() + INTERVAL;
        Random r = new Random(System.nanoTime());
        if (mCurrentWord.getLastInterval() <= INTERVAL * 1.1) {
            return getTimeSeconds() + (int) (INTERVAL * 6 * (r.nextFloat() * (1.1 - 0.9) + 0.9));
        } else {
            return getTimeSeconds() + (int) (mCurrentWord.getLastInterval() * mCurrentWord.getDiffCoefficient()
                    * (r.nextFloat() * (1.1 - 0.9) + 0.9));
        }
    }

    /**
     * Checks if there is some word that should be reviewed
     *
     * @return true if there is word that should be reviewed, false otherwise
     */
    public boolean checkToRepeat() {
        if (reviseCount == count) {
            repeating = false;
            return false;
        }
//        Cursor cur = mWordsDb.rawQuery("SELECT * FROM " + LearnedWordEntry.TABLE_NAME + " WHERE " +
//                LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " < " + getTimeSeconds()
//                + " ORDER BY " + LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " ASC LIMIT 1", null);
        // order by diff
        Cursor cur = mWordsDb.rawQuery("SELECT * FROM " + LearnedWordEntry.TABLE_NAME + " WHERE " +
                LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " < " + getTimeSeconds()
                + " ORDER BY " + LearnedWordEntry.COLUMN_NAME_DIFFICULTY + " DESC LIMIT 1", null);
        //order by ratio correct-incorrect
//        Cursor cur = mWordsDb.rawQuery("SELECT * FROM " + LearnedWordEntry.TABLE_NAME + " WHERE " +
//                LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " < " + getTimeSeconds()
//                + " ORDER BY (" + LearnedWordEntry.COLUMN_NAME_INCORRECT_COUNT
//                + " - " + LearnedWordEntry.COLUMN_NAME_CORRECT_COUNT + ") DESC LIMIT 1", null);

        //order custom function first?
//        Cursor cur = mWordsDb.rawQuery("SELECT * FROM " + LearnedWordEntry.TABLE_NAME + " WHERE " +
//                LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " < " + getTimeSeconds()
//                + " ORDER BY (" + LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " * (1 / "
//                + LearnedWordEntry.COLUMN_NAME_DIFFICULTY + ")) DESC LIMIT 1", null); //todo custom function?
        if (cur.moveToFirst()) {
            repeating = true;
//            wordToRepeat = new Word();
            mCurrentWord.setWord(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_WORD)));
            mCurrentWord.setTranslations(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS)).split(";"));
            mCurrentWord.setPronunciation(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION)));
            mCurrentWord.setLastInterval(cur.getInt(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL)));
            mCurrentWord.setCategories(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_CATEGORIES)).split(";"));
            mCurrentWord.setHumanCategories(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_HUMAN_CATEGORIES)).split(";"));
            mCurrentWord.setIncorrectAnswers(cur.getInt(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_INCORRECT_COUNT)));
            mCurrentWord.setCorrectAnswers(cur.getInt(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_CORRECT_COUNT)));
            mCurrentWord.setDiffCoefficient(cur.getFloat(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_COEFFICIENT_DIFF)));
            cur.close();
            progress.setMax(progress.getMax() + 100);
            reviseCount++;
            return true;
        } else {
            repeating = false;
            cur.close();
            return false;
        }
    }

    /**
     * Gets new word for user. Only when there's no word for reviewing
     */
    public void getNewWord() {
        repeating = false;
        Cursor currentCursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME + " WHERE " +
                WordEntry.COLUMN_NAME_LEARNED + " = 0 ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY +
                " - " + mSkill + ") LIMIT 20", null);
        Random r = new Random(System.nanoTime());

        if (currentCursor.moveToPosition(r.nextInt(20))) {
            mCurrentWord.setWord(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_WORD)));
            mCurrentWord.setTranslations(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_TRANSLATIONS)).split(";"));
            mCurrentWord.setPronunciation(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_PRONUNCIATION)));
            mCurrentWord.setDifficulty(currentCursor.getFloat(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_DIFFICULTY)));
            mCurrentWord.setCategories(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_CATEGORIES)).split(";"));
            mCurrentWord.setHumanCategories(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_HUMAN_CATEGORIES)).split(";"));
            mCurrentWord.setLearnedCount(currentCursor.getInt(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED_COUNT)));
            currentCount++;
        }
        currentCursor.close();
    }

    /**
     * Initializes text to speech engine
     *
     * @param status status of text to speech engine
     */
    @Override
    public void onInit(int status) {
        Log.i("zxcv", "on init");
        if (status != TextToSpeech.ERROR) {
            tts.setLanguage(Locale.ENGLISH);
        }
    }

    /**
     * Randomly selects direction of translation: cz-en || en-cz
     *
     * @return 0 for en-cz, 1 for cz-en
     */
    public int getDirection() {
        if (fromNotification) {
            fromNotification = false;
            return 0;
        }
        Random r = new Random(System.nanoTime());
        return r.nextInt(2);
    }

    /**
     * Gets current time in seconds
     *
     * @return current time in seconds
     */
    public long getTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }


    private void makeButtonsUnclickable() {
        a.setClickable(false);
        b.setClickable(false);
        c.setClickable(false);
        d.setClickable(false);
        dontKnow.setClickable(false);
    }

    /**
     * Gets 3 distractors based on similar difficulty and users skill
     */
    public void getDistractorsSimilarDifficulty() {
        float magic = 0.1f;
        Cursor cursor;
        do {
            /*cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME + " WHERE " +
                    WordEntry.COLUMN_NAME_DIFFICULTY + " BETWEEN " + (mSkill - magic) + " AND " +
                    (mSkill + magic) + " ORDER BY " + WordEntry.COLUMN_NAME_LEARNED_COUNT +
                    " ASC LIMIT 20", null);*/
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME + " WHERE " +
                    WordEntry.COLUMN_NAME_DIFFICULTY + " BETWEEN " + (mSkill - magic) + " AND " +
                    (mSkill + magic) + " ORDER BY RANDOM() LIMIT 20", null);
            magic += 0.1;

        } while (cursor.getCount() < 10);
//        cursor.moveToFirst();
//        String word = cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_WORD));
//        String pron = cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_PRONUNCIATION));
//        mCurrentWordDiff = cursor.getFloat(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_DIFFICULTY));
//        mCurrentWordLearnedCount = cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED_COUNT));
//
//        String tran = mCurrentWordLearnedCount + "x " + cursor.getString(cursor.getColumnIndexOrThrow(WordEntry
//                .COLUMN_NAME_TRANSLATIONS)).split(";")[0] +
//                " " + String.format("%.2f", mCurrentWordDiff);
//        mWord.setText(word);
//        mPron.setText(pron);
//        mAns.add(tran);
//        translation = tran;
        fromCursorToWords(cursor);
        cursor.close();
        Collections.shuffle(mWords);
        mWords = mWords.subList(0, 3);
    }

    public void getDistractorsSimilarDifficultySimilarCategory() {
        Cursor cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE \"%" + mCurrentWord.getCategories()[0]
                + "%\""
                + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 40", null);
        fromCursorToWords(cursor);
        if (cursor.getCount() < 10) {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 10", null);
        }
        fromCursorToWords(cursor);
        cursor.close();
        Collections.shuffle(mWords);
        mWords = mWords.subList(0, 3);
    }

    public void getDistractorsSimilarDifficultySameCategoryClosestLevenshtein() {
        float magic = 0.1f;
        Cursor cursor;
        do {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE \"%" + mCurrentWord.getCategories()[0]
                    + "%\""
                    + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 40", null);
            magic += 0.1;

        } while (cursor.getCount() < 10);

        fromCursorToWords(cursor);
        if (cursor.getCount() < 10) {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 10", null);
        }
        fromCursorToWords(cursor);

        cursor.close();
        Collections.shuffle(mWords);
        Collections.sort(mWords, new WordLevenshteinComparator());
        mWords = mWords.subList(0, 3);
        Log.i("levenshtein", mWords.get(0).getLevenshteinToCurrent() + " - " + mWords.get(0).getWord() + "\n" +
                mWords.get(1).getLevenshteinToCurrent() + " - " + mWords.get(1).getWord() + "\n" +
                mWords.get(2).getLevenshteinToCurrent() + " - " + mWords.get(2).getWord());
    }


    public void getDistractorsSimilarDifficultyClosestLevenshtein() {
        float magic = 0.1f;
        Cursor cursor;
        do {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 40", null);
            magic += 0.1;

        } while (cursor.getCount() < 10);

        fromCursorToWords(cursor);
        if (cursor.getCount() < 10) {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 10", null);
        }
        fromCursorToWords(cursor);


        cursor.close();

        Collections.sort(mWords, new WordLevenshteinComparator());
        mWords = mWords.subList(0, 3);
        Log.i("levenshtein", mWords.get(0).getLevenshteinToCurrent() + " - " + mWords.get(0).getWord() + "\n" +
                mWords.get(1).getLevenshteinToCurrent() + " - " + mWords.get(1).getWord() + "\n" +
                mWords.get(2).getLevenshteinToCurrent() + " - " + mWords.get(2).getWord());
    }

    public void getDistractorsSimilarDifficultySameCategory() {
        Cursor cursor;
        //presne vsetky kategorie
//        cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
//                + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " = \"" + arrayToString(mCurrentWord.getCategories())
//                + "\" ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 100", null);

        //obsahuje vsetky kategorie (rovnake poradie)
        cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE \"%" + arrayToString(mCurrentWord.getCategories())
                + "%\" ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 40", null);
        fromCursorToWords(cursor);
        if (mWords.size() < 6) {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE \"%" + mCurrentWord.getCategories()[0]
                    + "%\" ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 30", null);
            fromCursorToWords(cursor);
            if (mWords.size() < 4) {
                cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                        + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 20", null);
                fromCursorToWords(cursor);
            }
        }
        cursor.close();
        Collections.shuffle(mWords);
        mWords = mWords.subList(0, 3);
        /*} /*else {
            Toast.makeText(getActivity(), "Error while loading new word. Please restart app", Toast.LENGTH_LONG).show();
        }*/
    }


    public void getDistractorsSameCategory() {
        Cursor cursor;
        //obsahuje vsetky kategorie (rovnake poradie)
        cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE \"%" + arrayToString(mCurrentWord.getCategories())
                + "%\" LIMIT 40", null);
        fromCursorToWords(cursor);
        if (mWords.size() < 6) {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE \"%" + mCurrentWord.getCategories()[0]
                    + "%\" ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 30", null);
            fromCursorToWords(cursor);
            if (mWords.size() < 4) {
                cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                        + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 20", null);
                fromCursorToWords(cursor);
            }
        }
        cursor.close();
        Collections.shuffle(mWords);
        mWords = mWords.subList(0, 3);
    }

    public void getDistractorsFinal() {
        Cursor cursor;
        cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE \"%" + arrayToString(mCurrentWord.getCategories())
                + "%\" ORDER BY RANDOM() LIMIT 40", null);
        fromCursorToWords(cursor);
        if (mWords.size() < 10) {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE \"%" + mCurrentWord.getCategories()[0]
                    + "%\" ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 30", null);
            fromCursorToWords(cursor);
            if (mWords.size() < 8) {
                cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                        + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 20", null);
                fromCursorToWords(cursor);
            }
        }

        cursor.close();
        if (mCurrentWord.getWord().length() < 8) {
            Collections.shuffle(mWords);
            mWords = mWords.subList(0, 3);
        } else {
            Collections.sort(mWords, new WordLevenshteinComparator());
            List<Word> helper = new ArrayList<>(mWords);
            mWords.clear();
            for (Word w : helper) {
                if (w.getLevenshteinToCurrent() < mCurrentWord.getWord().length() / 2) {
                    mWords.add(w);
                }
            }
            helper.removeAll(mWords);
            if (mWords.size() < 3) {
                Collections.shuffle(helper);
                mWords.addAll(helper.subList(0, 3));
            }
            mWords = mWords.subList(0, 3);
        }
    }

    public void fromCursorToWords(Cursor cursor) {
        if (cursor.moveToFirst()) {
            do {
                Word w = new Word();
                w.setWord(cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_WORD)));
                w.setTranslations(cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_TRANSLATIONS)).split(";"));
                if (mWords.contains(w) || w.equals(mCurrentWord)) continue; //?
                boolean contains = false;
                for (int i = 0; i < mCurrentWord.getTranslations().length; i++) {
                    for (int j = 0; j < w.getTranslations().length; j++) {
                        if (mCurrentWord.getTranslations()[i].equals(
                                w.getTranslations()[j])) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) break;
                }
                if (contains) continue;
                w.setIsLearned(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED)));//??
                w.setPronunciation(cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_PRONUNCIATION)));
                w.setDifficulty(cursor.getFloat(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_DIFFICULTY)));
                w.setFrequency(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_FREQUENCY)));
                w.setLearnedCount(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED_COUNT)));
                w.setPercentil(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_PERCENTIL)));
                w.setCategories(cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_CATEGORIES)).split(";"));
                w.setHumanCategories(cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_HUMAN_CATEGORIES)).split(";"));
                w.setLevenshteinToCurrent(levenshteinDistance(mCurrentWord.getWord(), w.getWord()));
                mWords.add(w);
            } while (cursor.moveToNext());
        }

    }


    public void highlightCorrect() {
        String answer = null;
        if (direction == 0) {
            answer = mCurrentWord.getTranslations()[0];
        } else {
            answer = mCurrentWord.getWord();
        }
        if (a.getText().toString().equals(answer)) {
            if (direction == 1) {
                if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("pref_sounds", true)) {
                    readWord(a);
                }
            }
            a.setBackground(getResources().getDrawable(R.drawable.button_correct));
            b.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            c.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            d.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            selectedButton = 1;
        } else if (b.getText().toString().equals(answer)) {
            if (direction == 1) {
                if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("pref_sounds", true)) {
                    readWord(b);
                }
            }
            b.setBackground(getResources().getDrawable(R.drawable.button_correct));
            a.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            c.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            d.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            selectedButton = 2;
        } else if (c.getText().toString().equals(answer)) {
            if (direction == 1) {
                if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("pref_sounds", true)) {
                    readWord(c);
                }
            }
            c.setBackground(getResources().getDrawable(R.drawable.button_correct));
            b.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            a.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            d.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            selectedButton = 3;
        } else if (d.getText().toString().equals(answer)) {
            if (direction == 1) {
                if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("pref_sounds", true)) {
                    readWord(d);
                }
            }
            d.setBackground(getResources().getDrawable(R.drawable.button_correct));
            b.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            c.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            a.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            selectedButton = 4;
        }

    }

    public static class ClickToContinueDialog extends DialogFragment {

        private LearningFragment mFragment;
        private boolean correct;

        public static ClickToContinueDialog newInstance(boolean correct) {
            ClickToContinueDialog ctc = new ClickToContinueDialog();
            Bundle args = new Bundle();
            args.putBoolean("correct", correct);
            ctc.setArguments(args);
            return ctc;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            mFragment.next();
            mFragment = null;
            super.onDismiss(dialog);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                correct = getArguments().getBoolean("correct");
            }

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mFragment = (LearningFragment) getFragmentManager().findFragmentById(R.id.learning_fragment);
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View view = inflater.inflate(R.layout.dialog_click_to_continue, null);

            builder.setView(view);


            final Dialog d = builder.create();
            ProgressBar progress = (ProgressBar) view.findViewById(R.id.tap_to_progress);
            TextView tap = (TextView) view.findViewById(R.id.tap_to_continue);
            if (correct) {
                tap.setVisibility(View.GONE);
                progress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                    }
                });
                ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", 0);
                animation.setDuration(1000);
                animation.setInterpolator(new LinearInterpolator());
                animation.start();
            } else {
                progress.setVisibility(View.GONE);
                tap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                    }
                });
            }

            d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            return d;
        }
    }

    //https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java //Not recursive and faster
    public int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    //todo http://gradleplease.appspot.com/#levenshtein

    public static String arrayToString(String[] array) {
        StringBuilder builder = new StringBuilder();
        String delim = "";
        for (String s : array) {
            builder.append(delim).append(s);
            delim = ";";
        }
        return builder.toString();
    }

    public static <T> void swap(T[] arr, int i, int j) {
        T t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    public TextView getWord() {
        return mWord;
    }

    public void endSession(boolean streak) {
        Results r = new Results();
        r.res = new ArrayList<>(results);
        getFragmentManager().popBackStack();
        Fragment f = ResultsFragment.newInstance(r);
        getFragmentManager().beginTransaction().add(R.id.learning_layout, f, "results").commit();

        if (streak) {
            SharedPreferences streakPref = getActivity().getSharedPreferences("streak", Context.MODE_PRIVATE);
            float daysSinceNow = ((float) System.currentTimeMillis() + TimeZone.getDefault()
                    .getOffset(System.currentTimeMillis())) / DAY_MILISECONDS;
            float daysSinceLast = ((float) streakPref.getLong("last_set", 0L)) / DAY_MILISECONDS;
            int days = (int) daysSinceNow - (int) daysSinceLast;
            if (days == 1) {
                streakPref.edit().putInt("current", streakPref.getInt("current", 0) + 1).commit();
            } else if (streakPref.getInt("current", 0) == 0) {
                streakPref.edit().putInt("current", 1).commit();
            }

            if (streakPref.getInt("current", 0) > streakPref.getInt("record", 0)) {
                streakPref.edit().putInt("record", streakPref.getInt("current", 0)).commit();
            }
            streakPref.edit().putLong("last_set", System.currentTimeMillis() + TimeZone.getDefault()
                    .getOffset(System.currentTimeMillis())).apply();
        }
        results.clear();
    }

    public void saveToPost(boolean correct, int selected, float diffChange) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("current", mCurrentWord.getWord());
            jo.put("correct", correct);
            jo.put("distractors", mWords.get(0).getWord() + ";" + mWords.get(1).getWord() + ";"
                    + mWords.get(2).getWord() + ";" + mWords.get(3).getWord());
            jo.put("selected", selected);
            jo.put("time", getTimeSeconds());
            jo.put("skill", mSkill);
            jo.put("diff_change", diffChange);
            SharedPreferences postPref = getActivity().getSharedPreferences("post", Context.MODE_PRIVATE);
            JSONArray resultsArray = new JSONArray(postPref.getString("results", "[]"));
            resultsArray.put(jo);
            postPref.edit().putString("results", resultsArray.toString()).commit();

            selectedButton = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("learningFragment", "json exception while save to post");
        }
    }

    public void saveIncorrect() {
        try {
            JSONObject jo = new JSONObject();
            jo.put("word", mCurrentWord.getWord());
            jo.put("pronunciation", mCurrentWord.getPronunciation());
            jo.put("direction", direction);
            jo.put("distractors", mWords.get(0).getWord() + ";" + mWords.get(1).getWord() + ";"
                    + mWords.get(2).getWord() + ";" + mWords.get(3).getWord());
            IncorrectQuestion question = new IncorrectQuestion();
            question.setCurrentWord(mCurrentWord);
            question.setDirection(direction);
            question.setDistractors(new ArrayList<>(mWords));
            incorrectList.add(question);
//            incorrectList.add(mCurrentWord);
//            incorrectList.addAll(mWords);
            incorrect.put(jo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadIncorrect() {
        a.setBackground(getResources().getDrawable(R.drawable.button));
        b.setBackground(getResources().getDrawable(R.drawable.button));
        c.setBackground(getResources().getDrawable(R.drawable.button));
        d.setBackground(getResources().getDrawable(R.drawable.button));
        a.setClickable(true);
        b.setClickable(true);
        c.setClickable(true);
        d.setClickable(true);
        dontKnow.setClickable(true);
        a.setPressed(false);
        b.setPressed(false);
        c.setPressed(false);
        d.setPressed(false);
        dontKnow.setPressed(false);

        mCurrentWord = incorrectList.get(0).getCurrentWord();
        direction = incorrectList.get(0).getDirection();
        if (direction == 0) { //direction en-cz
            mWord.setText(incorrectList.get(0).getCurrentWord().getWord());
            mPron.setText(incorrectList.get(0).getCurrentWord().getPronunciation());

            Collections.shuffle(incorrectList.get(0).getDistractors());
            a.setText(incorrectList.get(0).getDistractors().get(0).getTranslations()[0]);
            b.setText(incorrectList.get(0).getDistractors().get(1).getTranslations()[0]);
            c.setText(incorrectList.get(0).getDistractors().get(2).getTranslations()[0]);
            d.setText(incorrectList.get(0).getDistractors().get(3).getTranslations()[0]);
        } else { // direction cz-en
            mWord.setText(incorrectList.get(0).getCurrentWord().getTranslations()[0]);
            mPron.setText("");
            Collections.shuffle(incorrectList.get(0).getDistractors());
            a.setText(incorrectList.get(0).getDistractors().get(0).getWord());
            b.setText(incorrectList.get(0).getDistractors().get(1).getWord());
            c.setText(incorrectList.get(0).getDistractors().get(2).getWord());
            d.setText(incorrectList.get(0).getDistractors().get(3).getWord());
        }
//        incorrect.remove(0);// json?
        mWords.addAll(incorrectList.get(0).getDistractors());
        incorrectList.remove(0);


    }

    public boolean hasIncorrect() {
        return !incorrectList.isEmpty();
    }

    public void answerIncorrect(View v) {
        boolean correct;

        if (v == null) {
            correct = false;
            saveIncorrect();
        } else {
            if (direction == 0) {
                if (!((Button) v).getText().equals(mCurrentWord.getTranslations()[0])) {
                    correct = false;
                    saveIncorrect();
                } else {
//                    progress.incrementProgressBy(1);
                    ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", progress.getProgress() + 100);
                    animation.setDuration(500); // 0.5 second
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();

                    correct = true;
                }
            } else {
                if (!((Button) v).getText().equals(mCurrentWord.getWord())) {
                    correct = false;
                    saveIncorrect();
                } else {
//                    progress.incrementProgressBy(1);
                    ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", progress.getProgress() + 100);
                    animation.setDuration(500); // 0.5 second
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();

                    correct = true;
                }
            }
        }
        mWords.clear();
        if (correct) {
            final ClickToContinueDialog idf = ClickToContinueDialog.newInstance(correct);
            idf.show(getFragmentManager(), "incorrect_dialog");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    next();
                    if (idf != null) {
                        idf.dismiss();
                    }
                }
            }, 1000);
        } else {
            if (v != null) {
                v.setBackground(getResources().getDrawable(R.drawable.button_incorrect_clicked));
            }
            ClickToContinueDialog.newInstance(correct).show(getFragmentManager(), "incorrect_dialog");
        }
    }

    public void readWord(View v) {
        if (Build.VERSION.SDK_INT < 21) {
            tts.speak(((TextView) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        } else {
            tts.speak(((TextView) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "test");
        }


    }

    public boolean showResults() {
        return !results.isEmpty();
    }
}
