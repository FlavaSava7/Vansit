package devgam.vansit;


import android.*;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Requests;
import devgam.vansit.JSON_Classes.Users;

import static devgam.vansit.Util.makeToast;

public class ViewRequests extends Fragment  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private FragmentManager fragmentManager;

    private ListView listView;
    private ArrayAdapter requestAdapter;
    private ArrayList<Requests> requestsList;

    private Spinner spinnerCity;
    private static String whichCity="";// to give it a new value in a spinner to fetch new items
    private static int listCounter = 5;
    private static final int listCounterOriginal = listCounter;
    ChildEventListener QCEL;// list to child updates
    DatabaseReference RequestRefs; // ref. to where we are listening

    private GoogleApiClient mGoogleApiClient;
    boolean mLocationPermissionGranted;
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    double Longitude,Latitude;
    LocationManager locationManager;


    public ViewRequests()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_view_requests, container, false);
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.request_view_serving);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                return true;
            }
        });
        item.setVisible(true);
    }
    @Override
    public void onStop()
    {
        if (mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
            Log.v("Main"," mGoogleApiClient disconnected");
        }
        StopRequestUpdates();
        whichCity="";//reset
        super.onStop();
    }
    @Override
    public void onResume()
    {
        super.onResume();

        //Util.ChangePageTitle(getActivity(), "");

        SetUpFragment();

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
            //Log.v("Main", "GPS_PROVIDER " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            //Log.v("Main", "NETWORK_PROVIDER " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        }

        if (Build.VERSION.SDK_INT >= 23)
            if (ActivityCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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
        }

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,null);
        else
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,this,null);


        if(requestsList==null)
        {
            requestsList = new ArrayList<>();
        }
        else
        {
            listView.setAdapter(requestAdapter);
        }


    }

    public void SetUpFragment()
    {
        listView = (ListView) getActivity().findViewById(R.id.frag_viewRequests_listview);
        requestAdapter = new itemsAdapter(getContext());
        fragmentManager  = getActivity().getSupportFragmentManager();
        spinnerCity = (Spinner)getActivity().findViewById(R.id.frag_viewRequests_spinCity);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.city_list));
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            boolean stopAutoFiringCode=false;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(stopAutoFiringCode)
                {
                    if(!whichCity.equals(parent.getSelectedItem().toString()))// prevent clicking same city and waste time
                    {
                        whichCity = parent.getSelectedItem().toString();
                        ChangeListItems();
                    }

                }
                stopAutoFiringCode = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        try {
            getActivity().findViewById(R.id.loadingPanel_main).setVisibility(View.GONE);
        } catch (Exception e){
        }


    }


    private class itemsAdapter extends ArrayAdapter<Requests> {

        Context context;

        itemsAdapter(Context c) {
            super(c, R.layout.fragment_view_requests_listview_items, requestsList);
            this.context = c;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent)
        {

            final ViewHolder holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowItem = inflater.inflate(R.layout.fragment_view_requests_listview_items, parent, false);

            final Requests tempRequest = requestsList.get(position);
            final Offers requestOffer = tempRequest.getOffer();
            final Users requestUser = tempRequest.getUser();

            holder.Title = (TextView) rowItem.findViewById(R.id.viewRequests_items_TitleData);
            holder.Title.setText(requestOffer.getTitle());

            holder.Address = (TextView) rowItem.findViewById(R.id.viewRequests_items_addressData);
            holder.Distance = (TextView) rowItem.findViewById(R.id.viewRequests_items_distanceData);
            holder.Status = (TextView) rowItem.findViewById(R.id.viewRequests_items_statusData);

            holder.Address.setText(tempRequest.getAddress());

            if(tempRequest.isServed())
            {
                holder.Status.setText("User Is Being Served!");

                holder.Status.setTextColor(getResources().getColor(R.color.deleteButtonColor));
            }
            else
            {
                holder.Status.setText("Available To Serve!");
                holder.Status.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }


            float distanceValue = Float.valueOf(tempRequest.getDistanceFromRequestToUser());
            String disString;
            if(distanceValue<1f)// don't divide on 1000, it's already in meters
            {
                disString = String.valueOf(distanceValue);
                disString = disString.substring(0,disString.indexOf(".")+3) + " M";
            }else
            {
                disString = String.valueOf(distanceValue/1000);
                disString = disString.substring(0,disString.indexOf(".")+3) + " KM";
            }
            //Log.v("Main","diString " +disString);
            holder.Distance.setText(disString);

            holder.typeIcon = (ImageView) rowItem.findViewById(R.id.viewRequests_items_typeIcon);
            holder.typeIcon.setImageDrawable(Util.getDrawableResource(getActivity(), Util.changeIcon(requestOffer.getType())));

            holder.profileText = (LinearLayout) rowItem.findViewById(R.id.viewRequests_items_profile_layout);
            holder.callText = (LinearLayout) rowItem.findViewById(R.id.viewRequests_items_call_layout);
            holder.serveText = (LinearLayout) rowItem.findViewById(R.id.viewRequests_items_serve_layout);

            final String phoneNumber = requestUser.getPhone();
            holder.callText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" +phoneNumber));
                    startActivity(intent);
                }
            });

            final userInformation userIn = new userInformation(getActivity(),requestUser, fragmentManager);
            holder.profileText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userIn.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    userIn.show();
                }
            });

            holder.serveText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+
                            tempRequest.getLatitude()+","+tempRequest.getLongitude()+
                            "&mode=d");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(context.getPackageManager()) != null)
                    {
                        startActivity(mapIntent);
                    }
                    else
                    {
                        Util.makeToast(context,"Cannot find Google Maps!");
                    }

                }
            });



            return rowItem;
        }
        @Override
        public int getCount() {
            return requestsList.size();
        }

        @Override
        public Requests getItem(int position) {
            return requestsList.get(position);
        }
    }

    static class ViewHolder {
        TextView Title,Address, Distance, Status;
        ImageView typeIcon;

        LinearLayout profileText, callText, serveText;
    }

    public void ChangeListItems()
    {

        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            this.requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
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

        if(whichCity.isEmpty()|| whichCity.equals("Select City"))
        {
            return;
        }

        requestsList.clear();
        listCounter = listCounterOriginal;

        ShowMoreBtn(listView);
        StopRequestUpdates();
        StartRequestUpdates();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().
                child(Util.RDB_REQUESTS);
        Query query = myRef.orderByChild("offer/"+Util.CITY).equalTo(whichCity).limitToLast(listCounter);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())
                {
                    Util.makeToast(getContext(),"No Requests here!");
                }
                else
                {
                    Log.v("Main","geoLocation.Latitude "+Latitude);
                    Log.v("Main","geoLocation.Longitude "+Longitude);

                    for (DataSnapshot ds1: dataSnapshot.getChildren())
                    {
                        Requests tempRequest = ds1.getValue(Requests.class);
                        tempRequest = SetUpDistance(tempRequest);
                        requestsList.add(tempRequest);
                    }

                    SortByDistanceDesc(requestsList);

                }

                listView.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
    public void ShowMoreBtn(final ListView listView)
    {
        if(listView == null)
            return;
        if(listView.getFooterViewsCount()>=1)
            return;

        Button showMore = new Button(getContext());
        showMore.setText(getResources().getString(R.string.main_show_more));
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                listCounter +=listCounterOriginal;
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS);
                Query query = myRef.orderByChild("offer/"+Util.CITY).equalTo(whichCity).limitToLast(listCounter);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot areaSnapshot: dataSnapshot.getChildren())
                        {
                            Requests tempRequest = areaSnapshot.getValue(Requests.class);

                            boolean toAdd= true;
                            for(Requests req : requestsList)
                                if(tempRequest.getOffer().getUserID().equals(req.getOffer().getUserID()))
                                {
                                    toAdd=false;
                                    break;
                                }


                            if(toAdd)
                            {
                                tempRequest = SetUpDistance(tempRequest);
                                requestsList.add(tempRequest);
                            }
                        }

                        SortByDistanceDesc(requestsList);
                        requestAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        listView.addFooterView(showMore);

    }
    public void StartRequestUpdates()
    {
        Log.v("Main","StartRequestUpdates");
        RequestRefs = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS);
        Query query = RequestRefs.orderByChild("offer/"+Util.CITY).equalTo(whichCity);
        QCEL = new ChildEventListener()
        {
            int RequestObjIndex(Requests changedChild)
            {
                if(requestsList==null || requestsList.isEmpty())
                    return -1;

                for(int index = 0 ; index<requestsList.size();index++)
                {
                    if(changedChild.getOffer().getUserID().
                            equals(requestsList.get(index).getOffer().getUserID()))
                    {
                       return index;
                    }

                }
                return -1;
            }
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                Log.v("Main","onChildChanged");
                Requests tempRequest = dataSnapshot.getValue(Requests.class);
                int objIndex = RequestObjIndex(tempRequest);
                if(objIndex != -1)// -1 means : not found or list is empty or null
                {
                    Log.v("Main","objIndex != -1 "+tempRequest.getAddress());
                    tempRequest = SetUpDistance(tempRequest);
                    requestsList.set(objIndex,tempRequest);
                    SortByDistanceDesc(requestsList);
                    requestAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                Log.v("Main","onChildRemoved");
                Requests tempRequest = dataSnapshot.getValue(Requests.class);
                int objIndex = RequestObjIndex(tempRequest);
                if(objIndex != -1)// -1 means : not found or list is empty or null
                {
                    Log.v("Main","objIndex != -1 "+tempRequest.getAddress());
                    requestsList.remove(objIndex);
                    SortByDistanceDesc(requestsList);
                    requestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {
                Log.v("Main","onChildMoved");
                Requests tempRequest = dataSnapshot.getValue(Requests.class);
                int objIndex = RequestObjIndex(tempRequest);
                if(objIndex != -1)// -1 means : not found or list is empty or null
                {
                    Log.v("Main","objIndex != -1 "+tempRequest.getAddress());
                    requestsList.remove(objIndex);
                    SortByDistanceDesc(requestsList);
                    requestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addChildEventListener(QCEL);
    }
    public void StopRequestUpdates()
    {
        if(RequestRefs!=null && QCEL!=null)
            RequestRefs.removeEventListener(QCEL);
        else
        {
            Log.v("Main","(myRef!=null && QCEL!=null ELSE");
        }
    }
    private Requests SetUpDistance(Requests tempRequest)
    {
        Location locationRequest = new Location("Request Location");
        locationRequest.setLatitude(tempRequest.getLatitude());
        locationRequest.setLongitude(tempRequest.getLongitude());

        Location locationMine = new Location("My Location");
        locationMine.setLatitude(Latitude);
        locationMine.setLongitude(Longitude);

        float distance = locationMine.distanceTo(locationRequest);
        tempRequest.setDistanceFromRequestToUser(String.valueOf(distance));

        return  tempRequest;
    }


    private ArrayList<Requests> SortByDistanceDesc(ArrayList<Requests> arrayToSort)
    {

        Collections.sort(arrayToSort, new Comparator<Requests>() {
            @Override
            public int compare(Requests o1, Requests o2)
            {

                return Float.valueOf(o1.getDistanceFromRequestToUser()).
                        compareTo(Float.valueOf(o2.getDistanceFromRequestToUser()));
            }
        });
        return arrayToSort;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if(location!=null)
        {
            mLocationPermissionGranted = true;
            Longitude = location.getLongitude();
            Latitude = location.getLatitude();

            //Log.v("Main","Location changed: "+location.getLatitude()+ "," + location.getLongitude());
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String myAddress = getCompleteAddressString(Latitude,Longitude);
                    if(!myAddress.equals(""))
                        Log.v("Main","This User Address Is "+myAddress);
                    else
                        Log.v("Main","Cannot get Address!");

                }
            }, 500);
            Util.makeToast(getContext(), "Found Your Location");
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
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //Log.v("Main", "grantResults.length > 0");
                    mLocationPermissionGranted = true;
                    ChangeListItems();
                }
                else
                {
                    mLocationPermissionGranted = false;
                    if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION))
                    {
                        //Log.v("Main","user denied the permission but DID NOT CHECK the 'never show again' option.");
                    }
                    else{
                        //Log.v("Main","user denied the permission and CHECKED the 'never show again' option.");
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
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
                    /*Uri gmmIntentUri = Uri.parse("google.navigation:q="+
                            tempRequest.getLatitude()+","+tempRequest.getLongitude()+
                            "&mode=d");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(context.getPackageManager()) != null)
                    {
                        startActivity(mapIntent);
                    }
                    else
                    {
                        Util.makeToast(context,"Cannot find Google Maps!");
                    }*/