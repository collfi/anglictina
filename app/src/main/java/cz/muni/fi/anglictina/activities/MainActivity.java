package cz.muni.fi.anglictina.activities;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.fragments.SettingsFragment;
import cz.muni.fi.anglictina.utils.network.AlarmReceiver;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static int sCorrect;
    public static int sIncorrect;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
//        test();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.learning_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(OnDbFinishedReceiver, new IntentFilter("INTENT"));

        if (!getDatabasePath("words.db").exists()) {
            pd = new ProgressDialog(this);
            pd.setMessage("Prosim cekejte...");
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            try {
                WordDbHelper helper = new WordDbHelper(this);
                SQLiteDatabase db = helper.getWritableDatabase();
                BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("words6.txt")));


                String sql = "INSERT INTO " + WordContract.WordEntry.TABLE_NAME + " VALUES (?,?,?,?,?,?,?,?,?,?,?);";
                SQLiteStatement statement = db.compileStatement(sql);
                db.beginTransaction();
                String line = reader.readLine();
                while (line != null) {
//                    ContentValues values = new ContentValues();
                    String[] s = line.split("\t");
//                    values.put(WordEntry.COLUMN_NAME_WORD, s[0]);
//                    values.put(WordEntry.COLUMN_NAME_TRANSLATIONS, s[1]);
//                    values.put(WordEntry.COLUMN_NAME_FREQUENCY, s[2]);
//                    values.put(WordEntry.COLUMN_NAME_PERCENTIL, s[3]);
//                    values.put(WordEntry.COLUMN_NAME_PRONUNCIATION, s[4]);
//                    values.put(WordEntry.COLUMN_NAME_DIFFICULTY, ((Float.valueOf(s[3]) * 4f / 100f) - 2f));
//                    values.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, 0);
//                    values.put(WordEntry.COLUMN_NAME_LEARNED, 0);
//                    values.put(WordEntry.COLUMN_NAME_CATEGORIES, s[5]);
//                    db.insert(WordEntry.TABLE_NAME, null, values);


                    statement.clearBindings();
                    statement.bindLong(1, System.nanoTime());
                    statement.bindString(2, s[0]);
                    statement.bindString(3, s[1]);
                    statement.bindLong(4, Integer.valueOf(s[2]));
                    statement.bindLong(5, Integer.valueOf(s[3]));
                    statement.bindDouble(6, ((Float.valueOf(s[3]) * 4f / 100f) - 2f));
                    statement.bindLong(7, 0);
                    statement.bindLong(8, 0);
                    statement.bindString(9, s[5]);
                    statement.bindString(10, s[6]);
                    statement.bindString(11, s[4]);

                    statement.execute();

                    line = reader.readLine();
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
                reader.close();
                Log.i("konec", "db");
            } catch (IOException ioe) {
                Log.e("Main Activity", "error loading db");
            }
        }

        SharedPreferences sp = getSharedPreferences("stats", Context.MODE_PRIVATE);
        sCorrect = sp.getInt("correct", 0);
        sIncorrect = sp.getInt("incorrect", 0);

        firstTime();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.learning_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_learn) {
            startActivity(new Intent(this, LearnActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_statistics) {
            DialogFragment dialog = new StatisticsFragment();
            dialog.show(getSupportFragmentManager(), "StatisticsDialogFragment");
        } else if (id == R.id.nav_revise) {
            startActivity(new Intent(this, LearnedWordsActivity.class));
        } else if (id == R.id.nav_feedback) {
            SettingsFragment.FeedbackDialog dialog = new SettingsFragment.FeedbackDialog();
            dialog.show(getSupportFragmentManager(), "feedback");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else if (id == R.id.nav_about) {
            Toast.makeText(this, "Zatím nic.", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.learning_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class StatisticsFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View v = getActivity().getLayoutInflater()
                    .inflate(R.layout.fragment_statistics_dialog, null);
            TextView c = (TextView) v.findViewById(R.id.correct);
            TextView i = (TextView) v.findViewById(R.id.incorrect);
            c.setText(String.valueOf(MainActivity.sCorrect));
            i.setText(String.valueOf(MainActivity.sIncorrect));
            builder.setPositiveButton("OK", null).setView(v);
            // Create the AlertDialog object and return it
            AlertDialog a = builder.create();
            float density = getResources().getDisplayMetrics().density;
            a.getWindow().setLayout((int) (160 * density), (int) (160 * density));
            return a;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i("qqq", "main activity on Resume");
    }

    public void firstTime() {
        SharedPreferences sp = getSharedPreferences("firstTime", Context.MODE_PRIVATE);
        if (sp.getBoolean("run", true)) {
            sp.edit().putBoolean("run", false).apply();
            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME, 5000, AlarmManager.INTERVAL_DAY, pendingIntent);

            new PostInfo().execute();
        }
    }

    public void deleteDb(View v) {
        try {
            File f = new File(Environment.DIRECTORY_PICTURES, "words.db");
            Log.i("asdf", f.exists() + "");
            copy(getDatabasePath("words.db"), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        WordDbHelper helper = new WordDbHelper(this);
//        SQLiteDatabase db = helper.getWritableDatabase();
//        db.delete(WordContract.LearnedWordEntry.TABLE_NAME, null, null);
//        db.close();


//        new TestTask2().execute();
    }

    public class PostInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                StringBuilder sb = new StringBuilder();
                WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                String address = info.getMacAddress();
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

                sb.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.DEVICE)
                        .append(" Android version: ").append(Build.VERSION.RELEASE)
                        .append(" ").append(address)
                        .append(" ").append(telephonyManager.getDeviceId())
                        .append(" Time: ").append(System.currentTimeMillis());
                String data = sb.toString();
                Log.i("post output", data);
                URL url = new URL("http://collfi.pythonanywhere.com/info");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/plain");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(data);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();
                Log.d("POST", "MSG " + connection.getResponseMessage());
                Log.d("POST RES", "" + responseCode);
                if (responseCode != 200) {
                    return null;
                }
//                final StringBuilder output = new StringBuilder("Request URL " + url);
//                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
//                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String line = "";
//                StringBuilder responseOutput = new StringBuilder();
//                while ((line = br.readLine()) != null) {
//                    responseOutput.append(line);
//                }
//                br.close();
//
//                Log.d("output", responseOutput.toString());
//
//                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());


            } catch (MalformedURLException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
            }
            return null;
        }

    }


    public class Levenshtein extends AsyncTask<Void, Pair<Integer, String>, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String a[] = new String[6353];
            try {

                BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("onlywords.txt")));
                String line = reader.readLine();
                int i = 0;
                while (line != null) {
                    a[i] = line;
                    i++;
                    line = reader.readLine();
                }

                reader.close();
            } catch (IOException ioe) {
                Log.e("Main Activity", "error loading db");
            }
            publishProgress(new Pair<Integer, String>(1, "done loading"));
            for (int i = 0; i < 1; i++) {


                for (int j = 0; j < a.length; j++) {
                    Log.i("leven", "" + i + "-" + j + ". " + a[i] + " = " + levenshteinDistance(a[i], a[j]));
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Pair<Integer, String>... values) {
            Toast.makeText(MainActivity.this, values[0].first + ". " + values[0].second, Toast.LENGTH_SHORT).show();
        }

        public int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
            int len0 = lhs.length() + 1;
            int len1 = rhs.length() + 1;

            // the array of distances
            int[] cost = new int[len0];
            int[] newcost = new int[len0];

            // initial cost of skipping prefix in String s0
            for (int i = 0; i < len0; i++) cost[i] = i;

            // dynamically computing the array of distances

            // transformation cost for each letter in s1
            for (int j = 1; j < len1; j++) {
                // initial cost of skipping prefix in String s1
                newcost[0] = j;

                // transformation cost for each letter in s0
                for (int i = 1; i < len0; i++) {
                    // matching current letters in both strings
                    int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                    // computing cost for each transformation
                    int cost_replace = cost[i - 1] + match;
                    int cost_insert = cost[i] + 1;
                    int cost_delete = newcost[i - 1] + 1;

                    // keep minimum cost
                    newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
                }

                // swap cost/newcost arrays
                int[] swap = cost;
                cost = newcost;
                newcost = swap;
            }

            // the distance is the cost for transforming all letters in both strings
            return cost[len0 - 1];
        }
    }

    public void test() {
        Word w = new Word();
        Word r = new Word();
        Log.i("word", w.equals(r) + "");
        w.setWord("asdf");
        Log.i("word", w.equals(r) + "");
        r.setWord("asdf");
        Log.i("word", w.equals(r) + "");
        r.setWord("asdf");
        Log.i("word", w.equals(r) + "");
        r.setTranslations(new String[1]);
        Log.i("word", w.equals(r) + "");
        w.setTranslations(new String[1]);
        Log.i("word", w.getTranslations().length + " " + r.getTranslations().length);
        Log.i("word", w.equals(r) + "");
        w.getTranslations()[0] = "a";
        Log.i("word", w.equals(r) + "");
        r.getTranslations()[0] = "a";
        Log.i("word", w.equals(r) + "");
        r.getTranslations()[0] = "b";
        Log.i("word", w.equals(r) + "");
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(OnDbFinishedReceiver);
        super.onDestroy();
    }

   private BroadcastReceiver OnDbFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }
    };
}
