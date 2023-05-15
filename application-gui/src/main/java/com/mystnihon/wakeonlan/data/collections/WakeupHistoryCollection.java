package com.mystnihon.wakeonlan.data.collections;

import com.mystnihon.nitrite.annotations.Collection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Collection
@NoArgsConstructor
public class WakeupHistoryCollection implements Serializable {

    @Id
    private UUID documentId;
    private String host;
    private String macAddress;
    private int port;
    private OffsetDateTime offsetDateTime;

    public WakeupHistoryCollection(String host, String macAddress, int port) {
        documentId = UUID.randomUUID();
        this.host = host;
        this.macAddress = macAddress;
        this.port = port;
        offsetDateTime = OffsetDateTime.now();
    }
}
