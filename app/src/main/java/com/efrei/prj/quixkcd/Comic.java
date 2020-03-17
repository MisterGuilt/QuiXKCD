package com.efrei.prj.quixkcd;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class Comic
{
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "image_link")
    public String imageLink;

    @ColumnInfo(name = "alt_text")
    public String altText;

    @ColumnInfo(name = "transcript")
    public String transcript;

    @ColumnInfo(name = "favorited")
    public int favorited;

    public Comic(int id, String title, String date, String imageLink, String altText, String transcript) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.imageLink = imageLink;
        this.altText = altText;
        this.transcript = transcript;
        this.favorited = 0;
    }

    public Comic(JSONObject comicJSON)
    {
        try
        {
            this.id = (int)comicJSON.get("num");
            this.title = (String)comicJSON.get("safe_title");
            this.date = comicJSON.get("year") + "/" +
                    comicJSON.get("month") + "/" +
                    comicJSON.get("day");
            this.imageLink = (String)comicJSON.get("img");
            this.altText = (String)comicJSON.get("alt");
            this.transcript = (String)comicJSON.get("transcript");
            this.favorited = 0;
        }
        catch(JSONException e)
        {
            Log.e("JSON parsing", e.getMessage());
        }
    }
}