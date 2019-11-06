package com.pppcar.richeditor;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TResult;
import com.pppcar.richeditorlibary.utils.ImageUtils;
import com.pppcar.richeditorlibary.utils.UriUtil;
import com.pppcar.richeditorlibary.view.DataImageView;
import com.pppcar.richeditorlibary.view.RichEditor;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends TakePhotoActivity {

    @BindView(R.id.ib_pic)
    ImageButton mIbPic;
    @BindView(R.id.ib_video)
    ImageButton mIbVideo;
    @BindView(R.id.rich_et)
    RichEditor mContent;
    private ProgressDialog insertDialog;
    private boolean hasPermission;
    private TakePhoto mTakePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initPermission();
        mTakePhoto = getTakePhoto();
        initView();
        initEvent();
    }

    @OnClick({R.id.ib_pic, R.id.ib_video})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_pic:
                if (hasPermission) {
                    uploadImage();
                }
                break;
            case R.id.ib_video:
                if (hasPermission) {
                    uploadVideo();
                }
                break;
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
                    mTakePhoto.onPickFromGallery();
                    break;
                case PictureSelecctDialog.TAKE_PICTURE:
                    File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
                    File parentFile = file.getParentFile();
                    if (parentFile != null) {
                        if (!parentFile.exists()) {
                            boolean mkdirs = parentFile.mkdirs();
                            Log.e(MainActivity.class.getSimpleName(), mkdirs ? "create success" : "create failed");
                        }
                        Uri imageUri = Uri.fromFile(file);
                        mTakePhoto.onPickFromCapture(imageUri);
                    }
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
                    Intent intent = new Intent();
                    intent.setType("video/*"); //选择视频（mp4 3gp 是android支持的视频格式）
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, RichEditor.VIDEO_REQUEST);
                    break;
                case VideoSelecctDialog.BY_CAMERA:
                    Intent camera = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    camera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    startActivityForResult(camera, RichEditor.VIDEO_REQUEST);
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

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //第二个参数是需要申请的权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //权限已经被授予，在这里直接写要执行的相应方法即可
                hasPermission = true;
            } else {
                //权限还没有授予，需要在这里写申请权限的代码
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //第二个参数是需要申请的权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //权限已经被授予，在这里直接写要执行的相应方法即可
                hasPermission = true;
            } else {
                //权限还没有授予，需要在这里写申请权限的代码
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, 2);
            }
        } else {
            hasPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            } else {
                // Permission Denied
                Toast.makeText(this, "请在应用权限管理中打开权限", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;

            } else {
                // Permission Denied
                Toast.makeText(this, "请在应用权限管理中打开权限", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        if (result.getImages() == null || result.getImages().size() == 0) {
            return;
        }
        //可以在这里选择上传至服务器得到url再加载url
        for (TImage tImage : result.getImages()) {
            String path = tImage.getOriginalPath();
            insertImagesSync(path);
        }
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
    }

    /**
     * 异步方式插入图片
     *
     * @param imagePath 图片路径
     */
    private void insertImagesSync(final String imagePath) {
        insertDialog.show();

        Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            try {

                //Log.i("NewActivity", "###imagePath="+imagePath);
                subscriber.onNext(imagePath);

                subscriber.onCompleted();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
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

        Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            try {
                subscriber.onNext(videoPath);
                subscriber.onCompleted();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        insertDialog.dismiss();
                        mContent.addEditTextAtIndex(mContent.getLastIndex(), " ");
                        Toast.makeText(MainActivity.this, "视频插入成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        insertDialog.dismiss();
                        Toast.makeText(MainActivity.this, "视频插入失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                case RichEditor.VIDEO_REQUEST:
                    Uri uriVideo = data.getData();
                    // 转化为路径
                    String mVideoPath = UriUtil.getPath(this, uriVideo);
                    Bitmap bitmap = ImageUtils.getFirstImg(mVideoPath);
                    String firstImgPath = ImageUtils.saveFirstBitmap(bitmap);
                    insertVideosSync(mVideoPath, firstImgPath);
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
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//裁剪剧中
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
