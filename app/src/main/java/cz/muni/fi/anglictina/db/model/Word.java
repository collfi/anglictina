package cz.muni.fi.anglictina.db.model;

import java.util.Arrays;

/**
 * Created by collfi on 17. 11. 2015.
 */
public class Word {
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
    // todo slovny druh

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

        if (word != null ? !word.equals(word1.word) : word1.word != null) return false;
//        if (translations == null && word1.translations == null) return true;
//        if (translations == null) return false;
//        if (word1.translations == null) return false;
//        if (translations.length == 0 && word1.translations.length == 0) return true;
//        if (translations.length == 0) return false;
//        if (word1.translations.length == 0) return false;
        return (translations[0] != null ? !translations[0].equals(word1.translations[0]) : word1.translations[0] != null);

    }

    @Override
    public int hashCode() {
        int result = word != null ? word.hashCode() : 0;
        result = 31 * result + (translations != null ? Arrays.deepHashCode(translations) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Word{" +
                "word='" + word + '\'' +
                ", 1. translation=" + translations[0] +
                '}';
    }
}
