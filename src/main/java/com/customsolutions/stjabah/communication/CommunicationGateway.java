package com.customsolutions.stjabah.communication;

import org.springframework.stereotype.Component;

@Component
public interface CommunicationGateway {
    // topic naming pattern {audience}/{domain}/{event}
    void broadcast(String topic, Object payload);
}
