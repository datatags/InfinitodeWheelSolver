package me.datatags.infinitodewheelsolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionUtils {
    private static final Map<Class<?>,Map<String,Field>> fields = new HashMap<>();
    private static final Map<Class<?>,Map<String,Method>> methods = new HashMap<>();
    private ReflectionUtils() {}

    private static Field getField(Class<?> clazz, String fieldName) {
        return fields.computeIfAbsent(clazz, c -> new HashMap<>()).computeIfAbsent(fieldName, f -> {
            try {
                System.out.println("Computing new field " + clazz.getName() + "." + f);
                Field field = clazz.getDeclaredField(f);
                field.setAccessible(true);
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                return field;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static Method getMethod(Class<?> clazz, String methodName, String identifier, Object... params) {
        return methods.computeIfAbsent(clazz, c -> new HashMap<>()).computeIfAbsent(identifier, id -> {
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
                Method method = clazz.getDeclaredMethod(methodName, types);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get the value of a field from an object. The field may be private.
     * @param obj The object to get the field from.
     * @param fieldName The name of the field to get.
     * @return The current value of the field.
     * @param <T> The type of the field.
     */
    public static <T> T getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, obj.getClass(), fieldName);
    }

    /**
     * Get the value of a field. The field may be private.
     * @param obj The object to get the field value from. May be null if the field is static.
     * @param clazz The class the field resides in. If {@code obj} is not null, {@code clazz == obj.getClass()}
     * @param fieldName The name of the field to get.
     * @return The current value of the field.
     * @param <T> The type of the field.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, Class<?> clazz, String fieldName) {
        try {
            return (T) getField(clazz, fieldName).get(obj);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the value of a field on an object. The field may be private and/or final.
     * @param obj The object to set the field value on.
     * @param fieldName The name of the field to set.
     * @param value The new value of the field.
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            getField(obj.getClass(), fieldName).set(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke a method on an object using reflection. The method may be private.
     * @param obj The object to invoke the method on.
     * @param methodName The name of the method.
     * @param identifier An arbitrary identifier of the method, to assist with caching. One method may have multiple
     *                   identifiers, but multiple methods cannot have the same identifier.
     * @param params The parameters to invoke the method with.
     * @return The result of the method.
     * @param <T> The result type of the method.
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object obj, String methodName, String identifier, Object... params) {
        try {
            return (T) getMethod(obj.getClass(), methodName, identifier, params).invoke(obj, params);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
