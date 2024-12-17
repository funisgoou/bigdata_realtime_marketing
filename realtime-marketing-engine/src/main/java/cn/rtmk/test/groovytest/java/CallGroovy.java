package cn.rtmk.test.groovytest.java;

import cn.rtmk.test.groovytest.groovy.Caculator;

public class CallGroovy {
    public static void main(String[] args) {
        Caculator caculator = new Caculator();
        int add = caculator.add(1, 2);
        System.out.println("调用groovy代码的结果："+add);
    }
}
