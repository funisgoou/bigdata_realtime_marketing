package cn.rtmk.test.groovytest.java;

import groovy.lang.GroovyClassLoader;

import java.sql.*;

public class DanymicCallGroovy2 {
    public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://doitedu:3306/rtmk", "root", "root");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT groovy_code FROM t_dynamic_code");
        while(resultSet.next()){
            String string = resultSet.getString("groovy_code");
            //
            GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
            Class<?> groovyClass = groovyClassLoader.parseClass(string);
            Person person = (Person)   groovyClass.newInstance();
            String liming = person.saySomeThing("liming");
            System.out.println("在Java中打印groovy代码调用后的返回值:"+liming);
        }
        connection.close();
    }
}
