package cz.muni.fi.anglictina.utils;

import java.util.Comparator;

import cz.muni.fi.anglictina.db.model.Word;

/**
 * Created by collfi on 7. 1. 2016.
 */
public class WordLevenshteinComparator implements Comparator<Word> {


    @Override
    public int compare(Word o1, Word o2) {

        // descending order (ascending order would be:
        // o1.getGrade()-o2.getGrade())
        return o1.getLevenshteinToCurrent() - o2.getLevenshteinToCurrent();
    }

}
