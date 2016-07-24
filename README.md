# hadoop-bigstep
Bigstep support libraries for hadoop.

This is usefull if you want to be able to interact with datasets stored in Bigstep's Datalake. 

To use, first copy the jar to :
1. Vanilla Hadoop : hadoop-2.7.x/share/hadoop/tools/
2. CDH: /opt/cloudera/parcels/CDH/lib/hadoop/

first add the following to core-site.xml:

```xml
<property>
  <name>fs.dl.impl</name>
  <value>org.apache.hadoop.fs.dl.DLFileSystem</value>
</property>

<property>
  <name>fs.dl.impl.kerberosPrincipal</name>
  <value>kxxx@bigstep.io</value>
</property>

<property>
  <name>fs.dl.impl.kerberosKeytab</name>
  <value>/Users/alexandrubordei/code/hadoop/hadoop-2.7.2/etc/hadoop/k7.keytab</value>
</property>

<property>
  <name>fs.dl.impl.homeDirectory</name>
  <value>/data_lake/dl267</value>
</property>

<property>
  <name>fs.dl.impl.kerberosRealm</name>
  <value>bigstep.io</value>
</property>
```

Make sure that the jar is available on all the cluster machines. Also the keytab must be reachable to yarn user (eg: not /root).

Then you can use regular hadoop commands like distcp:
```bash
hadoop distcp hdfs://localhost/user/hdfs/test dl://node10930-datanodes-data-lake01-uk-reading.bigstep.io:14000/data_lake/dlzzz
```

To compile copy the directory into the hadoop-2.7.1-src/hadoop-tools/ directory:
```bash
cd hadoop-2.7.1-src/hadoop-tools
git clone <this-repo>
cd hadoop-2.7.1-src/hadoop-tools/hadoop-bigstep
mvn package
```
