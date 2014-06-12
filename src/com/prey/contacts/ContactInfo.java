/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.contacts;

import android.graphics.Bitmap;

/**
 * A model object containing contact data.
 */
public class ContactInfo {

    private String mDisplayName;
    private String mPhoneNumber;
    private Integer photoId;
    private Bitmap picture;

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

	public Integer getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Integer photoId) {
		this.photoId = photoId;
	}

	public Bitmap getPicture() {
		return picture;
	}

	public void setPicture(Bitmap picture) {
		this.picture = picture;
	}
}
