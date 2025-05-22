package me.datatags.infinitodewheelsolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtils {
    private ReflectionUtils() {}

    public static <T> T getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, obj.getClass(), fieldName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object obj, String methodName, Object... params) {
        try {
            Class<?>[] types = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                try {
                    // Unbox if we can, i.e. java.lang.Float.class -> float.class
                    types[i] = (Class<?>) params[i].getClass().getDeclaredField("TYPE").get(params[i].getClass());
                    continue;
                } catch (ReflectiveOperationException ignored) {
                }
                // Otherwise just use the field as-is
                types[i] = params[i].getClass();
            }
            Method method = obj.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return (T) method.invoke(obj, params);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
