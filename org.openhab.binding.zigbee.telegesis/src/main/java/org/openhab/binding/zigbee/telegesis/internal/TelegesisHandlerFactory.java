/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.telegesis.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.zigbee.discovery.ZigBeeDiscoveryService;
import org.openhab.binding.zigbee.handler.ZigBeeCoordinatorHandler;
import org.openhab.binding.zigbee.telegesis.TelegesisBindingConstants;
import org.openhab.binding.zigbee.telegesis.handler.TelegesisHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link TelegesisHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "org.openhab.binding.zigbee.telegesis")
@NonNullByDefault
public class TelegesisHandlerFactory extends BaseThingHandlerFactory {
    private Map<ThingUID, ServiceRegistration> discoveryServiceRegs = new HashMap<>();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(TelegesisBindingConstants.THING_TYPE_TELEGESIS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        ZigBeeCoordinatorHandler coordinator = null;
        if (thingTypeUID.equals(TelegesisBindingConstants.THING_TYPE_TELEGESIS)) {
            coordinator = new TelegesisHandler((Bridge) thing);

            ZigBeeDiscoveryService discoveryService = new ZigBeeDiscoveryService(coordinator);
            discoveryService.activate();

            discoveryServiceRegs.put(coordinator.getThing().getUID(), bundleContext.registerService(
                    DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));

            return coordinator;
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof TelegesisHandler) {
            ServiceRegistration serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                ZigBeeDiscoveryService service = (ZigBeeDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
