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

package com.mmz.specs.application.utils.validation;

import com.mmz.specs.model.UserTypeEntity;
import com.mmz.specs.model.UsersEntity;

public class ValidationUtils {
    public static boolean validateUserEntity(UsersEntity usersEntity) throws UsernameValidationException, UserTypeValidationException {
        if (usersEntity == null) throw new UsernameValidationException();

        validateUserName(usersEntity.getUsername());

        validateUserType(usersEntity.getUserType());


        return true;

    }

    private static void validateUserType(UserTypeEntity userTypeEntity) throws UserTypeValidationException {
        if (userTypeEntity == null) throw new UserTypeValidationException();
    }

    public static void validateUserName(String username) throws UsernameValidationException {
        if (username == null) throw new UsernameValidationException();

        if (username.length() < 4 || username.length() > 20) throw new UsernameValidationException();

        if (username.startsWith(" ") || username.contains(" "))
            throw new UsernameValidationException();
    }
}
