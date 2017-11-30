package com.pppcar.richeditorlibary.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 作者:  Logan on 2017/11/30.
 * 邮箱:  490636907@qq.com
 * 描述:  SD卡工具类
 */
public class SDCardUtil {
	public static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
	/**
	 * 检查是否存在SDCard
	 * @return
	 */
	public static boolean hasSdcard(){
		String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 获得文章图片保存路径
	 * @return
	 */
	public static String getPictureDir(){
		String imageCacheUrl = SDCardRoot + "XRichText" + File.separator ;
		File file = new File(imageCacheUrl);
		if(!file.exists())
			file.mkdir();  //如果不存在则创建
		return imageCacheUrl;
	}

	/**
	 * 图片保存到SD卡
	 * @param bitmap
	 * @return
	 */
	public static String saveToSdCard(Bitmap bitmap) {
		String imageUrl = getPictureDir() + System.currentTimeMillis() + "-";
		File file = new File(imageUrl);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}

	/**
	 * 判断是否是emoji表情
	 * @param codePoint
	 * @return
     */
	public static boolean isEmojiCharacter(char codePoint) {
		return !((codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD) || ((codePoint >= 0x20) && codePoint <= 0xD7FF)) || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
	}


	/** 保存方法 */
	public static String saveBitmap(Bitmap bmp) {
		if (bmp == null) {
//            showToast("保存出错");
			return "保存出错";
		}
		// 首先保存图片
//        File appDir = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/", "pppcar");
		String path= Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard/good/TempPic/";
		File appDir = new File(path);
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		String fileName = null;
		if (appDir.isDirectory()) {
			fileName = "tempRotate.jpg";
			File file = new File(appDir, fileName);
			if (file.exists()) {
				file.delete();
			}
			try {
				FileOutputStream fos = new FileOutputStream(file);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
//                showToast("文件未发现");
				e.printStackTrace();
			} catch (IOException e) {
//                showToast("保存出错");
			} catch (Exception e) {
//                showToast("保存出错");
				e.printStackTrace();
			}

		}
		return path+fileName;
//        return SAVE_REAL_PATH+"tempRotate.jpg";
	}

	/**
	 * 保存到指定路径，笔记中插入图片
	 * @param bitmap
	 * @param path
	 * @return
	 */
	public static String saveToSdCard(Bitmap bitmap, String path) {
		File file = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("文件保存路径："+ file.getAbsolutePath());
		return file.getAbsolutePath();
	}

	/** 删除文件 **/
	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists())
			file.delete(); // 删除文件
	}

	/**
	 * 根据Uri获取图片文件的绝对路径
	 */
	public static String getFilePathByUri(Context context, final Uri uri) {
		if (null == uri) {
			return null;
		}
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}

}
