package org.netcorepal.cap4j.ddd.archinfo;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.netcorepal.cap4j.ddd.application.event.annotation.IntegrationEvent;
import org.netcorepal.cap4j.ddd.application.query.Query;
import org.netcorepal.cap4j.ddd.application.saga.SagaHandler;
import org.netcorepal.cap4j.ddd.archinfo.model.ArchInfo;
import org.netcorepal.cap4j.ddd.archinfo.model.Architecture;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.*;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.aggreagate.*;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.pubsub.DomainEventElement;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.pubsub.SubscriberElement;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.reqres.CommandElement;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.reqres.QueryElement;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.reqres.RequestElement;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.reqres.SagaElement;
import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;
import org.netcorepal.cap4j.ddd.domain.event.EventSubscriber;
import org.netcorepal.cap4j.ddd.domain.event.annotation.DomainEvent;
import org.netcorepal.cap4j.ddd.domain.service.annotation.DomainService;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.netcorepal.cap4j.ddd.share.misc.ScanUtils;
import org.springframework.context.annotation.Description;
import org.springframework.context.event.EventListener;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.netcorepal.cap4j.ddd.share.Constants.ARCH_INFO_VERSION;

/**
 * 架构信息管理器
 *
 * @author binking338
 * @date 2024/11/21
 */
@RequiredArgsConstructor
public class ArchInfoManager {
    private final String name;
    private final String version;
    private final String basePackage;

    protected Function<ArchInfo, ArchInfo> config;

    public void configure(Function<ArchInfo, ArchInfo> config) {
        this.config = config;
    }

    public ArchInfo getArchInfo() {
        resolve();
        ArchInfo archInfo = loadArchInfo();
        return config == null ? archInfo : config.apply(archInfo);
    }

    protected ArchInfo loadArchInfo() {
        ArchInfo archInfo = ArchInfo.builder()
                .name(name)
                .version(version)
                .archInfoVersion(ARCH_INFO_VERSION)
                .architecture(loadArchitecture())
                .build();
        return archInfo;
    }

    protected Architecture loadArchitecture() {
        Architecture architecture = Architecture.builder()
                .application(loadApplication())
                .domain(loadDomain())
                .build();
        return architecture;
    }

    protected Architecture.Application loadApplication() {
        return Architecture.Application.builder()
                .requests(loadRequests())
                .events(loadEvents())
                .build();
    }

    protected MapCatalog loadRequests() {
        ListCatalog commandCatalog = new ListCatalog("commands", "命令", commandClasses.stream()
                .map(cls -> CommandElement.builder()
                        .classRef(cls.getName())
                        .requestClassRef(resolveRequestClass(cls).getTypeName())
                        .responseClassRef(resolveResponseClass(cls).getTypeName())
                        .name(cls.getSimpleName())
                        .description(getDescription(cls, ""))
                        .build()
                ).collect(Collectors.toList()));
        ListCatalog queryCatalog = new ListCatalog("queries", "查询", queryClasses.stream()
                .map(cls -> QueryElement.builder()
                        .classRef(cls.getName())
                        .requestClassRef(resolveRequestClass(cls).getTypeName())
                        .responseClassRef(resolveResponseClass(cls).getTypeName())
                        .name(cls.getSimpleName())
                        .description(getDescription(cls, ""))
                        .build()
                ).collect(Collectors.toList()));
        ListCatalog sagaCatalog = new ListCatalog("sagas", "SAGA", sagaClasses.stream()
                .map(cls -> SagaElement.builder()
                        .classRef(cls.getName())
                        .requestClassRef(resolveRequestClass(cls).getTypeName())
                        .responseClassRef(resolveResponseClass(cls).getTypeName())
                        .name(cls.getSimpleName())
                        .description(getDescription(cls, ""))
                        .build()
                ).collect(Collectors.toList()));
        ListCatalog requestCatalog = new ListCatalog("requests", "请求处理", requestClasses.stream()
                .map(cls -> RequestElement.builder()
                        .classRef(cls.getName())
                        .requestClassRef(resolveRequestClass(cls).getTypeName())
                        .responseClassRef(resolveResponseClass(cls).getTypeName())
                        .name(cls.getSimpleName())
                        .description(getDescription(cls, ""))
                        .build()
                ).collect(Collectors.toList()));
        return new MapCatalog("requests", "请求响应", Arrays.asList(
                commandCatalog, queryCatalog, sagaCatalog, requestCatalog
        ));
    }

    protected Map<Class<?>, List<SubscriberElement>> loadEventSubscriberMap() {
        Map<Class<?>, List<SubscriberElement>> eventSubscriberMap = new HashMap<>();
        subscriberClasses.forEach(cls -> {
                    if (EventSubscriber.class.isAssignableFrom(cls)) {
                        Class<?> eventCls = ClassUtils.findMethod(cls, "onEvent", method -> method.getParameterCount() == 1)
                                .getParameterTypes()[0];

                        if (!eventSubscriberMap.containsKey(eventCls)) {
                            eventSubscriberMap.put(eventCls, new ArrayList<>());
                        }
                        eventSubscriberMap.get(eventCls).add(
                                SubscriberElement.builder()
                                        .classRef(cls.getName())
                                        .name(cls.getSimpleName())
                                        .description(getDescription(cls, ""))
                                        .eventRef(resolveEventRef(eventCls))
                                        .build()
                        );
                    } else {

                        Arrays.stream(cls.getDeclaredMethods())
                                .filter(method -> null != getDomainOrIntegrationEventListener(method))
                                .forEach(method -> {
                                    EventListener eventListener = method.getAnnotation(EventListener.class);
                                    Class<?> eventCls = eventListener.classes().length > 0 ? eventListener.classes()[0] : eventListener.value()[0];
                                    if (!eventSubscriberMap.containsKey(eventCls)) {
                                        eventSubscriberMap.put(eventCls, new ArrayList<>());
                                    }
                                    eventSubscriberMap.get(eventCls).add(SubscriberElement.builder()
                                            .classRef(cls.getName())
                                            .name(cls.getSimpleName() + "#" + method.getName())
                                            .description(getDescription(method, ""))
                                            .eventRef(resolveEventRef(eventCls))
                                            .build()
                                    );
                                });
                    }
                }
        );
        return eventSubscriberMap;
    }

    protected MapCatalog loadEvents() {
        Map<Class<?>, List<SubscriberElement>> eventSubscriberMap = loadEventSubscriberMap();
        ListCatalog subscriberCatalog = new ListCatalog("subscribers", "订阅者", eventSubscriberMap.values().stream().flatMap(list -> list.stream()).collect(Collectors.toList()));
        ListCatalog domainEventCatalog = new ListCatalog("domain", "领域事件", domainEventClasses.stream()
                .map(cls -> ElementRef.builder()
                        .ref(resolveEventRef(cls))
                        .name(cls.getSimpleName())
                        .description(getDescription(cls, ""))
                        .build()
                ).collect(Collectors.toList())
        );
        ListCatalog integrationEventCatalog = new ListCatalog("integration", "集成事件", integrationEventClasses.stream()
                .map(cls -> DomainEventElement.builder()
                        .classRef(cls.getName())
                        .name(cls.getSimpleName())
                        .description(getDescription(cls, ""))
                        .subscribersRef(eventSubscriberMap.get(cls).stream().map(sub -> "/architecture/application/events/subscribers/" + sub.getName()).collect(Collectors.toList()))
                        .build()
                ).collect(Collectors.toList()));

        return new MapCatalog("events", "事件",
                Arrays.asList(domainEventCatalog, integrationEventCatalog, subscriberCatalog)
        );
    }

    protected Architecture.Domain loadDomain() {
        return Architecture.Domain.builder()
                .aggregates(loadAggregates())
                .services(loadDomainServices())
                .build();
    }

    protected MapCatalog loadAggregates() {
        Map<Class<?>, List<SubscriberElement>> eventSubscriberMap = loadEventSubscriberMap();
        return new MapCatalog("aggregates", "聚合", repositoryClasses
                .stream().map(cls -> {
                    Aggregate aggregate = getAggregate(cls);
                    Element rootCatalog =
                            entityClasses.stream()
                                    .filter(entityCls -> getAggregate(entityCls).aggregate().equals(aggregate.aggregate())
                                            && getAggregate(entityCls).root())
                                    .map(entityCls -> (Element) EntityElement.builder()
                                            .classRef(entityCls.getName())
                                            .name(getAggregate(entityCls).name())
                                            .description(getDescription(entityCls, getAggregate(entityCls).description()))
                                            .root(true)
                                            .build())
                                    .findFirst().orElse(new NoneElement());
                    Element repositoryCatalog =
                            repositoryClasses.stream()
                                    .filter(repositoryCls -> getAggregate(repositoryCls).aggregate().equals(aggregate.aggregate()))
                                    .map(repositoryCls -> (Element) RepositoryElement.builder()
                                            .classRef(repositoryCls.getName())
                                            .name(getAggregate(repositoryCls).name())
                                            .description(getDescription(repositoryCls, getAggregate(repositoryCls).description()))
                                            .build())
                                    .findFirst().orElse(new NoneElement());
                    Element factoryCatalog =
                            factoryClasses.stream()
                                    .filter(factoryCls -> getAggregate(factoryCls).aggregate().equals(aggregate.aggregate()))
                                    .map(factoryCls -> (Element) FactoryElement.builder()
                                            .classRef(factoryCls.getName())
                                            .name(getAggregate(factoryCls).name())
                                            .description(getDescription(factoryCls, getAggregate(factoryCls).description()))
                                            .build())
                                    .findFirst().orElse(new NoneElement());
                    ListCatalog entityCatalog = new ListCatalog("entities", "实体",
                            entityClasses.stream()
                                    .filter(entityCls -> getAggregate(entityCls).aggregate().equals(aggregate.aggregate())
                                            && !getAggregate(entityCls).root())
                                    .map(entityCls -> EntityElement.builder()
                                            .classRef(entityCls.getName())
                                            .name(getAggregate(entityCls).name())
                                            .description(getDescription(entityCls, getAggregate(entityCls).description()))
                                            .root(false)
                                            .build())
                                    .collect(Collectors.toList()));
                    ListCatalog valueObjectCatalog = new ListCatalog("valueObjects", "值对象",
                            valueObjectClasses.stream()
                                    .filter(valueObjectCls -> getAggregate(valueObjectCls).aggregate().equals(aggregate.aggregate()))
                                    .map(valueObjectCls -> ValueObjectElement.builder()
                                            .classRef(valueObjectCls.getName())
                                            .name(getAggregate(valueObjectCls).name())
                                            .description(getDescription(valueObjectCls, getAggregate(valueObjectCls).description()))
                                            .build())
                                    .collect(Collectors.toList()));
                    ListCatalog enumCatalog = new ListCatalog("enums", "枚举",
                            enumObjectClasses.stream()
                                    .filter(enumCls -> getAggregate(enumCls).aggregate().equals(aggregate.aggregate()))
                                    .map(enumCls -> EnumElement.builder()
                                            .classRef(enumCls.getName())
                                            .name(getAggregate(enumCls).name())
                                            .description(getDescription(enumCls, getAggregate(enumCls).description()))
                                            .build())
                                    .collect(Collectors.toList()));
                    ListCatalog specificationCatalog = new ListCatalog("specifications", "规约",
                            specificationClasses.stream()
                                    .filter(specCls -> getAggregate(specCls).aggregate().equals(aggregate.aggregate()))
                                    .map(specCls -> SpecificationElement.builder()
                                            .classRef(specCls.getName())
                                            .name(getAggregate(specCls).name())
                                            .description(getDescription(specCls, getAggregate(specCls).description()))
                                            .build())
                                    .collect(Collectors.toList()));
                    ListCatalog eventCatalog = new ListCatalog("events", "领域事件",
                            domainEventClasses.stream()
                                    .filter(domainEventCls -> getAggregate(domainEventCls).aggregate().equals(aggregate.aggregate()))
                                    .map(domainEventCls -> DomainEventElement.builder()
                                            .classRef(domainEventCls.getName())
                                            .name(getAggregate(domainEventCls).name())
                                            .description(getDescription(domainEventCls, getAggregate(domainEventCls).description()))
                                            .subscribersRef(eventSubscriberMap.get(domainEventCls).stream().map(sub -> "/architecture/application/events/subscribers/" + sub.getName()).collect(Collectors.toList()))
                                            .build())
                                    .collect(Collectors.toList()));
                    Map<String, Element> elements = new HashMap<>();
                    elements.put("root", rootCatalog);
                    elements.put("repository", repositoryCatalog);
                    elements.put("factory", factoryCatalog);
                    elements.put("entities", entityCatalog);
                    elements.put("valueObjects", valueObjectCatalog);
                    elements.put("enums", enumCatalog);
                    elements.put("specifications", specificationCatalog);
                    elements.put("events", eventCatalog);
                    return new MapCatalog(aggregate.aggregate(), aggregate.description(), elements);
                }).collect(Collectors.toList()));
    }

    protected ListCatalog loadDomainServices() {
        return new ListCatalog("services", "领域服务", domainServiceClasses
                .stream().map(cls -> {
                    DomainService domainService = ((DomainService) cls.getAnnotation(DomainService.class));
                    return DomainServiceElement.builder()
                            .classRef(cls.getName())
                            .name(domainService.name())
                            .description(getDescription(cls, domainService.description()))
                            .build();
                }).collect(Collectors.toList()));
    }

    protected String resolveEventRef(Class<?> eventCls) {
        if (eventCls.isAnnotationPresent(DomainEvent.class) && eventCls.isAnnotationPresent(Aggregate.class)) {
            return "/architecture/domain/aggregates/" + eventCls.getAnnotation(Aggregate.class).aggregate() + "/events/" + eventCls.getAnnotation(Aggregate.class).name();
        } else if (eventCls.isAnnotationPresent(IntegrationEvent.class)) {
            return "/architecture/application/events/integration/" + eventCls.getSimpleName();
        }
        return null;
    }

    protected Type resolveRequestClass(Class<?> requestHandlerCls) {
        Method method = ClassUtils.findMethod(requestHandlerCls, "exec", m -> m.getParameterCount() == 1);
        return method.getGenericParameterTypes()[0];
    }

    protected Type resolveResponseClass(Class<?> requestHandlerCls) {
        Method method = ClassUtils.findMethod(requestHandlerCls, "exec", m -> m.getParameterCount() == 1);
        return method.getGenericReturnType();
    }

    protected Aggregate getAggregate(AnnotatedElement accessibleObject) {
        if (accessibleObject.isAnnotationPresent(Aggregate.class)) {
            return ((Aggregate) accessibleObject.getAnnotation(Aggregate.class));
        } else {
            return null;
        }
    }

    protected String getDescription(AnnotatedElement accessibleObject, String defaultVal) {
        if (accessibleObject.isAnnotationPresent(Description.class)) {
            return ((Description) accessibleObject.getAnnotation(Description.class)).value();
        } else {
            return defaultVal;
        }
    }

    protected EventListener getDomainOrIntegrationEventListener(Method method) {
        if (!method.isAnnotationPresent(EventListener.class)) {
            return null;
        }
        EventListener eventListener = method.getAnnotation(EventListener.class);
        if (eventListener.classes().length == 1) {
            if (eventListener.classes()[0].isAnnotationPresent(DomainEvent.class)
                    || eventListener.classes()[0].isAnnotationPresent(IntegrationEvent.class)) {
                return eventListener;
            }
        } else if (eventListener.value().length == 1) {
            if (eventListener.value()[0].isAnnotationPresent(DomainEvent.class)
                    || eventListener.value()[0].isAnnotationPresent(IntegrationEvent.class)) {
                return eventListener;
            }
        }
        return null;
    }

    List<Class> repositoryClasses = new ArrayList<>();
    List<Class> factoryClasses = new ArrayList<>();
    List<Class> entityClasses = new ArrayList<>();
    List<Class> valueObjectClasses = new ArrayList<>();
    List<Class> enumObjectClasses = new ArrayList<>();
    List<Class> specificationClasses = new ArrayList<>();
    List<Class> domainServiceClasses = new ArrayList<>();

    List<Class> domainEventClasses = new ArrayList<>();
    List<Class> integrationEventClasses = new ArrayList<>();
    List<Class> subscriberClasses = new ArrayList<>();

    List<Class> queryClasses = new ArrayList<>();
    List<Class> commandClasses = new ArrayList<>();
    List<Class> requestClasses = new ArrayList<>();
    List<Class> sagaClasses = new ArrayList<>();
    boolean resolved = false;

    protected void resolve() {
        if (resolved) {
            return;
        }
        Set<Class<?>> classes = ScanUtils.scanClass(basePackage, true);
        classes.forEach(cls -> {
            if (Arrays.stream(cls.getDeclaredMethods())
                    .anyMatch(method -> null != getDomainOrIntegrationEventListener(method))
            ) {
                subscriberClasses.add(cls);
            }
            if (cls.isAnnotationPresent(Aggregate.class)) {
                Aggregate aggregate = cls.getAnnotation(Aggregate.class);
                switch (aggregate.type()) {
                    case Aggregate.TYPE_REPOSITORY:
                        repositoryClasses.add(cls);
                        break;
                    case Aggregate.TYPE_FACTORY:
                        factoryClasses.add(cls);
                        break;
                    case Aggregate.TYPE_ENTITY:
                        entityClasses.add(cls);
                        break;
                    case Aggregate.TYPE_VALUE_OBJECT:
                        valueObjectClasses.add(cls);
                        break;
                    case Aggregate.TYPE_ENUM:
                        enumObjectClasses.add(cls);
                        break;
                    case Aggregate.TYPE_SPECIFICATION:
                        specificationClasses.add(cls);
                        break;
                    case Aggregate.TYPE_DOMAIN_EVENT:
                        domainEventClasses.add(cls);
                        break;
                    default:
                        break;
                }
                return;
            }
            if (cls.isAnnotationPresent(DomainService.class)) {
                domainServiceClasses.add(cls);
                return;
            }
            if (cls.isAnnotationPresent(DomainEvent.class)) {
                domainEventClasses.add(cls);
                return;
            }
            if (cls.isAnnotationPresent(IntegrationEvent.class)) {
                integrationEventClasses.add(cls);
                return;
            }
            if (EventSubscriber.class.isAssignableFrom(cls)) {
                subscriberClasses.add(cls);
                return;
            }

            if (Query.class.isAssignableFrom(cls)) {
                queryClasses.add(cls);
                return;
            }
            if (Command.class.isAssignableFrom(cls)) {
                commandClasses.add(cls);
                return;
            }
            if (SagaHandler.class.isAssignableFrom(cls)) {
                sagaClasses.add(cls);
                return;
            }
            if (RequestHandler.class.isAssignableFrom(cls)) {
                requestClasses.add(cls);
                return;
            }
        });
        resolved = true;
    }
}
