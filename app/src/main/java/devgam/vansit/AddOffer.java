package devgam.vansit;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    Button btnSave;
    FragmentManager fragmentManager;

    public Offers editOffer = null; // this will be set of we came from myOffers pages to edit an offer

    boolean doubleBackToExitPressedOnce = false;
    private Drawable errorIcon;

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
        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            editOffer = (Offers) bundle.getSerializable("editOffer");
        }
        return inflater.inflate(R.layout.fragment_add_offer, container, false);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        Util.ChangePageTitle(getActivity(), R.string.main_fab_text_add_offer);
        spinnerCity = (Spinner)getActivity().findViewById(R.id.addOffer_spinCity);
        spinnerType = (Spinner)getActivity().findViewById(R.id.addOffer_spinType);
        FillSpinners();//default filling

        editTitle = (EditText) getActivity().findViewById(R.id.addOffer_editTitle);
        editDesc = (EditText) getActivity().findViewById(R.id.addOffer_editDesc);

        if(editOffer!=null)//we came from myOffers so set these fields
        {
            editTitle.setText(editOffer.getTitle());
            editDesc.setText(editOffer.getDescription());
            ArrayList<String> tempCityList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.city_list)));
            spinnerCity.setSelection(tempCityList.indexOf(editOffer.getCity()));

            ArrayList<String> tempTypeList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.type_list)));
            spinnerType.setSelection(tempTypeList.indexOf(editOffer.getType()));
        }

        nameText = (TextView) getActivity().findViewById(R.id.addOffer_name_text);
        phoneText = (TextView) getActivity().findViewById(R.id.addOffer_phone_text);
        cityText = (TextView) getActivity().findViewById(R.id.addOffer_city_text);

        fragmentManager  = getActivity().getSupportFragmentManager();

        btnSave = (Button) getActivity().findViewById(R.id.addOffer_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(editOffer==null)
                    SaveOffer();
                else
                    EditOffer();

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
                    Util.makeToast(getContext(),getResources().getString(R.string.add_offer_click_again_to_exit));

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
        if(!Util.IS_USER_CONNECTED)
        {
            makeToast(getContext(), getString(R.string.noInternetMsg));
            return;
        }
        if(!checkEditText(editTitle.getText().toString(),editDesc.getText().toString()))
            return;

        Offers myOffer = new Offers(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                editTitle.getText().toString(), editDesc.getText().toString(),
                spinnerType.getSelectedItem().toString(),spinnerCity.getSelectedItem().toString(),
                Util.RDB_JORDAN, System.currentTimeMillis());

        DatabaseReference mRef = FirebaseDatabase.getInstance().
                getReference(Util.RDB_OFFERS);
        mRef.push().setValue(myOffer);

        myOffers offer = new myOffers();
        Util.ChangeFrag(offer, fragmentManager);
    }
    public void EditOffer()
    {
        if(!Util.IS_USER_CONNECTED)
        {
            Util.makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
            return;
        }

        //String tempKeyHolder  = editOffer.getOfferKey();// to fix stupid bug with null keys when updating to Offers/UserKey/offer

        DatabaseReference mRef = FirebaseDatabase.getInstance().
                getReference(Util.RDB_OFFERS+"/"+editOffer.getOfferKey());
        if(checkEditText(editTitle.getText().toString(),editDesc.getText().toString()))
        {
            editOffer.setTitle(editTitle.getText().toString());
            editOffer.setDescription(editDesc.getText().toString());
            editOffer.setCity(spinnerCity.getSelectedItem().toString());
            editOffer.setType(spinnerType.getSelectedItem().toString());
            editOffer.setTimeStamp(System.currentTimeMillis());
            editOffer.setOfferKey(null);
            mRef.setValue(editOffer);// set the new value
        }
        else
        {
            return;
        }


        editOffer= null;//reset

        myOffers offer = new myOffers();
        Util.ChangeFrag(offer, fragmentManager);
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


    private boolean checkEditText(String title, String desc)
    {
        // checks edit texts and spinners
        boolean isValid = true;
        if(TextUtils.isEmpty(title))
        {

            isValid = false;
        }
        if(TextUtils.isEmpty(desc))
        {

            isValid = false;
        }
        if(spinnerCity.getSelectedItem().toString().isEmpty()|| spinnerCity.getSelectedItem().toString().equals("Select City") ||
                spinnerType.getSelectedItem().toString().isEmpty()|| spinnerType.getSelectedItem().toString().equals("Select Type") )
        {
            isValid = false;
        }

        return isValid;
    }
}
