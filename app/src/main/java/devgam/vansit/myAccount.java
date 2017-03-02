package devgam.vansit;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import devgam.vansit.JSON_Classes.Users;

public class myAccount extends Fragment implements View.OnClickListener{


    private EditText nameEdit, phoneEdit ;
    private TextView birthEdit;
    private Button saveButton;
    private RadioButton maleRadio, femaleRadio;
    DatePickerDialog datePicker;
    private Spinner citySpinner;
    private ArrayAdapter<CharSequence> cityAdapter;

    //temp day, month, year to save data from picker until data click save
    //because may be user cancel change
    //that's will product real data on fireBase
    private int tempDayOfBirth, tempMonthOfBirth, tempYearOfBirth ;
    private String tempUserName, tempPhoneNumber, tempUserCity, tempUserGander ;


    SharedPreferences userData ;
    SharedPreferences.Editor userDataEditor ;

    public myAccount() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //shared preference initialize
        Context context = getActivity();
        userData = context.getSharedPreferences("Vansit user Data", Context.MODE_PRIVATE);
        userDataEditor = userData.edit();

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
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if(fab!=null)
            fab.setVisibility(View.GONE);

        //Views initialize
        nameEdit = (EditText) getActivity().findViewById(R.id.my_account_name_edit);
        phoneEdit = (EditText) getActivity().findViewById(R.id.my_account_phone_edit);
        birthEdit = (TextView) getActivity().findViewById(R.id.my_account_birth_edit);
        saveButton = (Button) getActivity().findViewById(R.id.my_account_save_button);

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

        //if user don't set his birthday
        //temp code !!!
        birthEdit.setText(Util.dayNow + " / " + Util.monthNow + "/ " + Util.yearNow);

        maleRadio = (RadioButton) getActivity().findViewById(R.id.my_account_male_radio);
        femaleRadio = (RadioButton) getActivity().findViewById(R.id.my_account_female_radio);

        birthEdit.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        if(maleRadio.isChecked())
            tempUserGander = "male";
        else if(femaleRadio.isChecked())
            tempUserGander = "female";

    }


    @Override
    public void onClick(View v) {
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
                final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setMessage("User name :" + tempUserName + "\n"
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
                //End of temp code
            } // if statement
        }
    }

    //Check if values is valid
    private boolean checkAndChange(){
        String tempStringCheck = nameEdit.getText().toString();

        //Check if name field is not null !
        if(tempStringCheck.isEmpty() || tempStringCheck == "") {
            Util.makeToast(getActivity(), "Name is required");
            return false;
        } else {
            //that's temp code, we need to change it to first & last name
            tempUserName = tempStringCheck;
            Util.makeToast(getContext(), "name done");
        }

        //set values from views to vars
        tempStringCheck = ""; //to make it null after we have signed name value in it
        tempStringCheck = phoneEdit.getText().toString();
        if(!tempStringCheck.isEmpty() && tempStringCheck != "") {
            tempPhoneNumber = tempStringCheck;
            Util.makeToast(getContext(), "phone done");
        }


         if(tempYearOfBirth == 0 || Util.yearNow < tempYearOfBirth + 16) {
            //Check if user add real birthDate not current date !
            //Just year because no body born in this year can make account
            //No one less than 16 can drive or make deal with other people
            Util.makeToast(getActivity(), "Invalid Birth day");
            return false;
        }

        //user object to push data on DB
        //TODO: UPDATE THE VALUE HERE TO PUT FIRST NAME , LAST NAME ( constructer take one more parameter for last name now )
        /*Users userData = new Users(tempUserName,tempUserCity,tempPhoneNumber,tempUserGander,
                tempDayOfBirth + "", tempMonthOfBirth + "", tempYearOfBirth + "");*/

        /*Users userData = new Users(tempUserName, tempUserCity,
                tempPhoneNumber, tempUserGander,
                tempDayOfBirth + "", tempMonthOfBirth + "", tempYearOfBirth + "");*/

        //Temp code
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        try{

            final String tempUID = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference mRef = FirebaseDatabase.getInstance().
                    getReference(Util.RDB_USERS +"/"+
                    tempUID);

            mRef.setValue(userData);
            Util.makeToast(getContext(), "Save Successfully");
        } catch (Exception e){

        }
        return true;
    }

    private void saveDataToDatabase(){

    }

}
