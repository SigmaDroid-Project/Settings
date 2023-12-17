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

import android.animation.ValueAnimator;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.*;
import com.android.settings.fuelgauge.batteryusage.*;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.Utils;
import com.android.settingslib.utils.ThreadUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopMenuBattery extends Preference implements
        BatteryPreferenceController {

    private static final String TAG = "TopMenuBattery";
    private static final String ARG_BATTERY_TIP = "battery_tip";
    private static final int ANIM_DURATION = 1300;
    private static final int BATTERY_MAX_LEVEL = 100;
    private final Pattern mNumberPattern = Pattern.compile("[\\d]*[\\٫.,]?[\\d]+");

    private TextView mTopBatterySummary;
    private TextView mUsageSummary;
    private ProgressBar mProgressBar;
    private CharSequence mTextUsageSummary;
    private CharSequence mTextBatterySummary;
    private String mBatteryStatusLabel;
    private int mPercent = -1;

    public TopMenuBattery(Context context) {
    	this(context, null);
    }

    public TopMenuBattery(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.top_level_battery);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
    	Context context = getContext();

        LinearLayout mTopBattery = (LinearLayout) holder.findViewById(R.id.top_level_battery);
        mTopBattery.setOnClickListener(v -> {
            Intent intent = new Intent("android.intent.action.POWER_USAGE_SUMMARY");
            context.startActivity(intent);
        });
        mTopBatterySummary = (TextView) holder.findViewById(R.id.battery_summary);
        mTopBatterySummary.setText(mTextBatterySummary);

        LinearLayout mBatteryProgressBar = (LinearLayout) holder.findViewById(R.id.battery_progress);
        mBatteryProgressBar.setOnClickListener(v -> {
            Intent intent = new Intent("android.intent.action.POWER_USAGE_SUMMARY");
            context.startActivity(intent);
        });

        if (com.android.settings.Utils.isBatteryPresent(context)) {
            quickUpdateBattery();
        } else {
            mBatteryProgressBar.removeAllViews();
        }

        mUsageSummary = (TextView) holder.findViewById(R.id.usage_summary);
        mUsageSummary.setText(enlargeFontOfNumber(mTextUsageSummary));
        if (isNightMode()) mUsageSummary.setTextColor(context.getColor(
                android.R.color.system_accent1_500));

        ProgressBar progressBar = mProgressBar = (ProgressBar) holder.findViewById(R.id.progress);
        ValueAnimator animator = ValueAnimator.ofInt(0, mPercent);
        if (mPercent < 0) {
            progressBar.setIndeterminate(true);
        } else {
            progressBar.setIndeterminate(false);
            animator.setDuration(ANIM_DURATION);
            animator.addUpdateListener(animation -> {
                int animProgress = (Integer) animation.getAnimatedValue();
                progressBar.setProgress(animProgress);
                progressBar.setProgressTintList(ColorStateList.valueOf(
                        colorTint()));
            });
            animator.start();
        }

        LinearLayout mBatteryUsage = (LinearLayout) holder.findViewById(R.id.battery_usage);
        mBatteryUsage.setOnClickListener(v -> {
                new SubSettingLauncher(context)
                        .setDestination(PowerUsageAdvanced.class.getName())
                        .setSourceMetricsCategory(SettingsEnums.FUELGAUGE_BATTERY_HISTORY_DETAIL)
                        .launch();
        });
        TextView mBatteryUsageSummary = (TextView) holder.findViewById(R.id.battery_usage_summary);
        mBatteryUsageSummary.setText(context.getString(R.string.advanced_battery_preference_summary));

        LinearLayout textFrame = (LinearLayout) holder.findViewById(R.id.text_frame);
        LinearLayout bubbleFrame = (LinearLayout) holder.findViewById(R.id.battery_usage_frame);
        textFrame.setSelected(true);
        bubbleFrame.setSelected(true);

    }

    private CharSequence enlargeFontOfNumber(CharSequence summary) {
        if (TextUtils.isEmpty(summary)) {
            return "";
        }

        final Matcher matcher = mNumberPattern.matcher(summary);
        if (matcher.find()) {
            final SpannableString spannableSummary =  new SpannableString(summary);
            spannableSummary.setSpan(new AbsoluteSizeSpan(44, true /* dip */), matcher.start(),
                    matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableSummary;
        }
        return summary;
    }

    public void quickUpdateBattery() {
    	Context context = getContext();
        BatteryInfo info = BatteryUtils.getInstance(context).getBatteryInfo(TAG);
        BatteryStatusFeatureProvider status = FeatureFactory.getFactory(context)
                .getBatteryStatusFeatureProvider(context);
        if (info == null) return;
        final boolean triggerBatteryStatusUpdate =
                status.triggerBatteryStatusUpdate(this, info);
        if (!triggerBatteryStatusUpdate) {
            mBatteryStatusLabel = null; // will generateLabel()
        }
        setBatterySummary(
                (mBatteryStatusLabel == null) ? generateLabel(info) : mBatteryStatusLabel);
        CharSequence usageSummary =
        formatBatteryPercentageText(info.batteryLevel);
        setUsageSummary(usageSummary);
        setPercent(info.batteryLevel, BATTERY_MAX_LEVEL);
    }

    /**
     * Callback which receives text for the label.
     */
    public void updateBatteryStatus(String label, BatteryInfo info) {
        mBatteryStatusLabel = label; // Null if adaptive charging is not active

        // Do not triggerBatteryStatusUpdate(), otherwise there will be an infinite loop
        setBatterySummary(
                (label == null) ? generateLabel(info) : label);
        CharSequence usageSummary =
                formatBatteryPercentageText(info.batteryLevel);
        setUsageSummary(usageSummary);
        setPercent(info.batteryLevel, BATTERY_MAX_LEVEL);
    }

    private CharSequence formatBatteryPercentageText(int batteryLevel) {
        return TextUtils.expandTemplate(getContext().getText(R.string.battery_header_title_alternate),
                NumberFormat.getIntegerInstance().format(batteryLevel));
    }

    private CharSequence generateLabel(BatteryInfo info) {
    	Context context = getContext();
        Bundle bundle = new Bundle();
        BatteryTip mBatteryTip = bundle.getParcelable(ARG_BATTERY_TIP);
        PowerManager mPowerManager = context.getSystemService(PowerManager.class);
        if (BatteryUtils.isBatteryDefenderOn(info)) {
            return null;
        } else if (info.remainingLabel == null
                || info.batteryStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            // Present status only if no remaining time or status anomalous
            return info.statusLabel;
        } else if (info.statusLabel != null && !info.discharging) {
            // Charging state
            return context.getString(
                    R.string.battery_state_and_duration, info.statusLabel, info.remainingLabel);
        } else if (mPowerManager.isPowerSaveMode()) {
            // Power save mode is on
            final String powerSaverOn = context.getString(
                    R.string.battery_tip_early_heads_up_done_title);
            return getContext().getString(
                    R.string.battery_state_and_duration, powerSaverOn, info.remainingLabel);
        } else if (mBatteryTip != null
                && mBatteryTip.getType() == BatteryTip.TipType.LOW_BATTERY) {
            // Low battery state
            final String lowBattery = context.getString(R.string.low_battery_summary);
            return context.getString(
                    R.string.battery_state_and_duration, lowBattery, info.remainingLabel);
        } else {
            // Discharging state
            return info.remainingLabel;
        }
    }

    /** Set usage summary, number in the summary will show with enlarged font size. */
    public void setUsageSummary(CharSequence usageSummary) {
        if (TextUtils.equals(mTextUsageSummary, usageSummary)) {
            return;
        }
        mTextUsageSummary = usageSummary;
        if (mUsageSummary != null && usageSummary != null)
                mUsageSummary.setText(enlargeFontOfNumber(usageSummary));
    }

    /** Set battery summary. */
    public void setBatterySummary(CharSequence batterySummary) {
        if (TextUtils.equals(mTextBatterySummary, batterySummary)) {
            return;
        }
        mTextBatterySummary = batterySummary;
        if (mTopBatterySummary != null && batterySummary != null)
                mTopBatterySummary.setText(batterySummary);
    }

    /** Set percentage of the progress bar. */
    public void setPercent(long usage, long total) {
        if (usage >  total) {
            return;
        }
        if (total == 0L) {
            if (mPercent != 0) {
                mPercent = 0;
                notifyChanged();
            }
            return;
        }
        final int percent = (int) (usage / (double) total * 100);
        if (mPercent == percent) {
            return;
        }
        mPercent = percent;
        ProgressBar progressBar = mProgressBar;
        ValueAnimator animator = ValueAnimator.ofInt(0, percent);
        if (progressBar == null || animator == null) return;
        if (percent < 0) {
            progressBar.setIndeterminate(true);
        } else {
            progressBar.setIndeterminate(false);
            animator.setDuration(ANIM_DURATION);
            animator.addUpdateListener(animation -> {
                int animProgress = (Integer) animation.getAnimatedValue();
                progressBar.setProgress(animProgress);
                progressBar.setProgressTintList(ColorStateList.valueOf(
                        colorTint()));
            });
            animator.start();
        }
    }

    public int colorTint() {
    	return Utils.getColorAttrDefaultColor(getContext(), android.R.attr.colorAccent);
    }

    public boolean isNightMode() {
        return (getContext().getResources().getConfiguration().uiMode
                 & Configuration.UI_MODE_NIGHT_YES) != 0;
    }
}
