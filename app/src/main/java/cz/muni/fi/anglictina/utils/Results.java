package cz.muni.fi.anglictina.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.anglictina.db.model.Word;

/**
 * Created by collfi on 31. 1. 2016.
 */
public class Results implements Parcelable {
    public List<Pair<Word, Boolean>> res;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.res);
    }

    public Results() {
    }

    protected Results(Parcel in) {
        this.res = new ArrayList<Pair<Word, Boolean>>();
        in.readList(this.res, List.class.getClassLoader());
    }

    public static final Creator<Results> CREATOR = new Creator<Results>() {
        public Results createFromParcel(Parcel source) {
            return new Results(source);
        }

        public Results[] newArray(int size) {
            return new Results[size];
        }
    };
}
