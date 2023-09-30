/* Licensed under Apache-2.0 2023. */
package com.example.catalog.mapper;

import com.example.catalog.projection.item.ItemProjection;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import org.mapstruct.Mapper;

@Mapper
public interface FindOneResponseDtoMapper {

  FindOneResponseDto map(ItemProjection itemProjection);
}
