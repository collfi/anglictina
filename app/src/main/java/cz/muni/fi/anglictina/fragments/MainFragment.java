package cz.muni.fi.anglictina.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.activities.LearnActivity;
import cz.muni.fi.anglictina.activities.MainActivity;
import cz.muni.fi.anglictina.activities.SettingsActivity;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;

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
    private Button learn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getActivity().getSharedPreferences("stats", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mSkill = (TextView) view.findViewById(R.id.skill);
        mCorrect = (TextView) view.findViewById(R.id.correct);
        mIncorrect = (TextView) view.findViewById(R.id.incorrect);
        learn = (Button) view.findViewById(R.id.button_start);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        int height72dp = (int) (getResources().getDisplayMetrics().density * 72);
        int width = (int) ((getResources().getDisplayMetrics().widthPixels - getResources().getDisplayMetrics().density * 22.5) / 2);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height72dp);
        params.addRule(RelativeLayout.ABOVE, R.id.buttons_layout);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.bottomMargin = (int) (getResources().getDisplayMetrics().density * 15);
        learn.setLayoutParams(params);

    }

    public void computeUserSkill() {
        WordDbHelper helper = new WordDbHelper(getActivity());
        SQLiteDatabase db = helper.getReadableDatabase();
//        Cursor c = db.rawQuery("SELECT * FROM " + WordContract.WordEntry.TABLE_NAME + " ORDER BY " +
//                WordContract.WordEntry.COLUMN_NAME_DIFFICULTY + " ASC", null);
//        float sum = 0.0f;
//        int count = c.getCount();
//        while (c.moveToNext()) {
//            sum += 1 / (1 + (float) Math.exp(-(getActivity().getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0f)
//                    - c.getFloat(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_DIFFICULTY)))));
//        }
//        mSkill.setText(String.format("%.0f%%", (sum / (float) count) * 100));
        Cursor c = db.rawQuery("SELECT * FROM "
                + WordContract.WordEntry.TABLE_NAME + " ORDER BY "
                + WordContract.WordEntry.COLUMN_NAME_DIFFICULTY + " DESC LIMIT 1", null);
        float chance = 0;
        if (c.moveToFirst()) {
            chance = 1 / (1 + (float) Math.exp(-(getActivity().getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0)
                    - c.getFloat(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_DIFFICULTY)))));
        }
//        Log.i("skill", "" + getActivity().getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0));
//        Log.i("skill", "" + c.getFloat(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_DIFFICULTY)));
//        Log.i("skill", "" + c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_WORD)));
        mSkill.setText(String.format("%.0f%%", chance * 100));

        c.close();
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();
//        mSkill.setText(String.format("%.2f", mPreferences.getFloat("skill", 0)));
        computeUserSkill();
        mCorrect.setText(String.valueOf(MainActivity.sCorrect));
        mIncorrect.setText(String.valueOf(MainActivity.sIncorrect));
        Log.i("QQQ", String.valueOf((int) mPreferences.getFloat("skill", 0)) + " " + ((int) ((mPreferences.getFloat("skill", 0) % 1) * 100)));

    }
}
