package com.t1.officebooking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class Bounds {
    @Column(name = "x")
    private Integer x;

    @Column(name = "y")
    private Integer y;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    public Bounds(Integer x, Integer y, Integer width, Integer height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}


