package cz.sd2.altconv;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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

    protected JSONObject cache = new JSONObject();
    protected String fromCurrency;
    protected String toCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: btn to update cache
        new RequestTask().execute("https://mgalix.sd2.cz/altconv/");

        Button btnConvert = (Button) findViewById(R.id.btn_convert);

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(MainActivity.class.getName(), fromCurrency + " " + toCurrency);
            }
        });
    }

    @Override
    protected void onResume(){
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpSpinnerData(){
        Spinner spFrom=(Spinner)findViewById(R.id.fromcurrency_spin);
        Spinner spTo=(Spinner)findViewById(R.id.tocurrency_spin);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencyType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrom.setAdapter(adapter);
        spFrom.setOnItemSelectedListener(new ItemSelectedFrom());
        spTo.setAdapter(adapter);
        spTo.setOnItemSelectedListener(new ItemSelectedTo());

    }

    protected void makeToast(final String msg, final int length){
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(getApplicationContext(), msg, length).show();
            }
        });
    }

    private class ItemSelectedFrom implements OnItemSelectedListener {
        public void onNothingSelected(AdapterView<?> av){
        }

        public void onItemSelected(AdapterView<?> av, View view, int position, long id){
            TextView sel=(TextView)view;
            String from=sel.getText().toString();
            fromCurrency=from;
            EditText te_value=(EditText)findViewById(R.id.te_value);
            te_value.setHint("Enter "+fromCurrency+" amount");
        }
    }

    private class ItemSelectedTo implements OnItemSelectedListener{
        public void onNothingSelected(AdapterView<?> av){

        }
        public void onItemSelected(AdapterView<?> av, View view, int position, long id){
            TextView sel=(TextView)view;
            String to=sel.getText().toString();
            toCurrency=to;
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
                makeToast("Downloading finished.", Toast.LENGTH_SHORT);
            } catch (IOException e) {
                Log.i(MainActivity.class.getName(), rx);
                makeToast("Downloading failed.", Toast.LENGTH_SHORT);
            } catch (JSONException e) {
                Log.i(MainActivity.class.getName(), cache.toString());
                makeToast("Downloading failed.", Toast.LENGTH_SHORT);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        private String getJsonString(String url)throws IOException {
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
