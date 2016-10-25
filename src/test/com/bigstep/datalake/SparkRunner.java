package com.bigstep.datalake;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.Serializable;

class SparkRunner  implements Serializable
{
    public SparkSession getSparkSession()
    {
        return  SparkSession
                .builder()
                .appName("JavaPageRank")
                .master("local[4]")
                .config("fs.dl.impl","DLFileSystem")
                .config("fs.dl.impl.kerberosPrincipal","k7@bigstep.io")
                .config("fs.dl.impl.kerberosKeytab","/etc/k7.keytab")
                .config("fs.dl.impl.kerberosRealm","bigstep.io")
                .config("fs.dl.impl.homeDirectory","/data_lake/dl267")
                .getOrCreate();

    }

    public void testPath(String path) throws IOException {


        SparkSession spark = getSparkSession();
        Path hdfsPath = new Path(path);
        FileSystem fs = hdfsPath.getFileSystem(spark.sessionState().newHadoopConf());
        Path qualified = hdfsPath.makeQualified(fs.getUri(), fs.getWorkingDirectory());
    }

    public void testSpark(String path)
    {
        SparkSession spark = getSparkSession();

        spark.read()
                .option("header", "true")
                .option("inferSchema","false")
                .csv(path);


    }

}