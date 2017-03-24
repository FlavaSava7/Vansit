package devgam.vansit;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

import devgam.vansit.JSON_Classes.Requests;


public class MyRequest extends Dialog implements android.view.View.OnClickListener
{
    private Requests myRequest;
    private Activity activity;
    private FragmentManager tempFragmentManager;

    private TextView addressText, timeText, typeText, updateText;

    private static final long WaitTimeBeforeExit=1500;
    private Calendar requestTimeEnds,currentTime;
    private CountDownTimer timer;
    private static final int requestMaxTime=30;//after 30 min delete it


    public MyRequest(Activity ac, Requests mReq, FragmentManager fragmentManager)
    {
        super(ac);
        this.activity = ac;
        this.myRequest = mReq;
        this.tempFragmentManager = fragmentManager;

    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_my_request);

        timeText = (TextView) findViewById(R.id.my_request_time);
        addressText = (TextView) findViewById(R.id.my_request_address);
        typeText = (TextView) findViewById(R.id.my_request_type);
        updateText = (TextView) findViewById(R.id.my_request_update);

        updateText.setOnClickListener(this);

        try
        {
            addressText.setText("Address: "+myRequest.getAddress());
            typeText.setText("Type: "+myRequest.getOffer().getType());

            requestTimeEnds = Calendar.getInstance();
            requestTimeEnds.setTimeInMillis(myRequest.getTimeStamp());
            requestTimeEnds.add(Calendar.MINUTE,requestMaxTime);

            currentTime = Calendar.getInstance();
            currentTime.setTimeInMillis(System.currentTimeMillis());

            if(currentTime.after(requestTimeEnds))
            {
                timeText.setText("Too Late Your Request Was Deleted !");
                return;
            }

            timer = CountDownTimer();
            timer.start();
        }
        catch (Exception e)
        {

        }

    }
    private CountDownTimer CountDownTimer()
    {

        return new CountDownTimer(requestTimeEnds.getTimeInMillis()-currentTime.getTimeInMillis(), 1000)
        {

            public void onTick(long millisUntilFinished)
            {
                String Min = String.valueOf(millisUntilFinished / 60000);
                String Sec = String.valueOf((millisUntilFinished / 1000)%60);

                if(Long.valueOf(Min)<10)
                    Min = 0+Min;
                if(Long.valueOf(Sec)<10)
                    Sec = 0+Sec;

                timeText.setText("Time remaining: " + Min +":"+Sec);
            }

            public void onFinish()
            {
                timeText.setTextColor(activity.getResources().getColor(R.color.deleteButtonColor));
                timeText.setText("Your Request is Deleted !");

                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        DatabaseReference myRefRequests = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS +"/"+
                                myRequest.getOffer().getUserID());
                        myRefRequests.removeValue();

                        myRequest=null;

                        addRequest addRequestPage = new addRequest();
                        hideLayout();

                        Util.ChangeFrag(addRequestPage,tempFragmentManager);
                    }
                },WaitTimeBeforeExit);



            }
        };
    }
    private void hideLayout()
    {
        this.hide();
    }
    @Override
    public void onClick(View v)// just reset time?
    {
        if(myRequest==null)
            return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Util.RDB_REQUESTS
                +"/"+myRequest.getOffer().getUserID());
        myRequest.setTimeStamp(System.currentTimeMillis());
        databaseReference.setValue(myRequest);

        timer.cancel();

        requestTimeEnds = Calendar.getInstance();
        requestTimeEnds.setTimeInMillis(myRequest.getTimeStamp());
        requestTimeEnds.add(Calendar.MINUTE,requestMaxTime);

        currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());

        timer=CountDownTimer();
        timer.start();
    }
}

