package devgam.vansit.JSON_Classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Requests implements Serializable
{
    private Users user;

    private String city;
    private String title;
    private String description;
    private String type;

    private String address;
    private double longitude;
    private double latitude;

    private Long timeStamp;

    private boolean served;//true = served , false = not served

    private String deviceToken;

    private ArrayList<Users> serveDrivers;
    // this wont be shown in the database, set it during list view adapter setter, to compare from far to near distance
    // must be string so we dont show it
    private String distanceFromRequestToUser;

    public Requests() {}

    public Requests(Users user
                    ,String title,String desc,String type,String city
                    ,String address, double latitude, double longitude
                    ,Long timeStamp, String token)
    {
        this.user = user;
        this.title = title;
        this.description = desc;
        this.type = type;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
        this.deviceToken = token;
        this.served = Boolean.FALSE;// by default
        this.serveDrivers = new ArrayList<>();
        //this.serveDrivers.add(new Users());// garbage value, just to show it in JSON
    }

    public ArrayList<Users> getServeDrivers() {
        return serveDrivers;
    }

    public void setServeDrivers(ArrayList<Users> serveDrivers) {
        this.serveDrivers = serveDrivers;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
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

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
