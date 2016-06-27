package com.skyoung09.legolib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * ClassLoader注入辅助类，兼容各版本android系统
 * 
 */
public class ClassLoaderInjectHelper {
	/**
	 * 开始注入ClassLoader
	 * 
	 * @param parentClassLoader
	 * @param childClassLoader
	 * @param someClass
	 * @return
	 */
	public static InjectResult inject(ClassLoader parentClassLoader, ClassLoader childClassLoader, String someClass,
			boolean parentFirst) {
		if (parentClassLoader == null || childClassLoader == null) {
			return null;
		}

		boolean hasBaseDexClassLoader = true;
		try {
			Class.forName("dalvik.system.BaseDexClassLoader");
		} catch (ClassNotFoundException e) {
			hasBaseDexClassLoader = false;
		}
		if (!hasBaseDexClassLoader) {
			return injectBelowApiLevel14(parentClassLoader, childClassLoader, someClass, parentFirst);
		}
		return injectAboveEqualApiLevel14(parentClassLoader, childClassLoader, someClass, parentFirst);
	}

	private static InjectResult injectBelowApiLevel14(ClassLoader parentClassLoader, ClassLoader childClassLoader,
			String someClass, boolean parentFirst) {
		if (parentClassLoader == null || childClassLoader == null) {
			return null;
		}

		InjectResult result = null;

		try {
			PathClassLoader pathClassLoader = (PathClassLoader) parentClassLoader;
			DexClassLoader dexClassLoader = (DexClassLoader) childClassLoader;

			// 验证classloader有效
			dexClassLoader.loadClass(someClass);

			setField(pathClassLoader, PathClassLoader.class, "mPaths",
					appendArray(getField(pathClassLoader, PathClassLoader.class, "mPaths"), getField(dexClassLoader, DexClassLoader.class, "mRawDexPath")));
			if (parentFirst) {
				setField(pathClassLoader, PathClassLoader.class, "mDexs",
						combineArray(getField(pathClassLoader, PathClassLoader.class, "mDexs"), getField(dexClassLoader, DexClassLoader.class, "mDexs")));
				setField(pathClassLoader, PathClassLoader.class, "mFiles",
						combineArray(getField(pathClassLoader, PathClassLoader.class, "mFiles"), getField(dexClassLoader, DexClassLoader.class, "mFiles")));
				setField(pathClassLoader, PathClassLoader.class, "mZips",
						combineArray(getField(pathClassLoader, PathClassLoader.class, "mZips"), getField(dexClassLoader, DexClassLoader.class, "mZips")));
			} else {
				setField(pathClassLoader, PathClassLoader.class, "mDexs",
						combineArray(getField(dexClassLoader, DexClassLoader.class, "mDexs"), getField(pathClassLoader, PathClassLoader.class, "mDexs")));
				setField(pathClassLoader, PathClassLoader.class, "mFiles",
						combineArray(getField(dexClassLoader, DexClassLoader.class, "mFiles"), getField(pathClassLoader, PathClassLoader.class, "mFiles")));
				setField(pathClassLoader, PathClassLoader.class, "mZips",
						combineArray(getField(dexClassLoader, DexClassLoader.class, "mZips"), getField(pathClassLoader, PathClassLoader.class, "mZips")));
			}

			try {
				// 兼容http://sourceforge.jp/projects/gb-231r1-is01/scm/git/GB_2.3_IS01/blobs/03dd31c20a17df5659fe3710057ad4f4180dd90f/libcore/dalvik/src/main/java/dalvik/system/PathClassLoader.java
				@SuppressWarnings("unchecked")
				ArrayList<String> libPaths = (ArrayList<String>) getField(pathClassLoader, PathClassLoader.class, "libraryPathElements");
				String[] libArray = (String[]) getField(dexClassLoader, DexClassLoader.class, "mLibPaths");
				for (String path : libArray) {
					libPaths.add(path);
				}
				Collections.sort(libPaths, new Comparator<Object>() {

					@Override
					public int compare(Object object1, Object object2) {
						if (object1 instanceof String && object2 instanceof String) {
							return compareTieba((String) object1, (String) object2);
						}
						return 0;
					}
				});
			} catch (Exception e) {
				setField(
						pathClassLoader,
						PathClassLoader.class,
						"mLibPaths",
						combineArray(getField(pathClassLoader, PathClassLoader.class, "mLibPaths"), getField(dexClassLoader, DexClassLoader.class, "mLibPaths")));
			}
		} catch (NoSuchFieldException e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (NoSuchFieldError e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (IllegalAccessException e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (Throwable e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		}

		if (result == null) {
			result = makeInjectResult(true, null);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static InjectResult injectAboveEqualApiLevel14(ClassLoader parentClassLoader, ClassLoader childClassLoader,
			String someClass, boolean parentFirst) {
		if (parentClassLoader == null || childClassLoader == null) {
			return null;
		}

		InjectResult result = null;
		try {
			PathClassLoader pathClassLoader = (PathClassLoader) parentClassLoader;
			DexClassLoader dexClassLoader = (DexClassLoader) childClassLoader;
			// 验证classloader有效
			dexClassLoader.loadClass(someClass);

			Object pathClassLoaderPathList = getPathList(pathClassLoader);
			Object dexClassLoaderPathList = getPathList(dexClassLoader);

			Object dexElements = null;
			if (parentFirst) {
				dexElements = combineArray(getDexElements(pathClassLoaderPathList), getDexElements(dexClassLoaderPathList));
			} else {
				dexElements = combineArray(getDexElements(dexClassLoaderPathList), getDexElements(pathClassLoaderPathList));
			}

			setField(pathClassLoaderPathList, pathClassLoaderPathList.getClass(), "dexElements", dexElements);

			Object dexNativeLibraryDirs = combineArray(getNativeLibraryDirectories(pathClassLoaderPathList),
					getNativeLibraryDirectories(dexClassLoaderPathList));

			if (dexNativeLibraryDirs instanceof File[]) {
				Arrays.sort((File[]) dexNativeLibraryDirs, getSoPathComparator());
			} else if (dexNativeLibraryDirs instanceof List) {
				List<File> list = (List<File>) dexNativeLibraryDirs;
				Collections.sort(list, getSoPathComparator());
				dexNativeLibraryDirs = list;
			}

			setField(pathClassLoaderPathList, pathClassLoaderPathList.getClass(), "nativeLibraryDirectories", dexNativeLibraryDirs);
			// 6.0的ClassLoader里面的PathList类有变动，需要调用内部方法生成一个数组赋值
			if (Build.VERSION.SDK_INT >= 23) {
				Object systemNativeLibraryDirs = getField(pathClassLoaderPathList, pathClassLoaderPathList.getClass(), "systemNativeLibraryDirectories");
				List<File> listSystem = (List<File>) systemNativeLibraryDirs;
				List<File> listSelf = (List<File>) dexNativeLibraryDirs;
				List<File> listAll = new ArrayList<File>(listSelf);
				listAll.addAll(listSystem);
				Method method = pathClassLoaderPathList.getClass().getDeclaredMethod("makePathElements", List.class, File.class, List.class);
				method.setAccessible(true);
				Object object = method.invoke(pathClassLoaderPathList.getClass(), listAll, null, new ArrayList<IOException>());
				setField(pathClassLoaderPathList, pathClassLoaderPathList.getClass(), "nativeLibraryPathElements", object);
			}
		} catch (IllegalArgumentException e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (NoSuchFieldException e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (NoSuchFieldError e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (IllegalAccessException e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (SecurityException e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (NoSuchMethodException e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		} catch (Throwable e) {
			result = makeInjectResult(false, e);
			Log.e("ClassLoaderInjectHelper", e.getLocalizedMessage());
		}
		if (result == null) {
			result = makeInjectResult(true, null);
		}
		return result;
	}

	private static void setField(Object oObj, Class<?> aCl, String aField, Object value) throws NoSuchFieldException, NoSuchFieldError, IllegalArgumentException,
			IllegalAccessException {
		Field localField = aCl.getDeclaredField(aField);
		localField.setAccessible(true);
		localField.set(oObj, value);
	}

	private static Object getField(Object obj, Class<?> clazz, String field) throws NoSuchFieldException, NoSuchFieldError,IllegalArgumentException, IllegalAccessException {
		Field localField = clazz.getDeclaredField(field);
		localField.setAccessible(true);
		return localField.get(obj);
	}

	@SuppressWarnings("unchecked")
	private static Object combineArray(Object aArrayLhs, Object aArrayRhs) {
		if (aArrayLhs == null) {
			return aArrayRhs;
		}
		if (aArrayRhs == null) {
			return aArrayLhs;
		}
		if (aArrayLhs.getClass().isArray() && aArrayRhs.getClass().isArray()) {
			Class<?> localClass = aArrayLhs.getClass().getComponentType();
			int i = Array.getLength(aArrayLhs);
			int j = i + Array.getLength(aArrayRhs);
			Object result = Array.newInstance(localClass, j);
			for (int k = 0; k < j; k++) {
				if (k < i) {
					Array.set(result, k, Array.get(aArrayLhs, k));
				} else {
					Array.set(result, k, Array.get(aArrayRhs, k - i));
				}
			}
			return result;
		} else if (aArrayLhs instanceof List
				&& aArrayRhs instanceof List) {
			List<File> lList = (List<File>) aArrayLhs;
			List<File> rList = (List<File>) aArrayRhs;
			ArrayList<File> result = new ArrayList<File>(lList.size() + rList.size());
			result.addAll(lList);
			result.addAll(rList);
			return result;
		} else {
			return aArrayLhs;
		}

	}

	private static Object appendArray(Object array, Object value) {
		Class<?> localClass = array.getClass().getComponentType();
		int i = Array.getLength(array);
		int j = i + 1;
		Object localObject = Array.newInstance(localClass, j);
		for (int k = 0; k < j; k++) {
			if (k < i) {
				Array.set(localObject, k, Array.get(array, k));
			} else {
				Array.set(localObject, k, value);
			}
		}
		return localObject;
	}

	private static InjectResult makeInjectResult(boolean aResult, Throwable aT) {
		InjectResult ir = new InjectResult();
		ir.mIsSuccessful = aResult;
		ir.mErrMsg = (aT != null ? aT.getLocalizedMessage() : null);
		return ir;
	}

	private static Object getPathList(Object obj) throws IllegalArgumentException, NoSuchFieldException, NoSuchFieldError, IllegalAccessException, ClassNotFoundException {
		return getField(obj, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
	}

	private static Object getDexElements(Object obj) throws IllegalArgumentException, NoSuchFieldException, NoSuchFieldError, IllegalAccessException {
		return getField(obj, obj.getClass(), "dexElements");
	}

	private static Object getNativeLibraryDirectories(Object aParamObject) throws IllegalArgumentException, NoSuchFieldException, NoSuchFieldError, IllegalAccessException {
		return getField(aParamObject, aParamObject.getClass(), "nativeLibraryDirectories");
	}
	/**
	 * ClassLoader注入结果
	 * @author hebin03
	 *
	 */
	public static class InjectResult {
		/**
		 * 是否成功
		 */
		public boolean mIsSuccessful;
		/**
		 * 注入结果信息
		 */
		public String mErrMsg;
	}
	/**
	 * 使用上下文注入ClassLoader对象
	 * @param context
	 * @param classLoader
	 * @param someClass
	 * @return
	 */
	public static InjectResult inject2(Context context, ClassLoader classLoader, String someClass) {
		InjectResult result = null;

		try {
			// 验证classloader有效
			classLoader.loadClass(someClass);
						
			Object apk;
			if (Build.VERSION.SDK_INT <= 7) {
				// 在API 7以前，Context的实现是ApplicationContext
				apk = getField(context.getApplicationContext(), Class.forName("android.app.ApplicationContext"), "mPackageInfo");
			} else {
				// API 7之后，Context的实现是ContextImpl，实际是ContextWrapper.mBase
				Object baseContext = getField(context.getApplicationContext(), ContextWrapper.class, "mBase");
				apk = getField(baseContext, Class.forName("android.app.ContextImpl"), "mPackageInfo");
			}

			setField(apk, apk.getClass(), "mClassLoader", classLoader);
			result = makeInjectResult(true, null);
		} catch (IllegalArgumentException e) {
			result = makeInjectResult(false, e);
		} catch (IllegalAccessException e) {
			result = makeInjectResult(false, e);
		} catch (Throwable e) {
			result = makeInjectResult(false, e);
		}
		
		return result;
	}

	private static final Comparator<File> getSoPathComparator() {
		return new Comparator<File>() {

			@Override
			public int compare(File object1, File object2) {
				if (object1 != null && object2 != null) {
					return compareTieba(object1.getAbsolutePath(), object2.getAbsolutePath());
				}
				return 0;
			}
		};
	}

	private static final int compareTieba(String str1, String str2) {
		if (str1 != null && str2 != null) {
			int left = 0;
			int right = 0;
			if (str1.contains("com.baidu.tieba")) {
				left = -1;
			}
			if (str2.contains("com.baidu.tieba")) {
				right = -1;
			}
			return left - right;
		}
		return 0;
	}
}