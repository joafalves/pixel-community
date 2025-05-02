package org.pixel.commons.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ServiceRegistry<T>
{
    private final Class<T> serviceType;
    private final Class<?> settingsType;
    private final ServiceFactory<T> serviceFactory;
}
