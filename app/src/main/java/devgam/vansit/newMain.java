package devgam.vansit;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import devgam.vansit.R;


public class newMain extends Fragment {

    GridView categoryList;
    String[] types = new String[] { "Car", "Bus", "Taxi"};


    public newMain() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public void onResume() {
        super.onResume();
        categoryList = (GridView) getActivity().findViewById(R.id.new_main_list);
        itemAdpater adapter = new itemAdpater(getContext());
        categoryList.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_main, container, false);
    }

    class itemAdpater extends ArrayAdapter<String> {

        Context context;


        public itemAdpater(Context context) {
            super(context, R.layout.new_main_list_item, types);
        }

        @Override
        public int getCount() {
            return types.length;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = layoutInflater.inflate(R.layout.new_main_list_item, parent);
            String type = types[position];
            TextView title = (TextView) rowView.findViewById(R.id.new_main_title);
            title.setText(type);
            return rowView;
        }
    }






}
