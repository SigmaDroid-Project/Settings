/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.SpannedString;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.bluetooth.BluetoothLengthDeviceNameFilter;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.deviceinfo.aboutphone.DeviceCardView;
import com.android.settings.wifi.tether.WifiDeviceNameTextValidator;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;
import com.android.settingslib.widget.LayoutPreference;

import kotlin.Unit;

public class DeviceNamePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener,
        LifecycleObserver,
        OnSaveInstanceState,
        OnCreate {

    private static final String KEY_MARKET_NAME_PROP = "ro.product.marketname";
    private static final String KEY_PENDING_DEVICE_NAME = "key_pending_device_name";
    @VisibleForTesting
    static final int RES_SHOW_DEVICE_NAME_BOOL = R.bool.config_show_device_name;
    private String mDeviceName;
    protected WifiManager mWifiManager;
    private final BluetoothAdapter mBluetoothAdapter;
    private final WifiDeviceNameTextValidator mWifiDeviceNameTextValidator;
    private LayoutPreference mPreference;
    private DeviceCardView mDeviceCard;
    private DeviceNamePreferenceHost mHost;
    private String mPendingDeviceName;

    public DeviceNamePreferenceController(Context context, String key) {
        super(context, key);

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiDeviceNameTextValidator = new WifiDeviceNameTextValidator();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initializeDeviceName();
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        mDeviceCard = mPreference.findViewById(R.id.deviceNameCard);
        final CharSequence deviceName = getSummary();
        mDeviceCard.setDeviceName(deviceName.toString(), mWifiDeviceNameTextValidator.isTextValid(deviceName.toString()));
        mDeviceCard.setListener(s -> {
            setDeviceName(s);
            return Unit.INSTANCE;
        });
    }

    private void initializeDeviceName() {
        mDeviceName = Settings.Global.getString(mContext.getContentResolver(),
                Settings.Global.DEVICE_NAME);
        if (mDeviceName == null) {
            mDeviceName = SystemProperties.get(KEY_MARKET_NAME_PROP, Build.MODEL);
        }
    }

    @Override
    public CharSequence getSummary() {
        return mDeviceName;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mPendingDeviceName = (String) newValue;
        if (mHost != null) {
            mHost.showDeviceNameWarningDialog(mPendingDeviceName);
        }
        return true;
    }

    public void updateDeviceName(boolean update) {
        if (update && mPendingDeviceName != null) {
            setDeviceName(mPendingDeviceName);
        } else {
            setDeviceName(getSummary().toString());
        }
    }

    public void setHost(DeviceNamePreferenceHost host) {
        mHost = host;
    }

    private void setDeviceName(String deviceName) {
        if (mWifiDeviceNameTextValidator.isTextValid(deviceName)) {
            mDeviceName = deviceName;
            setSettingsGlobalDeviceName(deviceName);
            setBluetoothDeviceName(deviceName);
            setTetherSsidName(deviceName);
            mDeviceCard.setDeviceName(deviceName);
        }
    }

    private void setSettingsGlobalDeviceName(String deviceName) {
        Settings.Global.putString(mContext.getContentResolver(), Settings.Global.DEVICE_NAME,
                deviceName);
    }

    private void setBluetoothDeviceName(String deviceName) {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.setName(getFilteredBluetoothString(deviceName));
        }
    }

    /**
     * Using a UTF8ByteLengthFilter, we can filter a string to be compliant with the Bluetooth spec.
     * For more information, see {@link com.android.settings.bluetooth.BluetoothNameDialogFragment}.
     */
    private static final String getFilteredBluetoothString(final String deviceName) {
        CharSequence filteredSequence = new BluetoothLengthDeviceNameFilter().filter(deviceName, 0,
                deviceName.length(),
                new SpannedString(""),
                0, 0);
        // null -> use the original
        if (filteredSequence == null) {
            return deviceName;
        }
        return filteredSequence.toString();
    }

    private void setTetherSsidName(String deviceName) {
        final SoftApConfiguration config = mWifiManager.getSoftApConfiguration();
        // TODO: If tether is running, turn off the AP and restart it after setting config.
        mWifiManager.setSoftApConfiguration(
                new SoftApConfiguration.Builder(config).setSsid(deviceName).build());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPendingDeviceName = savedInstanceState.getString(KEY_PENDING_DEVICE_NAME, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_PENDING_DEVICE_NAME, mPendingDeviceName);
    }

    public interface DeviceNamePreferenceHost {
        void showDeviceNameWarningDialog(String deviceName);
    }
}
