package devgam.vansit;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Requests;
import devgam.vansit.JSON_Classes.Users;

public class AcceptedRequest extends Fragment {


    ChildEventListener QCEL;
    DatabaseReference QCEL_Ref;
    FragmentManager fragmentManager;
    DatabaseReference DataBaseRoot;

    private static final long TimeBeforeFinish = 1000;

    Requests myRequest; // request of user and driver
    Users user;// user waiting to be served
    Users driver;// driver to serve

    String userKeyOfRequest = "";

    // WE WILL USE 2 LAYOUTS ONE FOR DRIVER AND ONE FOR CUSTOMER

    // driver layout
    TextView userNameTxt, userAgeTxt, userCityTxt;
    LinearLayout userCallLayout;
    ImageView userImg;
    Button mapBtn,naviBtn,cancelBtnDriver;

    // user layout
    TextView driverNameTxt,  driverAgeTxt, driverCityTxt;
    ImageView driverImg;
    Button cancelBtnUser,doneBtnUser;
    LinearLayout driverCallLayout, rateLayout;

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

        if(getArguments()!=null) {
            userKeyOfRequest = getArguments().getString(RequestNotifications.USER_KEY);
        }

        if(userKeyOfRequest!=null && !userKeyOfRequest.equals("")) {// DRIVER LAYOUT//
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
        DataBaseRoot = FirebaseDatabase.getInstance().getReference();//connect to DB root
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
        userCityTxt = (TextView) getActivity().findViewById(R.id.accepted_request_driver_city);
        userAgeTxt = (TextView) getActivity().findViewById(R.id.accepted_request_driver_age);
        userCallLayout = (LinearLayout) getActivity().findViewById(R.id.accepted_request_driver_call);
        userImg = (ImageView) getActivity().findViewById(R.id.accepted_request_driver_img);

        mapBtn = (Button) getActivity().findViewById(R.id.accepted_request_driver_map);
        naviBtn = (Button) getActivity().findViewById(R.id.accepted_request_driver_navigation);

        cancelBtnDriver = (Button) getActivity().findViewById(R.id.accepted_request_driver_cancel);

        userNameTxt.setText(user.getFirstName()+" "+user.getLastName());

        userCityTxt.setText(getContext().getResources().getString(R.string.user_information_home_city)+ " " + user.getCity());
        //set User age to text on dialog
        int age =  Util.yearNow - Integer.parseInt(user.getDateYear()) ;
        age = (Util.monthNow > Integer.parseInt(user.getDateMonth()) ? age : age -1 );
        userAgeTxt.setText(getContext().getResources().getString(R.string.user_information_age)+ " " + age );

        if(!user.getGender().isEmpty() || user.getGender() != ""){
            if (user.getGender().equals("male"))
                userImg.setImageResource(R.drawable.ic_user_male);
            else if (user.getGender().equals("female"))
                userImg.setImageResource(R.drawable.ic_user_female);
        }

        userCallLayout.setOnClickListener(new View.OnClickListener() {
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
        driverAgeTxt = (TextView) getActivity().findViewById(R.id.accepted_request_user_ageData);
        driverCityTxt = (TextView) getActivity().findViewById(R.id.accepted_request_user_cityData);
        driverImg = (ImageView) getActivity().findViewById(R.id.accepted_request_user_imgData);

        cancelBtnUser = (Button) getActivity().findViewById(R.id.accepted_request_user_cancel);
        doneBtnUser = (Button) getActivity().findViewById(R.id.accepted_request_user_done);
        //commitRatingBtnUser = (Button) getActivity().findViewById(R.id.accepted_request_user_commit);

        driverCallLayout = (LinearLayout) getActivity().findViewById(R.id.accepted_request_user_callData);
        rateLayout = (LinearLayout) getActivity().findViewById(R.id.accepted_request_user_rateData);
        /*ratingNameService  = (TextView) getActivity().findViewById(R.id.accepted_request_user_serviceRatingName);
        ratingNamePrice = (TextView) getActivity().findViewById(R.id.accepted_request_user_priceRatingName);

        ratingService = (RatingBar) getActivity().findViewById(R.id.accepted_request_user_serviceRatingData);
        ratingPrice = (RatingBar) getActivity().findViewById(R.id.accepted_request_user_priceRatingData);*/

        driverNameTxt.setText(driver.getFirstName()+" "+driver.getLastName());

        //set User age to text on dialog
        int age =  Util.yearNow - Integer.parseInt(driver.getDateYear()) ;
        age = (Util.monthNow > Integer.parseInt(driver.getDateMonth()) ? age : age -1 );
        driverAgeTxt.setText(getContext().getResources().getString(R.string.user_information_age)+ " " + age );

        driverCityTxt.setText(getContext().getResources().getString(R.string.user_information_home_city)+ " " + driver.getCity());

        if(!driver.getGender().isEmpty() || driver.getGender() != ""){
            if (driver.getGender().equals("male"))
                driverImg.setImageResource(R.drawable.ic_user_male);
            else if (driver.getGender().equals("female"))
                driverImg.setImageResource(R.drawable.ic_user_female);
        }

        driverCallLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" +driver.getPhone()));
                startActivity(intent);
            }
        });

        //To show Dialog
        final ratingDialog rateUser = new ratingDialog(getActivity(), driver);
        rateUser.initialDialog(rateUser);

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        rateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = v;
                if (!Util.isLogged()) {
                    // User is not logged
                    Util.makeSnackbar(v, getResources().getString(R.string.user_not_logged));
                    return;
                } else if (driver.getUserID().equals(firebaseAuth.getCurrentUser().getUid())) {
                    // User cant vote for self.
                    Util.makeSnackbar(v, getResources().getString(R.string.user_rate_to_hisself));
                    return;
                }

                DatabaseReference query = DataBaseRoot.child(Util.RDB_USERS + "/" + firebaseAuth.getCurrentUser().getUid() + "/" + Util.RATED_FOR);
                ValueEventListener VEL = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        ArrayList<String> ratedForList = (ArrayList<String>) dataSnapshot.getValue();
                        if (ratedForList == null)
                            Util.makeToast(getActivity(), "Something wrong happened!");
                        else {
                            boolean canRate = true;
                            for (String value : ratedForList) {
                                if (value.equals(driver.getUserID())){//User already voted for this driver
                                    canRate = false;
                                    break;
                                }
                            }

                            if (canRate) {
                                rateUser.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                rateUser.show();
                            } else
                                Util.makeSnackbar(view, getResources().getString(R.string.user_rate_agian));
                        }
                    } @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                query.addListenerForSingleValueEvent(VEL);

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

        //SetUpRating();
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


    /*void SetUpRating()// because its too messy
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
    }*/
}

