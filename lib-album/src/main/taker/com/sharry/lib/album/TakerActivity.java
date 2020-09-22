package com.sharry.lib.album;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.sharry.lib.album.toolbar.ImageViewOptions;
import com.sharry.lib.album.toolbar.SToolbar;
import com.sharry.lib.camera.AspectRatio;
import com.sharry.lib.camera.IPreviewer;
import com.sharry.lib.camera.SCameraView;

import java.lang.reflect.Constructor;

/**
 * 图片/视频拍摄页面
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02
 */
public class TakerActivity extends AppCompatActivity implements
        ITakerContract.IView,
        AspectRatioFragment.Listener,
        RecorderButton.Interaction {

    public static final int REQUEST_CODE = 286;
    public static final String RESULT_EXTRA_MEDIA_META = "RESULT_EXTRA_MEDIA_META";
    private static final String EXTRA_TAKER_CONFIG = "extra_taker_config";


    public static void launchForResult(CallbackFragment fragment, TakerConfig config) {
        Intent intent = new Intent(fragment.getActivity(), TakerActivity.class);
        intent.putExtra(EXTRA_TAKER_CONFIG, config);
        fragment.startActivityForResult(intent, REQUEST_CODE);
    }

    private static final AspectRatio[] ASPECT_RATIOS = {
            AspectRatio.of(1, 1),
            AspectRatio.of(4, 3),
            AspectRatio.of(16, 9)
    };

    /**
     * The presenter associated with this Activity.
     */
    private ITakerContract.IPresenter mPresenter;

    /**
     * Widgets
     */
    private SToolbar mToolbar;
    private ImageView mIvAspect;
    private ImageView mIvFullScreen;
    private SCameraView mCameraView;
    private RecorderButton mBtnRecord;
    private ImageView mIvPicturePreview;
    private VideoView mVideoPlayer;
    private ConstraintLayout mClEnsurePanel;
    private ImageView mIvDenied;
    private ImageView mIvGranted;
    private int mStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_ablum_activity_taker);
        initTitle();
        initViews();
        initPresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoPlayer.getVisibility() == View.VISIBLE) {
            mVideoPlayer.resume();
        } else {
            mVideoPlayer.suspend();
        }
        if (mCameraView.getVisibility() == View.VISIBLE) {
            mCameraView.startPreview();
        } else {
            mCameraView.stopPreview();
        }
    }

    @Override
    protected void onPause() {
        mCameraView.stopPreview();
        mVideoPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mPresenter.handleViewDestroy();
        super.onDestroy();
    }

    ////////////////////////////////////ITakerContract.IView///////////////////////////////////////


    @Override
    public void setPreviewAspect(@NonNull AspectRatio aspect) {
        mCameraView.setAspectRatio(aspect);
    }

    @Override
    public void setPreviewFullScreen(boolean fullScreen) {
        mCameraView.setAdjustViewBounds(!fullScreen);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mCameraView.getLayoutParams();
        params.topToTop = fullScreen ? ConstraintSet.PARENT_ID : ConstraintSet.UNSET;
        params.topToBottom = fullScreen ? ConstraintSet.UNSET : R.id.toolbar;
        mCameraView.setLayoutParams(params);
    }

    @Override
    public void setPreviewRenderer(@NonNull String rendererClassName) {
        try {
            Class<? extends IPreviewer.Renderer> rendererClass = (Class<? extends IPreviewer.Renderer>)
                    Class.forName(rendererClassName);
            Constructor constructor = rendererClass.getDeclaredConstructor(Context.class);
            constructor.setAccessible(true);
            IPreviewer.Renderer renderer = (IPreviewer.Renderer) constructor.newInstance(this);
            mCameraView.getPreviewer().setRenderer(renderer);
        } catch (Throwable e) {
            // ignore.
        }
    }

    @Override
    public void setRecordButtonVisible(boolean visible) {
        mBtnRecord.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setSupportVideoRecord(boolean isVideoRecord) {
        mBtnRecord.setLongClickEnable(isVideoRecord);
    }

    @Override
    public void setMaxRecordDuration(long maxDuration) {
        mBtnRecord.setMaxProgress(maxDuration);
    }

    @Override
    public void setRecordButtonProgress(long currentDuration) {
        mBtnRecord.setCurrentProgress(currentDuration);
    }

    @Override
    public void setProgressColor(int recordProgressColor) {
        mBtnRecord.setProgressColor(recordProgressColor);
    }

    @Override
    public void setPreviewSource(@NonNull Bitmap bitmap) {
        mIvPicturePreview.setImageBitmap(bitmap);
    }

    @Override
    public void startVideoPlayer(@NonNull Uri uri) {
        mVideoPlayer.setVideoURI(uri);
    }

    @Override
    public void stopVideoPlayer() {
        mVideoPlayer.suspend();
    }

    @Override
    public Bitmap getCameraBitmap() {
        return mCameraView.takePicture();
    }

    @Override
    public void setStatus(int status) {
        mStatus = status;
        switch (status) {
            case STATUS_PICKED:
                // Keeping.
                break;
            case STATUS_VIDEO_PLAY:
                // 停止预览
                mCameraView.stopPreview();
                // 置为视频播放状态
                mToolbar.setVisibility(View.INVISIBLE);
                mCameraView.setVisibility(View.INVISIBLE);
                mBtnRecord.setVisibility(View.INVISIBLE);
                mIvPicturePreview.setVisibility(View.INVISIBLE);
                mVideoPlayer.setVisibility(View.VISIBLE);
                mClEnsurePanel.setVisibility(View.VISIBLE);
                break;
            case STATUS_PICTURE_PREVIEW:
                // 停止预览
                mCameraView.stopPreview();
                // 置为照片预览状态
                mToolbar.setVisibility(View.INVISIBLE);
                mCameraView.setVisibility(View.INVISIBLE);
                mBtnRecord.setVisibility(View.INVISIBLE);
                mVideoPlayer.setVisibility(View.INVISIBLE);
                mIvPicturePreview.setVisibility(View.VISIBLE);
                mClEnsurePanel.setVisibility(View.VISIBLE);
                break;
            case STATUS_CAMERA_PREVIEW:
            default:
                // 置为预览状态
                mToolbar.setVisibility(View.VISIBLE);
                mIvFullScreen.setVisibility(View.VISIBLE);
                mIvAspect.setVisibility(View.VISIBLE);
                mCameraView.setVisibility(View.VISIBLE);
                mBtnRecord.setVisibility(View.VISIBLE);
                mVideoPlayer.setVisibility(View.INVISIBLE);
                mIvPicturePreview.setVisibility(View.INVISIBLE);
                mClEnsurePanel.setVisibility(View.INVISIBLE);
                // 开始预览
                mCameraView.startPreview();
                break;
        }
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    @Override
    public void toast(@IntegerRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setResult(@NonNull MediaMeta mediaMeta) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_EXTRA_MEDIA_META, mediaMeta);
        setResult(RESULT_OK, intent);
        setStatus(STATUS_PICKED);
        finish();
    }

    ////////////////////////////////////AspectRatioFragment.Interaction///////////////////////////////////////

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        mCameraView.setAspectRatio(ratio);
    }

    ////////////////////////////////////RecordProgressButton.Interaction///////////////////////////////////////

    @Override
    public void onTakePicture() {
        mPresenter.handleTakePicture();
    }

    @Override
    public void onRecordStart() {
        mIvAspect.setVisibility(View.INVISIBLE);
        mIvFullScreen.setVisibility(View.INVISIBLE);
        mPresenter.handleRecordStart(mCameraView);
    }

    @Override
    public void onRecordFinish(long duration) {
        mIvAspect.setVisibility(View.VISIBLE);
        mIvFullScreen.setVisibility(View.VISIBLE);
        mPresenter.handleRecordFinish(duration);
    }

    ////////////////////////////////////Private method///////////////////////////////////////

    private void initTitle() {
        mToolbar = findViewById(R.id.toolbar);
        int paddingSize = DensityUtil.dp2px(this, 20f);
        // switch
        mToolbar.addLeftMenuImage(
                ImageViewOptions.Builder()
                        .setDrawableResId(R.drawable.ic_album_taker_camera_switch)
                        .setPaddingLeft(paddingSize)
                        .setListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int curFacing = mCameraView.getFacing();
                                mCameraView.setFacing(curFacing == SCameraView.FACING_FRONT ?
                                        SCameraView.FACING_BACK : SCameraView.FACING_FRONT);
                            }
                        })
                        .build()
        );
        // aspect
        mToolbar.addLeftMenuImage(
                ImageViewOptions.Builder()
                        .setDrawableResId(R.drawable.ic_album_taker_aspect)
                        .setPaddingLeft(paddingSize)
                        .setListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AspectRatioFragment.newInstance(ASPECT_RATIOS, mCameraView.getAspectRatio())
                                        .show(getSupportFragmentManager(), AspectRatioFragment.class.getSimpleName());
                            }
                        })
                        .build()
        );
        // 全面屏
        mToolbar.addLeftMenuImage(
                ImageViewOptions.Builder()
                        .setDrawableResId(R.drawable.ic_album_taker_full_screen)
                        .setPaddingLeft(paddingSize)
                        .setListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setPreviewFullScreen(mCameraView.getAdjustViewBounds());
                            }
                        })
                        .build()
        );
        // 获取 Toolbar 上的控件
        mIvAspect = mToolbar.getLeftMenuView(1);
        mIvFullScreen = mToolbar.getLeftMenuView(2);
    }

    private void initViews() {
        // SCameraView
        mCameraView = findViewById(R.id.camera_view);
        mCameraView.setAutoFocus(true);

        // RecordProgressButton
        mBtnRecord = findViewById(R.id.btn_record);

        // Taken picture preview
        mIvPicturePreview = findViewById(R.id.iv_picture_preview);

        // Video Player
        mVideoPlayer = findViewById(R.id.video_view);
        mVideoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
        mVideoPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mPresenter.handleVideoPlayFailed();
                return true;
            }
        });

        // Selector Panel
        mClEnsurePanel = findViewById(R.id.cl_ensure_panel);

        // Denied
        mIvDenied = findViewById(R.id.iv_denied);
        mIvDenied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleDenied();
            }
        });

        // Granted
        mIvGranted = findViewById(R.id.iv_granted);
        mIvGranted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleGranted();
            }
        });
    }

    private void initPresenter() {
        mPresenter = new TakerPresenter(
                this,
                (TakerConfig) getIntent().getParcelableExtra(EXTRA_TAKER_CONFIG)
        );
    }

}
