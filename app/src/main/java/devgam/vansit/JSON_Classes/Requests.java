package devgam.vansit.JSON_Classes;

public class Requests
{
    private Users user;
    private Offers offer;
    private double longitude;
    private double latitude;


    private Long timeStamp;

    public Requests()
    {

    }

    public Requests(Users user, Offers offer, double longitude, double latitude, Long timeStamp ) {
        this.user = user;
        this.offer = offer;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timeStamp = timeStamp;

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

}
