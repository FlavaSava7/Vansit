package devgam.vansit;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Requests;
import devgam.vansit.JSON_Classes.Users;

public class AcceptedRequest extends Fragment {


    ChildEventListener QCEL;
    DatabaseReference QCEL_Ref;
    FragmentManager fragmentManager;

    private static final long TimeBeforeFinish = 1000;

    Requests myRequest; // request of user and driver
    Users user;// user waiting to be served
    Users driver;// driver to serve

    String userKeyOfRequest = "";

    // WE WILL USE 2 LAYOUTS ONE FOR DRIVER AND ONE FOR CUSTOMER

    // driver layout
    TextView userNameTxt,userPhoneTxt;
    Button mapBtn,naviBtn,cancelBtnDriver;

    // user layout
    TextView driverNameTxt,driverPhoneTxt,ratingNameService, ratingNamePrice;
    Button cancelBtnUser,doneBtnUser,commitRatingBtnUser;
    RatingBar ratingService,ratingPrice;

    public AcceptedRequest()
    {
        // Required empty public constructor
    }

    @Override
    public void onStop()
    {
        super.onStop();
        StopRequestUpdates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view =  inflater.inflate(R.layout.fragment_accepted_request_user, container, false);// USER LAYOUT

        if(getArguments()!=null)
        {
            userKeyOfRequest = getArguments().getString(RequestNotifications.USER_KEY);
        }

        if(userKeyOfRequest!=null && !userKeyOfRequest.equals(""))// DRIVER LAYOUT
        {
            view = inflater.inflate(R.layout.fragment_accepted_request_driver, container, false);
        }
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        fragmentManager  = getActivity().getSupportFragmentManager();
        //Log.v("Main","user key "+userKeyOfRequest);
        DatabaseReference myRef;
        if(userKeyOfRequest.equals(""))
        {
            myRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS+"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        }else
        {
            myRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS+"/"+userKeyOfRequest);
        }

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    myRequest = dataSnapshot.getValue(Requests.class);
                    user = myRequest.getUser();
                    driver = myRequest.getServeDrivers().get(0);

                    if(userKeyOfRequest.equals(""))
                    {
                        UserFragmentSetUp();
                    }else
                    {
                        DriverFragmentSetUp();
                    }

                    StartRequestUpdates();
                }
                else
                {
                    Util.makeToast(getContext(),"Something Wrong!");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void DriverFragmentSetUp()
    {
        //Log.v("Main","DriverFragmentSetUp ");
        userNameTxt = (TextView) getActivity().findViewById(R.id.accepted_request_driver_user);
        userPhoneTxt = (TextView) getActivity().findViewById(R.id.accepted_request_driver_phoneData);

        mapBtn = (Button) getActivity().findViewById(R.id.accepted_request_driver_map);
        naviBtn = (Button) getActivity().findViewById(R.id.accepted_request_driver_navigation);

        cancelBtnDriver = (Button) getActivity().findViewById(R.id.accepted_request_driver_cancel);

        userNameTxt.setText("You Are Serving "+user.getFirstName()+" "+user.getLastName());
        userPhoneTxt.setText(user.getPhone());
        userPhoneTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" +user.getPhone()));
                startActivity(intent);
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                OpenMapUserLocation(myRequest.getLatitude(),myRequest.getLongitude());
            }
        });

        naviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenNavigationUserLocation(myRequest.getLatitude(),myRequest.getLongitude());
            }
        });

        cancelBtnDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(getContext())
                        .setTitle("Cancel Serving?")
                        .setMessage("")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {

                                RequestNotifications requestNotifications =
                                        new RequestNotifications(getContext(),
                                                myRequest.getDeviceToken()
                                                ,myRequest);
                                requestNotifications.SendNotificationToUserCancel(driver);

                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                        .child(Util.RDB_REQUESTS+"/"+user.getUserID());
                                myRequest.setServeDrivers(null);
                                myRequest.setTimeStamp(System.currentTimeMillis());
                                myRequest.setServed(Boolean.FALSE);
                                myRef.setValue(myRequest);

                                FinishFragment();


                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
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
    private void UserFragmentSetUp()
    {
        driverNameTxt = (TextView) getActivity().findViewById(R.id.accepted_request_user_nameData);
        driverPhoneTxt = (TextView) getActivity().findViewById(R.id.accepted_request_user_phoneData);

        cancelBtnUser = (Button) getActivity().findViewById(R.id.accepted_request_user_cancel);
        doneBtnUser = (Button) getActivity().findViewById(R.id.accepted_request_user_done);
        commitRatingBtnUser = (Button) getActivity().findViewById(R.id.accepted_request_user_commit);

        ratingNameService  = (TextView) getActivity().findViewById(R.id.accepted_request_user_serviceRatingName);
        ratingNamePrice = (TextView) getActivity().findViewById(R.id.accepted_request_user_priceRatingName);

        ratingService = (RatingBar) getActivity().findViewById(R.id.accepted_request_user_serviceRatingData);
        ratingPrice = (RatingBar) getActivity().findViewById(R.id.accepted_request_user_priceRatingData);

        driverNameTxt.setText("You Are Being Served By "+driver.getFirstName()+" "+driver.getLastName());
        driverPhoneTxt.setText(driver.getPhone());
        driverPhoneTxt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" +driver.getPhone()));
                startActivity(intent);
            }
        });
        cancelBtnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(getContext())
                        .setTitle("Cancel Serving?")
                        .setMessage("")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {

                                RequestNotifications requestNotifications =
                                        new RequestNotifications(getContext(),
                                                driver.getDeviceToken()
                                                ,myRequest);
                                requestNotifications.SendNotificationToDriver(user,Boolean.FALSE);
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                        .child(Util.RDB_REQUESTS+"/"+user.getUserID());
                                myRequest.setServeDrivers(null);
                                myRequest.setTimeStamp(System.currentTimeMillis());
                                myRequest.setServed(Boolean.FALSE);
                                myRef.setValue(myRequest);

                                FinishFragment();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        doneBtnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(getContext())
                        .setTitle("Finish?")
                        .setMessage("(Your Request will be deleted, and you Cant Go Back To This Page Again)")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                        .child(Util.RDB_REQUESTS+"/"+user.getUserID());
                                myRef.removeValue();

                                FinishFragment();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        SetUpRating();
    }
    private void OpenMapUserLocation(double latitude, double longitude)
    {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+latitude+","+longitude+"&z=16");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getContext().getPackageManager()) != null)
        {
            startActivity(mapIntent);
        }
        else
        {
            Util.makeToast(getContext(),"Cannot find Google Maps!");
        }
    }
    private void OpenNavigationUserLocation(double latitude, double longitude)
    {
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+
                latitude+","+longitude+
                "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getContext().getPackageManager()) != null)
        {
            startActivity(mapIntent);
        }
        else
        {
            Util.makeToast(getContext(),"Cannot find Google Maps!");
        }
    }

    private void FinishFragment()
    {
        getActivity().getSupportFragmentManager().popBackStack();
        // if user app wasn't opened and this is the only fragment we have then ...
        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 1)
        {
            //getActivity().finish();

            Main mainPage = new Main();
            Util.ChangeFrag(mainPage,fragmentManager);
        }
    }
    public void StartRequestUpdates()
    {
        QCEL_Ref = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS);
        Query query = QCEL_Ref.orderByKey().equalTo(myRequest.getUser().getUserID());
        QCEL = new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                Requests requests = dataSnapshot.getValue(Requests.class);
                if(!requests.isServed())
                {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            Util.makeToast(getContext(),"Request Got Canceled!");
                            FinishFragment();// for now the only thing we have is if user or driver canceled
                        }
                    }, TimeBeforeFinish);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        Util.makeToast(getContext(),"Request is Done!");
                        FinishFragment();// for now the only thing we have is if user or driver canceled
                    }
                },TimeBeforeFinish);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {
                Log.v("Main","onChildMoved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addChildEventListener(QCEL);
    }
    public void StopRequestUpdates()
    {
        if(QCEL_Ref!=null && QCEL!=null)
            QCEL_Ref.removeEventListener(QCEL);
    }
    void SetUpRating()// because its too messy
    {
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

        commitRatingBtnUser.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(getContext())
                        .setTitle("Are You Sure?")
                        .setMessage("")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {

                                if(!Util.isLogged()) // User is not logged
                                    return ;

                                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                if(driver.getUserID().equals(firebaseAuth.getCurrentUser().getUid()))// User cant vote for self.
                                    return ;

                                DatabaseReference query = FirebaseDatabase.getInstance().getReference()
                                        .child(Util.RDB_USERS+"/"+firebaseAuth.getCurrentUser().getUid()+"/"+Util.RATED_FOR);
                                ValueEventListener VEL = new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        ArrayList<String> ratedForList = (ArrayList<String>) dataSnapshot.getValue();
                                        if( ratedForList == null )
                                            Util.makeToast(getContext(),"Something wrong happened!");
                                        else
                                        {
                                            boolean canRate=true;
                                            for(String value : ratedForList)
                                            {
                                                if(value.equals(driver.getUserID()))//User already voted for this driver
                                                    canRate = false;
                                            }

                                            if(canRate)
                                            {
                                                ratedForList.add(driver.getUserID());

                                                commitRating(ratedForList,firebaseAuth.getCurrentUser().getUid() );//apply rate and add this driver to User

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
        //current userKey so we can search and add the updatedRatedForList for User.
        if(ratingPrice.getRating() != 0)
        {
            //Log.v("Main","ratingPrice.getRating() != 0");
            float totalRating = driver.getRatePrice() * driver.getRatePriceCount();
            totalRating += ratingPrice.getRating();

            FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS)
                    .child(driver.getUserID())
                    .child(Util.RATE_PRICE_COUNT)
                    .setValue(driver.getRatePriceCount()+1);

            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.DOWN);

            FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS)
                    .child(driver.getUserID())
                    .child(Util.RATE_PRICE)
                    .setValue(Float.parseFloat(df.format(totalRating/ (driver.getRatePriceCount()+1))));
        }

        if(ratingService.getRating() != 0)
        {
            //Log.v("Main","ratingService.getRating() != 0");
            float totalRating = driver.getRateService() * driver.getRateServiceCount();
            totalRating += ratingService.getRating();

            FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS)
                    .child(driver.getUserID())
                    .child(Util.RATE_SERVICE_COUNT)
                    .setValue(driver.getRateServiceCount()+1);

            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.DOWN);
            FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS)
                    .child(driver.getUserID())
                    .child(Util.RATE_SERVICE)
                    .setValue(Float.parseFloat(df.format(totalRating/ (driver.getRateServiceCount()+1))));

        }

        FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS)
                .child(userKey)
                .child(Util.RATED_FOR)
                .setValue(updatedRatedForList);
    }
}

