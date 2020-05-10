package emilsoft.hackernews.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ItemDeserializer implements JsonDeserializer<Item> {
    @Override
    public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            String itemType = jsonObject.get("type").getAsString();
            switch (emilsoft.hackernews.api.Type.fromString(itemType)) {
                case STORY_TYPE: return context.deserialize(json, Story.class);
                case JOB_TYPE: return context.deserialize(json, Job.class);
                case COMMENT_TYPE: return context.deserialize(json, Comment.class);
                case POLL_TYPE: return context.deserialize(json, Poll.class);
                case POLLOPT_TYPE: return context.deserialize(json, PollOpt.class);
                default: return context.deserialize(json, Item.class);
            }
        }
        return context.deserialize(json, typeOfT);
    }
}
