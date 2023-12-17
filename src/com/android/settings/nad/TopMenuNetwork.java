/*
 * Copyright (C) 2022 The Nusantara Project
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

package com.android.settings.nad;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.BidiFormatter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.AirplaneModeEnabler;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settings.network.MobileNetworkPreferenceController;
import com.android.settings.network.SubscriptionUtil;

public class TopMenuNetwork extends Preference implements
        AirplaneModeEnabler.OnAirplaneModeChangedListener {

    private final MobileNetworkPreferenceController mMobileNetworkPreferenceController;
	private LinearLayout mAirplane;
	private TextView mAirplaneTitle;
	private TextView mAirplaneSummary;

    public TopMenuNetwork(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.top_level_network);
        mMobileNetworkPreferenceController = new MobileNetworkPreferenceController(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);

    	Context context = getContext();
        LinearLayout mInternetMenu = (LinearLayout) holder.findViewById(R.id.internet_menu);
        mInternetMenu.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$NetworkDashboardActivity"));
            context.startActivity(intent);
        });
        TextView mInternetSummary = (TextView) holder.findViewById(R.id.internet_summary);
        mInternetSummary.setText(mMobileNetworkPreferenceController.isAvailable() ?
                BidiFormatter.getInstance()
                        .unicodeWrap(context.getString(R.string.network_dashboard_summary_mobile)) :
                BidiFormatter.getInstance()
                        .unicodeWrap(context.getString(R.string.network_dashboard_summary_no_mobile)));

        int colorTint = Utils.getColorAttrDefaultColor(context, isAirplaneModeEnable() ?
                android.R.attr.colorAccent : com.android.internal.R.attr.colorSurfaceHeader);

        LinearLayout airplane = mAirplane = (LinearLayout) holder.findViewById(R.id.airplane);
        airplane.getBackground().setTint(colorTint);
        AirplaneModeEnabler airplaneModeEnabler = new AirplaneModeEnabler(context, this);
        airplane.setOnClickListener(v -> {
        	boolean click = isAirplaneModeEnable() ? false : true;
        	airplaneModeEnabler.setAirplaneMode(click);
        });
        airplane.setOnLongClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$NetworkDashboardActivity"));
            context.startActivity(intent);
            return true;
        });
        TextView airplaneTitle = mAirplaneTitle = (TextView) holder.findViewById(R.id.airplane_title);
        TextView airplaneSummary = mAirplaneSummary = (TextView) holder.findViewById(
                R.id.airplane_summary);
        airplaneSummary.setText(context.getString(isAirplaneModeEnable() ?
                R.string.switch_on_text :
                R.string.switch_off_text));
        int textTint = Utils.getColorAttrDefaultColor(context, isAirplaneModeEnable() ?
                com.android.internal.R.attr.colorSurfaceHeader : android.R.attr.textColorPrimary);
        boolean nightMode = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (nightMode) {
            airplaneTitle.setTextColor(textTint);
            airplaneSummary.setTextColor(textTint);
        }

        LinearLayout mMobile = (LinearLayout) holder.findViewById(R.id.mobile);
        mMobile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$MobileNetworkActivity"));
            context.startActivity(intent);
        });

        TextView provider = (TextView) holder.findViewById(R.id.mobile_title);
        provider.setText(context.getString(R.string.mobile_data_settings_title));
        TextView providerSummary = (TextView) holder.findViewById(R.id.mobile_summary);
        final SubscriptionManager subscriptionManager =
                context.getSystemService(SubscriptionManager.class);
        SubscriptionInfo subInfo = subscriptionManager.getDefaultDataSubscriptionInfo();
        boolean available = subscriptionManager != null && subInfo != null;
        if (available) {
            providerSummary.setText(SubscriptionUtil.getUniqueSubscriptionDisplayName(
                    subInfo, context));
        }

        LinearLayout textFrame = (LinearLayout) holder.findViewById(R.id.text_frame);
        LinearLayout airplaneFrame = (LinearLayout) holder.findViewById(R.id.airplane_text_frame);
        LinearLayout mobileFrame = (LinearLayout) holder.findViewById(R.id.mobile_text_frame);
        textFrame.setSelected(true);
        airplaneFrame.setSelected(true);
        mobileFrame.setSelected(true);
    }

    public boolean isAirplaneModeEnable() {
    	return Settings.Global.getInt(getContext().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    @Override
    public void onAirplaneModeChanged(boolean isAirplaneModeOn) {
        int colorTint = Utils.getColorAttrDefaultColor(getContext(), isAirplaneModeEnable() ?
                android.R.attr.colorAccent : com.android.internal.R.attr.colorSurfaceHeader);
        int textTint = Utils.getColorAttrDefaultColor(getContext(), isAirplaneModeEnable() ?
                com.android.internal.R.attr.colorSurfaceHeader : android.R.attr.textColorPrimary);
        boolean nightMode = (getContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (mAirplane != null) mAirplane.getBackground().setTint(colorTint);
        if (mAirplaneTitle != null && nightMode) mAirplaneTitle.setTextColor(textTint);
        if (mAirplaneSummary != null) {
            mAirplaneSummary.setText(getContext().getString(isAirplaneModeEnable() ?
                    R.string.switch_on_text :
                    R.string.switch_off_text));
        	if (nightMode) {
        	    mAirplaneSummary.setTextColor(textTint);
        	}
        }
    }
}
