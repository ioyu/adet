/*
 * Copyright (C) 2012 manfeel@foxmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.mobileww.adet.graphics;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import cn.mobileww.adet.R;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.util.Log;

public class SvgDrawable extends PictureDrawable {

	private final String TAG = "SvgDrawable";

	private SvgState mSvgState;

	public SvgDrawable() {
		super(null);
	}

	/**
	 * @param picture
	 */
	public SvgDrawable(SVG svg) {
		super(svg.getPicture());
		this.mSvgState = new SvgState(svg);
	}

	private void bindSvg(SVG svg) {
		setPicture(svg.getPicture());
		this.mSvgState = new SvgState(svg);
	}

	@Override
	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
		// get visible attribute
		super.inflate(r, parser, attrs);

		// inflate from <svg adet:src="..."> tag
		TypedArray ta = r.obtainAttributes(attrs, R.styleable.SvgWrapper);

		final int id = ta.getResourceId(R.styleable.SvgWrapper_src, 0);
		if (id == 0) {
			throw new XmlPullParserException(parser.getPositionDescription() + ": <svg> requires a valid src attribute");
		}

		// TODO: parse exp4j expressions and save to some structure?
		SVG svg = SVGParser.getSVGFromResource(r, id);
		bindSvg(svg);

		ta.recycle();
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		// TODO:if inflated from <svg>, then set picture to null, and in draw(), create picture from svg?
	}

	/**
	 * Android Canvas.drawPicture not working in devices with ice cream sandwich
	 * see : http://stackoverflow.com/questions/10384613/android-canvas-drawpicture-not-working-in-devices-with-ice-cream-sandwich
	 * it works perfectly in devices with API<14 (SDK 4.0), 
	 * but in devices with Ice Cream Sandwich, it doesn't work. 
	 * Apparently this is because canvas.drawPicture is not 
	 * supported with Hardware Acceleration
	 * I had the same problem and solved by programmatically turning off 
	 * hardware acceleration only on the view that will draw the Picture
	 * view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	 */
	@Override
	public void draw(Canvas canvas) {
		if (getPicture() != null) {
			Rect bounds = getBounds();
			canvas.save();
			// draw picture to fit bounds!
			canvas.drawPicture(getPicture(), bounds);
			canvas.restore();
		}
	}

//	@Override
//	public int getIntrinsicWidth() {
//		Rect bounds = getBounds();
//		RectF limits = mSvgState.mSvg.getLimits();
//		if (bounds != null) {
//			return (int) bounds.width();
//		} else if (limits != null) {
//			return (int) limits.width();
//		} else {
//			return -1;
//		}
//	}
//
//	@Override
//	public int getIntrinsicHeight() {
//		Rect bounds = getBounds();
//		RectF limits = mSvgState.mSvg.getLimits();
//		if (bounds != null) {
//			return (int) bounds.height();
//		} else if (limits != null) {
//			return (int) limits.height();
//		} else {
//			return -1;
//		}
//	}

	@Override
	public int getChangingConfigurations() {
		int c = super.getChangingConfigurations() | mSvgState.mChangingConfigurations;
		Log.e(TAG, "CC = " + c);
		return c;
	}

	@Override
	public ConstantState getConstantState() {
		mSvgState.mChangingConfigurations = super.getChangingConfigurations();
		return this.mSvgState;
	}

	final static class SvgState extends ConstantState {
		int mChangingConfigurations;
		private SVG mSvg;

		private SvgState(SVG svg) {
			this.mSvg = svg;
		}

		/* (non-Javadoc)
		 * @see android.graphics.drawable.Drawable.ConstantState#newDrawable()
		 */
		@Override
		public Drawable newDrawable() {
			return new SvgDrawable(mSvg);
		}

		/* (non-Javadoc)
		 * @see android.graphics.drawable.Drawable.ConstantState#getChangingConfigurations()
		 */
		@Override
		public int getChangingConfigurations() {
			return mChangingConfigurations;
		}

	}

}