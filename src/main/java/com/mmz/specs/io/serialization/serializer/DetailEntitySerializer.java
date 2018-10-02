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

package com.mmz.specs.io.serialization.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mmz.specs.model.DetailEntity;

import java.lang.reflect.Type;

public class DetailEntitySerializer implements JsonSerializer<DetailEntity> {
    @Override
    public JsonElement serialize(DetailEntity detailEntity, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("id", detailEntity.getId());
        result.addProperty("code", detailEntity.getCode());
        result.addProperty("workpieceWeight", detailEntity.getWorkpieceWeight());
        result.addProperty("finishedWeight", detailEntity.getFinishedWeight());
        result.addProperty("unit", detailEntity.isUnit());
        result.add("title", context.serialize(detailEntity.getDetailTitleByDetailTitleId()));
        return result;
    }
}
