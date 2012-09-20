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

import java.lang.ref.WeakReference;

import cn.mobileww.adet.util.ReflectUtil;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class MyResources extends Resources {

	private ReflectUtil _self;
	private TypedValue mTmpValue;
	private ReflectUtil _mDrawableCache;

	public MyResources(Context c) {
		super(c.getAssets(), c.getResources().getDisplayMetrics(), c.getResources().getConfiguration());
		_self = new ReflectUtil(Resources.class, c.getResources());
		mTmpValue = (TypedValue) _self.get("mTmpValue");
		_mDrawableCache = new ReflectUtil(_self.get("mDrawableCache"));
	}

//	private WeakReference<Drawable.ConstantState> drawableCache_get(long key) {
//
//	}
//
//	private Drawable getCachedDrawable(long key) {
//		synchronized (mTmpValue) {
//			WeakReference<Drawable.ConstantState> wr = drawableCache_get(key);
//			if (wr != null) { // we have the key
//				Drawable.ConstantState entry = wr.get();
//				if (entry != null) {
//					//Log.i(TAG, "Returning cached drawable @ #" +
//					//        Integer.toHexString(((Integer)key).intValue())
//					//        + " in " + this + ": " + entry);
//					return entry.newDrawable(this);
//				} else { // our entry has been purged
//					drawableCache.delete(key);
//				}
//			}
//		}
//		return null;
//	}

	private Drawable getCachedDrawable(TypedValue value) {
		// lookup cached drawables
		final long key = (((long) value.assetCookie) << 32) | value.data;
		// TODO: 4.0 and later has different version of get cached drawables
//		if (Build.VERSION.SDK_INT >= 14) {
//
//		} else {
			return (Drawable) _self.invoke("getCachedDrawable", key);
//		}
	}

	private void save(TypedValue value, Drawable dr) {
		final long key = (((long) value.assetCookie) << 32) | value.data;

		dr.setChangingConfigurations(value.changingConfigurations);
		Drawable.ConstantState cs = dr.getConstantState();
		if (cs != null) {
			if ((Boolean) _self.get("mPreloading")) {
				Object sPreloadedDrawables = _self.get("sPreloadedDrawables");
				ReflectUtil _sPreloadedDrawables = new ReflectUtil(sPreloadedDrawables);

				_sPreloadedDrawables.invoke("put", key, cs);
			} else {

				synchronized (mTmpValue) {
					_mDrawableCache.invoke("put", new Class[] { long.class, Object.class }, key,
							new WeakReference<Drawable.ConstantState>(cs));
				}
			}
		}
	}

	public Drawable loadDrawable(TypedValue value, int id) throws NotFoundException {

		Drawable dr = null;

		boolean isSvg = false;
		boolean isXml = false;

		// XXX: id == 0 but value.string is the last drawable value!
		// the original Drawable process ColorDrawable first!
		boolean isColorDrawable = value.type >= TypedValue.TYPE_FIRST_COLOR_INT
				&& value.type <= TypedValue.TYPE_LAST_COLOR_INT;
		
		if (isColorDrawable || value.string == null || !(isSvg = value.string.toString().endsWith(".svg"))
				&& !(isXml = value.string.toString().endsWith(".xml"))) {
			return (Drawable) _self.invoke("loadDrawable", value, id);
		}

		// first we should lookup cached drawables
		if ((dr = getCachedDrawable(value)) != null)
			return dr;

		if (isSvg) {
			// parse svg data and create picture drawable
			//			SVG svg = SVGParser.getSVGFromResource(this, id);
			//			dr = svg.createPictureDrawable();
			// Retrieve ID of the resource
			dr = SvgDrawable.getDrawable(this, id);

		} else if (isXml) {
			String file = value.string.toString();
			try {
				XmlResourceParser rp = (XmlResourceParser) _self.invoke("loadXmlResourceParser", file, id, value.assetCookie,
						"drawable");
				dr = DrawableStub.createFromXml(this, rp);
				rp.close();
			} catch (Exception e) {
				NotFoundException rnf = new NotFoundException("File " + file + " from drawable resource ID #0x"
						+ Integer.toHexString(id));
				rnf.initCause(e);
				throw rnf;
			}
		}
		// save drawable to cache
		if (dr != null) {
			save(value, dr);
		}

		return dr;
	}
}
