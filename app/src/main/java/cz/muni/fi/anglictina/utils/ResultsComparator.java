package cz.muni.fi.anglictina.utils;

import android.support.v4.util.Pair;

import java.util.Comparator;

import cz.muni.fi.anglictina.db.model.Word;

/**
 * Created by collfi on 4. 4. 2016.
 */
public class ResultsComparator implements Comparator<Pair<Word, Boolean>> {
    @Override
    public int compare(Pair<Word, Boolean> lhs, Pair<Word, Boolean> rhs) {
        if (lhs.second == null || rhs.second == null) {
            return (int) (rhs.first.getDifficulty() * 10) - (int) (lhs.first.getDifficulty() * 10);
        }
        if (lhs.equals(rhs)) return 0;
        if (!lhs.second.equals(rhs.second)) {
            if (lhs.second) {
                return 1;
            } else {
                return -1;
            }
        }
        return (int) (rhs.first.getDifficulty() * 10) - (int) (lhs.first.getDifficulty() * 10);

    }
}
