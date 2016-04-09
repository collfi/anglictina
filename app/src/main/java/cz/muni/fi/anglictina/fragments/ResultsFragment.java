package cz.muni.fi.anglictina.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cz.muni.fi.anglictina.BuildConfig;
import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.activities.LearnActivity;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.utils.Categories;
import cz.muni.fi.anglictina.utils.Results;
import cz.muni.fi.anglictina.utils.ResultsComparator;
import cz.muni.fi.anglictina.utils.adapters.ResultsAdapter;

/**
 * Created by collfi on 31. 1. 2016.
 */
public class ResultsFragment extends Fragment implements TextToSpeech.OnInitListener{
    private List<Pair<Word, Boolean>> results;
    private ExpandableListView mList;
    private AutoCompleteTextView mFilter;
    private ResultsAdapter mResultsAdapter;
    private TextToSpeech tts;

    public static ResultsFragment newInstance(Results r) {
        ResultsFragment fragment = new ResultsFragment();
        Bundle args = new Bundle();
        args.putParcelable("results", r);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        if (getArguments() != null) {
            results = new ArrayList<>(((Results) getArguments().getParcelable("results")).res);
            Collections.sort(results, new ResultsComparator());
        }
        getActivity().setTitle("Výsledky");
        tts = new TextToSpeech(getActivity(), this);
        if (!BuildConfig.DEBUG) {
            new PostToServer(getActivity()).execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_results, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (item.getItemId() == R.id.nav_continue) {
            getActivity().finish();
        } else*/ if (item.getItemId() == R.id.nav_next) {
            getActivity().finish();
            startActivity(new Intent(getActivity(), LearnActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_words_list, container, false);
        mList = (ExpandableListView) view.findViewById(R.id.results);
        Set<String> cats = new HashSet<>();
        for (Pair<Word, Boolean> w : results) {
//            cats.addAll(Arrays.asList(w.first.getHumanCategories()));
            for (String s : w.first.getHumanCategories()) {
                cats.add(Categories.categoriesForHuman[Categories.categoriesForHumanAscii.indexOf(s)]);
            }
        }
        mResultsAdapter = new ResultsAdapter(getActivity(), results, tts);
        mList.setAdapter(mResultsAdapter);

        mFilter = (AutoCompleteTextView) view.findViewById(R.id.filter);
        mFilter.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                cats.toArray()));
        mFilter.clearFocus();

//        mFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                filter(mFilter.getText().toString());
//                Toast.makeText(getActivity(), mFilter.getText().toString(), Toast.LENGTH_SHORT).show();
//                mFilter.dismissDropDown();
//                return true;
//            }
//        });
        mFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilter.showDropDown();
            }
        });
        mFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mFilter.getText().length() == 0) {
                    hideSoftKeyboard();
                }
            }
        });
        mFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                filter(mFilter.getText().toString());
                mFilter.clearFocus();
                hideSoftKeyboard();
                mFilter.dismissDropDown();
                return true;
            }
        });
        mFilter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mFilter.getRight() - mFilter.getCompoundDrawables()[2].getBounds().width())) {
                        mFilter.setText("");
                        mFilter.clearFocus();
                        hideSoftKeyboard();
                        new SelectLearned().execute("");
                        return true;
                    } else {
                        mFilter.showDropDown();
                    }
                }
                return false;
            }
        });
        mFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filter(mFilter.getText().toString());
            }
        });

        return view;


    }

    public void filter(String cat) {
        hideSoftKeyboard();
        new SelectLearned().execute(cat);
    }

    public void refresh() {
        ((ResultsAdapter) mList.getExpandableListAdapter()).notifyDataSetChanged();
    }

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFilter.getWindowToken(), 0);
    }

    public class SelectLearned extends AsyncTask<String, Void, List<Pair<Word, Boolean>>> {
        @Override
        protected List<Pair<Word, Boolean>> doInBackground(String... params) {
            String what = Normalizer.normalize(params[0], Normalizer.Form.NFD)
                    .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "");
            List<Pair<Word, Boolean>> res = new ArrayList<>();
            for (Pair<Word, Boolean> w : results) {
                if (params[0] == null || params[0].equals("") || contains(what, w.first.getHumanCategories())) {
                    res.add(w);
                }
            }
            return res;
        }

        @Override
        protected void onPostExecute(List<Pair<Word, Boolean>> list) {
            mResultsAdapter = new ResultsAdapter(getActivity(), list, tts);
            mList.setAdapter(mResultsAdapter);
        }

        public boolean contains(String what, String[] where) {
            boolean result = false;
            for (String s : where) {
                if (s.toLowerCase().equals(what.toLowerCase()) || s.toLowerCase().startsWith(what.toLowerCase())) {
                    result = true;
                    break;
                }
            }
            return result;
        }
    }

    public class PostToServer extends AsyncTask<Void, Void, Integer> {
        private Context mContext;
        public PostToServer(Context c) {
            mContext = c;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                SharedPreferences pref = mContext.getSharedPreferences("post", Context.MODE_PRIVATE);
                JSONArray resultsArray = new JSONArray(pref.getString("results", "[]"));
                if (resultsArray.length() == 0) {
                    return 1;
                }
                JSONObject data = new JSONObject();
                WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                String address = info.getMacAddress();
                data.put("results", resultsArray);
                data.put("user", address);
                Log.i("post output", data.toString());
                URL url = new URL("http://collfi.pythonanywhere.com/post_all");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(data.toString());
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


            } catch (UnknownHostException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
                return -1;
            } catch (MalformedURLException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
                return -1;
            } catch (IOException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
                return -1;
            } catch (JSONException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
                return -1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            switch (integer) {
                case 0:
                    SharedPreferences sp = mContext.getSharedPreferences("post", Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.remove("results");
                    ed.apply();
                    Toast.makeText(mContext, "Poslání dát na server bylo úspěšné.", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(mContext, "Chyba při posílaní dat na server.", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    break;
            }
            mContext = null;

        }
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
            tts.setLanguage(Locale.ENGLISH);
        }
    }

    @Override
    public void onStop() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.i("asdf", "pause");
        results = null;
//        getFragmentManager().popBackStack(R.id.main_fragment, 0);
//        getFragmentManager().executePendingTransactions();
//        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.learning_fragment)).commit();
//
//        getFragmentManager().beginTransaction().remove(this).commit();
        getActivity().finish();
        super.onPause();
    }
}
