package com.efrei.prj.quixkcd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComicActivity extends AppCompatActivity {

    private Context context;
    private DisplayImageOptions defaultOptions;
    private ImageLoaderConfiguration config;
    private DisplayImageOptions options;
    private static int comicFavorited;
    private static int comicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        options = setupImageLoader();
        setContentView(R.layout.activity_comic);
        TextView singleComicTitle = findViewById(R.id.singleComicTitle);
        ImageView singleComicImage = findViewById(R.id.singleComicImage);
        TextView singleComicAltText = findViewById(R.id.singleComicAltText);
        TextView singleComicDate = findViewById(R.id.singleComicDate);
        ImageButton favoriteButton = findViewById(R.id.favoriteButton);


        Intent intent = getIntent();
        comicId = intent.getIntExtra(MainActivity.ID, 1);
        String comicImageLink = intent.getStringExtra(MainActivity.IMAGE_LINK);
        String comicTitle = intent.getStringExtra(MainActivity.TITLE);
        String comicAltText = intent.getStringExtra(MainActivity.ALT_TEXT);
        String comicDate = intent.getStringExtra(MainActivity.DATE);
        comicFavorited = intent.getIntExtra(MainActivity.FAVORITED, 0);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(comicImageLink, singleComicImage, options);
        singleComicTitle.setText(comicTitle);
        singleComicAltText.setText(comicAltText);
        singleComicDate.setText(comicDate);

        if(comicFavorited == 0)
        {
            favoriteButton.setImageResource(R.drawable.not_favorited);
        }
        else
        {
            favoriteButton.setImageResource(R.drawable.favorited);
        }

        favoriteButton.setOnClickListener(v -> updateFavorites(comicId, comicFavorited, favoriteButton));
    }

    /*@Override
    protected void onStop()
    {
        super.onStop();
        Intent callBackIntent = new Intent(ComicActivity.this, MainActivity.class);
        callBackIntent.putExtra(MainActivity.ID, comicId);
        callBackIntent.putExtra(MainActivity.FAVORITED, comicFavorited);
    }*/

    private void updateFavorites(int comicId, int comicFavorited, ImageButton favoriteButton){
        ExecutorService execService = Executors.newSingleThreadExecutor();
        ComicDatabase comicDatabase = ComicDatabase.getInstance(getApplicationContext());
        ComicDao db = comicDatabase.comicDao();
        int duration = Toast.LENGTH_SHORT;
        if(comicFavorited == 0)
        {
            execService.execute(() -> db.setFavorite(comicId, 1));
            ComicActivity.comicFavorited = 1;
            favoriteButton.setImageResource(R.drawable.favorited);
            Toast toast = Toast.makeText(this.getApplicationContext(), "Comic added to Favorites", duration);
            toast.show();
        }
        else
        {
            execService.execute(() -> db.setFavorite(comicId, 0));
            ComicActivity.comicFavorited = 0;
            favoriteButton.setImageResource(R.drawable.not_favorited);
            Toast toast = Toast.makeText(this.getApplicationContext(), "Comic removed from Favorites", duration);
            toast.show();
        }
    }

    private DisplayImageOptions setupImageLoader()
    {
        defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        config = new ImageLoaderConfiguration.Builder(
                context)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache()).build();

        ImageLoader.getInstance().init(config);
        int fallback = context.getResources().getIdentifier("@drawable/image_failed", null, context.getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageForEmptyUri(fallback)
                .showImageOnFail(fallback)
                .showImageOnLoading(fallback)
                .resetViewBeforeLoading(true).build();
        return options;
    }
}
