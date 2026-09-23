package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoProductFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "gestoPagoProduct",
        url = "${gestopago.product.url}",
        configuration = GestoPagoProductFeignConfig.class
)
public interface GestoPagoProductClient {

    @GetMapping("/sistema/service/getProductList.do")
    byte[] getProductList();
}