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

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.notification.LockScreenNotificationPreferenceController;
import com.android.settingslib.Utils;

public class TopMenuDisplay extends Preference {

    private UiModeManager mUiModeManager;
    private PowerManager mPowerManager;

    public TopMenuDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.top_level_display);
        mUiModeManager = context.getSystemService(UiModeManager.class);
        mPowerManager = context.getSystemService(PowerManager.class);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
    	Context context = getContext();

        LinearLayout mDisplay = (LinearLayout) holder.findViewById(R.id.top_level_display);
        mDisplay.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.DISPLAY_SETTINGS");
            context.startActivity(intent);
        });

        LinearLayout mDarkMode = (LinearLayout) holder.findViewById(R.id.dark_mode);
        mDarkMode.setOnClickListener(v -> {
            boolean click = isChecked() ? false : true;
            mUiModeManager.setNightModeActivated(click);
        });
        TextView title = (TextView) holder.findViewById(R.id.dark_mode_title);
        TextView summary = (TextView) holder.findViewById(R.id.dark_mode_summary);
        summary.setText(context.getText(isChecked() ? R.string.switch_on_text
                : R.string.switch_off_text));
        if (isChecked()) {
        	mDarkMode.getBackground().setTint(colorTint(false));
            summary.setTextColor(colorTint(true));
            title.setTextColor(colorTint(true));
        }

        LinearLayout mLockScreens = (LinearLayout) holder.findViewById(R.id.lockscreen);
        mLockScreens.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.LOCK_SCREEN_SETTINGS");
            context.startActivity(intent);
        });
        TextView mLockScreensSummary = (TextView) holder.findViewById(R.id.lockscreen_summary);
        mLockScreensSummary.setText(context.getText(
                LockScreenNotificationPreferenceController.getSummaryResource(context)));

        LinearLayout textFrame = (LinearLayout) holder.findViewById(R.id.text_frame);
        LinearLayout conversationsFrame = (LinearLayout) holder.findViewById(R.id.dark_text_frame);
        LinearLayout bubbleFrame = (LinearLayout) holder.findViewById(R.id.lockscreen_text_frame);
        textFrame.setSelected(true);
        conversationsFrame.setSelected(true);
        bubbleFrame.setSelected(true);
    }

    private int colorTint(boolean isText) {
    	return isText ? Utils.getColorAttrDefaultColor(getContext(), com.android.internal.R.attr.colorSurfaceHeader)
                 : Utils.getColorAttrDefaultColor(getContext(), android.R.attr.colorAccent);
    }

    public boolean isChecked() {
        return (getContext().getResources().getConfiguration().uiMode
                 & Configuration.UI_MODE_NIGHT_YES) != 0;
    }
}
