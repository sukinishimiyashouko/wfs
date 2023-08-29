package com.wbu.controller;

import com.wbu.BO.ServerInfo;
import com.wbu.DTO.ServerInfoDTO;
import com.wbu.response.CommonResponse;
import com.wbu.services.DiscoveryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Validated
@RestController
@RequestMapping("/")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @PostMapping("/register")
    public CommonResponse<?> register(@RequestBody @Valid ServerInfoDTO serverInfoDTO){
        discoveryService.register(serverInfoDTO);
        return CommonResponse.success();
    }

    @PutMapping("/heartbeat")
    public void heartbeat(@RequestBody @Valid ServerInfoDTO serverInfo){
        discoveryService.heartbeat(serverInfo);
    }

    @PostMapping("/services")
    public CommonResponse<?> services(){
        Map<String, List<ServerInfo>> map = discoveryService.services();
        return CommonResponse.success(map);
    }

}
