@echo off
setlocal enabledelayedexpansion

set KDC=bsiintegration.bigstepcloud.com
set REALM=integration.bigstepcloud.com

set bin=%~dp0
set LIB=%bin%..\lib\
set CONF=%bin%..\conf\

set KRB5_CONFIG=%CONF%krb5.conf
set DEBUG_KERBEROS=false
set JARS=%LIB%*
set CLASSPATH=%CONF%;%JARS%

if "%1"=="genkeytab" (
	set CLASS=com.bigstep.datalake.KeytabUtil
) else (
	set CLASS=org.apache.hadoop.fs.FsShell
)

java -cp %CLASSPATH% -Djava.security.krb5.conf=%KRB5_CONFIG% -Djava.security.krb5.kdc=%KDC% -Djava.security.krb5.realm=%REALM% -Dsun.security.krb5.debug=%DEBUG_KERBEROS% %CLASS% %*