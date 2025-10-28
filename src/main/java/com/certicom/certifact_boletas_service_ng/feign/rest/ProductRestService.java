package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.ProductDto;
import org.springframework.stereotype.Service;


@Service
public class ProductRestService {

    public Long findProductIdByCodigoOrDescripcion(String codigoProducto, String descripcion) {
        return null;
    }


    public ProductDto findById(Long productoId) {
        return null;
    }

    public void modificarStockPorIdProductoDB(Long productoId, long l) {


    }
}
