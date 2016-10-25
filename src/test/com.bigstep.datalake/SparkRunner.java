package com.bigstep.datalake;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.io.Serializable;

class SparkRunner  implements Serializable
{

    public void testSpark()
    {
        final long numAs, numBs;
        String logFile = "README.md"; // Should be some file on your system
        SparkConf conf = new SparkConf()
                .setAppName("Simple Application")
                .setMaster("local[4]");

        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> logData = sc.textFile(logFile).cache();

        numAs = logData.filter(new Function<String, Boolean>() {
            public Boolean call(String s) { return s.contains("a"); }
        }).count();

        numBs = logData.filter(new Function<String, Boolean>() {
            public Boolean call(String s) { return s.contains("b"); }
        }).count();
    }

}