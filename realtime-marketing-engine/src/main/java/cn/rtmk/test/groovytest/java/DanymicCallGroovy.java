package cn.rtmk.test.groovytest.java;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.ant.Groovy;

import java.sql.*;

public class DanymicCallGroovy {
    public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://doitedu:3306/rtmk", "root", "root");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT groovy_code FROM t_dynamic_code");
        resultSet.next();
        String string = resultSet.getString("groovy_code");
        //
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        Class<?> groovyClass = groovyClassLoader.parseClass(string);
        GroovyObject groovyObject = (GroovyObject)   groovyClass.newInstance();
        groovyObject.invokeMethod("sayHello","liming");
        
        connection.close();
    }
}
