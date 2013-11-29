package com.adecker.memeifyme;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by alex on 11/16/13.
 */
public class TextDragLayout extends FrameLayout {

	private static final String TAG = "TextOverlay";
	private HashMap<View,Point> points = new HashMap<View,Point>();
	private Rect frame = new Rect();
	private View selectedView;


	public TextDragLayout(Context context) {
		super(context);
		this.points = new HashMap<View, Point>();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int i = 0; i < getChildCount(); i++) {
			final View child = getChildAt(i);

			final int width = child.getMeasuredWidth();
			final int height = child.getMeasuredHeight();

			Point point = getPointForView(child);

			child.layout(point.x,point.y,point.x+width,point.y+height);
		}
	}

	public Point getPointForView(View view) {
		Point point = points.get(view);
		if(point == null) {
			Random random = new Random();
			int width = view.getMeasuredWidth();
			int height = view.getMeasuredHeight();
			int r = getRight();
			int l = getLeft();
			int b = getBottom();
			int t = getTop();

			point = new Point(random.nextInt(r - width - l) + l, random.nextInt(b - height - t)+t);
			points.put(view,point);
		}
		return point;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				for (int i = 0; i < getChildCount(); i++) {
					final View child = getChildAt(i);
					child.getHitRect(frame);
					if(frame.contains((int)event.getX(),(int)event.getY())) {
						selectedView = child;
						break;
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(selectedView != null) {
					selectedView.setX(event.getX());
					selectedView.setY(event.getY());
				}
				break;
			case MotionEvent.ACTION_UP:
				points.put(selectedView,new Point((int)event.getX(),(int)event.getY()));
				this.invalidate();
				selectedView = null;
				break;
		}

		return true;
	}
}
