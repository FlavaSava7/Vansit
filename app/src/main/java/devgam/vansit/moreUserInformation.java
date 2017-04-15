package devgam.vansit;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

public class moreUserInformation extends AppCompatActivity {

    private ImageView userImage;
    private TextView userName, userAge, userCity,  userRatingDesc, priceRatingDesc;
    private ListView listView;
    private ArrayList<Offers> offerList;// this will be refilled with Offers each time a User change City Filter
    private ArrayAdapter offerAdapter;
    private RatingBar userRating, priceRating;
    private LinearLayout callLayout, rateLayout;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    //Users userDriver = null;
    Users userDriver;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_user_information);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userImage = (ImageView) findViewById(R.id.more_user_information_img);
        userName = (TextView) findViewById(R.id.more_user_information_name);
        userAge = (TextView) findViewById(R.id.more_user_information_age);
        userCity = (TextView) findViewById(R.id.more_user_information_city);
        userRatingDesc = (TextView) findViewById(R.id.more_user_information_user_rate_desc);
        priceRatingDesc = (TextView) findViewById(R.id.more_user_information_price_rate_desc);
        listView = (ListView) findViewById(R.id.more_user_information_list);
        userRating = (RatingBar) findViewById(R.id.more_user_information_user_rate);
        priceRating = (RatingBar) findViewById(R.id.more_user_information_price_rate);
        callLayout = (LinearLayout) findViewById(R.id.more_user_information_call_layout);
        rateLayout = (LinearLayout) findViewById(R.id.more_user_information_rate_layout);
        offerAdapter = new moreUserInformation.itemsAdapter(this);
        fragmentManager  = getSupportFragmentManager();

        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            userDriver = (Users) bundle.getSerializable("userDriver");
        } catch (Exception e){

        }


        /*Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            userDriver = (Users) bundle.getSerializable("userDriver");
        }*/

        //offerAdapter = new itemsAdapter(this);
        setUpInfo();

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    void setUpInfo() {
        if (!Util.IS_USER_CONNECTED) {
            // error msg
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);


        if (userDriver.getGender().equals("male"))
            userImage.setImageResource(R.drawable.ic_user_male);
        else
            userImage.setImageResource(R.drawable.ic_user_female);

        userName.setText(userDriver.getFirstName() + " " + userDriver.getLastName());

        int age = Util.yearNow - Integer.parseInt(userDriver.getDateYear());
        age = (Util.monthNow > Integer.parseInt(userDriver.getDateMonth()) ? age : age - 1);
        userAge.setText(getResources().getString(R.string.user_information_age)+ " "+ age );

        userCity.setText(getResources().getString(R.string.user_information_home_city)+ " "+ userDriver.getCity());


        try {
            userRating.setRating(Float.parseFloat(userDriver.getRateService() + ""));
            priceRating.setRating(Float.parseFloat(userDriver.getRatePrice() + ""));
        } catch (Exception e){

        }

        int userRate = Math.round(Float.parseFloat(userDriver.getRateService() + ""));
        int priceRate = Math.round(Float.parseFloat(userDriver.getRatePrice() + ""));

        userRatingDesc.setText("("+userRate+") "+Util.getRateDesc(this, 1, userRate));
        priceRatingDesc.setText("("+priceRate+") "+Util.getRateDesc(this, 2, priceRate));

        //userPhone.setText("Phone: "+ userDriver.getPhone());
        callLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + userDriver.getPhone()));
                startActivity(intent);
            }
        });



        final ratingDialog rateUser = new ratingDialog(this, userDriver);
        rateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Util.isLogged()) {
                    // User is not logged
                    Util.makeSnackbar(v, getResources().getString(R.string.user_not_logged));
                    return;
                }
                rateUser.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                rateUser.show();
            }
        });


        if(offerList == null) {
            Util.ProgDialogStarter(progressDialog,getResources().getString(R.string.loading));
            offerList = new ArrayList<>();

            DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference()
                    .child(Util.RDB_OFFERS);

            Query query = DataBaseRoot.orderByChild(Util.USER_ID).equalTo(userDriver.getUserID());
            ValueEventListener QVEL= new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(!dataSnapshot.exists())// This User have no offer
                    {
                        Util.makeToast(getApplicationContext(), userDriver.getFirstName()+" Does not have any Offers");
                    }
                    else {
                        for (DataSnapshot offers : dataSnapshot.getChildren()) {
                            //Log.v("Main","Key:"+offers.getKey());
                            Offers tempOffer = offers.getValue(Offers.class);
                            tempOffer.setOfferKey(offers.getKey());

                            boolean toAdd = true;
                            for (Offers offer : offerList)
                                if (offer.getOfferKey().equals(tempOffer.getOfferKey()))
                                    toAdd = false;

                            if (toAdd)
                                if (tempOffer.getUserID().equals(userDriver.getUserID()))
                                    offerList.add(tempOffer);
                        }

                        for (Offers offer : offerList) {
                            //Log.v("Main", "offerList for this User: " + offer.getOfferKey());
                        }
                        Util.SortByTimeStampDesc(offerList);
                        listView.setAdapter(offerAdapter);
                    }
                    Util.ProgDialogDelay(progressDialog,100L);
                }
                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                }
            };
            query.addListenerForSingleValueEvent(QVEL);
        }
        else {
            listView.setAdapter(offerAdapter);
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // click to go to offerinfo page

                Bundle bundle = new Bundle();
                bundle.putSerializable("userOffer",offerList.get(position));
                bundle.putSerializable("userDriver",userDriver);
                Intent intent = new Intent(moreUserInformation.this, moreOfferInformation.class);
                //intent.putExtra("userDriver", userDriver);
                intent.putExtra("Bundle",bundle);
                startActivity(intent);

            }
        });
    }


    private class itemsAdapter extends ArrayAdapter<Offers>
    {
        Context context;
        itemsAdapter(Context c) {
            super(c, R.layout.activity_more_user_information_list_item, offerList);
            this.context = c;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent)
        {
            final moreUserInformation.ViewHolder holder = new moreUserInformation.ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowItem = inflater.inflate(R.layout.activity_more_user_information_list_item, parent, false);

            Offers tempOffer = offerList.get(position);

            holder.Title = (TextView) rowItem.findViewById(R.id.more_user_information_list_items_title);
            holder.Title.setText(tempOffer.getTitle());

            holder.City = (TextView) rowItem.findViewById(R.id.more_user_information_list_items_city);
            holder.City.setText(tempOffer.getCity());

            holder.Type = (TextView) rowItem.findViewById(R.id.more_user_information_list_items_type);
            holder.Type.setText(tempOffer.getType());

            holder.Desc = (TextView) rowItem.findViewById(R.id.more_user_information_list_items_desc);
            holder.Desc.setText(tempOffer.getDescription());

            holder.typeIcon = (ImageView) rowItem.findViewById(R.id.more_user_information_list_item_img);
            holder.typeIcon.setImageDrawable(Util.getDrawableResource(moreUserInformation.this, Util.changeIcon(tempOffer.getType())));
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
        TextView Title, City, Type, Desc;
        ImageView typeIcon;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



}
