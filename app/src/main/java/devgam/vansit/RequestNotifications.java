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
import com.google.firebase.iid.FirebaseInstanceId;
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

    public RequestNotifications() {}
    public RequestNotifications(Context context,String deviceToken, Requests request)
    {
        String serverKey = context.getString(R.string.server_key);
        String fcmUrl = context.getString(R.string.fcm_url);
        SendNotification(context,deviceToken,serverKey,fcmUrl, request);
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
            //Log.v("Main", "RequestNotifications -> Message Body: " + remoteMessage.getNotification().getBody());
            GetNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

        }
    }

    /**
     * If app is in foreground this function will run, otherwise the notification will be built inside Manifest meta-data for For FireBase
     * @param messageTitle
     * @param messageBody
     */
    private void GetNotification(String messageTitle, String messageBody)
    {
        Intent intent = new Intent(this, MainController.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("serve","PLEASE");
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
     * Send Notification to user that we will serve !
     * @param requestToken : token of the user
     */
    public void SendNotification(final Context context, String requestToken, final String serverKey, String fcmUrl, final Requests request)
    {
        String msg = "You got a driver!";
        String title = "Vansit";
        JSONObject obj = null;
        JSONObject objData = null;
        try {
            obj = new JSONObject();
            objData = new JSONObject();
            objData.put("body", msg);
            objData.put("title", title);
            obj.put("to", requestToken);
            obj.put("notification", objData);

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
                                ViewRequests.SuccessSendingNotifications(true,request);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                // TODO: SHOW ERROR TO USER SOME-HOW , TOAST DOES NOT WORK HERE with  getApplicationContext
                                ViewRequests.SuccessSendingNotifications(false,request);
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


}

