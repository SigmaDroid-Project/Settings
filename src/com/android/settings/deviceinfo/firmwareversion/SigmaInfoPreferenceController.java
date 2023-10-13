/*
 * Copyright (C) 2020 Wave-OS
 * Copyright (C) 2022 Project Arcana
 * Copyright (C) 2022 BananaDroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion;

//import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemProperties;
import android.widget.TextView;
//import android.view.View;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
//import androidx.preference.PreferenceViewHolder;
//import android.widget.LinearLayout;
import com.sigma.settings.utils.SpecUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;


public class SigmaInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_SIGMA_INFO = "sigma_info";

    private static final String PROP_SIGMA_VERSION = "ro.modversion";
    private static final String PROP_SIGMA_BUILD_DATE = "ro.sigma.display.build.date";
    private static final String PROP_SIGMA_DEVICE = "ro.product.device";
    private static final String PROP_SIGMA_RELEASETYPE = "ro.sigma.release.type";
    private static final String PROP_SIGMA_BUILD_PACKAGE = "ro.sigma.build.package";
    private static final String PROP_SIGMA_MAINTAINER = "ro.sigma.maintainer";
    private static final String KEY_BUILD_STATUS = "rom_build_status";
    private static final String PROP_SIGMA_BRAND_NAME = "ro.product.manufacturer";

    private static final String KEY_STORAGE = "storage";
    private static final String KEY_CHIPSET = "chipset";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_DISPLAY = "display";

    final String isOfficial = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));

    public SigmaInfoPreferenceController(Context context) {
        super(context);
    }

    private String getSigmaVersion() {
        final String version = SystemProperties.get(PROP_SIGMA_VERSION,
                this.mContext.getString(R.string.device_info_default));
        final String SigmaBuildPackage = SystemProperties.get(PROP_SIGMA_BUILD_PACKAGE,
                this.mContext.getString(R.string.device_info_default));
        final String SigmaBuildDate = SystemProperties.get(PROP_SIGMA_BUILD_DATE,
                this.mContext.getString(R.string.device_info_default));
        return version + " / " + SigmaBuildPackage  + " / "  + SigmaBuildDate;
    }

    private String getSigmaReleaseType() {
        final String releaseType = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));


        return releaseType.substring(0, 1).toUpperCase() +
                 releaseType.substring(1).toLowerCase();
    }

    private String getSigmaMaintainer() {
        final String SigmaMaintainer = SystemProperties.get(PROP_SIGMA_MAINTAINER,
                this.mContext.getString(R.string.device_info_default));

        final String buildType = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                    this.mContext.getString(R.string.device_info_default));
        final String isOffFine = this.mContext.getString(R.string.build_status_summary, SigmaMaintainer);
        final String isOffMiss = this.mContext.getString(R.string.build_status_oopsie);
        final String isCommMiss = this.mContext.getString(R.string.build_status_oopsie);
        final String isCommFine = this.mContext.getString(R.string.build_is_community_summary, SigmaMaintainer);

        if (buildType.toLowerCase().equals("official") && !SigmaMaintainer.equalsIgnoreCase("Unknown")) {
            return isOffFine;
        } else if (buildType.toLowerCase().equals("official") && SigmaMaintainer.equalsIgnoreCase("Unknown")) {
            return isOffMiss;
        } else if (buildType.equalsIgnoreCase("Community") && SigmaMaintainer.equalsIgnoreCase("Unknown")) {
            return isCommMiss;
        } else {
            return isCommFine;
        }
        }

    private String getDeviceName() {
        String deviceBrand = SystemProperties.get(PROP_SIGMA_BRAND_NAME,
                mContext.getString(R.string.device_info_default));
        String deviceCodename = SystemProperties.get(PROP_SIGMA_DEVICE,
                mContext.getString(R.string.device_info_default));
        String deviceModel = Build.MODEL;

        return deviceBrand + " " + deviceModel + " (" + deviceCodename + ")";
    }

    private boolean isOfficial() {
        final String releaseType = SystemProperties.get(PROP_SIGMA_RELEASETYPE, "");
        return releaseType.length() > 0 &&
                "official".equals(releaseType.toLowerCase());
    }

    private String getSigmaBuildStatus() {
	    final String buildType = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        final String isOfficial = this.mContext.getString(R.string.build_is_official_title);
	    final String isCommunity = this.mContext.getString(R.string.build_is_community_title);
        if (buildType.toLowerCase().equals("official")) {
		return isOfficial;
	    } else {
		return isCommunity;
	    }
    }

    //@Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference SigmaInfoPreference = screen.findPreference(KEY_SIGMA_INFO);
        final TextView version = (TextView) SigmaInfoPreference.findViewById(R.id.version_message);
        final Preference buildStatusPref = screen.findPreference(KEY_BUILD_STATUS);

        final TextView storText = (TextView) SigmaInfoPreference.findViewById(R.id.cust_storage_summary);
        final TextView battText = (TextView) SigmaInfoPreference.findViewById(R.id.cust_battery_summary);
        final TextView chipsetText = (TextView) SigmaInfoPreference.findViewById(R.id.cust_chipset_summary);
        final TextView displayText = (TextView) SigmaInfoPreference.findViewById(R.id.cust_display_summary);
        final TextView device = (TextView) SigmaInfoPreference.findViewById(R.id.device_message);

        final String SigmaVersion = getSigmaVersion();
        final String SigmaReleaseType = getSigmaReleaseType();
        final String SigmaMaintainer = getSigmaMaintainer();
        final String buildStatus = getSigmaBuildStatus();
        final String sigmaDevice = getDeviceName();
        final String isOfficial = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));

        buildStatusPref.setTitle(buildStatus);
	    buildStatusPref.setSummary(SigmaMaintainer);

        version.setText(SigmaVersion);
        device.setText(sigmaDevice);
        storText.setText(String.valueOf(SpecUtils.getTotalInternalMemorySize()) + "GB ROM + " + SpecUtils.getTotalRAM() + " RAM");
        battText.setText(SpecUtils.getBatteryCapacity(mContext) + " mAh");
        chipsetText.setText(SpecUtils.getProcessorModel());
        displayText.setText(SpecUtils.getScreenRes(mContext));
        
        //LinearLayout mbattery = holder.itemView.findViewById(mContext.getResources().
        //        getIdentifier("id/battery_info", null, mContext.getPackageName()));
        //mbattery.setClickable(true);
        //mbattery.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //        Intent intent = new Intent();
        //        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$PowerUsageSummaryActivity"));
        //        mContext.startActivity(intent);
        //    }
        //});

        //LinearLayout mStorage = holder.itemView.findViewById(mContext.getResources().
        //        getIdentifier("id/storage_info", null, mContext.getPackageName()));
        //mStorage.setClickable(true);
        //mStorage.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //        Intent intent = new Intent();
        //        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$StorageDashboardActivity"));
        //        mContext.startActivity(intent);
        //    }
        //});

        if (isOfficial.toLowerCase().equals("official")) {
		 buildStatusPref.setIcon(R.drawable.verified);
	    } else {
		buildStatusPref.setIcon(R.drawable.unverified);
	    }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SIGMA_INFO;
    }
}