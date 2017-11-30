package com.pppcar.richeditor;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

/**
 * Created by nurmemet on 2015/8/28.
 */

public class PictureSelecctDialog extends Dialog {

    final public static   int FROM_ALBUM=1;
    final public static   int TAKE_PICTURE=2;

    Button mFromAlbum;
    Button mTakePhoto;

    private View.OnClickListener onClickListener;

    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    public PictureSelecctDialog(Context context, View.OnClickListener clickListener) {
        super(context, R.style.select_picture_dialog);
        this.onClickListener = clickListener;
        this.setContentView(R.layout.image_select_dialog);
        mFromAlbum = (Button) findViewById(R.id.from_album);
        mFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(FROM_ALBUM);
                onClickListener.onClick(v);
                dismiss();
            }
        });
        mTakePhoto = (Button) findViewById(R.id.take_photo);
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(TAKE_PICTURE);
                onClickListener.onClick(v);
                dismiss();
            }
        });


    }

    @Override
    public void dismiss() {
        super.dismiss();
    }


}
