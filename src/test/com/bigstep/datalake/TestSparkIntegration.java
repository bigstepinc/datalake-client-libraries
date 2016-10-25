package com.bigstep.datalake;


import org.junit.Test;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * Created by alex on 10/24/16.
 */
public class TestSparkIntegration {

    static String path = "dl://node10930-datanodes-data-lake01-uk-reading.bigstep.io:14000/data_lake/dl267/baseballdatabank-master/core/AllstarFull.csv";

    //@Test
    public void testPathBuilding() throws IOException {
        SparkRunner runner= new SparkRunner();
        runner.testPath(path);
    }

    @Test
    public void testSpark()
    {
        SparkRunner runner= new SparkRunner();
        runner.testSpark(path);
    }

}


