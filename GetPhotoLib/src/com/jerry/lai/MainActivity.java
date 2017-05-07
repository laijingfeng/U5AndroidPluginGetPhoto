package com.jerry.lai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity {

	public enum REQ_CODE {
		None(0), CAMERA(1), PHOTO(2);

		private int value;

		private REQ_CODE(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	};

	public final static String path2SaveImg = "/mnt/sdcard/Android/data/jerry.lai.com/files";
	public final static String saveImgName = "jerrylai.png";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/*
	 * U3D调用
	 */
	public void PickImage(String strType) {
		if (strType.equals("Camera")) {
			openCamera();
		} else if (strType.equals("Photo")) {
			openPhoto();
		}
	}

	private void openCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
				Environment.getExternalStorageDirectory(), "temp.jpg")));
		startActivityForResult(intent, REQ_CODE.CAMERA.getValue());
		// Environment.getExternalStorageDirectory()
		// "/mnt/sdcard/Android/data/jerry.lai.com/files"
	}

	private void openPhoto() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Select Photo"),
				REQ_CODE.PHOTO.getValue());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String path = null;

		if (requestCode == REQ_CODE.PHOTO.getValue() && resultCode == RESULT_OK) {
			Uri uri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			@SuppressWarnings("deprecation")
			Cursor cursor = managedQuery(uri, proj,// Which columns to return
					null,// WHERE clause; which rows to return (all rows)
					null,// WHERE clause selection arguments (none)
					null); // Order-by clause (ascending by name)
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();

			path = cursor.getString(column_index);
		} else if (requestCode == REQ_CODE.CAMERA.getValue()
				&& resultCode == RESULT_OK) {
			path = Environment.getExternalStorageDirectory() + "/temp.jpg";
		}

		if (path == null) {
			Toast.makeText(MainActivity.this, "Have No Photo Selected",
					Toast.LENGTH_SHORT).show();
			return;
		}

		getCompressBitmap(path);

		UnityPlayer.UnitySendMessage("Main Camera", "GetPhotoCallback",
				saveImgName);
	}

	/*
	 * 计算压缩比率，原图/需要
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}

		return inSampleSize;
	}

	public Bitmap getCompressBitmap(String path) {
		final BitmapFactory.Options options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, 400, 400);
		options.inJustDecodeBounds = false;

		Bitmap bm = BitmapFactory.decodeFile(path, options);

		try {
			saveBitmap(bm);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bm;
	}

	public void saveBitmap(Bitmap bitmap) throws IOException {

		FileOutputStream fOut = null;
		try {
			// 查看这个路径是否存在，
			// 如果并没有这个路径，
			// 创建这个路径
			File destDir = new File(path2SaveImg);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			fOut = new FileOutputStream(path2SaveImg + "/" + saveImgName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// 将Bitmap对象写入本地路径中，Unity在去相同的路径来读取这个文件
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
