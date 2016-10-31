---
layout: post
title: "java collection"
description: ""
category: "java"
tags: "java"
---
{% include JB/setup %}

### Map ergodic
    
    1. 
        for(String key:map.keySet()){
            System.out.print("key= "+ key + "value= " + map.get(key));
        }

    2. 
        Iterator<Map.Entry<String,String>> it=map.entrySet().iterator();
        while (if.hasNext()){
            Map.Entry<String,String> entry=it.next();
            System.out.print("key= "+ entry.getKey() + "value= " + entry.getValue());
        }

    3. 
        for(Map.Entry<String,String> entry:map.entrySet()){
            System.out.print("key= "+ entry.getKey() + "value= " + entry.getValue());
        }

    4.  
        for(String v:map.values()){
            System.out.print("value= " + v);
        }    