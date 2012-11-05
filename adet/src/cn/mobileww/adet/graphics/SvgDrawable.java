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

import com.larvalabs.svgandroid.SVG;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.PictureDrawable;

public class SvgDrawable extends PictureDrawable {

	private SVG mSvg;
	private SvgState mSvgState = new SvgState(this);
	/**
	 * @param picture
	 */
	public SvgDrawable(SVG mSvg) {
		super(mSvg.getPicture());
		this.mSvg = mSvg;
	}
	
	
  @Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
	}


	@Override
  public void draw(Canvas canvas) {
      if (mSvg.getPicture() != null) {
          Rect bounds = getBounds();
          canvas.save();
          // draw picture to fit bounds!
          canvas.drawPicture(mSvg.getPicture(), bounds);
          canvas.restore();
      }
  }
	
	@Override
	public ConstantState getConstantState() {
		return this.mSvgState;
	}

	final static class SvgState extends ConstantState {
		private SvgDrawable mSvgDrawable;

		private SvgState(SvgDrawable svgDrawable) {
			this.mSvgDrawable = svgDrawable;
		}
		
		/* (non-Javadoc)
		 * @see android.graphics.drawable.Drawable.ConstantState#newDrawable()
		 */
		@Override
		public Drawable newDrawable() {
			return mSvgDrawable;
		}

		/* (non-Javadoc)
		 * @see android.graphics.drawable.Drawable.ConstantState#getChangingConfigurations()
		 */
		@Override
		public int getChangingConfigurations() {
			return 0;
		}
		
	}
	
}