package devgam.vansit;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

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

        //if user don't set his birthday
        //temp code !!!
            birthEdit.setText(Util.dayNow + " / " + Util.monthNow + "/ " + Util.yearNow);

        maleRadio = (RadioButton) getActivity().findViewById(R.id.my_account_male_radio);
        femaleRadio = (RadioButton) getActivity().findViewById(R.id.my_account_female_radio);

        birthEdit.setOnClickListener(this);
        saveButton.setOnClickListener(this);
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
            checkAndChange();
        }
    }

    //Check if values is valid
    private boolean checkAndChange(){
        String tempStringCheck = nameEdit.getText().toString();

        //Check if name field is not null !
        if(tempStringCheck.isEmpty() || tempStringCheck == "") {
            Util.makeToast(getActivity(), "Name is required");
            return false;
        }

        //Check birth day
        if(Util.dayOfBirth == 0 || Util.monthOfBirth == 0 || Util.yearOfBirth == 0){
            //The initial value of int is 0, then if still 0 that's mean user never add his birthday
            Util.makeToast(getActivity(), "Birth day is required");
            return false;
        } else if(Util.yearNow + 16 < tempYearOfBirth) {
            //Check if user add real birthDate not current date !
            //Just year because no body born in this year can make account
            //No one less than 16 can drive or make deal with other people
            Util.makeToast(getActivity(), "Invalid Birth day");
            return false;
        }

        //set values from views to vars
        tempUserName = nameEdit.getText().toString();

        tempStringCheck = phoneEdit.getText().toString();
        if(!tempStringCheck.isEmpty() && tempStringCheck != "")
            tempPhoneNumber = phoneEdit.getText().toString();

        //need code to set city & gender

        return true;

    }


}

