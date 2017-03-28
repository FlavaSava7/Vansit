package devgam.vansit;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FireBaseIDService extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh()
    {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.v("Main","refreshedToken "+refreshedToken);
        String spKey = getApplicationContext().getResources().getString(R.string.vansit_shared_preferences);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(spKey,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("deviceToken",refreshedToken);
        editor.apply();
    }
}
