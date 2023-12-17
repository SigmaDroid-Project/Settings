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
import android.app.usage.StorageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.format.Formatter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.deviceinfo.StorageDashboardFragment;
import com.android.settings.deviceinfo.storage.*;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.Utils;
import com.android.settingslib.deviceinfo.PrivateStorageInfo;
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider;
import com.android.settingslib.utils.ThreadUtils;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopMenuStorage extends Preference {

    private static final String TAG = "NadTopStorageProgress";

    private static final int ANIM_DURATION = 1300;
    long mUsedBytes;
    long mTotalBytes;
    private final StorageStatsManager mStorageStatsManager;
    private StorageEntry mStorageEntry;
    boolean mIsUpdateStateFromSelectedStorageEntry;
    private StorageCacheHelper mStorageCacheHelper;
    private final Pattern mNumberPattern = Pattern.compile("[\\d]*[\\٫.,]?[\\d]+");
    private final StorageManager mStorageManager;
    private final StorageManagerVolumeProvider mStorageManagerVolumeProvider;

    public TopMenuStorage(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.top_level_storage);
        mStorageStatsManager = context.getSystemService(StorageStatsManager.class);
        mStorageCacheHelper = new StorageCacheHelper(context, UserHandle.myUserId());
        mStorageEntry =
                StorageEntry.getDefaultInternalStorageEntry(getContext());
        mStorageManager = context.getSystemService(StorageManager.class);
        mStorageManagerVolumeProvider = new StorageManagerVolumeProvider(mStorageManager);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
    	Context context = getContext();

        LinearLayout mStorage = (LinearLayout) holder.findViewById(R.id.storage);
        mStorage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$StorageDashboardActivity"));
            context.startActivity(intent);
        });
        TextView storageSummary = (TextView) holder.findViewById(R.id.storage_summary);
        ThreadUtils.postOnBackgroundThread(() -> {
            final NumberFormat percentageFormat = NumberFormat.getPercentInstance();
            final PrivateStorageInfo info = PrivateStorageInfo.getPrivateStorageInfo(
                    getStorageManagerVolumeProvider());
            final double privateUsedBytes = info.totalBytes - info.freeBytes;

            ThreadUtils.postOnMainThread(() -> {
                storageSummary.setText(context.getString(R.string.storage_summary,
                        percentageFormat.format(privateUsedBytes / info.totalBytes),
                        Formatter.formatFileSize(context, info.freeBytes)));
            });
        });

        LinearLayout mProgressBar = (LinearLayout) holder.findViewById(R.id.progressbar);
        mProgressBar.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$StorageDashboardActivity"));
            context.startActivity(intent);
        });

        final TextView usageSummary = (TextView) holder.findViewById(R.id.usage_summary);
        final TextView totalSummary = (TextView) holder.findViewById(R.id.total_summary);
        final ProgressBar progressBar = (ProgressBar) holder.findViewById(R.id.storage_progress);

        // Use cached data for both total size and used size.
        if (mStorageEntry != null && mStorageEntry.isMounted() && mStorageEntry.isPrivate()) {
            StorageCacheHelper.StorageCache cachedData = mStorageCacheHelper.retrieveCachedSize();
            mUsedBytes = cachedData.totalSize;
            mTotalBytes = cachedData.totalUsedSize;
            mIsUpdateStateFromSelectedStorageEntry = true;
            long usage = mUsedBytes; long total = mTotalBytes;
            usageSummary.setText(enlargeFontOfNumber(StorageUtils.getStorageSummary(
                    context, R.string.storage_usage_summary, usage)));
            totalSummary.setText(StorageUtils.getStorageSummary(
                    context, R.string.storage_total_summary, total));
            if (isNightMode()) {
            	int color = android.R.color.system_accent1_500;
            	usageSummary.setTextColor(context.getColor(color));
            	totalSummary.setTextColor(context.getColor(color));
            }
            final int percent = (int) (usage / (double) total * 100);
            final ValueAnimator animator = ValueAnimator.ofInt(0, percent);
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
        // Get the latest data from StorageStatsManager.
        ThreadUtils.postOnBackgroundThread(() -> {
            try {
                if (mStorageEntry == null || !mStorageEntry.isMounted()) {
                    throw new IOException();
                }

                if (mStorageEntry.isPrivate()) {
                    // StorageStatsManager can only query private storages.
                    mTotalBytes = mStorageStatsManager.getTotalBytes(mStorageEntry.getFsUuid());
                    mUsedBytes = mTotalBytes
                            - mStorageStatsManager.getFreeBytes(mStorageEntry.getFsUuid());
                } else {
                    final File rootFile = mStorageEntry.getPath();
                    if (rootFile == null) {
                        Log.d(TAG, "Mounted public storage has null root path: " + mStorageEntry);
                        throw new IOException();
                    }
                    mTotalBytes = rootFile.getTotalSpace();
                    mUsedBytes = mTotalBytes - rootFile.getFreeSpace();
                }
            } catch (IOException e) {
                // The storage device isn't present.
                mTotalBytes = 0;
                mUsedBytes = 0;
            }
            mIsUpdateStateFromSelectedStorageEntry = true;
            ThreadUtils.postOnMainThread(() -> {
                if (!mIsUpdateStateFromSelectedStorageEntry) {
                    // Returns here to avoid jank by unnecessary UI update.
                    return;
                }
                mIsUpdateStateFromSelectedStorageEntry = false;
                long usage = mUsedBytes;
                long total = mTotalBytes;
                usageSummary.setText(enlargeFontOfNumber(StorageUtils.getStorageSummary(
                        context, R.string.storage_usage_summary, usage)));
                totalSummary.setText(StorageUtils.getStorageSummary(
                        context, R.string.storage_total_summary, total));
                if (isNightMode()) {
                    int color = android.R.color.system_accent1_500;
                    usageSummary.setTextColor(context.getColor(color));
                    totalSummary.setTextColor(context.getColor(color));
                }
                final int percent = (int) (usage / (double) total * 100);
                final ValueAnimator animator = ValueAnimator.ofInt(0, percent);
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
            });
        });

        LinearLayout mFreeUp = (LinearLayout) holder.findViewById(R.id.free_up);
        mFreeUp.setOnClickListener(v -> {
            final Intent intent = new Intent(StorageManager.ACTION_MANAGE_STORAGE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivityAsUser(intent, new UserHandle(UserHandle.myUserId()));
        });

        LinearLayout textFrame = (LinearLayout) holder.findViewById(R.id.text_frame);
        LinearLayout freeUpFrame = (LinearLayout) holder.findViewById(R.id.free_up_frame);
        textFrame.setSelected(true);
        freeUpFrame.setSelected(true);
    }

    protected StorageManagerVolumeProvider getStorageManagerVolumeProvider() {
        return mStorageManagerVolumeProvider;
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

    public int colorTint() {
    	return Utils.getColorAttrDefaultColor(getContext(), android.R.attr.colorAccent);
    }

    public boolean isNightMode() {
        return (getContext().getResources().getConfiguration().uiMode
                 & Configuration.UI_MODE_NIGHT_YES) != 0;
    }
}
