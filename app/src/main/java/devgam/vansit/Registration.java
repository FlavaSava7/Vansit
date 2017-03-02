package devgam.vansit;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registration extends Fragment implements View.OnClickListener{

    private EditText emailEdit, passwordEdit;
    private Button signUpButton;
    private TextView signInText, errorText;

    private ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    public Registration() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Initialize signing in progress Dialog :
        progressDialog = new ProgressDialog(getContext());

        //Initialize Views of activity :
        emailEdit = (EditText) getActivity().findViewById(R.id.register_email_edit);
        passwordEdit = (EditText) getActivity().findViewById(R.id.register_pass_edit);
        signUpButton = (Button) getActivity().findViewById(R.id.register_signup_button);
        signInText = (TextView) getActivity().findViewById(R.id.register_signin_text);
        errorText = (TextView) getActivity().findViewById(R.id.register_error_msg_text);

        //to set error text not visible if user out from app and come again :
        errorText.setVisibility(View.INVISIBLE);

        //on click listener for buttons :
        signUpButton.setOnClickListener(this);
        signInText.setOnClickListener(this);

        //to sign in process :
        firebaseAuth = FirebaseAuth.getInstance();
        fragmentManager  = getActivity().getSupportFragmentManager();
    }

    private void userRegistration(){
        final String email = emailEdit.getText().toString().trim();
        final String pass = passwordEdit.getText().toString().trim();


        //To check email is not empty :
        if(TextUtils.isEmpty(email)){
            //if user didn't enter his email
            return;
        }

        //To check pass is not empty :
        if(TextUtils.isEmpty(pass)){
            //if user didn't enter his password
            return;
        }

        //if all data are okay
        //first progress dialog is show :
        Util.ProgDialogStarter(progressDialog,"Registration");

        //To complete registration after sign up :
        final myAccount myAccountPage = new myAccount();

        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Util.ProgDialogDelay(progressDialog,1000L);// wait 1 more second

                        if(task.isSuccessful())
                        {
                            //Registration done
                            //now user should complete entering his data
                            Util.ChangeFrag(myAccountPage,fragmentManager);// use like this to go from fragment to other
                        }
                        else
                        {
                            Log.v("Main:","userRegistration: "+task.getException());
                        }
                    }

                });
    }


    @Override
    public void onClick(View v) {
        if(v == signUpButton){
            if(Util.IS_USER_CONNECTED)
                userRegistration();
            else
                Util.makeToast(getContext(), "No Internet");
                    /*Toast.makeText(getContext(), "No Internet",
                            Toast.LENGTH_SHORT).show();*/
        } else if(v == signInText) {
            // To go to sign up fragment if he hasn't account
            Login login = new Login();
            Util.ChangeFrag(login,fragmentManager);

        }
    }
}
