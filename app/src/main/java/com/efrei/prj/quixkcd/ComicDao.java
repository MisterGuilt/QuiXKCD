package com.efrei.prj.quixkcd;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ComicDao
{

    @Query("SELECT * FROM comic ORDER BY id DESC LIMIT 10")
    List<Comic> getRecentComics();

    @Query("SELECT * FROM comic WHERE title LIKE '%' || :query || '%'")
    List<Comic> searchComicTitles(String query);

    @Query("SELECT * FROM comic WHERE favorited = 1")
    List<Comic> getFavorites();

    @Query("UPDATE comic SET favorited = :value WHERE id = :id")
    void setFavorite(int id, int value);

    @Query("SELECT COUNT(id) FROM comic")
    int getComicCount();

    @Insert
    void insertComics(Comic... comics);
}
