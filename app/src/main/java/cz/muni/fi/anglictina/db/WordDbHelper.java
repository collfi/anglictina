package cz.muni.fi.anglictina.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.muni.fi.anglictina.db.WordContract.WordEntry;
import cz.muni.fi.anglictina.db.WordContract.LearnedWordEntry;


/**
 * Created by collfi on 28. 10. 2015.
 */
public class WordDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "words.db";

    private static final String CREATE_WORD_TABLE = "CREATE TABLE " + WordEntry.TABLE_NAME + " (" +
            WordEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + WordEntry.COLUMN_NAME_WORD +
            " TEXT , " + WordEntry.COLUMN_NAME_TRANSLATIONS + " TEXT, " + WordEntry.COLUMN_NAME_FREQUENCY +
            " INTEGER, " + WordEntry.COLUMN_NAME_PERCENTIL + " INTEGER, " + WordEntry.COLUMN_NAME_DIFFICULTY
            + " REAL, " + WordEntry.COLUMN_NAME_LEARNED_COUNT + " INTEGER, " +
            WordEntry.COLUMN_NAME_LEARNED + " INTEGER, " + WordEntry.COLUMN_NAME_PRONUNCIATION + " TEXT);";

    private static final String CREATE_LEARNED_WORD_TABLE = "CREATE TABLE " + LearnedWordEntry.TABLE_NAME +
            " (" + LearnedWordEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
            LearnedWordEntry.COLUMN_NAME_WORD + " TEXT , " +
            LearnedWordEntry.COLUMN_NAME_TRANSLATIONS + " TEXT, " +
            LearnedWordEntry.COLUMN_NAME_DIFFICULTY + " REAL, " +
            LearnedWordEntry.COLUMN_NAME_PRONUNCIATION + " TEXT, " +
            LearnedWordEntry.COLUMN_NAME_LAST_INTERVAL + " INTEGER, " +
            LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " INTEGER);";

    public WordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WORD_TABLE);
        db.execSQL(CREATE_LEARNED_WORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(CREATE_WORD_TABLE);
        db.execSQL(CREATE_LEARNED_WORD_TABLE);
        onCreate(db);
    }
}
