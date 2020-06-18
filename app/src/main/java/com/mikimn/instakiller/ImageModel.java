package com.mikimn.instakiller;

import com.google.firebase.Timestamp;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImageModel {

    @Expose private Timestamp createdAt;
    @Expose private String storageRef;
    @Expose private String uploaderId;
    @SerializedName("publicImage")
    @Expose private boolean publicImage;

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getStorageRef() {
        return storageRef;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public boolean isPublicImage() {
        return publicImage;
    }
}
