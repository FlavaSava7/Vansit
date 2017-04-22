package devgam.vansit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by Nimer Esam on 19/04/2017.
 */

public class recommendedSharedPref {

    private SharedPreferences recommendedSharedPref;
    private SharedPreferences.Editor recommendedSharedPrefEditor;
    private Activity activity;
    private int nothing, car, bus, truck, taxi;

    recommendedSharedPref(Activity a) {
        this.activity = a;
        recommendedSharedPref = activity.getSharedPreferences("recommendedSharedPref", Context.MODE_PRIVATE);
        recommendedSharedPrefEditor = recommendedSharedPref.edit();

    }

    public boolean adddToFavType(String type){
        convertTypeToInt();
        switch(type) {
            case "Car":
                car++;
                recommendedSharedPrefEditor.putString("car", car+"");
                break;
            case "Bus":
                bus++;
                recommendedSharedPrefEditor.putString("bus", bus+"");
                break;
            case "Truck":
                truck++;
                recommendedSharedPrefEditor.putString("truck", truck+"");
                break;
            case "Taxi":
                taxi++;
                recommendedSharedPrefEditor.putString("taxi", taxi+"");
                break;
            default :
                return false;
        }
        return recommendedSharedPrefEditor.commit();
    }

    private void convertTypeToInt(){
        //to get real data saved into preference :
        car = Integer.parseInt(recommendedSharedPref.getString("car", "0"));
        bus = Integer.parseInt(recommendedSharedPref.getString("bus", "0"));
        truck = Integer.parseInt(recommendedSharedPref.getString("truck", "0"));
        taxi = Integer.parseInt(recommendedSharedPref.getString("taxi", "0"));
    }

    public String getFavType(){
        convertTypeToInt();
        int[] maxArraySearch = {nothing, car, bus, taxi, truck};
        int maxNumber = 0;
        int maxIndex = 0;

        for(int i=0; i<5; i++)
            if(maxNumber < maxArraySearch[i]) {
                maxNumber = maxArraySearch[i];
                maxIndex = i;
            }

        switch (maxIndex){
            case 0:
                return "";
            case 1:
                return "Car" ;
            case 2:
                return "Bus";
            case 3:
                return "Taxi";
            case 4:
                return "Truck";
        }
        return "";
    }

}
