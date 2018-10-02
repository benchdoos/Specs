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

package com.mmz.specs.deserializer;

import com.google.gson.*;
import com.mmz.specs.model.DetailTitleEntity;

import java.lang.reflect.Type;

public class DetailTitleEntityDeserializer implements JsonDeserializer<DetailTitleEntity> {
    @Override
    public DetailTitleEntity deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = element.getAsJsonObject();
        DetailTitleEntity result = new DetailTitleEntity();
        result.setId(jsonObject.get("id").getAsInt());
        result.setTitle(jsonObject.get("title").getAsString());
        result.setActive(true);
        return result;
    }
}
