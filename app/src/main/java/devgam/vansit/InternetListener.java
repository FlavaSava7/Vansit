package devgam.vansit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

import devgam.vansit.Util;

public class InternetListener extends BroadcastReceiver
{
    public InternetListener()
    {
    }


    /**
     * THIS TO KEEP CHECKING IF WE HAVE INTERNET OR NOT
    */


    @Override
    public void onReceive(Context context, Intent intent)
    {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();


        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting() && isOnline())// internet still ON
        {
            Util.IS_USER_CONNECTED =true;
            //Log.v("Main", "InternetListener : NETWORK IS AVAILABLE");
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            Util.IS_USER_CONNECTED =isOnlineApi18(context);
            //Log.v("Main","InternetListener : NETWORK(18 Api) IS "+Util.IS_USER_CONNECTED);
        }
        else
        {
            Util.IS_USER_CONNECTED = false;
            //Log.v("Main","InternetListener : NETWORK IS NOT AVAILABLE");
        }

    }
    public boolean isOnline()
    {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
    public boolean isOnlineApi18(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++){
                    if (info[i].getState()== NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
