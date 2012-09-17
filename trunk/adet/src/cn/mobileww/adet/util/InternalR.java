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

package cn.mobileww.adet.util;

/**
 * @author Manfeel
 * the utility class for getting android internal styleable or attr values
 */
public class InternalR {

	private static Class<?> clz_styleable;
	private static Class<?> clz_attr;

	static {
		try {
			clz_styleable = Class.forName("android.R$styleable");
			clz_attr = Class.forName("android.R$attr");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static Object get(Class<?> clz, String name) {
		Object rst = null;

		if (clz == null)
			return rst;

		try {
			rst = clz.getField(name).get(null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rst;
	}

	private static int getInt(Class<?> clz, String name) {
		int rst = -1;

		if (clz == null)
			return rst;

		try {
			rst = clz.getField(name).getInt(null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rst;
	}

	public static int[] styleable(String name) {
		return (int[]) get(clz_styleable, name);
	}

	public static int styleable(String base, String sub) {
		String name = new StringBuilder(base).append("_").append(sub).toString();
		return getInt(clz_styleable, name);
	}

	public static int attr(String name) {
		return getInt(clz_attr, name);
	}
}
