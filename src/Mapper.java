import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class Mapper {
    public static Object map(Class clazz, String jsonInput) throws Exception {
        return map(clazz, new Path(""), jsonInput);
    }

    private static Object map(Class clazz, Path basePath, String jsonInput) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        JSONObject jsonObj = new JSONObject(jsonInput);

        Constructor<?> ctor = clazz.getDeclaredConstructor();
        Object mappedJson = ctor.newInstance();

        for (Field field : fields) {
            Path path = getPath(basePath, field);
            if (field.getType() == String.class)
                field.set(mappedJson, applyPath(jsonObj, path));
            else
                field.set(mappedJson, map(field.getType(), path, jsonInput));
        }

        return mappedJson;
    }

    private static Path getPath(Path basePath, Field field) {
        JsonAnnotation annotation = (JsonAnnotation) field.getAnnotation(JsonAnnotation.class);
        if (annotation == null)
            return Path.append(basePath, field.getName());
        else if (Path.isLeaf(annotation.value()))
            return Path.append(basePath, annotation.value());
        else
            return Path.append(basePath, annotation.value() + field.getName());
    }

    private static Object applyPath(JSONObject jsonObj, Path path) {
        try {
            for (int i = 0; i < path.segments.length - 1; i++)
                jsonObj = jsonObj.getJSONObject(path.segments[i]);
            return jsonObj.get(path.segments[path.segments.length - 1]);
        } catch (JSONException e) {
        }
        return null;
    }

    public static void printObject(Object mappedJson) throws Exception {
        printObject(0, mappedJson);
    }

    private static void printObject(int indent, Object mappedJson) throws Exception {
        for (Field field : mappedJson.getClass().getDeclaredFields())
            if (field.getType() == String.class)
                printField(indent, field.getName(), field.get(mappedJson));
            else {
                printField(indent, field.getName());
                printObject(indent + 2, field.get(mappedJson));
            }
    }

    private static void printField(int indent, String name) {
        System.out.printf("%" + (indent + 1) + "s %-10s\n", "", name);
    }

    private static void printField(int indent, String name, Object value) {
        System.out.printf("%" + (indent + 1) + "s %-10s: %s\n", "", name, value);
    }
}
