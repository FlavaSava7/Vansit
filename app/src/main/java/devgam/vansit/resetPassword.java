package devgam.vansit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class resetPassword extends Fragment implements View.OnClickListener{

    private EditText emailEdit;
    private Button resetButton ;

    private ProgressDialog progressDialog ;

    FirebaseAuth firebaseAuth;
    FragmentManager fragmentManager;

    public resetPassword() {
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
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Initialize signing in progress Dialog :
        progressDialog = new ProgressDialog(getContext());

        //Initialize Views of activity :
        emailEdit = (EditText) getActivity().findViewById(R.id.reset_emait_edit);
        resetButton = (Button) getActivity().findViewById(R.id.reset_button);

        resetButton.setOnClickListener(this);

        //to sign in process :
        firebaseAuth = FirebaseAuth.getInstance();
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    private void resetPass(){
        String emailAddress = emailEdit.getText().toString().trim();

        //To check email is not empty :
        if(TextUtils.isEmpty(emailAddress) ){
            //Email is empty
            Util.makeToast(getContext(), "Email required");
            //to stop Function :
            return;
        }

        Util.ProgDialogStarter(progressDialog,"Checking email address ..");

        firebaseAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            Util.HideKeyboard(getActivity());
                            alertDialog();
                            Util.ChangeFrag(new Login(), fragmentManager);
                        } else {
                            //Temp code
                            Util.makeToast(getContext(), "Fail");
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        if (v == resetButton) {
            //Check if user has internet connection or not !
            if(Util.IS_USER_CONNECTED)
                //reset process
                resetPass();
            else
                Util.makeToast(getContext(), "No Internet");

        }
    }

    private void alertDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setMessage("We've sent an email to your email address. Click the link in the email to reset your password");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
