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

import android.bluetooth.BluetoothAdapter;
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
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class TopMenuConnectedDevices extends Preference
        implements BluetoothCallback, CachedBluetoothDevice.Callback {

    private BluetoothMenuController mBluetoothMenuController;
    private BluetoothAdapter mBluetoothAdapter;
    private LocalBluetoothManager mLocalBluetoothManager;
	private boolean mIsBluetoothOn;
	private int mState;

    public TopMenuConnectedDevices(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.top_level_connected_devices);
        LocalBluetoothManager localBluetoothManager = mLocalBluetoothManager =
                LocalBluetoothManager.getInstance(getContext(), /* onInitCallback= */ null);
        if (localBluetoothManager != null) {
            localBluetoothManager.getEventManager().registerCallback(this);
            handleStateChanged(
                    localBluetoothManager.getBluetoothAdapter().getBluetoothState());
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);

        final Context context = getContext();
        LinearLayout mConnectedDevices = (LinearLayout) holder.findViewById(R.id.connected_devices_menu);
        mConnectedDevices.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$ConnectedDeviceDashboardActivity"));
            context.startActivity(intent);
        });

        int colorTint = Utils.getColorAttrDefaultColor(context, isChecked() ?
                android.R.attr.colorAccent : com.android.internal.R.attr.colorSurfaceHeader);
        int textTint = Utils.getColorAttrDefaultColor(context, isChecked() ?
                com.android.internal.R.attr.colorSurfaceHeader : android.R.attr.textColorPrimary);
        LinearLayout mBluetoothMenu = (LinearLayout) holder.findViewById(R.id.bluetooth_menu);
        mBluetoothMenu.getBackground().setTint(colorTint);
        TextView title = (TextView) holder.findViewById(R.id.bluetooth_title);
        TextView summary = (TextView) holder.findViewById(R.id.bluetooth_summary);
        boolean nightMode = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (nightMode) {
            summary.setTextColor(textTint);
            title.setTextColor(textTint);
        }
        summary.setText(context.getString(isChecked() ? R.string.bluetooth_setting_on :
                R.string.bluetooth_setting_off));
        mBluetoothMenuController = new BluetoothMenuController(mBluetoothMenu, title, summary);
        mBluetoothMenu.setOnClickListener(v -> {
            boolean click = isChecked() ? false : true;
            mLocalBluetoothManager.getBluetoothAdapter()
                   .setBluetoothEnabled(click);
        });

        mBluetoothMenu.setOnLongClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$BlueToothPairingActivity"));
            context.startActivity(intent);
            return true;
        });

        LinearLayout mPrefMenu = (LinearLayout) holder.findViewById(R.id.pref_menu);
        mPrefMenu.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$AdvancedConnectedDeviceActivity"));
            context.startActivity(intent);
        });

        LinearLayout textFrame = (LinearLayout) holder.findViewById(R.id.text_frame);
        LinearLayout bluetoothFrame = (LinearLayout) holder.findViewById(R.id.bluetooth_text_frame);
        LinearLayout prefFrame = (LinearLayout) holder.findViewById(R.id.pref_text_frame);
        textFrame.setSelected(true);
        bluetoothFrame.setSelected(true);
        prefFrame.setSelected(true);
    }

    void handleStateChanged(int state) {
    	mState = state;
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                setChecked(true);
                break;
            case BluetoothAdapter.STATE_ON:
                setChecked(true);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                setChecked(false);
                break;
            case BluetoothAdapter.STATE_OFF:
                setChecked(false);
                break;
        }
    }

    public void setChecked(boolean isChecked) {
        mIsBluetoothOn = isChecked;
        update(isChecked());
    }

    public boolean isChecked() {
        return mIsBluetoothOn;
    }

    public void update(boolean enable) {
    	if (mBluetoothAdapter == null) return;
    	mBluetoothMenuController.update(getContext(), enable);
    }

    @Override
    public void onDeviceAttributesChanged() {
        handleStateChanged(mState);
    }

    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        handleStateChanged(bluetoothState);
        if (mState != bluetoothState) mState = bluetoothState;
    }

    private class BluetoothMenuController {
        private LinearLayout mLayout;
        private TextView mTitle;
        private TextView mSummary;

        BluetoothMenuController(LinearLayout layout, TextView title, TextView summary) {
            mLayout = layout;
            mTitle = title;
            mSummary = summary;
        }

        void update(Context ctx, boolean enable) {
            int colorTint = Utils.getColorAttrDefaultColor(ctx, enable ?
                    android.R.attr.colorAccent : com.android.internal.R.attr.colorSurfaceHeader);
            int textTint = Utils.getColorAttrDefaultColor(ctx, enable ?
                    com.android.internal.R.attr.colorSurfaceHeader : android.R.attr.textColorPrimary);
            boolean nightMode = (ctx.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            mLayout.getBackground().setTint(colorTint);
            mSummary.setText(ctx.getString(enable ?
                    R.string.bluetooth_setting_on :
                    R.string.bluetooth_setting_off));
            if (nightMode) {
                mSummary.setTextColor(textTint);
                mTitle.setTextColor(textTint);
            }
        }
    }
}
