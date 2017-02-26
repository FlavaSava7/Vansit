package devgam.vansit;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.R;

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
//      Test Block - delete it soon
        {
            Offers n = new Offers();
            n.setTitle("My car");
            n.setCity("amman");
            myOffersArray.add(n);
        }

    }

    class itemAdapter extends BaseAdapter {

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
            TextView titleText = (TextView) listRow.findViewById(R.id.my_offers_list_items_title);
            TextView cityText = (TextView) listRow.findViewById(R.id.my_offers_list_items_city);
            ImageView itemIcon = (ImageView) listRow.findViewById(R.id.my_offers_list_items_img);
            ImageButton deleteButton = (ImageButton) listRow.findViewById(R.id.my_offers_list_items_delete);

            Offers offers = myOffersArray.get(position);
            titleText.setText(offers.getTitle());
            cityText.setText(offers.getCity());

            return listRow;
        }
    }
}
