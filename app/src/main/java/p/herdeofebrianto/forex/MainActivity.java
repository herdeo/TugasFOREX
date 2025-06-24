package p.herdeofebrianto.forex;

import static com.loopj.android.http.AsyncHttpClient.log;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout _swipeRefreshLayout1;
    private RecyclerView _recyclerView1;
    private TextView _timestampTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.swipeRefreshLayout1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initSwipeRefreshLayout();
        _recyclerView1 = findViewById(R.id.recyclerView1);
        _timestampTextView = findViewById(R.id.timestampTextView);

        bindRecyclerView();
    }

    private void bindRecyclerView() {
        String url = "https://openexchangerates.org/api/latest.json?app_id=4535cfee54cc4c218cc640929a7a59f8";
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                log.d("*Azhar*", new String(responseBody));
                String jsonString = new String(responseBody);
                JSONObject root;

                try {
                    root = new JSONObject(jsonString);
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject rates;
                long timestamp;

                try {
                    rates = root.getJSONObject("rates");
                    timestamp = root.getLong("timestamp");
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                setTimestamp(timestamp);

                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                ForexAdapter adapter = new ForexAdapter(rates);
                _recyclerView1.setLayoutManager(layoutManager);
                _recyclerView1.setAdapter(adapter);

                _swipeRefreshLayout1.setRefreshing(false);
            }

            private void setTimestamp(long timestamp) {
                SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale.getDefault());
                format.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
                String dateTime = format.format(new Date(timestamp * 1000));

                _timestampTextView.setText("Tanggal dan Waktu (UTC): " + dateTime);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(MainActivity.this, new String(responseBody), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initSwipeRefreshLayout() {
        _swipeRefreshLayout1 = findViewById(R.id.swipeRefreshLayout1);

        _swipeRefreshLayout1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { bindRecyclerView(); }
        });
    }
}