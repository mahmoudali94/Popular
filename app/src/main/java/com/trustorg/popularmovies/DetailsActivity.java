package com.trustorg.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class DetailsActivity extends AppCompatActivity {


    Integer id;
    TextView title, user_rating, release_date, synopsis;
    ImageView poster_image;

    //Add your api key here
    String api_key = "9ad9f84da2f4df99aff2dd100c8f746b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView) findViewById(R.id.title);
        user_rating = (TextView) findViewById(R.id.user_rating);
        release_date = (TextView) findViewById(R.id.release_date);
        synopsis = (TextView) findViewById(R.id.synopsis);
        poster_image = (ImageView) findViewById(R.id.poster_image);

        Intent intent = getIntent();
        id = intent.getIntExtra("Movie ID", 0);

        FetchMovieDetails fetchMovieDetails = new FetchMovieDetails();
        fetchMovieDetails.execute();
    }

    public class FetchMovieDetails extends AsyncTask<Void, Void, Void> {

        String LOG_TAG = "FetchMovieDetails";
        String original_title, releaseDate, plotSynopsis, poster_path;
        Double ratings;

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;

            try {
                String base_url = "https://api.themoviedb.org/3/movie/";
                URL url = new URL(base_url + id + "?api_key=" + api_key);
                Log.d(LOG_TAG, "URL: " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.d(LOG_TAG, "JSON Parsed: " + movieJsonStr);

                JSONObject main = new JSONObject(movieJsonStr);
                original_title = main.getString("original_title");
                releaseDate = main.getString("release_date");
                ratings = main.getDouble("vote_average");
                plotSynopsis = main.getString("overview");
                poster_path = "http://image.tmdb.org/t/p/w185" + main.getString("poster_path");

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            title.setText(original_title);
            user_rating.setText("User Ratings: " + ratings);
            release_date.setText("Release Date: " + releaseDate);
            synopsis.setText(plotSynopsis);
            poster_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            poster_image.setPadding(8, 8, 8, 8);
            Picasso.get().load(poster_path).into(poster_image);
        }
    }
}
