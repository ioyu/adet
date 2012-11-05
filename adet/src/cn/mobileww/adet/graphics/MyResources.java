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

import java.io.InputStream;
import java.lang.ref.WeakReference;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import cn.mobileww.adet.util.LongSparseArray;
import cn.mobileww.adet.util.ReflectUtil;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;

public class MyResources extends Resources {

	static final String TAG = "MyResources";
	private static final boolean DEBUG_LOAD = false;
	private static final boolean DEBUG_CONFIG = false;
	private static final boolean DEBUG_ATTRIBUTES_CACHE = false;
	private static final boolean TRACE_FOR_PRELOAD = false;
	private static final boolean TRACE_FOR_MISS_PRELOAD = false;

	// Information about preloaded resources.  Note that they are not
	// protected by a lock, because while preloading in zygote we are all
	// single-threaded, and after that these are immutable.
	private static final LongSparseArray<Drawable.ConstantState> sPreloadedDrawables = new LongSparseArray<Drawable.ConstantState>();
	private static final SparseArray<ColorStateList> mPreloadedColorStateLists = new SparseArray<ColorStateList>();
	private static final LongSparseArray<Drawable.ConstantState> sPreloadedColorDrawables = new LongSparseArray<Drawable.ConstantState>();
	private static boolean mPreloaded;

	final TypedValue mTmpValue = new TypedValue();

	// These are protected by the mTmpValue lock.
	private final LongSparseArray<WeakReference<Drawable.ConstantState>> mDrawableCache = new LongSparseArray<WeakReference<Drawable.ConstantState>>();
	private final SparseArray<WeakReference<ColorStateList>> mColorStateListCache = new SparseArray<WeakReference<ColorStateList>>();
	private final LongSparseArray<WeakReference<Drawable.ConstantState>> mColorDrawableCache = new LongSparseArray<WeakReference<Drawable.ConstantState>>();
	private boolean mPreloading;

	private ReflectUtil _self;
	private ReflectUtil _mDrawableCache;

	public MyResources(Context c) {
		super(c.getAssets(), c.getResources().getDisplayMetrics(), c.getResources().getConfiguration());
		_self = new ReflectUtil(Resources.class, c.getResources());
		_mDrawableCache = new ReflectUtil(_self.get("mDrawableCache"));
	}

	/*package*/Drawable loadDrawable(TypedValue value, int id) throws NotFoundException {

		if (TRACE_FOR_PRELOAD) {
			// Log only framework resources
			if ((id >>> 24) == 0x1) {
				final String name = getResourceName(id);
				if (name != null)
					android.util.Log.d("PreloadDrawable", name);
			}
		}

		final long key = (((long) value.assetCookie) << 32) | value.data;
		boolean isColorDrawable = false;
		if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			isColorDrawable = true;
		}
		Drawable dr = getCachedDrawable(isColorDrawable ? mColorDrawableCache : mDrawableCache, key);

		if (dr != null) {
			return dr;
		}

		Drawable.ConstantState cs = isColorDrawable ? sPreloadedColorDrawables.get(key) : sPreloadedDrawables.get(key);
		if (cs != null) {
			dr = cs.newDrawable();
		} else {
			if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
				dr = new ColorDrawable(value.data);
			}

			if (dr == null) {
				if (value.string == null) {
					throw new NotFoundException("Resource is not a Drawable (color or path): " + value);
				}

				String file = value.string.toString();

				if (TRACE_FOR_MISS_PRELOAD) {
					// Log only framework resources
					if ((id >>> 24) == 0x1) {
						final String name = getResourceName(id);
						if (name != null)
							android.util.Log.d(TAG, "Loading framework drawable #" + Integer.toHexString(id) + ": " + name + " at "
									+ file);
					}
				}

				if (DEBUG_LOAD)
					Log.v(TAG, "Loading drawable for cookie " + value.assetCookie + ": " + file);

				if (file.endsWith(".svg")) {
					SVG svg = SVGParser.getSVGFromResource(this, id);
					dr = new SvgDrawable(svg);
				} else if (file.endsWith(".xml")) {
					try {
						XmlResourceParser rp = (XmlResourceParser) _self.invoke("loadXmlResourceParser", file, id,
								value.assetCookie, "drawable");
						dr = DrawableStub.createFromXml(this, rp);
						rp.close();
					} catch (Exception e) {
						NotFoundException rnf = new NotFoundException("File " + file + " from drawable resource ID #0x"
								+ Integer.toHexString(id));
						rnf.initCause(e);
						throw rnf;
					}

				} else {
					try {
						// manfeel,2012-11-05
						// InputStream is = mAssets.openNonAsset(value.assetCookie, file, AssetManager.ACCESS_STREAMING);
						ReflectUtil ru = new ReflectUtil(_self.get("mAssets"));
						InputStream is = (InputStream) ru.invoke("openNonAsset", value.assetCookie, file,
								AssetManager.ACCESS_STREAMING);
						//                System.out.println("Opened file " + file + ": " + is);
						dr = Drawable.createFromResourceStream(this, value, is, file);
						is.close();
						//                System.out.println("Created stream: " + dr);
					} catch (Exception e) {
						NotFoundException rnf = new NotFoundException("File " + file + " from drawable resource ID #0x"
								+ Integer.toHexString(id));
						rnf.initCause(e);
						throw rnf;
					}
				}
			}
		}

		if (dr != null) {
			dr.setChangingConfigurations(value.changingConfigurations);
			cs = dr.getConstantState();
			if (cs != null) {
				if (mPreloading) {
					if (isColorDrawable) {
						sPreloadedColorDrawables.put(key, cs);
					} else {
						sPreloadedDrawables.put(key, cs);
					}
				} else {
					synchronized (mTmpValue) {
						//Log.i(TAG, "Saving cached drawable @ #" +
						//        Integer.toHexString(key.intValue())
						//        + " in " + this + ": " + cs);
						if (isColorDrawable) {
							mColorDrawableCache.put(key, new WeakReference<Drawable.ConstantState>(cs));
						} else {
							mDrawableCache.put(key, new WeakReference<Drawable.ConstantState>(cs));
						}
					}
				}
			}
		}

		return dr;
	}

	private Drawable getCachedDrawable(LongSparseArray<WeakReference<ConstantState>> drawableCache, long key) {
		synchronized (mTmpValue) {
			WeakReference<Drawable.ConstantState> wr = drawableCache.get(key);
			if (wr != null) { // we have the key
				Drawable.ConstantState entry = wr.get();
				if (entry != null) {
					//Log.i(TAG, "Returning cached drawable @ #" +
					//        Integer.toHexString(((Integer)key).intValue())
					//        + " in " + this + ": " + entry);
					return entry.newDrawable();
				} else { // our entry has been purged
					drawableCache.delete(key);
				}
			}
		}
		return null;
	}
}
