package com.adecker.memeifyme;

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

/**
 * Created by alex on 11/16/13.
 */
public class CameraPreviewOverlay extends View implements Camera.FaceDetectionListener {

	private static final String TAG = "Overlay";
	private final Bitmap doge;
	private RectF faceDriverRectF = new RectF();
	private RectF faceViewRectF = new RectF();
	private Rect faceViewRect = new Rect();
	private Matrix faceConvertMatrix = new Matrix();
	private int orientation = 0;

	public CameraPreviewOverlay(Context context) {
		super(context);
		doge = BitmapFactory.decodeResource(getResources(), R.drawable.doge);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		overlayOnCanvas(canvas,false);
	}

	public void overlayOnCanvas(Canvas canvas, boolean adjustToCanvas) {


//		if (faceDriverRectF != null) {
//			if(adjustToCanvas) {
//				Matrix matrix = new Matrix();
//				matrix.postScale(canvas.getWidth() / (float)getWidth(), canvas.getHeight() / (float)getHeight());
//				//matrix.postTranslate(canvas.getWidth() / 2f, canvas.getHeight() / 2f);
//				faceDriverRectF = new RectF(faceViewRect);
//				matrix.mapRect(faceViewRectF,faceDriverRectF);
//				faceViewRectF.round(faceViewRect);
//			}
			canvas.drawBitmap(doge,null,faceViewRect,null);
//		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		faceConvertMatrix = getFaceMatrix(orientation);
	}

	@Override
	public void onFaceDetection(Camera.Face[] faces, Camera camera) {

		if (faces.length == 0) {
			Log.i(TAG, " No Face Detected! ");
			//faceDriverRectF = null;
		} else {
			Log.i(TAG, String.valueOf(faces.length) + " Face Detected :) ");
			faceDriverRectF = new RectF(faces[0].rect);

			faceConvertMatrix.mapRect(faceViewRectF, faceDriverRectF);
			faceViewRectF.round(faceViewRect);
			invalidate();
		}
	}

	private Matrix getFaceMatrix(int orientation) {
		Matrix matrix = new Matrix();
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
		// Need mirror for front camera.
		boolean mirror = (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
		matrix.setScale(mirror ? -1 : 1, 1);
		// This is the value for android.hardware.Camera.setDisplayOrientation.
		matrix.postRotate(orientation);
		// Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
		// UI coordinates range from (0, 0) to (width, height).
		matrix.postScale(getWidth() / 2000f, getHeight() / 2000f);

		//bumping up scale to cover head better
		matrix.postScale(1.8f,1.8f);
		matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);

		//move head up a bit
		//matrix.postTranslate(0, -getHeight()/10f);
		return matrix;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
		faceConvertMatrix = getFaceMatrix(orientation);
	}

	public Rect getRect() {
		return faceViewRect;
	}
}
