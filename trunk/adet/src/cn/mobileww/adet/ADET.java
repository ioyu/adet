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

package cn.mobileww.adet;

import cn.mobileww.adet.graphics.MyResources;
import cn.mobileww.adet.util.ReflectUtil;
import android.content.Context;
import android.content.pm.ApplicationInfo;


/**
 * @author Manfeel
 * the Android Drawable Extension Toolkit
 * the ONLY static method init() we should to call in the onCreate() method 
 * of launch activity
 */
public class ADET {
	
	/**
	 * calling the very simple method to init our ADET
	 * @param c the Context instance
	 */
	public static void init(Context c) {

		Class<?> clzActivityThread = null;

		try {
			clzActivityThread = Class.forName("android.app.ActivityThread");
		}catch (ClassNotFoundException ex) {
			throw new NullPointerException("Sorry! We can't find class android.app.ActivityThread!");
		}

		ReflectUtil ru = new ReflectUtil(clzActivityThread, null);
		// call the static currentActivityThread() method of ActivityThread,
		// we can obtain the instance of current ActivityThread
		ru.bindInstance(ru.invoke("currentActivityThread"));
		
		ApplicationInfo ai = c.getApplicationInfo();
		
		// XXX: not work for 4.0,not found getPackageInfoNoCheck(ai)
		Object loadedApk = ru.invoke("getPackageInfoNoCheck", ai);
		// in 4.0,we found public final LoadedApk peekPackageInfo(String packageName, boolean includeCode)
		if(loadedApk == null)
			loadedApk = ru.invoke("peekPackageInfo", ai.packageName, true);
			
		ReflectUtil _loadedApk = new ReflectUtil(loadedApk);
		// get original resources instance
		Object mResources = _loadedApk.get("mResources");
		
		if(mResources == null) {
			throw new NullPointerException("Something is Wrong! mResources shouldn't be NULL!");
		}
		
		// and set to MyResources
		MyResources mr = new MyResources(c);
		_loadedApk.set("mResources", mr);
	}
}
