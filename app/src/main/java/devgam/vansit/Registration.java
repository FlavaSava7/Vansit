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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registration extends Fragment {


    public Registration() {
        // Required empty public constructor
    }

    EditText emailEdit, passwordEdit;
    Button signup;
    ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth;

    FragmentManager fragmentManager;// this is used for the ChangeFrag method

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

        emailEdit = (EditText) getActivity().findViewById(R.id.register_email_edit);
        passwordEdit = (EditText) getActivity().findViewById(R.id.register_pass_edit);
        signup = (Button) getActivity().findViewById(R.id.register_signup_button);

        progressDialog = new ProgressDialog(getContext());

        firebaseAuth = FirebaseAuth.getInstance();

        fragmentManager  = getActivity().getSupportFragmentManager();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(v == signup)
                {
                    // use Util.IS_USER_CONNECTED , this will keep track if we have internet , using internet broadcast receiver
//                   boolean internetConnection = checker.isConnected();
                    if(Util.IS_USER_CONNECTED)
                        userRegistration();
                    else
                        Toast.makeText(getContext(), "signup: No Internet",
                                Toast.LENGTH_SHORT).show();
                    //hi abdullah
                }
            }
        });
    }
    private void userRegistration(){
        String email = emailEdit.getText().toString().trim();
        String pass = passwordEdit.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            //if user didn't enter his email
            return;
        }

        if(TextUtils.isEmpty(pass)){
            //if user didn't enter his password
            return;
        }

        //if all data are okay
        Util.ProgDialogStarter(progressDialog,"Registration");

        final Main mainPage = new Main();

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

                            //startActivity(new Intent(getActivity().getApplicationContext(), MainPage.class));

                            Util.ChangeFrag(mainPage,fragmentManager);// use like this to go from fragment to other
                        }
                        else
                        {
                            Log.v("Main:","userRegistration: "+task.getException());
                        }




                    }

                });
    }


}
