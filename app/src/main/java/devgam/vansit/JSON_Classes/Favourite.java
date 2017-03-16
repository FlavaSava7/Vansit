package devgam.vansit.JSON_Classes;

/**
 * we will fetch EACH offer the user favoured by using Offer Key ,
 * if they Offer returned null when we queried then delete this offer favourite from the user.
 */
public class Favourite
{

    private String offerKey;

    public Favourite()
    {
        //empty constructor for Firebase API
    }

    public Favourite(String offerKey) {
        this.offerKey = offerKey;
    }

    public String getOfferKey() {
        return offerKey;
    }

    public void setOfferKey(String offerKey) {
        this.offerKey = offerKey;
    }

}
