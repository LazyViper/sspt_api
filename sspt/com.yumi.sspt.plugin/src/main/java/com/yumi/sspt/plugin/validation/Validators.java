/**
 * Copyright(c) Foresee Science & Technology Ltd.
 */
package com.yumi.sspt.plugin.validation;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * <pre>
 * 校验器
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 * @date 2017年04月18日
 */
public abstract class Validators {

    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    /**
     *
     * @param object
     * @return
     */
    public static void validate(Object object, Class<?>... groups) {
        if (object == null) return;
        if (groups == null) groups = new Class[0];
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> errors = validator.validate(object, groups);
        if (errors.isEmpty()) return;
        throw new ValidationException(errors);
    }

    /**
     * 判断是否通过校验
     * @param object
     * @return
     */
    public static boolean validateNoException(Object object) {
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> errors = validator.validate(object);
        return errors.isEmpty();

    }

    public static void validateParameters(Object object, Method method, Class<?>[] groups, Object... values) {
        if (object == null) return;
        Set<ConstraintViolation<Object>> errors = factory.getValidator()
                .forExecutables()
                .validateParameters(object, method, values);
        if (errors.isEmpty()) return;
        throw new ValidationException(errors, method);
    }

    public static void validateParameters(Object object, Method method, Object... values) {
        if (object == null) return;
        Set<ConstraintViolation<Object>> errors = factory.getValidator()
                .forExecutables()
                .validateParameters(object, method, values);
        if (errors.isEmpty()) return;
        throw new ValidationException(errors, method);
    }
}
