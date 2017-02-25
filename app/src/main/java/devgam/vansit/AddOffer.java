package devgam.vansit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.DebugUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;


public class AddOffer extends Fragment {


    public AddOffer() {
        // Required empty public constructor
    }
    Spinner spinnerCity,spinnerType;
    EditText editTitle,editDesc;
    Button btnSave,btnCancel;

    private boolean canGoBack = false;
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
//        Log.v("Main","onResume");
        spinnerCity = (Spinner)getActivity().findViewById(R.id.frag_addOffer_spinCity);
        spinnerType = (Spinner)getActivity().findViewById(R.id.frag_addOffer_spinType);
        FillSpinners();

        editTitle = (EditText) getActivity().findViewById(R.id.frag_addOffer_editTitle);
        editDesc = (EditText) getActivity().findViewById(R.id.frag_addOffer_editDesc);

        btnSave = (Button) getActivity().findViewById(R.id.frag_addOffer_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveOffer();
            }
        });
        btnCancel = (Button) getActivity().findViewById(R.id.frag_addOffer_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CancelOffer();
            }
        });

        DatabaseReference mRef = FirebaseDatabase.getInstance().
                getReference(Util.RDB_USERS);
        String newUserKey = mRef.push().getKey();
        Users newUser = new Users("Lara","Amman",796640858L,"Female","kjsk5465","9","7","1995",newUserKey);
        mRef.child(newUserKey).setValue(newUser);
        FragmentSetUp();
    }
    private void FragmentSetUp()// some custom settings for this fragment
    {

        /*getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener())*/

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
        Offers myOffer = new Offers("-KWcMDEOBw_cKsOJhI75",
                editTitle.getText().toString(),
                editDesc.getText().toString(),
                spinnerType.getSelectedItem().toString());

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
