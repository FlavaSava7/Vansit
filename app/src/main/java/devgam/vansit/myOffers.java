package devgam.vansit;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Offers;

/**
 * A simple {@link Fragment} subclass.
 */
public class myOffers extends Fragment {

    private ListView myOffersList;
    private ArrayList<Offers> myOffersArray ;
    FloatingActionButton addOfferFloating ;

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
        myOffersList = (ListView) getActivity().findViewById(R.id.my_offers_listView);

        myOffersArray = new ArrayList<>();
        myOffersList.setAdapter(new itemAdapter());


        addOfferFloating = (FloatingActionButton) getActivity().findViewById(R.id.my_offers_add);
        addOfferFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                AddOffer addOfferPage = new AddOffer();
                Util.ChangeFrag(addOfferPage,fragmentManager);
            }
        });

        {
            Offers n = new Offers();
            n.setTitle("Bus for student in university st.");
            n.setCity("amman");
            myOffersArray.add(n);

            Offers n1 = new Offers();
            n1.setTitle("Bus for student in university st." );
            n1.setCity("zarqa");
            myOffersArray.add(n1);

            Offers n2 = new Offers();
            n2.setTitle("Bus for student in university st.");
            n2.setCity("amman");
            myOffersArray.add(n2);
        }
    }

    class itemAdapter extends BaseAdapter implements View.OnClickListener {

        TextView titleText, cityText, editText, shareText,  deleteText;
        ImageView itemIcon;
        @Override
        public int getCount() {
            return myOffersArray.size();
        }

        @Override
        public Object getItem(int position) {
            return myOffersArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //declare & initialize inflater for list items
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View listRow = layoutInflater.inflate(
                    R.layout.my_offers_list_items,
                    parent,
                    false
            );

            //declare & initialize list item contents
            titleText = (TextView) listRow.findViewById(R.id.my_offers_list_items_title);
            cityText = (TextView) listRow.findViewById(R.id.my_offers_list_items_city);
            itemIcon = (ImageView) listRow.findViewById(R.id.main_items_typeIcon);
            editText = (TextView) listRow.findViewById(R.id.my_offers_list_items_edit);
            shareText = (TextView) listRow.findViewById(R.id.my_offers_list_items_share);
            deleteText = (TextView) listRow.findViewById(R.id.my_offers_list_items_delete);


            titleText.setText(myOffersArray.get(position).getTitle());
            cityText.setText(myOffersArray.get(position).getCity());

            editText.setOnClickListener(this);
            shareText.setOnClickListener(this);
            deleteText.setOnClickListener(this);
            return listRow;
        }

        @Override
        public void onClick(View v) {
            if(v == editText)
                Util.makeToast(getActivity(),"edit is run");

            if(v == shareText)
                Util.makeToast(getActivity(),"share is run");

            if(v == deleteText)
                Util.makeToast(getActivity(),"delete is run");
        }
    }
}
