package com.adecker.memeifyme;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.*;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class CameraPreviewFragment extends Fragment {

	private static final String TAG = "Fragment";
	private static final int PICTURE_RESULT = 12;
	private Camera mCamera;
	private CameraPreview mPreview;
	private CameraPreviewOverlay mOverlay;
	private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Rect faceRect = mOverlay.getRect();

			Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);

			Matrix mat = new Matrix();
			mat.postRotate(getPictureTakenRotation());
			mat.preScale(-1, 1);
			picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), mat, true);


			//picture = compositeBitmap(picture);
			getActivity().getFragmentManager().beginTransaction()
					.replace(R.id.container, new CapturedPreviewFragment(picture, faceRect))
					.addToBackStack(null)
					.commit();
		}
	};

	public CameraPreviewFragment() {
	}

	public int getPictureTakenRotation() {
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		int rotation = 0;
		switch (display.getRotation()) {
			case Surface.ROTATION_0: // This is display orientation
				rotation = 90;
				break;
			case Surface.ROTATION_90:
				rotation = 0;
				break;
			case Surface.ROTATION_180:
				rotation = 270;
				break;
			case Surface.ROTATION_270:
				rotation = 180;
				break;
		}
		return rotation;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		mCamera = getCameraInstance();
		setMaxPictureSize();
		//mCamera.enableShutterSound(false);
		int degrees = getCameraDisplayOrientation(getActivity(), Camera.CameraInfo.CAMERA_FACING_FRONT);
		mCamera.setDisplayOrientation(degrees);
		Log.i(TAG, "max faces: " + mCamera.getParameters().getMaxNumDetectedFaces());

		mOverlay = new CameraPreviewOverlay(getActivity());
		mOverlay.setOrientation(degrees);
		mCamera.setFaceDetectionListener(mOverlay);
		mPreview = new CameraPreview(getActivity(), mCamera);

		FrameLayout frame = (FrameLayout) rootView.findViewById(R.id.camera_preview);
		frame.addView(mPreview);
		frame.addView(mOverlay);


		// Add a listener to the Capture button
		Button captureButton = (Button) rootView.findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// get an image from the camera
						mCamera.takePicture(null, null, mPicture);
					}
				}
		);


		return rootView;
	}

	@Override
	public void onStop() {
		super.onStop();
		mCamera.release();
	}

	public static int getCameraDisplayOrientation(Activity activity,
	                                              int cameraId) {
		android.hardware.Camera.CameraInfo info =
				new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}

		return result;
	}

	/**
	 * A safe way to get an instance of the Camera object.
	 */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
		} catch (Exception e) {
			Log.e(TAG, "camera unavailable", e);
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	public void setMaxPictureSize() {
		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
		Camera.Size max = sizes.get(0);
		for(Camera.Size size : sizes) {
			if(size.width > max.width || size.height > max.height) {
				max = size;
			}
		}

		parameters.setPictureSize(max.width,max.height);
		mCamera.setParameters(parameters);
	}


	private Bitmap compositeBitmap(Bitmap bm) {
		Bitmap mutable = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
		Canvas canvas = new Canvas(mutable);

		canvas.drawBitmap(bm, new Matrix(), null);

		mOverlay.overlayOnCanvas(canvas, true);

		return mutable;
	}
}
