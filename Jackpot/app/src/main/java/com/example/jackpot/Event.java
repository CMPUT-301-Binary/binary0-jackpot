package com.example.jackpot;
import android.util.Log;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Event {
    private String eventId;
    private String organizerId;
    private String name;
    private String description;
    private UserList waitingList;
    private String location;
    private Date date;
    private Double lat;
    private Double lng;
//    private com.google.android.libraries.places.api.model.Money price;
    private Double price;
    private int capacity;
    private Date regOpenAt;
    private Date regCloseAt;
    private Image posterImage;
    private String qrCodeId;
//    private GeoPolicy geoPolicy;
    private boolean geoRequired;
    private String category;
    public Event() {}

    public Event(String eventId, String organizerId, String name, String description,
                 UserList waitingList, String location, Date date, Double lat,
                 Double lng, Double price, int capacity, Date regOpenAt,
                 Date regCloseAt, Image posterImage, String qrCodeId, boolean geoRequired, String category){
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.name = name;
        this.description = description;
        this.waitingList = waitingList;
        this.location = location;
        this.date = date;
        this.lat = lat;
        this.lng = lng;
        this.price = price;
        this.capacity = capacity;
        this.regOpenAt = regOpenAt;
        this.regCloseAt = regCloseAt;
        this.posterImage = posterImage;
        this.qrCodeId = qrCodeId;
        this.geoRequired = geoRequired;
        this.category = category;
    }
    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public String getOrganizerId() {
        return organizerId;
    }
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public UserList getWaitingList() {
        return waitingList;
    }
    public void setWaitingList(UserList waitingList) {
        this.waitingList = waitingList;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public Date getDate() {
        return date;
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
    public Date getRegOpenAt() {
        return regOpenAt;
    }
    public void setDate(Object date) {
        this.date = convertToDate(date);
    }

    public void setRegOpenAt(Object regOpenAt) {
        this.regOpenAt = convertToDate(regOpenAt);
    }

    public void setRegCloseAt(Object regCloseAt) {
        this.regCloseAt = convertToDate(regCloseAt);
    }

    private Date convertToDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }

        if (dateObj instanceof Date) {
            return (Date) dateObj;
        } else if (dateObj instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) dateObj).toDate();
        } else if (dateObj instanceof Map) {
            // Firestore Timestamp as Map
            Map<String, Object> timestampMap = (Map<String, Object>) dateObj;
            if (timestampMap.containsKey("seconds")) {
                long seconds = ((Number) timestampMap.get("seconds")).longValue();
                int nanoseconds = timestampMap.containsKey("nanoseconds")
                        ? ((Number) timestampMap.get("nanoseconds")).intValue()
                        : 0;
                return new Date(seconds * 1000 + nanoseconds / 1000000);
            }
        } else if (dateObj instanceof String) {
            String dateStr = (String) dateObj;
            SimpleDateFormat[] formats = {
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US),
                    new SimpleDateFormat("MM/dd/yyyy", Locale.US)
            };

            for (SimpleDateFormat format : formats) {
                try {
                    return format.parse(dateStr);
                } catch (ParseException e) {
                    // Try next format
                }
            }

            Log.e("Event", "Failed to parse date string: " + dateStr);
            return null;
        } else if (dateObj instanceof Long) {
            return new Date((Long) dateObj);
        }

        return null;
    }
    public Image getPosterImage() {
        return posterImage;
    }
    public void setPosterImage(Image posterImage) {
        this.posterImage = posterImage;
    }
    public String getQrCodeId() {
        return qrCodeId;
    }
    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }
    public boolean isGeoRequired() {
        return geoRequired;
    }
    public void setGeoRequired(boolean geoRequired) {
        this.geoRequired = geoRequired;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void schedule(List<Event> events) {
//        for(Event event : events){
//
//        }
    }
    public void setRegistrationWindow(Instant openAt, Instant closeAt){

    }
    public void attachPoster(Image posterImage) {

    }
    public void attachQRCode(UUID qrCodeId){

    }
    public void removePoster() {

    }
    public void toggleGeoRequirement() {

    }
    public boolean isRegistrationOpen(Instant now) {
        return true;
    }
    public int remainingSeats() {
        return 0;
    }
    public int totalEntrants() {
        return 0;
    }
    public boolean hasEntrant(Entrant entrant) {
        return waitingList.contains(entrant);
    }

    /**
     * Adds an entrant to the waiting list.
     * @param entrant The entrant to add to the waiting list.
     * @throws NullPointerException If the waiting list is null.
     * @throws IllegalStateException If the waiting list is full.
     * @throws IllegalArgumentException If the entrant is null.
     */
    public void addEntrantWaitingList(Entrant entrant) {
        if (entrant == null) {
            throw new IllegalArgumentException("Entrant is null");
        }
        if (waitingList == null) {
            throw  new NullPointerException("Waiting list is null");
        }
        if (waitingList.getCapacity()==null || waitingList.size() < waitingList.getCapacity()) {
            waitingList.add(entrant);
        } else {
            throw new IllegalStateException("Waiting list is full");
        }
    }

    /**
     * Removes an entrant from the waiting list.
     * @param entrant The entrant to remove from the waiting list.
     * @throws NullPointerException If the waiting list is null.
     * @throws IllegalArgumentException If the entrant is null.
     */
    public void removeEntrantWaitingList(Entrant entrant) {
        if (entrant == null) {
            throw new IllegalArgumentException("Entrant is null");
        }
        if (waitingList == null) {
            throw  new NullPointerException("Waiting list is null");
        } else {
            waitingList.remove(entrant);
        }
    }
//    public void recordInvitation(Notification invitation) {
//
//    }
//    public void recordEnrolment(Notification enrollment) {
//
//    }
    /**
     * Cancels an enrollment.
     * @param enrollment The enrollment to cancel.
     * @param reason The reason for the cancellation.
     * @throws IllegalArgumentException If the enrollment is null.
     */
    public Notification cancelEnrollment(Notification enrollment, String reason) {
        if (enrollment == null) {
            throw new IllegalArgumentException("enrollment is null");
        }
        return new Notification(enrollment.getRecipientID(), enrollment.getEventID(), "Cancellation", reason);
    }

    /**
     * Draws a random sample of entrants from the waiting list.
     * @param amountEntrants The number of entrants to draw.
     * @throws IllegalArgumentException If there are not enough entrants in the waiting list.
     * @return A list of entrants.
     */
    public ArrayList<User> drawEvent(int amountEntrants){
        if(amountEntrants>waitingList.size()){
            throw new IllegalArgumentException("Too few entrants in waiting list");
        }
        ArrayList<User> list = new ArrayList<>();
        for(int i = 0; i < amountEntrants; i++){
            int index = (int)(Math.random()*waitingList.size());
            User e = waitingList.get(index);
            list.add(e);
            waitingList.remove(e);
        }
        return list;
    }
//    public FinalRef exportFinalCSV(){
//        return new FinalRef();
//    }
}
