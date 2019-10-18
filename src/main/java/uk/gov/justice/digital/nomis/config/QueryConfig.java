package uk.gov.justice.digital.nomis.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;

@Configuration
public class QueryConfig {

    @Bean
    BeanPostProcessor pageablePostProcessor(@Value("${maxPageSize:#{T(Integer).MAX_VALUE}}") final Integer maxPageSize) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
                if (bean instanceof HateoasPageableHandlerMethodArgumentResolver) {
                    ((HateoasPageableHandlerMethodArgumentResolver) bean).setMaxPageSize(maxPageSize);
                }

                return bean;
            }
        };
    }

}
