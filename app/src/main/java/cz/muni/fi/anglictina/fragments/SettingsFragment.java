package cz.muni.fi.anglictina.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;

/**
 * Created by collfi on 27. 10. 2015.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sp;
    private SQLiteDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getPreferenceScreen().getSharedPreferences();
        db = new WordDbHelper(getActivity()).getWritableDatabase();
        EditTextPreference count = (EditTextPreference) findPreference("pref_count");
        count.setSummary(sp.getString("pref_count", "10"));
        count.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return false;
            }
        });
        PreferenceCategory p = (PreferenceCategory) findPreference("pref_general");
        Preference sync = findPreference("pref_sync_now");
        sync.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (isOnline()) {
                    new GetFromServer().execute();
                    new PostToServer().execute();
                } else {
                    Toast.makeText(getActivity(), "You are not connected" +
                            "to the internet.\nTry again later.", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (pref instanceof android.support.v7.preference.EditTextPreference) {
            EditTextPreference etp = (android.support.v7.preference.EditTextPreference) pref;
            pref.setSummary(etp.getText());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        db.close();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public class GetFromServer extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... params) {
            try {


                URL url = new URL("http://collfi.pythonanywhere.com/get/" + sp.getInt("last_sync", 0)); //

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                Log.d("GET", "MSG " + connection.getResponseMessage());
                Log.d("GET RES", "" + responseCode);
                if (responseCode != 200) {
                    return responseCode;
                }
                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();
                Log.d("get output", responseOutput.toString());

                JSONArray ja = new JSONArray(responseOutput.toString());
                for (int i = 0; i < ja.length(); i++) {
                    //todo check if is learned
                    ContentValues cv = new ContentValues();
                    cv.put(WordContract.WordEntry.COLUMN_NAME_DIFFICULTY,
                            ja.getJSONObject(i).getDouble("difficulty"));
                    cv.put(WordContract.WordEntry.COLUMN_NAME_LEARNED_COUNT,
                            ja.getJSONObject(i).getInt("learned_count"));//++
                    db.update(WordContract.WordEntry.TABLE_NAME, cv, WordContract.WordEntry.COLUMN_NAME_WORD + " = ?",
                            new String[]{ja.getJSONObject(i).getString("english")});
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("settings", "json exception downloading. " + e.getLocalizedMessage());
            } catch (MalformedURLException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            SharedPreferences.Editor ed = sp.edit();
            ed.putInt("last_sync", (int) (System.currentTimeMillis() / 1000));
            ed.apply();
            if (integer.equals(0)) {
                Toast.makeText(getActivity(), "Sync task 1 succeeded.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Sync 1 failed because of code: " + integer, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class PostToServer extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... params) {
            try {
                SharedPreferences pref = getActivity().getSharedPreferences("post", Context.MODE_PRIVATE);
                String data = pref.getString("post", "[]");
                Log.i("post output", data);
                URL url = new URL("http://collfi.pythonanywhere.com/post");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(data);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();
                Log.d("POST", "MSG " + connection.getResponseMessage());
                Log.d("POST RES", "" + responseCode);
                if (responseCode != 200) {
                    return responseCode;
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
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer.equals(0)) {
                SharedPreferences sp = getActivity().getSharedPreferences("post", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.remove("post");
                ed.apply();
                Toast.makeText(getActivity(), "Sync task 2 succeeded.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Sync 2 failed because of code: " + integer, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
