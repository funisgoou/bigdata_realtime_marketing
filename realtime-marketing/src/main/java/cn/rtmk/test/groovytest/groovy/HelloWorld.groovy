package cn.rtmk.test.groovytest.groovy

class HelloWorld {
    static void main(String[] args) {
        println("hello world")
        def caculator = new Caculator()
        def add = caculator.add(10, 20)
        println("工具调用的结果为:"+add)
    }
}
