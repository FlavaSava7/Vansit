package devgam.vansit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Random;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Requests;
import devgam.vansit.JSON_Classes.Users;


public class Main extends Fragment implements View.OnClickListener{
    public Main() {
        // Required empty public constructor
    }

    public Main(String type){
        whichType = type;
    }

    Button showMore;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    FloatingActionButton addFab, addOffer, addRequest;
    Animation fabOpen, fabClose, fabClockWise, fabAntiClockWise;
    TextView addOfferText, addRequestText, noResultText;
    boolean isFloatingActionOpen = false;

    private ListView listView;
    private ArrayList<Offers> offerList;// this will be refilled with Offers each time a User change City Filter
    private ArrayList<Users> userList;// to match offer with the User it has, we are filling in inside the getView
    private ArrayAdapter offerAdapter;
    private static int listCounter = 5;
    private static final int listCounterOriginal = listCounter;
    private static final int recentOfferCounter = 20;
    public Map<Offers, View> ViewMap=new HashMap<>();

    private Spinner spinnerCity,spinnerType;
    private static String whichCity="";// to give it a new value in a spinner to fetch new items
    private static String whichType="";// to give it a new value in a spinner to fetch new items
    private static ArrayList<String> allCities;//this will contain the values that are in strings.xml
    private static ArrayList<String> allTypes;//this will contain the values that are in strings.xml, used inside the getView to choose icon for type
    String[] cityArray;


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
        Util.ChangePageTitle(getActivity(),R.string.menu_home_text);

        cityArray = new String[]{
                getResources().getString(R.string.select_city),
                getResources().getString(R.string.Amman),
                getResources().getString(R.string.Zarqa),
                getResources().getString(R.string.Irbid),
                getResources().getString(R.string.Ajloun),
                getResources().getString(R.string.Aqaba),
                getResources().getString(R.string.Jarash),
                getResources().getString(R.string.Maan),
                getResources().getString(R.string.Madaba),
                getResources().getString(R.string.Mafraq),
                getResources().getString(R.string.Balqa),
                getResources().getString(R.string.Karak),
                getResources().getString(R.string.Tafila)
        };

        allCities  = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.city_list)));
        allTypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.type_list)));

        //String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log.v("Main","Token "+refreshedToken);

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
        FillSpinners();

        offerList = new ArrayList<>();
        userList = new ArrayList<>();
        FillListView();

        if(whichCity!=null && !whichCity.equals("")
                && whichType!=null && !whichType.equals("") )
        {
            whichCity = "Select City";
            whichType = "Select Type";
            spinnerCity.setSelection(0);
            spinnerType.setSelection(0);
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
                Bundle bundle = new Bundle();
                bundle.putSerializable("userOffer",offerList.get(position));
                for(Users user:userList)// cuz userList is distinct
                    if(offerList.get(position).getUserID().equals(user.getUserID()))
                        bundle.putSerializable("userDriver",user);
                Intent intent = new Intent(getActivity(), moreOfferInformation.class);
                intent.putExtra("Bundle",bundle);
                startActivity(intent);

            }
        });

    }

    private void FillSpinners()
    {

        //City
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,allCities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        //Type
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,
                allTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }
    private void FillListView()
    {
        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference()
                .child(Util.RDB_OFFERS);
        Query query = DataBaseRoot.orderByChild(Util.TIME_STAMP).limitToLast(recentOfferCounter);
        ValueEventListener QVEL= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot everyOffer : dataSnapshot.getChildren())
                {
                    Offers tempOffer = everyOffer.getValue(Offers.class);
                    tempOffer.setOfferKey(everyOffer.getKey());
                    offerList.add(tempOffer);
                }
                // sort them by time desc
                Util.SortByTimeStampDesc(offerList);

                listView.setAdapter(offerAdapter);
                try {
                    getActivity().findViewById(R.id.loadingPanel_main).setVisibility(View.GONE);
                } catch (Exception e){
                }

                if(offerList.isEmpty())
                    noResultText.setVisibility(View.VISIBLE);
                else
                    noResultText.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                getActivity().findViewById(R.id.loadingPanel_main).setVisibility(View.GONE);
            }
        };
        query.addListenerForSingleValueEvent(QVEL);
        if(!offerList.isEmpty())
            ShowMoreBtn(listView);
    }

    public void ChangeListItems() {
        // every time the spinner values change , update list

        if(whichCity.isEmpty()|| whichCity.equals("Select City") ||
                whichType.isEmpty()|| whichType.equals("Select Type") )
        {

            return;
        }

        offerList.clear();
        userList.clear();
        ViewMap.clear();
        if(!offerAdapter.isEmpty())
            offerAdapter.clear();


        listCounter = listCounterOriginal;//reset


        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference()
                .child(Util.RDB_OFFERS);
        Query query = DataBaseRoot.orderByChild(Util.RDB_TYPE).equalTo(whichType).limitToLast(listCounter);
        ValueEventListener QVEL= new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot areaSnapshot: dataSnapshot.getChildren())
                {

                    Offers tempOffer = areaSnapshot.getValue(Offers.class);
                    if(!tempOffer.getCity().equals(whichCity))
                        continue;
                    tempOffer.setOfferKey(areaSnapshot.getKey());
                    offerList.add(tempOffer);
                }

                // sort desc
                Util.SortByTimeStampDesc(offerList);
                listView.setAdapter(offerAdapter);
                try {
                    getActivity().findViewById(R.id.loadingPanel_main).setVisibility(View.GONE);
                } catch (Exception e){
                }

                if(offerList.isEmpty())
                    noResultText.setVisibility(View.VISIBLE);
                else
                    noResultText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        query.addListenerForSingleValueEvent(QVEL);

        if(!offerList.isEmpty())
            ShowMoreBtn(listView);

    }

    @Override
    public void onClick(View v) {
        if(v == addRequest || v == addRequestText)
        {
            //foo();
            //boo2();
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
        public View getView(final int position, View convertView, final ViewGroup parent)
        {

            final ViewHolder holder = new ViewHolder();
            final Offers tempOffer = offerList.get(position);
            View rowItem;

            if(!ViewMap.containsKey(tempOffer))
            {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowItem = inflater.inflate(R.layout.fragment_main_listview_items, parent, false);

                holder.Title = (TextView) rowItem.findViewById(R.id.main_items_TitleData);
                holder.Title.setText(tempOffer.getTitle());

                holder.City = (TextView) rowItem.findViewById(R.id.main_items_cityData);
                holder.City.setText(tempOffer.getCity());

                holder.Type = (TextView) rowItem.findViewById(R.id.main_items_typeData);
                holder.Type.setText(tempOffer.getType());

                holder.typeIcon = (ImageView) rowItem.findViewById(R.id.main_items_typeIcon);
                holder.typeIcon.setImageDrawable(Util.getDrawableResource(getActivity(), Util.changeIcon(tempOffer.getType())));

                holder.ratingService = (TextView) rowItem.findViewById(R.id.main_items_serviceRatingData);
                holder.ratingPrice = (TextView) rowItem.findViewById(R.id.main_items_priceRatingData);

                holder.userRating = (RatingBar) rowItem.findViewById(R.id.main_items_user_rate);
                holder.priceRating = (RatingBar) rowItem.findViewById(R.id.main_items_price_rate);

                holder.profileText = (LinearLayout) rowItem.findViewById(R.id.main_items_profile_layout);
                holder.callText = (LinearLayout) rowItem.findViewById(R.id.main_items_call_layout);
                Query query = databaseReference.child(tempOffer.getUserID());

                ValueEventListener VEL = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.getValue(Users.class)!=null) {
                            final Users tempUser = dataSnapshot.getValue(Users.class);
                            tempUser.setUserID(dataSnapshot.getKey());

                            boolean toAdd = true;
                            for(Users addedUser:userList)
                                if(tempUser.getUserID().equals(addedUser.getUserID()))
                                    toAdd=false;

                            if(toAdd)
                                userList.add(tempUser);

                            holder.ratingService.setText("("+(int) tempUser.getRateService()+")" + " " +Util.getRateDesc(getActivity(), 1,(int) tempUser.getRateService() ));
                            holder.ratingPrice.setText("("+(int) tempUser.getRatePrice()+")" +  " " +Util.getRateDesc(getActivity(), 2,(int) tempUser.getRatePrice() ));

                            holder.userRating.setRating(tempUser.getRateService());
                            holder.priceRating.setRating(tempUser.getRatePrice());



                            //add by nimer esam :
                            //To make call when User click on call text :
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
                ViewMap.put(tempOffer, rowItem);
            }
            else
            {
                rowItem = ViewMap.get(tempOffer);
            }


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
        TextView Title, City, Type, ratingService, ratingPrice;
        ImageView typeIcon;
        private RatingBar userRating, priceRating;

        //add by nimer esam for buttons :
        LinearLayout profileText, callText;
    }

    public void ShowMoreBtn(final ListView listView) {
        if(listView==null)
            return;
        if(listView.getFooterViewsCount()>=1)// prevent duplications for show more button
            return;

        showMore = new Button(getContext());
        showMore.setText(getResources().getString(R.string.main_show_more));
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //whichType = spinnerType.getSelectedItem().toString();
                //whichCity = spinnerCity.getSelectedItem().toString();

                listCounter +=listCounterOriginal;
                DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference().child(Util.RDB_OFFERS);
                Query query = DataBaseRoot.orderByChild(Util.RDB_TYPE).equalTo(whichType).limitToLast(listCounter);
                ValueEventListener QVEL= new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot areaSnapshot: dataSnapshot.getChildren())
                        {
                            //Log.v("MainController",""+areaSnapshot.getValue(Offers.class).getTitle());
                            Offers tempOffer = areaSnapshot.getValue(Offers.class);
                            if(!tempOffer.getCity().equals(whichCity))
                                continue;
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


    void foo()
    {
        ArrayList<String> s = new ArrayList<>();
        s.add("22 Olive Rd. ");
        s.add("Melrose, MA 02176");
        s.add("81 Cedar Swamp Drive ");
        s.add("East Hartford, CT 06118");
        s.add("490 Cleveland Court ");
        s.add("Middle Village, NY 11379");
        s.add("13 Fairview Street ");
        s.add("Bensalem, PA 19020");
        s.add("866 Leatherwood Drive ");
        s.add("Petersburg, VA 23803");
        s.add("9542 Military Street");
        s.add("Rockaway, NJ 07866");
        s.add("8342 Sherman Street ");
        s.add("Hollywood, FL 33020");
        s.add("74 Peachtree Ave. ");
        s.add("Southfield, MI 48076");
        s.add("9414 Hickory St. ");
        s.add("Garfield, NJ 07026");
        s.add("7 N. Sugar Street ");
        s.add("Strongsville, OH 44136");
        s.add("8085 Birch Hill St. ");
        s.add("Harrisonburg, VA 22801");
        s.add("92 East Deerfield Ave. ");
        s.add("Inman, SC 29349");
        s.add("49 Woodsman Street ");
        s.add("Gainesville, VA 20155");
        s.add("306 Alton St. ");
        s.add("Owensboro, KY 42301");
        s.add("690 Trenton Ave.");
        s.add("Chesterton, IN 46304");
        s.add("Goldsboro, NC 27530");
        s.add("Fremont, OH 43420");
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        Random randomGenerator = new Random();
        for(int i =0; i<s.size();i++)
        {
            int randomInt = randomGenerator.nextInt(4);
            String type = "";
            switch (randomInt) {
                case 0:
                    type = "Car";
                    break;
                case 1:
                    type = "Bus";
                    break;
                case 2:
                    type = "Truck";
                    break;
                case 3:
                    type = "Taxi";
                    break;
                default:
                    type = "Car";
                    break;
            }
            String city = "";
            switch (randomInt) {
                case 0:
                    city = "Amman";
                    break;
                case 1:
                    city = "Zarqa";
                    break;
                case 2:
                    city = "Irbid";
                    break;
                case 3:
                    city = "Ajloun";
                    break;
                default:
                    city = "Amman";
                    break;
            }
            Users users;

            switch (randomInt) {
                case 0:
                    users = new Users("Ahmad","Khaled","Amman","0796565604","MALE","5","7","1995");
                    users.setUserID("ID"+i);
                    break;
                case 1:
                    users = new Users("Lara","Manaseer","Zarqa","0796762540","FEMALE","7","1","1999");
                    users.setUserID("ID"+i);
                    break;
                case 2:
                    users = new Users("Abdullah","Asiri","Irbid","0796659860","MALE","5","4","1990");
                    users.setUserID("ID"+i);
                    break;
                case 3:
                    users = new Users("Jamal","Moath","Ajloun","0796121160","MALE","7","12","1997");
                    users.setUserID("ID"+i);
                    break;
                default:
                    users = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");
                    users.setUserID("ID"+i);
                    break;
            }

            if(randomInt%2==0)
            {
                Requests requests1 = new Requests(users,"Title"+i,"Desc"+i,type,city,s.get(i),
                        32.021371+((double)i/1000),35.848829+((double)i/1000),
                        System.currentTimeMillis(), FirebaseInstanceId.getInstance().getToken());
                myRef.child(Util.RDB_REQUESTS+"/"+"ID"+i).setValue(requests1);
            }else
            {

                Requests requests1 = new Requests(users,"Title"+i,"Desc"+i,type,city,s.get(i),

                        32.021371-((double)i/1000),35.848829-((double)i/1000),
                        System.currentTimeMillis(), FirebaseInstanceId.getInstance().getToken());
                myRef.child(Util.RDB_REQUESTS+"/"+users.getUserID()).setValue(requests1);
            }

        }

    }

    void boo()
    {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        List<Offers> offers = new ArrayList<>();
        Random randomGenerator = new Random();
        for(int i = 0;i<100;i++)
        {
                int randomInt = randomGenerator.nextInt(4);
                String type = "";
                switch (randomInt) {
                case 0:
                type = "Car";
                break;
                case 1:
                type = "Bus";
                break;
                case 2:
                type = "Truck";
                break;
                case 3:
                type = "Taxi";
                break;
            default:
                type = "Car";
                break;
                }
                String city = "";
                switch (randomInt) {
                case 0:
                city = "Amman";
                break;
                case 1:
                city = "Zarqa";
                break;
                case 2:
                city = "Irbid";
                break;
                case 3:
                city = "Ajloun";
                break;
            default:
                city = "Amman";
                break;
                }
                Offers offer = new Offers("JRtqgsjvHvMIsSLQVVs6EDNfL582",
                "Title" + i, "Desc" + i, type, city, Util.RDB_JORDAN, System.currentTimeMillis() + i * 10);
                offers.add(i, offer);
                myRef.child("Offers").push().setValue(offer);
        }
    }
    void boo2()
    {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        List<Users> usersList = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        Random randomGenerator = new Random();


        for(int i = 0 ; i<25 ; i++)
        {
            int randomInt = randomGenerator.nextInt(4);
            ids.add(String.valueOf((i*randomInt+(randomInt*450))));
        }


        Users tempUser = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser);

        Users tempUser2 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser2);

        Users tempUser3 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser3);

        Users tempUser4 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser4);

        Users tempUser5 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser5);

        Users tempUser6 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser6);

        Users tempUser7 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser7);

        Users tempUser8 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser8);

        Users tempUser9 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser9);

        Users tempUser10 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser10);

        Users tempUser11 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser11);

        Users tempUser12 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser12);

        Users tempUser13 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser13);

        Users tempUser14 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser14);

        Users tempUser15 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser15);

        Users tempUser16 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser16);

        Users tempUser17 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser17);

        Users tempUser18 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser18);


        Users tempUser19 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser19);

        Users tempUser20 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser20);

        Users tempUser21 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser21);

        Users tempUser22 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser22);

        Users tempUser23 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser23);

        Users tempUser24 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser24);

        Users tempUser25 = new Users("Sara","Imran","Ajloun","0790000000","FEMALE","9","9","1993");

        usersList.add(tempUser25);

        for(int k = 0 ; k<ids.size();k++)
        {
            usersList.get(k).setUserID(ids.get(k));
        }
        for(Users user : usersList)
        {
            String pushKey = myRef.child("Users").push().getKey();
            myRef.child("Users/"+pushKey).setValue(user);
        }

    }
}

