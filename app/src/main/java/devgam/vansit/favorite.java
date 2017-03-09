package devgam.vansit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.R;


public class favorite extends Fragment {

    private ListView myFavoriteList;
    private ArrayList<Offers> myFavoriteArray ;

    public favorite() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        myFavoriteList = (ListView) getActivity().findViewById(R.id.favorite_listview);

        myFavoriteArray = new ArrayList<>();
        myFavoriteList.setAdapter(new itemAdapter());

    }

    class itemAdapter extends BaseAdapter implements View.OnClickListener {

        TextView titleText, cityText, userRatingText, priceRatingText;
        Button loveButton, profileButton, callButton;
        ImageView itemIcon;
        @Override
        public int getCount() {
            return myFavoriteArray.size();
        }

        @Override
        public Object getItem(int position) {
            return myFavoriteArray.get(position);
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
                    R.layout.fragment_my_offers_list_items,
                    parent,
                    false
            );

            //declare & initialize list item contents
            titleText = (TextView) listRow.findViewById(R.id.favorite_items_TitleData);
            cityText = (TextView) listRow.findViewById(R.id.favorite_items_cityData);
            userRatingText  = (TextView) listRow.findViewById(R.id.favorite_items_serviceRatingData);
            priceRatingText = (TextView) listRow.findViewById(R.id.favorite_items_priceRatingData);
            itemIcon = (ImageView) listRow.findViewById(R.id.favorite_items_typeIcon);
            loveButton = (Button) listRow.findViewById(R.id.favorite_items_love_text);
            profileButton = (Button) listRow.findViewById(R.id.favorite_items_profile_text);
            callButton = (Button) listRow.findViewById(R.id.favorite_items_call_text);


            //titleText.setText(myOffersArray.get(position).getTitle());
            //cityText.setText(myOffersArray.get(position).getCity());

            /*editText.setOnClickListener(this);
            shareText.setOnClickListener(this);
            deleteText.setOnClickListener(this);*/
            return listRow;
        }

        @Override
        public void onClick(View v) {

        }
    }

}
