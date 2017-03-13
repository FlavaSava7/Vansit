package devgam.vansit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import com.google.firebase.auth.FirebaseAuth;
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
import java.util.List;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;


public class Main extends Fragment implements View.OnClickListener{
    public Main() {
        // Required empty public constructor
    }


    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    FloatingActionButton addFab, addOffer, addRequest;
    Animation fabOpen, fabClose, fabClockWise, fabAntiClockWise;
    TextView addOfferText, addRequestText, noResultText;
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

        addFab = (FloatingActionButton) getActivity().findViewById(R.id.add_fab);// we should show this when he is logged
        addOffer = (FloatingActionButton) getActivity().findViewById(R.id.fab_add_offer);
        addRequest = (FloatingActionButton) getActivity().findViewById(R.id.fab_add_request);

        addOfferText = (TextView) getActivity().findViewById(R.id.fab_add_offer_text);
        addRequestText = (TextView) getActivity().findViewById(R.id.fab_add_request_text);
        noResultText = (TextView) getActivity().findViewById(R.id.main_no_result_text);

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
                        addOffer.startAnimation(fabClose);
                        addRequest.setClickable(false);
                        addOffer.setClickable(false);
                        addRequestText.setVisibility(View.INVISIBLE);
                        addOfferText.setVisibility(View.INVISIBLE);
                        isFloatingActionOpen = false;
                    }else {
                        addFab.startAnimation(fabClockWise);
                        addRequest.startAnimation(fabOpen);
                        addOffer.startAnimation(fabOpen);
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

        //FAB On Click Listener from overriden method :
        addOffer.setOnClickListener(this);
        addOfferText.setOnClickListener(this);
        addRequest.setOnClickListener(this);
        addRequestText.setOnClickListener(this);

        fragmentManager  = getActivity().getSupportFragmentManager();

        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS);
        offerAdapter = new itemsAdapter(getContext(),DataBaseRoot);
        listView = (ListView) getActivity().findViewById(R.id.frag_main_listview);

        spinnerCity = (Spinner)getActivity().findViewById(R.id.frag_main_spinCity);
        spinnerType = (Spinner)getActivity().findViewById(R.id.frag_main_spinType);

        if(offerList==null && userList==null)
        {
            offerList = new ArrayList<>();
            userList = new ArrayList<>();
            FillSpinnersAndListView();
        }
        else
        {
            listView.setAdapter(offerAdapter);
        }

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
                        if(offerList.get(position).getUserID().equals(user.getUserID()))
                            bundle.putSerializable("userDriver",user);

                    offerInfoPage.setArguments(bundle);
                    Util.ChangeFrag(offerInfoPage,fragmentManager);
                }
        });
    }

    private void FillSpinnersAndListView()
    {

        /*// must check for internet
        if(!Util.IS_USER_CONNECTED)
        {
            // show msg
            return;
        }*/

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

        //final ProgressDialog progressDialog = new ProgressDialog(getContext(),ProgressDialog.STYLE_SPINNER);
        //Util.ProgDialogStarter(progressDialog,"Loading...");

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
                Util.SortByTimeStampDesc(offerList);

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
                try {
                    getActivity().findViewById(R.id.loadingPanel_main).setVisibility(View.GONE);
                } catch (Exception e){

                }
                //Util.ProgDialogDelay(progressDialog,100L);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                getActivity().findViewById(R.id.loadingPanel_main).setVisibility(View.GONE);
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
                Util.SortByTimeStampDesc(offerList);
                listView.setAdapter(offerAdapter);
            }




            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        query.addListenerForSingleValueEvent(QVEL);

    }

    @Override
    public void onClick(View v) {
        if(v == addRequest || v == addRequestText)
        {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            addRequest addRequestPage = new addRequest();
            Util.ChangeFrag(addRequestPage, fragmentManager);
        } if( v == addOffer || v == addOfferText)
        {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            AddOffer addOfferPage = new AddOffer();
            Util.ChangeFrag(addOfferPage, fragmentManager);
        }
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
            holder.typeIcon.setImageDrawable(Util.getDrawableResource(getActivity(), Util.changeIcon(tempOffer.getType())));

            holder.ratingService = (TextView) rowItem.findViewById(R.id.main_items_serviceRatingData);
            holder.ratingPrice = (TextView) rowItem.findViewById(R.id.main_items_priceRatingData);

            //initialized by nimer esam for text buttons on list item :
            //holder.loveText = (Button) rowItem.findViewById(R.id.main_items_love_text);
            holder.profileText = (LinearLayout) rowItem.findViewById(R.id.main_items_profile_layout);
            holder.callText = (LinearLayout) rowItem.findViewById(R.id.main_items_call_layout);
            Query query = databaseReference.child(tempOffer.getUserID());

            ValueEventListener VEL = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.getValue(Users.class)!=null) {

                        //Log.v("Main:","KEY "+dataSnapshot.getKey());
                        final Users tempUser = dataSnapshot.getValue(Users.class);
                        tempUser.setUserID(dataSnapshot.getKey());

                        boolean toAdd = true;
                        for(Users addedUser:userList)
                            if(tempUser.getUserID().equals(addedUser.getUserID()))
                                toAdd=false;

                        if(toAdd)
                            userList.add(tempUser);




                        holder.ratingService.setText("("+tempUser.getRateService()+"/5)");
                        holder.ratingPrice.setText("("+tempUser.getRatePrice()+"/5)");

                        //add by nimer esam :
                        //To make call when user click on call text :
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
        LinearLayout profileText, callText;
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
                        Util.SortByTimeStampDesc(offerList);
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


}
