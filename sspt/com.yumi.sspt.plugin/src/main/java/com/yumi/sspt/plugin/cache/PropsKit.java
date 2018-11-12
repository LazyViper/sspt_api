package com.yumi.sspt.plugin.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * SpElKit
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 */
@Component
public class PropsKit {

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    private static ConfigurableBeanFactory defaultBeanFactory;

    @PostConstruct
    public void init() {
        defaultBeanFactory = beanFactory;
    }

    private static ExpressionParser parser = new SpelExpressionParser();


    public static EvaluationContext newContext(Object[] args, String[] paramNames) {
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return context;
    }

    public static int getIntValue(String expr) {
        String value = defaultBeanFactory.resolveEmbeddedValue(expr);
        return Integer.parseInt(value);

    }

    public static <T> T getValue(String expr, Class<T> type) {
        String value = defaultBeanFactory.resolveEmbeddedValue(expr);
        return defaultBeanFactory.getTypeConverter()
                .convertIfNecessary(value, type);
    }


    public static String getValue(String expr) {
        String value = defaultBeanFactory.resolveEmbeddedValue(expr);
        return value;
    }

    public static String getValue(String expr, EvaluationContext context) {
        return parser.parseExpression(expr).getValue(context, String.class);
    }

    public static void main(String[] args) {

    }
}
