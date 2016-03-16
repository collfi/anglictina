package cz.muni.fi.anglictina.db.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by collfi on 17. 11. 2015.
 */
public class Word implements Parcelable {
    private String word;
    private String pronunciation;
    private String[] translations;
    private int frequency;
    private int percentil;
    private float difficulty;
    private int learnedCount;
    private int isLearned;
    private int lastInterval;
    private String[] categories;
    private int levenshteinToCurrent;
    private int correctAnswers;
    private int incorrectAnswers;
    private float diffCoefficient;
    private String[] humanCategories;

    public String[] getHumanCategories() {
        return humanCategories;
    }

    public void setHumanCategories(String[] humanCategories) {
        this.humanCategories = humanCategories;
    }

    public float getDiffCoefficient() {
        return diffCoefficient;
    }

    public void setDiffCoefficient(float diffCoefficient) {
        this.diffCoefficient = diffCoefficient;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void incrementCorrect() {
        correctAnswers++;
    }

    public void incrementIncorrect() {
        incorrectAnswers++;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getPercentil() {
        return percentil;
    }

    public void setPercentil(int percentil) {
        this.percentil = percentil;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public int getLearnedCount() {
        return learnedCount;
    }

    public void setLearnedCount(int learnedCount) {
        this.learnedCount = learnedCount;
    }

    public int getIsLearned() {
        return isLearned;
    }

    public void setIsLearned(int isLearned) {
        this.isLearned = isLearned;
    }

    public int getLastInterval() {
        return lastInterval;
    }

    public void setLastInterval(int lastInterval) {
        this.lastInterval = lastInterval;
    }

    public String[] getTranslations() {
        return translations;
    }

    public void setTranslations(String[] translations) {
        this.translations = translations;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public int getLevenshteinToCurrent() {
        return levenshteinToCurrent;
    }

    public void setLevenshteinToCurrent(int levenshteinToCurrent) {
        this.levenshteinToCurrent = levenshteinToCurrent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Word word1 = (Word) o;

        return !(word != null ? !word.equals(word1.word) : word1.word != null);

    }

    @Override
    public int hashCode() {
        return word != null ? word.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Word{" +
                "word='" + word + '\'' +
                ", 1. translation=" + translations[0] +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.word);
        dest.writeString(this.pronunciation);
        dest.writeStringArray(this.translations);
        dest.writeInt(this.frequency);
        dest.writeInt(this.percentil);
        dest.writeFloat(this.difficulty);
        dest.writeInt(this.learnedCount);
        dest.writeInt(this.isLearned);
        dest.writeInt(this.lastInterval);
        dest.writeStringArray(this.categories);
        dest.writeInt(this.levenshteinToCurrent);
        dest.writeInt(this.correctAnswers);
        dest.writeInt(this.incorrectAnswers);
        dest.writeFloat(this.diffCoefficient);
        dest.writeStringArray(this.humanCategories);
    }

    public Word() {
    }

    protected Word(Parcel in) {
        this.word = in.readString();
        this.pronunciation = in.readString();
        this.translations = in.createStringArray();
        this.frequency = in.readInt();
        this.percentil = in.readInt();
        this.difficulty = in.readFloat();
        this.learnedCount = in.readInt();
        this.isLearned = in.readInt();
        this.lastInterval = in.readInt();
        this.categories = in.createStringArray();
        this.levenshteinToCurrent = in.readInt();
        this.correctAnswers = in.readInt();
        this.incorrectAnswers = in.readInt();
        this.diffCoefficient = in.readFloat();
        this.humanCategories = in.createStringArray();
    }

    public static final Creator<Word> CREATOR = new Creator<Word>() {
        public Word createFromParcel(Parcel source) {
            return new Word(source);
        }

        public Word[] newArray(int size) {
            return new Word[size];
        }
    };
}
