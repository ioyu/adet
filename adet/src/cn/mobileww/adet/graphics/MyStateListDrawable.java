/*
 * Copyright (C) 2012 Manfeel
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
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.StateSet;


/**
 * @author Manfeel
 *
 */
public class MyStateListDrawable extends StateListDrawable {
	private ReflectUtil _self, _super;
	private ReflectUtil _mStateListState;

	public MyStateListDrawable() {
		_self = new ReflectUtil(StateListDrawable.class, this);
		_super = new ReflectUtil(Drawable.class, this);
		_mStateListState = new ReflectUtil(_self.get("mStateListState"));
	}

	@Override
	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {

		TypedArray a = r.obtainAttributes(attrs, InternalR.styleable("StateListDrawable"));

		_super.invoke("inflateWithAttributes", 
				new Class[] { Resources.class, XmlPullParser.class, TypedArray.class,	int.class }, 
				r, parser, a, InternalR.styleable("StateListDrawable", "visible"));

		_mStateListState.invoke("setVariablePadding",
				a.getBoolean(InternalR.styleable("StateListDrawable", "variablePadding"), false));

		_mStateListState.invoke("setConstantSize",
				a.getBoolean(InternalR.styleable("StateListDrawable", "constantSize"), false));

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

			int drawableRes = 0;

			int i;
			int j = 0;
			final int numAttrs = attrs.getAttributeCount();
			int[] states = new int[numAttrs];
			for (i = 0; i < numAttrs; i++) {
				final int stateResId = attrs.getAttributeNameResource(i);
				if (stateResId == 0)
					break;
				if (stateResId == InternalR.attr("drawable")) {
					drawableRes = attrs.getAttributeResourceValue(i, 0);
				} else {
					states[j++] = attrs.getAttributeBooleanValue(i, false) ? stateResId : -stateResId;
				}
			}
			states = StateSet.trimStateSet(states, j);

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

			_mStateListState.invoke("addStateSet", new Class[] {int[].class, Drawable.class}, states, dr);
		}

		onStateChange(getState());
	}

}
