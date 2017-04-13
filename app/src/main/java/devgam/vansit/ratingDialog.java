package devgam.vansit;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Users;

public class ratingDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    TextView userName, userRateDesc, priceRateDesc;
    RatingBar userRating, priceRating;
    Button addRateButton;
    Users tempUserDriver;
    String tempUserName;
    DatabaseReference DataBaseRoot;

    public ratingDialog(Activity activity) {
        super(activity);
        // Required empty public constructor
        this.c = activity;
     }

    public ratingDialog(Activity activity, Users userDriver){
        super(activity);
        // Required empty public constructor
        this.c = activity;

        this.tempUserDriver = userDriver;

        this.tempUserName = userDriver.getFirstName()+" "+userDriver.getLastName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_dialog);

        userName = (TextView) findViewById(R.id.rating_dialog_name_text);
        userRateDesc = (TextView) findViewById(R.id.rating_dialog_user_rate_desc);
        priceRateDesc = (TextView) findViewById(R.id.rating_dialog_price_rate_desc);
        userRating = (RatingBar) findViewById(R.id.rating_dialog_user_rate_bar);
        priceRating = (RatingBar) findViewById(R.id.rating_dialog_price_rate_bar);
        addRateButton = (Button) findViewById(R.id.rating_dialog_add_rate_btn);

        DataBaseRoot = FirebaseDatabase.getInstance().getReference();//connect to DB root

        userName.setText(tempUserName);

        userRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                userRateDesc.setVisibility(View.VISIBLE);
                userRateDesc.setText(Util.getRateDesc(c, 1, (int) userRating.getRating()));
            }
        });

        priceRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                priceRateDesc.setVisibility(View.VISIBLE);
                priceRateDesc.setText(Util.getRateDesc(c, 2, (int) priceRating.getRating()));
            }
        });
    }

    @Override
    public void onClick(View v){
        if(v == addRateButton) {


            if (!Util.isLogged()) // User is not logged
                return;

            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (tempUserDriver.getUserID().equals(firebaseAuth.getCurrentUser().getUid()))// User cant vote for self.
                return;


            DatabaseReference query = DataBaseRoot.child(Util.RDB_USERS + "/" + firebaseAuth.getCurrentUser().getUid() + "/" + Util.RATED_FOR);
            ValueEventListener VEL = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<String> ratedForList = (ArrayList<String>) dataSnapshot.getValue();
                    if (ratedForList == null)
                        Util.makeToast(getContext(), "Something wrong happened!");
                    else {
                        boolean canRate = true;
                        for (String value : ratedForList) {
                            if (value.equals(tempUserDriver.getUserID()))//User already voted for this driver
                                canRate = false;
                        }

                        if (canRate) {
                            ratedForList.add(tempUserDriver.getUserID());
                            commitRating(ratedForList, firebaseAuth.getCurrentUser().getUid());//apply rate and add this driver to User
                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }

    }

    void commitRating(ArrayList<String> updatedRatedForList , String userKey)
    {
        //current userKey so we can search and add the updatedRatedForList for User.
        if(priceRating.getRating() != 0)
        {
            //Log.v("Main","ratingPrice.getRating() != 0");
            float totalRating = tempUserDriver.getRatePrice() * tempUserDriver.getRatePriceCount();
            totalRating += priceRating.getRating();

            DataBaseRoot.child(Util.RDB_USERS)
                    .child(tempUserDriver.getUserID())
                    .child(Util.RATE_PRICE_COUNT)
                    .setValue(tempUserDriver.getRatePriceCount()+1);

            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.DOWN);

            DataBaseRoot.child(Util.RDB_USERS)
                    .child(tempUserDriver.getUserID())
                    .child(Util.RATE_PRICE)
                    .setValue(Float.parseFloat(df.format(totalRating/ (tempUserDriver.getRatePriceCount()+1))));
        }

        if(userRating.getRating() != 0)
        {
            //Log.v("Main","ratingService.getRating() != 0");
            float totalRating = tempUserDriver.getRateService() * tempUserDriver.getRateServiceCount();
            totalRating += userRating.getRating();

            DataBaseRoot.child(Util.RDB_USERS)
                    .child(tempUserDriver.getUserID())
                    .child(Util.RATE_SERVICE_COUNT)
                    .setValue(tempUserDriver.getRateServiceCount()+1);

            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.DOWN);
            DataBaseRoot.child(Util.RDB_USERS)
                    .child(tempUserDriver.getUserID())
                    .child(Util.RATE_SERVICE)
                    .setValue(Float.parseFloat(df.format(totalRating/ (tempUserDriver.getRateServiceCount()+1))));

        }


        DataBaseRoot.child(Util.RDB_USERS)
                .child(userKey)
                .child(Util.RATED_FOR)
                .setValue(updatedRatedForList);


    }
}
