package devgam.vansit.JSON_Classes;

/**
 THIS CLASS WILL CONTAIN ALL OFFER DATA TO SEND TO THE DATA BASE

 - Class variables will be private , thus access data using SET and GET
 */

public class Offers
{
    private String User_ID;
    private String Title;
    private String Description;
    private String Type;

    //added by Nimer for my offers activity
    private String City;

    // The following variables will not be shown in the DataBase Offers Object Just to get more Information
    private String offerKey;//this is used inside the list views so we can know if a certain obj already exists or not inside the list


    public Offers()
    {
        //empty cons for Firebase

    }
    public Offers(String mUserID, String mTitle,String mDesc,String mType)
    {
        this.User_ID = mUserID;
        this.Title = mTitle;
        this.Description = mDesc;
        this.Type = mType;

    }

    //constructor added by Nimer for my offers activity
    public Offers(String title, String city) {
        Title = title;
        City = city;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getUser_ID() {
        return User_ID;
    }

    public void setUser_ID(String user_ID) {
        User_ID = user_ID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getOfferKey()
    {
        return offerKey;
    }

    public void setOfferKey(String offerKey) {
        this.offerKey = offerKey;
    }
}
