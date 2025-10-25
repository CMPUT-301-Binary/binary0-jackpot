package com.example.jackpot;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Event {
    private UUID eventId;
    private UUID organizerId;
    private String title;
    private String description;
    private String locationAddress;
    private Double lat;
    private Double lng;
//    private com.google.android.libraries.places.api.model.Money price;
    private Double price;
    private int capacity;
    private Instant regOpenAt;
    private Instant regCloseAt;
    private UUID posterImageId;
    private UUID qrCodeId;
//    private GeoPolicy geoPolicy;
    private boolean geoRequired;
    public UUID getEventId() {
        return eventId;
    }
    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }
    public UUID getOrganizerId() {
        return organizerId;
    }
    public void setOrganizerId(UUID organizerId) {
        this.organizerId = organizerId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getLocationAddress() {
        return locationAddress;
    }
    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
    public Double getLat() {
        return lat;
    }
    public void setLat(Double lat) {
        this.lat = lat;
    }
    public Double getLng() {
        return lng;
    }
    public void setLng(Double lng) {
        this.lng = lng;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public Instant getRegOpenAt() {
        return regOpenAt;
    }
    public void setRegOpenAt(Instant regOpenAt) {
        this.regOpenAt = regOpenAt;
    }
    public Instant getRegCloseAt() {
        return regCloseAt;
    }
    public void setRegCloseAt(Instant regCloseAt) {
        this.regCloseAt = regCloseAt;
    }
    public UUID getPosterImageId() {
        return posterImageId;
    }
    public void setPosterImageId(UUID posterImageId) {
        this.posterImageId = posterImageId;
    }
    public UUID getQrCodeId() {
        return qrCodeId;
    }
    public void setQrCodeId(UUID qrCodeId) {
        this.qrCodeId = qrCodeId;
    }
    public boolean isGeoRequired() {
        return geoRequired;
    }
    public void setGeoRequired(boolean geoRequired) {
        this.geoRequired = geoRequired;
    }
    public schedule(List<Event> events){
//        for(Event event : events){
//
//        }
    }
}
