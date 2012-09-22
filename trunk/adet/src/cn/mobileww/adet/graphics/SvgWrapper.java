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
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.libsvg.SvgRaster;

import cn.mobileww.adet.R;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * @author Manfeel
 * the wrapper class of SVG drawable
 * this class is used in <svg-wrapper> tag
 */
public class SvgWrapper extends SvgDrawable {
	private float scaleX = 1.0f;
	private float scaleY = 1.0f;

	private int innerWidth, innerHeight;
	private int mWidth, mHeight;

	@Override
	public void draw(Canvas canvas) {
		// need to scale canvas
		//canvas.scale(scaleX, scaleY);
		if(mWidth == -1 || mHeight == -1) {
			Rect r = this.getBounds();
			int w = r.width();
			int h = r.height();
			SvgRaster.svgAndroidRenderToArea(getSvgId(), canvas, 0, 0, w, h);
		} else {
			SvgRaster.svgAndroidRenderToArea(getSvgId(), canvas, 0, 0, mWidth, mHeight);
		}
	}

	@Override
	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
		super.inflate(r, parser, attrs);
		
		TypedArray ta = r.obtainAttributes(attrs, R.styleable.SvgWrapper);
		
    final int id = ta.getResourceId(R.styleable.SvgWrapper_src, 0);
    if (id == 0) {
        throw new XmlPullParserException(parser.getPositionDescription() + ": <svg-wrapper> requires a valid src attribute");
    }		
		
    // load svg from id
    InputStream is = r.openRawResource(id);
    LoadSvg(is);
    
    innerWidth = this.getIntrinsicWidth();
    innerHeight = this.getIntrinsicHeight();
    
		mWidth = ta.getDimensionPixelSize(R.styleable.SvgWrapper_width, -1);
		mHeight = ta.getDimensionPixelSize(R.styleable.SvgWrapper_height, -1);

		scaleX = (float)mWidth / innerWidth;
		scaleY = (float)mHeight / innerHeight;
		
		ta.recycle();
	}
}
