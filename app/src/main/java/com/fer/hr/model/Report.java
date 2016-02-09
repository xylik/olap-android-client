package com.fer.hr.model;

import com.fer.hr.rest.dto.discover.SaikuCube;

/**
 * Created by igor on 24/01/16.
 */
public class Report {
    private String reportName;
    private String mdx;
    private SaikuCube cube;

    public Report(String reportName, String mdx, SaikuCube cube) {
        this.reportName = reportName;
        this.mdx = mdx;
        this.cube = cube;
    }

    public String getMdx() {
        return mdx;
    }

    public void setMdx(String mdx) {
        this.mdx = mdx;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public SaikuCube getCube() {
        return cube;
    }

    public void setCube(SaikuCube cube) {
        this.cube = cube;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Report that = (Report) o;

        if (!reportName.equals(that.reportName)) return false;
        if (!mdx.equals(that.mdx)) return false;
        return cube.equals(that.cube);
    }

    @Override
    public int hashCode() {
        int result = reportName.hashCode();
        result = 31 * result + mdx.hashCode();
        result = 31 * result + cube.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return reportName;
    }
}
