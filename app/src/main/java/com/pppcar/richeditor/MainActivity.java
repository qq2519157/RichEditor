package com.pppcar.richeditor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.pppcar.richeditorlibary.utils.ImageUtils;
import com.pppcar.richeditorlibary.utils.UriUtil;
import com.pppcar.richeditorlibary.view.DataImageView;
import com.pppcar.richeditorlibary.view.RichEditor;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE_IMAGE = 10086;
    private static final int REQUEST_CODE_CHOOSE_VIDEO = 10010;
    @BindView(R.id.ib_pic)
    ImageButton mIbPic;
    @BindView(R.id.ib_video)
    ImageButton mIbVideo;
    @BindView(R.id.rich_et)
    RichEditor mContent;
    private ProgressDialog insertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initEvent();
    }

    @OnClick({R.id.ib_pic, R.id.ib_video})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_pic:
                AndPermission.with(this)
                        .runtime()
                        .permission(Permission.READ_EXTERNAL_STORAGE, Permission.CAMERA)
                        .onGranted(permissions -> {
                            // Storage permission are allowed.
                            uploadImage();
                        })
                        .onDenied(permissions -> {
                            // Storage permission are not allowed.
                            Toast.makeText(this, "请在应用权限管理中打开权限", Toast.LENGTH_SHORT).show();
                        })
                        .start();
                break;
            case R.id.ib_video:
                AndPermission.with(this)
                        .runtime()
                        .permission(Permission.READ_EXTERNAL_STORAGE, Permission.CAMERA)
                        .onGranted(permissions -> {
                            // Storage permission are allowed.
                            uploadVideo();
                        })
                        .onDenied(permissions -> {
                            // Storage permission are not allowed.
                            Toast.makeText(this, "请在应用权限管理中打开权限", Toast.LENGTH_SHORT).show();
                        })
                        .start();
        }
    }

    /**
     * 上传照片
     */
    public void uploadImage() {
        PictureSelecctDialog pictureSelecctDialog = new PictureSelecctDialog(this, v -> {
            int tag = (Integer) v.getTag();
            switch (tag) {
                case PictureSelecctDialog.FROM_ALBUM:
                    PictureSelector.create(this)
                            .openGallery(PictureMimeType.ofImage())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .selectionMode(PictureConfig.MULTIPLE)
                            .forResult(REQUEST_CODE_CHOOSE_IMAGE);
                    break;
                case PictureSelecctDialog.TAKE_PICTURE:
                    PictureSelector.create(this)
                            .openCamera(PictureMimeType.ofImage())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .isPreviewImage(true)
                            .forResult(REQUEST_CODE_CHOOSE_IMAGE);
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
            switch (tag) {
                case VideoSelecctDialog.FROM_ALBUM:
                    PictureSelector.create(this)
                            .openGallery(PictureMimeType.ofVideo())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .selectionMode(PictureConfig.SINGLE)
                            .forResult(REQUEST_CODE_CHOOSE_VIDEO);
                    break;
                case VideoSelecctDialog.BY_CAMERA:
                    PictureSelector.create(this)
                            .openCamera(PictureMimeType.ofVideo())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .selectionMode(PictureConfig.SINGLE)
                            .forResult(REQUEST_CODE_CHOOSE_VIDEO);
                    break;
                default:
                    break;
            }
        });
        videoSelecctDialog.show();
    }

    private void initView() {
        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);
    }

    private void initEvent() {
        mContent.setOnFocusChangeListener((v, hasFocus) -> {
            mIbPic.setEnabled(hasFocus);
            mIbPic.setClickable(hasFocus);
            mIbVideo.setEnabled(hasFocus);
            mIbVideo.setClickable(hasFocus);
        });

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
                        mContent.addEditTextAtIndex(mContent.getLastIndex(), " ");
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
                        mContent.insertImage(imagePath, mContent.getMeasuredWidth());
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
                        mContent.addEditTextAtIndex(mContent.getLastIndex(), " ");
                        Toast.makeText(MainActivity.this, "视频插入成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(String videoPath) {
                        mContent.insertVideo(videoPath, firstImgUrl);
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
                case REQUEST_CODE_CHOOSE_IMAGE:
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia localMedia : selectList) {
                        insertImagesSync(localMedia.getPath());
                    }
                    break;
                case REQUEST_CODE_CHOOSE_VIDEO:
                    List<LocalMedia> selectVideoList = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia localMedia : selectVideoList) {
                        String path = localMedia.getPath();
                        Bitmap bitmap = ImageUtils.getFirstImg(path);
                        String firstImgPath = ImageUtils.saveFirstBitmap(bitmap);
                        insertVideosSync(path, firstImgPath);
                    }
                    break;
                case RichEditor.ROTATE_IMAGE:
                    String imagePath = data.getStringExtra("imagePath");
                    if (imagePath == null) {
                        return;
                    }
                    Log.e("imagePath+++++", imagePath);
                    int index = data.getIntExtra("index", 0);
                    Log.e("index+++++", index + "");
                    LinearLayout allLayout = (LinearLayout) mContent.getChildAt(0);
                    RelativeLayout childAt = (RelativeLayout) allLayout.getChildAt(index);
                    ImageView open = childAt.findViewById(R.id.iv_open);
                    ImageView rotate = childAt.findViewById(R.id.iv_rotate);
                    ImageView delete = childAt.findViewById(R.id.iv_delete);
                    open.setImageResource(R.mipmap.open);
                    mContent.hideMenu(open, delete, rotate);
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
//                    imageView.setImageBitmap(mContent.getScaledBitmap(imagePath, mContent.getMeasuredWidth()));
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
