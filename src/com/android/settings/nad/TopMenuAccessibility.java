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

import android.app.settings.SettingsEnums;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.display.ColorDisplayManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.accessibility.AudioAdjustmentFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.Utils;

public class TopMenuAccessibility extends Preference {

    private final ColorDisplayManager mColorDisplayManager;
    private LinearLayout mExtraDim;
    private TextView mExtraDimTitle;
    private TextView mExtraDimSummary;

    public TopMenuAccessibility(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.top_level_accessibility);
        mColorDisplayManager = context.getSystemService(ColorDisplayManager.class);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
    	Context context = getContext();

        LinearLayout mAccessibility = (LinearLayout) holder.findViewById(R.id.accessibility);
        mAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
            context.startActivity(intent);
        });

        boolean enable = isChecked();
        boolean nightMode = isNightMode();

        mExtraDim = (LinearLayout) holder.findViewById(R.id.extra_dim);
        mExtraDimTitle = (TextView) holder.findViewById(R.id.extra_dim_title);
        mExtraDimSummary = (TextView) holder.findViewById(R.id.extra_dim_summary);
        int colorPrimary = Utils.getColorAttrDefaultColor(
                getContext(), android.R.attr.textColorPrimary);
        mExtraDim.setOnClickListener(v -> {
            boolean click = isChecked() ? false : true;
            mColorDisplayManager.setReduceBrightColorsActivated(click);
            mExtraDim.getBackground().setTint(colorTint(click ? false : true));
            if (nightMode) {
                mExtraDimTitle.setTextColor(click ? colorTint(true) : colorPrimary);
                mExtraDimSummary.setTextColor(click ? colorTint(true) : colorPrimary);
            }
            mExtraDimSummary.setText(getContext().getText(click ? R.string.switch_on_text
                    : R.string.switch_off_text));
        });
        mExtraDim.setOnLongClickListener(v -> {
            Intent intent = new Intent("android.settings.REDUCE_BRIGHT_COLORS_SETTINGS");
            context.startActivity(intent);
            return true;
        });
        mExtraDim.getBackground().setTint(colorTint(enable ? false : true));

        if (nightMode) {
        	mExtraDimTitle.setTextColor(enable ? colorTint(true) : colorPrimary);
        	mExtraDimSummary.setTextColor(enable ? colorTint(true) : colorPrimary);
        }

        mExtraDimSummary.setText(context.getText(enable ? R.string.switch_on_text
                : R.string.switch_off_text));

        LinearLayout mAudio = (LinearLayout) holder.findViewById(R.id.audio_adjustment);
        mAudio.setOnClickListener(v -> {
            new SubSettingLauncher(context)
                        .setDestination(AudioAdjustmentFragment.class.getName())
                        .setSourceMetricsCategory(SettingsEnums.ACCESSIBILITY_AUDIO_ADJUSTMENT)
                        .launch();
        });

        LinearLayout textFrame = (LinearLayout) holder.findViewById(R.id.text_frame);
        LinearLayout dimFrame = (LinearLayout) holder.findViewById(R.id.extra_dim_text_frame);
        LinearLayout audioFrame = (LinearLayout) holder.findViewById(R.id.audio_adjustment_frame);
        textFrame.setSelected(true);
        dimFrame.setSelected(true);
        audioFrame.setSelected(true);

    }

    private int colorTint(boolean isText) {
    	return Utils.getColorAttrDefaultColor(getContext(), isText ?
                 com.android.internal.R.attr.colorSurfaceHeader :
                 android.R.attr.colorAccent);
    }

    public boolean isChecked() {
        return mColorDisplayManager.isReduceBrightColorsActivated();
    }

    public boolean isNightMode() {
        return (getContext().getResources().getConfiguration().uiMode
                 & Configuration.UI_MODE_NIGHT_YES) != 0;
    }
}
