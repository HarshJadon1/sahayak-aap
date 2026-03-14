package com.sahayak.app;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_history);

        recyclerView = findViewById(R.id.rv_emergency_history);
        tvEmpty = findViewById(R.id.tv_empty_history);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadVideos();
    }

    private void loadVideos() {
        List<EmergencyVideo> videoList = new ArrayList<>();
        
        Uri collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Video.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"%Sahayak/Emergency_Recordings%"};
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE
        };

        try (Cursor cursor = getContentResolver().query(collection, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

                do {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    long dateAdded = cursor.getLong(dateColumn);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                    videoList.add(new EmergencyVideo(name, contentUri, dateAdded));
                } while (cursor.moveToNext());
            }
        }

        if (videoList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            adapter = new VideoAdapter(videoList);
            recyclerView.setAdapter(adapter);
        }
    }

    // Simple Data Model
    static class EmergencyVideo {
        String name;
        Uri uri;
        long dateAdded;

        EmergencyVideo(String name, Uri uri, long dateAdded) {
            this.name = name;
            this.uri = uri;
            this.dateAdded = dateAdded;
        }
    }

    // Adapter Class
    class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
        private List<EmergencyVideo> videos;

        VideoAdapter(List<EmergencyVideo> videos) {
            this.videos = videos;
        }

        @NonNull
        @Override
        public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_video, parent, false);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
            EmergencyVideo video = videos.get(position);
            holder.tvName.setText(video.name);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy | hh:mm a", Locale.getDefault());
            holder.tvDetails.setText(sdf.format(new Date(video.dateAdded * 1000)));

            // Using Glide for thumbnail (Need to add dependency if not present, otherwise use standard)
            // holder.ivThumbnail.setImageURI(video.uri); // Or use thumbnail utility
            
            holder.btnPlay.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(video.uri, "video/mp4");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            });

            holder.btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/mp4");
                shareIntent.putExtra(Intent.EXTRA_STREAM, video.uri);
                startActivity(Intent.createChooser(shareIntent, "Share Emergency Video"));
            });
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        class VideoViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetails;
            ImageView ivThumbnail;
            View btnPlay, btnShare;

            VideoViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_video_name);
                tvDetails = itemView.findViewById(R.id.tv_video_details);
                ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
                btnPlay = itemView.findViewById(R.id.btn_play_video);
                btnShare = itemView.findViewById(R.id.btn_share_video);
            }
        }
    }
}
