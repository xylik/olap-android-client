package com.fer.hr.model;

import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuCubeMetadata;

public class CubeMeta {
    private SaikuCube cube;
    private SaikuCubeMetadata cubesMeta;

    public CubeMeta(SaikuCube cube, SaikuCubeMetadata cubesMeta) {
        this.cube = cube;
        this.cubesMeta = cubesMeta;
    }

    public SaikuCube getCube() {
        return cube;
    }

    public void setCube(SaikuCube cube) {
        this.cube = cube;
    }

    public SaikuCubeMetadata getCubesMeta() {
        return cubesMeta;
    }

    public void setCubesMeta(SaikuCubeMetadata cubesMeta) {
        this.cubesMeta = cubesMeta;
    }
}