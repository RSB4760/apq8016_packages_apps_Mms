/*
 * Copyright (C) 2010-2014, The Linux Foundation. All rights reserved.
 * Not a Contribution.
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import java.util.HashMap;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Conversations;
import android.provider.Telephony.Threads;
import android.provider.Telephony.TextBasedSmsColumns;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.rcs.RcsNotificationMessageListItem;
import com.google.android.mms.MmsException;

import com.suntek.mway.rcs.client.aidl.common.RcsColumns;
import com.suntek.mway.rcs.client.aidl.constant.Constants;

/**
 * The back-end data adapter of a message list.
 */
public class MessageListAdapter extends CursorAdapter {
    private static final String TAG = LogTag.TAG;
    private static final boolean LOCAL_LOGV = false;

    static final String[] PROJECTION_DEFAULT = new String[] {
        // TODO: should move this symbol into com.android.mms.telephony.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.SUBSCRIPTION_ID,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        Mms.STATUS,
        Mms.TEXT_ONLY,
        Mms.SUBSCRIPTION_ID,
        Threads.RECIPIENT_IDS  // add for obtaining address of MMS
    };

    public static final String[] MAILBOX_PROJECTION_DEFAULT = new String[] {
        // TODO: should move this symbol into android.provider.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.SUBSCRIPTION_ID,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        Mms.STATUS,
        Mms.TEXT_ONLY,
        Mms.SUBSCRIPTION_ID,   // add for DSDS
        Threads.RECIPIENT_IDS  // add for obtaining address of MMS
    };

    static final String[] PROJECTION_RCS = new String[] {
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.SUBSCRIPTION_ID,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        Mms.STATUS,
        Mms.TEXT_ONLY,
        Mms.SUBSCRIPTION_ID,
        Threads.RECIPIENT_IDS,  // add for obtaining address of MMS
        RcsColumns.SmsRcsColumns.RCS_FILENAME,
        RcsColumns.SmsRcsColumns.RCS_THUMB_PATH,
        RcsColumns.SmsRcsColumns.RCS_MSG_TYPE,
        RcsColumns.SmsRcsColumns.RCS_BURN,
        RcsColumns.SmsRcsColumns.RCS_IS_DOWNLOAD,
        RcsColumns.SmsRcsColumns.RCS_MSG_STATE,
        RcsColumns.SmsRcsColumns.RCS_MIME_TYPE,
        RcsColumns.SmsRcsColumns.RCS_FAVOURITE,
        RcsColumns.SmsRcsColumns.RCS_FILE_SIZE,
        RcsColumns.SmsRcsColumns.RCS_MESSAGE_ID,
        RcsColumns.SmsRcsColumns.RCS_CHAT_TYPE
    };

    public static final String[] MAILBOX_PROJECTION_RCS = new String[] {
        // TODO: should move this symbol into android.provider.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.SUBSCRIPTION_ID,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        Mms.STATUS,
        Mms.TEXT_ONLY,
        Mms.SUBSCRIPTION_ID,
        Threads.RECIPIENT_IDS,  // add for obtaining address of MMS
        RcsColumns.SmsRcsColumns.RCS_FILENAME,
        RcsColumns.SmsRcsColumns.RCS_THUMB_PATH,
        RcsColumns.SmsRcsColumns.RCS_MSG_TYPE,
        RcsColumns.SmsRcsColumns.RCS_BURN,
        RcsColumns.SmsRcsColumns.RCS_IS_DOWNLOAD,
        RcsColumns.SmsRcsColumns.RCS_MSG_STATE,
        RcsColumns.SmsRcsColumns.RCS_MIME_TYPE,
        RcsColumns.SmsRcsColumns.RCS_FAVOURITE,
        RcsColumns.SmsRcsColumns.RCS_FILE_SIZE,
        RcsColumns.SmsRcsColumns.RCS_MESSAGE_ID,
        RcsColumns.SmsRcsColumns.RCS_CHAT_TYPE
    };

    static final String[] PROJECTION= MmsConfig.isRcsVersion() ?
            PROJECTION_RCS : PROJECTION_DEFAULT;

    static final String[] MAILBOX_PROJECTION= MmsConfig.isRcsVersion() ?
            MAILBOX_PROJECTION_RCS : MAILBOX_PROJECTION_DEFAULT;

    static final String[] FORWARD_PROJECTION = new String[] {
        "'sms' AS " + MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        Sms.ADDRESS,
        Sms.BODY,
        Sms.SUBSCRIPTION_ID,
        RcsColumns.SmsRcsColumns.RCS_FILENAME,
        RcsColumns.SmsRcsColumns.RCS_THUMB_PATH,
        RcsColumns.SmsRcsColumns.RCS_MSG_TYPE,
        RcsColumns.SmsRcsColumns.RCS_BURN,
        RcsColumns.SmsRcsColumns.RCS_IS_DOWNLOAD,
        RcsColumns.SmsRcsColumns.RCS_MSG_STATE,
        RcsColumns.SmsRcsColumns.RCS_MIME_TYPE,
        RcsColumns.SmsRcsColumns.RCS_FAVOURITE,
        RcsColumns.SmsRcsColumns.RCS_FILE_SIZE,
        RcsColumns.SmsRcsColumns.RCS_MESSAGE_ID,
        RcsColumns.SmsRcsColumns.RCS_CHAT_TYPE,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE
    };

    // The indexes of the default columns which must be consistent
    // with above PROJECTION.
    public static final int COLUMN_MSG_TYPE            = 0;
    public static final int COLUMN_ID                  = 1;
    public static final int COLUMN_THREAD_ID           = 2;
    public static final int COLUMN_SMS_ADDRESS         = 3;
    public static final int COLUMN_SMS_BODY            = 4;
    static final int COLUMN_SUB_ID              = 5;
    public static final int COLUMN_SMS_DATE            = 6;
    public static final int COLUMN_SMS_DATE_SENT       = 7;
    public static final int COLUMN_SMS_READ            = 8;
    public static final int COLUMN_SMS_TYPE            = 9;
    public static final int COLUMN_SMS_STATUS          = 10;
    public static final int COLUMN_SMS_LOCKED          = 11;
    public static final int COLUMN_SMS_ERROR_CODE      = 12;
    public static final int COLUMN_MMS_SUBJECT         = 13;
    public static final int COLUMN_MMS_SUBJECT_CHARSET = 14;
    public static final int COLUMN_MMS_DATE            = 15;
    public static final int COLUMN_MMS_DATE_SENT       = 16;
    public static final int COLUMN_MMS_READ            = 17;
    public static final int COLUMN_MMS_MESSAGE_TYPE    = 18;
    public static final int COLUMN_MMS_MESSAGE_BOX     = 19;
    public static final int COLUMN_MMS_DELIVERY_REPORT = 20;
    public static final int COLUMN_MMS_READ_REPORT     = 21;
    public static final int COLUMN_MMS_ERROR_TYPE      = 22;
    public static final int COLUMN_MMS_LOCKED          = 23;
    public static final int COLUMN_MMS_STATUS          = 24;
    public static final int COLUMN_MMS_TEXT_ONLY       = 25;
    public static final int COLUMN_MMS_SUB_ID          = 26;
    public static final int COLUMN_RECIPIENT_IDS       = 27;
    public static final int COLUMN_RCS_PATH            = 28;
    public static final int COLUMN_RCS_THUMB_PATH      = 29;
    public static final int COLUMN_RCS_MSG_TYPE        = 30;
    public static final int COLUMN_RCS_BURN            = 31;
    public static final int COLUMN_RCS_IS_DOWNLOAD     = 32;
    public static final int COLUMN_RCS_MSG_STATE       = 33;
    public static final int COLUMN_RCS_MIME_TYPE       = 34;
    public static final int COLUMN_FAVOURITE           = 35;
    public static final int COLUMN_RCS_FILESIZE        = 36;
    public static final int COLUMN_RCS_MESSAGE_ID      = 37;
    public static final int COLUMN_RCS_CHAT_TYPE       = 38;

    private static final int CACHE_SIZE         = 50;

    public static final int INCOMING_ITEM_TYPE_SMS = 0;
    public static final int OUTGOING_ITEM_TYPE_SMS = 1;
    public static final int INCOMING_ITEM_TYPE_MMS = 2;
    public static final int OUTGOING_ITEM_TYPE_MMS = 3;
    public static final int GROUP_CHAT_ITEM_TYPE = 4;

    protected LayoutInflater mInflater;
    private final ListView mListView;
    private final MessageItemCache mMessageItemCache;
    private final ColumnsMap mColumnsMap;
    private OnDataSetChangedListener mOnDataSetChangedListener;
    private Handler mMsgListItemHandler;
    private Pattern mHighlight;
    private Context mContext;
    private boolean mIsGroupConversation;
    private boolean mMultiChoiceMode = false;
    private boolean mIsMsimIccCardActived = false;
    // for multi delete sim messages or forward merged message
    private int mMultiManageMode = MessageUtils.INVALID_MODE;

    private float mTextSize = 0;

    /* Begin add for RCS */

    private long mGroupId;
    private HashMap<Integer, String> mBodyCache;

    private boolean mRcsIsStopDown = false;

    public void setRcsIsStopDown(boolean rcsIsStopDown){
        this.mRcsIsStopDown = rcsIsStopDown;
    }
    /* End add for RCS */

    public MessageListAdapter(
            Context context, Cursor c, ListView listView,
            boolean useDefaultColumnsMap, Pattern highlight) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        mContext = context;
        mHighlight = highlight;

        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mMessageItemCache = new MessageItemCache(CACHE_SIZE);
        mListView = listView;

        if (useDefaultColumnsMap) {
            mColumnsMap = new ColumnsMap();
        } else {
            mColumnsMap = new ColumnsMap(c);
        }

        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if (view instanceof MessageListItem) {
                    MessageListItem mli = (MessageListItem) view;
                    // Clear references to resources
                    mli.unbind();
                }
            }
        });
    }

    public void setIsMsimIccCardActived(boolean isActived) {
        mIsMsimIccCardActived = isActived;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof MessageListItem) {
            if (mListView.isItemChecked(cursor.getPosition())) {
                if (view != null) {
                    ((MessageListItem) view).markAsSelected(true);
                }
            } else {
                ((MessageListItem) view).markAsSelected(false);
            }
            String type = cursor.getString(mColumnsMap.mColumnMsgType);
            long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);

            MessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
            if (msgItem != null) {
                MessageListItem mli = (MessageListItem) view;
                int position = cursor.getPosition();
                if (mMultiManageMode != MessageUtils.INVALID_MODE) {
                    mli.setManageSelectMode(mMultiManageMode);
                }
                mli.setIsMsimIccCardActived(mIsMsimIccCardActived);
                mli.bind(msgItem, mIsGroupConversation, position, mGroupId);
                mli.setMsgListItemHandler(mMsgListItemHandler);
            }
        } else if (view instanceof RcsNotificationMessageListItem) {
            ((RcsNotificationMessageListItem) view).bind(cursor, mColumnsMap);
        }
    }

    @Override
    public long getItemId(int position) {
        if (getCursor() != null) {
            getCursor().moveToPosition(position);
            return position;
        }
        return 0;
    }

    public interface OnDataSetChangedListener {
        void onDataSetChanged(MessageListAdapter adapter);
        void onContentChanged(MessageListAdapter adapter);
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        mOnDataSetChangedListener = l;
    }

    public void setMsgListItemHandler(Handler handler) {
        mMsgListItemHandler = handler;
    }

    public void setIsGroupConversation(boolean isGroup) {
        mIsGroupConversation = isGroup;
    }
    public void setRcsGroupId(long groupId) {
        mGroupId = groupId;
    }

    public void cancelBackgroundLoading() {
        mMessageItemCache.evictAll();   // causes entryRemoved to be called for each MessageItem
                                        // in the cache which causes us to cancel loading of
                                        // background pdu's and images.
    }

    public void setMultiChoiceMode(boolean isMultiChoiceMode) {
        mMultiChoiceMode = isMultiChoiceMode;
    }

    public void setMultiManageMode(int manageMode) {
        mMultiManageMode = manageMode;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (LOCAL_LOGV) {
            Log.v(TAG, "MessageListAdapter.notifyDataSetChanged().");
        }

        mMessageItemCache.evictAll();

        if (mOnDataSetChangedListener != null) {
            mOnDataSetChangedListener.onDataSetChanged(this);
        }
    }

    @Override
    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed()) {
            if (mOnDataSetChangedListener != null) {
                mOnDataSetChangedListener.onContentChanged(this);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int boxType = getItemViewType(cursor);
        View view;
        if (MmsConfig.isRcsVersion() && boxType == GROUP_CHAT_ITEM_TYPE) {
            view = mInflater.inflate(R.layout.rcs_message_item_group_chat_notification, parent,
                    false);
        } else {
            if (mMultiChoiceMode) {
                view = mInflater.inflate((boxType == INCOMING_ITEM_TYPE_SMS ||
                        boxType == INCOMING_ITEM_TYPE_MMS) ?
                                R.layout.message_list_multi_recv : R.layout.message_list_multi_send,
                        parent, false);
            } else {
                view = mInflater.inflate((boxType == INCOMING_ITEM_TYPE_SMS ||
                        boxType == INCOMING_ITEM_TYPE_MMS) ?
                                R.layout.message_list_item_recv : R.layout.message_list_item_send,
                        parent, false);
            }
            if (boxType == INCOMING_ITEM_TYPE_MMS || boxType == OUTGOING_ITEM_TYPE_MMS) {
                // We've got an mms item, pre-inflate the mms portion of the view
                view.findViewById(R.id.mms_layout_view_stub).setVisibility(View.VISIBLE);
            }
        }
        return view;
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        MessageItem item = mMessageItemCache.get(getKey(type, msgId));
        if (item == null && c != null && isCursorValid(c)) {
            try {
                item = new MessageItem(mContext, type, c, mColumnsMap, mHighlight);
                mMessageItemCache.put(getKey(item.mType, item.mMsgId), item);
            } catch (MmsException e) {
                Log.e(TAG, "getCachedMessageItem: ", e);
            }
        }
        return item;
    }

    private boolean isCursorValid(Cursor cursor) {
        // Check whether the cursor is valid or not.
        if (cursor == null || cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return false;
        }
        return true;
    }

    private static long getKey(String type, long id) {
        if (type.equals("mms")) {
            return -id;
        } else {
            return id;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /* MessageListAdapter says that it contains four types of views. Really, it just contains
     * a single type, a MessageListItem. Depending upon whether the message is an incoming or
     * outgoing message, the avatar and text and other items are laid out either left or right
     * justified. That works fine for everything but the message text. When views are recycled,
     * there's a greater than zero chance that the right-justified text on outgoing messages
     * will remain left-justified. The best solution at this point is to tell the adapter we've
     * got two different types of views. That way we won't recycle views between the two types.
     * @see android.widget.BaseAdapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        return 5;   // Incoming and outgoing messages, both sms and mms,
                    // and Rcs Group Chat notification.
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor)getItem(position);
        return getItemViewType(cursor);
    }

    private int getItemViewType(Cursor cursor) {
        if (MmsConfig.isRcsVersion()) {
            int rcsMsgType = cursor.getInt(mColumnsMap.mColumnRcsMsgType);
            // RCS Group chat notification message.
            if (rcsMsgType == Constants.MessageConstants.CONST_MESSAGE_NOTIFICATION) {
                return GROUP_CHAT_ITEM_TYPE;
            }
        }
        String type = cursor.getString(mColumnsMap.mColumnMsgType);
        int boxId;
        if ("sms".equals(type)) {
            boxId = cursor.getInt(mColumnsMap.mColumnSmsType);
            // Note that messages from the SIM card all have a boxId of zero.
            return (boxId == TextBasedSmsColumns.MESSAGE_TYPE_INBOX ||
                    boxId == TextBasedSmsColumns.MESSAGE_TYPE_ALL) ?
                    INCOMING_ITEM_TYPE_SMS : OUTGOING_ITEM_TYPE_SMS;
        } else {
            boxId = cursor.getInt(mColumnsMap.mColumnMmsMessageBox);
            // Note that messages from the SIM card all have a boxId of zero: Mms.MESSAGE_BOX_ALL
            return (boxId == Mms.MESSAGE_BOX_INBOX || boxId == Mms.MESSAGE_BOX_ALL) ?
                    INCOMING_ITEM_TYPE_MMS : OUTGOING_ITEM_TYPE_MMS;
        }
    }

    public boolean hasSmsInConversation(Cursor cursor) {
        boolean hasSms = false;
        if (isCursorValid(cursor)) {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(mColumnsMap.mColumnMsgType);
                    if ("sms".equals(type)) {
                        hasSms = true;
                        break;
                    }
                } while (cursor.moveToNext());
                // Reset the position to 0
                cursor.moveToFirst();
            }
        }
        return hasSms;
    }

    public Cursor getCursorForItem(MessageItem item) {
        Cursor cursor = getCursor();
        if (isCursorValid(cursor)) {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(mRowIDColumn);
                    String type = cursor.getString(mColumnsMap.mColumnMsgType);
                    if (id == item.mMsgId && (type != null && type.equals(item.mType))) {
                        return cursor;
                    }
                } while (cursor.moveToNext());
            }
        }
        return null;
    }

    public static class ColumnsMap {
        public int mColumnMsgType;
        public int mColumnMsgId;
        public int mColumnSmsAddress;
        public int mColumnSmsBody;
        public int mColumnSubId;
        public int mColumnSmsDate;
        public int mColumnSmsDateSent;
        public int mColumnSmsRead;
        public int mColumnSmsType;
        public int mColumnSmsStatus;
        public int mColumnSmsLocked;
        public int mColumnSmsErrorCode;
        public int mColumnMmsSubject;
        public int mColumnMmsSubjectCharset;
        public int mColumnMmsDate;
        public int mColumnMmsDateSent;
        public int mColumnMmsRead;
        public int mColumnMmsMessageType;
        public int mColumnMmsMessageBox;
        public int mColumnMmsDeliveryReport;
        public int mColumnMmsReadReport;
        public int mColumnMmsErrorType;
        public int mColumnMmsLocked;
        public int mColumnMmsStatus;
        public int mColumnMmsTextOnly;
        public int mColumnMmsSubId;
        public int mColumnRecipientIds;
        public int mColumnRcsMsgType;
        public int mColumnRcsPath;
        public int mColumnRcsThumbPath;
        public int mColumnRcsBurnMessage;
        public int mColumnRcsIsDownload;
        public int mColumnRcsMsgState;
        public int mColumnRcsMimeType;
        public int mColumnFavoutite;
        public int mColumnRcsFileSize;
        public int mColumnRcsPlayTime;
        public int mColumnRcsChatType;
        public int mColumnRcsMessageId;
        public int mColumnTopTime;

        public ColumnsMap() {
            mColumnMsgType            = COLUMN_MSG_TYPE;
            mColumnMsgId              = COLUMN_ID;
            mColumnSmsAddress         = COLUMN_SMS_ADDRESS;
            mColumnSmsBody            = COLUMN_SMS_BODY;
            mColumnSubId              = COLUMN_SUB_ID;
            mColumnSmsDate            = COLUMN_SMS_DATE;
            mColumnSmsDateSent        = COLUMN_SMS_DATE_SENT;
            mColumnSmsType            = COLUMN_SMS_TYPE;
            mColumnSmsStatus          = COLUMN_SMS_STATUS;
            mColumnSmsLocked          = COLUMN_SMS_LOCKED;
            mColumnSmsErrorCode       = COLUMN_SMS_ERROR_CODE;
            mColumnMmsSubject         = COLUMN_MMS_SUBJECT;
            mColumnMmsSubjectCharset  = COLUMN_MMS_SUBJECT_CHARSET;
            mColumnMmsMessageType     = COLUMN_MMS_MESSAGE_TYPE;
            mColumnMmsMessageBox      = COLUMN_MMS_MESSAGE_BOX;
            mColumnMmsDeliveryReport  = COLUMN_MMS_DELIVERY_REPORT;
            mColumnMmsReadReport      = COLUMN_MMS_READ_REPORT;
            mColumnMmsErrorType       = COLUMN_MMS_ERROR_TYPE;
            mColumnMmsLocked          = COLUMN_MMS_LOCKED;
            mColumnMmsStatus          = COLUMN_MMS_STATUS;
            mColumnMmsTextOnly        = COLUMN_MMS_TEXT_ONLY;
            mColumnMmsSubId           = COLUMN_MMS_SUB_ID;
            mColumnRecipientIds       = COLUMN_RECIPIENT_IDS;
            mColumnRcsPath            = COLUMN_RCS_PATH;
            mColumnRcsThumbPath       = COLUMN_RCS_THUMB_PATH;
            mColumnRcsBurnMessage     = COLUMN_RCS_BURN;
            mColumnRcsIsDownload      = COLUMN_RCS_IS_DOWNLOAD;
            mColumnRcsMsgState        = COLUMN_RCS_MSG_STATE;
            mColumnRcsMimeType        = COLUMN_RCS_MIME_TYPE;
            mColumnRcsMsgType         = COLUMN_RCS_MSG_TYPE;
            mColumnFavoutite          = COLUMN_FAVOURITE;
            mColumnRcsFileSize        = COLUMN_RCS_FILESIZE;
            mColumnRcsChatType        = COLUMN_RCS_CHAT_TYPE;
            mColumnRcsMessageId       = COLUMN_RCS_MESSAGE_ID;
        }

        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnMsgType = cursor.getColumnIndexOrThrow(
                        MmsSms.TYPE_DISCRIMINATOR_COLUMN);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMsgId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsAddress = cursor.getColumnIndexOrThrow(Sms.ADDRESS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsBody = cursor.getColumnIndexOrThrow(Sms.BODY);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSubId = cursor.getColumnIndexOrThrow(Sms.SUBSCRIPTION_ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnFavoutite = cursor
                        .getColumnIndexOrThrow(RcsColumns.SmsRcsColumns.RCS_FAVOURITE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsFileSize = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_FILE_SIZE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsChatType = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_CHAT_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsMessageId = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_MESSAGE_ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsMimeType = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_MIME_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsPath = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_FILENAME);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsThumbPath = cursor
                    .getColumnIndexOrThrow(RcsColumns.SmsRcsColumns.RCS_THUMB_PATH);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsMsgType = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_MSG_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsBurnMessage = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_BURN);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsIsDownload = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_IS_DOWNLOAD);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRcsMsgState = cursor.getColumnIndexOrThrow(
                        RcsColumns.SmsRcsColumns.RCS_MSG_STATE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsDate = cursor.getColumnIndexOrThrow(Sms.DATE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsDateSent = cursor.getColumnIndexOrThrow(Sms.DATE_SENT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsType = cursor.getColumnIndexOrThrow(Sms.TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsStatus = cursor.getColumnIndexOrThrow(Sms.STATUS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsLocked = cursor.getColumnIndexOrThrow(Sms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsErrorCode = cursor.getColumnIndexOrThrow(Sms.ERROR_CODE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubject = cursor.getColumnIndexOrThrow(Mms.SUBJECT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubjectCharset = cursor.getColumnIndexOrThrow(Mms.SUBJECT_CHARSET);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsMessageType = cursor.getColumnIndexOrThrow(Mms.MESSAGE_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsMessageBox = cursor.getColumnIndexOrThrow(Mms.MESSAGE_BOX);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsDeliveryReport = cursor.getColumnIndexOrThrow(Mms.DELIVERY_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsReadReport = cursor.getColumnIndexOrThrow(Mms.READ_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsErrorType = cursor.getColumnIndexOrThrow(PendingMessages.ERROR_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsLocked = cursor.getColumnIndexOrThrow(Mms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsStatus = cursor.getColumnIndexOrThrow(Mms.STATUS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsTextOnly = cursor.getColumnIndexOrThrow(Mms.TEXT_ONLY);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubId = cursor.getColumnIndexOrThrow(Mms.SUBSCRIPTION_ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRecipientIds = cursor.getColumnIndexOrThrow(Threads.RECIPIENT_IDS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
        }
    }

    private static class MessageItemCache extends LruCache<Long, MessageItem> {
        public MessageItemCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(boolean evicted, Long key,
                MessageItem oldValue, MessageItem newValue) {
            oldValue.cancelPduLoading();
        }
    }

    public void setTextSize(float size) {
        mTextSize = size;
    }
}
