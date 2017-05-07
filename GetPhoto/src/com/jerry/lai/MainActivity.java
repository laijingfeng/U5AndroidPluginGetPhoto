package com.jerry.lai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	ImageView img1 = null;
	TextView text1 = null;

	ImageView img2 = null;
	TextView text2 = null;

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
		setContentView(R.layout.activity_main);

		addBtn();
		intImgText();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void addBtn() {
		Button btnCamera = (Button) findViewById(R.id.btnCamera);
		Button btnPhoto = (Button) findViewById(R.id.btnPhoto);
		Button btnPop = (Button) findViewById(R.id.btnPop);

		Button.OnClickListener btnListener = new Button.OnClickListener() {
			public void onClick(View arg0) {
				switch (arg0.getId()) {
				case R.id.btnCamera:
					openCamera();
					break;
				case R.id.btnPhoto:
					openPhoto();
					break;
				case R.id.btnPop:
					CharSequence[] items = { "Camera", "Photo" };
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("Select Photo Source")
							.setItems(items,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											if (which == 0) {
												openCamera();
											} else {
												openPhoto();
											}
										}
									}).create().show();
					break;
				default:
					break;
				}
			}
		};

		btnCamera.setOnClickListener(btnListener);
		btnPhoto.setOnClickListener(btnListener);
		btnPop.setOnClickListener(btnListener);
	}

	private void intImgText() {
		img1 = (ImageView) findViewById(R.id.imageView1);
		img2 = (ImageView) findViewById(R.id.imageView2);
		text1 = (TextView) findViewById(R.id.textView1);
		text2 = (TextView) findViewById(R.id.textView2);
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
		} else if (requestCode == REQ_CODE.CAMERA.getValue() && resultCode == RESULT_OK) {
			path = Environment.getExternalStorageDirectory() + "/temp.jpg";
		}

		if (path == null) {
			Toast.makeText(MainActivity.this, "Have No Photo Selected",
					Toast.LENGTH_SHORT).show();
			return;
		}

		Bitmap bmp = getCompressBitmap(path);

		text1.setText(path);
		if (img1 != null) {
			img1.setImageBitmap(bmp);
		}

		String newPath = path2SaveImg + "/" + saveImgName;
		text2.setText(newPath);
		Bitmap bmp2 = BitmapFactory.decodeFile(newPath);
		if (img2 != null) {
			img2.setImageBitmap(bmp2);
		}
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