package devgam.vansit;

import android.app.ProgressDialog;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends Fragment implements View.OnClickListener{

    private Button signInButton;
    private EditText emailEdit, passEdit ;
    private TextView signUpText, errorText, forgetPassText;
    private TextInputLayout emailInput, passInput;

    private ProgressDialog progressDialog ;

    FirebaseAuth firebaseAuth;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method

    private Drawable errorIcon;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Initialize signing in progress Dialog :
        progressDialog = new ProgressDialog(getContext());

        //Initialize Views of activity :
        signInButton = (Button) getActivity().findViewById(R.id.login_signin_button);
        emailEdit = (EditText) getActivity().findViewById(R.id.login_email_edit);
        passEdit = (EditText) getActivity().findViewById(R.id.login_pass_edit);
        signUpText = (TextView) getActivity().findViewById(R.id.login_signup_text);
        errorText = (TextView) getActivity().findViewById(R.id.login_error_msg_text);
        forgetPassText = (TextView) getActivity().findViewById(R.id.login_forget_pass_text);
        emailInput = (TextInputLayout) getActivity().findViewById(R.id.login_email_input);
        passInput = (TextInputLayout) getActivity().findViewById(R.id.login_password_input);

        //to set error text not visible if user out from app and come again :
        errorText.setVisibility(View.INVISIBLE);

        //on click listener for buttons :
        signInButton.setOnClickListener(this);
        signUpText.setOnClickListener(this);
        forgetPassText.setOnClickListener(this);

        //to sign in process :
        firebaseAuth = FirebaseAuth.getInstance();
        fragmentManager = getActivity().getSupportFragmentManager();

    }

    private void userSignIn(){
        /*final String Email = emailEdit.getText().toString().trim();
        final String password = passEdit.getText().toString().trim();

        //To check email is not empty :
        if(TextUtils.isEmpty(Email) ){
            //Email is empty
            //Temp code
            Util.makeToast(getContext(), "Email required");
            //to stop Function :
            return;
        } else if(Util.isValidEmail(emailEdit.getText().toString())) {

        }

        //To check password is not empty :
        if(TextUtils.isEmpty(password)){
            //password is empty
            //Temp code
            Util.makeToast(getContext(), "password required");
            //to stop Function :
            return;
        }*/
        if( ! checkEditText())
            return;

        final String Email = emailEdit.getText().toString().trim();
        final String password = passEdit.getText().toString().trim();

        //if all data are okay
        //first progress dialog is show :
        Util.ProgDialogStarter(progressDialog,"Signing in ..");

        //To go to main page after sign in :
        final Main mainPage = new Main();

        firebaseAuth.signInWithEmailAndPassword(Email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //Util.ProgDialogDelay(progressDialog,1000L);// wait 1 more second
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            //sign in done
                            Util.makeToast(getContext(), "Authentication successful");
                            Util.ChangeFrag(mainPage,fragmentManager);// use like this to go from fragment to other

                        }else {
                            Log.v("Main:","signInWithEmailAndPassword ::"+task.getException());
                            Util.makeToast(getContext(), "signInWithEmailAndPassword : Authentication failed");
                            errorText.setVisibility(View.VISIBLE);
                        }
                        // ...
                    }
                });
    }

    //Check if values is valid
    private boolean checkEditText() {
        final String Email = emailEdit.getText().toString().trim();
        final String password = passEdit.getText().toString().trim();

        boolean isValid = true;

        //To check email is not empty :

        //To check password is not empty :
        if(TextUtils.isEmpty(password)){
            //password is empty
            Util.checkEdit(getActivity(), errorIcon, passEdit, String.valueOf(R.string.login_password_null_error));
            //to stop Function :
            isValid = false;
        }


        if(TextUtils.isEmpty(Email) ){
            //Email is empty
            Util.checkEdit(getActivity(), errorIcon, emailEdit, String.valueOf(R.string.login_email_null_error));
            //to stop Function :
            isValid = false;
        }

        //not work until now :
        /* else if(Util.isValidEmail(Email)) {
            //Email is not valid
            Util.checkEdit(getActivity(), errorIcon, emailEdit, "not valid email");
            //to stop Function :
            isValid = false;
        }*/

        return isValid;
    }

    @Override
    public void onClick(View v){
        if(v == signInButton){

            if(Util.IS_USER_CONNECTED)
                userSignIn();
            else
                Util.makeToast(getContext(), "No Internet");

        } else if(v == signUpText) {
            // To go to sign up fragment if he hasn't account
            Registration registerPage = new Registration();
            Util.ChangeFrag(registerPage,fragmentManager);

        } else if(v == forgetPassText){
            //Go to forget password page ..
            resetPassword reset = new resetPassword();
            Util.ChangeFrag(reset,fragmentManager);
        }
    }


}
