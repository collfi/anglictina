package cz.muni.fi.anglictina.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cz.muni.fi.anglictina.R;

/**
 * Created by collfi on 4. 2. 2016.
 */
public class LearnedWordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise);
        setTitle("Revising...");
    }
}
