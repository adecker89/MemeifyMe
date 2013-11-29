package com.adecker.memeifyme;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by alex on 11/16/13.
 */
public class CapturedPreviewFragment extends Fragment {


	private String[] text = {"Wow", "What r\nyou doing", "so scare", "Concern", "plz no", "nevr scratch again"};
	private Rect faceRect;
	private ArrayList<TextView> blurbs = new ArrayList<TextView>();
	private Bitmap bm;
	private TextDragLayout mTextOverlay;
	private MemeOverlay mMemeOverlay;
	private FrameLayout mOverlayFrame;
	private ImageView mImageView;

	public CapturedPreviewFragment(Bitmap bm, Rect faceRect) {
		this.bm = bm;
		this.faceRect = faceRect;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_capture, container, false);

		mImageView = (ImageView) rootView.findViewById(R.id.image);
		mImageView.setImageBitmap(bm);

		mTextOverlay = new TextDragLayout(getActivity());
		mMemeOverlay = new MemeOverlay(getActivity(), faceRect);

		mOverlayFrame = (FrameLayout) rootView.findViewById(R.id.text_overlay);
		mOverlayFrame.addView(mMemeOverlay);
		mOverlayFrame.addView(mTextOverlay);


		Button captureButton = (Button) rootView.findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						shareBitmap(bm);
					}
				}
		);


		Random rnd = new Random();
		for (String str : text) {
			TextView tv = new TextView(getActivity());
			tv.setTextSize(32);
			tv.setText(str);
			tv.setTextColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
			tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));

			blurbs.add(tv);
			mTextOverlay.addView(tv);
		}

		return rootView;
	}

	public void shareBitmap(Bitmap bm) {
		Intent intent = new Intent();

		intent.setAction(Intent.ACTION_SEND);

		intent.setType("image/jpg");


		Uri uri = storeImage(compositeBitmap(bm));

		intent.putExtra(Intent.EXTRA_STREAM, uri);

		startActivity(intent);
	}

	private Uri storeImage(Bitmap bm) {
		File file = getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
			bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);

			bos.flush();
			bos.close();
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Uri.parse("file://" + file.getAbsolutePath());
	}

	/**
	 * Create a File for saving an image or video
	 */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "Doge");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("Doge", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_" + timeStamp + ".jpg");
		} else if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	private Bitmap compositeBitmap(Bitmap bm) {
		Bitmap mutable = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
		Canvas canvas = new Canvas(mutable);
		canvas.drawBitmap(bm, new Matrix(), null);

		Matrix matrix = new Matrix();
		matrix.postScale(canvas.getWidth() / (float) mImageView.getWidth(), canvas.getHeight() / (float) mImageView
				.getHeight());
		canvas.setMatrix(matrix);

		mMemeOverlay.draw(canvas);
		mTextOverlay.draw(canvas);
		return mutable;
	}
}
