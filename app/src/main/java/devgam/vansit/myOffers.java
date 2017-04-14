package devgam.vansit;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Debug;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

import static devgam.vansit.Util.makeToast;

/**
 * A simple {@link Fragment} subclass.
 */
public class myOffers extends Fragment {

    private ListView myOffersList;
    private ArrayAdapter myOffersAdapter;
    private ArrayList<Offers> myOffersArray ;
    FloatingActionButton addOfferFloating ;

    ProgressDialog progressDialog;

    public myOffers() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_offers, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.ChangePageTitle(getActivity(),R.string.menu_my_offers_text);
        myOffersList = (ListView) getActivity().findViewById(R.id.my_offers_listView);
        myOffersAdapter = new itemsAdapter(getContext());
        progressDialog = new ProgressDialog(getContext(),ProgressDialog.STYLE_SPINNER);

        addOfferFloating = (FloatingActionButton) getActivity().findViewById(R.id.my_offers_add);
        addOfferFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                AddOffer addOfferPage = new AddOffer();
                Util.ChangeFrag(addOfferPage,fragmentManager);
            }
        });

        if(myOffersArray==null)
        {
            Util.ProgDialogStarter(progressDialog,getResources().getString(R.string.loading));
            myOffersArray = new ArrayList<>();
            SetUpMyOffers();
        }
        else
        {
            myOffersList.setAdapter(myOffersAdapter);
        }



    }

    private void SetUpMyOffers()
    {
        if(!Util.IS_USER_CONNECTED)
        {
            Util.ProgDialogDelay(progressDialog,100L);
            return;
        }
        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference()
                .child(Util.RDB_OFFERS);

        Query query = DataBaseRoot.orderByChild(Util.USER_ID).equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ValueEventListener QVEL= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())// This User have no offer
                {
                    Util.makeToast(getActivity(), "You Don't Have Offers!");
                }
                else
                {
                    for(DataSnapshot offers : dataSnapshot.getChildren())
                    {
                        //Log.v("Main","Key:"+offers.getKey());
                        Offers tempOffer = offers.getValue(Offers.class);
                        tempOffer.setOfferKey(offers.getKey());
                        myOffersArray.add(tempOffer);
                    }
                    Util.SortByTimeStampDesc(myOffersArray);
                    myOffersList.setAdapter(myOffersAdapter);
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

    private class itemsAdapter extends ArrayAdapter<Offers>
    {
        Context context;

        itemsAdapter(Context c) {
            super(c, R.layout.fragment_my_offers_list_items, myOffersArray);
            this.context = c;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {

            //declare & initialize inflater for list items
            final myOffers.ViewHolder holder = new myOffers.ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listRow = layoutInflater.inflate(R.layout.fragment_my_offers_list_items, parent, false);

            //declare & initialize list item contents
            holder.titleText = (TextView) listRow.findViewById(R.id.my_offers_list_items_title);
            holder.cityText = (TextView) listRow.findViewById(R.id.my_offers_list_items_city);
            holder.typeText = (TextView) listRow.findViewById(R.id.my_offers_list_items_type);
            holder.descText = (TextView) listRow.findViewById(R.id.my_offers_list_items_desc);
            holder.itemIcon = (ImageView) listRow.findViewById(R.id.main_items_typeIcon);
            holder.editText = (TextView) listRow.findViewById(R.id.my_offers_list_items_edit);
            holder.deleteText = (TextView) listRow.findViewById(R.id.my_offers_list_items_delete);

            final Offers tempOffer = myOffersArray.get(position);

            holder.titleText.setText(tempOffer.getTitle());
            holder.cityText.setText(tempOffer.getCity());
            holder.typeText.setText(tempOffer.getType());
            holder.descText.setText(tempOffer.getDescription());

            holder.itemIcon.setImageDrawable(Util.getDrawableResource(getActivity(), Util.changeIcon(tempOffer.getType())));

            holder.editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    editOffer(tempOffer);
                }
            });
            holder.deleteText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    deleteOffer(tempOffer);
                }
            });
            return listRow;
        }

        @Override
        public int getCount() {
            return myOffersArray.size();
        }

        @Override
        public Offers getItem(int position) {
            return myOffersArray.get(position);
        }
    }
    static class ViewHolder
    {
        // this class is called in getView and assigned it all "items" layouts Views,for smooth scrolling
        TextView titleText, cityText, typeText, descText, editText, deleteText;
        ImageView itemIcon;

    }

    private void deleteOffer(final Offers offerToDelete)
    {
        if(!Util.IS_USER_CONNECTED)
        {
            makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle("")
                .setMessage(getResources().getString(R.string.my_offer_sure))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        DatabaseReference DataBaseRoot = FirebaseDatabase.getInstance().getReference()
                                .child(Util.RDB_OFFERS +"/"+offerToDelete.getOfferKey());
                        DataBaseRoot.removeValue();

                        myOffersArray.remove(offerToDelete);
                        myOffersAdapter.notifyDataSetChanged();
                        myOffersList.setAdapter(myOffersAdapter);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void editOffer(Offers offerToEdit)
    {
        if(!Util.IS_USER_CONNECTED)
        {
            makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
            return;
        }

        AddOffer addOfferPage = new AddOffer();
        Bundle bundle = new Bundle();
        bundle.putSerializable("editOffer",offerToEdit);
        addOfferPage.setArguments(bundle);
        FragmentManager fragmentManager  = getActivity().getSupportFragmentManager();
        Util.ChangeFrag(addOfferPage,fragmentManager);
    }

}
