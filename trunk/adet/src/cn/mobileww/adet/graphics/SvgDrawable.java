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

/**
 * 
 * original author: Pavel.B.Chernov (based on ideas of kushnarev)
 * @author Manfeel, Modified for pure drawable without underling bitmap
 */

import android.graphics.drawable.Drawable;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import org.apache.http.util.EncodingUtils;
import com.libsvg.SvgRaster;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class SvgDrawable extends Drawable {
	private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;

	// Load native libraries
	static {
		System.loadLibrary("svgandroid");
	}

	// libsvg-android rasterizer id
	private long mSvgId;

	private Paint mPaint = new Paint(DEFAULT_PAINT_FLAGS);

	private int mWidth = -1;
	private int mHeight = -1;

	/**
	 * Creates a new SvgDrawable object and loads SVG by resource ID
	 */
	static public SvgDrawable getDrawable(Resources res, int id) {
		InputStream inStream = res.openRawResource(id);
		if (inStream == null) {
			return null;
		}

		SvgDrawable obj = new SvgDrawable(inStream);
		return obj;
	}

	/**
	 * Create a drawable by opening a given file path
	 * @throws FileNotFoundException 
	 */
	public SvgDrawable(String filepath) throws FileNotFoundException {
		FileInputStream f = new FileInputStream(filepath);
		LoadSvg(f);
	}

	/**
	 * Create a drawable by decoding a bitmap from the given input stream.
	 */
	public SvgDrawable(java.io.InputStream is) {
		LoadSvg(is);
	}

	/**
	 * Decodes a drawable from the given input stream.
	 */
	public boolean LoadSvg(java.io.InputStream is) {
		try {
			// Read into the buffer
			if (is != null) {
				byte[] buffer = new byte[is.available()];
				is.read(buffer);
				is.close();

				// And convert to utf8 string
				String content = EncodingUtils.getString(buffer, "UTF-8");
				buffer = null;

				return LoadSvg(content);
			}
		} catch (IOException ex) {

		}
		return false;
	}

	/**
	 * Decodes a drawable from the given content.
	 */
	public boolean LoadSvg(String content) {
		// Parse it
		mSvgId = SvgRaster.svgAndroidCreate();
		if (SvgRaster.svgAndroidParseBuffer(mSvgId, content) != 0) {
			android.util.Log.w("SvgDrawable", "SvgDrawable cannot decode!");
			return false;
		}

		SvgRaster.svgAndroidSetAntialiasing(mSvgId, true);

		mWidth = SvgRaster.svgAndroidGetWidth(mSvgId);
		mHeight = SvgRaster.svgAndroidGetHeight(mSvgId);
		return true;
	}

	public final Paint getPaint() {
		return mPaint;
	}

	public void setAntiAlias(boolean aa) {
		com.libsvg.SvgRaster.svgAndroidSetAntialiasing(mSvgId, aa);
	}

	@Override
	public void setFilterBitmap(boolean filter) {
		mPaint.setFilterBitmap(filter);
	}

	@Override
	public void setDither(boolean dither) {
		mPaint.setDither(dither);
	}

	@Override
	public void draw(Canvas canvas) {
		Rect bounds = this.getBounds();
		int dw = bounds.width();
		int dh = bounds.height();

		//canvas.drawLine(0, 0, dw, dh, mPaint);

		//canvas.scale(0.5f, 0.5f);
		// render the svg direct to drawable's canvas
		SvgRaster.svgAndroidRenderToArea(mSvgId, canvas, 0, 0, dw, dh);

	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getIntrinsicWidth() {
		return mWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return mHeight;
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void finalize() throws Throwable {
		SvgRaster.svgAndroidDestroy(mSvgId);
		super.finalize();
	}
}