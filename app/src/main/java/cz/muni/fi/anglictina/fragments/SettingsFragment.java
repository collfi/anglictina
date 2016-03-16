package cz.muni.fi.anglictina.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.muni.fi.anglictina.R;
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
                showKeyboard();
                return true;
            }
        });
        PreferenceCategory p = (PreferenceCategory) findPreference("pref_general");

        Preference feedback = findPreference("pref_feedback");
        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FeedbackDialog dialog = new FeedbackDialog();
                dialog.show(getFragmentManager(), "feedback");
                showKeyboard();
                return false;
            }
        });

        Preference about = findPreference("pref_about");
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "Zatím nic.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        hideKeyboard(getActivity().getCurrentFocus());
        Preference pref = findPreference(key);

        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            if (etp.getText().equals("") || Integer.valueOf(etp.getText()) == 0) {
                Toast.makeText(getActivity(), "Počet musí být věčší ako 1.", Toast.LENGTH_SHORT).show();
                etp.setSummary("1");
                etp.setText("1");
                etp.callChangeListener("1");
                return;
            }
            pref.setSummary(etp.getText());
        }
        //todo timepicker dialog, change notification time(also download)
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


    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public static class FeedbackDialog extends DialogFragment {
        private EditText message;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_feedback, null);
            message = (EditText) view.findViewById(R.id.message);
            builder.setView(view);
            builder.setMessage("Prosím, podělte se o svou spětnou vazbu. Je to důležité.");
            builder.setPositiveButton("Odeslat", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (isConnected()) {
                        new SendFeedback2(getActivity()).execute(message.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), "Nejste připojen k Internetu.", Toast.LENGTH_SHORT).show();
                    }
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(message.getWindowToken(), 0);
                    dismiss();
                }
            });
            builder.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(message.getWindowToken(), 0);
                    dismiss();
                }
            });


            final Dialog d = builder.create();
            return d;
        }

        private boolean isConnected() {
            if (isAdded()) {
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
            return false;
        }

        public class SendFeedback extends AsyncTask<String, Void, Integer> {


            @Override
            protected Integer doInBackground(String... params) {
                try {


                    URL url = new URL("http://collfi.pythonanywhere.com/feedback/" + params[0]);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    Log.d("GET", "MSG " + connection.getResponseMessage());
                    Log.d("GET RES", "" + responseCode);
                    return responseCode;
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
                if (integer == 200) {
                    Log.i("feedback", "OK");
                    Toast.makeText(getActivity(), "Úspěšne odesláno.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("feedback", "Fail");
                    Toast.makeText(getActivity(), "Chyba při odesílání.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public class SendFeedback2 extends AsyncTask<String, Void, Integer> {
            private Context mContext;

            public SendFeedback2(Context context) {
                mContext = context;
            }

            @Override
            protected Integer doInBackground(String... params) {
                try {

                    URL url = new URL("http://collfi.pythonanywhere.com/feedback2");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "text/plain");
                    connection.setDoOutput(true);
                    DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                    dStream.writeBytes(params[0]);
                    dStream.flush();
                    dStream.close();
                    int responseCode = connection.getResponseCode();
                    Log.d("POST", "MSG " + connection.getResponseMessage());
                    Log.d("POST RES", "" + responseCode);
                    if (responseCode != 200) {
                        return -1;
                    } else {
                        return responseCode;
                    }
                } catch (MalformedURLException e) {
                    Log.e("settings", e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("settings", e.getLocalizedMessage());
                    e.printStackTrace();
                }
                return -1;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                if (integer == 200) {
                    Log.i("feedback", "OK");
                    Toast.makeText(mContext, "Úspěšne odesláno.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("feedback", "Fail");
                    Toast.makeText(mContext, "Chyba při odesílání.", Toast.LENGTH_SHORT).show();
                }
                mContext = null;
            }
        }
    }


}
