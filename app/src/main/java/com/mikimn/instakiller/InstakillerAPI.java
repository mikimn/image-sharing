package com.mikimn.instakiller;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.List;

public class InstakillerAPI {

    interface Callback<T> {
        void onResult(T object);
    }

    interface ListCallback<T> {
        void onResult(List<T> objects);
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static InstakillerAPI instakillerAPI = new InstakillerAPI();

    public static InstakillerAPI instance() {
        return instakillerAPI;
    }

    public void getPublicImages(final ListCallback<ImageModel> callback) {
        db.collection("images")
                .whereEqualTo("publicImage", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        callback.onResult(queryDocumentSnapshots.toObjects(ImageModel.class));
                    }
                }).addOnFailureListener(e -> callback.onResult(Collections.<ImageModel>emptyList()));
    }

    public void getPrivateImages(final ListCallback<ImageModel> callback) {
        // TODO Room Implementation
    }
}
