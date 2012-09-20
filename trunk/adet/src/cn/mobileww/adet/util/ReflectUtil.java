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

package cn.mobileww.adet.util;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;
import android.widget.TextView.SavedState;

/**
 * @author Manfeel
 * Reflection utility class
 */
public class ReflectUtil {

	private Class<?> clz;
	private Object inst;

	/**
	 * primitive types table
	 */
	private static HashMap<Class<?>, Class<?>> types = new HashMap<Class<?>, Class<?>>();

	private static HashMap<KeyOfCachedMethodOrField, SoftReference<Method>> cachedMethods = new HashMap<ReflectUtil.KeyOfCachedMethodOrField, SoftReference<Method>>();
	private static HashMap<KeyOfCachedMethodOrField, SoftReference<Field>> cachedFields = new HashMap<ReflectUtil.KeyOfCachedMethodOrField, SoftReference<Field>>();

	static {
		types.put(Boolean.class, boolean.class);
		types.put(Character.class, char.class);
		types.put(Byte.class, byte.class);
		types.put(Short.class, short.class);
		types.put(Integer.class, int.class);
		types.put(Long.class, long.class);
		types.put(Float.class, float.class);
		types.put(Double.class, double.class);
	}

	/**
	 * retrieve primitive type from wrapped class,
	 * if not, return the class itself
	 * @param wrapperClz
	 * @return
	 */
	private Class getRealType(Class wrapperClz) {
		if (types.containsKey(wrapperClz)) {
			return types.get(wrapperClz);
		}
		return wrapperClz;
	}

	public ReflectUtil(Class<?> clz, Object inst) {
		this.clz = clz;
		this.inst = inst;
	}

	public ReflectUtil(Object inst) {
		if (inst != null) {
			this.clz = inst.getClass();
			this.inst = inst;
		}
	}

	private Method getCachedMethod(String name, Class[] ptypes) {
		KeyOfCachedMethodOrField key = new KeyOfCachedMethodOrField(this.clz, name, ptypes);
		SoftReference<Method> wm = cachedMethods.get(key);
		if (wm == null)
			return null;

		return wm.get();
	}

	private Field getCachedField(String name) {
		KeyOfCachedMethodOrField key = new KeyOfCachedMethodOrField(this.clz, name);
		SoftReference<Field> wf = cachedFields.get(key);
		if (wf == null)
			return null;

		return wf.get();
	}

	private void save(String name, Class[] ptypes, Method m) {
		KeyOfCachedMethodOrField key = new KeyOfCachedMethodOrField(this.clz, name, ptypes);
		cachedMethods.put(key, new SoftReference<Method>(m));

		// debug purpose
//		for (KeyOfCachedMethodOrField k : cachedMethods.keySet()) {
//			SoftReference<Method> wm = cachedMethods.get(k); 
//			Log.i(wm == null ? "NULL" : wm.get().toString() , k.toString());
//		}
	}

	private void save(String name, Field f) {
		KeyOfCachedMethodOrField key = new KeyOfCachedMethodOrField(this.clz, name);
		cachedFields.put(key, new SoftReference<Field>(f));
		
		// debug purpose
//		for (KeyOfCachedMethodOrField k : cachedFields.keySet()) {
//			SoftReference<Field> wf = cachedFields.get(k); 
//			Log.i(wf == null ? "NULL" : wf.get().toString() , k.toString());
//		}

	}

	private Method getDeclaredMethod(String name, Class[] ptypes) {

		Method m = getCachedMethod(name, ptypes);

		if (m != null)
			return m;

		Class<?> current = clz;
		while (current != Object.class) {
			try {
				m = current.getDeclaredMethod(name, ptypes);
				break;
			} catch (NoSuchMethodException ex) {
				Log.w(name, "not found in " + current.getName() + " class level");
				current = current.getSuperclass();
			}
		}

		if (m != null) {
			m.setAccessible(true);
			//save(m);
			save(name, ptypes, m);
		}

		return m;
	}

	private Field getDeclaredField(String name) {
		Field f = getCachedField(name);

		if (f != null)
			return f;

		try {
			f = clz.getDeclaredField(name);
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
		}

		if (f != null) {
			f.setAccessible(true);
			//save(f);
			save(name, f);
		}

		return f;
	}

	public static Object New(String className) {
		Class<?> clz = null;
		try {
			clz = Class.forName(className);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (clz != null)
			return New(clz);

		return null;
	}

	public static Object New(Class<?> clz) {
		Object inst = null;
		try {
			Constructor c = clz.getDeclaredConstructor(new Class[] {});
			c.setAccessible(true);
			inst = c.newInstance(new Object[] {});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return inst;
	}

	/**
	 * bind to instance
	 * @param inst
	 */
	public void bindInstance(Object inst) {
		this.inst = inst;
	}

	public void printAllMethods() {
		try {
			Method[] ms = clz.getDeclaredMethods();
			for (Method m : ms) {
				System.out.println(m.getName());
				Class[] pts = m.getParameterTypes();
				for (Class pt : pts) {
					System.out.println("==>" + pt);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Object get(String fname) {
		Object val = null;

		Field f = getDeclaredField(fname);

		try {
			val = f.get(inst);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return val;
	}

	public void set(String fname, Object val) {
		try {
			Field f = getDeclaredField(fname);
			f.set(inst, val);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 调用不带参数的 mname 方法
	 * @param mname
	 * @return
	 */
	public Object invoke(String mname) {
		return invoke(mname, new Object[] {});
	}

	/**
	 * 调用mname指定的方法，其签名由params中的类型决定（有可能因为参数类型的转换而失败！）
	 * @param mname
	 * @param params
	 * @return
	 */
	public Object invoke(String mname, Object... params) {
		Object rst = null;
		try {
			Class[] ptypes = params.length > 0 ? new Class[params.length] : new Class[] {};
			for (int i = 0; i < params.length; i++) {
				Class c = params[i].getClass();
				ptypes[i] = getRealType(c);
			}

			rst = invoke(mname, ptypes, params);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rst;
	}

	/**
	 * 调用方法
	 * @param mname 方法名称
	 * @param ptypes 签名类型
	 * @param params 具体的参数
	 * @return
	 */
	public Object invoke(String mname, Class[] ptypes, Object... params) {
		Object rst = null;

		try {
			Method m = getDeclaredMethod(mname, ptypes);
			rst = m.invoke(inst, params);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rst;
	}

	static final class KeyOfCachedMethodOrField {
		private Class<?> clz;
		private String name;
		private Class[] ptypes;

		public KeyOfCachedMethodOrField(Class<?> clz, String name) {
			this.clz = clz;
			this.name = name;
			this.ptypes = null;
		}

		public KeyOfCachedMethodOrField(Class<?> clz, String name, Class[] ptypes) {
			this.clz = clz;
			this.name = name;
			this.ptypes = ptypes;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(clz.getName()).append(".").append(name).append(" ptypes ==> ");
			if (ptypes != null) {
				sb.append("\n");
				for (int i = 0; i < ptypes.length; i++) {
					sb.append("p").append(i).append(" = ").append(ptypes[i]).append("\n");
				}
			} else {
				sb.append("void\n");
			}
			return sb.toString();
		}

	}
}
