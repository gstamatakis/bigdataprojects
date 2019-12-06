package Spark;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "runtime",
        "sparkProperties",
        "systemProperties",
        "classpathEntries"
})
public class SparkEnv {

    @JsonProperty("runtime")
    public SparkRuntime runtime;
    @JsonProperty("sparkProperties")
    public List<List<String>> sparkProperties = null;
    @JsonProperty("systemProperties")
    public List<List<String>> systemProperties = null;
    @JsonProperty("classpathEntries")
    public List<List<String>> classpathEntries = null;

    @Override
    public String toString() {
        return "SparkEnv{" +
                "runtime=" + runtime +
                ", sparkProperties=" + sparkProperties +
                ", systemProperties=" + systemProperties +
                ", classpathEntries=" + classpathEntries +
                '}';
    }
}

/**
 * http://rserver01:8189/proxy/application_1532029170450_0623/api/v1/applications/application_1532029170450_0623/environment
 * <p>
 * {
 * "runtime": {
 * "javaVersion": "1.8.0_77 (Oracle Corporation)",
 * "javaHome": "/usr/jdk64/jdk1.8.0_77/jre",
 * "scalaVersion": "version 2.11.8"
 * },
 * "sparkProperties": [
 * [
 * "spark.app.id",
 * "application_1532029170450_0455"
 * ],
 * [
 * "spark.app.name",
 * "NetworkWordCount"
 * ],
 * [
 * "spark.driver.appUIAddress",
 * "http://127.0.0.1:4040"
 * ],
 * [
 * "spark.driver.extraLibraryPath",
 * "/usr/hdp/current/hadoop-client/lib/native:/usr/hdp/current/hadoop-client/lib/native/Linux-amd64-64"
 * ],
 * [
 * "spark.driver.host",
 * "127.0.0.1"
 * ],
 * [
 * "spark.driver.memory",
 * "512m"
 * ],
 * [
 * "spark.driver.port",
 * "57737"
 * ],
 * [
 * "spark.eventLog.dir",
 * "hdfs:///spark2-history/"
 * ],
 * [
 * "spark.eventLog.enabled",
 * "true"
 * ],
 * [
 * "spark.executor.cores",
 * "1"
 * ],
 * [
 * "spark.executor.extraLibraryPath",
 * "/usr/hdp/current/hadoop-client/lib/native:/usr/hdp/current/hadoop-client/lib/native/Linux-amd64-64"
 * ],
 * [
 * "spark.executor.id",
 * "driver"
 * ],
 * [
 * "spark.executor.instances",
 * "3"
 * ],
 * [
 * "spark.executor.memory",
 * "512m"
 * ],
 * [
 * "spark.history.fs.logDirectory",
 * "hdfs:///spark2-history/"
 * ],
 * [
 * "spark.history.kerberos.keytab",
 * "none"
 * ],
 * [
 * "spark.history.kerberos.principal",
 * "none"
 * ],
 * [
 * "spark.history.provider",
 * "org.apache.spark.deploy.history.FsHistoryProvider"
 * ],
 * [
 * "spark.history.ui.port",
 * "18081"
 * ],
 * [
 * "spark.jars",
 * "file:/usr/hdp/2.6.3.0-235/spark2/examples/jars/spark-examples_2.11-2.2.0.2.6.3.0-235.jar"
 * ],
 * [
 * "spark.master",
 * "yarn"
 * ],
 * [
 * "spark.org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter.param.PROXY_HOSTS",
 * "rserver01"
 * ],
 * [
 * "spark.org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter.param.PROXY_URI_BASES",
 * "http://rserver01:8189/proxy/application_1532029170450_0455"
 * ],
 * [
 * "spark.scheduler.mode",
 * "FIFO"
 * ],
 * [
 * "spark.submit.deployMode",
 * "client"
 * ],
 * [
 * "spark.ui.filters",
 * "org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter"
 * ],
 * [
 * "spark.yarn.historyServer.address",
 * "rserver01:18081"
 * ],
 * [
 * "spark.yarn.queue",
 * "default"
 * ]
 * ],
 * "systemProperties": [
 * [
 * "SPARK_SUBMIT",
 * "true"
 * ],
 * [
 * "SPARK_YARN_MODE",
 * "true"
 * ],
 * [
 * "awt.toolkit",
 * "sun.awt.X11.XToolkit"
 * ],
 * [
 * "file.encoding",
 * "UTF-8"
 * ],
 * [
 * "file.encoding.pkg",
 * "sun.io"
 * ],
 * [
 * "file.separator",
 * "/"
 * ],
 * [
 * "hdp.version",
 * "2.6.3.0-235"
 * ],
 * [
 * "java.awt.graphicsenv",
 * "sun.awt.X11GraphicsEnvironment"
 * ],
 * [
 * "java.awt.printerjob",
 * "sun.print.PSPrinterJob"
 * ],
 * [
 * "java.class.version",
 * "52.0"
 * ],
 * [
 * "java.endorsed.dirs",
 * "/usr/jdk64/jdk1.8.0_77/jre/lib/endorsed"
 * ],
 * [
 * "java.ext.dirs",
 * "/usr/jdk64/jdk1.8.0_77/jre/lib/ext:/usr/java/packages/lib/ext"
 * ],
 * [
 * "java.home",
 * "/usr/jdk64/jdk1.8.0_77/jre"
 * ],
 * [
 * "java.io.tmpdir",
 * "/tmp"
 * ],
 * [
 * "java.library.path",
 * "/usr/hdp/current/hadoop-client/lib/native:/usr/hdp/current/hadoop-client/lib/native/Linux-amd64-64:/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib"
 * ],
 * [
 * "java.runtime.name",
 * "Java(TM) SE Runtime Environment"
 * ],
 * [
 * "java.runtime.version",
 * "1.8.0_77-b03"
 * ],
 * [
 * "java.specification.name",
 * "Java Platform API Specification"
 * ],
 * [
 * "java.specification.vendor",
 * "Oracle Corporation"
 * ],
 * [
 * "java.specification.version",
 * "1.8"
 * ],
 * [
 * "java.vendor",
 * "Oracle Corporation"
 * ],
 * [
 * "java.vendor.url",
 * "http://java.oracle.com/"
 * ],
 * [
 * "java.vendor.url.bug",
 * "http://bugreport.sun.com/bugreport/"
 * ],
 * [
 * "java.version",
 * "1.8.0_77"
 * ],
 * [
 * "java.vm.info",
 * "mixed mode"
 * ],
 * [
 * "java.vm.name",
 * "Java HotSpot(TM) 64-Bit Server VM"
 * ],
 * [
 * "java.vm.specification.name",
 * "Java Virtual Machine Specification"
 * ],
 * [
 * "java.vm.specification.vendor",
 * "Oracle Corporation"
 * ],
 * [
 * "java.vm.specification.version",
 * "1.8"
 * ],
 * [
 * "java.vm.vendor",
 * "Oracle Corporation"
 * ],
 * [
 * "java.vm.version",
 * "25.77-b03"
 * ],
 * [
 * "line.separator",
 * "\n"
 * ],
 * [
 * "os.arch",
 * "amd64"
 * ],
 * [
 * "os.name",
 * "Linux"
 * ],
 * [
 * "os.version",
 * "3.13.0-147-generic"
 * ],
 * [
 * "path.separator",
 * ":"
 * ],
 * [
 * "sun.arch.data.model",
 * "64"
 * ],
 * [
 * "sun.boot.class.path",
 * "/usr/jdk64/jdk1.8.0_77/jre/lib/resources.jar:/usr/jdk64/jdk1.8.0_77/jre/lib/rt.jar:/usr/jdk64/jdk1.8.0_77/jre/lib/sunrsasign.jar:/usr/jdk64/jdk1.8.0_77/jre/lib/jsse.jar:/usr/jdk64/jdk1.8.0_77/jre/lib/jce.jar:/usr/jdk64/jdk1.8.0_77/jre/lib/charsets.jar:/usr/jdk64/jdk1.8.0_77/jre/lib/jfr.jar:/usr/jdk64/jdk1.8.0_77/jre/classes"
 * ],
 * [
 * "sun.boot.library.path",
 * "/usr/jdk64/jdk1.8.0_77/jre/lib/amd64"
 * ],
 * [
 * "sun.cpu.endian",
 * "little"
 * ],
 * [
 * "sun.cpu.isalist",
 * ""
 * ],
 * [
 * "sun.io.unicode.encoding",
 * "UnicodeLittle"
 * ],
 * [
 * "sun.java.command",
 * "org.apache.spark.deploy.SparkSubmit --master yarn-client --conf spark.driver.memory=512m --class org.apache.spark.examples.streaming.NetworkWordCount --num-executors 3 --executor-memory 512m --executor-cores 1 examples/jars/spark-examples_2.11-2.2.0.2.6.3.0-235.jar localhost 9999"
 * ],
 * [
 * "sun.java.launcher",
 * "SUN_STANDARD"
 * ],
 * [
 * "sun.jnu.encoding",
 * "UTF-8"
 * ],
 * [
 * "sun.management.compiler",
 * "HotSpot 64-Bit Tiered Compilers"
 * ],
 * [
 * "sun.nio.ch.bugLevel",
 * ""
 * ],
 * [
 * "sun.os.patch.level",
 * "unknown"
 * ],
 * [
 * "user.country",
 * "US"
 * ],
 * [
 * "user.dir",
 * "/usr/hdp/2.6.3.0-235/spark2"
 * ],
 * [
 * "user.home",
 * "/home/gstamatakis"
 * ],
 * [
 * "user.language",
 * "en"
 * ],
 * [
 * "user.name",
 * "gstamatakis"
 * ],
 * [
 * "user.timezone",
 * "Europe/Athens"
 * ]
 * ],
 * "classpathEntries": [
 * [
 * "/etc/hadoop/conf/",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/conf/",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/JavaEWAH-0.3.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/RoaringBitmap-0.5.11.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/ST4-4.0.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/activation-1.1.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/aircompressor-0.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/antlr-2.7.7.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/antlr-runtime-3.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/antlr4-runtime-4.5.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/aopalliance-1.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/aopalliance-repackaged-2.4.0-b34.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/apache-log4j-extras-1.2.17.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/apacheds-i18n-2.0.0-M15.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/apacheds-kerberos-codec-2.0.0-M15.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/api-asn1-api-1.0.0-M20.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/api-util-1.0.0-M20.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/arpack_combined_all-0.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/avro-1.7.7.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/avro-ipc-1.7.7.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/avro-mapred-1.7.7-hadoop2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/aws-java-sdk-core-1.10.6.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/aws-java-sdk-kms-1.10.6.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/aws-java-sdk-s3-1.10.6.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/azure-keyvault-core-0.8.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/azure-storage-5.4.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/base64-2.3.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/bcprov-jdk15on-1.51.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/bonecp-0.8.0.RELEASE.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/breeze-macros_2.11-0.13.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/breeze_2.11-0.13.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/calcite-avatica-1.2.0-incubating.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/calcite-core-1.2.0-incubating.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/calcite-linq4j-1.2.0-incubating.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/chill-java-0.8.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/chill_2.11-0.8.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-beanutils-1.7.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-beanutils-core-1.8.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-cli-1.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-codec-1.10.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-collections-3.2.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-compiler-3.0.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-compress-1.4.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-configuration-1.6.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-crypto-1.0.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-dbcp-1.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-digester-1.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-httpclient-3.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-io-2.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-lang-2.6.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-lang3-3.5.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-logging-1.1.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-math3-3.4.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-net-2.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/commons-pool-1.5.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/compress-lzf-1.0.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/core-1.1.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/curator-client-2.6.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/curator-framework-2.6.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/curator-recipes-2.6.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/datanucleus-api-jdo-3.2.6.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/datanucleus-core-3.2.10.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/datanucleus-rdbms-3.2.9.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/derby-10.12.1.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/eigenbase-properties-1.1.5.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/gson-2.2.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/guava-14.0.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/guice-3.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/guice-servlet-3.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-annotations-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-auth-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-aws-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-azure-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-client-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-common-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-hdfs-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-mapreduce-client-app-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-mapreduce-client-common-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-mapreduce-client-core-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-mapreduce-client-jobclient-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-mapreduce-client-shuffle-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-openstack-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-yarn-api-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-yarn-client-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-yarn-common-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-yarn-registry-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-yarn-server-common-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hadoop-yarn-server-web-proxy-2.7.3.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hive-beeline-1.21.2.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hive-cli-1.21.2.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hive-exec-1.21.2.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hive-jdbc-1.21.2.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hive-metastore-1.21.2.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hk2-api-2.4.0-b34.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hk2-locator-2.4.0-b34.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/hk2-utils-2.4.0-b34.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/htrace-core-3.1.0-incubating.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/httpclient-4.5.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/httpcore-4.4.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/ivy-2.4.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-annotations-2.6.5.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-core-2.6.5.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-core-asl-1.9.13.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-databind-2.6.5.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-dataformat-cbor-2.6.5.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-jaxrs-1.9.13.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-mapper-asl-1.9.13.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-module-paranamer-2.6.5.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-module-scala_2.11-2.6.5.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jackson-xc-1.9.13.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/janino-3.0.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/java-xmlbuilder-1.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/javassist-3.18.1-GA.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/javax.annotation-api-1.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/javax.inject-1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/javax.inject-2.4.0-b34.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/javax.servlet-api-3.1.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/javax.ws.rs-api-2.0.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/javolution-5.5.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jaxb-api-2.2.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jcip-annotations-1.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jcl-over-slf4j-1.7.16.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jdo-api-3.0.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jersey-client-2.22.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jersey-common-2.22.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jersey-container-servlet-2.22.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jersey-container-servlet-core-2.22.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jersey-guava-2.22.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jersey-media-jaxb-2.22.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jersey-server-2.22.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jets3t-0.9.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jetty-6.1.26.hwx.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jetty-sslengine-6.1.26.hwx.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jetty-util-6.1.26.hwx.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jline-2.12.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/joda-time-2.9.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jodd-core-3.5.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jpam-1.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/json-smart-1.1.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/json4s-ast_2.11-3.2.11.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/json4s-core_2.11-3.2.11.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/json4s-jackson_2.11-3.2.11.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jsp-api-2.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jsr305-1.3.9.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jta-1.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jtransforms-2.4.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/jul-to-slf4j-1.7.16.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/kryo-shaded-3.0.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/leveldbjni-all-1.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/libfb303-0.9.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/libthrift-0.9.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/log4j-1.2.17.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/lz4-1.3.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/machinist_2.11-0.6.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/macro-compat_2.11-1.1.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/mail-1.4.7.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/metrics-core-3.1.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/metrics-graphite-3.1.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/metrics-json-3.1.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/metrics-jvm-3.1.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/minlog-1.3.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/mx4j-3.0.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/netty-3.9.9.Final.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/netty-all-4.0.43.Final.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/nimbus-jose-jwt-3.9.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/objenesis-2.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/okhttp-2.4.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/okio-1.4.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/opencsv-2.3.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/orc-core-1.4.1-nohive.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/orc-mapreduce-1.4.1-nohive.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/oro-2.0.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/osgi-resource-locator-1.0.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/paranamer-2.6.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/parquet-column-1.8.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/parquet-common-1.8.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/parquet-encoding-1.8.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/parquet-format-2.3.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/parquet-hadoop-1.8.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/parquet-hadoop-bundle-1.6.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/parquet-jackson-1.8.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/pmml-model-1.2.15.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/pmml-schema-1.2.15.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/protobuf-java-2.5.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/py4j-0.10.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/pyrolite-4.13.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/scala-compiler-2.11.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/scala-library-2.11.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/scala-parser-combinators_2.11-1.0.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/scala-reflect-2.11.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/scala-xml_2.11-1.0.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/scalap-2.11.8.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/shapeless_2.11-2.3.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/slf4j-api-1.7.16.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/slf4j-log4j12-1.7.16.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/snappy-0.2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/snappy-java-1.1.2.6.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-catalyst_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-cloud_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-core_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-graphx_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-hive-thriftserver_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-hive_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-launcher_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-mllib-local_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-mllib_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-network-common_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-network-shuffle_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-repl_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-sketch_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-sql_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-streaming_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-tags_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-unsafe_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spark-yarn_2.11-2.2.0.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spire-macros_2.11-0.13.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/spire_2.11-0.13.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/stax-api-1.0-2.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/stax-api-1.0.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/stream-2.7.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/stringtemplate-3.2.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/super-csv-2.2.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/univocity-parsers-2.2.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/validation-api-1.1.0.Final.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/xbean-asm5-shaded-4.4.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/xercesImpl-2.9.1.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/xmlenc-0.52.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/xz-1.0.jar",
 * "System Classpath"
 * ],
 * [
 * "/usr/hdp/current/spark2-client/jars/zookeeper-3.4.6.2.6.3.0-235.jar",
 * "System Classpath"
 * ],
 * [
 * "spark://127.0.0.1:57737/jars/spark-examples_2.11-2.2.0.2.6.3.0-235.jar",
 * "Added By User"
 * ]
 * ]
 * }
 **/