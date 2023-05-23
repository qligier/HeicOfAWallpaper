package ch.qligier.heicofawallpaper.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A GSON adapter for {@link LocalTime} instances.
 *
 * @author Quentin Ligier
 **/
public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects for any non-trivial
     * field of the returned object. However, you should never invoke it on the same type passing {@code json} since
     * that will cause an infinite loop (Gson will call your call-back method again).
     *
     * @param jsonElement The Json data being deserialized
     * @param type        The type of the Object to deserialize to
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public LocalTime deserialize(final JsonElement jsonElement,
                                 final Type type,
                                 final JsonDeserializationContext jsonDeserializationContext)
        throws JsonParseException {
        return DateTimeFormatter.ISO_LOCAL_TIME.parse(jsonElement.getAsString(), LocalTime::from);
    }

    /**
     * Gson invokes this call-back method during serialization when it encounters a field of the specified type.
     *
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any non-trivial field
     * of the {@code src} object. However, you should never invoke it on the {@code src} object itself since that will
     * cause an infinite loop (Gson will call your call-back method again).</p>
     *
     * @param localTime the local time that needs to be converted to Json.
     * @param type      the actual type (fully generalized version) of the source object.
     * @return a JsonElement corresponding to the specified object.
     */
    @Override
    public JsonElement serialize(final LocalTime localTime,
                                 final Type type,
                                 final JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
    }
}
