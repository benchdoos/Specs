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

package com.mmz.specs.dao;

import com.mmz.specs.application.core.Main;
import org.junit.Test;

public class TechProcessDaoImplTest {

    @Test
    public void getTechProcessByAlikeValue() {
        Main.main(new String[]{"-server"});
        TechProcessDao dao = new TechProcessDaoImpl();
        dao.getTechProcessByAlikeValue("ток сл");
        dao.getTechProcessByAlikeValue("ток чпу");
        dao.getTechProcessByAlikeValue("сл сл");
        dao.getTechProcessByAlikeValue("фр сл");
        dao.getTechProcessByAlikeValue("св чпу");

    }
}