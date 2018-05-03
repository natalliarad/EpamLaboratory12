package com.natallia.radaman.epamlabtrack;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Requests for runtime permissions are made in a separate class.
 */
public final class PermissionSettings {
    public static final int REQUEST_CODE = 200;

    /**
     * Runtime Permission Check
     */
    public static void requestPermission(AppCompatActivity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults) {
        int permissionSize = grantPermissions.length;
        for (int i = 0; i < permissionSize; i++) {
            if (Manifest.permission.ACCESS_FINE_LOCATION.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    public static class LocationSettingDialog extends DialogFragment {
        public static LocationSettingDialog newInstance() {
            return new LocationSettingDialog();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(getText(R.string.location_permission))
                    .setMessage(R.string.location_permission_text)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .create();
        }
    }
}
