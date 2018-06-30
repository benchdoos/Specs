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

package com.mmz.specs.application.utils;

import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.service.DetailService;
import com.mmz.specs.service.DetailServiceImpl;
import org.hibernate.Session;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringUtils {
    private Session session;

    public StringUtils(Session session) {
        this.session = session;
    }

    private static Map<String, String> parseMap(String text) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String keyValue : text.split(",")) {
            String[] parts = keyValue.split("=", 2);
            map.put(parts[0], parts[1]);
        }
        return map;
    }

    //com.mmz.specs.model.DetailEntity@2694bbe3[id=880,index=ММ-110.70.000,unit=true,finishedWeigth=<null>,workpieceWeight=<null>,imagePath=<null>,active=true,techProcessByTechProcessId=<null>,detailTitleByDetailTitleId=com.mmz.specs.model.DetailTitleEntity@34a1f57f[id=185,title=Гидрооборудование,active=true]]
    public DetailEntity getDetailEntityFromString(String string) {
        DetailEntity result = null;
        if (string != null && !string.isEmpty()) {
            if (string.contains(DetailEntity.class.getName())) {
                if (string.contains("code")) {
                    final Map<String, String> map = parseMap(string);
                    final String code = map.get("code");

                    try {
                        DetailService service = new DetailServiceImpl(session);
                        result = service.getDetailByCode(code);
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return result;
    }
}
