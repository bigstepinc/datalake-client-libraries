package com.bigstep.datalake;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by alex on 10/25/16.
 */
public class TestDLFileSystem {


    @Test
    public void testListStatus() throws IOException {
        String path = "dl://node10930-datanodes-data-lake01-uk-reading.bigstep.io:14000/data_lake/dl267/baseballdatabank-master/core/AllstarFull.csv";
        Path hdfsPath = new Path(path);
        Configuration conf= new Configuration();
        conf.addResource(getClass().getClassLoader().getResource("core-site.xml").getFile());

        FileSystem fs = hdfsPath.getFileSystem(conf);
        Path qualified = hdfsPath.makeQualified(fs.getUri(), fs.getWorkingDirectory());
    }


    @Test
    public void testGetFileStatus() throws IOException {
        String path = "dl://node10930-datanodes-data-lake01-uk-reading.bigstep.io:14000/data_lake/dl267/baseballdatabank-master/core/AllstarFull.csv";
        Path hdfsPath = new Path(path);
        Configuration conf= new Configuration();
        conf.addResource(getClass().getClassLoader().getResource("core-site.xml").getFile());

        FileSystem fs = hdfsPath.getFileSystem(conf);
        FileStatus status=fs.getFileStatus(hdfsPath);

    }

    @Test
    public void testListFileStatus() throws IOException {
        String path = "dl://node10930-datanodes-data-lake01-uk-reading.bigstep.io:14000/data_lake/dl267/baseballdatabank-master/core/AllstarFull.csv";
        Path hdfsPath = new Path(path);
        Configuration conf= new Configuration();
        conf.addResource(getClass().getClassLoader().getResource("core-site.xml").getFile());

        FileSystem fs = hdfsPath.getFileSystem(conf);
        FileStatus[] status=fs.listStatus(hdfsPath);

        assertEquals(path, status[0].getPath().toString());
    }

}
