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
package cn.mobileww.adet.sample;

import cn.mobileww.adet.ADET;
import android.app.Application;

/**
 * @author Manfeel
 *
 */
public class MyApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// call this init method at your application once, and we done!
		ADET.init(getApplicationContext());
	}

}
