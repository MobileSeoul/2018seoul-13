package com.rollcake.indi.indiplace.activity.view;

public class ConcertPlaceInfo {

    private String image;
    private String name;
    private String content;
    private String gu;
    private String address;
    private double lat;
    private double loc;

    public ConcertPlaceInfo(String name, String content, String image, String address, String gu, double lat, double loc) {
        this.image = image;
        this.name = name;
        this.content = content;
        this.gu = gu;
        this.address = address;
        this.lat = lat;
        this.loc = loc;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getGu() {
        return gu;
    }

    public double getLat() {
        return lat;
    }

    public double getLoc() {
        return loc;
    }

    public String getAddress() {
        return address;
    }
}
