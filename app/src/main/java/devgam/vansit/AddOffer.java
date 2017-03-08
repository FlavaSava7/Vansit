package devgam.vansit;

import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

import static devgam.vansit.Util.makeToast;


public class AddOffer extends Fragment {


    public AddOffer() {
        // Required empty public constructor
    }

    TextView nameText, phoneText, cityText;
    Spinner spinnerCity,spinnerType;
    EditText editTitle,editDesc;
    Button btnSave,btnCancel;
    FragmentManager fragmentManager;

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
        //FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        //if(fab!=null)
            //fab.setVisibility(View.GONE);
        spinnerCity = (Spinner)getActivity().findViewById(R.id.addOffer_spinCity);
        spinnerType = (Spinner)getActivity().findViewById(R.id.addOffer_spinType);
        FillSpinners();

        editTitle = (EditText) getActivity().findViewById(R.id.addOffer_editTitle);
        editDesc = (EditText) getActivity().findViewById(R.id.addOffer_editDesc);

        nameText = (TextView) getActivity().findViewById(R.id.addOffer_name_text);
        phoneText = (TextView) getActivity().findViewById(R.id.addOffer_phone_text);
        cityText = (TextView) getActivity().findViewById(R.id.addOffer_city_text);

        fragmentManager  = getActivity().getSupportFragmentManager();

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

        FragmentSetUp();
        //to add user info under offer :
        addUserData();
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
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        //Type
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.type_list));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        if(Util.IS_USER_CONNECTED) {
            // here we will AUTO go to the child where his Country == the country he signed up in the app
            //Edit it later cuz for now we dont have the User details yet
            DatabaseReference mRef = FirebaseDatabase.getInstance().
                    getReference(Util.RDB_COUNTRY+"/"+
                            Util.RDB_JORDAN+"/"+
                            spinnerCity.getSelectedItem().toString()+"/"+
                            Util.RDB_OFFERS);
            mRef.push().setValue(myOffer);

            Util.makeToast(getContext(),"Success!");

            myOffers offer = new myOffers();
            Util.ChangeFrag(offer, fragmentManager);
        } else {
            makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
        }



    }
    public void CancelOffer()
    {
        editDesc.setText("");
        editTitle.setText("");
    }

    private void addUserData(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String tempUID = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference mRef = FirebaseDatabase.getInstance().
                getReference(Util.RDB_USERS +"/"+
                        tempUID);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                nameText.setText(users.getFirstName() + " " + users.getLastName());
                phoneText.setText(users.getPhone());
                cityText.setText(users.getCity());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
