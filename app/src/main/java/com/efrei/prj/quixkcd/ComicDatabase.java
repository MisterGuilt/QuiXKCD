package com.efrei.prj.quixkcd;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Comic.class}, version = 1, exportSchema = false)
public abstract class ComicDatabase extends RoomDatabase
{
    private static ComicDatabase instance;

    public static synchronized ComicDatabase getInstance(Context context)
    {
        if(instance == null)
        {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    ComicDatabase.class, "comics").build();
        }
        return instance;
    }

    public abstract ComicDao comicDao();
}
