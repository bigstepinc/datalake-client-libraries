package com.bigstep.datalake;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.web.resources.GetOpParam;
import org.apache.hadoop.hdfs.web.resources.HttpOpParam;
import org.apache.hadoop.hdfs.web.resources.Param;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.security.SecureRandom;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 * Created by alex on 10/25/16.
 * To run these unit tests you need a working datalake,  configured (by editing test/resources/core-site.xml) with the BDB dataset in the root dir:
 * eg /baseballdatabank-master/core/AllstarFull.csv must be present. It is available in the
 * src/test/resources directory. Eventualy this will be done automatically.
 * Full dataset available here http://www.baseball-databank.org/files/BDB-csv-2011-03-28.zip
 */
public class TestDLFileSystem {

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

    /**
     * Uploads a fixture before starting the tests. If this fails check your datalake config.
     * @throws IOException
     * @throws URISyntaxException
     */
    @Before
    public void setupFixturesInDatalake() throws IOException, URISyntaxException {
        String path = getBasePath()+"/baseballdatabank-master/core/AllstarFull.csv";
        Path hdfsPath = new Path(path);

        FileSystem fs = hdfsPath.getFileSystem(getConf());

        if(!fs.exists(hdfsPath)){
            Path localFilePath = new Path(getClass().getResource("/baseballdatabank-master/core/AllstarFull.csv").toURI().getRawPath());
            fs.mkdirs(new Path("/baseballdatabank-master"));
            fs.mkdirs(new Path("/baseballdatabank-master/core"));
            fs.copyFromLocalFile(localFilePath, hdfsPath);
        }
    }

    @Test
    public void testShellPut() throws Exception {
        SecureRandom random = new SecureRandom();
        String randomNameSuffix = new BigInteger(130, random).toString(32);;

        String pathSrc = getClass().getResource("/baseballdatabank-master/core/test_file4.txt").toURI().getRawPath();
        String pathDest = getBasePath()+"/enc_test/AllstarFull" + randomNameSuffix + ".csv";

        FsShell shell = new FsShell();
        shell.setConf(getConf());

        String[] argv = {"-put", pathSrc, pathDest};
        shell.run(argv);
    }

    @Test
    public void testFSPut() throws Exception {
        SecureRandom random = new SecureRandom();
        String randomNameSuffix = new BigInteger(130, random).toString(32);;
        String path = getBasePath()+"/baseballdatabank-master/core/AllstarFull" + randomNameSuffix + ".csv";

        Path hdfsPath = new Path(path);

        FileSystem fs = hdfsPath.getFileSystem(getConf());

        Path localFilePath = new Path(getClass().getResource("/baseballdatabank-master/core/AllstarFull.csv").toURI().getRawPath());
        fs.mkdirs(new Path("/baseballdatabank-master"));
        fs.mkdirs(new Path("/baseballdatabank-master/core"));
        fs.copyFromLocalFile(localFilePath, hdfsPath);
    }

    @Test
    public void testShellGet() throws Exception {
        String pathSrc = getBasePath()+"/enc_test/AllstarFull.csv";
        String pathDest = "/C:/ExportVHosts/datalake-client-libraries/target/test-classes/baseballdatabank-master/core/AllstarFull_DEC.csv";

        FsShell shell = new FsShell();
        shell.setConf(getConf());

        String[] argv = {"-get", pathSrc, pathDest};
        shell.run(argv);
    }

    @Test
    public void testShellText() throws Exception {
        String pathSrc = getBasePath()+"/enc_test/test_append.csv";

        FsShell shell = new FsShell();
        shell.setConf(getConf());

        String[] argv = {"-text", pathSrc};
        shell.run(argv);
    }

    @Test
    public void testGetFileStatus() throws IOException {
        String path = getBasePath()+"/baseballdatabank-master/core/AllstarFull.csv";
        Path hdfsPath = new Path(path);

        FileSystem fs = hdfsPath.getFileSystem(getConf());
        FileStatus status=fs.getFileStatus(hdfsPath);

        assertEquals(path, status.getPath().toString());
    }

    @Test
    public void testListFileStatus() throws IOException {
        String path = getBasePath()+"/baseballdatabank-master/core/AllstarFull.csv";
        Path hdfsPath = new Path(path);
        FileSystem fs = hdfsPath.getFileSystem(getConf());

        FileStatus[] status=fs.listStatus(hdfsPath);

        assertEquals(path, status[0].getPath().toString());
    }

    @Test
    public void testAppendToFile() throws Exception {
        String pathSrc = getClass().getResource("/baseballdatabank-master/core/test_file1.csv").toURI().getRawPath();
        String pathDest = getBasePath()+"/enc_test/test_append.csv";

        FsShell shell = new FsShell();
        shell.setConf(getConf());

        String[] argv = {"-put", pathSrc, pathDest};
        shell.run(argv);

        String pathSrc2 = getClass().getResource("/baseballdatabank-master/core/test_file2.csv").toURI().getRawPath();

        String[] argv2 = {"-appendToFile", pathSrc2, pathDest};
        shell.run(argv2);
        shell.run(argv2);
    }

    @Test
    public void testListFileStatusOnDirectory() throws IOException {
        String path = getBasePath()+"/baseballdatabank-master/core";
        Path hdfsPath = new Path(path);
        Configuration conf= new Configuration();
        conf.addResource(getClass().getClassLoader().getResource("core-site.xml").getFile());

        FileSystem fs = hdfsPath.getFileSystem(conf);
        FileStatus[] status=fs.listStatus(hdfsPath);
        assertEquals(1, status.length);
        assertEquals(path.concat("/AllstarFull.csv"), status[0].getPath().toString());
    }

    @Test
    public void testgetCanonicalUriShort() throws IOException {
        String path = "/baseballdatabank-master/core";
        Path hdfsPath = new Path(path);
        DLFileSystem fs = (DLFileSystem)hdfsPath.getFileSystem(getConf());

        URI uri = fs.getCanonicalUri();

        assertEquals(getBasePath(),uri.toString());
    }

    @Test
    public void testgetCanonicalUriLong() throws IOException {
        String path = getBasePath()+"baseballdatabank-master/core";
        Path hdfsPath = new Path(path);
        DLFileSystem fs = (DLFileSystem)hdfsPath.getFileSystem(getConf());

        URI uri = fs.getCanonicalUri();

        assertEquals(getBasePath(),uri.toString());
    }

    @Test
    public void testListFileStatusOnHomeDirectoryShort() throws IOException {
        String path = "/baseballdatabank-master/core";
        Path hdfsPath = new Path(path);
        Configuration conf= new Configuration();
        conf.addResource(getClass().getClassLoader().getResource("core-site.xml").getFile());

        DLFileSystem fs = (DLFileSystem)hdfsPath.getFileSystem(conf);
        FileStatus status=fs.getFileStatus(hdfsPath);

        assertEquals(getBasePath()+"/baseballdatabank-master/core", status.getPath().toString());
    }


    @Test
    public void testmakeQualifiedShort() throws IOException {
        String path = "/baseballdatabank-master/core";
        Path fsPath = new Path(path);

        Configuration conf= getConf();
        String homeDir = conf.get(DLFileSystem.FS_DL_IMPL_HOME_DIRECTORY);


        DLFileSystem fs = (DLFileSystem)fsPath.getFileSystem(conf);

        String qualifiedPath=fs.makeQualified(fsPath).toUri().getRawPath();

        assertEquals(homeDir+"/baseballdatabank-master/core", qualifiedPath);
    }


    @Test
    public void testmakeQualifiedWithDLDir() throws IOException {



        Configuration conf= getConf();
        String homeDir = conf.get(DLFileSystem.FS_DL_IMPL_HOME_DIRECTORY);

        String path = homeDir+"/baseballdatabank-master/core";
        Path fsPath = new Path(path);


        DLFileSystem fs = (DLFileSystem)fsPath.getFileSystem(conf);

        String qualifiedPath=fs.makeQualified(fsPath).toUri().getRawPath();

        assertEquals(homeDir+"/baseballdatabank-master/core", qualifiedPath);
    }

    @Test
    public void testmakeQualifiedFull() throws IOException {
        String path = getBasePath()+"/baseballdatabank-master/core";
        Path fsPath = new Path(path);
        Configuration conf= getConf();
        String homeDir = conf.get(DLFileSystem.FS_DL_IMPL_HOME_DIRECTORY);

        DLFileSystem fs = (DLFileSystem)fsPath.getFileSystem(conf);

        String qualifiedPath=fs.makeQualified(fsPath).toUri().getRawPath();

        assertEquals(homeDir+"/baseballdatabank-master/core", qualifiedPath);
    }


    @Test
    public void testmakeQualifiedJustSlash() throws IOException {
        String path = "/";
        Path fsPath = new Path(path);
        Configuration conf= getConf();
        String homeDir = conf.get(DLFileSystem.FS_DL_IMPL_HOME_DIRECTORY);

        DLFileSystem fs = (DLFileSystem)fsPath.getFileSystem(conf);

        String qualifiedPath=fs.makeQualified(fsPath).toUri().getRawPath();

        assertEquals(homeDir, qualifiedPath);
    }


    @Test
    public void testListFileStatusOnHomeDirectory() throws IOException {
        String path = "/";
        Path hdfsPath = new Path(path);

        DLFileSystem fs = (DLFileSystem)hdfsPath.getFileSystem(getConf());
        FileStatus status=fs.getFileStatus(hdfsPath);

        assertEquals(getBasePath(), status.getPath().toString());
    }


}
