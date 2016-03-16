package cz.muni.fi.anglictina.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.activities.LearnActivity;
import cz.muni.fi.anglictina.activities.MainActivity;
import cz.muni.fi.anglictina.activities.SettingsActivity;

/**
 * Created by collfi on 24. 10. 2015.
 */
public class MainFragment extends Fragment {
    private SharedPreferences mPreferences;
//    private TextView mSkill;
//    private CircularProgressView mSkillProgress;
    private int i;
    private TextView mSkill;
    private TextView mCorrect;
    private TextView mIncorrect;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getActivity().getSharedPreferences("stats", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Button learn = (Button) view.findViewById(R.id.button_start);
        mSkill = (TextView) view.findViewById(R.id.skill);
        mCorrect = (TextView) view.findViewById(R.id.correct);
        mIncorrect = (TextView) view.findViewById(R.id.incorrect);
        learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LearnActivity.class));
            }
        });

        Button settings = (Button) view.findViewById(R.id.button_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.fragmentContainer, new SettingsFragment())
//                        .commit();
            }
        });

        Button help = (Button) view.findViewById(R.id.button_help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Zatím nic.", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkill.setText(String.format("%.2f", mPreferences.getFloat("skill", 0)));
        mCorrect.setText(String.valueOf(MainActivity.sCorrect));
        mIncorrect.setText(String.valueOf(MainActivity.sIncorrect));
//        mSkill.setText(String.valueOf((int) mPreferences.getFloat("skill", 0)));
//        mSkillProgress.setProgress(((int) ((mPreferences.getFloat("skill", 0) % 1) * 100)));
//        mSkillProgress.startAnimation();
        Log.i("QQQ", String.valueOf((int) mPreferences.getFloat("skill", 0)) + " " + ((int) ((mPreferences.getFloat("skill", 0) % 1) * 100)));
//        SQLiteDatabase db = new WordDbHelper(getActivity()).getWritableDatabase();
//        Cursor c = db.rawQuery("SELECT " + WordContract.WordEntry.COLUMN_NAME_DIFFICULTY + ", " +
//                WordContract.WordEntry.COLUMN_NAME_WORD + " FROM "
//                + WordContract.WordEntry.TABLE_NAME + " ORDER BY " + WordContract.WordEntry.COLUMN_NAME_DIFFICULTY
//                + " ASC", null);
//        double a = 0d;
//        while (c.moveToNext()) {
//            a += c.getDouble(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_DIFFICULTY));
//            if (c.getPosition() % 100 == 0) {
//                Log.i("mapa", a / 100 + "  ==  " + c. getPosition());
//                Log.i("mapa", "chance " + (1 / (1 + (double) Math.exp(-(mPreferences.getFloat("skill", 0) - (a / 100))))));
//                //todo mapa znalosti
//                a = 0d;
//            }
//        }
//        c.close();
//        db.close();
    }
}
