package devgam.vansit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import devgam.vansit.JSON_Classes.Offers;


public class AddOffer extends Fragment {


    public AddOffer() {
        // Required empty public constructor
    }
    Spinner spinnerCity,spinnerType;
    EditText editTitle,editDesc;
    Button btnSave,btnCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_offer, container, false);
    }

    @Override
    public void onResume()
    {
        super.onResume();

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

    }
    private void FillSpinners()
    {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Util.RDB_COUNTRY+"/"+Util.RDB_JORDAN);

        //City
        final ArrayList<String> cityList = new ArrayList<>();
        ValueEventListener VEL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot areaSnapshot: dataSnapshot.getChildren())
                {
                    cityList.add(areaSnapshot.getKey());

                }
                ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item,cityList);
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(cityAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addListenerForSingleValueEvent(VEL);


        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.typesList));
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

        Toast.makeText(getContext(), "Successful Save!", Toast.LENGTH_SHORT).show();



    }
    public void CancelOffer()
    {
        editDesc.setText("");
        editTitle.setText("");
    }
}
