package cz.muni.fi.anglictina.fragments;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.activities.MainActivity;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordContract.LearnedWordEntry;
import cz.muni.fi.anglictina.db.WordContract.WordEntry;
import cz.muni.fi.anglictina.db.WordDbHelper;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.utils.OnSwipeTouchListener;
import cz.muni.fi.anglictina.utils.WordLevenshteinComparator;
import cz.muni.fi.anglictina.utils.adapters.TranslationsAdapter;

/**
 * Created by collfi on 27. 10. 2015.
 */
public class LearningFragment extends Fragment implements TextToSpeech.OnInitListener {
    public static final long DAY = 100000000;//60 * 60 * 24;
    private float mSkill;
    private Button a;
    private Button b;
    private Button c;
    private Button d;
    private TextView mWord;
    private TextView mPron;
    private SQLiteDatabase mWordsDb;
    private TextToSpeech tts;
    private TextView mViewSkill;
    private TextView mChance;
    private SharedPreferences mPreferences;
    private List<Word> mWords;
    boolean repeating;
    private Word mCurrentWord;
    private int direction;
    private Handler mHandler;
    private static View longClicked;
//    private OnButtonClickListener l;

    String[] projection = {WordContract.WordEntry.COLUMN_NAME_ID, WordContract.WordEntry.COLUMN_NAME_WORD,
            WordContract.WordEntry.COLUMN_NAME_TRANSLATIONS, WordContract.WordEntry.COLUMN_NAME_PRONUNCIATION,
            WordContract.WordEntry.COLUMN_NAME_FREQUENCY, WordContract.WordEntry.COLUMN_NAME_PERCENTIL};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(getActivity(), this);
        mPreferences = getActivity().getSharedPreferences("stats", Context.MODE_PRIVATE);
        mSkill = mPreferences.getFloat("skill", 0);

        mWords = new ArrayList<>();

        WordDbHelper helper = new WordDbHelper(getActivity());
        mWordsDb = helper.getWritableDatabase();
        mHandler = new Handler();
//        l = new OnButtonClickListener();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_learning, container);

        a = (Button) v.findViewById(R.id.button_a);
        b = (Button) v.findViewById(R.id.button_b);
        c = (Button) v.findViewById(R.id.button_c);
        d = (Button) v.findViewById(R.id.button_d);
        mWord = (TextView) v.findViewById(R.id.word);
        mPron = (TextView) v.findViewById(R.id.pronunciation);
        mViewSkill = (TextView) v.findViewById(R.id.skill);
        mChance = (TextView) v.findViewById(R.id.chance);
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

        mViewSkill.setText(String.valueOf(mSkill));
        mWord.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (direction == 0) {
                    if (Build.VERSION.SDK_INT < 21) {
                        tts.speak(((TextView) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        tts.speak(((TextView) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "test");
                    }
                } else {
                    TranslationsDialogFragment.newInstance(mCurrentWord.getTranslations(), 4).
                            show(getFragmentManager(), "translations");
                }
                return true;
            }
        });
//        mWord.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (Build.VERSION.SDK_INT < 21) {
//                    tts.speak(mWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
//                } else {
//                    tts.speak(mWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "test");
//                }
//            }
//        });
//        View.OnLongClickListener ll = new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (Build.VERSION.SDK_INT < 21) {
//                    tts.speak(((Button) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
//                } else {
//                    tts.speak(((Button) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "test");
//                }
//                return true;
//            }
//        };
//        a.setOnLongClickListener(ll);
//        b.setOnLongClickListener(ll);
//        c.setOnLongClickListener(ll);
//        d.setOnLongClickListener(ll);
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
                answer(v);
            }

            @Override
            public void onLongClick(View v) {
                if (!(v instanceof Button)) return;
                longClicked = v;
                if (direction == 0) {
                    if (v.equals(a)) {
                        TranslationsDialogFragment.newInstance(mWords.get(0).getTranslations(), 0).
                                show(getFragmentManager(), "translations");
                    } else if (v.equals(b)) {
                        TranslationsDialogFragment.newInstance(mWords.get(1).getTranslations(), 1).
                                show(getFragmentManager(), "translations");
                    } else if (v.equals(c)) {
                        TranslationsDialogFragment.newInstance(mWords.get(2).getTranslations(), 2).
                                show(getFragmentManager(), "translations");
                    } else if (v.equals(d)) {
                        TranslationsDialogFragment.newInstance(mWords.get(3).getTranslations(), 3).
                                show(getFragmentManager(), "translations");
                    }
                } else {
                    if (Build.VERSION.SDK_INT < 21) {
                        tts.speak(((Button) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        tts.speak(((Button) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "test");
                    }
                }
            }

            @Override
            public void onSwipeLeft() {
                highlightCorrect();
                makeButtonsUnclickable();
                boolean correct;
                mWords.clear();
                correct = false;
                MainActivity.sIncorrect++;
//                getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));


                if (!repeating) {
                    float newWordDiff;

                    float coefficientUser = /*0.1f;*/1 / (1 + 0.05f * (MainActivity.sCorrect + MainActivity.sIncorrect));
                    float coefficientWord = /*0.1f;*/ 1 / (1 + 0.05f * (mCurrentWord.getLearnedCount()));
                    float chanceUser = 1 / (1 + (float) Math.exp(-(mSkill - mCurrentWord.getDifficulty())));
                    float chanceWord = 1 / (1 + (float) Math.exp(-(mCurrentWord.getDifficulty() - mSkill)));

                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
                    updateDbNewWord(newWordDiff);
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
//                    cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + DAY);
//                    cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, DAY);
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

                new IncorrectDialogFragment().show(getFragmentManager(), "incorrect_dialog");
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
//            public void onSwipeLeft() {
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
//                    cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + DAY);
//                    cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, DAY);
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
        next();
    }

//    #F44336 red
//    #8BC34A green

    /**
     * Fill screen with new data. If there is word for repeating or using new word and distractors
     */
    public void next() {
        a.setTextColor(Color.BLACK);
        b.setTextColor(Color.BLACK);
        c.setTextColor(Color.BLACK);
        d.setTextColor(Color.BLACK);
//        getView().setBackgroundColor(Color.WHITE);
        a.setBackground(getResources().getDrawable(R.drawable.button));
        b.setBackground(getResources().getDrawable(R.drawable.button));
        c.setBackground(getResources().getDrawable(R.drawable.button));
        d.setBackground(getResources().getDrawable(R.drawable.button));
        a.setClickable(true);
        b.setClickable(true);
        c.setClickable(true);
        d.setClickable(true);
        a.setLongClickable(true);
        b.setLongClickable(true);
        c.setLongClickable(true);
        d.setLongClickable(true);
        a.setPressed(false);
        b.setPressed(false);
        c.setPressed(false);
        d.setPressed(false);
//        a.setOnClickListener(l);
//        b.setOnClickListener(l);
//        c.setOnClickListener(l);
//        d.setOnClickListener(l);
        mCurrentWord = new Word();

        if (!checkToRepeat()) {
            getNewWord();
        }

//        getDistractorsSimilarDifficulty();
//        getDistractorsSimilarDifficultySimilarCategory();
//        getDistractorsSimilarDifficultySameCategoryClosestLevenshtein();
        getDistractorsSimilarDifficultyClosestLevenshtein();
//        getDistractorsSimilarDifficultySameCategory();
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
            mPron.setText(mCurrentWord.getTranslations()[0]);
            mPron.setText(mCurrentWord.getPronunciation()); //delete^

            Collections.shuffle(mWords);
            a.setText(mWords.get(0).getWord());
            b.setText(mWords.get(1).getWord());
            c.setText(mWords.get(2).getWord());
            d.setText(mWords.get(3).getWord());
        }

        mViewSkill.setText(String.format("%.5f", mSkill));
        float chance = 1 / (1 + (float) Math.exp(-(mSkill - mCurrentWord.getDifficulty())));
        mChance.setText(String.format("%.2f", chance));


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
                cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + DAY);
                cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, DAY);
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
        boolean correct;
        mWords.clear();

        if (!repeating) {
            float coefficientUser = /*0.1f;*/1 / (1 + 0.05f * (MainActivity.sCorrect + MainActivity.sIncorrect));
            float coefficientWord = /*0.1f;*/ 1 / (1 + 0.05f * (mCurrentWord.getLearnedCount()));
            float chanceUser = 1 / (1 + (float) Math.exp(-(mSkill - mCurrentWord.getDifficulty())));
            float chanceWord = 1 / (1 + (float) Math.exp(-(mCurrentWord.getDifficulty() - mSkill)));
            float newWordDiff;

            if (direction == 0) {
                if (!((Button) v).getText().equals(mCurrentWord.getTranslations()[0])) {
                    correct = false;
                    MainActivity.sIncorrect++;
//                    getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
                } else {
                    MainActivity.sCorrect++;
                    correct = true;
//                    getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));
                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (1 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (0 - chanceWord));
                }
            } else {
                if (!((Button) v).getText().equals(mCurrentWord.getWord())) {
                    correct = false;
//                    getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                    MainActivity.sIncorrect++;

                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (0 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (1 - chanceWord));
                } else {
                    MainActivity.sCorrect++;
                    correct = true;
//                    getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));
                    mSkill = (float) (mSkill + (coefficientUser < 0.1 ? 0.1 : coefficientUser) * (1 - chanceUser));
                    newWordDiff = (float) (mCurrentWord.getDifficulty() + (coefficientWord < 0.1 ? 0.1 : coefficientWord) * (0 - chanceWord));
                }
            }
            updateDbNewWord(newWordDiff);
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
//            cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + DAY);
//            cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, DAY);
//            cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mCurrentWord.getPronunciation());
//            cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, arrayToString(mCurrentWord.getTranslations()));
//            cv.put(LearnedWordEntry.COLUMN_NAME_CATEGORIES, arrayToString(mCurrentWord.getCategories()));
//            mWordsDb.insert(LearnedWordEntry.TABLE_NAME, null, cv);
        } else {
            if (direction == 1) {
                if (!((Button) v).getText().equals(mCurrentWord.getWord())) {
                    correct = false;
                    MainActivity.sIncorrect++;
//                    getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                } else {
                    MainActivity.sCorrect++;
                    correct = true;
//                    getView().setBackgroundColor(getResources().getColor(R.color.greenCorrect));

                }
            } else {
                if (!((Button) v).getText().equals(mCurrentWord.getTranslations()[0])) {
                    correct = false;
                    MainActivity.sIncorrect++;
//                    getView().setBackgroundColor(getResources().getColor(R.color.redIncorrect));
                } else {
                    MainActivity.sCorrect++;
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
        if (correct) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    next();
                }
            }, 1000);
        } else {
            new IncorrectDialogFragment().show(getFragmentManager(), "incorrect_dialog");
        }
    }

    public void updateDbNewWord(float newWordDiff) {
        //update
        ContentValues cv = new ContentValues();
        if (MainActivity.sIncorrect + MainActivity.sCorrect > 10) { //check
            cv.put(WordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);//++
            cv.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, mCurrentWord.getLearnedCount() + 1);//++
            cv.put(WordEntry.COLUMN_NAME_LEARNED, 1);
            mWordsDb.update(WordEntry.TABLE_NAME, cv, WordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});
            //todo save string for upload ↑
            try {
                SharedPreferences sp = getActivity().getSharedPreferences("post", Context.MODE_PRIVATE);
                JSONArray ja = new JSONArray(sp.getString("post", "[]"));
                JSONObject upload = new JSONObject();
                upload.put("english", mCurrentWord.getWord());
                upload.put("difficulty", newWordDiff - mCurrentWord.getDifficulty());
                ja.put(upload);
                Log.i("learning", ja.toString());
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("post", ja.toString());
                ed.apply();
            } catch (JSONException e) {
                Log.e("learning", "json exception saving string to post. " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

//            mPreferences.edit().putString(mWord.getText().toString(), mWord.getText().toString()).apply();

        //save to learned
        cv = new ContentValues();
        cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mCurrentWord.getWord());
//            cv.put(LearnedWordEntry.COLUMN_NAME_DIFFICULTY, newWordDiff);
        cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, getTimeSeconds() + DAY);
        cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, DAY);
        cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mCurrentWord.getPronunciation());
        cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, arrayToString(mCurrentWord.getTranslations()));
        cv.put(LearnedWordEntry.COLUMN_NAME_CATEGORIES, arrayToString(mCurrentWord.getCategories()));
        mWordsDb.insert(LearnedWordEntry.TABLE_NAME, null, cv);
    }

    public void updateDbRepeating(boolean correct) {
        ContentValues cv = new ContentValues();
//                cv.put(LearnedWordEntry.COLUMN_NAME_WORD, mWord.getText().toString());
        cv.put(LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1));
        cv.put(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL, computeRepeatTime(correct, mCurrentWord.getLearnedCount(), 1) -
                getTimeSeconds());
//                cv.put(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION, mPron.getText().toString());
//                cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, ((Button) v).getText().toString());
        mWordsDb.update(LearnedWordEntry.TABLE_NAME, cv, LearnedWordEntry.COLUMN_NAME_WORD + " = ?", new String[]{mCurrentWord.getWord()});

    }


    /**
     * Computes when current word should be reviewed.
     *
     * @param correct true if user answered correctly
     * @param count   how many times user answered to this word
     * @param mozno   placeholder
     * @return time in seconds when current word should be reviewed
     * For debug purposes returns current time + 10 seconds
     */
    public long computeRepeatTime(boolean correct, int count, int mozno) {
        return getTimeSeconds() + 100000000;
//        if (!correct) return getTimeSeconds() + DAY;
//        if (mCurrentWordLastInterval <= DAY) { //*1.1
//            return DAY * 6;
//        } else {
//            long coefficient = 1; //todo mCurrentWord.getLastInterval - 0.8 + 0.28q - 0.02q^2 [1.3, 2.5]
//            return mCurrentWordLastInterval * coefficient; //todo random 0.95-1.05
//        }
    }

    /**
     * Checks if there is some word that should be reviewed
     *
     * @return true if there is word that should be reviewed, false otherwise
     */
    public boolean checkToRepeat() {
        Cursor cur = mWordsDb.rawQuery("SELECT * FROM " + LearnedWordEntry.TABLE_NAME + " WHERE " +
                LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " < " + getTimeSeconds()
                + " ORDER BY " + LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " ASC LIMIT 1", null);
        if (cur.moveToFirst()) {
            repeating = true;
//            wordToRepeat = new Word();
            mCurrentWord.setWord(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_WORD)));
            mCurrentWord.setTranslations(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS)).split(";"));
            mCurrentWord.setPronunciation(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION)));
            mCurrentWord.setLastInterval(cur.getInt(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL)));
//            w1.setDifficulty(cur.getFloat(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_DIFFICULTY)));
            mCurrentWord.setCategories(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_CATEGORIES)).split(";"));
            Log.i("zzzzzz", "check to repeat: " + arrayToString(cur.getString(cur.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_TRANSLATIONS)).split(";")));

            cur.close();
            return true;
        } else {
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
                " - " + mSkill + ") LIMIT 10", null);
        Random r = new Random(System.nanoTime());

        if (currentCursor.moveToPosition(r.nextInt(10))) {

//                    wordToRepeat = new Word();
            mCurrentWord.setWord(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_WORD)));
            //todo uvazovat vsetky preklady
            mCurrentWord.setTranslations(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_TRANSLATIONS)).split(";"));
            mCurrentWord.setPronunciation(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_PRONUNCIATION)));
            mCurrentWord.setDifficulty(currentCursor.getFloat(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_DIFFICULTY)));
            mCurrentWord.setCategories(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_CATEGORIES)).split(";"));
            mCurrentWord.setLearnedCount(currentCursor.getInt(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED_COUNT)));
        }
        Log.i("zzzzzz", "get new word: " + arrayToString(currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_TRANSLATIONS)).split(";")));
        currentCursor.close();
    }

    /**
     * Initializes text to speech engine
     *
     * @param status status of text to speech engine
     */
    @Override
    public void onInit(int status) {
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
        a.setLongClickable(false);
        b.setLongClickable(false);
        c.setLongClickable(false);
        d.setLongClickable(false);
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
        float magic = 0.1f;
        Cursor cursor;

//            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
//                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE '%" + mCurrentWord.getCategories()
//                    + "%' AND " + WordEntry.COLUMN_NAME_DIFFICULTY + " BETWEEN " + (mSkill - magic)
//                    + " AND " + (mSkill + magic)
//                    + " ORDER BY RANDOM() LIMIT 100", null);
        cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE '%" + mCurrentWord.getCategories()[0]
                + "%'"
                + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 100", null);
        magic += 0.1;
        Log.i("dist cur count: ", cursor.getCount() + "");


        fromCursorToWords(cursor);
        cursor.close();
        Collections.shuffle(mWords);
        mWords = mWords.subList(0, 3);
    }

    public void getDistractorsSimilarDifficultySameCategoryClosestLevenshtein() {
        float magic = 0.1f;
        Cursor cursor;
        do {
//            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
//                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE '%" + mCurrentWord.getCategories()
//                    + "%' AND " + WordEntry.COLUMN_NAME_DIFFICULTY + " BETWEEN " + (mSkill - magic)
//                    + " AND " + (mSkill + magic)
//                    + " ORDER BY RANDOM() LIMIT 100", null);
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE '%" + mCurrentWord.getCategories()[0]
                    + "%'"
                    + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 100", null);
            magic += 0.1;

        } while (cursor.getCount() < 10);

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
//            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
//                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE '%" + mCurrentWord.getCategories()
//                    + "%' AND " + WordEntry.COLUMN_NAME_DIFFICULTY + " BETWEEN " + (mSkill - magic)
//                    + " AND " + (mSkill + magic)
//                    + " ORDER BY RANDOM() LIMIT 100", null);
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 100", null);
            magic += 0.1;

        } while (cursor.getCount() < 10);

        fromCursorToWords(cursor);
        cursor.close();
//            Collections.shuffle(mWords);
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
                + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE '%" + arrayToString(mCurrentWord.getCategories())
                + "%' ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 100", null);
        fromCursorToWords(cursor);
        if (mWords.size() < 6) {
            cursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME
                    + " WHERE " + WordEntry.COLUMN_NAME_CATEGORIES + " LIKE '%" + mCurrentWord.getCategories()[0]
                    + "%' ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY + " - " + mSkill + ") LIMIT 100", null);
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
            a.setBackground(getResources().getDrawable(R.drawable.button_correct));
            b.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            c.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            d.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
//            a.setTextColor(getResources().getColor(R.color.greenCorrect));
//            b.setTextColor(getResources().getColor(R.color.redIncorrect));
//            c.setTextColor(getResources().getColor(R.color.redIncorrect));
//            d.setTextColor(getResources().getColor(R.color.redIncorrect));
        } else if (b.getText().toString().equals(answer)) {
            b.setBackground(getResources().getDrawable(R.drawable.button_correct));
            a.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            c.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            d.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
//            b.setTextColor(getResources().getColor(R.color.greenCorrect));
//            a.setTextColor(getResources().getColor(R.color.redIncorrect));
//            c.setTextColor(getResources().getColor(R.color.redIncorrect));
//            d.setTextColor(getResources().getColor(R.color.redIncorrect));
        } else if (c.getText().toString().equals(answer)) {
            c.setBackground(getResources().getDrawable(R.drawable.button_correct));
            b.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            a.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            d.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
//            c.setTextColor(getResources().getColor(R.color.greenCorrect));
//            b.setTextColor(getResources().getColor(R.color.redIncorrect));
//            a.setTextColor(getResources().getColor(R.color.redIncorrect));
//            d.setTextColor(getResources().getColor(R.color.redIncorrect));
        } else if (d.getText().toString().equals(answer)) {
            d.setBackground(getResources().getDrawable(R.drawable.button_correct));
            b.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            c.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
            a.setBackground(getResources().getDrawable(R.drawable.button_incorrect));
//            a.setTextColor(getResources().getColor(R.color.greenCorrect));
//            b.setTextColor(getResources().getColor(R.color.redIncorrect));
//            c.setTextColor(getResources().getColor(R.color.redIncorrect));
//            a.setTextColor(getResources().getColor(R.color.redIncorrect));
        }

    }

    public static class IncorrectDialogFragment extends DialogFragment {

        LearningFragment mFragment;

        @Override
        public void onDismiss(DialogInterface dialog) {
            mFragment.next();
            mFragment = null;
            super.onDismiss(dialog);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mFragment = (LearningFragment) getFragmentManager().findFragmentById(R.id.learning_fragment);
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View view = inflater.inflate(R.layout.dialog_incorrect, null);

            builder.setView(view);


            final Dialog d = builder.create();
            TextView tap = (TextView) view.findViewById(R.id.tap_to_continue);

            tap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
            d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            return d;
        }
    }

    public static class TranslationsDialogFragment extends DialogFragment {

        private ArrayList<String> translations;
        private int buttonPosition;

        public static TranslationsDialogFragment newInstance(String[] t, int position) {
            TranslationsDialogFragment fragment = new TranslationsDialogFragment();
            Bundle args = new Bundle();
            args.putStringArray("translations", t);
            args.putInt("position", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                String[] t = getArguments().getStringArray("translations");
                buttonPosition = getArguments().getInt("position");
                translations = new ArrayList<>();
                Collections.addAll(translations, t);
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
            list.setAdapter(new TranslationsAdapter(getActivity(), translations));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getCount() == 1 || position == 0) {
                        dismiss();
                        return;
                    }
                    LearningFragment fragment = (LearningFragment) getFragmentManager().
                            findFragmentById(R.id.learning_fragment);
                    String word = null;
                    String[] trans = null;
                    if (buttonPosition < 4) {
                        ((Button) longClicked).setText(translations.get(position));
                        swap(fragment.getWords().get(buttonPosition).getTranslations(), 0, position);
                        trans = fragment.getWords().get(buttonPosition).getTranslations();
                        word = fragment.getWords().get(buttonPosition).getWord();
                    } else {
                        fragment.getWord().setText(translations.get(position));
                        swap(fragment.getCurrentWord().getTranslations(), 0, position);
                        trans = fragment.getCurrentWord().getTranslations();
                        word = fragment.getCurrentWord().getWord();
//                        longClicked.postInvalidate();
                    }
                    SQLiteDatabase db = new WordDbHelper(getActivity()).getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    cv.put(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS, arrayToString(
                            trans));
                    db.update(LearnedWordEntry.TABLE_NAME, cv, LearnedWordEntry.COLUMN_NAME_WORD
                            + " = ?", new String[]{word});
                    cv.clear();
                    cv.put(WordEntry.COLUMN_NAME_TRANSLATIONS, arrayToString(
                            trans));
                    db.update(WordEntry.TABLE_NAME, cv, WordEntry.COLUMN_NAME_WORD
                            + " = ?", new String[]{word});

                    db.close();
                    dismiss();
                }
            });
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

    public static final String arrayToString(String[] array) {
        StringBuilder builder = new StringBuilder();
        String delim = "";
        for (String s : array) {
            builder.append(delim).append(s);
            delim = ";";
        }
        return builder.toString();
    }

    public static final <T> void swap(T[] arr, int i, int j) {
        T t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    public List<Word> getWords() {
        return mWords;
    }

    public Word getCurrentWord() {
        return mCurrentWord;
    }

    public TextView getWord() {
        return mWord;
    }
}
