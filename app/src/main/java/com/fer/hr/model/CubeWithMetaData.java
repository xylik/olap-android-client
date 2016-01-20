package com.fer.hr.model;

import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuCubeMetadata;

public class CubeWithMetaData {
    private SaikuCube cube;
    private SaikuCubeMetadata saikuCubeMetadata;

    public CubeWithMetaData(SaikuCube cube, SaikuCubeMetadata cubesMeta) {
        this.cube = cube;
        this.saikuCubeMetadata = cubesMeta;
    }

    public SaikuCube getCube() {
        return cube;
    }

    public void setCube(SaikuCube cube) {
        this.cube = cube;
    }

    public SaikuCubeMetadata getSaikuCubeMetadata() {
        return saikuCubeMetadata;
    }

    public void setSaikuCubeMetadata(SaikuCubeMetadata saikuCubeMetadata) {
        this.saikuCubeMetadata = saikuCubeMetadata;
    }
}