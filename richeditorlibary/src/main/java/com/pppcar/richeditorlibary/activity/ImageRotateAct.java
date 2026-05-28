package com.pppcar.richeditorlibary.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pppcar.richeditorlibary.R;
import com.pppcar.richeditorlibary.utils.ImageUtils;
import com.pppcar.richeditorlibary.utils.ScreenUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings({"deprecation", "unused"})
public class ImageRotateAct extends Activity {

    private ImageButton mCancel;
    private TextView mSure;
    private ImageButton mRotate;
    private ImageView mPic;
    private String mImagePath;
    private Bitmap mSmallBitmap;
    private int mIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_rotate_img);
        initView();
        mImagePath = getIntent().getStringExtra("imagePath");
        mIndex = getIntent().getIntExtra("index", 0);
        checkImage(mImagePath);
        initEvent();
    }

    private void checkImage(String imagePath) {
        if (mImagePath.contains("content") || mImagePath.contains("storage") || mImagePath.contains("sdcard")) {
            int width = ScreenUtils.getScreenWidth(this);
            int height = ScreenUtils.getScreenHeight(this);
            mSmallBitmap = ImageUtils.getSmallBitmap(mImagePath, width, height);
            if (mPic != null && mSmallBitmap != null) {
                mPic.setImageBitmap(mSmallBitmap);
            }
        } else {
            Glide.with(this).asBitmap().load(imagePath).skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            mSmallBitmap = resource;
                            mPic.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
    }

    private void initEvent() {
        mRotate.setOnClickListener(v -> {
            if (mSmallBitmap != null) {
                int orientationDegree = 90;
                Bitmap newBitmap = adjustPhotoRotation(mSmallBitmap, orientationDegree);
                mPic.setImageBitmap(newBitmap);
                mSmallBitmap = newBitmap;
            }
        });

        mSure.setOnClickListener(v -> {
            if (mSmallBitmap != null) {
                String s = ImageUtils.saveBitmap(mSmallBitmap, mIndex);
                if ("保存出错".equals(s)) {
                    Toast.makeText(ImageRotateAct.this, "临时图片保存出错", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("index", mIndex);
                    intent.putExtra("imagePath", s);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });

        mCancel.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSmallBitmap != null && !mSmallBitmap.isRecycled()) {
            mSmallBitmap.recycle();
            mSmallBitmap = null;
        }
    }

    private void initView() {
        mCancel = findViewById(R.id.ib_cancel);
        mSure = findViewById(R.id.sure);
        mRotate = findViewById(R.id.ib_rotate);
        mPic = findViewById(R.id.iv);
    }

    private Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);

        return bm1;
    }
}
