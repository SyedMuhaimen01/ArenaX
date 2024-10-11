package com.muhaimen.arenax.accountSettings;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

 class ClearAllSharedPreferences{


    public static void clearAllSharedPreferences(Context context) {
        // Get the shared preferences directory
        File sharedPrefsDir = new File(context.getApplicationInfo().dataDir + "/shared_prefs/");

        // List all files in the shared preferences directory
        File[] files = sharedPrefsDir.listFiles();

        if (files != null) {
            for (File file : files) {
                // Get the name of the preference file (without the .xml extension)
                String prefName = file.getName().replace(".xml", "");

                // Clear the SharedPreferences
                SharedPreferences preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear(); // Clear the preferences
                editor.apply(); // Apply the changes
            }
        }
    }
}
