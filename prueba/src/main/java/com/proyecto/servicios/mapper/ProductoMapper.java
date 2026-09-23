package com.proyecto.servicios.mapper;

import com.proyecto.servicios.document.gestopago.ProductoDocument;
import com.proyecto.servicios.model.gestopago.product.GestoPagoProduct;
import com.proyecto.servicios.model.producto.ProductoResponse;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    ProductoDocument toDocument(GestoPagoProduct product, LocalDateTime fechaSincronizacion);

    ProductoResponse toResponse(ProductoDocument document);

    List<ProductoResponse> toResponses(List<ProductoDocument> documents);
}
