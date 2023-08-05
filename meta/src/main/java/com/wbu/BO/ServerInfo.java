package com.wbu.BO;

import lombok.Data;

import java.util.Objects;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
public class ServerInfo {
    private String ServiceId;
    private String host;
    private String port;
    private String schema;
    private Long preTimeStamp;
    private Boolean alive;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo info = (ServerInfo) o;
        return Objects.equals(ServiceId, info.ServiceId) && Objects.equals(host, info.host) && Objects.equals(port, info.port) && Objects.equals(schema, info.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ServiceId, host, port, schema);
    }
}
