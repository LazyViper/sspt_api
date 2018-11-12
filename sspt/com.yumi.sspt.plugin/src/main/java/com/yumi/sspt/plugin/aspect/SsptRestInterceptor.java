package com.yumi.sspt.plugin.aspect;

import com.yumi.sspt.plugin.exception.SsptRuntimeException;
import com.yumi.sspt.plugin.rest.RestResponse;
import com.yumi.sspt.plugin.utils.ExceptionUtil;
import com.yumi.sspt.plugin.utils.Jackson;
import com.yumi.sspt.plugin.utils.RequestUtils;
import com.yumi.sspt.plugin.validation.ValidationException;
import com.yumi.sspt.plugin.validation.Validators;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * SsptRestControllerAspect
 *  控制器切面
 * @author liyuming@foresee.com.cn
 * @version 1.0
 * @time 2018/11/11 0011
 */
@Aspect
@Order
public class SsptRestInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    //Controller层切点
    @Pointcut("execution(* com.yumi..rest..*.*(..)) && within(@(@org.springframework.stereotype.Controller *) *)")
    public void controllerAspect() {
        // empty body
    }

    @Around("controllerAspect()")
    public Object interceptor(ProceedingJoinPoint p) throws Throwable {
        MethodSignature signature = null;
        try {
            MethodInvocationProceedingJoinPoint pjp = (MethodInvocationProceedingJoinPoint) p;

            Object[] args = pjp.getArgs();
            signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();

            Object target = pjp.getTarget();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] parameterAnnotation = parameterAnnotations[i];
                for (Annotation annotation : parameterAnnotation) {
                    if (annotation instanceof Validated) {
                        Validated validated = (Validated) annotation;
                        Validators.validate(args[i], validated.value());
                    }
                }
            }
            try {
                Validators.validateParameters(target, method, args);
            } catch (ValidationException e) {
                throw e;
            } catch (Exception e) {
                for (Object arg : args) {
                    Validators.validate(arg);
                }
            }

            return pjp.proceed();
        } catch (Throwable throwable) {
            return handleException(throwable, signature);
        }
    }

    protected Object handleException(Throwable throwable, MethodSignature signature) throws Throwable {
        String message = throwable.getMessage();
        if (StringUtils.isBlank(message)) {
            message = throwable.toString();
        }
        String traces = ExceptionUtil.getSimpleStackTrace(throwable);
        String rawBody = "";
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            rawBody = RequestUtils.getRawBody(request);
        }


        Throwable exception = tryConvertToFtcspRuntimeException(throwable);
        if (exception instanceof SsptRuntimeException) {
            SsptRuntimeException ftcspRuntimeException = (SsptRuntimeException) exception;

            if ( ftcspRuntimeException.getParams() == null) {
                String errorCode = ftcspRuntimeException.getErrorCode();
                errorCode = StringUtils.trimToEmpty(errorCode);

                return new RestResponse(errorCode, ftcspRuntimeException.getErrorMsg());
            }
            throw ftcspRuntimeException;
        }
        if (throwable instanceof ValidationException) {
            List<ValidationException.ValidationDetail> details = ((ValidationException) throwable).getDetails();
            logger.info("校验失败, Request:{}, {}\n{}", signature, Jackson.toJson(details), rawBody);
            String errorCode = "0110119";
            return new RestResponse(errorCode, message);
        } else {
            logger.error("Handle Error, {}\nRequest:{}", traces, rawBody);
        }

        throw throwable;
    }

    private Throwable tryConvertToFtcspRuntimeException(Throwable throwable) {
        if (throwable instanceof SsptRuntimeException) {
            return throwable;
        }

        Throwable cause = throwable.getCause();
        // 避免无限循环
        for (int i = 0; i < 4; i++) {
            if (cause instanceof SsptRuntimeException) {
                return cause;
            }
            cause = throwable.getCause();
        }
        return throwable;
    }



}
