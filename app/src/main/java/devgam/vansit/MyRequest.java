package devgam.vansit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

import devgam.vansit.JSON_Classes.Requests;
import devgam.vansit.JSON_Classes.Users;


public class MyRequest extends Dialog implements android.view.View.OnClickListener
{
    private Requests myRequest;
    private Activity activity;
    private Context context;
    private FragmentManager fragmentManager;

    private TextView addressText, timeText, typeText, updateText, isThereDriversText;
    private ListView listView;
    private ArrayList<Users> driversList;
    private ArrayAdapter driversAdapter;


    private static final long WaitTimeBeforeExit=1500;
    private Calendar requestTimeEnds,currentTime;
    private CountDownTimer timer;// obj which get ticks down to zero
    private static final int requestMaxTime=30;//after 30 min delete it


    public MyRequest(Activity ac,Context c, Requests mReq, FragmentManager fragmentManager)
    {
        super(ac);
        this.activity = ac;
        this.context = c;
        this.myRequest = mReq;
        this.fragmentManager = fragmentManager;
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
        isThereDriversText = (TextView) findViewById(R.id.my_request_is_there_drivers);

        updateText.setOnClickListener(this);

        try
        {
            addressText.setText("Address: "+myRequest.getAddress());
            typeText.setText("Type: "+myRequest.getType());

            requestTimeEnds = Calendar.getInstance();
            requestTimeEnds.setTimeInMillis(myRequest.getTimeStamp());
            requestTimeEnds.add(Calendar.MINUTE,requestMaxTime);

            currentTime = Calendar.getInstance();
            currentTime.setTimeInMillis(System.currentTimeMillis());
            timer = CountDownTimer();

            if(currentTime.after(requestTimeEnds))
            {
                timeText.setText("Too Late Your Request Was Deleted !");
                return;
            }


            timer.start();


            driversList = myRequest.getServeDrivers();
            if(driversList==null)// no drivers to serve you
            {
                isThereDriversText.setVisibility(View.VISIBLE);
                return;
            }
            else
            {
                if(driversList.isEmpty())
                {
                    isThereDriversText.setVisibility(View.VISIBLE);
                    return;
                }

                isThereDriversText.setVisibility(View.GONE);
                listView = (ListView) findViewById(R.id.my_request_listView);
                driversAdapter = new itemsAdapter(context);
                listView.setAdapter(driversAdapter);
            }








        }
        catch (Exception e)
        {
            Log.v("Main","error "+e.getLocalizedMessage());
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
                                myRequest.getUser().getUserID());
                        myRefRequests.removeValue();

                        myRequest=null;

                        addRequest addRequestPage = new addRequest();
                        hideLayout();
                        fragmentManager.popBackStack();
                        Util.ChangeFrag(addRequestPage, fragmentManager);
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
                +"/"+myRequest.getUser().getUserID());
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

    private class itemsAdapter extends ArrayAdapter<Users>
    {
        Context context;
        itemsAdapter(Context c) {
            super(c, R.layout.fragment_my_request_listview_items, driversList);
            this.context = c;

        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent)
        {

            final ViewHolder holder = new ViewHolder();
            final Users tempDriver = driversList.get(position);
            View rowItem;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowItem = inflater.inflate(R.layout.fragment_my_request_listview_items, parent, false);

            holder.Name = (TextView) rowItem.findViewById(R.id.my_request_items_nameData);
            holder.ratingService = (TextView) rowItem.findViewById(R.id.my_request_items_serviceRatingData);
            holder.ratingPrice = (TextView) rowItem.findViewById(R.id.my_request_items_priceRatingData);

            holder.acceptText = (Button) rowItem.findViewById(R.id.my_request_items_accept_text);
            holder.profileText = (Button) rowItem.findViewById(R.id.my_request_items_profile_text);
            holder.declineText = (Button) rowItem.findViewById(R.id.my_request_items_decline_text);

            holder.Name.setText(tempDriver.getFirstName()+" "+tempDriver.getLastName());
            holder.ratingService.setText("("+tempDriver.getRateService()+"/5)");
            holder.ratingPrice.setText("("+tempDriver.getRatePrice()+"/5)");


            holder.acceptText.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Toast.makeText(context, "accepted", Toast.LENGTH_SHORT).show();
                }
            });

            final userInformation userIn = new userInformation(activity,tempDriver, fragmentManager);
            holder.profileText.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    userIn.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    userIn.show();
                }
            });

            holder.declineText.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Toast.makeText(context, "declineText", Toast.LENGTH_SHORT).show();
                }
            });


            return rowItem;


        }
        @Override
        public int getCount() {
            return driversList.size();
        }

        @Override
        public Users getItem(int position) {
            return driversList.get(position);
        }

    }


    private static class ViewHolder {
        TextView Name, ratingService, ratingPrice;
        Button acceptText, profileText, declineText ;
    }
}

