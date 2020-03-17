package com.efrei.prj.quixkcd;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

class ComicListAdapter extends ArrayAdapter<Comic> {
    private Context context;
    private int resource;
    private DisplayImageOptions options;

    public ComicListAdapter(@NonNull Context context, int resource, @NonNull List<Comic> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        options = setupImageLoader();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ImageLoader imageLoader = ImageLoader.getInstance();

        int id = getItem(position).id;
        String title = getItem(position).title;
        String date = getItem(position).date;
        String imageLink = getItem(position).imageLink;
        String altText = getItem(position).altText;
        String transcript = getItem(position).transcript;

        Comic comic = new Comic(id, title, date, imageLink, altText, transcript);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        ImageView comicImage = convertView.findViewById(R.id.comicImage);
        TextView comicTitle = convertView.findViewById(R.id.comicTitle);

        Log.d("ComicListAdapter", "About to load " + comic.imageLink);
        imageLoader.displayImage(comic.imageLink, comicImage, options);
        Log.d("ComicListAdapter", comic.imageLink + " loaded");
        comicTitle.setText(comic.title);

        return convertView;
    }

    private DisplayImageOptions setupImageLoader()
    {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache()).build();

        ImageLoader.getInstance().init(config);
        int fallback = context.getResources().getIdentifier("@drawable/image_failed", null, context.getPackageName());
        return new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageForEmptyUri(fallback)
                .showImageOnFail(fallback)
                .showImageOnLoading(fallback)
                .resetViewBeforeLoading(true).build();
    }
}
