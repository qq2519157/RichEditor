package com.pppcar.richeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.MediaExtraInfo;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.utils.MediaUtils;
import com.pppcar.richeditor.databinding.ActivityMainBinding;
import com.pppcar.richeditorlibary.utils.ImageUtils;
import com.pppcar.richeditorlibary.view.DataImageView;
import com.pppcar.richeditorlibary.view.RichEditor;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnResultCallbackListener<LocalMedia> {

    private AlertDialog insertDialog;
    private ActivityMainBinding mBinding;
    private final ActivityResultLauncher<Intent> rotateImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String imagePath = data.getStringExtra("imagePath");
                    if (imagePath == null) {
                        return;
                    }
                    Log.e("imagePath+++++", imagePath);
                    int index = data.getIntExtra("index", 0);
                    Log.e("index+++++", index + "");
                    LinearLayout allLayout = (LinearLayout) mBinding.richEt.getChildAt(0);
                    RelativeLayout childAt = (RelativeLayout) allLayout.getChildAt(index);
                    ImageView open = childAt.findViewById(com.pppcar.richeditorlibary.R.id.iv_open);
                    ImageView rotate = childAt.findViewById(com.pppcar.richeditorlibary.R.id.iv_rotate);
                    ImageView delete = childAt.findViewById(com.pppcar.richeditorlibary.R.id.iv_delete);
                    open.setImageResource(com.pppcar.richeditorlibary.R.mipmap.open);
                    mBinding.richEt.hideMenu(open, delete, rotate);
                    DataImageView imageView = (DataImageView) childAt.getChildAt(0);
                    imageView.setAbsolutePath(imagePath);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    int imageHeight = allLayout.getWidth() * 3 / 5;
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, imageHeight);
                    lp.bottomMargin = 10;
                    imageView.setLayoutParams(lp);
                    Glide.with(MainActivity.this).load(imagePath).skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        requestPermissions();
    }

    private void requestPermissions() {
        XXPermissions.with(this)
                .permission(Permission.CAMERA)
                .permission(Permission.READ_MEDIA_IMAGES)
                .permission(Permission.READ_MEDIA_VIDEO)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        initView();
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            new AlertDialog.Builder(MainActivity.this,
                                    androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                                    .setMessage("权限未授予,部分功能可能无法正常执行，请在设置中手动开启权限")
                                    .setPositiveButton("去设置", (dialog, which) ->
                                            XXPermissions.startPermissionActivity(MainActivity.this, permissions))
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                        initView();
                    }
                });
    }

    /**
     * 上传照片
     */
    public void uploadImage() {
        PictureSelecctDialog pictureSelecctDialog = new PictureSelecctDialog(this, v -> {
            int tag = (Integer) v.getTag();
            PictureSelector pictureSelector = PictureSelector.create(MainActivity.this);
            switch (tag) {
                case PictureSelecctDialog.FROM_ALBUM:
                    pictureSelector
                            .openGallery(SelectMimeType.ofImage())
                            .setImageEngine(GlideEngine.createGlideEngine())
                            .setSelectionMode(SelectModeConfig.MULTIPLE)
                            .forResult(MainActivity.this);
                    break;
                case PictureSelecctDialog.TAKE_PICTURE:
                    pictureSelector
                            .openCamera(SelectMimeType.ofImage())
                            .isCameraForegroundService(false)
                            .forResult(MainActivity.this);
                    break;
                default:
                    break;
            }
        });
        pictureSelecctDialog.show();
    }

    /**
     * 上传视频
     */
    public void uploadVideo() {
        VideoSelecctDialog videoSelecctDialog = new VideoSelecctDialog(this, v -> {
            int tag = (Integer) v.getTag();
            PictureSelector pictureSelector = PictureSelector.create(MainActivity.this);
            switch (tag) {
                case VideoSelecctDialog.FROM_ALBUM:
                    pictureSelector
                            .openGallery(SelectMimeType.ofVideo())
                            .setImageEngine(GlideEngine.createGlideEngine())
                            .setSelectionMode(SelectModeConfig.SINGLE)
                            .forResult(MainActivity.this);
                    break;
                case VideoSelecctDialog.BY_CAMERA:
                    pictureSelector
                            .openCamera(SelectMimeType.ofVideo())
                            .isCameraForegroundService(true)
                            .forResult(MainActivity.this);
                    break;
                default:
                    break;
            }
        });
        videoSelecctDialog.show();
    }

    public void initView() {
        mBinding.richEt.setOnImageRotateListener(intent -> rotateImageLauncher.launch(intent));
        initEvent();
    }

    private void initEvent() {
        mBinding.richEt.setOnFocusChangeListener((v, hasFocus) -> {
            mBinding.ibPic.setEnabled(hasFocus);
            mBinding.ibPic.setClickable(hasFocus);
            mBinding.ibVideo.setEnabled(hasFocus);
            mBinding.ibVideo.setClickable(hasFocus);
        });
        mBinding.ibPic.setOnClickListener(v -> uploadImage());
        mBinding.ibVideo.setOnClickListener(v -> uploadVideo());
    }

    private void showInsertDialog() {
        if (insertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
            builder.setView(view);
            builder.setCancelable(false);
            insertDialog = builder.create();
        }
        insertDialog.show();
    }

    private void dismissInsertDialog() {
        if (insertDialog != null && insertDialog.isShowing()) {
            insertDialog.dismiss();
        }
    }

    /**
     * 异步方式插入图片
     */
    private void insertImagesSync(final String imagePath) {
        showInsertDialog();
        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                subscriber.onNext(imagePath);
                subscriber.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        dismissInsertDialog();
                        mBinding.richEt.addEditTextAtIndex(mBinding.richEt.getLastIndex(), " ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissInsertDialog();
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(String imagePath) {
                        mBinding.richEt.insertImage(imagePath, mBinding.richEt.getMeasuredWidth());
                    }
                });
    }

    /**
     * 异步方式插入视频
     */
    private void insertVideosSync(final String videoPath, final String firstImgUrl) {
        showInsertDialog();
        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                subscriber.onNext(videoPath);
                subscriber.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onError(Throwable e) {
                        dismissInsertDialog();
                    }

                    @Override
                    public void onComplete() {
                        dismissInsertDialog();
                        mBinding.richEt.addEditTextAtIndex(mBinding.richEt.getLastIndex(), " ");
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(String videoPath) {
                        mBinding.richEt.insertVideo(videoPath, firstImgUrl);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResult(ArrayList<LocalMedia> result) {
        for (LocalMedia media : result) {
            if (media.getWidth() == 0 || media.getHeight() == 0) {
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    MediaExtraInfo imageExtraInfo = MediaUtils.getImageSize(media.getPath());
                    media.setWidth(imageExtraInfo.getWidth());
                    media.setHeight(imageExtraInfo.getHeight());
                } else if (PictureMimeType.isHasVideo(media.getMimeType())) {
                    MediaExtraInfo videoExtraInfo = MediaUtils.getVideoSize(this, media.getPath());
                    media.setWidth(videoExtraInfo.getWidth());
                    media.setHeight(videoExtraInfo.getHeight());
                }
            }
            String path = "";
            if (media.isCompressed()) {
                path = media.getCompressPath();
            } else if (media.isCut()) {
                path = media.getCutPath();
            } else if (media.isToSandboxPath()) {
                path = media.getSandboxPath();
            } else {
                path = media.getRealPath();
            }
            if (TextUtils.isEmpty(path)) {
                continue;
            }
            if (PictureMimeType.isHasImage(media.getMimeType())) {
                insertImagesSync(path);
            } else if (PictureMimeType.isHasVideo(media.getMimeType())) {
                Bitmap bitmap = ImageUtils.getFirstImg(path);
                String firstImgPath = ImageUtils.saveFirstBitmap(bitmap);
                insertVideosSync(path, firstImgPath);
            }
        }
    }

    @Override
    public void onCancel() {
    }
}
