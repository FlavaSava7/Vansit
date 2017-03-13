package devgam.vansit;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;

public class Login extends Fragment implements View.OnClickListener{

    private Button signInButton;
    private EditText emailEdit, passEdit ;
    private TextView signUpText, errorText, forgetPassText;
    private TextInputLayout emailInput, passInput;
    LinearLayout googleSignIn;

    private ProgressDialog progressDialog ;

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener firebaseListener;
    FragmentManager fragmentManager;// this is used for the ChangeFrag method
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 1;

    private Drawable errorIcon;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //to sign in process :
        firebaseAuth = FirebaseAuth.getInstance();
        fragmentManager = getActivity().getSupportFragmentManager();

        firebaseListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

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
        googleSignIn = (LinearLayout) getActivity().findViewById(R.id.login_google_layout);

        //to set error text not visible if user out from app and come again :
        errorText.setVisibility(View.INVISIBLE);

        //on click listener for buttons :
        signInButton.setOnClickListener(this);
        signUpText.setOnClickListener(this);
        forgetPassText.setOnClickListener(this);
        googleSignIn.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseListener);
    }

    private void userSignIn(){
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
                Util.makeToast(getContext(), String.valueOf(R.string.noInternetMsg));

        } else if(v == signUpText) {
            // To go to sign up fragment if he hasn't account
            Registration registerPage = new Registration();
            Util.ChangeFrag(registerPage,fragmentManager);

        } else if(v == forgetPassText){
            //Go to forget password page ..
            resetPassword reset = new resetPassword();
            Util.ChangeFrag(reset,fragmentManager);
        } else if(v == googleSignIn){
            signIn();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                try {
                    firebaseAuthWithGoogle(account);
                } catch (Exception e){

                }
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        myAccount ma = new myAccount();
                        Util.ChangeFrag(ma, fragmentManager);
                    }

                });
    }



}
