package devgam.vansit.JSON_Classes;

/**
 * Created by Nimer Esam on 08/03/2017.
 */

public class Favourite {
    private String offerKey, offerCity;

    public Favourite(String offerKey, String offerCity) {
        this.offerKey = offerKey;
        this.offerCity = offerCity;
    }

    public String getOfferKey() {
        return offerKey;
    }

    public String getOfferCity() {
        return offerCity;
    }
}
