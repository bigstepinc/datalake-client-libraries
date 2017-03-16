mvn package
cd target
tar -xvsf datalake-1.5-SNAPSHOT-bin.tar.gz
cd .. 
cp _REFRESH/core-site.xml target/datalake-1.5-SNAPSHOT/conf/core-site.xml
cp _REFRESH/dl target/datalake-1.5-SNAPSHOT/bin/dl
