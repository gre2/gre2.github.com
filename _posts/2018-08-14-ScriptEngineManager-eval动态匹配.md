---
layout: post
title: "ScriptEngineManager动态匹配"
description: ""
category: [实践]
tags: [实践]
---
{% include JB/setup %}

### ScriptEngineManager eval 动态匹配

1.背景：想根据条件，筛选出来匹配的规则，但是条件想灵活配置，用表达式的方式实现

```
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

public class TestExecuteExpression {
    public static void main(String[] args) {
        Map<String,String> map=new HashMap<>();
        map.put("code","A00001");
        map.put("name","wulei");
        map.put("age","18");
        executeCodeExpression(map,"(${code}=='A00000' || ${code}=='A00001') && ${name}=='wulei'");
    }

    private static boolean executeCodeExpression(Map<String,String> map, String expression) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        try {
            for(Map.Entry<String,String> entry:map.entrySet()){
                engine.put(entry.getKey(),entry.getValue());
            }
            engine.eval(getFunction(map, expression));
            StringBuilder execFunction = new StringBuilder();
            execFunction.append("notifyCallback(");
            for(Map.Entry<String,String> entry:map.entrySet()){
                execFunction.append(entry.getKey()).append(",");
            }
            execFunction.deleteCharAt(execFunction.length()-1);
            execFunction.append(");");
            Object result = engine.eval(execFunction.toString());
            if (null != result && "true".equalsIgnoreCase(result.toString())) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getFunction(Map<String,String> map, String expression) {
        //处理参数占位符($) 举例：${code}=='A00000' ||${code}=='Q00408'  -> code=='A00000' ||code=='M0006'
        expression = expression.replaceAll("(\\$|\\{|\\})", "");
        StringBuilder function = new StringBuilder();
        function.append("function notifyCallback(");
        for(Map.Entry<String,String> entry:map.entrySet()){
            function.append(entry.getKey()).append(",");
        }
        function.deleteCharAt(function.length()-1);
        function.append(")");
        function.append(" {");
        function.append("return (").append(expression).append(");");
        function.append(" } ");
        return function.toString();
    }
}

```