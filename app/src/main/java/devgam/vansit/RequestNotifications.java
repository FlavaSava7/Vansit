package devgam.vansit;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import devgam.vansit.JSON_Classes.Requests;
import devgam.vansit.JSON_Classes.Users;

public class RequestNotifications extends FirebaseMessagingService
{

    private Context context;
    private String deviceToken;
    private Requests request;
    private String serverKey;
    private String fcmUrl;

    public static final String TYPE_OF_SERVE = "typeServe";
    public static final String ASK_TO_SERVE = "askServe";
    public static final String ACCEPTED_SERVE = "acceptServe";
    public static final String DECLINED_SERVE = "declineServe";
    public static final String USER_KEY = "userKey";// to store the request key in it , so we can use it in accepted request to fetch the request

    public RequestNotifications() {}
    public RequestNotifications(Context context,String deviceToken, Requests request)
    {
        this.context = context;
        this.deviceToken = deviceToken;
        this.request = request;
        this.serverKey = context.getString(R.string.server_key);
        this.fcmUrl = context.getString(R.string.fcm_url);
    }

    /**
     * This will run when app is in foreground
     * @param remoteMessage : message from server
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        if (remoteMessage.getNotification() != null)
        {
            if(remoteMessage.getData().size() > 0)
            {
                JSONObject result = null;
                try {
                    result = new JSONObject(remoteMessage.getData());
                    CreateNotification(remoteMessage.getNotification().getTitle(),
                            remoteMessage.getNotification().getBody(),
                            result.getString(TYPE_OF_SERVE),result.getString(USER_KEY));
                    //Log.v("Main", "result -> " + result.getString(TYPE_OF_SERVE));
                } catch (JSONException e)
                {
                    e.printStackTrace();
                    Log.v("Main", "error data " + e.getLocalizedMessage());
                }

            }
            else
            {
                Log.v("Main", "NO DATA");
            }

        }
//        Log.v("Main", "onMessageReceived");
    }

    /**
     * If app is in foreground this function will run, otherwise the notification will be built inside Manifest meta-data for For FireBase
     * @param messageTitle
     * @param messageBody
     * @param typeOfServe : it will decide where the current user/driver will go when he/she click on the notification
     */
    private void CreateNotification(String messageTitle, String messageBody, String typeOfServe, String userKey)
    {

        Intent intent = new Intent(this, MainController.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra(TYPE_OF_SERVE,typeOfServe);

        if(!userKey.equals(""))
        {
            intent.putExtra(USER_KEY,userKey);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(messageTitle)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

    }

    /**
     * Send Notification from driver to user that we WANT to serve !
     */
    public void SendNotificationToUser()
    {

        String title = context.getString(R.string.app_name);
        String msg = "You Got A Driver!";
        JSONObject obj = null;
        JSONObject objData = null;
        try {
            obj = new JSONObject();
            objData = new JSONObject();
            objData.put("body", msg);
            objData.put("title", title);
            obj.put("to", deviceToken);
            obj.put("notification", objData);
            obj.put("data", DataJSON(ASK_TO_SERVE,null));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest =
                new JsonObjectRequest(Request.Method.POST,fcmUrl, obj,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response)
                            {
                                //Log.v("Main","SUCCESS: "+ response + "");
                                Util.makeToast(context,"Success");
                                ViewRequests.SuccessSendingNotifications(true,request,context);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {

                                ViewRequests.SuccessSendingNotifications(false,request,context);
                                Util.makeToast(context,"Fail");
                                //Log.v("Main","FAIL: "+ error + "|"+error.getLocalizedMessage());
                            }
                        })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", "key=" + serverKey);
                        params.put("Content-Type", "application/json");
                        return params;
                    }
                };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }
    /**
     * Send Notification from driver to user that we CANCELED  serving !
     */
    public void SendNotificationToUserCancel(Users driver)
    {

        String title = context.getString(R.string.app_name);
        String msg = "Driver "+driver.getFirstName()+" Canceled!";
        JSONObject obj = null;
        JSONObject objData = null;
        try {
            obj = new JSONObject();
            objData = new JSONObject();
            objData.put("body", msg);
            objData.put("title", title);
            obj.put("to", deviceToken);
            obj.put("notification", objData);
            obj.put("data", DataJSON(ASK_TO_SERVE,null));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest =
                new JsonObjectRequest(Request.Method.POST,fcmUrl, obj,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response)
                            {
                                //Log.v("Main","SUCCESS: "+ response + "");
                                Util.makeToast(context,"Success");
                                //ViewRequests.SuccessSendingNotifications(true,request,context);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {

                                //ViewRequests.SuccessSendingNotifications(false,request,context);
                                Util.makeToast(context,"Fail");
                                //Log.v("Main","FAIL: "+ error + "|"+error.getLocalizedMessage());
                            }
                        })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", "key=" + serverKey);
                        params.put("Content-Type", "application/json");
                        return params;
                    }
                };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }
    /**
     * Send Notification from user to driver that user accepted or declined !
     * if accepted the driver can click on noti to go to a page to continue serving
     */
    public void SendNotificationToDriver(Users user, boolean didAccept)
    {
        String msg;
        String title = context.getString(R.string.app_name);

        if(didAccept)
        {
            msg = user.getFirstName() + " Accepted your Service!";
        }else
        {
            msg = user.getFirstName() + " Declined your Service!";
        }

        JSONObject obj = null;
        JSONObject objData = null;
        try {
            obj = new JSONObject();
            objData = new JSONObject();
            objData.put("body", msg);
            objData.put("title", title);
            obj.put("to", deviceToken);
            obj.put("notification", objData);
            obj.put("data", didAccept ? DataJSON(ACCEPTED_SERVE,user) : DataJSON(DECLINED_SERVE,user));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest =
                new JsonObjectRequest(Request.Method.POST,fcmUrl, obj,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response)
                            {
                                //Log.v("Main","SUCCESS: "+ response + "");
                                Util.makeToast(context,"Success");

                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {


                                Util.makeToast(context,"Fail");
                                //Log.v("Main","FAIL: "+ error + "|"+error.getLocalizedMessage());
                            }
                        })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", "key=" + serverKey);
                        params.put("Content-Type", "application/json");
                        return params;
                    }
                };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }

    private JSONObject DataJSON(String typeServe, Users user) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        switch (typeServe)
        {
            case ASK_TO_SERVE:jsonObject.put(TYPE_OF_SERVE,ASK_TO_SERVE);break;
            case ACCEPTED_SERVE:jsonObject.put(TYPE_OF_SERVE,ACCEPTED_SERVE);break;
            case DECLINED_SERVE:jsonObject.put(TYPE_OF_SERVE,DECLINED_SERVE);break;

        }
        if(user!=null)// this will be set only when sending from user to driver
            jsonObject.put(USER_KEY,user.getUserID());
        else
        {
            jsonObject.put(USER_KEY,"");
        }
        return  jsonObject;
    }
}

