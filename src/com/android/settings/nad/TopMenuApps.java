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

import android.app.Application;
import android.app.role.RoleManager;
import android.app.usage.UsageStats;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.icu.text.ListFormatter;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;
import androidx.core.text.BidiFormatter;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.applications.*;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.utils.StringUtil;
import com.android.settingslib.widget.AppPreference;

import java.util.ArrayList;
import java.util.List;

public class TopMenuApps extends Preference {

    private View mRootView;
    private TextView mAllAppTitle;
    public static final int SHOW_RECENT_APP_COUNT = 3;
    List<RecentAppStatsMixin.UsageStatsWrapper> mRecentApps;
    private RoleManager mRoleManager;

    public TopMenuApps(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.top_level_apps);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);

    	Context context = getContext();
        LinearLayout mTopMenuApps = (LinearLayout) holder.findViewById(R.id.top_level_apps);
        mTopMenuApps.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings",
                       "com.android.settings.Settings$AppDashboardActivity"));
                context.startActivity(intent);
            });

        LinearLayout mAllApps = (LinearLayout) holder.findViewById(R.id.all_apps);
        mAllApps.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings",
                       "com.android.settings.Settings$ManageApplicationsActivity"));
                context.startActivity(intent);
            });
        mAllAppTitle = (TextView) holder.findViewById(R.id.all_apps_title);
    	refreshUi(mAllAppTitle);

        LinearLayout mDefaultApps = (LinearLayout) holder.findViewById(R.id.default_apps);
        mDefaultApps.setOnClickListener(v -> {
                context.startActivity(new Intent(
                       Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
            });

        TextView mDefaultAppSummary = (TextView) holder.findViewById(R.id.default_apps_summary);
        mDefaultAppSummary.setText(getDefaultAppsSummary());

        LinearLayout textFrame = (LinearLayout) holder.findViewById(R.id.text_frame);
        LinearLayout allAppsFrame = (LinearLayout) holder.findViewById(R.id.all_apps_text_frame);
        LinearLayout defaultAppsFrame = (LinearLayout) holder.findViewById(R.id.default_apps_text_frame);
        textFrame.setSelected(true);
        allAppsFrame.setSelected(true);
        defaultAppsFrame.setSelected(true);
    }

    public CharSequence getDefaultAppsSummary() {
        final List<CharSequence> defaultAppLabels = new ArrayList<>();
        final CharSequence defaultBrowserLabel = getDefaultAppLabel(RoleManager.ROLE_BROWSER);
        if(!TextUtils.isEmpty(defaultBrowserLabel)) {
            defaultAppLabels.add(defaultBrowserLabel);
        }
        final CharSequence defaultPhoneLabel = getDefaultAppLabel(RoleManager.ROLE_DIALER);
        if(!TextUtils.isEmpty(defaultPhoneLabel)) {
            defaultAppLabels.add(defaultPhoneLabel);
        }
        final CharSequence defaultSmsLabel = getDefaultAppLabel(RoleManager.ROLE_SMS);
        if(!TextUtils.isEmpty(defaultSmsLabel)) {
            defaultAppLabels.add(defaultSmsLabel);
        }
        if (defaultAppLabels.isEmpty()) {
            return null;
        }
        return ListFormatter.getInstance().format(defaultAppLabels);
    }

    private CharSequence getDefaultAppLabel(String roleName) {
    	RoleManager roleManager = (RoleManager) getContext().getSystemService(RoleManager.class);
        final List<String> packageNames = roleManager.getRoleHolders(roleName);
        if (packageNames.isEmpty()) {
            return null;
        }
        final String packageName = packageNames.get(0);
        return BidiFormatter.getInstance().unicodeWrap(AppUtils.getApplicationLabel(
                getContext().getPackageManager(), packageName));
    }

    @VisibleForTesting
    void refreshUi(TextView title) {
        loadAllAppsCount(title);
        mRecentApps = loadRecentApps();
    }

    @VisibleForTesting
    void loadAllAppsCount(TextView title) {
        // Show total number of installed apps as See all's summary.
        new InstalledAppCounter(getContext(), InstalledAppCounter.IGNORE_INSTALL_REASON,
                getContext().getPackageManager()) {
            @Override
            protected void onCountComplete(int num) {
                if (!mRecentApps.isEmpty()) {
                    title.setText(
                            getContext().getResources().getQuantityString(R.plurals.see_all_apps_title,
                                    num, num));
                } else {
                    title.setText(getContext().getString(R.string.apps_summary, num));
                }
            }
        }.execute();
    }

    @VisibleForTesting
    List<RecentAppStatsMixin.UsageStatsWrapper> loadRecentApps() {
        final RecentAppStatsMixin recentAppStatsMixin = new RecentAppStatsMixin(getContext(),
                SHOW_RECENT_APP_COUNT);
        recentAppStatsMixin.loadDisplayableRecentApps(SHOW_RECENT_APP_COUNT);
        return recentAppStatsMixin.mRecentApps;
    }
}
