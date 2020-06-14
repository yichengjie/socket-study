package com.yicj.study.client.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ServerInfo {
    private int port;
    private String ip;
    private String sn;
}