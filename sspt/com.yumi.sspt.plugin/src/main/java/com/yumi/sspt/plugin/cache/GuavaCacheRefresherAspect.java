package com.yumi.sspt.plugin.cache;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yumi.sspt.plugin.log.Loggers;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * GuavaCacheRefresher
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 */
@Component
@Aspect
public class GuavaCacheRefresherAspect {

    private Logger logger = Loggers.make();

    ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("CacheGet-CacheRefresher-pool-%d")
            .setUncaughtExceptionHandler((t,e) -> logger.error("guava cache refresh error", e))
            .setDaemon(true).build();

    ExecutorService parentExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            100,
            1,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(1000, false),
            threadFactory
    );

    final ListeningExecutorService backgroundRefreshPools = MoreExecutors.listeningDecorator(parentExecutor);


    private LoadingCache<Long, LoadingCache<KeyWrapper<String, Object>, Optional<Object>>> loadingCache;

    @PostConstruct
    public void init() {
        loadingCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<Long, LoadingCache<KeyWrapper<String, Object>, Optional<Object>>>() {
                    @Override
                    public LoadingCache<KeyWrapper<String, Object>, Optional<Object>> load(Long key) throws Exception {
                        return buildCache(key);
                    }
                });
    }

    private LoadingCache<KeyWrapper<String, Object>, Optional<Object>> buildCache(Long timeout) {

        return CacheBuilder.newBuilder()
                .maximumSize(100000)
                .refreshAfterWrite(timeout, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<KeyWrapper<String, Object>, Optional<Object>>() {
                    @Override
                    public Optional<Object> load(KeyWrapper<String, Object> keyWrapper) throws Exception {
                        Object result = generateValue(keyWrapper);
                        return Optional.ofNullable(result);
                    }

                    private Object generateValue(KeyWrapper<String, Object> keyWrapper) {
                        String key = keyWrapper.getKey();
                        return keyWrapper.getFunc().apply(key);
                    }

                    @Override
                    public ListenableFuture<Optional<Object>> reload(KeyWrapper<String, Object> key, Optional<Object> oldValue) throws Exception {

                        String requestId = MDC.get("requestId");
                        Callable<Optional<Object>> optionalCallable = () -> {
                            MDC.put("requestId", requestId);
                            return load(key);
                        };
                        return backgroundRefreshPools.submit(optionalCallable);
                    }
                });
    }


    @Pointcut("execution(* com.yumi.sspt..*.*(..)) && @annotation(com.yumi.sspt.plugin.cache.CacheGet)")
    public void cacheAspect() {
        // empty body
    }

    @Around("cacheAspect()")
    public Object interceptor(ProceedingJoinPoint p) throws Throwable {
        MethodInvocationProceedingJoinPoint pjp = (MethodInvocationProceedingJoinPoint) p;

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        Object[] args = pjp.getArgs();
        String[] paramNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
        EvaluationContext spElContext = PropsKit.newContext(args, paramNames);


        String methodName = method.getName();
        CacheGet cacheGetAnno = method.getAnnotation(CacheGet.class);

        String group = getCls(method.getDeclaringClass(), cacheGetAnno.cls());
        String subKey = getSubKey(cacheGetAnno.key(), args, methodName, spElContext);
        String key = group + "." + subKey;

        Object result = getValue(p, cacheGetAnno, key, spElContext);
        return result;
    }

    private Object getValue(ProceedingJoinPoint p, CacheGet cacheGetAnno, String key, EvaluationContext spElContext) {
        Function<String, Object> func = s -> {
            try {
                return p.proceed();
            } catch (Throwable throwable) {
                throw Throwables.propagate(throwable);
            }
        };
        KeyWrapper<String, Object> keyWrapper = new KeyWrapper<String, Object>().setKey(key).setFunc(func);
        int timeout = cacheGetAnno.timeout();
        TimeUnit unit = cacheGetAnno.unit();
        long timeoutMills = unit.toMillis(timeout);
        Optional<Object> optional = loadingCache.getUnchecked(timeoutMills).getUnchecked(keyWrapper);
        return optional.orElse(null);
    }

    private String getCls(Class<?> declaringClass, Class<?> cls) {
        if (cls.equals(CacheGet.EnclosingContainer.class)) {
            cls = declaringClass;
        }
        return cls.getCanonicalName();
    }

    private String getSubKey(String key, Object[] args, String methodName, EvaluationContext spElContext) {
        if (StringUtils.isEmpty(key)) {
            if (args.length == 0) {
                return methodName;
            } else {
                return methodName + ":" + StringUtils.join(args, "-");
            }
        }

        return PropsKit.getValue(key, spElContext);
    }


}
