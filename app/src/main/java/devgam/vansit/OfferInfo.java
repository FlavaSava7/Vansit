package devgam.vansit;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

public class OfferInfo extends Fragment {

    public Users userDriver = null; // the one who posted the offer
    public Offers userOffer = null; // the offer itself

    DatabaseReference DataBaseRoot;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    ImageView typeIcon;
    TextView Title,Description,Name, City,Phone, ratingNameService, ratingNamePrice;
    RatingBar ratingService, ratingPrice;
    Button Commit;

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

        DataBaseRoot = FirebaseDatabase.getInstance().getReference();//connect to DB root
        fragmentManager = getActivity().getSupportFragmentManager();

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

        Commit = (Button) getActivity().findViewById(R.id.offerInfo_commitRating);

        SetUpInfo();
    }
    void SetUpInfo()
    {
        typeIcon.setImageDrawable(Util.getDrawableResource(getActivity(), Util.changeIcon(userOffer.getType())));

        Title.setText(userOffer.getTitle());
        Description.setText(userOffer.getDescription());
        Name.setText(userDriver.getFirstName()+" "+userDriver.getLastName());
        Name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userInformation user = new userInformation(getActivity(),userDriver, fragmentManager);
                user.show();
            }
        });

        City.setText(userOffer.getCity());
        Phone.setText(userDriver.getPhone());
        Phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+Phone.getText().toString()));
                startActivity(intent);
            }
        });
        Phone.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Phone Number", Phone.getText());
                clipboard.setPrimaryClip(clip);

                Util.makeToast(getContext(),"Copied To Clipboard");

                return true;
            }
        });


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


        Commit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(getContext())
                        .setTitle("want?")
                        .setMessage("sure?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {

                                if(!Util.isLogged()) // user is not logged
                                    return ;

                                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                if(userDriver.getUserID().equals(firebaseAuth.getCurrentUser().getUid()))// user cant vote for self.
                                    return ;



                                DatabaseReference query = DataBaseRoot.child(Util.RDB_USERS+"/"+firebaseAuth.getCurrentUser().getUid()+"/"+Util.RATED_FOR);
                                ValueEventListener VEL = new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        ArrayList<String> ratedForList = (ArrayList<String>) dataSnapshot.getValue();
                                        //Log.v("Main",""+p3.toString());
                                        if( ratedForList == null )
                                            Util.makeToast(getContext(),"Something wrong happened!");
                                        else
                                        {
                                            boolean canRate=true;
                                            for(String value : ratedForList)
                                            {
                                                if(value.equals(userDriver.getUserID()))//user already voted for this driver
                                                    canRate = false;
                                            }

                                            if(canRate)
                                            {
                                                ratedForList.add(userDriver.getUserID());

                                                commitRating(ratedForList,firebaseAuth.getCurrentUser().getUid() );//apply rate and add this driver to user

                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                };
                                query.addListenerForSingleValueEvent(VEL);

                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }
    void commitRating(ArrayList<String> updatedRatedForList , String userKey)
    {
        //current userKey so we can search and add the updatedRatedForList for user.
        if(ratingPrice.getRating() != 0)
        {
            //Log.v("Main","ratingPrice.getRating() != 0");
            float totalRating = userDriver.getRatePrice() * userDriver.getRatePriceCount();
            totalRating += ratingPrice.getRating();

            DataBaseRoot.child(Util.RDB_USERS)
                    .child(userDriver.getUserID())
                    .child(Util.RATE_PRICE_COUNT)
                    .setValue(userDriver.getRatePriceCount()+1);

            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.DOWN);

            DataBaseRoot.child(Util.RDB_USERS)
                    .child(userDriver.getUserID())
                    .child(Util.RATE_PRICE)
                    .setValue(Float.parseFloat(df.format(totalRating/ (userDriver.getRatePriceCount()+1))));
        }

        if(ratingService.getRating() != 0)
        {
            //Log.v("Main","ratingService.getRating() != 0");
            float totalRating = userDriver.getRateService() * userDriver.getRateServiceCount();
            totalRating += ratingService.getRating();

            DataBaseRoot.child(Util.RDB_USERS)
                    .child(userDriver.getUserID())
                    .child(Util.RATE_SERVICE_COUNT)
                    .setValue(userDriver.getRateServiceCount()+1);

            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.DOWN);
            DataBaseRoot.child(Util.RDB_USERS)
                    .child(userDriver.getUserID())
                    .child(Util.RATE_SERVICE)
                    .setValue(Float.parseFloat(df.format(totalRating/ (userDriver.getRateServiceCount()+1))));

        }


        DataBaseRoot.child(Util.RDB_USERS)
                .child(userKey)
                .child(Util.RATED_FOR)
                .setValue(updatedRatedForList);


    }
}
