# Bigstep DataLake client libraries

These libraries enable the "dl://" prefix in hadoop and associated tools so that **hdfs dfs -ls** or **distcopy** work properly. They can also be used as a standalone FileSystem implementation to enable easy interaction with the DataLake from java or scala applications.
They also build with a standalone version of "hdfs dfs".

[![Build Status](https://travis-ci.org/bigstepinc/datalake-client-libraries.svg?branch=master)](https://travis-ci.org/bigstepinc/datalake-client-libraries)

###Before getting started: 

1. Deploy a "DataLake" in the Bigstep's Control panel.

2. Install Java Cryptography Extension on all nodes of the cluster.

###Using the standalone tool
The command line tool should be independent of the environment. It can coexist with any kerberos instalation.
To use:
0. Have java installed, have the unlimited strenght policy files installed (openJDK includes them)
1. Download the binaries

[latest SNAPSHOT](https://github.com/bigstepinc/datalake-client-libraries/releases/latest)

2. Generate a keytab:

```bash
 ./bin/dl genkeytab kxxx@bigstep.io /etc/kxxxx.keytab
```
3. Add your kerberos identity to the embeded core-site.xml:
```
vim conf/core-site.xml
```

3. Execute any hdfs dfs command
 ./bin/dl fs -ls dl://node10930-datanodes-data-lake01-uk-reading.bigstep.io:14000/data_lake/dlxxx/


###Using as part of a Hadoop environment:

To use the library within a hadoop environment:


3. Install kerberos client libraries:
  Centos:
  ```bash
  yum install krb5-workstation
  ```
  Ubuntu:
  ```bash
  apt-get install krb5-config, krb5-user, krb5-clients
  ```
  OsX:
  ```bash
  brew install krb5
  ```
4. Update the **/etc/krb5.conf** or download the auto-generated file from you Bigstep account:
  
  ```ini
  [appdefaults]
  validate=false
  
  [libdefaults]
   default_realm =  bigstep.io
   dns_lookup_realm = false
   dns_lookup_kdc = false
   ticket_lifetime = 24h
   renew_lifetime = 7d
   forwardable = true
   validate=false
   rdns = false
   ignore_acceptor_hostname = true
   udp_preference_limit = 1
   
  default_tgs_enctypes = aes256-cts-hmac-sha1-96 aes128-cts-hmac-sha1-96 des3-cbc-sha1 arcfour-hmac-md5 camellia256-cts-cmac camellia128-cts-cmac des-cbc-crc des-cbc-md5 des-cbc-md4
  default_tkt_enctypes = aaes256-cts-hmac-sha1-96 aes128-cts-hmac-sha1-96 des3-cbc-sha1 arcfour-hmac-md5 camellia256-cts-cmac camellia128-cts-cmac des-cbc-crc des-cbc-md5 des-cbc-md4
  permitted_enctypes = aes256-cts-hmac-sha1-96 aes128-cts-hmac-sha1-96 des3-cbc-sha1 arcfour-hmac-md5 camellia256-cts-cmac camellia128-cts-cmac des-cbc-crc des-cbc-md5 des-cbc-md4
  
  [realms]
    bigstep.io = {
    kdc = fullmetal.bigstep.com
    admin_server = fullmetal.bigstep.com
    max_renewable_life = 5d 0h 0m 0s
   }
   ```

5. Add the following to **core-site.xml**. If using Spark standalone place this file in the **spark-2.x.x/conf** directory:
  
  ```xml
  <property>
    <name>fs.dl.impl</name>
    <value>com.bigstep.datalake.DLFileSystem</value>
  </property>
  
  <property>
    <name>fs.dl.impl.kerberosPrincipal</name>
    <value>kxxx@bigstep.io</value>
  </property>
  
  <property>
    <name>fs.dl.impl.kerberosKeytab</name>
    <value>/etc/hadoop/kx.keytab</value>
  </property>
  
  <property>
    <name>fs.dl.impl.kerberosRealm</name>
    <value>bigstep.io</value>
  </property>
  
  <property>
    <name>fs.dl.impl.homeDirectory</name>
    <value>/data_lake/dlxxxx</value>
  </property>
  
  <!-- optional -->
  <property>
    <name>fs.dl.impl.defaultFilePermissions</name>
    <value>00640</value>
  </property>
  
  <!-- optional -->
  <property>
    <name>fs.dl.impl.defaultUMask</name>
    <value>007</value>
  </property>
  ```

5. Create a keytab:
  ```bash
  # ktutil
  ktutil:  addent -password -p kXXX@bigstep.io -k 1 -e aes256-cts
  ktutil:  wkt /root/.k5keytab
  ktutil:  exit
  ```
  
6. Make sure that the jar is available on all the cluster machines. Also the keytab must be reachable to yarn user (eg: not /root).
  * Vanilla Hadoop  **hadoop-2.7.x/share/hadoop/common/**
  * Vanilla Spark 2.0 **spark-2.x.x/jars**
  * Cloudera CDH 5.x  **/opt/cloudera/parcels/CDH/lib/hadoop/**
  * It should be possible technically to be used with any hadoop enabled application as long as it is added to the classpath. 

You should now be able to use regular hadoop commands like distcp:
```bash
hadoop distcp hdfs://localhost/user/hdfs/test dl://node10930-datanodes-data-lake01-uk-reading.bigstep.io:14000/data_lake/dlzzz
```

##Using as part of a Spark environment:
1. Extract a vanilla spark 2.* directory
2. Copy the datalake-*.jar in the jars directory of spark-2.*
```
cp datalake-client-libraries-*.jar jars
```
4. Modify core-site.xml to include the above
5. Start spark-shell
6. 

###Using via Spark
```bash
./bin/spark-shell --packages com.bigstep:datalake-client-libraries:1.4
```
###Using programatically:
To use directly in a maven use:
```xml
<dependency>
  <groupId>com.bigstep</groupId>
  <artifactId>datalake-client-libraries</artifactId>
  <version>1.4</version>
</dependency>
```
Javadoc is available from maven central.

To compile use:
```bash
mvn package
```

More information can be found at [the DataLake documentation](https://fullmetal.bigstep.com/docs#documents/61).


###Troubleshooting
You can find out a lot more about what the library is doing by enabling debug:. Change the log4j.rootLogger to DEBUG in conf/log4j.properties file:
```
log4j.rootLogger=DEBUG, A1
```

```
Key for the principal kxxx@bigstep.io not available in /etc/kxxx.keytab
		[Krb5LoginModule] authentication failed
Unable to obtain password from user
```
Try enabling Kerberos debug via:
```
 export KERBEROS_DEBUG=true
```
If you see something along the lines of:
```
Found unsupported keytype (18) for k7@bigstep.io
```
You need to enable AES 256 (assuming it's not illegal in your country)
by adding the "Unlimited Strength JCE" policy files. Follow your operating system guides for installing that.
For Mac Try:
http://bigdatazone.blogspot.ro/2014/01/mac-osx-where-to-put-unlimited-jce-java.html

If a command stalls for an unknown reason and then timeouts with KDC not found try adding the following in the [libdefaults] section of your /etc/krb5.conf file:
```
udp_preference_limit=1
```

If you see errors related to the missing ```StringUtil.toLowerCase``` try the hadoop2.6 branch as the master is linked to hadoop-2.7.

### Using file encryption
It is possible to encrypt/decrypt files when uploading/downloading from the datalake. To enable this, add the following properties to `core-site.xml`:
```xml
  <!-- This tells the DataLake client if it should encrypt/decrypt files when
  uploading/downloading. If property is missing, the default value is false. -->
  <property>
    <name>fs.dl.impl.shouldUseEncryption</name>
    <value>true</value>
  </property>
  
  <!-- The location of the AES key. The file should be exactly 16 bytes long.
  This property is required if fs.fl.impl.shouldUseEncryption is set to true. -->
  <property>
    <name>fs.dl.impl.encryptionKeyPath</name>
    <value>/etc/PUT_YOUR_KEY_PATH_HERE</value>
  </property>
```
By setting `fs.dl.impl.shouldUseEncryption` to true, you should also provide the path to the file containing the AES key in `fs.dl.impl.encryptionKeyPath` (the file should be exactly 16 bytes long). This will enable encrypting or decrypting of files when uploading or appending, or, respectively, downloading them.
You can easily disable this by setting `fs.dl.impl.shouldUseEncryption` to false.
