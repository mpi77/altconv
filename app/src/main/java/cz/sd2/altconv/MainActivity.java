package cz.sd2.altconv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    public static final String BUNDLE_CACHE = "cache";
    public static final String BUNDLE_SERVER_URI = "serverURI";
    public static final String CACHE_COURSES = "courses";
    protected JSONObject cache = new JSONObject();
    protected String fromCurrency;
    protected String toCurrency;
    protected String serverURI = "https://mgalix.sd2.cz/altconv/";
    protected EditText te_value;
    protected TextView calc_value;
    protected Spinner spFrom;
    protected Spinner spTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spFrom = (Spinner) findViewById(R.id.fromcurrency_spin);
        spTo = (Spinner) findViewById(R.id.tocurrency_spin);
        te_value = (EditText) findViewById(R.id.te_value);
        calc_value = (TextView) findViewById(R.id.tv_calc_value);

        Button btnConvert = (Button) findViewById(R.id.btn_convert);

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convert();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(MainActivity.BUNDLE_CACHE, cache.toString());
        savedInstanceState.putString(MainActivity.BUNDLE_SERVER_URI, serverURI);
        calc_value.setText("");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            cache = new JSONObject(savedInstanceState.getString(MainActivity.BUNDLE_CACHE));
            serverURI = savedInstanceState.getString(MainActivity.BUNDLE_SERVER_URI);
        } catch (JSONException e) {
            Log.i(MainActivity.class.getName(), savedInstanceState.getString(MainActivity.BUNDLE_CACHE));
            makeToast(getResources().getString(R.string.msg_restore_courses_fail), Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpSpinnerData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            settingsDialog();
        } else if (id == R.id.action_download) {
            new RequestTask().execute(serverURI);
        }

        return super.onOptionsItemSelected(item);
    }

    private void settingsDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View settView = li.inflate(R.layout.settings_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(settView);
        final EditText set_val = (EditText) settView.findViewById(R.id.te_set_val);
        set_val.setText(serverURI);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                serverURI = set_val.getText().toString();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setUpSpinnerData() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencyType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrom.setAdapter(adapter);
        spFrom.setOnItemSelectedListener(new ItemSelectedFrom());
        spTo.setAdapter(adapter);
        spTo.setOnItemSelectedListener(new ItemSelectedTo());

    }

    private void convert() {
        double cc_btc = 0;
        double cc_dst = 0;
        try {
            if(!cache.has(MainActivity.CACHE_COURSES)){
                makeToast(getResources().getString(R.string.msg_empty_courses), Toast.LENGTH_SHORT);
                return;
            }
            cc_btc = cache.getJSONObject(MainActivity.CACHE_COURSES).getDouble(fromCurrency);
            cc_dst = cache.getJSONObject(MainActivity.CACHE_COURSES).getDouble(toCurrency);
            if (fromCurrency.equals(toCurrency)) {
                calc_value.setText(String.format("%s %s", te_value.getText().length() > 0 ? te_value.getText() : "0", toCurrency));
            } else {
                double amount = Double.parseDouble(te_value.getText().toString());
                double toDestCurrency = amount * cc_btc * (1.0 / cc_dst);
                calc_value.setText(String.format("%.3f %s", toDestCurrency, toCurrency));
            }
        } catch (Exception e) {
            Log.i(MainActivity.class.getName(), fromCurrency + " " + toCurrency + " " + cc_btc);
            makeToast(getResources().getString(R.string.msg_conversion_failed), Toast.LENGTH_SHORT);
            calc_value.setText("");
        }
    }

    protected void makeToast(final String msg, final int length) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, length).show();
            }
        });
    }

    private class ItemSelectedFrom implements OnItemSelectedListener {
        public void onNothingSelected(AdapterView<?> av) {
        }

        public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
            TextView sel = (TextView) view;
            String from = sel.getText().toString();
            fromCurrency = from;
            te_value.setHint(getResources().getString(R.string.msg_type_amount) + " " + fromCurrency);
        }
    }

    private class ItemSelectedTo implements OnItemSelectedListener {
        public void onNothingSelected(AdapterView<?> av) {

        }

        public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
            TextView sel = (TextView) view;
            String to = sel.getText().toString();
            toCurrency = to;
        }
    }


    class RequestTask extends AsyncTask<String, String, String> {

        private String rx;

        @Override
        protected String doInBackground(String... url) {
            try {
                rx = getJsonString(url[0]);
                cache = new JSONObject(rx);
                Log.i(MainActivity.class.getName(), cache.toString());
                makeToast(getResources().getString(R.string.msg_download_ok), Toast.LENGTH_SHORT);
            } catch (IOException e) {
                Log.i(MainActivity.class.getName(), rx);
                makeToast(getResources().getString(R.string.msg_download_fail), Toast.LENGTH_SHORT);
            } catch (JSONException e) {
                Log.i(MainActivity.class.getName(), cache.toString());
                makeToast(getResources().getString(R.string.msg_download_fail), Toast.LENGTH_SHORT);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        private String getJsonString(String url) throws IOException {
            StringBuilder build = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String con;
            while ((con = reader.readLine()) != null) {
                build.append(con);
            }
            return build.toString();
        }
    }
}
