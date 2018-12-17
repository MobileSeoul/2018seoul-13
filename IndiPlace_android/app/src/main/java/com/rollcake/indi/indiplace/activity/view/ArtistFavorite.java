package com.rollcake.indi.indiplace.activity.view;

public class ArtistFavorite {

    private String pkValue;
    private String id;
    private String name;

    public ArtistFavorite(String pkValue , String id, String name) {
        this.pkValue = pkValue;
        this.id = id;
        this.name = name;
    }


    public String getPkValue() {
        return pkValue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
