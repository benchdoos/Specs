/*
 * (C) Copyright 2018.  Eugene Zrazhevsky and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Contributors:
 * Eugene Zrazhevsky <eugene.zrazhevsky@gmail.com>
 */

package com.mmz.specs.io.serialization.deserializer;

import com.google.gson.*;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;

import java.lang.reflect.Type;

public class DetailEntityDeserializer implements JsonDeserializer<DetailEntity> {
    @Override
    public DetailEntity deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = element.getAsJsonObject();
        DetailEntity result = new DetailEntity();
        result.setId(jsonObject.get("id").getAsInt());
        result.setCode(jsonObject.get("code").getAsString());
        final JsonElement workpieceWeight = jsonObject.get("workpieceWeight");
        if (workpieceWeight != null) {
            result.setWorkpieceWeight(workpieceWeight.getAsDouble());
        }
        final JsonElement finishedWeight = jsonObject.get("finishedWeight");
        if (finishedWeight != null) {
            result.setFinishedWeight(finishedWeight.getAsDouble());
        }
        result.setUnit(jsonObject.get("unit").getAsBoolean());
        result.setDetailTitleByDetailTitleId(context.deserialize(jsonObject.get("title"), DetailTitleEntity.class));
        result.setActive(true);
        return result;
    }
}
