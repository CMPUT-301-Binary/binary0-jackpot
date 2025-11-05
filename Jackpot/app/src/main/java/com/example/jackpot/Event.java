package com.example.jackpot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Event {
    private UUID eventId;
    private UUID organizerId;
    private String title;
    private String description;
    private EntrantList waitingList;
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
    public Event(UUID organizerId, String title, String description,
                 EntrantList waitingList, String locationAddress, Double lat,
                 Double lng, Double price, int capacity, Instant regOpenAt,
                 Instant regCloseAt, UUID posterImageId, UUID qrCodeId, boolean geoRequired){
        this.eventId = UUID.randomUUID();
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.waitingList = waitingList;
        this.locationAddress = locationAddress;
        this.lat = lat;
        this.lng = lng;
        this.price = price;
        this.capacity = capacity;
        this.regOpenAt = regOpenAt;
        this.regCloseAt = regCloseAt;
        this.posterImageId = posterImageId;
        this.qrCodeId = qrCodeId;
        this.geoRequired = geoRequired;
    }
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
    public EntrantList getWaitingList() {
        return waitingList;
    }
    public void setWaitingList(EntrantList waitingList) {
        this.waitingList = waitingList;
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
    public ArrayList<Entrant> drawEvent(int amountEntrants){
        if(amountEntrants>waitingList.size()){
            throw new IllegalArgumentException("Too few entrants in waiting list");
        }
        ArrayList<Entrant> list = new ArrayList<>();
        for(int i = 0; i < amountEntrants; i++){
            int index = (int)(Math.random()*waitingList.size());
            Entrant e = waitingList.get(index);
            list.add(e);
            waitingList.remove(e);
        }
        return list;
    }
//    public FinalRef exportFinalCSV(){
//        return new FinalRef();
//    }
}
