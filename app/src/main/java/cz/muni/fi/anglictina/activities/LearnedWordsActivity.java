package cz.muni.fi.anglictina.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cz.muni.fi.anglictina.App;
import cz.muni.fi.anglictina.R;

/**
 * Created by collfi on 4. 2. 2016.
 */
public class LearnedWordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise);
        setTitle("Opakování...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
    }
}
