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

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.PictureDrawable;

public class SvgDrawable extends PictureDrawable {

	private Picture mPicture;
	/**
	 * @param picture
	 */
	public SvgDrawable(Picture picture) {
		super(picture);
		this.mPicture = picture;
	}
	
  @Override
  public void draw(Canvas canvas) {
      if (mPicture != null) {
          Rect bounds = getBounds();
          canvas.save();
          // draw picture to fit bounds!
          canvas.drawPicture(mPicture, bounds);
          canvas.restore();
      }
  }
	
}