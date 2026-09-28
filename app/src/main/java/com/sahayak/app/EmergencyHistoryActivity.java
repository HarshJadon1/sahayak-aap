package com.sahayak.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
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
        
        loadPrivateVideos();
    }

    private void loadPrivateVideos() {
        List<EmergencyVideo> videoList = new ArrayList<>();
        File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "Sahayak_Recordings");
        
        if (movieDir.exists() && movieDir.isDirectory()) {
            File[] files = movieDir.listFiles((dir, name) -> name.endsWith(".mp4"));
            if (files != null) {
                for (File file : files) {
                    videoList.add(new EmergencyVideo(file.getName(), file, file.lastModified()));
                }
            }
        }

        // Sort by date (newest first)
        videoList.sort((v1, v2) -> Long.compare(v2.dateAdded, v1.dateAdded));

        if (videoList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No private recordings found");
        } else {
            tvEmpty.setVisibility(View.GONE);
            adapter = new VideoAdapter(videoList);
            recyclerView.setAdapter(adapter);
        }
    }

    static class EmergencyVideo {
        String name;
        File file;
        long dateAdded;

        EmergencyVideo(String name, File file, long dateAdded) {
            this.name = name;
            this.file = file;
            this.dateAdded = dateAdded;
        }
    }

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
            holder.tvDetails.setText(sdf.format(new Date(video.dateAdded)));

            Glide.with(EmergencyHistoryActivity.this)
                 .load(video.file)
                 .placeholder(android.R.drawable.ic_menu_gallery)
                 .error(android.R.drawable.ic_menu_report_image)
                 .centerCrop()
                 .into(holder.ivThumbnail);
            
            holder.btnPlay.setOnClickListener(v -> {
                Uri contentUri = FileProvider.getUriForFile(EmergencyHistoryActivity.this, 
                        getPackageName() + ".fileprovider", video.file);
                
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(contentUri, "video/mp4");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(EmergencyHistoryActivity.this, "No video player found", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnShare.setOnClickListener(v -> {
                Uri contentUri = FileProvider.getUriForFile(EmergencyHistoryActivity.this, 
                        getPackageName() + ".fileprovider", video.file);
                        
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/mp4");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
