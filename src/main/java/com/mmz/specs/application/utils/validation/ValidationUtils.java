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
