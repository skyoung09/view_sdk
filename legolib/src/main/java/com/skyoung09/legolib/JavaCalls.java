package com.skyoung09.legolib;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Java方法调用工具类
 *
 */
public class JavaCalls {
	private static final HashMap<Class<?>, Class<?>> PRIMITIVE_MAP = new HashMap<Class<?>, Class<?>>();

	static {
		PRIMITIVE_MAP.put(Boolean.class, Boolean.TYPE);
		PRIMITIVE_MAP.put(Byte.class, Byte.TYPE);
		PRIMITIVE_MAP.put(Character.class, Character.TYPE);
		PRIMITIVE_MAP.put(Short.class, Short.TYPE);
		PRIMITIVE_MAP.put(Integer.class, Integer.TYPE);
		PRIMITIVE_MAP.put(Float.class, Float.TYPE);
		PRIMITIVE_MAP.put(Long.class, Long.TYPE);
		PRIMITIVE_MAP.put(Double.class, Double.TYPE);
		PRIMITIVE_MAP.put(Boolean.TYPE, Boolean.TYPE);
		PRIMITIVE_MAP.put(Byte.TYPE, Byte.TYPE);
		PRIMITIVE_MAP.put(Character.TYPE, Character.TYPE);
		PRIMITIVE_MAP.put(Short.TYPE, Short.TYPE);
		PRIMITIVE_MAP.put(Integer.TYPE, Integer.TYPE);
		PRIMITIVE_MAP.put(Float.TYPE, Float.TYPE);
		PRIMITIVE_MAP.put(Long.TYPE, Long.TYPE);
		PRIMITIVE_MAP.put(Double.TYPE, Double.TYPE);
	}
	/**
	 * 调用类的指定方法
	 * @param targetInstance
	 * @param methodName
	 * @param args
	 * @return
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static <T> T callMethod(Object targetInstance, String methodName, Object[] args) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return callMethodOrThrow(targetInstance, methodName, args);
	}
	/**
	 * 调用类的指定方法
	 * @param targetInstance
	 * @param methodName
	 * @param args
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callMethodOrThrow(Object targetInstance, String methodName, Object[] args) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?> clazz = targetInstance.getClass();

		Method method = getDeclaredMethod(clazz, methodName, getParameterTypes(args));

		Object result = method.invoke(targetInstance, getParameters(args));

		return (T) result;
	}

	/**
	 * 设置属性值
	 * @param oObj
	 * @param aCl
	 * @param aField
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws NoSuchFieldError
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void setField(Object oObj, Class<?> aCl, String aField,Object value) 
			throws NoSuchFieldException, NoSuchFieldError, IllegalArgumentException, IllegalAccessException {
		Field localField = aCl.getDeclaredField(aField);
		localField.setAccessible(true);
		localField.set(oObj, value);
	}
	private static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>[] parameterTypes) throws NoSuchMethodException, SecurityException {
		Method[] methods = clazz.getDeclaredMethods();
		Method method = findMethodByName(methods, name, parameterTypes);
		if(method != null){
			method.setAccessible(true);
		}
		return method;
	}

	private static Method findMethodByName(Method[] list, String name, Class<?>[] parameterTypes) throws NoSuchMethodException {
		if (name == null) {
			throw new NullPointerException("Method name must not be null.");
		}

		Method[] arrayOfMethod = list;
		int j = list.length;
		for (int i = 0; i < j; i++) {
			Method method = arrayOfMethod[i];
			if ((method.getName().equals(name)) && (compareClassLists(method.getParameterTypes(), parameterTypes))) {
				return method;
			}
		}

		throw new NoSuchMethodException(name);
	}

	private static boolean compareClassLists(Class<?>[] a, Class<?>[] b) {
		if (a == null) {
			return (b == null) || (b.length == 0);
		}

		int length = a.length;

		if (b == null) {
			return length == 0;
		}

		if (length != b.length) {
			return false;
		}

		for (int i = length - 1; i >= 0; i--) {
			if ((a[i].isAssignableFrom(b[i])) || ((PRIMITIVE_MAP.containsKey(a[i])) && (((Class<?>) PRIMITIVE_MAP.get(a[i])).equals(PRIMITIVE_MAP.get(b[i]))))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 调用类的静态方法(使用类名)
	 * @param className
	 * @param methodName
	 * @param args
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callStaticMethodOrThrow(String className, String methodName, Object[] args) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		Class<?> clazz = Class.forName(className);
		Method method = getDeclaredMethod(clazz, methodName, getParameterTypes(args));

		Object result = method.invoke(null, getParameters(args));

		try {
			return (T) result;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 合并数组
	 * @param array
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object combineArray(Object aArrayLhs, Object aArrayRhs) {
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
	/**
	 * 调用类的静态方法(使用Class)
	 * @param clazz
	 * @param methodName
	 * @param args
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callStaticMethodOrThrow(Class<?> clazz, String methodName, Object[] args) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method method = getDeclaredMethod(clazz, methodName, getParameterTypes(args));

		Object result = method.invoke(null, getParameters(args));
		try {
			return (T) result;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 生成类的实例
	 * @param clazz
	 * @param args
	 * @return
	 */
	public static <T> T getInstance(Class<?> clazz, Object[] args) {
		try {
			return getInstanceOrThrow(clazz, args);
		} catch (Exception e) {
		}
		return null;
	}
	/**
	 * 生成类的实例
	 * @param clazz
	 * @param args
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getInstanceOrThrow(Class<?> clazz, Object[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<?> constructor = clazz.getConstructor(getParameterTypes(args));

		try {
			return (T) constructor.newInstance(getParameters(args));
		} catch (Exception e) {
			return null;
		}
	}

	private static Class<?>[] getParameterTypes(Object[] args) {
		Class<?>[] parameterTypes = null;

		if ((args != null) && (args.length > 0)) {
			parameterTypes = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				Object param = args[i];
				if ((param != null) && ((param instanceof JavaParam)))
					parameterTypes[i] = ((JavaParam<?>) param).clazz;
				else {
					parameterTypes[i] = (param == null ? null : param.getClass());
				}
			}
		}
		return parameterTypes;
	}

	private static Object[] getParameters(Object[] args) {
		Object[] parameters = null;

		if ((args != null) && (args.length > 0)) {
			parameters = new Object[args.length];
			for (int i = 0; i < args.length; i++) {
				Object param = args[i];
				if ((param != null) && ((param instanceof JavaParam)))
					parameters[i] = ((JavaParam<?>) param).obj;
				else {
					parameters[i] = param;
				}
			}
		}
		return parameters;
	}
	/**
	 * 根据方法名获得方法的属性
	 * @param object
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 */
	public static Method getDeclaredMethod(Object object, String methodName, Class<?>[] parameterTypes) {
		for (Class<?> clazz = object.getClass(); clazz != Object.class;) {
			try {
				return clazz.getDeclaredMethod(methodName, parameterTypes);
			} catch (Exception localException) {
				clazz = clazz.getSuperclass();
			}

		}

		return null;
	}
	/**
	 * 调用指定方法
	 * @param object
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 */
	public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
		Method method = getDeclaredMethod(object, methodName, parameterTypes);
		try {
			if (method != null) {
				method.setAccessible(true);

				return method.invoke(object, parameters);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * 根据成员变量名称获得实例成员变量的属性
	 * @param object
	 * @param name
	 * @return
	 */
	public static Field findField(Object object, String name) {
		return findField(object, name, null);
	}
	/**
	 * 根据成员变量名称和类型获得实例成员变量的属性
	 * @param object
	 * @param name
	 * @param type
	 * @return
	 */
	public static Field findField(Object object, String name, Class<?> type) {
		Class<?> searchType = object.getClass();
		while ((!Object.class.equals(searchType)) && (searchType != null)) {
			Field[] fields = searchType.getDeclaredFields();
			for (Field field : fields) {
				if (((name == null) || (name.equals(field.getName()))) && ((type == null) || (type.equals(field.getType())))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}
	/**
	 * 获得实例中指定的值
	 * @param obj
	 * @param clazz
	 * @param field
	 * @return
	 * @throws NoSuchFieldException
	 * @throws NoSuchFieldError
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object getField(Object obj, Class<?> clazz, String field) throws NoSuchFieldException, NoSuchFieldError,IllegalArgumentException, IllegalAccessException {
		Field localField = clazz.getDeclaredField(field);
		localField.setAccessible(true);
		return localField.get(obj);
	}
	/**
	 * Java接口调用参数定义
	 * @author hebin03
	 *
	 * @param <T>
	 */
	public static class JavaParam<T> {
		/**
		 * 参数属性
		 */
		public final Class<? extends T> clazz;
		/**
		 * 参数值
		 */
		public final T obj;
		/**
		 * 构造方法
		 * @param clazz
		 * @param obj
		 */
		public JavaParam(Class<? extends T> clazz, T obj) {
			this.clazz = clazz;
			this.obj = obj;
		}
	}
}