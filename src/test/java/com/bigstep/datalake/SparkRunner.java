package com.bigstep.datalake;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import scala.collection.immutable.Seq;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * The spark runner class is required as Junit classes are not serializable and there is a
 * spark requirement to be able to serialize the enclosing class.
 */
class SparkRunner  implements Serializable
{
    public SparkSession getSparkSession()
    {
        return  SparkSession
                .builder()
                .appName("spark")
                .master("local[4]")
                .getOrCreate();

    }

    public JavaSparkContext getSparkContext()
    {
        return new JavaSparkContext(getSparkSession().sparkContext());
    }


    public List testCSVRead(String path)
    {
        SparkSession spark = getSparkSession();

        Dataset df=spark.read()
                .option("header", "true")
                .option("inferSchema","false")
                .csv(path).toDF();

        return df.collectAsList();
    }

    public List<Row>  testParquet(List<Integer> data, String path)
    {
        SparkSession spark = getSparkSession();
        JavaSparkContext sc = getSparkContext();
        SQLContext sqlContext = spark.sqlContext();

        JavaRDD<Integer> distData = sc.parallelize(data);

        JavaRDD<IntegerSchema> rowRDD = distData.map(new Function<Integer, IntegerSchema>() {
            @Override
            public IntegerSchema call(Integer val) {
                IntegerSchema record = new IntegerSchema();
                record.setVal(val);
                return record;
            }
        });

        Dataset<Row> df = sqlContext.createDataFrame(rowRDD, IntegerSchema.class);

        df.write().parquet(path);

        Dataset<Row> df2 = sqlContext.read().parquet(path);
        return df2.collectAsList();
    }


}