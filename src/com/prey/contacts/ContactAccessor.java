package com.prey.contacts;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;


/**
 * This implementation has several advantages:
 * <ul>
 * <li>It sees contacts from multiple accounts.
 * <li>It works with aggregated contacts. So for example, if the contact is the result
 * of aggregation of two raw contacts from different accounts, it may return the name from
 * one and the phone number from the other.
 * <li>It is efficient because it uses the more efficient current API.
 * <li>Not obvious in this particular example, but it has access to new kinds
 * of data available exclusively through the new APIs. Exercise for the reader: add support
 * for nickname (see {@link android.provider.ContactsContract.CommonDataKinds.Nickname}) or
 * social status updates (see {@link android.provider.ContactsContract.StatusUpdates}).
 * </ul>
 */
public class ContactAccessor {

    public Intent getPickContactIntent() {
        return new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
    }

    /**
     * Retrieves the contact information.
     */
    public ContactInfo loadContact(ContentResolver contentResolver, Uri contactUri) {
        ContactInfo contactInfo = new ContactInfo();
        long contactId = -1;

        // Load the display name for the specified person
        Cursor cursor = contentResolver.query(contactUri,
                new String[]{Contacts._ID, Contacts.DISPLAY_NAME}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                contactId = cursor.getLong(0);
                contactInfo.setDisplayName(cursor.getString(1));
            }
        } finally {
            cursor.close();
        }

        // Load the phone number (if any).
        cursor = contentResolver.query(Phone.CONTENT_URI,
                new String[]{Phone.NUMBER},
                Phone.CONTACT_ID + "=" + contactId, null, Phone.IS_SUPER_PRIMARY + " DESC");
        try {
            if (cursor.moveToFirst()) {
                contactInfo.setPhoneNumber(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }

        return contactInfo;
    }
}