package devgam.vansit;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class Login extends Fragment {


    public Login() {
        // Required empty public constructor
    }

    Button signinButton ;
    EditText emailEdit, passEdit ;
    TextView signupText ;


    ProgressDialog progressDialog ;

    FirebaseAuth firebaseAuth;
    //private FirebaseAuth.AuthStateListener mAuthListener;

    FragmentManager fragmentManager;// this is used for the ChangeFrag method

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
        progressDialog = new ProgressDialog(getContext());

        signinButton = (Button) getActivity().findViewById(R.id.login_signin_button);
        emailEdit = (EditText) getActivity().findViewById(R.id.login_email_edit);
        passEdit = (EditText) getActivity().findViewById(R.id.login_pass_edit);
        signupText = (TextView) getActivity().findViewById(R.id.login_signup_text);

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                if(Util.IS_USER_CONNECTED)
                    registerUser();
                else
                    Toast.makeText(getContext(), "No Internet",
                            Toast.LENGTH_SHORT).show();

            }
        });
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        fragmentManager = getActivity().getSupportFragmentManager();

        if (firebaseAuth.getCurrentUser() != null) {
            // User is signed in
            //open activity

            /*finish();
            startActivity(new Intent(getApplicationContext(), Profile.class));*/

            Profile profilePage = new Profile();
            Util.ChangeFrag(profilePage,fragmentManager);

        }
    }
    private void registerUser(){
        String Email = emailEdit.getText().toString().trim();
        String password = passEdit.getText().toString().trim();

        if(TextUtils.isEmpty(Email)){
            //Email is empty
            Toast.makeText(getContext(), "Email required", Toast.LENGTH_SHORT ).show();
            //to stop Function :
            return;
        }

        if(TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(getContext(), "password required", Toast.LENGTH_SHORT ).show();
            //to stop Function :
            return;
        }

        //validation is ok :
        //first progress dialog is show :

        Util.ProgDialogStarter(progressDialog,"Signing in ..");


        final Main mainPage = new Main();

        firebaseAuth.signInWithEmailAndPassword(Email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Util.ProgDialogDelay(progressDialog,1000L);// wait 1 more second
                        //Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful())
                        {
                            Log.v("Main:","signInWithEmailAndPassword ::"+task.getException());
                            Toast.makeText(getContext(), "signInWithEmailAndPassword : Authentication failed",
                                    Toast.LENGTH_SHORT).show();

                            Util.ChangeFrag(mainPage,fragmentManager);// use like this to go from fragment to other

                        }else {
                            Toast.makeText(getContext(), "Authentication successful",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });


    }

}
