package ru.dargen.fancy.spring.server.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class FancyPostBeanProcessor implements BeanPostProcessor {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final HandlerPacketRegistry registry;

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        val beanClass = AopUtils.getTargetClass(bean);
        if (beanClass.isAnnotationPresent(EnableFancy.class)) Arrays.stream(beanClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(FancyHandler.class))
                .forEach(method -> {
                    val packetType = (Class<? extends Packet>) method.getParameterTypes()[0];
                    val handler = wrapHandler(bean, method);

                    var packetId = registry.getPacketIdFromType(packetType);
                    if (packetId == -1) {
                        registry.register(packetType);
                        packetId = registry.getPacketIdFromType(packetType);
                    }

                    registry.registerHandler(packetId, handler);
                });
        return bean;
    }

    @SneakyThrows
    private Handler<?> wrapHandler(Object instance, Method method) {
        val methodHandle = LOOKUP.unreflect(method).bindTo(instance);
        if (method.getReturnType() == void.class) {
            if (method.getParameterCount() == 1) {
                val handler = this.<Consumer<DataPacket>>createLambda(Consumer.class, methodHandle);
                return (packet, remote, id) -> handler.accept(packet);
            } else {
                val handler = this.<BiConsumer<DataPacket, FancyRemote>>createLambda(BiConsumer.class, methodHandle);
                return (packet, remote, id) -> handler.accept(packet, remote);
            }
        } else {
            if (method.getParameterCount() == 1) {
                val handler = this.<Function<DataPacket, Packet>>createLambda(Function.class, methodHandle);
                return (packet, remote, id) -> {
                    val response = handler.apply(packet);
                    if (response != null) remote.write(response, id);
                };
            } else {
                val handler = this.<BiFunction<DataPacket, FancyRemote, Packet>>createLambda(BiFunction.class, methodHandle);
                return (packet, remote, id) -> {
                    val response = handler.apply(packet, remote);
                    if (response != null) remote.write(response, id);
                };
            }
        }
    }

    @SneakyThrows
    private <L> L createLambda(Class<?> interfaceClass, MethodHandle methodHandle) {
        return (L) MethodHandleProxies.asInterfaceInstance(interfaceClass, methodHandle);
    }

}
