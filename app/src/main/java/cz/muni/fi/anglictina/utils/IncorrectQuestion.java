package cz.muni.fi.anglictina.utils;

import java.util.List;

import cz.muni.fi.anglictina.db.model.Word;

/**
 * Created by collfi on 30. 3. 2016.
 */
public class IncorrectQuestion {
    private Word mCurrentWord;
    private List<Word> mDistractors;
    private int direction;

    public Word getCurrentWord() {
        return mCurrentWord;
    }

    public void setCurrentWord(Word currentWord) {
        mCurrentWord = currentWord;
    }

    public List<Word> getDistractors() {
        return mDistractors;
    }

    public void setDistractors(List<Word> distractors) {
        mDistractors = distractors;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
