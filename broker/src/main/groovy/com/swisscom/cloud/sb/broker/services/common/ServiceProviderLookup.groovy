package com.swisscom.cloud.sb.broker.services.common

import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.Plan
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

import java.beans.Introspector

@Component
@Slf4j
class ServiceProviderLookup {
    public static final String POSTFIX_SERVICE_PROVIDER = "ServiceProvider"

    @Autowired
    private ApplicationContext appContext

    ServiceProvider findServiceProvider(String name) {

        log.info("Lookup for bean:${name}")

        return appContext.getBean(name)
    }

    ServiceProvider findServiceProvider(Plan plan) {
        Preconditions.checkNotNull(plan, "A valid plan is needed for finding the corresponding ServiceProvider")

        if (plan?.serviceProviderClassName) {
            log.info("Service provider lookup will be based on PLAN serviceProviderName:${plan.serviceProviderClassName}")
            return findServiceProvider(plan.serviceProviderClassName)
        }

        if (plan?.internalName) {
            log.info("Service provider lookup will be based on PLAN internalName:${plan.internalName}")
            return findServiceProvider(plan.internalName + POSTFIX_SERVICE_PROVIDER)
        }

        if (plan.service.serviceProviderClassName){
            return findServiceProvider(plan.service.serviceProviderClassName)
        }

        return findServiceProvider(plan.service.internalName + POSTFIX_SERVICE_PROVIDER)
    }

    public static String findInternalName(Class clazz) {
        def partialClassName = clazz.getSimpleName().substring(0, clazz.getSimpleName().lastIndexOf(POSTFIX_SERVICE_PROVIDER))
        return Introspector.decapitalize(partialClassName)
    }
}
