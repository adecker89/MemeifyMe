package com.adecker.memeifyme;

import android.content.Context;
import android.graphics.*;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by alex on 11/16/13.
 */
public class TextPreviewOverlay extends View {

	private static final String TAG = "TextOverlay";
	private final Rect faceRect;
	private final Bitmap doge;
	private String[] text = {"Wow", "What r\nyou doing", "so scare", "Concern", "plz no", "nevr scratch again"};
	private ArrayList<Point> layouts = new ArrayList<Point>();
	private ArrayList<TextPaint> paints = new ArrayList<TextPaint>();

	public TextPreviewOverlay(Context context, Rect faceRect) {
		super(context);
		doge = BitmapFactory.decodeResource(getResources(), R.drawable.doge);
		this.faceRect = faceRect;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		generatePaint();
		generatePoints();

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		overlayOnCanvas(canvas, false);
	}

	public void overlayOnCanvas(Canvas canvas, boolean adjustToCanvas) {
		Matrix matrix = new Matrix();
		if (adjustToCanvas) {

			matrix.postScale(canvas.getWidth() / (float) getWidth(), canvas.getHeight() / (float) getHeight());

			//matrix.mapPoints(null);
		}

		for (int i = 0; i < text.length; i++) {
			Point p = layouts.get(i);

			//matrix.mapPoints(newPoints);

			//canvas.setMatrix(matrix);

			canvas.drawBitmap(doge,null,faceRect,null);
			canvas.drawText(text[i], p.x, p.y, paints.get(i));
		}
	}

	public void generatePaint() {
		paints.clear();
		Random rnd = new Random();

		for (String str : text) {
			TextPaint paint = new TextPaint();
			paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

			DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
			paint.setTextSize((int) ((24 * displayMetrics.density) + 0.5));
			paints.add(paint);
		}
	}

	public void generatePoints() {
		DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
		int height = (int) ((24 * displayMetrics.density) + 0.5);

		Random random = new Random();
		layouts.clear();
		for (int i = 0; i < text.length; i++) {
			int width = (int) paints.get(i).measureText(text[i]);


			Point point;
			do {

				point = new Point(random.nextInt(getWidth() - width), random.nextInt(getHeight()));

			//} while (faceRect.contains(point.x, point.y));
			} while (Rect.intersects(faceRect, new Rect(point.x, point.y,point.x+width,point.y+height)));

			layouts.add(point);
		}
	}
}
