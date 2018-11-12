/**
 * Copyright(c) Foresee Science & Technology Ltd.
 */
package com.yumi.sspt.plugin.validation;

import com.yumi.sspt.plugin.utils.Jackson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import javax.validation.ConstraintViolation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
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
public class ValidationException extends RuntimeException {
    private List<ValidationDetail> details = new ArrayList<>(10);


    public static class ValidationDetail {
        private String field;
        private String message;
        private Object value;

        public static ValidationDetail
            valueOf(Class clazz, String property, String message, Object pathVal) {
            String path = StringUtils.defaultIfEmpty(property, clazz.toString());
            ValidationDetail detail = new ValidationDetail();
            detail.setField(path);
            detail.setMessage(message);
            detail.setValue(pathVal);
            return detail;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
    public void addDetail(ValidationDetail validationDetail) {
        details.add(validationDetail);
    }
    public ValidationException(Set<ConstraintViolation<Object>> errors) {
        errors.forEach(violation -> {
            String message = violation.getMessage();
            String path = violation.getPropertyPath().toString();
            Class clazz = violation.getRootBeanClass();
            Object pathVal = violation.getInvalidValue();
            addDetail(ValidationDetail.valueOf(clazz, path, message, pathVal));
        });
    }
    public ValidationException(Set<ConstraintViolation<Object>> errors, Method method) {
        errors.forEach(violation -> {
            String message = violation.getMessage();
            String path = getPath(method, violation.getPropertyPath().toString());
            Class clazz = violation.getRootBeanClass();
            Object pathVal = violation.getInvalidValue();
            addDetail(ValidationDetail.valueOf(clazz, path, message, pathVal));
        });
    }

    private String getPath(Method method, String path) {
        String[] pathDetails = StringUtils.split(path, ".");
        String methodArg = pathDetails[pathDetails.length - 1];
        if (!methodArg.startsWith("arg")) {
            return path;
        }
        String newMethodArg = getParameterName(method, methodArg);
        return newMethodArg;

    }

    private String getParameterName(Method method, String var) {
        String index = StringUtils.split(var, "arg")[0];
        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
        if (null != parameterNames) {
            return parameterNames[Integer.parseInt(index)];
        }

        Parameter[] parameters = method.getParameters();
        parameterNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterNames[i] = parameters[i].getName();
        }
        return parameterNames[Integer.parseInt(index)];
    }

    public List<ValidationDetail> getDetails() {
        return this.details;
    }

    @Override
    public String getMessage() {
        if (this.details != null && this.details.size() > 0) {
            return this.getDetails().get(0).getMessage();
        }
        return null;
    }

    @Override
    public String toString() {
        return Jackson.toJson(this);
    }
}
