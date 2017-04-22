package devgam.vansit;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import devgam.vansit.JSON_Classes.Users;


public class userInformation extends Dialog implements
        android.view.View.OnClickListener {

    //When call this dialog use this code to make it transparent :
    //userObj.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    public Activity c;
    TextView nameText, ageText, cityText, moreOffersText;
    ImageView userImg;
    String tempUserName, tempUserAgeYear, tempUserAgeMonth, tempUserCity, tempUserGender;


    FragmentManager tempFragmentManager;
    Users tempUserDriver;//used for the above strings inside constructor and for onClick.

    public userInformation(Activity activity)
    {
        super(activity);
        // Required empty public constructor
        this.c = activity;
    }

    public userInformation(Activity activity, Users userDriver, FragmentManager fragmentManager){
        super(activity);
        // Required empty public constructor
        this.c = activity;

        this.tempUserDriver = userDriver;

        this.tempUserName = userDriver.getFirstName()+" "+userDriver.getLastName();
        this.tempUserAgeYear = userDriver.getDateYear();
        this.tempUserAgeMonth = userDriver.getDateMonth();
        this.tempUserCity = userDriver.getCity();
        this.tempUserGender = userDriver.getGender();

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

        userImg = (ImageView) findViewById(R.id.more_user_information_img);

        moreOffersText.setOnClickListener(this);

        try{
            //set User name to text on dialog
            if(tempUserName != "" || ! tempUserName.isEmpty())
                nameText.setText(tempUserName);

            //set User age to text on dialog
            int age =  Util.yearNow - Integer.parseInt(tempUserAgeYear) ;
            age = (Util.monthNow > Integer.parseInt(tempUserAgeMonth) ? age : age -1 );
            ageText.setText(getContext().getResources().getString(R.string.user_information_age)+ " " + age );

            //set User name to text on dialog
            if(tempUserCity != "" || ! tempUserCity.isEmpty())
                cityText.setText(getContext().getResources().getString(R.string.user_information_home_city)+ " " + tempUserCity );

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
        /*oreOffers moreOffersPage = new MoreOffers();
        Bundle bundle = new Bundle();
        bundle.putSerializable("userDriver",tempUserDriver);
        moreOffersPage.setArguments(bundle);
        this.hide();
        Util.ChangeFrag(moreOffersPage,tempFragmentManager);*/


        Intent intent = new Intent(c, moreUserInformation.class);
        intent.putExtra("userDriver", tempUserDriver);
        c.startActivity(intent);
    }
}
