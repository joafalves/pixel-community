package org.pixel.commons.service;

import java.util.List;

public interface ServiceRegistrar {
    List<ServiceRegistry<?>> getServices();
}
