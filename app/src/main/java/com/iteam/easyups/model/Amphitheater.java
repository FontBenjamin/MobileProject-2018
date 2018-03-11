package com.iteam.easyups.model;

public class Amphitheater {

    private String name;
    private Double latitude;
    private Double longitude;
    private String building;

    public Amphitheater() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getBuilding() { return building; }

    public void setBuilding(String building) { this.building = building; }

}
