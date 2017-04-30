package devgam.vansit;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


public class newMain extends Fragment implements View.OnClickListener{

    RelativeLayout carLayout, taxiLayout, mircrobusLayout,
            busLayout, pickupLayout, truckLayout,
            bulldozerLayout, excavatorLayout;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method


    public newMain() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager  = getActivity().getSupportFragmentManager();

        carLayout = (RelativeLayout) getActivity().findViewById(R.id.new_main_car);
        taxiLayout = (RelativeLayout) getActivity().findViewById(R.id.new_main_taxi);
        mircrobusLayout = (RelativeLayout) getActivity().findViewById(R.id.new_main_microbus);
        busLayout = (RelativeLayout) getActivity().findViewById(R.id.new_main_bus);
        pickupLayout = (RelativeLayout) getActivity().findViewById(R.id.new_main_pickup);
        truckLayout = (RelativeLayout) getActivity().findViewById(R.id.new_main_truck);
        bulldozerLayout = (RelativeLayout) getActivity().findViewById(R.id.new_main_bulldozer);
        excavatorLayout = (RelativeLayout) getActivity().findViewById(R.id.new_main_excavator);

        /*carLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main viewRequests = new Main("Car");
                Util.ChangeFrag(viewRequests, fragmentManager);
            }
        });
        //taxiLayout.setOnClickListener(this);

        /*
        mircrobusLayout.setOnClickListener(this);
        busLayout.setOnClickListener(this);
        pickupLayout.setOnClickListener(this);
        truckLayout.setOnClickListener(this);
        bulldozerLayout.setOnClickListener(this);
        excavatorLayout.setOnClickListener(this);*/


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_main, container, false);
    }


    @Override
    public void onClick(View v) {
        if(v == carLayout){

        }
    }
}
