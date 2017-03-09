package devgam.vansit.JSON_Classes;

/**
 * Created by Nimer Esam on 08/03/2017.
 */

public class Favorite {
    private String offerKey, offerCity;

    public Favorite(String offerKey, String offerCity) {
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
