package devgam.vansit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;
import devgam.vansit.R;

public class MoreOffers extends Fragment {


    private ImageView userImage;
    private TextView userName,userAge,userCity,userPhone;

    private ListView listView;
    private ArrayList<Offers> offerList;// this will be refilled with Offers each time a user change City Filter
    private ArrayAdapter offerAdapter;

    Users userDriver = null;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method
    DatabaseReference DataBaseRoot;
    public MoreOffers() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            userDriver = (Users) bundle.getSerializable("userDriver");
        }
        return inflater.inflate(R.layout.fragment_more_offers, container, false);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if(fab!=null)
            fab.setVisibility(View.GONE);

        userImage = (ImageView) getActivity().findViewById(R.id.moreOffers_img);
        userName = (TextView) getActivity().findViewById(R.id.moreOffers_name);
        userAge = (TextView) getActivity().findViewById(R.id.moreOffers_age);
        userCity = (TextView) getActivity().findViewById(R.id.moreOffers_city);
        userPhone = (TextView) getActivity().findViewById(R.id.moreOffers_phone);
        listView = (ListView) getActivity().findViewById(R.id.moreOffers_offersList);
        offerAdapter = new itemsAdapter(getContext());
        offerList = new ArrayList<>();
        DataBaseRoot = FirebaseDatabase.getInstance().getReference();//connect to DB root
        fragmentManager = getActivity().getSupportFragmentManager();

        setUpInfo();



    }
    void setUpInfo()
    {
        if(!Util.IS_USER_CONNECTED)
        {
            // error msg
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(getContext(),ProgressDialog.STYLE_SPINNER);
        Util.ProgDialogStarter(progressDialog,"Loading...");

        if (userDriver.getGender().equals("male"))
            userImage.setImageResource(R.drawable.ic_user_male);
        else
            userImage.setImageResource(R.drawable.ic_user_female);

        userName.setText(userDriver.getFirstName()+" "+userDriver.getLastName());

        int age =  Util.yearNow - Integer.parseInt(userDriver.getDateYear()) ;
        age = (Util.monthNow > Integer.parseInt(userDriver.getDateMonth()) ? age : age -1 );
        userAge.setText("Age is " + age + " years old");

        userCity.setText("City: " + userDriver.getCity());

        userPhone.setText("Phone: "+ userDriver.getPhone());
        userPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+userDriver.getPhone()));
                startActivity(intent);
            }
        });

        Query query = DataBaseRoot.child(Util.RDB_COUNTRY+"/"+Util.RDB_JORDAN);// not efficient
        ValueEventListener QVEL= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot it : dataSnapshot.getChildren())
                {
                    for(DataSnapshot it2 : it.getChildren())
                    {
                        for(DataSnapshot it3 : it2.getChildren())
                        {
                            //Log.v("Main","Key:"+it3.getKey());
                            Offers tempOffer = it3.getValue(Offers.class);
                            tempOffer.setOfferKey(it3.getKey());

                            boolean toAdd=true;
                            for(Offers offer:offerList)
                                if(offer.getOfferKey().equals(tempOffer.getOfferKey()))
                                    toAdd=false;

                            if(toAdd)
                                if(tempOffer.getUserID().equals(userDriver.getUserKey()))
                                    offerList.add(tempOffer);
                        }
                    }
                }

                for(Offers offer : offerList)
                {
                    Log.v("Main", "offerList for this user: " + offer.getOfferKey());
                }

                listView.setAdapter(offerAdapter);
                Util.ProgDialogDelay(progressDialog,1000L);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        query.addListenerForSingleValueEvent(QVEL);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // click to go to offerinfo page

                OfferInfo offerInfoPage = new OfferInfo();
                Bundle bundle = new Bundle();

                bundle.putSerializable("userOffer",offerList.get(position));
                bundle.putSerializable("userDriver",userDriver);
                offerInfoPage.setArguments(bundle);
                //Log.v("Main","Sending to OfferInfo: "+offerList.get(position).getTitle());

                Util.ChangeFrag(offerInfoPage,fragmentManager);
            }
        });
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
            final Main.ViewHolder holder = new Main.ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowItem = inflater.inflate(R.layout.fragment_more_offers_listview_items, parent, false);

            Offers tempOffer = offerList.get(position);

            holder.Title = (TextView) rowItem.findViewById(R.id.moreOffers_items_TitleData);
            holder.Title.setText(tempOffer.getTitle());

            holder.City = (TextView) rowItem.findViewById(R.id.moreOffers_items_cityData);
            holder.City.setText(tempOffer.getCity());

            holder.typeIcon = (ImageView) rowItem.findViewById(R.id.moreOffers_items_typeIcon);
            switch(tempOffer.getType())
            {
                case "Car":holder.typeIcon.setImageDrawable(getDrawableResource(R.drawable.car));break;
                case "Bus":holder.typeIcon.setImageDrawable(getDrawableResource(R.drawable.bus));break;
                case "Taxi":holder.typeIcon.setImageDrawable(getDrawableResource(R.drawable.taxi));break;
                case "Truck":holder.typeIcon.setImageDrawable(getDrawableResource(R.drawable.truck));break;
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

    static class ViewHolder
    {
        // this class is called in getView and assigned it all "items" layouts Views,for smooth scrolling
        TextView Title, City;
        ImageView typeIcon;
    }

    private Drawable getDrawableResource(int resID)//used in list view to set icons to rows
    {
        return ContextCompat.getDrawable(getActivity().getApplicationContext(), resID);//context.compat checks the version implicitly
    }
}
