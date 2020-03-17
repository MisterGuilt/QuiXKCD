package com.efrei.prj.quixkcd;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            ComicDatabase comicDatabase = ComicDatabase.getInstance(this);
            ComicDao db = comicDatabase.comicDao();
            ExecutorService execService = Executors.newSingleThreadExecutor();
            List<Comic> searchResults = Collections.emptyList();
            try {
                searchResults = execService.submit(() -> db.searchComicTitles(query)).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            ListView searchResultsList = findViewById(R.id.searchResultsList);
            searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
                Intent displayIntent = new Intent(this.getApplicationContext(), ComicActivity.class);
                Comic selectedComic = (Comic) searchResultsList.getItemAtPosition(position);
                displayIntent.putExtra(MainActivity.ID, selectedComic.id);
                displayIntent.putExtra(MainActivity.IMAGE_LINK, selectedComic.imageLink);
                displayIntent.putExtra(MainActivity.TITLE, selectedComic.title);
                displayIntent.putExtra(MainActivity.ALT_TEXT, selectedComic.altText);
                displayIntent.putExtra(MainActivity.DATE, selectedComic.date);
                displayIntent.putExtra(MainActivity.FAVORITED, selectedComic.favorited);
                startActivity(displayIntent);
            });
            ComicListAdapter comicListAdapter = new ComicListAdapter(this, R.layout.comic_list_adapter_layout, searchResults);
            searchResultsList.setAdapter(comicListAdapter);
        }
    }

}
