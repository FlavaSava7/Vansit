package devgam.vansit.JSON_Classes;

import org.json.JSONObject;

import java.lang.*;
import java.util.*;
import java.io.Serializable;
/**
 THIS WILL CONTAIN ALL THE DESCRIPTIONS OF A USER.

 - Class variables will be private , thus access data using SET and GET
 */
public class Users implements Serializable
{
    private String firstName;
    private String lastName;
    private String City;
    private String Phone;
    private String Gender;

    private String dateDay;
    private String dateMonth;
    private String dateYear;

    private float rateService;
    private int rateServiceCount;
    private float ratePrice;
    private int ratePriceCount;
    private ArrayList<String> RatedFor;//lists that contains IDs of who THIS user rated for.

    // The following variables will not be shown in the DataBase Users Object Just to get more Information
    private String userID;//this is used inside the list views so we can know if a certain obj already exists or not inside the list
    private String deviceToken;// for requests to communicate from users to drivers
    public Users()
    {
        //empty constructor for Firebase API
    }

    // we will use this after a new user is created in Auth. table
    public Users(String firstName,String lastName, String city, String phone, String gender,
                 String dateDay, String dateMonth, String dateYear)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.City = city;
        this.Phone = phone;
        this.Gender = gender;
        this.dateDay = dateDay;
        this.dateMonth = dateMonth;
        this.dateYear = dateYear;
        this.RatedFor = new ArrayList<>();
        this.RatedFor.add(".");// garbage value, just to show it in JSON
    }

    public int getRateServiceCount() {
        return rateServiceCount;
    }

    public int getRatePriceCount() {
        return ratePriceCount;
    }
    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getDateDay() {
        return dateDay;
    }

    public void setDateDay(String dateDay) {
        this.dateDay = dateDay;
    }

    public String getDateMonth() {
        return dateMonth;
    }

    public void setDateMonth(String dateMonth) {
        this.dateMonth = dateMonth;
    }

    public String getDateYear() {
        return dateYear;
    }

    public void setDateYear(String dateYear) {
        this.dateYear = dateYear;
    }

    public float getRateService() {
        return rateService;
    }

    public float getRatePrice() {
        return ratePrice;
    }

    public ArrayList<String> getRatedFor() {
        return RatedFor;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
