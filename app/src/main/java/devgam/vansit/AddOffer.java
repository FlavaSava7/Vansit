package devgam.vansit;

import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import devgam.vansit.JSON_Classes.Offers;


public class AddOffer extends Fragment {


    public AddOffer() {
        // Required empty public constructor
    }
    Spinner spinnerCity,spinnerType;
    EditText editTitle,editDesc;
    Button btnSave,btnCancel;

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Log.v("Main","onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        //Log.v("Main","onCreateView");
        return inflater.inflate(R.layout.fragment_add_offer, container, false);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if(fab!=null)
            fab.setVisibility(View.GONE);
        spinnerCity = (Spinner)getActivity().findViewById(R.id.addOffer_spinCity);
        spinnerType = (Spinner)getActivity().findViewById(R.id.addOffer_spinType);
        FillSpinners();

        editTitle = (EditText) getActivity().findViewById(R.id.addOffer_editTitle);
        editDesc = (EditText) getActivity().findViewById(R.id.addOffer_editDesc);

        btnSave = (Button) getActivity().findViewById(R.id.addOffer_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveOffer();
            }
        });
        btnCancel = (Button) getActivity().findViewById(R.id.addOffer_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CancelOffer();
            }
        });

       /*DatabaseReference mRef = FirebaseDatabase.getInstance().
                getReference(Util.RDB_USERS);
        String newUserKey = mRef.push().getKey();
        Users newUser = new Users("Abood","Amman","796640858","Female","9","7","1995");
        mRef.child(newUserKey).setValue(newUser);*/
        FragmentSetUp();
    }
    private void FragmentSetUp()// some custom settings for this fragment
    {

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if(KeyEvent.KEYCODE_BACK == keyCode)
                {
                    if (doubleBackToExitPressedOnce) {
                        return false;
                    }

                    doubleBackToExitPressedOnce = true;
                    Util.makeToast(getContext(),"Please click BACK again to exit");

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce=false;
                        }
                    }, 5000);
                }
                return true;
            }
        });

        Util.OutsideTouchKeyBoardHider(getActivity().findViewById(R.id.fragParent_add_offer),getActivity());
    }
    private void FillSpinners()
    {
        //City
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.city_list));
        spinnerCity.setAdapter(cityAdapter);

        //Type
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.type_list));
        spinnerType.setAdapter(typeAdapter);
    }
    public void SaveOffer()
    {
        // Send the Offer to the DataBase
        // must check for internet
        if(editDesc.getText().toString().isEmpty() || editTitle.getText().toString().isEmpty()||
                editDesc.getText().toString().equals("") || editTitle.getText().toString().equals(""))
        {
            return;
        }

        //for now the User Id will be inputted manually,cuz for now we dont have the User details yet
        // TODO: CHANGE THE ID HERE
        Offers myOffer = new Offers(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                editTitle.getText().toString(),
                editDesc.getText().toString(),
                spinnerType.getSelectedItem().toString(),spinnerCity.getSelectedItem().toString(), System.currentTimeMillis());

        // here we will AUTO go to the child where his Country == the country he signed up in the app
        //Edit it later cuz for now we dont have the User details yet
        DatabaseReference mRef = FirebaseDatabase.getInstance().
                getReference(Util.RDB_COUNTRY+"/"+
                        Util.RDB_JORDAN+"/"+
                        spinnerCity.getSelectedItem().toString()+"/"+
                        Util.RDB_OFFERS);
        mRef.push().setValue(myOffer);

        Util.makeToast(getContext(),"Success!");


    }
    public void CancelOffer()
    {
        editDesc.setText("");
        editTitle.setText("");
    }
}
