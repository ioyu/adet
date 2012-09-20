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

import cn.mobileww.adet.util.InternalR;
import cn.mobileww.adet.util.ReflectUtil;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;


/**
 * @author Manfeel
 * Custom LayerDrawable to give us a chance for
 * defining custom XML drawable tags
 *
 */
public class MyLayerDrawable extends LayerDrawable {

	ReflectUtil _self = new ReflectUtil(LayerDrawable.class, this);
	ReflectUtil _super = new ReflectUtil(Drawable.class, this);

	public MyLayerDrawable() {
		this(new Drawable[] {});
	}

	public MyLayerDrawable(Drawable[] layers) {
		super(layers);
	}

	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
		// XXX: manfeel notes: the "super" is LayerDrawable, but we want to call Drawable's inflate method,
		// so we shouldn't call LayerDrawable's inflate method here!  
		// super.inflate(r, parser, attrs);
		// _super.invoke cause recursive call!!!
		// _super.invoke("inflate", new Class[] {Resources.class, XmlPullParser.class, AttributeSet.class}, r, parser, attrs);
		TypedArray a = r.obtainAttributes(attrs, InternalR.styleable("Drawable"));

		_super.invoke("inflateWithAttributes", new Class[] { Resources.class, XmlPullParser.class, TypedArray.class,
				int.class }, r, parser, a, InternalR.styleable("Drawable", "visible"));

		a.recycle();

		int type;

		final int innerDepth = parser.getDepth() + 1;
		int depth;
		while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
				&& ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
			if (type != XmlPullParser.START_TAG) {
				continue;
			}

			if (depth > innerDepth || !parser.getName().equals("item")) {
				continue;
			}

			a = r.obtainAttributes(attrs, InternalR.styleable("LayerDrawableItem"));
			
			int left = a.getDimensionPixelOffset(InternalR.styleable("LayerDrawableItem", "left"), 0);
			int top = a.getDimensionPixelOffset(InternalR.styleable("LayerDrawableItem", "top"), 0);
			int right = a.getDimensionPixelOffset(InternalR.styleable("LayerDrawableItem", "right"), 0);
			int bottom = a.getDimensionPixelOffset(InternalR.styleable("LayerDrawableItem", "bottom"), 0);
			int drawableRes = a.getResourceId(InternalR.styleable("LayerDrawableItem", "drawable"), 0);
			int id = a.getResourceId(InternalR.styleable("LayerDrawableItem", "id"), View.NO_ID);

			a.recycle();

			Drawable dr;
			if (drawableRes != 0) {
				dr = r.getDrawable(drawableRes);
			} else {
				while ((type = parser.next()) == XmlPullParser.TEXT) {
				}
				if (type != XmlPullParser.START_TAG) {
					throw new XmlPullParserException(parser.getPositionDescription()
							+ ": <item> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
				}
				dr = DrawableStub.createFromXmlInner(r, parser, attrs);
			}

			_self.invoke("addLayer", new Class[] { Drawable.class, int.class, int.class, int.class, int.class, int.class },
					dr, id, left, top, right, bottom);
		}

		_self.invoke("ensurePadding");
		onStateChange(getState());
	}
}
