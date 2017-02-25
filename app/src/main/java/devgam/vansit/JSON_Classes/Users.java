package devgam.vansit.JSON_Classes;

import java.lang.*;
import java.util.*;

/**
 THIS WILL CONTAIN ALL THE DESCRIPTIONS OF A USER.

 - Class variables will be private , thus access data using SET and GET
 */
public class Users
{
    private String Name;
    private String City;
    private long Phone;
    private String Gender;
    // it contains the Authentication ID from Authentication table,
    // must be set AFTER we create a record in Authentication table then get the ID then create a User.
    private String AuthID;

    private String dateDay;
    private String dateMonth;
    private String dateYear;

    private int rateServiceCount;
    private int rateService;
    private int ratePrice;
    private int ratePriceCount;
    private List<String> RatedFor;//lists that contains IDs of who THIS user rated for.

    public Users()
    {
        //empty constructor for Firebase API
    }

    // we will use this after a new user is created in Auth. table
    public Users(String name, String city, long phone, String gender, String authID, String dateDay, String dateMonth, String dateYear, String userKey)
    {

        this.Name = name;
        this.City = city;
        this.Phone = phone;
        this.Gender = gender;
        this.AuthID = authID;
        this.dateDay = dateDay;
        this.dateMonth = dateMonth;
        this.dateYear = dateYear;

        this.RatedFor = new ArrayList<>();
        this.RatedFor.add(userKey);// so the user cant rate himself
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
    public String getAuthID() {
        return AuthID;
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

    public int getRateService() {
        return rateService;
    }

    public int getRatePrice() {
        return ratePrice;
    }

    public List<String> getRatedFor() {
        return RatedFor;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public long getPhone() {
        return Phone;
    }

    public void setPhone(long phone) {
        Phone = phone;
    }
}
