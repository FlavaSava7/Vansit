package devgam.vansit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

public class moreOfferInformation extends AppCompatActivity {

    public Users userDriver = null; // the one who posted the offer
    public Offers userOffer = null; // the offer itself

    DatabaseReference DataBaseRoot;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    private TextView Title,Description,Name, City, Age, HomeCity, Type, ratingNameService, ratingNamePrice;
    private RatingBar ratingService, ratingPrice;
    private LinearLayout callLayout, profileLayout, favLayout;
    private ListView recommendList;
    ArrayList<Offers> offersArrayList;
    ArrayAdapter arrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_offer_information);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_offers);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("Bundle");
        if (bundle != null)
        {
            userDriver = (Users) bundle.getSerializable("userDriver");
            userOffer = (Offers) bundle.getSerializable("userOffer");
        }

        //collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.more_offer_toolbar_layout);
        //AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.more_offer_app_bar);

       //collapsingToolbarLayout.setBackgroundResource(R.drawable.no_photo);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        if(userDriver==null | userOffer==null)
        {
            Log.v("Main","userDriver OR userOffer = NULL");
            //SHOW ERRIR MSG ?
        }

        DataBaseRoot = FirebaseDatabase.getInstance().getReference();//connect to DB root
        fragmentManager = getSupportFragmentManager();

        Title = (TextView) findViewById(R.id.offerInfo_titleData);
        Description = (TextView) findViewById(R.id.offerInfo_descData);
        Name = (TextView) findViewById(R.id.offerInfo_nameData);
        City = (TextView) findViewById(R.id.offerInfo_cityData);
        Type = (TextView) findViewById(R.id.offerInfo_typeData);
        Age = (TextView) findViewById(R.id.offerInfo_ageData);
        HomeCity = (TextView) findViewById(R.id.offerInfo_homeCityData);
        ratingNameService = (TextView) findViewById(R.id.offerInfo_serviceRatingName);
        ratingNamePrice = (TextView) findViewById(R.id.offerInfo_priceRatingName);
        recommendList = (ListView) findViewById(R.id.more_offer_information_recommend_list);

        callLayout = (LinearLayout) findViewById(R.id.offerInfo_callLayout);
        favLayout = (LinearLayout) findViewById(R.id.offerInfo_favLayout);
        profileLayout = (LinearLayout) findViewById(R.id.offerInfo_profileLayout);


        ratingService = (RatingBar) findViewById(R.id.offerInfo_serviceRatingData);
        ratingPrice = (RatingBar) findViewById(R.id.offerInfo_priceRatingData);

        fillRecommendedList();

        arrayAdapter = new moreOfferInformation.itemAdapter(this);
        offersArrayList = new ArrayList<>();
        recommendList.setAdapter(arrayAdapter);




        SetUpInfo();
    }

    void SetUpInfo() {
        int age = Util.yearNow - Integer.parseInt(userDriver.getDateYear());
        age = (Util.monthNow > Integer.parseInt(userDriver.getDateMonth()) ? age : age - 1);

        Title.setText(userOffer.getTitle());
        Description.setText(userOffer.getDescription());
        Name.setText(userDriver.getFirstName()+" "+userDriver.getLastName());
        HomeCity.setText(userDriver.getCity());
        Age.setText("" + age );
        City.setText(userOffer.getCity());
        Type.setText(userOffer.getType());

        try {
            ratingService.setRating(Float.parseFloat(userDriver.getRateService() + ""));
            ratingPrice.setRating(Float.parseFloat(userDriver.getRatePrice() + ""));
        } catch (Exception e){

        }

        int userRate = Math.round(Float.parseFloat(userDriver.getRateService() + ""));
        int priceRate = Math.round(Float.parseFloat(userDriver.getRatePrice() + ""));

        ratingNameService.setText("("+userRate+") "+Util.getRateDesc(this, 1, userRate));
        ratingNamePrice.setText("("+priceRate+") "+Util.getRateDesc(this, 2, priceRate));


        callLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + userDriver.getPhone()));
                startActivity(intent);
            }
        });

        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(moreOfferInformation.this, moreUserInformation.class);
                intent.putExtra("userDriver", userDriver);
                startActivity(intent);
            }
        });

        favLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : add to fav List
            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void fillRecommendedList(){
        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference()
                .child(Util.RDB_OFFERS);
        Query query = DataBaseRoot.orderByChild(Util.TIME_STAMP).limitToLast(10);

        final recommendedSharedPref recommended = new recommendedSharedPref(moreOfferInformation.this);
        Toast.makeText(getApplicationContext(), recommended.getFavType(), Toast.LENGTH_SHORT ).show();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot allOffers : dataSnapshot.getChildren()){
                    Offers tempOffer = allOffers.getValue(Offers.class);
                    tempOffer.setOfferKey(allOffers.getKey());

                    //For Recommend
                    if(tempOffer.getType().equals("Bus"))
                    //if(tempOffer.getUserID().equals())
                        offersArrayList.add(tempOffer);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addValueEventListener(valueEventListener);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public class itemAdapter extends ArrayAdapter<Offers>{

        Context context;

        public itemAdapter(Context context) {
            super(context, R.layout.fragment_main_listview_items, offersArrayList);
            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final viewHolder holder = new viewHolder();
            final Offers tempOffer = offersArrayList.get(position);
            View rowItem;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowItem = inflater.inflate(R.layout.fragment_main_listview_items, parent, false);

            holder.Title = (TextView) rowItem.findViewById(R.id.main_items_TitleData);
            holder.Title.setText(tempOffer.getTitle());

            holder.City = (TextView) rowItem.findViewById(R.id.main_items_cityData);
            holder.City.setText(tempOffer.getCity());

            holder.Type = (TextView) rowItem.findViewById(R.id.main_items_typeData);
            holder.Type.setText(tempOffer.getType());

            holder.typeIcon = (ImageView) rowItem.findViewById(R.id.main_items_typeIcon);
            holder.typeIcon.setImageDrawable(Util.getDrawableResource(moreOfferInformation.this, Util.changeIcon(tempOffer.getType())));

            holder.ratingService = (TextView) rowItem.findViewById(R.id.main_items_serviceRatingData);
            holder.ratingPrice = (TextView) rowItem.findViewById(R.id.main_items_priceRatingData);

            holder.userRating = (RatingBar) rowItem.findViewById(R.id.main_items_user_rate);
            holder.priceRating = (RatingBar) rowItem.findViewById(R.id.main_items_price_rate);

            //initialized by nimer esam for text buttons on list item :
            holder.profileText = (LinearLayout) rowItem.findViewById(R.id.main_items_profile_layout);
            holder.callText = (LinearLayout) rowItem.findViewById(R.id.main_items_call_layout);

            try {
                holder.userRating.setRating(Float.parseFloat(userDriver.getRateService() + ""));
                holder.priceRating.setRating(Float.parseFloat(userDriver.getRatePrice() + ""));
            } catch (Exception e){

            }

            int userRate = Math.round(Float.parseFloat(userDriver.getRateService() + ""));
            int priceRate = Math.round(Float.parseFloat(userDriver.getRatePrice() + ""));

            holder.ratingService.setText("("+userRate+") "+Util.getRateDesc(moreOfferInformation.this, 1, userRate));
            holder.ratingPrice.setText("("+priceRate+") "+Util.getRateDesc(moreOfferInformation.this, 2, priceRate));

            return rowItem;
        }

        @Override
        public int getCount() {
            return offersArrayList.size();
        }


        @Override
        public Offers getItem(int position) {
            return offersArrayList.get(position);
        }
    }

    static class viewHolder{
        TextView Title, City, Type, ratingService, ratingPrice;
        ImageView typeIcon;
        private RatingBar userRating, priceRating;

        //add by nimer esam for buttons :
        LinearLayout profileText, callText;
    }


}
