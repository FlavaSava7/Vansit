package devgam.vansit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

public class OfferInfo extends Fragment {

    public Users userDriver = null; // the one who posted the offer
    public Offers userOffer = null; // the offer itself

    ImageView typeIcon;
    TextView Title,Description,Name, City,Phone, ratingNameService, ratingNamePrice;
    RatingBar ratingService, ratingPrice;


    public OfferInfo()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Log.v("Main","onCreate:");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //Log.v("Main","onCreateView:");
        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            userDriver = (Users) bundle.getSerializable("userDriver");
            userOffer = (Offers) bundle.getSerializable("userOffer");
        }
        return inflater.inflate(R.layout.fragment_offer_info, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(userDriver==null | userOffer==null)
        {
            Log.v("Main","userDriver OR userOffer = NULL");
            //SHOW ERRIR MSG ?
        }

        typeIcon = (ImageView) getActivity().findViewById(R.id.offerInfo_typeIcon);

        Title = (TextView) getActivity().findViewById(R.id.offerInfo_titleData);
        Description = (TextView) getActivity().findViewById(R.id.offerInfo_descData);
        Name = (TextView) getActivity().findViewById(R.id.offerInfo_nameData);
        City = (TextView) getActivity().findViewById(R.id.offerInfo_cityData);
        Phone = (TextView) getActivity().findViewById(R.id.offerInfo_phoneData);
        ratingNameService = (TextView) getActivity().findViewById(R.id.offerInfo_serviceRatingName);
        ratingNamePrice = (TextView) getActivity().findViewById(R.id.offerInfo_priceRatingName);

        ratingService = (RatingBar) getActivity().findViewById(R.id.offerInfo_serviceRatingData);
        ratingPrice = (RatingBar) getActivity().findViewById(R.id.offerInfo_priceRatingData);

        setUpInfo();
    }
    void setUpInfo()
    {

        switch(userOffer.getType())
        {
            case Util.RDB_CAR:typeIcon.setImageDrawable(getDrawableResource(R.drawable.car));break;
            case Util.RDB_BUS:typeIcon.setImageDrawable(getDrawableResource(R.drawable.bus));break;
            case Util.RDB_TAXI:typeIcon.setImageDrawable(getDrawableResource(R.drawable.taxi));break;
            case Util.RDB_TRUCK:typeIcon.setImageDrawable(getDrawableResource(R.drawable.truck));break;
        }

        Title.setText(userOffer.getTitle());
        Description.setText(userOffer.getDescription());
        Name.setText(userDriver.getName());
        City.setText(userOffer.getCity());
        Phone.setText(userDriver.getPhone());


        final String[] rating_service=getResources().getStringArray(R.array.rating_service);
        final String[] rating_price=getResources().getStringArray(R.array.rating_price);

        ratingService.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser)
            {
                if(fromUser)
                {

                    switch (((int) Math.floor(rating)))
                    {
                        case 1:ratingNameService.setText("("+rating_service[0]+")");break;
                        case 2:ratingNameService.setText("("+rating_service[1]+")");break;
                        case 3:ratingNameService.setText("("+rating_service[2]+")");break;
                        case 4:ratingNameService.setText("("+rating_service[3]+")");break;
                        case 5:ratingNameService.setText("("+rating_service[4]+")");break;
                    }
                }


            }
        });
        ratingPrice.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                if(fromUser)
                {

                    switch (((int) Math.floor(rating)))
                    {
                        case 1:ratingNamePrice.setText("("+rating_price[4]+")");break;
                        case 2:ratingNamePrice.setText("("+rating_price[3]+")");break;
                        case 3:ratingNamePrice.setText("("+rating_price[2]+")");break;
                        case 4:ratingNamePrice.setText("("+rating_price[1]+")");break;
                        case 5:ratingNamePrice.setText("("+rating_price[0]+")");break;
                    }
                }


            }
        });
    }
    private Drawable getDrawableResource(int resID)//used in list view to set icons to rows
    {
        return ContextCompat.getDrawable(getActivity().getApplicationContext(), resID);//context.compat checks the version implicitly
    }
}
