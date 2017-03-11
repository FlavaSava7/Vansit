package devgam.vansit;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import devgam.vansit.JSON_Classes.Users;

import static devgam.vansit.Util.dayNow;
import static devgam.vansit.Util.makeToast;
import static devgam.vansit.Util.monthNow;
import static devgam.vansit.Util.yearNow;

public class myAccount extends Fragment implements View.OnClickListener{

    private EditText firstNameEdit, lastNameEdit, phoneEdit ;
    private TextView birthEdit;
    private Button saveButton;
    private RadioButton maleRadio, femaleRadio;
    DatePickerDialog datePicker;
    private Spinner citySpinner;
    private ArrayAdapter<CharSequence> cityAdapter;
    private Drawable errorIcon;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    FirebaseAuth firebaseAuth;
    DatabaseReference mRef;
    String tempUID;


    //temp day, month, year to save data from picker until data click save
    //because may be user cancel change
    //that's will product real data on fireBase
    private int tempDayOfBirth, tempMonthOfBirth, tempYearOfBirth ;
    private static String tempUserFirstName, tempUserLastName , tempPhoneNumber, tempUserCity, tempUserGander ;

    public myAccount() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_account, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // A.J.I. : Hide Fab
        //FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        //if(fab!=null)
            //fab.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        tempUID = firebaseAuth.getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().
                getReference(Util.RDB_USERS +"/"+
                        tempUID);

        //Views initialize
        firstNameEdit = (EditText) getActivity().findViewById(R.id.my_account_firstName_edit);
        lastNameEdit = (EditText) getActivity().findViewById(R.id.my_account_lastName_edit);
        phoneEdit = (EditText) getActivity().findViewById(R.id.my_account_phone_edit);
        birthEdit = (TextView) getActivity().findViewById(R.id.my_account_birth_edit);
        saveButton = (Button) getActivity().findViewById(R.id.my_account_save_button);
        maleRadio = (RadioButton) getActivity().findViewById(R.id.my_account_male_radio);
        femaleRadio = (RadioButton) getActivity().findViewById(R.id.my_account_female_radio);

        //spinner initialize
        citySpinner = (Spinner) getActivity().findViewById(R.id.my_account_city_spinner);
        cityAdapter = ArrayAdapter.createFromResource(getContext(), R.array.city_list, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tempUserCity = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fragmentManager  = getActivity().getSupportFragmentManager();

        firebaseAuth = FirebaseAuth.getInstance();
        tempUID = firebaseAuth.getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().
                getReference(Util.RDB_USERS +"/"+
                        tempUID);


        //get data from shared to fill views :
        try {
            setDataToViews();
        } catch (Exception e){

        }


        //on click listener for buttons :
        birthEdit.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        maleRadio.setOnClickListener(this);
        femaleRadio.setOnClickListener(this);

        //may be user don't click because it's already clicked
        if(maleRadio.isChecked())
            tempUserGander = "male";
        else
            tempUserGander = "female";
    }


    @Override
    public void onClick(View v) {
        if (v == maleRadio)
            tempUserGander = "male";

        if (v == femaleRadio)
            tempUserGander = "female";

        if (v == birthEdit) {
            datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    birthEdit.setText(dayOfMonth + " / " + month + "/ " + year);
                    tempDayOfBirth = dayOfMonth;
                    tempMonthOfBirth = month;
                    tempYearOfBirth = year;
                }
            }, Util.yearNow, Util.monthNow, Util.dayNow);
            datePicker.show();

        } if(v == saveButton){
            if(checkAndChange()) {
                //Temp code to check data is correct :
                /*final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setMessage("User first name :" + tempUserFirstName + "\n"
                        + "User last name :" + tempUserLastName + "\n"
                        + "User phone :" + tempPhoneNumber + "\n"
                        + "User birthday :" + tempDayOfBirth + " / " + tempMonthOfBirth + "/ " + tempYearOfBirth + "\n"
                        + "User city :" + tempUserCity + "\n"
                        + "User gender :" + tempUserGander + "\n");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                //End of temp code*/

                if(Util.IS_USER_CONNECTED) {
                    saveDataToDatabase();
                    Main mainPage = new Main();
                    Util.ChangeFrag(mainPage, fragmentManager);
                } else {
                    makeToast(getContext(), String.valueOf(R.string.noInternetMsg));
                }
            } // if statement

        }
    }

    //Check if values is valid
    private boolean checkAndChange(){
        //I have declare check edit method to ..
        // - check value of edit text and initialize it if it is not empty

        //Check if phone field is not null !
        if( ! Util.checkEdit(getActivity(), errorIcon, phoneEdit, "phone number is required"))
            return false;
        else
            tempPhoneNumber = phoneEdit.getText().toString();

        //Check if last name field is not null !
        if( ! Util.checkEdit(getActivity(), errorIcon, lastNameEdit, "last name is required"))
            return false;
        else
            tempUserLastName = lastNameEdit.getText().toString();

        //Check if first name field is not null !
        if( ! Util.checkEdit(getActivity(), errorIcon, firstNameEdit, "first name is required"))
            return false;
        else
            tempUserFirstName = firstNameEdit.getText().toString();

        if(tempYearOfBirth == 0 || Util.yearNow < tempYearOfBirth + 16) {
            //Check if user add real birthDate not current date !
            //Just year because no body born in this year can make account
            //No one less than 16 can drive or make deal with other people
            Util.makeToast(getActivity(), "Invalid Birth day");
            return false;
        }

        return true;
    }

    private void saveDataToDatabase(){
        //user object to push data on DB
        //TODO: UPDATE THE VALUE HERE TO PUT FIRST NAME , LAST NAME ( constructer take one more parameter for last name now )
        Users userData = new Users(tempUserFirstName, tempUserLastName,tempUserCity,tempPhoneNumber,tempUserGander,
                tempDayOfBirth + "", tempMonthOfBirth + "", tempYearOfBirth + "");

        //to save data in shared preferance :
        //setUserData(userData);

        //Temp code
        try{
            mRef.setValue(userData);
            Util.makeToast(getContext(), "Save Successfully");
        } catch (Exception e){

        }

    }

    //to set data to views after data set it one time
    private void setDataToViews() {

        //temp code:
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    final Users tempUser = dataSnapshot.getValue(Users.class);

                    if (!tempUser.getFirstName().isEmpty()) {
                        //For Check method
                        tempYearOfBirth = Integer.parseInt(tempUser.getDateYear());
                        tempMonthOfBirth = Integer.parseInt(tempUser.getDateMonth());
                        tempDayOfBirth = Integer.parseInt(tempUser.getDateDay());

                        firstNameEdit.setText(tempUser.getFirstName());
                        lastNameEdit.setText(tempUser.getLastName());
                        phoneEdit.setText(tempUser.getPhone());
                        birthEdit.setText(tempDayOfBirth + " / " +
                                (tempMonthOfBirth + 1) + "/ " +
                                tempYearOfBirth);

                        if (tempUser.getGender().equals("male")) {
                            maleRadio.setChecked(true);
                        } else {
                            femaleRadio.setChecked(true);
                        }

                    }
                } catch (Exception e){
                    birthEdit.setText(dayNow + " / " +
                            (monthNow + 1) + "/ " +
                            yearNow);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
