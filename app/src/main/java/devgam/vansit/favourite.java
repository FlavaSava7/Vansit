package devgam.vansit;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

    /**
     * Set Up User Favourites and call setAdapterAndUpdateUserFavourites
     */
    private void SetUpMyFavourites()
    {
        if(!Util.IS_USER_CONNECTED)
        {
            makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
            return;
        }

        // used later to update User fav
        final ArrayList<Favourite> favsToKeep = new ArrayList<>();

        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_FAVOURITE
                                +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        ValueEventListener QVEL= new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())//User does not have any favourites
                {
                    noFav.setVisibility(View.VISIBLE);
                    Util.ProgDialogDelay(progressDialog,100L);
                }else
                {
                    for(DataSnapshot favObj : dataSnapshot.getChildren())
                    {
                        Favourite fav = favObj.getValue(Favourite.class);
                        myFavourits.add(fav);
                        favsToKeep.add(fav);
                    }

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
                                //this offer is deleted by the Driver,
                                // So store it inside favsToKeep to update User's fav list.
                                if(!dataSnapshot.exists())// offer does not exist
                                {
                                    int indexToDelete=0;// to store the index which we will delete from favsToKeep
                                    for(int innerIndex =0 ;innerIndex<favsToKeep.size();innerIndex++)
                                    {
                                        if(myFavourits.get(finalI).getOfferKey().equals(favsToKeep.get(innerIndex).getOfferKey()))
                                        {
                                            indexToDelete = innerIndex;
                                            break;
                                        }
                                    }
                                    favsToKeep.remove(indexToDelete);

                                }else// offer exists
                                {
                                    Offers offer = dataSnapshot.getValue(Offers.class);
                                    offer.setOfferKey(dataSnapshot.getKey());
                                    myFavouritsOffers.add(offer);
                                }

                                if((finalI+1)==myFavourits.size())
                                {
                                    // last iteration: set adapter and update fav list in database
                                    new Handler().postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            setAdapterAndUpdateUserFavourites(favsToKeep);
                                        }
                                    },500L);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {
                            }
                        };
                        mRef2.addListenerForSingleValueEvent(QVEL2);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        };
        mRef.addListenerForSingleValueEvent(QVEL);
    }

    /**
     * Set the ListView Adapter and update the User favourites list in the database.
     * It's called after we are done fetching all favourites and the favourites that still points to offers.
     * @param favsToKeep : the favourites that still points on offers in the database
     */
    private void setAdapterAndUpdateUserFavourites(ArrayList<Favourite> favsToKeep)
    {

        if(!myFavouritsOffers.isEmpty())
            myFavouriteList.setAdapter(myFavouriteAdapter);
        else // all of this User favs were deleted, delete his list
        {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_FAVOURITE+"/"+
                    FirebaseAuth.getInstance().getCurrentUser().getUid());
            myRef.removeValue();
        }
        if(!favsToKeep.isEmpty())
        {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_FAVOURITE+"/"+
            FirebaseAuth.getInstance().getCurrentUser().getUid());
            myRef.setValue(favsToKeep);
            // so if user want to delete a favourite we can use myFavourites to set the new values in the database
            // cuz favsToKeep is local value
            myFavourits = favsToKeep;
        }
        Util.ProgDialogDelay(progressDialog,100L);
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

            holder.profileText = (Button) rowItem.findViewById(R.id.favorite_items_profile_text);
            holder.callText = (Button) rowItem.findViewById(R.id.favorite_items_call_text);
            holder.deleteText = (Button) rowItem.findViewById(R.id.favorite_items_delete_text);

            Query query = databaseReference.child(tempOffer.getUserID());
            ValueEventListener VEL = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.getValue(Users.class)!=null)
                    {
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
            holder.deleteText.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int indexToDelete =0;
                    for(int i = 0;i<myFavourits.size();i++)
                    {
                        if (myFavourits.get(i).getOfferKey().equals(tempOffer.getOfferKey()))
                        {
                            indexToDelete = i;
                            break;
                        }
                    }
                    myFavourits.remove(indexToDelete);

                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(Util.RDB_FAVOURITE+"/"+
                            FirebaseAuth.getInstance().getCurrentUser().getUid());
                    myRef.setValue(myFavourits);

                    myFavouritsOffers.remove(tempOffer);
                    myFavouriteAdapter.notifyDataSetChanged();
                }
            });

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
    private static class ViewHolder {
        // this class is called in getView and assigned it all "items" layouts Views,for smooth scrolling
        TextView Title, City;
        ImageView typeIcon;
        Button profileText, callText, deleteText;
    }

}
