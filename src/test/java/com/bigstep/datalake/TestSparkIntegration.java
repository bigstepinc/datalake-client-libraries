package com.bigstep.datalake;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.junit.Before;
import org.junit.Test;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by alex on 10/24/16.
 * To run these tests a working datalake must be available and configured in the test/resources/core-site.xml.
 */
public class TestSparkIntegration {

    private Configuration getConf()
    {
        Configuration conf= new Configuration();
        conf.addResource(getClass().getClassLoader().getResource("core-site.xml").getFile());
        return conf;
    }

    public String getBasePath()
    {
        return getConf().get(FileSystem.FS_DEFAULT_NAME_KEY);
    }

    @Before
    public void setupFixture() throws IOException, URISyntaxException {
        TestDLFileSystem tmp = new TestDLFileSystem();
        tmp.setupFixturesInDatalake();
    }

    @Test
    public void testCSVRead()
    {
        SparkRunner runner= new SparkRunner();
        List results = runner.testCSVRead(getBasePath()+"/baseballdatabank-master/core/AllstarFull.csv");
        assertEquals(5069,results.size());
        GenericRowWithSchema row = (GenericRowWithSchema) results.get(10);
        assertEquals("crowdal01", row.values()[0]);
    }



    @Test
    public void testParquetReadWriteFullPath()
    {

        String path = getBasePath()+"/df-"+System.currentTimeMillis()+".parquet";

        SparkRunner runner= new SparkRunner();

        List<Integer> arr = Arrays.asList(1, 2, 3, 4, 5);

        List<Row> df2List= runner.testParquet(arr, path);

        assertEquals(5,df2List.size());

    }

    @Test
    public void testParquetReadWriteShortPath()
    {

        String path = "/df-"+System.currentTimeMillis()+".parquet";

        SparkRunner runner= new SparkRunner();

        List<Integer> arr = Arrays.asList(1, 2, 3, 4, 5);

        List<Row> df2List= runner.testParquet(arr, path);

        assertEquals(5,df2List.size());

    }





}


