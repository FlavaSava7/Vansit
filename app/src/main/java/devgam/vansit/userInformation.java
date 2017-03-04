package devgam.vansit;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import devgam.vansit.JSON_Classes.Users;


public class userInformation extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    TextView nameText, ageText, cityText, moreOffersText;
    ImageView userImg;
    String tempUserName, tempUserAgeYear, tempUserAgeMonth, tempUserCity, tempUserGender;

    // A.J.I. : to get the user offers for MoreOffers Page ( this var. is used in onClick )
    FragmentManager tempFragmentManager;// i cant get this from this activity dialog , i need to have it in your constructor

    public userInformation(Activity activity)
    {
        super(activity);
        // Required empty public constructor
        this.c = activity;
    }

    public userInformation(Activity activity, String name, String year, String month, String city, String gender,
                           FragmentManager fragmentManager){
        super(activity);
        // Required empty public constructor
        this.c = activity;
        this.tempUserName = name;
        this.tempUserAgeYear = year;
        this.tempUserAgeMonth = month;
        this.tempUserCity = city;
        this.tempUserGender = gender;
        this.tempFragmentManager = fragmentManager;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_user_information);

        nameText = (TextView) findViewById(R.id.user_information_name);
        ageText = (TextView) findViewById(R.id.user_information_age);
        cityText = (TextView) findViewById(R.id.user_information_city);
        moreOffersText = (TextView) findViewById(R.id.user_information_more);

        userImg = (ImageView) findViewById(R.id.user_information_img);

        moreOffersText.setOnClickListener(this);

        try{
            //set user name to text on dialog
            if(tempUserName != "" || ! tempUserName.isEmpty())
                nameText.setText(tempUserName);

            //set user age to text on dialog
            int age =  Util.yearNow - Integer.parseInt(tempUserAgeYear) ;
            age = (Util.monthNow > Integer.parseInt(tempUserAgeMonth) ? age : age -1 );
            ageText.setText("Age is " + age + " years old");

            //set user name to text on dialog
            if(tempUserCity != "" || ! tempUserCity.isEmpty())
                cityText.setText("Lives in " + tempUserCity );

            if(!tempUserGender.isEmpty() || tempUserGender != ""){
                if (tempUserGender.equals("male"))
                    userImg.setImageResource(R.drawable.ic_user_male);
                else if (tempUserGender.equals("female"))
                    userImg.setImageResource(R.drawable.ic_user_female);
            }


        }catch (Exception e){

        }

    }


    @Override
    public void onClick(View v)
    {
        MoreOffers moreOffersPage = new MoreOffers();
        Bundle bundle = new Bundle();
        moreOffersPage.setArguments(bundle);
        this.hide();
        Util.ChangeFrag(moreOffersPage,tempFragmentManager);
    }
}
