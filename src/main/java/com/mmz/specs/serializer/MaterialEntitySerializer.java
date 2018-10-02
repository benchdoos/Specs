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

package com.mmz.specs.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mmz.specs.model.MaterialEntity;

import java.lang.reflect.Type;

public class MaterialEntitySerializer implements JsonSerializer<MaterialEntity> {
    @Override
    public JsonElement serialize(MaterialEntity materialEntity, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        result.addProperty("id", materialEntity.getId());
        result.addProperty("longProfile", materialEntity.getLongProfile());
        result.addProperty("longMark", materialEntity.getLongMark());
        result.addProperty("shortProfile", materialEntity.getShortProfile());
        result.addProperty("shortMark", materialEntity.getShortMark());
        return result;
    }
}
