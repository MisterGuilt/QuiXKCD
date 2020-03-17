package com.efrei.prj.quixkcd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        ComicDatabase comicDatabase = ComicDatabase.getInstance(this);
        ComicDao db = comicDatabase.comicDao();
        ExecutorService execService = Executors.newSingleThreadExecutor();
        List<Comic> favoriteComics = Collections.emptyList();
        try {
            favoriteComics = execService.submit(() -> db.getFavorites()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        ListView favoritesList = findViewById(R.id.favoritesList);
        ComicListAdapter comicListAdapter = new ComicListAdapter(this, R.layout.comic_list_adapter_layout, favoriteComics);
        favoritesList.setAdapter(comicListAdapter);
        favoritesList.setOnItemClickListener((parent, view, position, id) -> {
            Intent displayIntent = new Intent(this.getApplicationContext(), ComicActivity.class);
            Comic selectedComic = (Comic) favoritesList.getItemAtPosition(position);
            displayIntent.putExtra(MainActivity.ID, selectedComic.id);
            displayIntent.putExtra(MainActivity.IMAGE_LINK, selectedComic.imageLink);
            displayIntent.putExtra(MainActivity.TITLE, selectedComic.title);
            displayIntent.putExtra(MainActivity.ALT_TEXT, selectedComic.altText);
            displayIntent.putExtra(MainActivity.DATE, selectedComic.date);
            displayIntent.putExtra(MainActivity.FAVORITED, selectedComic.favorited);
            startActivity(displayIntent);
        });
    }
}
