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
    static boolean IS_USER_CONNECTED = false ; // if InternetListener detected the User lost connection to internet this will be FALSE , otherwise TRUE
    static Users currentUser = null;

    // TODO: Real Time Database Variable Names
    static final String RDB_USERS = "Users";
    static final String RDB_OFFERS = "Offers";
    static final String RDB_FAVOURITE = "Favourites";
    static final String RDB_REQUESTS = "Requests";
    static final String RDB_JORDAN = "Jordan";
    static final String RDB_TYPE = "type";

    // TODO: Real Time Database Variable Names FOR USERS CLASS

    static final String FIRST_NAME = "firstName";
    static final String LAST_NAME = "lastName";
    static final String CITY = "city";
    static final String PHONE = "phone";
    static final String GENDER = "gender";
    static final String DATE_DAY = "dateDay";
    static final String DATE_MONTH = "dateMonth";
    static final String DATE_YEAR = "dateYear";
    static final String RATED_FOR = "ratedFor";

    static final String RATE_SERVICE = "rateService";
    static final String RATE_SERVICE_COUNT = "rateServiceCount";
    static final String RATE_PRICE = "ratePrice";
    static final String RATE_PRICE_COUNT = "ratePriceCount";

    // TODO: Real Time Database Variable Names FOR OFFERS CLASS
    // no city var cuz we have the same on users class
    static final String USER_ID = "userID";
    static final String COUNTRY = "country";
    static final String TYPE = "type";
    static final String DESCRIPTION = "description";
    static final String TITLE = "title";
    static final String TIME_STAMP = "timeStamp";


    final static Calendar CALENDAR = Calendar.getInstance();
    //These values to get current date and open date picker on current date
    static int dayNow = CALENDAR.get(Calendar.DAY_OF_MONTH);
    static int monthNow = CALENDAR.get(Calendar.MONTH);
    static int yearNow = CALENDAR.get(Calendar.YEAR);

    // TODO: METHODS

    /**
     * Initial filling of the IS_USER_CONNECTED boolean
     * Check Connection is used to check if the User is connected to the internet, it uses isOnline() method
     * And You should use isOnline18 , if the API target is 18
     * However The InternetListener broadcast receiver is used to change IS_USER_CONNECTED to true or false.
     * @param context
     * @return
     */
    static boolean CheckConnection(Context context)
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

    /**
     * Is the User Logged?
     * @return True if User is Logged
     */
    static boolean isLogged()// to check if User is already logged
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        return ( firebaseAuth.getCurrentUser() != null);
    }

    /**
     * Change the Current Fragment to Other One
     * @param fragment : fragment to go to
     * @param fragmentManager
     */
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

    /**
     * Starts a Progress Dialog OR Stop Progress Dialog if it got called from ProgDialogDelay
     * @param progressDialog
     * @param message : Message to show, Can be null because we call this method from ProgDialogDelay to stop the progress after X time
     */
    static void ProgDialogStarter(ProgressDialog progressDialog , @Nullable String message )
    {

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

    /**
     * This Is used to Delay the Progress Dialog that was started by ProgDialogStarter
     * @param progressDialog
     * @param timer
     */
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

    /**
     * Create A Toast
     * @param context
     * @param msg : Message to Show
     */
    static void makeToast(Context context, String msg)
    {
        Toast.makeText(context ,msg, Toast.LENGTH_SHORT ).show();
    }

    /**
     * Use This So Yopu Can Click Outside an EditText to Hide The Keyboard Using HideKeyboard Method
     * @param view : the layout that contains the edit texts
     * @param fragmentActivity : getActivity()
     */
    static void OutsideTouchKeyBoardHider(View view, final FragmentActivity fragmentActivity)
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

    /**
     * Hide The KeyBoard, used in OutsideTouchKeyBoardHider Method
     * @param fragmentActivity : getActivity()
     */
    static void HideKeyboard(FragmentActivity fragmentActivity)
    {

        FrameLayout frameLayout = (FrameLayout)fragmentActivity.findViewById(R.id.FragmentContainer);
        InputMethodManager imm = (InputMethodManager)fragmentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(frameLayout.getWindowToken(), 0);
    }

    /**
     * Used in List View to Set Icons to Rows
     * @param activity : getActivity()
     * @param resID : ID of the Image
     * @return Vaild Image To Use
     */
    static Drawable getDrawableResource(Activity activity, int resID) {
        return ContextCompat.getDrawable(activity.getApplicationContext(), resID);//context.compat checks the version implicitly
    }

    /**
     *
     * @param type : name of the Type
     * @return : ID of the icon
     */
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

    /**
     *
     * @param target : the character to check if email is valid
     * @return false of target parameter is null, otherwise check if Email is vaild
     */
    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    /**
     * To Check First, Last Name & Phone Edit Text Is Empty or Not !
     * @param activity : getActivity()
     * @param errorIcon : Icon of the Error
     * @param editText : The Edit Text to check on
     * @param errorMsg : Message of The Error
     * @return True if EditText.getText is Valid, otherwise false
     */
    static boolean checkEdit(Activity activity,  Drawable errorIcon, EditText editText, String errorMsg){
        Util.setErrorMsg(activity, errorIcon);
        if(! editText.getText().toString().isEmpty() && !(editText.getText().toString().equals(""))) {
            //Util.makeToast(getContext(), "name done");
            return true;
        } else {
            editText.setError(errorMsg, errorIcon);
            editText.requestFocus();

            //Util.makeToast(getActivity(), "Name is required");
            return false;
        }
    }

    /**
     * To Set an Error Icon on Edit Text If It Was Empty
     * @param activity : getActivity()
     * @param errorIcon : Icon of the Error
     */
    private static void setErrorMsg(Activity activity, Drawable errorIcon){
        errorIcon = activity.getResources().getDrawable(R.drawable.ic_error);
        errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
        //editText.setError(null,errorIcon);
    }

    /**
     * Sort ArrayList by Date that is in Offer Class
     * @param arrayToSort
     * @return Sorted ArrayList
     */
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

    /**
     * Change the Title of the page.
     * @param fragmentActivity : use getActivity()
     * @param titleID : R.string.ID_OF_THE_STRING
     */
    static void ChangePageTitle(FragmentActivity fragmentActivity, int titleID)
    {
        try {
            if(!fragmentActivity.getTitle().equals(fragmentActivity.getResources().getString(titleID)))
                fragmentActivity.setTitle(fragmentActivity.getResources().getString(titleID));
        }
        catch (NullPointerException e ) {
        }
    }
}
