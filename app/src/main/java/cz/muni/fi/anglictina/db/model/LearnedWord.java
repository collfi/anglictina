package cz.muni.fi.anglictina.db.model;

/**
 * Created by collfi on 19. 11. 2015.
 */
public class LearnedWord {
    private String word;
    private long timeToRepeat;
//    private float difficulty;
    private long lastInterval;
    private String[] translations;
    private String pronuanciation;
    private String[] categories;
    //slovny druh a ine charakteristiky na vybranie distraktorov


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public long getTimeToRepeat() {
        return timeToRepeat;
    }

    public void setTimeToRepeat(long timeToRepeat) {
        this.timeToRepeat = timeToRepeat;
    }

//    public float getDifficulty() {
//        return difficulty;
//    }

//    public void setDifficulty(float difficulty) {
//        this.difficulty = difficulty;
//    }

    public long getLastInterval() {
        return lastInterval;
    }

    public void setLastInterval(long lastInterval) {
        this.lastInterval = lastInterval;
    }

    public String getPronuanciation() {
        return pronuanciation;
    }

    public void setPronuanciation(String pronuanciation) {
        this.pronuanciation = pronuanciation;
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
}
