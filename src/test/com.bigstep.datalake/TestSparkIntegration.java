package com.bigstep.datalake;


import org.junit.Test;
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;


/**
 * Created by alex on 10/24/16.
 */
public class TestSparkIntegration {

    @Test
    public void testSpark()
    {
            String logFile = "README.md"; // Should be some file on your system
            SparkConf conf = new SparkConf().setAppName("Simple Application");
            JavaSparkContext sc = new JavaSparkContext(conf);
            JavaRDD<String> logData = sc.textFile(logFile).cache();

            long numAs = logData.filter(new Function<String, Boolean>() {
                public Boolean call(String s) { return s.contains("a"); }
            }).count();

            long numBs = logData.filter(new Function<String, Boolean>() {
                public Boolean call(String s) { return s.contains("b"); }
            }).count();

    }

}
