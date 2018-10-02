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

package com.mmz.specs.io.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class FromStringBuilder {

    /**
     * Parses a string formatted with toStringBuilder
     *
     * @param input - ex. "Path[id=1039916,displayName=School Home,description=<null>,...]"
     * @return hashmap of name value pairs - ex. id=1039916,...
     */
    public static Map<String, String> stringToMap(String input) {
        LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
        String partsString = StringUtils.substringBetween(input, "[", "]");
        String[] parts = partsString.split(",");
        for (String part : parts) {
            String[] nv = part.split("=");
            if (!StringUtils.equals("<null>", nv[1])) {
                ret.put(nv[0], nv[1]);
            }
        }
        return ret;
    }

    public static <T> T stringToObject(String input, Class<T> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<String, String> map = stringToMap(input);
        T ret = clazz.newInstance();
        BeanUtils.copyProperties(ret, map);
        return ret;
    }

    /*            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);*/

}