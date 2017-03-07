package devgam.vansit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;


public class Main extends Fragment {
    public Main() {
        // Required empty public constructor
    }


    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    FloatingActionButton addFab, addOffer, addRequest;
    Animation fabOpen, fabClose, fabClockWise, fabAntiClockWise;
    TextView addOfferText, addRequestText;
    boolean isFloatingActionOpen = false;

    private ListView listView;
    private ArrayList<Offers> offerList;// this will be refilled with Offers each time a user change City Filter
    private ArrayList<Users> userList;// to match offer with the user it has, we are filling in inside the getView
    private ArrayAdapter offerAdapter;
    private static int listCounter = 5;
    private static final int listCounterOriginal = listCounter;
    private static final int recentOfferCounter = 20;


    private Spinner spinnerCity,spinnerType;
    private static String whichCity="";// to give it a new value in a spinner to fetch new items
    private static String whichType="";// to give it a new value in a spinner to fetch new items
    private static String allCities[];//this will contain the values that are in strings.xml
    private static String allTypes[];//this will contain the values that are in strings.xml, used inside the getView to choose icon for type

    //Shared Prferance to add offer to favorite list
    static SharedPreferences userFavoriteOffers;
    static SharedPreferences.Editor userFavoriteEditor;
    static int userFavoriteCount = 0;

    //Long StartTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.v("Main","onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.v("Main","onCreateView");
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //StartTime= System.currentTimeMillis();
        allCities = getResources().getStringArray(R.array.city_list);
        allTypes = getResources().getStringArray(R.array.type_list);

        //shared preferance initialize :
        userFavoriteOffers = getContext().getSharedPreferences("userFavoriteOffers", Context.MODE_PRIVATE);
        /*userFavoriteOffersId = getContext().getSharedPreferences("userFavoriteOffersId", Context.MODE_PRIVATE);
        userFavoriteCount = getContext().getSharedPreferences("userFavoriteCount", Context.MODE_PRIVATE);
        userFavoriteEditor = userFavoriteOffersCity.edit();
        userFavoriteEditor = userFavoriteOffersId.edit();*/
        userFavoriteEditor = userFavoriteOffers.edit();

        addFab = (FloatingActionButton) getActivity().findViewById(R.id.add_fab);// we should show this when he is logged
        addOffer = (FloatingActionButton) getActivity().findViewById(R.id.fab_add_offer);
        addRequest = (FloatingActionButton) getActivity().findViewById(R.id.fab_add_request);

        addOfferText = (TextView) getActivity().findViewById(R.id.fab_add_offer_text);
        addRequestText = (TextView) getActivity().findViewById(R.id.fab_add_request_text);

        fabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        fabClockWise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
        fabAntiClockWise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anticlockwise);

        if(Util.isLogged()) {
            addFab.setVisibility(View.VISIBLE);
            addFab.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(isFloatingActionOpen){
                        addFab.startAnimation(fabAntiClockWise);
                        addRequest.startAnimation(fabClose);
                        //addRequestText.startAnimation(fabClose);
                        addOffer.startAnimation(fabClose);
                        //addOfferText.startAnimation(fabClose);
                        addRequest.setClickable(false);
                        addOffer.setClickable(false);
                        addRequestText.setVisibility(View.INVISIBLE);
                        addOfferText.setVisibility(View.INVISIBLE);
                        isFloatingActionOpen = false;
                    }else {
                        addFab.startAnimation(fabClockWise);
                        addRequest.startAnimation(fabOpen);
                        //addRequestText.startAnimation(fabOpen);
                        addOffer.startAnimation(fabOpen);
                        //addOfferText.startAnimation(fabClose);
                        addRequest.setClickable(true);
                        addOffer.setClickable(true);
                        addRequestText.setVisibility(View.VISIBLE);
                        addOfferText.setVisibility(View.VISIBLE);
                        isFloatingActionOpen = true;
                    }
                }
            });
        }else {
            //Log.v("Main","User is not logged in ");
            addFab.setVisibility(View.GONE);
        }

        addOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                AddOffer addOfferPage = new AddOffer();
                Util.ChangeFrag(addOfferPage, fragmentManager);
            }
        });

        addRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                addRequest addRequestPage = new addRequest();
                Util.ChangeFrag(addRequestPage, fragmentManager);
            }
        });


        fragmentManager  = getActivity().getSupportFragmentManager();


        listView = (ListView) getActivity().findViewById(R.id.frag_main_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // click to go to offerinfo page

                    OfferInfo offerInfoPage = new OfferInfo();
                    Bundle bundle = new Bundle();

                    bundle.putSerializable("userOffer",offerList.get(position));
                    for(Users user:userList)// cuz userList is distinct
                        if(offerList.get(position).getUserID().equals(user.getUserKey()))
                            bundle.putSerializable("userDriver",user);

                    offerInfoPage.setArguments(bundle);
                    //Log.v("Main","Sending to OfferInfo: "+offerList.get(position).getTitle());

                    Util.ChangeFrag(offerInfoPage,fragmentManager);
                }
        });

        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS);
        offerAdapter = new itemsAdapter(getContext(),DataBaseRoot);
        offerList = new ArrayList<>();
        userList = new ArrayList<>();

        spinnerCity = (Spinner)getActivity().findViewById(R.id.frag_main_spinCity);
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            boolean stopAutoFiringCode=false;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(stopAutoFiringCode) {
                    whichCity = parent.getSelectedItem().toString();
                    ChangeListItems();
                    //Log.v("Main", "spinnerCity");
                }
                stopAutoFiringCode = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerType = (Spinner)getActivity().findViewById(R.id.frag_main_spinType);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            boolean stopAutoFiringCode=false;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(stopAutoFiringCode)
                {
                    whichType = parent.getSelectedItem().toString();
                    ChangeListItems();
                    //Log.v("Main", "spinnerType");
                }
                stopAutoFiringCode = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(!offerList.isEmpty())// if we came back to this page dont reload lists
        {
            //Log.v("Main","offerList: "+offerList);

        }else
        {
            //Log.v("Main","offerList:isEmpty()");
            FillSpinnersAndListView();//To fill City and Type Spinners, And a default Filling of the List View.
        }
    }


    private void FillSpinnersAndListView()
    {

        // must check for internet
        if(!Util.IS_USER_CONNECTED)
        {
            // show msg
            return;
        }

        //City
        ArrayList<String> tempCityList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.city_list)));
        tempCityList.add(0,"");
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,tempCityList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCity.setAdapter(cityAdapter);

        //Type
        ArrayList<String> tempTypeList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.type_list)));
        tempTypeList.add(0,"");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,
                tempTypeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        final ProgressDialog progressDialog = new ProgressDialog(getContext(),ProgressDialog.STYLE_SPINNER);
        Util.ProgDialogStarter(progressDialog,"Loading...");

        //Initial Filling of ListView, default

        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference()
                .child(Util.RDB_COUNTRY +"/"+ Util.RDB_JORDAN);
        Query query = DataBaseRoot;
        ValueEventListener QVEL= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                for(DataSnapshot cityRoot : dataSnapshot.getChildren())
                {
                    for (DataSnapshot offerRoot : cityRoot.getChildren())
                    {
                        for (DataSnapshot everyOffer : offerRoot.getChildren())
                        {

                            Offers tempOffer = everyOffer.getValue(Offers.class);
                            tempOffer.setOfferKey(everyOffer.getKey());
                            offerList.add(tempOffer);
                        }
                    }
                }
                // sort them by time desc
                SortByTimeStampDesc(offerList);

                if(offerList.size()>=recentOfferCounter)
                {
                    //Log.v("Main","offerList.size:"+offerList.size());
                    for(int index =offerList.size()-1; index>=recentOfferCounter; index--)
                    {

                        //Log.v("Main","index:"+index);
                        //Log.v("Main","removed:"+offerList.get(index).getTitle());
                        offerList.remove(index);
                    }

                }


                /*for (Offers offer: offerList)
                    Log.v("Main","offerList:"+offer.getTitle());*/

                listView.setAdapter(offerAdapter);
                Util.ProgDialogDelay(progressDialog,1000L);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        query.addListenerForSingleValueEvent(QVEL);
    }

    public void ChangeListItems() {
        // every time the spinner values change , update list
        //must AUTO input the city of the User
        //must check for internet

        if(whichCity.isEmpty()|| whichCity.equals("") ||
                whichType.isEmpty()|| whichType.equals("") ) {
            return;
        }

        ShowMoreBtn(listView);

        listCounter = listCounterOriginal;//reset
        offerList.clear();

        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference()
                .child(Util.RDB_COUNTRY+"/"+
                        Util.RDB_JORDAN+"/"+
                        whichCity+"/"+
                        Util.RDB_OFFERS);
        Query query = DataBaseRoot.orderByChild(Util.RDB_TYPE).equalTo(whichType).limitToFirst(listCounter);
        ValueEventListener QVEL= new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot areaSnapshot: dataSnapshot.getChildren())
                {
                    Offers tempOffer = areaSnapshot.getValue(Offers.class);
                    tempOffer.setOfferKey(areaSnapshot.getKey());
                    offerList.add(tempOffer);
                }

                // sort desc
                SortByTimeStampDesc(offerList);
                listView.setAdapter(offerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        query.addListenerForSingleValueEvent(QVEL);
    }


    private class itemsAdapter extends ArrayAdapter<Offers> {

        Context context;
        DatabaseReference databaseReference;

        itemsAdapter(Context c,DatabaseReference databaseReference) {
            super(c, R.layout.fragment_main_listview_items, offerList);
            this.context = c;
            this.databaseReference = databaseReference;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowItem = inflater.inflate(R.layout.fragment_main_listview_items, parent, false);

            final Offers tempOffer = offerList.get(position);

            holder.Title = (TextView) rowItem.findViewById(R.id.main_items_TitleData);
            holder.Title.setText(tempOffer.getTitle());

            holder.City = (TextView) rowItem.findViewById(R.id.main_items_cityData);
            holder.City.setText(tempOffer.getCity());

            holder.typeIcon = (ImageView) rowItem.findViewById(R.id.main_items_typeIcon);

            switch(tempOffer.getType()) {
                case "Car":holder.typeIcon.setImageDrawable(getDrawableResource(R.mipmap.ic_type_car));break;
                case "Bus":holder.typeIcon.setImageDrawable(getDrawableResource(R.mipmap.ic_type_bus));break;
                case "Taxi":holder.typeIcon.setImageDrawable(getDrawableResource(R.mipmap.ic_type_taxi));break;
                case "Truck":holder.typeIcon.setImageDrawable(getDrawableResource(R.mipmap.ic_type_truck));break;
            }

            holder.ratingService = (TextView) rowItem.findViewById(R.id.main_items_serviceRatingData);
            holder.ratingPrice = (TextView) rowItem.findViewById(R.id.main_items_priceRatingData);

            //initialized by nimer esam for text buttons on list item :
            holder.loveText = (Button) rowItem.findViewById(R.id.main_items_love_text);
            holder.profileText = (Button) rowItem.findViewById(R.id.main_items_profile_text);
            holder.callText = (Button) rowItem.findViewById(R.id.main_items_call_text);
            Query query = databaseReference.child(tempOffer.getUserID());

            ValueEventListener VEL = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.getValue(Users.class)!=null) {

                        //Log.v("Main:","KEY "+dataSnapshot.getKey());
                        final Users tempUser = dataSnapshot.getValue(Users.class);
                        tempUser.setUserKey(dataSnapshot.getKey());

                        boolean toAdd = true;
                        for(Users addedUser:userList)
                            if(tempUser.getUserKey().equals(addedUser.getUserKey()))
                                toAdd=false;

                        if(toAdd)
                            userList.add(tempUser);




                        holder.ratingService.setText("("+tempUser.getRateService()+"/5)");
                        holder.ratingPrice.setText("("+tempUser.getRatePrice()+"/5)");

                        //add by nimer esam :
                        //To make call when user click on call text :

                        // TODO: Implement Love here
                        //holder.Love
                        final String phoneNumber = tempUser.getPhone();
                        holder.callText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" +phoneNumber));
                                startActivity(intent);
                            }
                        });

                        final userInformation userIn = new userInformation(getActivity(),tempUser, fragmentManager);
                        holder.profileText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                userIn.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                userIn.show();
                            }
                        });


                        final String offerCity = tempOffer.getCity();
                        final String offerId = tempOffer.getOfferKey();
                        holder.loveText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //to add +1 for numbers of favorite list

                                if(getNumberOfOffer(offerId) == 0) {
                                    //That's mean this offer not in favorite list
                                    //use method because inside listener can't use non-final vars
                                    userFavoriteCount++;
                                    addToFavoriteList(offerId, offerCity, userFavoriteCount);
                                    Util.makeToast(getContext(), getNumberOfOffer(offerId) + "");
                                    holder.loveText.setTextColor(getResources().getColor(R.color.loveButtonColorChange));
                                } else {
                                    deleteFromFavoriteList(getNumberOfOffer(offerId));
                                    holder.loveText.setTextColor(getResources().getColor(R.color.loveButtonColor));
                                }

                            }
                        });



                    }
                    else {
                        //Log.v("Main:","==null");
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            query.addListenerForSingleValueEvent(VEL);

            // more work
            return rowItem;
        }
        @Override
        public int getCount() {
            return offerList.size();
        }

        @Override
        public Offers getItem(int position) {
            return offerList.get(position);
        }


    }

    static class ViewHolder {
        // this class is called in getView and assigned it all "items" layouts Views,for smooth scrolling
        TextView Title, City, ratingService, ratingPrice;
        ImageView typeIcon;

        //add by nimer esam for buttons :
        Button loveText, profileText, callText;
    }


    public void ShowMoreBtn(final ListView listView)
    {
        if(listView==null)
            return;
        if(listView.getFooterViewsCount()>=1)// prevent duplications for show more button
            return;


        //listCounter += 1;
        //Log.v("MainController","listCounter: "+listCounter);


        Button showMore = new Button(getContext());
        showMore.setText("Show More");
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                listCounter +=listCounterOriginal;
                DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference().child(Util.RDB_COUNTRY+"/"+
                        Util.RDB_JORDAN+"/"+
                        whichCity+"/"+
                        Util.RDB_OFFERS);
                Query query = DataBaseRoot.orderByChild(Util.RDB_TYPE).equalTo(whichType).limitToFirst(listCounter);
                ValueEventListener QVEL= new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot areaSnapshot: dataSnapshot.getChildren())
                        {
                            Log.v("MainController",""+areaSnapshot.getValue(Offers.class).getTitle());
                            Offers tempOffer = areaSnapshot.getValue(Offers.class);
                            tempOffer.setOfferKey(areaSnapshot.getKey());

                            boolean toAdd=true;
                            for(Offers obj : offerList)
                                if(tempOffer.getOfferKey().equals(obj.getOfferKey()))
                                    toAdd = false;

                            if(toAdd)
                            {
                                offerList.add(tempOffer);
                                //Log.v("MainController","ShowMoreBtn Offer: "+tempOffer.getTitle()+" is NEW");
                            }
                            else{
                                //Log.v("MainController","ShowMoreBtn Offer: "+tempOffer.getTitle()+" Already exists!");
                            }


                        }
                        // sort desc again
                        SortByTimeStampDesc(offerList);
                        offerAdapter.notifyDataSetChanged();
                        listView.smoothScrollToPosition(0);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                    }
                };
                query.addListenerForSingleValueEvent(QVEL);
            }
        });

        listView.addFooterView(showMore);



    }

    //used in list view to set icons to rows

    private Drawable getDrawableResource(int resID) {
        return ContextCompat.getDrawable(getActivity().getApplicationContext(), resID);//context.compat checks the version implicitly
    }

    private void SortByTimeStampDesc(ArrayList<Offers> arrayToSort) {
        //Log.v("Main","Before Sorting:"+ System.currentTimeMillis()/1000);
        Collections.sort(arrayToSort, new Comparator<Offers>() {
            @Override
            public int compare(Offers o1, Offers o2) {
                return o1.getTimeStamp().compareTo(o2.getTimeStamp());
            }
        });

        Collections.reverse(arrayToSort);

        /*for(int i = 0;i<offerList.size();i++)
            Log.v("Main","index: "+i+"|| offer:"+offerList.get(i).getTitle());*/
        //Log.v("Main","Started at: "+StartTime/1000);
        //Log.v("Main","Finished Sorting at:"+ System.currentTimeMillis()/1000);
    }

    static void addToFavoriteList(String offerId, String offerCity, int number){
        userFavoriteEditor.putString("city" + number, offerCity );
        userFavoriteEditor.putString("id" + number, offerId );
        userFavoriteEditor.commit();
    }

    int getNumberOfOffer(String key){
        if(userFavoriteCount == 0)
            return 0;

        for(int i=1; i<= userFavoriteCount; i++)
            if(userFavoriteOffers.getString("id" + i, "") == key)
                return i;

        return 0;
    }

    static void deleteFromFavoriteList(int count){
        if(userFavoriteCount == 0)
            return ;
        for(int i= count; i< userFavoriteCount; i++) {
            userFavoriteEditor.putString("city" + i, userFavoriteOffers.getString("city" + (i+1), "") );
            userFavoriteEditor.putString("id" + i, userFavoriteOffers.getString("id" + (i+1), "") );
            userFavoriteEditor.commit();
        }
    }




}
