package com.pppcar.richeditor;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.luck.picture.lib.app.PictureAppMaster;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.MediaExtraInfo;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.utils.MediaUtils;
import com.luck.picture.lib.utils.ToastUtils;
import com.pppcar.richeditor.databinding.ActivityMainBinding;
import com.pppcar.richeditorlibary.utils.ImageUtils;
import com.pppcar.richeditorlibary.view.DataImageView;
import com.pppcar.richeditorlibary.view.RichEditor;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements OnResultCallbackListener<LocalMedia> {

    private ProgressDialog insertDialog;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        MainActivityPermissionsDispatcher.initViewWithPermissionCheck(this);
    }

    /**
     * 上传照片
     */
    public void uploadImage() {
        PictureSelecctDialog pictureSelecctDialog = new PictureSelecctDialog(this, v -> {
            int tag = (Integer) v.getTag();
            PictureSelector pictureSelector = PictureSelector.create(this);
            switch (tag) {
                case PictureSelecctDialog.FROM_ALBUM:
                    pictureSelector
                            .openGallery(SelectMimeType.ofImage())
                            .setImageEngine(GlideEngine.createGlideEngine())
                            .setSelectionMode(SelectModeConfig.MULTIPLE)
                            .forResult(this);
                    break;
                case PictureSelecctDialog.TAKE_PICTURE:
                    pictureSelector
                            .openCamera(SelectMimeType.ofImage())
                            .isCameraForegroundService(false)
                            .forResult(this);
                    break;
                default:
                    break;
            }
        });
        pictureSelecctDialog.show();
    }

    @OnShowRationale({Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    //给用户解释要请求什么权限，为什么需要此权限
    public void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(MainActivity.this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setMessage("使用此功能需要权限，是否继续请求权限")
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();//继续执行请求
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.cancel();//取消执行请求
            }
        })
                .show();
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void multiNeverAsk() {
        ToastUtils.showToast(this, "权限未授予,部分功能可能无法正常执行");
        initView();
    }

    @OnPermissionDenied({Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})//一旦用户拒绝了
    public void multiDenied() {
        ToastUtils.showToast(this, "已拒绝一个或以上权限,可能影响正常使用");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    /**
     * 上传视频
     */
    public void uploadVideo() {
        VideoSelecctDialog videoSelecctDialog = new VideoSelecctDialog(this, v -> {
            int tag = (Integer) v.getTag();
            PictureSelector pictureSelector = PictureSelector.create(this);
            switch (tag) {
                case VideoSelecctDialog.FROM_ALBUM:
                    pictureSelector
                            .openGallery(SelectMimeType.ofVideo())
                            .setImageEngine(GlideEngine.createGlideEngine())
                            .setSelectionMode(SelectModeConfig.SINGLE)
                            .forResult(this);
                    break;
                case VideoSelecctDialog.BY_CAMERA:
                    pictureSelector
                            .openCamera(SelectMimeType.ofVideo())
                            .isCameraForegroundService(true)
                            .forResult(this);
                    break;
                default:
                    break;
            }
        });
        videoSelecctDialog.show();
    }

    @NeedsPermission({Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void initView() {
        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);
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


    /**
     * 异步方式插入图片
     *
     * @param imagePath 图片路径
     */
    private void insertImagesSync(final String imagePath) {
        insertDialog.show();
        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {

                //Log.i("NewActivity", "###imagePath="+imagePath);
                subscriber.onNext(imagePath);

                subscriber.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        insertDialog.dismiss();
                        mBinding.richEt.addEditTextAtIndex(mBinding.richEt.getLastIndex(), " ");
                        Toast.makeText(MainActivity.this, "图片插入成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        insertDialog.dismiss();
                        Toast.makeText(MainActivity.this, "图片插入失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
     *
     * @param videoPath 视频路径
     */
    private void insertVideosSync(final String videoPath, final String firstImgUrl) {
        insertDialog.show();

        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                subscriber.onNext(videoPath);
                subscriber.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onError(Throwable e) {
                        insertDialog.dismiss();
                        Toast.makeText(MainActivity.this, "视频插入失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        insertDialog.dismiss();
                        mBinding.richEt.addEditTextAtIndex(mBinding.richEt.getLastIndex(), " ");
                        Toast.makeText(MainActivity.this, "视频插入成功", Toast.LENGTH_SHORT).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RichEditor.ROTATE_IMAGE:
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
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//裁剪居中
                    int imageHeight = allLayout.getWidth() * 3 / 5;
                    //调整图片高度，这里是否有必要，如果出现微博长图，可能会很难看
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, imageHeight);//设置图片固定高度
                    lp.bottomMargin = 10;
                    imageView.setLayoutParams(lp);
                    Glide.with(this).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
//                    imageView.setImageBitmap(mBinding.richEt.getScaledBitmap(imagePath, mBinding.richEt.getMeasuredWidth()));
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                    MediaExtraInfo videoExtraInfo = MediaUtils.getVideoSize(PictureAppMaster.getInstance().getAppContext(), media.getPath());
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
