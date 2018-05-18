package com.fly.video.downloader.layout.listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fly.video.downloader.R;
import com.fly.video.downloader.core.io.Storage;
import com.fly.video.downloader.core.listener.FragmentListener;
import com.fly.video.downloader.util.AnalyzerTask;
import com.fly.video.downloader.util.DownloadQueue;
import com.fly.video.downloader.util.content.Downloader;
import com.fly.video.downloader.util.content.FileStorage;
import com.fly.video.downloader.util.content.Video;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class VideoFragmentListener extends FragmentListener implements AnalyzerTask.AnalyzeListener, DownloadQueue.QueueListener {

    protected Video video = null;
    protected DownloadQueue downloadQueue = new DownloadQueue();

    private Unbinder unbinder;
    @BindView(R.id.video_avatar)
    ImageView avatar;
/*    @BindView(R.id.video_cover)
    ImageView cover;*/
    @BindView(R.id.video_nickname)
    TextView nickname;
    @BindView(R.id.video_content)
    TextView content;
    @BindView(R.id.video_player)
    TextureView textureView;

    PlayerListener playerListener;


    public VideoFragmentListener(Fragment fragment, Context context) {

        super(fragment, context);
        this.downloadQueue.setQueueListener(this);
    }

    @Override
    public void onCreateView(View view)
    {
        unbinder = ButterKnife.bind(this, view);

        playerListener = new PlayerListener(context, textureView);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        if (playerListener != null)
            playerListener.destoryVideo();
    }


    @Override
    public void onAnalyzed(Video video)
    {
        synchronized (VideoFragmentListener.class) {
            this.video = video;
            downloadQueue.clear();
            nickname.setText(video.getUser().getNickname());
            content.setText(video.getTitle());

            downloadQueue.add("avatar_thumb-" + video.getUser().getId(), new Downloader(video.getUser().getAvatarThumbUrl()).setFileAsCache(FileStorage.TYPE.IMAGE, "avatar_thumb-" + video.getUser().getId()));
            //downloadQueue.add("cover-" + video.getId(), new Downloader(video.getCoverUrl(), FileStorage.TYPE.IMAGE, "cover-" + video.getId()).saveToCache());

            Downloader videoDownloader = new Downloader(video.getUrl()).setFileAsDCIM(FileStorage.TYPE.VIDEO, "video-"+ video.getId() + ".mp4");

            if (videoDownloader.getFile().exists())
                playerListener.playVideo(Uri.fromFile(videoDownloader.getFile()));
            else {
                downloadQueue.add("video-" + video.getId(), videoDownloader);
                playerListener.playVideo(video.getUrl());
            }

            try{
                downloadQueue.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public void onQueueDownloaded(DownloadQueue downloadQueue, ArrayList<String> canceledHashes) {
        Toast.makeText(this.context, R.string.download_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQueueProgress(DownloadQueue downloadQueue, long loaded, long total) {
    }

    @Override
    public void onDownloaded(String hash, Downloader downloader) {
        String[] segments = hash.split("-");

        Bitmap bitmap;
        switch (segments[0])
        {
/*            case "cover":
                bitmap = BitmapFactory.decodeFile(downloader.getFile().getAbsolutePath());
                cover.setImageBitmap(bitmap);
                break;*/
            case "avatar_thumb":
                bitmap = BitmapFactory.decodeFile(downloader.getFile().getAbsolutePath());
                avatar.setImageBitmap(bitmap);
                break;
            case "video":
                Storage.rescanGallery(this.context, downloader.getFile());
                break;
        }
    }

    @Override
    public void onDownloadProgress(String hash, Downloader downloader, long loaded, long total) {

    }

    @Override
    public void onDownloadCanceled(String hash, Downloader downloader) {

    }

    @Override
    public void onDownloadError(String hash, Downloader downloader, Exception e) {

    }

    @Override
    public void onAnalyzeError(Exception e) {
        Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAnalyzeCanceled() {

    }


    @OnClick(R.id.video_close)
    public void onClose()
    {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
