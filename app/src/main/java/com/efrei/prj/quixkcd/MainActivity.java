package com.efrei.prj.quixkcd;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static final String ID = "id";
    public static final String IMAGE_LINK = "imageLink";
    public static final String TITLE = "title";
    public static final String ALT_TEXT = "altText";
    public static final String DATE = "date";
    public static final String FAVORITED = "favorited";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ComicDatabase comicDatabase = ComicDatabase.getInstance(getApplicationContext());
        ComicDao db = comicDatabase.comicDao();
        ListView recentComicList = findViewById(R.id.recentComicList);
        //Open the comic that was tapped in another activity
        recentComicList.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, ComicActivity.class);
            Comic selectedComic = (Comic) recentComicList.getItemAtPosition(position);
            intent.putExtra(ID, selectedComic.id);
            intent.putExtra(IMAGE_LINK, selectedComic.imageLink);
            intent.putExtra(TITLE, selectedComic.title);
            intent.putExtra(ALT_TEXT, selectedComic.altText);
            intent.putExtra(DATE, selectedComic.date);
            intent.putExtra(FAVORITED, selectedComic.favorited);
            startActivity(intent);
        });
        //Open the favorites menu if the link in the menu is tapped
        toolbar.setOnMenuItemClickListener((i) -> {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
            return true;
        });
        List<Comic> recentComics = Collections.emptyList();
        try
        {
            //Executor required to access the database outside the UI thread
            ExecutorService execService = Executors.newSingleThreadExecutor();
            fetchComicJSON(db);
            recentComics = execService.submit(() -> db.getRecentComics()).get();
        }
        catch(ExecutionException | InterruptedException e)
        {
            Log.e(null, "Error: " + e.getMessage());
        }
        ComicListAdapter comicListAdapter = new ComicListAdapter(this, R.layout.comic_list_adapter_layout, recentComics);
        recentComicList.setAdapter(comicListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        //Link the search bar to the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), SearchActivity.class)));
        searchView.setSubmitButtonEnabled(true);

        return true;
    }

    private void fetchComicJSON(ComicDao db)
    {
        int duration = Toast.LENGTH_SHORT;
        ExecutorService execService = Executors.newSingleThreadExecutor();
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        try {
            int mostRecentComicId = execService.submit(() -> fetchMostRecentComicId()).get();
            int comicCount = execService.submit(() -> db.getComicCount()).get();
            if(mostRecentComicId - 1 != comicCount) //-1 because 404 does not exist
            {
                Toast toast = Toast.makeText(MainActivity.this, "Updating comics...", duration);
                toast.show();
                for(int i = comicCount + 1; i <= mostRecentComicId; i++)
                {
                    if(i == 404) continue; //because comic 404 does not exist
                    String url = "https://xkcd.com/" + i + "/info.0.json";
                    StringRequest comicRequest = makeComicRequest(db, url);
                    queue.add(comicRequest);
                }
                queue.start();
            }
            else
            {
                Toast toast = Toast.makeText(MainActivity.this, "Comic database up to date", duration);
                toast.show();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void insertNewComic(ComicDao db, String json)
    {
        ExecutorService execService = Executors.newSingleThreadExecutor();
        try
        {
            JSONObject obj = new JSONObject(json);
            Comic com = new Comic(obj);
            Log.d("Insert comic", "ID: " + com.id);
            execService.execute(() -> db.insertComics(com));
        }
        catch(JSONException e)
        {
            Log.e("JSON", e.getMessage());
        }
    }

    private int fetchMostRecentComicId()
    {
        JSONObject obj = null;
        try {
            //We need a synchronous connection to check for updates, which is why Volley is not used here
            URL recentUrl = new URL("https://xkcd.com/info.0.json");
            HttpURLConnection urlConnection = (HttpURLConnection) recentUrl.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String json = readStream(in);
            obj = new JSONObject(json);
            urlConnection.disconnect();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        Comic com = new Comic(obj);
        Log.d("Fetch Recent Comic ID", "fetched ID " + com.id);
        return com.id;
    }

    private StringRequest makeComicRequest(ComicDao db, String url)
    {
        return new StringRequest(url,
                response -> {
                    Log.d("Fetch OK", response);
                    insertNewComic(db, response);
                }, volleyError -> Log.e("Volley Comic Request", volleyError.getMessage()));
    }

    //This method translates an input stream into a string
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
