package ru.dargen.fancy.spring.server.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import ru.dargen.fancy.packet.DataPacket;
import ru.dargen.fancy.packet.Packet;
import ru.dargen.fancy.packet.registry.HandlerPacketRegistry;
import ru.dargen.fancy.packet.registry.handler.Handler;
import ru.dargen.fancy.server.FancyRemote;
import ru.dargen.fancy.spring.server.annotation.EnableFancy;
import ru.dargen.fancy.spring.server.annotation.FancyHandler;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class FancyPostBeanProcessor implements BeanPostProcessor {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final HandlerPacketRegistry registry;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        val beanClass = AopUtils.getTargetClass(bean);
        if (beanClass.isAnnotationPresent(EnableFancy.class)) Arrays.stream(beanClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(FancyHandler.class))
                .forEach(method -> {
                    val packetType = method.getParameterTypes()[0];
                    val handler = wrapHandler(bean, method);

                    registry.registerHandler(registry.getPacketIdFromType((Class<? extends Packet>) packetType), handler);
                });
        return bean;
    }

    @SneakyThrows
    private Handler<?> wrapHandler(Object instance, Method method) {
        val methodHandle = LOOKUP.unreflect(method).bindTo(instance);;
        if (method.getReturnType() == void.class) {
            val handler = this.<BiConsumer<DataPacket, FancyRemote>>createLambda(BiConsumer.class, methodHandle);
            return (packet, remote, id) -> handler.accept(packet, remote);
        } else {
            val handler = this.<BiFunction<DataPacket, FancyRemote, Packet>>createLambda(BiFunction.class, methodHandle);
            return (packet, remote, id) -> {
                val response = handler.apply(packet, remote);
                if (response != null) remote.write(response, id);
            };
        }
    }

    @SneakyThrows
    private <L> L createLambda(Class<?> interfaceClass, MethodHandle methodHandle) {
        return (L) MethodHandleProxies.asInterfaceInstance(interfaceClass, methodHandle);
    }

}
