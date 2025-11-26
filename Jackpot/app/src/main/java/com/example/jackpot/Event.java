package com.example.jackpot;
import android.util.Log;

import com.example.jackpot.ui.image.Image;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an Event. This is a central object in the architecture.
 */
public class Event implements Serializable {
    private String eventId;
    private String organizerId;
    private String name;
    private String description;
    private UserList waitingList;
    private UserList joinedList;
    private UserList invitedList;
    private String location;
    private Date date;
    private Double lat;
    private Double lng;
//    private com.google.android.libraries.places.api.model.Money price;
    private Double price;
    private int capacity;
    private Date regOpenAt;
    private Date regCloseAt;
    private String posterUri;
    private String qrCodeId;
//    private GeoPolicy geoPolicy;
    private boolean geoRequired;
    private String category;

    /**
     * Empty constructor for firebase.
     */
    public Event() {}

    /**
     * Constructor for an event.
     * @param eventId The id of the event
     * @param organizerId The id of the organizer
     * @param name The name of the event
     * @param description The description of the event
     * @param waitingList The waiting list of the event
     * @param joinedList The joined list of the event
     * @param invitedList The invited list of the event
     * @param location The location of the event
     * @param date The date of the event
     * @param lat The latitude of the event
     * @param lng The longitude of the event
     * @param price The price of the event
     * @param capacity The capacity of the event
     * @param regOpenAt The registration open date of the event
     * @param regCloseAt The registration close date of the event
     * @param posterUri The poster uri of the event
     * @param qrCodeId The qr code id of the event
     * @param geoRequired The geo required of the event
     * @param category The category of the event
     */
    public Event(String eventId, String organizerId, String name, String description,
                 UserList waitingList, UserList joinedList, UserList invitedList, String location, Date date, Double lat,
                 Double lng, Double price, int capacity, Date regOpenAt,
                 Date regCloseAt, String posterUri, String qrCodeId, boolean geoRequired, String category){
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.name = name;
        this.description = description;
        this.waitingList = waitingList;
        this.joinedList = joinedList;
        this.invitedList = invitedList;
        this.location = location;
        this.date = date;
        this.lat = lat;
        this.lng = lng;
        this.price = price;
        this.capacity = capacity;
        this.regOpenAt = regOpenAt;
        this.regCloseAt = regCloseAt;
        this.posterUri = posterUri;
        this.qrCodeId = qrCodeId;
        this.geoRequired = geoRequired;
        this.category = category;
    }

    /**
     * Gets the id of the event.
     *
     * @return The id of the event.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the id of the event.
     *
     * @param eventId The id to set.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the id of the organizer.
     *
     * @return The id of the organizer.
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Sets the id of the organizer.
     *
     * @param organizerId The id to set.
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Gets the name of the event.
     *
     * @return The name of the event.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the event.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the event.
     *
     * @return The description of the event.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the waiting list of the event.
     *
     * @return The waiting list of the event.
     */
    public UserList getWaitingList() {
        return waitingList;
    }

    /**
     * Sets the waiting list of the event.
     *
     * @param waitingList The waiting list to set.
     */
    public void setWaitingList(UserList waitingList) {
        this.waitingList = waitingList;
    }

    /**
     * Gets the joined list of the event.
     *
     * @return The joined list of the event.
     */
    public UserList getJoinedList() {
        return joinedList;
    }

    /**
     * Sets the joined list of the event.
     *
     * @param joinedList The joined list to set.
     */
    public void setJoinedList(UserList joinedList) {
        this.joinedList = joinedList;
    }

    /**
     * Gets the invited list of the event.
     * @return The invited list of the event.
     */
    public UserList getInvitedList() {
        return invitedList;
    }

    /**
     * Sets the invited list of the event.
     * @param invitedList
     */
    public void setInvitedList(UserList invitedList) {
        this.invitedList = invitedList;
    }

    /**
     * Gets the location of the event.
     *
     * @return The location of the event.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the event.
     *
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the date of the event.
     *
     * @return The date of the event.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the latitude of the event
     * @return the latitude
     */
    public Double getLat() {
        return lat;
    }

    /**
     * Sets the latitude of the event
     *
     * @param lat The latitude to set
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * Gets the longitude of the event
     *
     * @return the longitude
     */
    public Double getLng() {
        return lng;
    }

    /**
     * Sets the longitude of the event
     *
     * @param lng The longitude to set
     */
    public void setLng(Double lng) {
        this.lng = lng;
    }

    /**
     * Gets the price of the event
     *
     * @return the price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Sets the price of the event
     *
     * @param price The price to set
     */
    public void setPrice(Double price) {
        this.price = price;
    }
    /**
     * Gets the capacity of the event
     *
     * @return the capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the capacity of the event
     *
     * @param capacity The capacity to set
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Gets the registration open date of the event.
     *
     * @return The registration open date of the event.
     */
    public Date getRegOpenAt() {
        return regOpenAt;
    }

    /**
     * Sets the date of the event
     * @param date The date to set
     */
    public void setDate(Object date) {
        this.date = convertToDate(date);
    }

    /**
     * Sets the registration open date of the event.
     *
     * @param regOpenAt The date to set
     */
    public void setRegOpenAt(Object regOpenAt) {
        this.regOpenAt = convertToDate(regOpenAt);
    }

    /**
     * Sets the registration close date of the event
     * @param regCloseAt The date to set.
     */
    public void setRegCloseAt(Object regCloseAt) {
        this.regCloseAt = convertToDate(regCloseAt);
    }

    /**
     * Converts a date object to a Date object.
     * @param dateObj The date object to convert.
     * @return The converted Date object.
     */
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
                    new SimpleDateFormat("MMM d, yyyy", Locale.US),
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

    /**
     * Gets the poster uri of the event.
     *
     * @return The poster uri of the event.
     */
    public String getPosterUri() {
        return posterUri;
    }

    /**
     * Sets the poster uri of the event.
     *
     * @param posterUri The poster uri to set.
     */
    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    /**
     * Gets the qr code id of the event.
     *
     * @return The qr code id of the event.
     */
    public String getQrCodeId() {
        return qrCodeId;
    }

    /**
     * Gets the registration close date of the event.
     *
     * @return The registration close date of the event.
     */
    public Date getRegCloseAt() {
        return regCloseAt;
    }

    /**
     * Sets the qr code id of the event.
     *
     * @param qrCodeId The qr code id to set.
     */
    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    /**
     * Gets the geo policy of the event.
     *
     * @return The geo policy of the event (true or false)
     */
    public boolean isGeoRequired() {
        return geoRequired;
    }

    /**
     * Sets the geo policy of the event.
     *
     * @param geoRequired The geopolicy to set (true or false)
     */
    public void setGeoRequired(boolean geoRequired) {
        this.geoRequired = geoRequired;
    }
    /**
     * Gets the category of the event.
     *
     * @return The category of the event.
     */
    public String getCategory() {
        return category;
    }
    /**
     * Sets the category of the event.
     *
     * @param category The category to set.
     */
    public void setCategory(String category) {
        this.category = category;
    }
//    public void schedule(List<Event> events) {
//        for(Event event : events){
//
//        }
//    }

    /**
     * Sets the registration window of the event.
     * @param openAt The date and time the event opens
     * @param closeAt The date and time the event closes
     */
    public void setRegistrationWindow(Instant openAt, Instant closeAt){

    }

    /**
     * Attaches a poster to the event.
     * @param posterImage The poster to attach.
     */
    public void attachPoster(Image posterImage) {

    }

    /**
     * Attaches a QR code to the event.
     * @param qrCodeId The QR code to attach.
     */
    public void attachQRCode(UUID qrCodeId){

    }

    /**
     * Removes the poster from the event.
     */
    public void removePoster() {

    }

    /**
     * Toggles geo requirement.
     */
    public void toggleGeoRequirement() {

    }

    /**
     * Checks if the registration window is open.
     * @param now The current time.
     * @return True if the registration window is open, false otherwise.
     */
    public boolean isRegistrationOpen(Instant now) {
        return true;
    }

    /**
     * Checks if there are any remaining seats.
     * @return The number of seats.
     */
    public int remainingSeats() {
        return 0;
    }

    /**
     * Returns the number of entrants.
     * @return The number of entrants.
     */
    public int totalEntrants() {
        return 0;
    }

    /**
     * Checks if an entrant is in the waiting list.
     * @param entrant The entrant to check.
     * @return True if the entrant is in the waiting list, false otherwise.
     */
    public boolean entrantInWaitingList(Entrant entrant) {
        //Loop through the waiting list and see if an identical entrant is in there
        for (int i = 0; i < waitingList.size(); i++) {
            if (Objects.equals(waitingList.get(i).getId(), entrant.getId())) {
                return true;
            }
        }
        return false;
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
        if (waitingList.getCapacity()== 0 || waitingList.size() < waitingList.getCapacity()) {
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

    /**
     * Checks if an entrant is in the list.
     * @param entrant The entrant to check.
     * @param list The list to check.
     * @return True if the entrant is in the joined list, false otherwise.
     */
    public boolean entrantInList(Entrant entrant, UserList list) {
        return list.contains(entrant);
    }
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
