package nctu.fintech.tzing.appmatedemo;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nctu.fintech.appmate.DbAdapter;

public class MainActivity extends AppCompatActivity {

    private DbAdapter _adapter;
    private TextView _tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize adapter
        Resources res = getResources();
        _adapter = new DbAdapter(
                res.getString(R.string.db_host),
                res.getString(R.string.db_table),
                res.getString(R.string.db_username),
                res.getString(R.string.db_password)
        );

        // get text view
        _tv = (TextView)findViewById(R.id.console);

        // delegate onclick listener
        findViewById(R.id.get_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Task().execute();
            }
            class Task extends AsyncTask<Void, Void, JSONArray> {
                @Override
                protected JSONArray doInBackground(Void... params) {
                    return _adapter.Get();
                }

                @Override
                protected void onPostExecute(JSONArray array) {
                    super.onPostExecute(array);
                    if (array == null)
                    {
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    try {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            builder.append(obj.toString());
                            builder.append(",\n");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    _tv.setText(builder.toString());
                }
            }
        });

    }

}
