package cz.muni.fi.anglictina.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by collfi on 27. 10. 2015.
 */
public class LearningFragment extends Fragment implements TextToSpeech.OnInitListener {
    public static final long DAY = 10;//60 * 60 * 24;
    private float mSkill;
    //    private float mCurrentWordDiff;
//    private int mCurrentWordLearnedCount;
    //    private int position;
    private Button a;
    private Button b;
    private Button c;
    private Button d;
    private TextView mWord;
    private TextView mPron;
    private SQLiteDatabase mWordsDb;
    //    private String translation;
    private TextToSpeech tts;
    private TextView mViewSkill;
    private TextView mChance;
    private SharedPreferences mPreferences;
    private List<Word> mWords;
    //    private long mCurrentWordLastInterval;
    boolean repeating;
    private Word mCurrentWord;
    private int direction;
    private Handler mHandler;
    private OnButtonClickListener l;

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
        l = new OnButtonClickListener();
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


        a.setOnClickListener(l);
        b.setOnClickListener(l);
        c.setOnClickListener(l);
        d.setOnClickListener(l);

        mViewSkill.setText(String.valueOf(mSkill));
        mWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 21) {
                    tts.speak(mWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    tts.speak(mWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "test");
                }
            }
        });
        View.OnLongClickListener ll = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Build.VERSION.SDK_INT < 21) {
                    tts.speak(((Button) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    tts.speak(((Button) v).getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "test");
                }
                return true;
            }
        };
        a.setOnLongClickListener(ll);
        b.setOnLongClickListener(ll);
        c.setOnLongClickListener(ll);
        d.setOnLongClickListener(ll);


        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        next();
    }
    //    #F44336 red
//    #8BC34A green

    public void next() {
        getView().setBackgroundColor(Color.WHITE);
        a.setClickable(true);
        b.setClickable(true);
        c.setClickable(true);
        d.setClickable(true);

        a.setOnClickListener(l);
        b.setOnClickListener(l);
        c.setOnClickListener(l);
        d.setOnClickListener(l);
        mCurrentWord = new Word();

        if (!checkToRepeat()) {
            getNewWord();
        }





        getDistractorsSimilarDifficulty();


        Collections.shuffle(mWords);
        mWords = mWords.subList(0, 3);
        mWords.add(mCurrentWord);
        direction = getDirection();
        if (direction == 0) { //direction en-cz

            mWord.setText(mCurrentWord.getWord());
            mPron.setText(mCurrentWord.getPronunciation());

            Collections.shuffle(mWords);
            a.setText(mWords.get(0).getTranslations());
            b.setText(mWords.get(1).getTranslations());
            c.setText(mWords.get(2).getTranslations());
            d.setText(mWords.get(3).getTranslations());


        } else { // direction cz-en

            mWord.setText(mCurrentWord.getTranslations());
            mPron.setText(mCurrentWord.getTranslations());

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

    private class OnButtonClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            a.setClickable(false);
            b.setClickable(false);
            c.setClickable(false);
            d.setClickable(false);
            a.setOnClickListener(null);
            b.setOnClickListener(null);
            c.setOnClickListener(null);
            d.setOnClickListener(null);
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

    public long computeRepeatTime(boolean correct, int count, int mozno) {
        return getTimeSeconds() + 10;
//        if (!correct) return getTimeSeconds() + DAY;
//        if (mCurrentWordLastInterval <= DAY) { //*1.1
//            return DAY * 6;
//        } else {
//            long coefficient = 1; //todo mCurrentWord.getLastInterval - 0.8 + 0.28q - 0.02q^2 [1.3, 2.5]
//            return mCurrentWordLastInterval * coefficient; //todo random 0.95-1.05
//        }
    }

    public boolean checkToRepeat() {
        Cursor cur = mWordsDb.rawQuery("SELECT * FROM " + LearnedWordEntry.TABLE_NAME + " WHERE " +
                LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " < " + getTimeSeconds()
                + " ORDER BY " + LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " ASC LIMIT 1", null);
        if (cur.moveToFirst()) {
            repeating = true;
//            wordToRepeat = new Word();
            mCurrentWord.setWord(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_WORD)));
            //todo uvazovat vsetky preklady
            mCurrentWord.setTranslations(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_TRANSLATIONS)));
            mCurrentWord.setPronunciation(cur.getString(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION)));
            mCurrentWord.setLastInterval(cur.getInt(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL)));
//            w1.setDifficulty(cur.getFloat(cur.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_DIFFICULTY)));
            cur.close();
            return true;
        } else {
            cur.close();
            return false;
        }
    }

    public void getNewWord() {
        repeating = false;
        Cursor currentCursor = mWordsDb.rawQuery("SELECT * FROM " + WordEntry.TABLE_NAME + " WHERE " +
                WordEntry.COLUMN_NAME_LEARNED + " = 0 ORDER BY ABS(" + WordEntry.COLUMN_NAME_DIFFICULTY +
                " - " + mSkill + ") LIMIT 10", null);
        Random r = new Random(System.nanoTime());

        if (currentCursor.moveToPosition(r.nextInt(10))) {

//                    wordToRepeat = new Word();
            mCurrentWord.setWord(currentCursor.getString(currentCursor.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_WORD)));
            //todo uvazovat vsetky preklady
            mCurrentWord.setTranslations(currentCursor.getInt(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED_COUNT)) +
                    "x " + currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_TRANSLATIONS)).split(";")[0] +
                    " " + currentCursor.getString(currentCursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_DIFFICULTY)));
            mCurrentWord.setPronunciation(currentCursor.getString(currentCursor.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_PRONUNCIATION)));
            mCurrentWord.setDifficulty(currentCursor.getFloat(currentCursor.getColumnIndexOrThrow(LearnedWordEntry.COLUMN_NAME_DIFFICULTY)));

        }
        currentCursor.close();
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
            tts.setLanguage(Locale.ENGLISH);
        }
    }

    public int getDirection() {
        Random r = new Random(System.nanoTime());
        return r.nextInt(2);
    }

    public long getTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

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
        if (cursor.moveToFirst()) {
            do {
                Word w = new Word();
                w.setWord(cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_WORD)));
                if (mWords.contains(w) || w.equals(mCurrentWord)) continue; //?
                w.setIsLearned(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED)));//??
                //todo uvazovat vsetky preklady
                w.setTranslations(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED_COUNT)) +
                        "x " + cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_TRANSLATIONS)).split(";")[0] +
                        " " + cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_DIFFICULTY)));
                w.setPronunciation(cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_PRONUNCIATION)));
                w.setDifficulty(cursor.getFloat(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_DIFFICULTY)));
                w.setFrequency(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_FREQUENCY)));
                w.setLearnedCount(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_LEARNED_COUNT)));
                w.setPercentil(cursor.getInt(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_PERCENTIL)));

                mWords.add(w);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(getActivity(), "Error while loading new word. Please restart app", Toast.LENGTH_LONG).show();
        }
    }


}
