package cz.muni.fi.anglictina.db;

import android.provider.BaseColumns;

/**
 * Created by collfi on 28. 10. 2015.
 */
public class WordContract {
    public static abstract class WordEntry implements BaseColumns {
        public static final String TABLE_NAME = "words";
        public static final String COLUMN_NAME_ID = "wordId";
        public static final String COLUMN_NAME_WORD = "word";
        public static final String COLUMN_NAME_PRONUNCIATION = "pronunciation";
        public static final String COLUMN_NAME_TRANSLATIONS = "translations";
        public static final String COLUMN_NAME_FREQUENCY = "frequency";
        public static final String COLUMN_NAME_PERCENTIL = "percentil";
        public static final String COLUMN_NAME_DIFFICULTY = "difficulty";
        public static final String COLUMN_NAME_LEARNED_COUNT = "learned_count";
        public static final String COLUMN_NAME_LEARNED = "learned";
        public static final String COLUMN_NAME_CATEGORIES = "categories";
    }

    public static abstract class LearnedWordEntry implements BaseColumns {
        public static final String TABLE_NAME = "learnedWords";
        public static final String COLUMN_NAME_ID = "wordId";
        public static final String COLUMN_NAME_WORD = "word";
        public static final String COLUMN_NAME_PRONUNCIATION = "pronunciation";
        public static final String COLUMN_NAME_TRANSLATIONS = "translations";
        public static final String COLUMN_NAME_DIFFICULTY = "difficulty";
        public static final String COLUMN_NAME_TIME_TO_REPEAT = "repeatAt";
        public static final String COLUMN_NAME_LAST_INTERVAL = "lastInterval";
        public static final String COLUMN_NAME_CATEGORIES = "categories";
    }

}
