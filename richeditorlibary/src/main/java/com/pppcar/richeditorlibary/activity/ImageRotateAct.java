package com.pppcar.richeditorlibary.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.pppcar.richeditorlibary.R;
import com.pppcar.richeditorlibary.utils.ImageUtils;
import com.pppcar.richeditorlibary.utils.ScreenUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 作者:  Logan on 2017/11/30.
 * 邮箱:  490636907@qq.com
 * 描述:  图片旋转界面
 */

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
            //本地路径
            int width = ScreenUtils.getScreenWidth(this);
            int height = ScreenUtils.getScreenHeight(this);
            mSmallBitmap = ImageUtils.getSmallBitmap(mImagePath, width, height);
            if (mPic != null && mSmallBitmap != null) {
                mPic.setImageBitmap(mSmallBitmap);
            }
        } else {
            Glide.with(this).load(imagePath).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    mSmallBitmap = resource;
                    mPic.setImageBitmap(resource);
                }
            }); //方法中设置asBitmap可以设置回调类型
        }

    }

    private void initEvent() {
        mRotate.setOnClickListener(new View.OnClickListener() {
            private Bitmap mNewBitmap;

            @Override
            public void onClick(View v) {
                if (mSmallBitmap != null) {

                    mNewBitmap = adjustPhotoRotation(mSmallBitmap, 90);
                    mPic.setImageBitmap(mNewBitmap);
                    mSmallBitmap = mNewBitmap;
                }
            }
        });

        mSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSmallBitmap != null) {
                    String s = ImageUtils.saveBitmap(mSmallBitmap, mIndex);
                    if ("保存出错".equals(s)) {
                        Toast.makeText(ImageRotateAct.this, "临时图片保存出错", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Intent intent=new Intent();
                        intent.putExtra("index",mIndex);
                        intent.putExtra("imagePath",s);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }


            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mCancel = (ImageButton) findViewById(R.id.ib_cancel);
        mSure = (TextView) findViewById(R.id.sure);
        mRotate = (ImageButton) findViewById(R.id.ib_rotate);
        mPic = (ImageView) findViewById(R.id.iv);
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

    /**
     * 根据图片的url路径获得Bitmap对象
     *
     * @param url
     * @return
     */
    private Bitmap decodeUriAsBitmapFromNet(String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;

        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
