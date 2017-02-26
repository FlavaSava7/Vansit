package devgam.vansit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;


public class Main extends Fragment
{
    public Main() {
        // Required empty public constructor
    }

    private DatabaseReference DataBaseRoot;
    private FirebaseAuth firebaseAuth;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    private ListView listView;
    private ArrayList<Offers> offerList;// this will be refilled with Offers each time a user change City Filter
    private ArrayList<Users> userList;// to match offer with the user it has, we are filling in inside the getView
    private ArrayAdapter offerAdapter;
    private static int listCounter = 1;

    private Spinner spinnerCity,spinnerType;
    private static String whichCity=Util.RDB_AMMAN;// to give it a new value in a spinner to fetch new items
    private static String whichType=Util.RDB_CAR;// to give it a new value in a spinner to fetch new items




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

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);// we should show this when he is logged

        if(Util.isLogged())
        {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    AddOffer addOfferPage = new AddOffer();
                    Util.ChangeFrag(addOfferPage,fragmentManager);
                }
            });
        }else
        {
            Log.v("Main","User is not logged in ");
            fab.setVisibility(View.GONE);
        }

        DataBaseRoot = FirebaseDatabase.getInstance().getReference();//connect to DB root
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
                    bundle.putSerializable("userDriver",userList.get(position));
                    bundle.putSerializable("userOffer",offerList.get(position));
                    offerInfoPage.setArguments(bundle);
                    Log.v("Main","Sending to OfferInfo: "+userList.get(position).getName());
                    Log.v("Main","Sending to OfferInfo: "+offerList.get(position).getTitle());

                    Util.ChangeFrag(offerInfoPage,fragmentManager);
                }
        });
        ShowMoreBtn(listView);
        offerAdapter = new itemsAdapter(getContext());
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
                    Log.v("Main", "spinnerCity");
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
                    Log.v("Main", "spinnerType");
                }
                stopAutoFiringCode = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(!offerList.isEmpty())// if we came back to this page dont reload lists
        {
            Log.v("Main","offerList: "+offerList);

        }else
        {
            FillSpinnersAndListView();//To fill City and Type Spinners, And a default Filling of the List View.
        }




    }
    private void FillSpinnersAndListView()
    {
        // must check for internet
        DatabaseReference mRef = DataBaseRoot.child(Util.RDB_COUNTRY+"/"+Util.RDB_JORDAN);

        //City
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.city_list));
        spinnerCity.setAdapter(cityAdapter);
        /*
        final ArrayList <String> cityList = new ArrayList<>();
        ValueEventListener VEL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot areaSnapshot: dataSnapshot.getChildren())
                    cityList.add(areaSnapshot.getKey());
                ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item,cityList);
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(cityAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addListenerForSingleValueEvent(VEL);

        mRef.removeEventListener(VEL); check it later
        */


        //Type
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.type_list));
        spinnerType.setAdapter(typeAdapter);

        //Initial Filling of ListView

        // here we will AUTO go to the child where his Country == the country he signed up in the app
        //Edit it later cuz for now we dont have the User details yet
        Query query = DataBaseRoot.child(Util.RDB_COUNTRY+"/"+
                Util.RDB_JORDAN+"/"+
                Util.RDB_AMMAN+"/"+
                Util.RDB_OFFERS).orderByChild("type").equalTo(Util.RDB_CAR).limitToFirst(listCounter);
        ValueEventListener QVEL= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot areaSnapshot: dataSnapshot.getChildren())
                {
                    Offers tempOffer = areaSnapshot.getValue(Offers.class);
                    tempOffer.setOfferKey(areaSnapshot.getKey());
                    offerList.add(tempOffer);
                }
                listView.setAdapter(offerAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        query.addListenerForSingleValueEvent(QVEL);
    }

    public void ChangeListItems()
    {
        // every time the spinner values change , update list
        //must AUTO input the city of the User
        //must check for internet

        if(whichCity.isEmpty()|| whichCity.equals("")
                ||
                whichType.isEmpty()|| whichType.equals("") )
        {
            return;
        }

        listCounter = 1;
        offerList.clear();

        Query query = DataBaseRoot.child(Util.RDB_COUNTRY+"/"+
                Util.RDB_JORDAN+"/"+
                whichCity+"/"+
                Util.RDB_OFFERS).orderByChild("type").equalTo(whichType).limitToFirst(listCounter);
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
                listView.setAdapter(offerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        query.addListenerForSingleValueEvent(QVEL);
    }

    private class itemsAdapter extends ArrayAdapter<Offers>
    {
        Context context;
        itemsAdapter(Context c)
        {
            super(c, R.layout.fragment_main_listview_items, offerList);
            this.context = c;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent)
        {
            final ViewHolder holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowItem = inflater.inflate(R.layout.fragment_main_listview_items, parent, false);

            Offers tempOffer = offerList.get(position);

            holder.Title = (TextView) rowItem.findViewById(R.id.main_items_TitleData);
            holder.Title.setText(tempOffer.getTitle());

            holder.City = (TextView) rowItem.findViewById(R.id.main_items_cityData);
            holder.City.setText(tempOffer.getCity());

            holder.typeIcon = (ImageView) rowItem.findViewById(R.id.main_items_typeIcon);
            switch(tempOffer.getType())
            {
                case Util.RDB_CAR:holder.typeIcon.setImageDrawable(getDrawableResource(R.drawable.car));break;
                case Util.RDB_BUS:holder.typeIcon.setImageDrawable(getDrawableResource(R.drawable.bus));break;
                case Util.RDB_TAXI:holder.typeIcon.setImageDrawable(getDrawableResource(R.drawable.taxi));break;
                case Util.RDB_TRUCK:holder.typeIcon.setImageDrawable(getDrawableResource(R.drawable.truck));break;
            }


            holder.ratingService = (TextView) rowItem.findViewById(R.id.main_items_serviceRatingData);
            holder.ratingPrice = (TextView) rowItem.findViewById(R.id.main_items_priceRatingData);

            DatabaseReference query = DataBaseRoot.child(Util.RDB_USERS+"/"+tempOffer.getUserID());
            ValueEventListener VEL = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.getValue(Users.class)!=null)
                    {

                        //Log.v("Main:","KEY "+dataSnapshot.getKey());
                        Users tempUser = dataSnapshot.getValue(Users.class);
                        tempUser.setUserKey(dataSnapshot.getKey());

                        boolean toAdd=true;
                        for(Users addedUser:userList)
                            if(tempUser.getUserKey().equals(addedUser.getUserKey()))
                                toAdd=false;

                        if(toAdd)
                            userList.add(tempUser);

                        /*for(Users user:userList)
                            Log.v("Main:","user: "+user.getName());*/

                        holder.ratingService.setText("("+tempUser.getRateService()+"/5)");
                        holder.ratingPrice.setText("("+tempUser.getRatePrice()+"/5)");
                    }
                    else
                    {
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

    static class ViewHolder
    {
        // this class is called in getView and assigned it all "items" layouts Views,for smooth scrolling
        TextView Title, City, ratingService, ratingPrice;
        ImageView typeIcon;
    }


    public void ShowMoreBtn(final ListView listView)
    {
        if(listView==null)
            return;

        //listCounter += 1;
        //Log.v("MainController","listCounter: "+listCounter);

        Button showMore = new Button(getContext());
        showMore.setText("Show More");
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                listCounter +=1;
                Query query = DataBaseRoot.child(Util.RDB_COUNTRY+"/"+
                        Util.RDB_JORDAN+"/"+
                        whichCity+"/"+
                        Util.RDB_OFFERS).orderByChild("type").equalTo(whichType).limitToFirst(listCounter);
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

                        offerAdapter.notifyDataSetChanged();

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

    private Drawable getDrawableResource(int resID)//used in list view to set icons to rows
    {
        return ContextCompat.getDrawable(getActivity().getApplicationContext(), resID);//context.compat checks the version implicitly
    }
}
