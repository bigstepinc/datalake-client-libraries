# Bigstep DataLake client libraries

These libraries enable the "dl://" prefix in hadoop and associated tools so that **hdfs dfs -ls** or **distcopy** work properly. They can also be used as a standalone FileSystem implementation to enable easy interaction with the DataLake from java or scala applications.

1. Before getting started deploy a "DataLake" in the Bigstep's Control panel.
2. Install Java Cryptography Extension on all nodes of the cluster.
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

To use directly in a maven use:
```xml
<dependency>
  <groupId>com.bigstep</groupId>
  <artifactId>datalake</artifactId>
  <version>1.0</version>
</dependency>
```

To compile use:
```bash
mvn package
```

More information can be found at [the DataLake documentation](https://fullmetal.bigstep.com/docs#documents/61).
