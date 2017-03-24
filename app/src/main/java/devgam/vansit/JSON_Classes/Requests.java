package devgam.vansit.JSON_Classes;

import java.io.Serializable;

public class Requests implements Serializable
{
    private Users user;
    private Offers offer;

    private String address;
    private double longitude;
    private double latitude;

    private Long timeStamp;

    private boolean served;//true = served , false = not served

    // this wont be shown in the database, set it during list view adapter setter, to compare from far to near distance
    // must be string so we dont show it
    private String distanceFromRequestToUser;

    public Requests() {}

    public Requests(Users user, Offers offer,String address, double latitude, double longitude, Long timeStamp) {
        this.user = user;
        this.offer = offer;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
        this.served = Boolean.FALSE;// by default
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Offers getOffer() {
        return offer;
    }

    public void setOffer(Offers offer) {
        this.offer = offer;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isServed() {
        return served;
    }

    public void setServed(boolean served) {
        this.served = served;
    }

    public String getDistanceFromRequestToUser() {
        return distanceFromRequestToUser;
    }

    public void setDistanceFromRequestToUser(String distanceFromRequestToUser) {
        this.distanceFromRequestToUser = distanceFromRequestToUser;
    }

}
