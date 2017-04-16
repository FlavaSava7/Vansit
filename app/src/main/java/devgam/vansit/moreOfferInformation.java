package devgam.vansit;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Favourite;
import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

public class moreOfferInformation extends AppCompatActivity {

    public Users userDriver = null; // the one who posted the offer
    public Offers userOffer = null; // the offer itself

    DatabaseReference DataBaseRoot;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    TextView Title,Description,Name, City, Age, HomeCity, Type, ratingNameService, ratingNamePrice;
    RatingBar ratingService, ratingPrice;
    private LinearLayout callLayout, profileLayout, favLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_offer_information);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_offers);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("Bundle");
        if (bundle != null)
        {
            userDriver = (Users) bundle.getSerializable("userDriver");
            userOffer = (Offers) bundle.getSerializable("userOffer");
        }

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        if(userDriver==null | userOffer==null)
        {
            Log.v("Main","userDriver OR userOffer = NULL");
            //SHOW ERRIR MSG ?
        }

        DataBaseRoot = FirebaseDatabase.getInstance().getReference();//connect to DB root
        fragmentManager = getSupportFragmentManager();

        Title = (TextView) findViewById(R.id.offerInfo_titleData);
        Description = (TextView) findViewById(R.id.offerInfo_descData);
        Name = (TextView) findViewById(R.id.offerInfo_nameData);
        City = (TextView) findViewById(R.id.offerInfo_cityData);
        Type = (TextView) findViewById(R.id.offerInfo_typeData);
        Age = (TextView) findViewById(R.id.offerInfo_ageData);
        HomeCity = (TextView) findViewById(R.id.offerInfo_homeCityData);
        ratingNameService = (TextView) findViewById(R.id.offerInfo_serviceRatingName);
        ratingNamePrice = (TextView) findViewById(R.id.offerInfo_priceRatingName);

        callLayout = (LinearLayout) findViewById(R.id.offerInfo_callLayout);
        favLayout = (LinearLayout) findViewById(R.id.offerInfo_favLayout);
        profileLayout = (LinearLayout) findViewById(R.id.offerInfo_profileLayout);


        ratingService = (RatingBar) findViewById(R.id.offerInfo_serviceRatingData);
        ratingPrice = (RatingBar) findViewById(R.id.offerInfo_priceRatingData);


        SetUpInfo();
    }

    void SetUpInfo() {
        int age = Util.yearNow - Integer.parseInt(userDriver.getDateYear());
        age = (Util.monthNow > Integer.parseInt(userDriver.getDateMonth()) ? age : age - 1);

        Title.setText(userOffer.getTitle());
        Description.setText(userOffer.getDescription());
        Name.setText(userDriver.getFirstName()+" "+userDriver.getLastName());
        HomeCity.setText(userDriver.getCity());
        Age.setText("" + age );
        City.setText(userOffer.getCity());
        Type.setText(userOffer.getType());

        try {
            ratingService.setRating(Float.parseFloat(userDriver.getRateService() + ""));
            ratingPrice.setRating(Float.parseFloat(userDriver.getRatePrice() + ""));
        } catch (Exception e){

        }

        int userRate = Math.round(Float.parseFloat(userDriver.getRateService() + ""));
        int priceRate = Math.round(Float.parseFloat(userDriver.getRatePrice() + ""));

        ratingNameService.setText("("+userRate+") "+Util.getRateDesc(this, 1, userRate));
        ratingNamePrice.setText("("+priceRate+") "+Util.getRateDesc(this, 2, priceRate));


        callLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + userDriver.getPhone()));
                startActivity(intent);
            }
        });

        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(moreOfferInformation.this, moreUserInformation.class);
                intent.putExtra("userDriver", userDriver);
                startActivity(intent);
            }
        });

        favLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                addAFavorite();
            }
        });



    }

    private void addAFavorite()
    {
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_FAVOURITE
                +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<Favourite> favs = new ArrayList<>();
                Favourite offerToFavourite = new Favourite(userOffer.getOfferKey());
                DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference().child(Util.RDB_FAVOURITE
                        +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
                if(!dataSnapshot.exists())
                {
                    favs.add(offerToFavourite);
                    mRef2.setValue(favs);
                }else
                {
                    boolean toAdd = true;
                    for(DataSnapshot favObj : dataSnapshot.getChildren())
                    {
                        Favourite fav = favObj.getValue(Favourite.class);
                        favs.add(fav);
                    }

                    for(Favourite tempFav : favs)
                    {
                        if(tempFav.getOfferKey().equals(offerToFavourite.getOfferKey()))
                        {
                            toAdd= false;
                            break;
                        }
                    }
                    if(toAdd)
                    {
                        favs.add(offerToFavourite);
                        mRef2.setValue(favs);

                        Util.makeToast(getApplication(),getString(R.string.added_fav));
                    }
                    else
                    {
                        Util.makeToast(getApplication(),getString(R.string.added_fav_fail));
                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
