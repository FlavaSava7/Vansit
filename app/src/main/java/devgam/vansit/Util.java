package devgam.vansit;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import devgam.vansit.JSON_Classes.Offers;
import devgam.vansit.JSON_Classes.Users;

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
    static Users currentUser = null;

    // TODO: Real Time Database Variable Names
    static final String RDB_USERS = "Users";
    static final String RDB_OFFERS = "Offers";
    static final String RDB_FAVORITE = "favorite";
    static final String RDB_COUNTRY = "Country";
    static final String RDB_JORDAN = "Jordan";
    static final String RDB_TYPE = "type";

    static final String RDB_AMMAN = "Amman";
    static final String RDB_ZARQA = "Zarqa";

    // TODO: Real Time Database Variable Names FOR USERS CLASS

    static final String FIRST_NAME = "firstName";
    static final String LAST_NAME = "lastName";
    static final String CITY = "city";
    static final String PHONE = "phone";
    static final String GENDER = "gender";

    static final String DATE_DAY = "dateDay";
    static final String DATE_MONTH = "dateMonth";
    static final String DATE_YEAR = "dateYear";

    static final String RATE_SERVICE = "rateService";
    static final String RATE_SERVICE_COUNT = "rateServiceCount";
    static final String RATE_PRICE = "ratePrice";
    static final String RATE_PRICE_COUNT = "ratePriceCount";
    static final String RATED_FOR = "ratedFor";

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
    static private boolean isOnline()
    { // this works with Check Connection Function

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException | InterruptedException e)
        { e.printStackTrace(); }


        return false;
    }
    static boolean isOnlineApi18(Context context)
    {
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
            ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_out);
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

    static void makeToast(Context context, String msg)
    {
        Toast.makeText(context ,msg, Toast.LENGTH_SHORT ).show();
    }
    static void OutsideTouchKeyBoardHider(View view, final FragmentActivity fragmentActivity)// use this so u can click outside an Editbox to hide he keyboard
    {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    HideKeyboard(fragmentActivity);
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                OutsideTouchKeyBoardHider(innerView,fragmentActivity);
            }
        }
    }

    static void HideKeyboard(FragmentActivity fragmentActivity)
    {

        FrameLayout frameLayout = (FrameLayout)fragmentActivity.findViewById(R.id.FragmentContainer);
        InputMethodManager imm = (InputMethodManager)fragmentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(frameLayout.getWindowToken(), 0);
    }

    //used in list view to set icons to rows
    static Drawable getDrawableResource(Activity activity, int resID) {
        return ContextCompat.getDrawable(activity.getApplicationContext(), resID);//context.compat checks the version implicitly
    }

    static int changeIcon(String type){
        int typeIcon = R.drawable.common_google_signin_btn_icon_dark;
        switch(type) {
            case "Car":
                typeIcon = R.mipmap.ic_type_car;
                break;
            case "Bus":
                typeIcon = R.mipmap.ic_type_bus;
                break;
            case "Taxi":
                typeIcon = R.mipmap.ic_type_taxi;
                break;
            case "Truck":
                typeIcon = R.mipmap.ic_type_truck;
                break;
        }

        return typeIcon;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    //To check first, last name & phone edit text is empty or not !
    static boolean checkEdit(Activity activity,  Drawable errorIcon, EditText editText, String errorMsg){
        Util.setErrorMsg(activity, errorIcon);
        if(! editText.getText().toString().isEmpty() && !(editText.getText().toString() == "")) {
            //Util.makeToast(getContext(), "name done");
            return true;
        } else {
            editText.setError(errorMsg, errorIcon);
            editText.requestFocus();

            //Util.makeToast(getActivity(), "Name is required");
            return false;
        }
    }

    //to set an error icon on edit text if it was empty
    private static void setErrorMsg(Activity activity, Drawable errorIcon){
        errorIcon = activity.getResources().getDrawable(R.drawable.ic_error);
        errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
        //editText.setError(null,errorIcon);
    }

    static ArrayList<Offers>  SortByTimeStampDesc(ArrayList<Offers> arrayToSort)// sort offers by date
    {
        Collections.sort(arrayToSort, new Comparator<Offers>() {
            @Override
            public int compare(Offers o1, Offers o2) {
                return o1.getTimeStamp().compareTo(o2.getTimeStamp());
            }
        });
        Collections.reverse(arrayToSort);
        return arrayToSort;
    }
}
