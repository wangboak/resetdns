package com.wangboak.resetdns.main;

import lombok.Data;

@Data
public class Config {

    private String accessKey;

    private String secretKey;

    private String hostname;

    private String domain;
}
