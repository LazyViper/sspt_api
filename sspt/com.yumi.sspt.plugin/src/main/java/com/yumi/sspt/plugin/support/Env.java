package com.yumi.sspt.plugin.support;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * EnvKit
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 */
@Component
public class Env {

    @Autowired
    private AbstractEnvironment env;


    public Map<String, Object> getAllProperties() {
        HashMap<String, Object> map = Maps.newHashMap();
        for(Iterator it = env.getPropertySources().iterator(); it.hasNext(); ) {
            org.springframework.core.env.PropertySource propertySource = (org.springframework.core.env.PropertySource) it.next();
            if (propertySource instanceof MapPropertySource) {
                map.putAll(((MapPropertySource) propertySource).getSource());
            }

            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerablePropertySource =
                        (EnumerablePropertySource<?>) propertySource;
                for (String propertyKey : enumerablePropertySource.getPropertyNames()) {
                    String propertyValue = env.getProperty(propertyKey);
                    map.put(propertyKey, propertyValue);
                }
            }
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }
}


