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

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.activities.LearnActivity;
import cz.muni.fi.anglictina.activities.SettingsActivity;

/**
 * Created by collfi on 24. 10. 2015.
 */
public class MainFragment extends Fragment {
    private SharedPreferences mPreferences;
    private TextView mSkill;
    private CircularProgressView mSkillProgress;

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

        mSkill = (TextView) view.findViewById(R.id.skill);
        mSkillProgress = (CircularProgressView) view.findViewById(R.id.skill_progress);
        mSkillProgress.setProgress(0);
//        mSkillProgress.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkill.setText(String.valueOf((int) mPreferences.getFloat("skill", 0)));
        mSkillProgress.setProgress(((int) ((mPreferences.getFloat("skill", 0) % 1) * 100)));
        mSkillProgress.startAnimation();
        Log.i( "QQQ", String.valueOf((int) mPreferences.getFloat("skill", 0)) + " " +((int) ((mPreferences.getFloat("skill", 0) % 1) * 100)) );
    }
}
