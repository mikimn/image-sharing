package com.mikimn.instakiller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseStorage publicStorage = FirebaseStorage.getInstance();
    private List<ImageModel> images;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        recyclerView = view.findViewById(R.id.feed_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InstakillerAPI.instance().getPublicImages(images -> {
            this.images = images;
            recyclerView.setAdapter(new FeedAdapter());
        });
    }

    private class FeedAdapter extends RecyclerView.Adapter<FeedViewHolder> {
        @NonNull
        @Override
        public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FeedViewHolder(getLayoutInflater().inflate(R.layout.item_image, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
            holder.setImage(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }
    }

    private class FeedViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView titleTextView;
        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.image_view);
            this.titleTextView = itemView.findViewById(R.id.title_text_view);
        }

        void setImage(ImageModel image) {
            if (image.isPublicImage()) {
                publicStorage.getReference(image.getStorageRef())
                        .getDownloadUrl()
                        .addOnSuccessListener(uri -> Glide.with(imageView.getContext()).load(uri).into(imageView));
            } else {
                Glide.with(imageView.getContext()).load(image.getStorageRef()).into(imageView);
            }
            SimpleDateFormat format = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
            titleTextView.setText(String.format("By: %s. Uploaded at %s", image.getUploaderId(),
                    format.format(image.getCreatedAt().toDate())));
        }
    }
}
