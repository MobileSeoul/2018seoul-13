package com.rollcake.indi.indiplace.activity.view;

public class ArtistInfo {

    private String title;
    private String content;
    private String url;
    private String artistId;
    private String gener;

    public ArtistInfo(String title, String content, String url, String artistId, String gener) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.artistId = artistId;
        this.gener = gener;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getGener() {
        return gener;
    }
}
