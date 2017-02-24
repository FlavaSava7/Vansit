package devgam.vansit;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;


import java.io.IOException;
import java.util.Calendar;

/**
 THIS CLASS IS FOR METHODS THAT WILL BE RE-USED ALOT
 and FOR ALL GLOBAL VARIABLES THAT WILL BE USED FOR ALL ACTIVITIES.

 - Global Variables should be in ALL_CAPS
 */

class Util
{

    Util()
    {
        // Empty Constructor}
    }
    // TODO: Global Normal Variables
    static boolean IS_USER_CONNECTED = false ; // if InternetListener detected the user lost connection to internet this will be FALSE , otherwise TRUE


    // TODO: Real Time Database Variable Names
    static final String RDB_USERS = "Users";
    static final String RDB_OFFERS = "Offers";
    static final String RDB_COUNTRY = "Country";
    static final String RDB_JORDAN = "Jordan";

    static final String RDB_AMMAN = "Amman";
    static final String RDB_ZARQA = "Zarqa";


    static final String RDB_CAR = "Car";
    static final String RDB_BUS = "Bus";
    static final String RDB_TRUCK = "Truck";
    static final String RDB_TAXI = "Taxi";

    // TODO: Variables for sharedPreference Data to get locally As static members
    static String userName, phoneNumber, userGender, userCity;
    static int dayOfBirth, monthOfBirth, yearOfBirth;

    final static Calendar CALENDAR = Calendar.getInstance();
    //These values to get current date and open date picker on current date
    static int dayNow = CALENDAR.get(Calendar.DAY_OF_MONTH);
    static int monthNow = CALENDAR.get(Calendar.MONTH);
    static int yearNow = CALENDAR.get(Calendar.YEAR);



    // TODO: Methods
    static boolean CheckConnection(Context context)//this will use isOnline or isOnlineApi18 to check for internet
    {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting() && isOnline());
    }
    static private boolean isOnline() { // this works with Check Connection Function

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException | InterruptedException e)
        { e.printStackTrace(); }


        return false;
    }
    static boolean isOnlineApi18(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++){
                    if (info[i].getState()==NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean isLogged()// to check if user is already logged
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        return ( firebaseAuth.getCurrentUser() != null);
    }


    static void ChangeFrag(Fragment fragment, FragmentManager fragmentManager)//change fragments
    {
        String backStateName = fragment.getClass().getName();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);
        if (!fragmentPopped)
        {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.FragmentContainer, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }


    static void ProgDialogStarter(ProgressDialog progressDialog , @Nullable String message )
    {
        // to show progress bar ,then u need to call ProgDialogDelay method with time parameter to stop ProgressDialog after time Ends.
        // @Nullable String message so we can call this method from  ProgDialogDelay without passing msg , because JAVA does not have default values ex. (String msg="")
        if(progressDialog.isShowing())
        {
            progressDialog.dismiss();
            return;
        }
        try {
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            if(message!=null)
                progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }catch (Exception e)
        {

        }
    }
    static void ProgDialogDelay(final ProgressDialog progressDialog, long timer) // to stop progress bar after timer seconds
    {
        if(progressDialog==null)
            return;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Util.ProgDialogStarter(progressDialog,null);
            }
        }, timer);
    }

    static void makeToast(Context context, String msg){
        Toast.makeText(context ,msg, Toast.LENGTH_SHORT ).show();
    }

}
