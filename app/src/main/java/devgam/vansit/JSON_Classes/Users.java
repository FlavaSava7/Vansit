package devgam.vansit.JSON_Classes;


/**
 THIS WILL CONTAIN ALL THE DESCRIPTIONS OF A USER.

 - Class variables will be private , thus access data using SET and GET
 */
public class Users
{
    private String Name;
    private String City;
    private int Age;
    private long Phone;

    //

    public Users()
    {
        //empty cons for Firebase
    }
    public Users(String mName,String mCity,int mAge,long mPhone)
    {
        this.Name = mName;
        this.City = mCity;
        this.Age = mAge;
        this.Phone = mPhone;
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

    public int getAge() {
        return Age;
    }

    public void setAge(int age) {
        Age = age;
    }

    public long getPhone() {
        return Phone;
    }

    public void setPhone(long phone) {
        Phone = phone;
    }
}
