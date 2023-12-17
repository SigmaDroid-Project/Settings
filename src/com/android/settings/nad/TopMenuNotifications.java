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

import static android.provider.Settings.Secure.NOTIFICATION_BUBBLES;

import android.app.settings.SettingsEnums;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.notification.BubbleNotificationSettings;
import com.android.settings.notification.NotificationBackend;

public class TopMenuNotifications extends Preference {

    public TopMenuNotifications(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.top_level_notifications);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
    	Context context = getContext();

        LinearLayout mNotifications = (LinearLayout) holder.findViewById(R.id.notifications);
        mNotifications.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.NOTIFICATION_SETTINGS");
            context.startActivity(intent);
        });

        LinearLayout mConversations = (LinearLayout) holder.findViewById(R.id.conversation);
        mConversations.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.CONVERSATION_SETTINGS");
            context.startActivity(intent);
        });
        TextView mConversationSummary = (TextView) holder.findViewById(R.id.conversations_summary);
        final int count = new NotificationBackend().getConversations(true).getList().size();
        mConversationSummary.setText(count == 0 ? context.getText(
                R.string.priority_conversation_count_zero) :
                context.getResources().getQuantityString(
                R.plurals.priority_conversation_count,
                count, count));

        LinearLayout mBubble = (LinearLayout) holder.findViewById(R.id.bubble);
        mBubble.setOnClickListener(v -> {
            new SubSettingLauncher(context)
                        .setDestination(BubbleNotificationSettings.class.getName())
                        .setSourceMetricsCategory(SettingsEnums.BUBBLE_SETTINGS)
                        .launch();
        });
        TextView mBubbleSummary = (TextView) holder.findViewById(R.id.bubble_summary);
        mBubbleSummary.setText(context.getString(
                areBubblesEnabled()
                        ? R.string.notifications_bubble_setting_on_summary
                        : R.string.switch_off_text));

        LinearLayout textFrame = (LinearLayout) holder.findViewById(R.id.text_frame);
        LinearLayout conversationsFrame = (LinearLayout) holder.findViewById(R.id.conversations_text_frame);
        LinearLayout bubbleFrame = (LinearLayout) holder.findViewById(R.id.bubble_text_frame);
        textFrame.setSelected(true);
        conversationsFrame.setSelected(true);
        bubbleFrame.setSelected(true);
    }

    private boolean areBubblesEnabled() {
        return Settings.Secure.getInt(getContext().getContentResolver(),
                NOTIFICATION_BUBBLES, 1) == 1;
    }
}
