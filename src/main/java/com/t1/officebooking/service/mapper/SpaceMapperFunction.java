package com.t1.officebooking.service.mapper;

import com.t1.officebooking.dto.response.SpaceResponse;
import com.t1.officebooking.model.Bounds;
import com.t1.officebooking.model.Space;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class SpaceMapperFunction implements Function<Space, SpaceResponse> {
    @Override
    public SpaceResponse apply(Space space) {
        if (space == null) return null;

        SpaceResponse.BoundsResponse boundsResponse = null;
        Bounds b = space.getBounds();
        if (b != null) {
            boundsResponse = SpaceResponse.BoundsResponse.builder()
                    .x(b.getX())
                    .y(b.getY())
                    .width(b.getWidth())
                    .height(b.getHeight())
                    .build();
        }

        return SpaceResponse.builder()
                .id(space.getId())
                .locationId(space.getLocation().getId())
                .spaceTypeId(space.getSpaceType().getId())
                .spaceType(space.getSpaceType().getType())
                .capacity(space.getCapacity())
                .floor(space.getFloor())
                .bookable(space.isBookable())
                .bounds(boundsResponse)
                .build();
    }
}


