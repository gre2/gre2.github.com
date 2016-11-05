---
layout: post
title: "dbcp"
description: ""
category: [java,mysql]
tags: [java,mysql]
---
{% include JB/setup %}

### configuration

    1.connectionProperties

      desc  the connection properties will be send to JDBC driver when establishing new connections  

      1.properties

        useunicode                 true
        characterEncoding          UTF8
        connectionTimeout          300      establish socket connection timeout twith database 
        sockettimeout              160000   socket read and write timeout
        autoReconnect              true     whether the database connection interruption reconnection
        autoReconnectforPools      true     whether use reconnection to database pools
        initialTimeout             3        reconnection two intervals
        maxReconnects              1        reconnection count
        failOverReadOnly           false    after the reconnection if read-only
        roundRobinLoadBalance      true     When autoReconnect is enabled, and failoverReadonly is false, 
                                            should  we pick hosts to connect to on a round-robin basis?
        rewriteBatchedstatements   true     batch execution sql-----see reference

    1.other properties

      initialSize        			10
      maxActive          			100               max active connection size
      maxIdle            			80       		  max free connection size
      minIdle            			10       		  min free connection size
      validationQuery    			"select 1"        check sql
      timeBetweenEvictionRunsMillis 30000             Run free collector time
      minEvictableIdleTimeMillis    160000            the connection in database pools will be recycled in 160000
      numTestsPerEvictionRun        10                Run free collector examine nums once
      testWhileIdle                 true              the connection if examine by free collector
      testOnReturn                  false             
      testOnBorrow                  false             



1. [reference](http://dev.mysql.com/doc/connectors/en/connector-j-reference-configuration-properties.html)
1. [reference](http://commons.apache.org/proper/commons-dbcp/configuration.html)
1. [reference](http://www.cnblogs.com/chenjianjx/archive/2012/08/14/2637914.html)


