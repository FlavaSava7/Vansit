package devgam.vansit;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import devgam.vansit.JSON_Classes.Favourite;
import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

import static devgam.vansit.Util.ProgDialogDelay;
import static devgam.vansit.Util.makeToast;


public class favourite extends Fragment {

    private ListView myFavouriteList;
    private ArrayAdapter myFavouriteAdapter;
    private ArrayList<Offers> myFavouritsOffers;
    private ArrayList<Favourite> myFavourits;//hold our favourites keys

    ProgressDialog progressDialog;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    TextView noFav;
    public favourite() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        Util.ChangePageTitle(getActivity(),R.string.menu_fav_text);

        noFav = (TextView) getActivity().findViewById(R.id.favorite_nofav);
        myFavouriteList = (ListView) getActivity().findViewById(R.id.favorite_listview);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_USERS);
        fragmentManager  = getActivity().getSupportFragmentManager();
        myFavouriteAdapter = new itemsAdapter(getContext(),mRef);

        progressDialog = new ProgressDialog(getContext(),ProgressDialog.STYLE_SPINNER);

        if(myFavouritsOffers ==null || myFavourits ==null)
        {
            Util.ProgDialogStarter(progressDialog,getResources().getString(R.string.loading));
            myFavouritsOffers = new ArrayList<>();
            myFavourits = new ArrayList<>();
            SetUpMyFavourites();
        }
        else
        {
            myFavouriteList.setAdapter(myFavouriteAdapter);
        }

    }
    private void SetUpMyFavourites()
    {
        if(!Util.IS_USER_CONNECTED)
        {
            makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
            return;
        }
        // used later to delete any offers if we didnt find them
        // we add on this list inside QVEL
        final List<String> offerKeysToDelete = new ArrayList<>();
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_FAVOURITE
                                +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        ValueEventListener QVEL= new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())//user does not have any favourites
                {
                    noFav.setVisibility(View.VISIBLE);
                }else
                {
                    for(DataSnapshot favObj : dataSnapshot.getChildren())
                    {
                        Favourite fav = favObj.getValue(Favourite.class);
                        myFavourits.add(fav);
                    }
                    for(Favourite fav : myFavourits)
                        Log.v("Main","fav: "+fav.getOfferKey());

                    DatabaseReference mRef2;//just an inner reference

                    for(int i = 0;i<myFavourits.size();i++)
                    {
                        mRef2 = FirebaseDatabase.getInstance().getReference().child(Util.RDB_OFFERS+"/"+
                                myFavourits.get(i).getOfferKey());

                        final int finalI = i;//temp reference to use inside the QVEL2
                        ValueEventListener QVEL2= new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                //this offer is deleted by the Driver, So delete it from this user Fav.
                                // But first store it in another variable then later delete it,
                                // so the myFavouritsOffers does not fail
                                if(dataSnapshot==null)
                                {
                                    offerKeysToDelete.add(myFavouritsOffers.get(finalI).getOfferKey());
                                }else// offer exists
                                {
                                    Offers offer = dataSnapshot.getValue(Offers.class);
                                    myFavouritsOffers.add(offer);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {
                            }
                        };
                        mRef2.addListenerForSingleValueEvent(QVEL2);

                    }

                    myFavouriteList.setAdapter(myFavouriteAdapter);
                    for(Offers offer : myFavouritsOffers)
                        Log.v("Main","Offers Fav: "+offer.getTitle());

                }
                Util.ProgDialogDelay(progressDialog,100L);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        mRef.addListenerForSingleValueEvent(QVEL);

        //delete any favourites from this user if we found any offer was deleted
        if(!offerKeysToDelete.isEmpty())
        {
            DatabaseReference mRef2;
            for(String offerKey : offerKeysToDelete)
            {
                mRef2 = FirebaseDatabase.getInstance().getReference().child(Util.RDB_FAVOURITE
                        +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+offerKey);
                mRef2.removeValue();
            }
        }
    }
    private class itemsAdapter extends ArrayAdapter<Offers>
    {
        Context context;
        DatabaseReference databaseReference;
        itemsAdapter(Context c,DatabaseReference databaseReference) {
            super(c, R.layout.fragment_favourite_listview_items, myFavouritsOffers);
            this.context = c;
            this.databaseReference = databaseReference;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowItem = inflater.inflate(R.layout.fragment_favourite_listview_items, parent, false);

            final Offers tempOffer = myFavouritsOffers.get(position);

            holder.Title = (TextView) rowItem.findViewById(R.id.favorite_items_TitleData);
            holder.Title.setText(tempOffer.getTitle());

            holder.City = (TextView) rowItem.findViewById(R.id.favorite_items_cityData);
            holder.City.setText(tempOffer.getCity());

            holder.typeIcon = (ImageView) rowItem.findViewById(R.id.favorite_items_typeIcon);
            holder.typeIcon.setImageDrawable(Util.getDrawableResource(getActivity(), Util.changeIcon(tempOffer.getType())));

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
            return myFavouritsOffers.size();
        }

        @Override
        public Offers getItem(int position) {
            return myFavouritsOffers.get(position);
        }
    }
    static class ViewHolder {
        // this class is called in getView and assigned it all "items" layouts Views,for smooth scrolling
        TextView Title, City;
        ImageView typeIcon;
        LinearLayout profileText, callText;
    }

}
