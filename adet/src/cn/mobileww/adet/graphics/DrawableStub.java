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

import cn.mobileww.adet.util.ReflectUtil;

import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.Xml;

public class DrawableStub {
	public static Drawable createFromXml(Resources r, XmlPullParser parser) throws XmlPullParserException, IOException {
		AttributeSet attrs = Xml.asAttributeSet(parser);

		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
			// Empty loop
		}

		if (type != XmlPullParser.START_TAG) {
			throw new XmlPullParserException("No start tag found");
		}

		Drawable drawable = createFromXmlInner(r, parser, attrs);

		if (drawable == null) {
			throw new RuntimeException("Unknown initial tag: " + parser.getName());
		}

		return drawable;
	}

	/**
	* Create from inside an XML document.  Called on a parser positioned at
	* a tag in an XML document, tries to create a Drawable from that tag.
	* Returns null if the tag is not a valid drawable.
	*/
	public static Drawable createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs)
			throws XmlPullParserException, IOException {
		Drawable drawable;

		final String name = parser.getName();
		// XXX: we can add our custom drawable tags here  
		if (name.equals("selector")) {
			drawable = new MyStateListDrawable();
		} else if (name.equals("level-list")) {
			drawable = new LevelListDrawable();
		} else if (name.equals("layer-list")) {
			drawable = new MyLayerDrawable();
		} else if (name.equals("svg")) {
			drawable = new SvgDrawable(); // custom svg wrapper drawable
		} else if (name.equals("transition")) {
			drawable = ReflectUtil.New(TransitionDrawable.class);//new TransitionDrawable();
		} else if (name.equals("color")) {
			drawable = new ColorDrawable();
		} else if (name.equals("shape")) {
			drawable = new GradientDrawable();
		} else if (name.equals("scale")) {
			drawable = ReflectUtil.New(ScaleDrawable.class);//new ScaleDrawable();
		} else if (name.equals("clip")) {
			drawable = ReflectUtil.New(ClipDrawable.class);//new ClipDrawable();
		} else if (name.equals("rotate")) {
			drawable = new RotateDrawable();
		} else if (name.equals("animated-rotate")) {
			drawable = ReflectUtil.New("android.graphics.drawable.AnimatedRotateDrawable");//new AnimatedRotateDrawable();            
		} else if (name.equals("animation-list")) {
			drawable = new AnimationDrawable();
		} else if (name.equals("inset")) {
			drawable = ReflectUtil.New(InsetDrawable.class);//new InsetDrawable();
		} else if (name.equals("bitmap")) {
			drawable = new BitmapDrawable();
			if (r != null) {
				((BitmapDrawable) drawable).setTargetDensity(r.getDisplayMetrics());
			}
		} else if (name.equals("nine-patch")) {
			drawable = ReflectUtil.New(NinePatchDrawable.class);//new NinePatchDrawable();
			if (r != null) {
				((NinePatchDrawable) drawable).setTargetDensity(r.getDisplayMetrics());
			}
		} else {
			throw new XmlPullParserException(parser.getPositionDescription() + ": invalid drawable tag " + name);
		}

		drawable.inflate(r, parser, attrs);
		return drawable;
	}
}
