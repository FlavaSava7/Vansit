package devgam.vansit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Requests;
import devgam.vansit.JSON_Classes.Users;

import static devgam.vansit.Util.makeToast;


public class addRequest extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public addRequest() {
        // Required empty public constructor
    }

    private GoogleApiClient mGoogleApiClient;
    boolean mLocationPermissionGranted;
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    double Longitude,Latitude;
    LocationManager locationManager;
    LocationListener mLocationListener;
    LocationRequest mLocationRequest;
    private static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    TextView addressText;
    Spinner spinnerCity,spinnerType;
    EditText editTitle,editDesc;
    Button requestSendBtn,requestDeleteBtn;

    Users myUser;
    Requests myRequest;
    FragmentManager fragmentManager;
    String myAddress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_add_request, container, false);
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.request_status);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                UpdateIfRequestExists();
                return true;
            }
        });
        item.setVisible(true);
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStop()
    {
        if (mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
            //Log.v("Main"," mGoogleApiClient disconnected");

            myRequest=null;
        }

        super.onStop();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Util.ChangePageTitle(getActivity(), R.string.main_fab_text_add_request);

        initLayoutVars();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationPermissionGranted = false;

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            Util.AlertDialog(getContext(), "Warning!", "Please Enable Location Services", intent);
            //Log.v("Main", "All location services are disabled");
            return;

        } else {
            mLocationPermissionGranted = true;
           // Log.v("Main", "GPS_PROVIDER " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            //Log.v("Main", "NETWORK_PROVIDER " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        }

        if (Build.VERSION.SDK_INT >= 23)
            if (ActivityCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionGranted = true;
                //Log.v("Main", "We Have One of the permissions");
            }
            else
            {
                Util.makeToast(getContext(),"Please Enable Location Services for This Application");
                //Log.v("Main", "We Dont Have any of the permissions for SDK_INT >= 23");
                mLocationPermissionGranted = false;
                return;
            }

            if(Longitude==0||Latitude==0)
            {
                Util.makeToast(getContext(), "Searching for Location...");
            }else
            {
                addressText.setText(myAddress);
            }

    }


    private void initLayoutVars()
    {
        fragmentManager  = getActivity().getSupportFragmentManager();

        requestSendBtn = (Button) getActivity().findViewById(R.id.addRequest_Send);
        requestDeleteBtn = (Button) getActivity().findViewById(R.id.addRequest_Delete);
        editTitle = (EditText) getActivity().findViewById(R.id.addRequest_editTitle);
        editDesc = (EditText) getActivity().findViewById(R.id.addRequest_editDesc);

        spinnerCity = (Spinner) getActivity().findViewById(R.id.addRequest_spinCity);
        spinnerType = (Spinner)getActivity().findViewById(R.id.addRequest_spinType);

        addressText = (TextView)getActivity().findViewById(R.id.addRequest_location_text);


        requestSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddRequest();
            }
        });

        requestDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteRequest();
            }
        });

        FillSpinners();

        if(!Util.IS_USER_CONNECTED)
        {
            makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
            return;
        }
        DatabaseReference myRefUsers = FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS+"/"+
                FirebaseAuth.getInstance().getCurrentUser().getUid());
        myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    myUser = dataSnapshot.getValue(Users.class);
                    myUser.setUserID(dataSnapshot.getKey());
                }
                else
                {
                    myUser = null;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        checkIfRequestExists();

    }

    /**
     * initial checking if we got a request in the database
     */
    public void checkIfRequestExists()
    {
        // check if this user already has a request so he can click on View My Request menu button
        DatabaseReference myRefUsers = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS+"/"+
                FirebaseAuth.getInstance().getCurrentUser().getUid());
        myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    myRequest = dataSnapshot.getValue(Requests.class);

                    editTitle.setText(myRequest.getTitle());
                    editDesc.setText(myRequest.getDescription());

                    ArrayList<String> tempCityList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.city_list)));
                    spinnerCity.setSelection(tempCityList.indexOf(myRequest.getCity()));

                    ArrayList<String> tempTypeList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.type_list)));
                    spinnerType.setSelection(tempTypeList.indexOf(myRequest.getType()));

                }
                else
                {
                    myRequest = null;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    /**
     * to keep the My Request . request variable up to date when it changes from driver or user i.e. serveDrivers
     */
    public void UpdateIfRequestExists()
    {
        // check if this user already has a request so he can click on View My Request menu button
        DatabaseReference myRefUsers = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS+"/"+
                FirebaseAuth.getInstance().getCurrentUser().getUid());
        myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    myRequest = dataSnapshot.getValue(Requests.class);
                    MyRequest myRequestPage = new MyRequest(getActivity(),getContext(),myRequest, fragmentManager);
                    myRequestPage.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    myRequestPage.show();
                }
                else
                {
                    myRequest = null;
                    Util.makeToast(getContext(),"You Don't Have Any Requests!");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void FillSpinners()
    {
        //City
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.city_list));
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        //Type
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.type_list));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

    }
    private void AddRequest()
    {
        // add or update current

        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            this.requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;

        }

        if(!Util.IS_USER_CONNECTED)
        {
            makeToast(getContext(), getString(R.string.noInternetMsg));
            return;
        }
        if(!mLocationPermissionGranted)//we don't have any of permissions
            return;

        if(Longitude==0 || Latitude==0)// we have permissions but user didn't enable GPS
        {
            Util.makeToast(getContext(), "Please Enable GPS");
            return;
        }


        if(myUser == null)
            return;

        if(!checkEditText(editTitle.getText().toString(),editDesc.getText().toString()))
            return;
        if(spinnerCity.getSelectedItem().toString().equals("Select City")||
                spinnerType.getSelectedItem().toString().equals("Select Type"))
            return;

        String deviceToken = Util.DeviceToken(getContext());
        if(deviceToken==null)
        {
            Util.makeToast(getContext(), "Something Wrong!");
            return;
        }


        DatabaseReference myRefRequests = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS
                +"/"+myUser.getUserID());
        Requests request = new Requests(myUser,
                editTitle.getText().toString(),
                editDesc.getText().toString(),
                spinnerType.getSelectedItem().toString(),
                spinnerCity.getSelectedItem().toString(),
                addressText.getText().toString()
                ,Latitude,Longitude,
                System.currentTimeMillis(),deviceToken);

        myRequest = request;
        myRefRequests.setValue(request);
        Util.makeToast(getContext(), "Request Sent!");

    }
    private void DeleteRequest()
    {
        if(myUser == null)
            return;
        if(myRequest==null)
        {
            Util.makeToast(getContext(), "You Don't Have Any Requests!");
            return;
        }
        DatabaseReference myRefRequests = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS +"/"+
                myUser.getUserID());
        myRefRequests.removeValue();

        myRequest=null;

        editTitle.setText("");
        editDesc.setText("");
        spinnerCity.setSelection(0);
        spinnerType.setSelection(0);


        Util.makeToast(getContext(), "Request Deleted!");
    }
    private boolean checkEditText(String title, String desc)
    {
        // checks edit texts and spinners
        boolean isValid = true;
        if(TextUtils.isEmpty(title))
        {

            isValid = false;
        }
        if(TextUtils.isEmpty(desc))
        {

            isValid = false;
        }
        if(spinnerCity.getSelectedItem().toString().isEmpty()|| spinnerCity.getSelectedItem().toString().equals("Select City") ||
                spinnerType.getSelectedItem().toString().isEmpty()|| spinnerType.getSelectedItem().toString().equals("Select Type") )
        {
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {

            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("Main", "grantResults.length > 0");
                    mLocationPermissionGranted = true;
                }
                else
                {
                    mLocationPermissionGranted = false;
                    if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION))
                    {
                        Log.v("Main","user denied the permission but DID NOT CHECK the 'never show again' option.");
                    }
                    else{
                        Log.v("Main","user denied the permission and CHECKED the 'never show again' option.");
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        Util.AlertDialog(getContext(),"Warning!",
                                "Please Manually Enable Location Services for this app inside your settings",intent);
                    }
                }
            }
        }

    }

    private void UpdateLocation(Location location)
    {
        mLocationPermissionGranted = true;
        Longitude = location.getLongitude();
        Latitude = location.getLatitude();
        addressText.setText("Longitude: "+Longitude+" Latitude: "+Latitude);
        Log.v("Main","UpdateLocation: "+location.getLatitude()+ "," + location.getLongitude());
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                myAddress = getCompleteAddressString(Latitude,Longitude);
                if(!myAddress.equals(""))
                {
                    Log.v("Main","This User Address Is "+myAddress);
                    addressText.setText(myAddress);
                }

                else
                    Log.v("Main","Cannot get Address!");

            }
        }, 500);




        LocationServices.FusedLocationApi.
                removeLocationUpdates(mGoogleApiClient,mLocationListener);

    }
    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        try
        {
            if (Build.VERSION.SDK_INT >=23)
            {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            if(Longitude!=0 && Latitude !=0)
                return;

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationListener = new LocationListener()
            {
                @Override
                public void onLocationChanged(Location location)
                {
                    if(location!=null)
                        UpdateLocation(location);
                }
            };
            LocationServices.FusedLocationApi.
                    requestLocationUpdates(mGoogleApiClient,mLocationRequest, mLocationListener);

        } catch (SecurityException e) {
            Log.v("Main", "1 " + e.getLocalizedMessage());
            Log.v("Main", "2 " + e.getMessage());

        }
    }
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE)
    {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        if(geocoder==null)
            return strAdd;
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                }
                strAdd = strReturnedAddress.toString();
                //Log.w("Main", "" + strReturnedAddress.toString());
            } else
            {
                Log.w("Main", "No Address returned!");

            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.w("Main", "Cannot get Address!");

        }
        return strAdd;
    }
    @Override
    public void onConnectionSuspended(int i) {
        //Log.v("Main","onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        //Log.v("Main","onConnectionFailed");
    }


    @Override
    public void onLocationChanged(Location location) {
        //Log.v("Main","Location changed");
    }
}
