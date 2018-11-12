package com.yumi.sspt.plugin.isolation;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yumi.sspt.plugin.annotation.EnableFtcspMVC;
import com.yumi.sspt.plugin.cache.PropsKit;
import com.yumi.sspt.plugin.support.Env;
import com.yumi.sspt.plugin.support.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年09月17日
 * <p>
 * <pre>
 * 修改记录
 * 修改后版本: 修改人： 修改日期: 修改内容:
 * </pre>
 */
@Component
public class ExecutorFactory {

    private final  Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<String, ThreadPool> executorMap = new ConcurrentHashMap<>();
    private Map<String, ExecutorCfg> cfgMap = new HashMap<>();

    @Autowired
    private Env env;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private ApplicationContext applicationContext;
    private ExecutorCfg defaultExecutorCfg;

    @PostConstruct
    public void init() throws Exception {
        initializeDefaultExecutorCfg();
        initializeCfgMap();
    }

    private void initializeDefaultExecutorCfg() throws NoSuchFieldException {
        logger.debug("初始化defaultExecutorCfg");
        Field defaultCfg = DefaultExecutors.class.getField("DEFAULT");
        defaultExecutorCfg = defaultCfg.getAnnotation(ExecutorCfg.class);
    }

    private void initializeCfgMap() {
        logger.debug("初始化cfgMap");
        Map<String, Object> beansWithAnnotation =
                applicationContext.getBeansWithAnnotation(EnableFtcspMVC.class);
        if (beansWithAnnotation == null || beansWithAnnotation.size() == 0) {
            return;
        }
        if (beansWithAnnotation.size() > 1) {
            throw new IllegalStateException("不能同时写多个EnableFtcspMVC");
        }

        // 启动类
        Object startUp = beansWithAnnotation.values().iterator().next();
        EnableFtcspMVC enableFtcspMVC = AnnotationUtils.findAnnotation(startUp.getClass(), EnableFtcspMVC.class);
        Class[] classes = enableFtcspMVC.threadDefines();
        Set<Class> klasses = Sets.newLinkedHashSet();
        klasses.add(DefaultExecutors.class);
        klasses.addAll(Lists.newArrayList(classes));
        for (Class klass : klasses) {
            Field[] fields = klass.getFields();
            for (Field field : fields) {
                if (field.getAnnotation(ExecutorCfg.class) != null) {
                    field.setAccessible(true);
                    Object o = null;
                    try {
                        o = field.get(null);
                        cfgMap.put((String) o, field.getAnnotation(ExecutorCfg.class));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("反射读取失败" + field);
                    }
                }
            }
        }
    }

    private ThreadPool newExecutor(String threadGroupName, ExecutorCfg cfg) {
        int corePoolSize = cfg.coreSize();
        int maxPoolSize = cfg.maxSize();
        int capacity = cfg.capacity() < maxPoolSize ? maxPoolSize : cfg.capacity();
        int keepAliveTime = cfg.keepAliveTime();
        logger.info("初始化线程池, name:{}, core:{}, max:{}, cap:{}, keepAliveTime:{}ms",
                threadGroupName, corePoolSize, maxPoolSize, capacity, keepAliveTime);
        ThreadPool threadPool = new ThreadPool(corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(capacity),
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadGroupName + "-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        logger.info("初始化线程池完毕, {}", threadGroupName);

        int timeout = PropsKit.getIntValue(cfg.timeout());
        threadPool.setTimeout(timeout * 1000);
        threadPool.setPoolName(threadGroupName);
        return threadPool;
    }

    public ThreadPool getExecutor(HttpServletRequest httpServletRequest) {
        String threadGroupName = findThreadGroupName(httpServletRequest);
        ExecutorCfg cfg = findExecutorCfg(threadGroupName);
        return executorMap.computeIfAbsent(threadGroupName, s -> newExecutor(s, cfg));
    }

    private String findThreadGroupName(HttpServletRequest httpServletRequest) {
        // 先找到method
        Method method = findMethod(httpServletRequest);
        if (method == null) {
            return DefaultExecutors.DEFAULT;
        }
        ThreadGroup threadGroup = findThreadGroup(method);
        if (threadGroup != null) {
            return threadGroup.value();
        }
        return DefaultExecutors.DEFAULT;
    }

    private ThreadGroup findThreadGroup(Method method) {
        // 这个method
        ThreadGroup threadGroup = AnnotationUtils.findAnnotation(method, ThreadGroup.class);
        if (threadGroup != null) {
            return threadGroup;
        }
        // 这个method所在的class
        Class<?> declaringClass = method.getDeclaringClass();
        threadGroup = AnnotationUtils.findAnnotation(declaringClass, ThreadGroup.class);
        if (threadGroup != null) {
            return threadGroup;
        }
        return null;
    }

    private Method findMethod(HttpServletRequest request) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                this.requestMappingHandlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> item : handlerMethods.entrySet()) {
            RequestMappingInfo mapping = item.getKey();
            mapping = mapping.getMatchingCondition(request);
            if (mapping == null) {
                continue;
            }
            HandlerMethod handlerMethod = item.getValue();
            Method method = handlerMethod.getMethod();
            return method;
        }
        return null;
    }

    private ExecutorCfg findExecutorCfg(String threadGroupName) {
        return cfgMap.getOrDefault(threadGroupName, defaultExecutorCfg);
    }
}
