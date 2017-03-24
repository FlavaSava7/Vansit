package devgam.vansit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
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

    TextView addressText;
    Spinner spinnerCity,spinnerType;
    EditText editTitle,editDesc;
    Button requestSendBtn,requestDeleteBtn;

    Users myUser;
    Requests myRequest;
    FragmentManager fragmentManager;

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
                if(myRequest==null)
                {
                    Util.makeToast(getContext(),"You Don't Have Any Requests!");
                    return false;
                }

                MyRequest myRequestPage = new MyRequest(getActivity(),myRequest, fragmentManager);
                myRequestPage.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                myRequestPage.show();
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
            Log.v("Main"," mGoogleApiClient disconnected");

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
            Log.v("Main", "All location services are disabled");
            return;

        } else {
            mLocationPermissionGranted = true;
            Log.v("Main", "GPS_PROVIDER " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            Log.v("Main", "NETWORK_PROVIDER " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        }

        if (Build.VERSION.SDK_INT >= 23)
            if (ActivityCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionGranted = true;
                Log.v("Main", "We Have One of the permissions");
            }
            else
            {
                Util.makeToast(getContext(),"Please Enable Location Services for This Application");
                Log.v("Main", "We Dont Have any of the permissions for SDK_INT >= 23");
                mLocationPermissionGranted = false;
                return;
            }

            if(Longitude==0||Latitude==0)
            {
                Util.makeToast(getContext(), "Searching for Location...");
            }
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,this);
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,null);
            else
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,this,null);






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

                    editTitle.setText(myRequest.getOffer().getTitle());
                    editDesc.setText(myRequest.getOffer().getDescription());

                    ArrayList<String> tempCityList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.city_list)));
                    spinnerCity.setSelection(tempCityList.indexOf(myRequest.getOffer().getCity()));

                    ArrayList<String> tempTypeList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.type_list)));
                    spinnerType.setSelection(tempTypeList.indexOf(myRequest.getOffer().getType()));

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
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            this.requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;

        }

        if(!Util.IS_USER_CONNECTED)
        {
            makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
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

        Offers myOffer = new Offers(myUser.getUserID(),
                editTitle.getText().toString(),
                editDesc.getText().toString(),
                spinnerType.getSelectedItem().toString(),
                spinnerCity.getSelectedItem().toString(),
                Util.RDB_JORDAN,
                System.currentTimeMillis());

        DatabaseReference myRefRequests = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS
                +"/"+myUser.getUserID());
        Requests request = new Requests(myUser,myOffer,addressText.getText().toString()
                ,Latitude,Longitude, System.currentTimeMillis());

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
    public void onLocationChanged(Location location)
    {
        if(location!=null)
        {
            mLocationPermissionGranted = true;
            Longitude = location.getLongitude();
            Latitude = location.getLatitude();
            addressText.setText("Latitude: "+Latitude+", Longitude: "+Longitude);
            //Log.v("Main","Location changed: "+location.getLatitude()+ "," + location.getLongitude());
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String myAddress = getCompleteAddressString(Latitude,Longitude);
                    if(!myAddress.equals(""))
                        addressText.setText("Address: "+myAddress);
                    else
                        addressText.setText("Cannot get Address!");

                }
            }, 500);

        }
        else
        {

        }

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
                        Log.v("Main","myUser denied the permission but DID NOT CHECK the 'never show again' option.");
                    }
                    else{
                        Log.v("Main","myUser denied the permission and CHECKED the 'never show again' option.");
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        Util.AlertDialog(getContext(),"Warning!",
                                "Please Manually Enable Location Services for this app inside your settings",intent);
                    }
                }
            }
        }

    }
    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        try
        {
            if (Build.VERSION.SDK_INT >=23)
            {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            if(Longitude!=0 && Latitude !=0)
                return;
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,null);
                //Log.v("Main","onConnected  IF");
            }
            else
            {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,this,null);
                //Log.v("Main","onConnected  ELSE");
            }

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
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Log.v("Main","onStatusChanged ");

    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.v("Main","onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Log.v("Main","onProviderDisabled");

    }

}
