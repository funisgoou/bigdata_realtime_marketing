package cn.rtmk.test.bitmap_inject;

import org.roaringbitmap.RoaringBitmap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RulePublish {
    public static void main(String[] args) throws IOException, SQLException {
        String ruleId="g01_rule01";
        //根据规则的条件，去es中查询人群
        int[] ruleProfileUsers={100,200,300,927,965};
        //把查询出来的人群的guid变成bitmap，发布到mysql
         //1.id转换成bitmap
        RoaringBitmap roaringBitmap = RoaringBitmap.bitmapOf(ruleProfileUsers);
         //2.把生成好的Bitmap序列化到一个字节数组中
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bout);
        roaringBitmap.serialize(dos);
        byte[] byteArray = bout.toByteArray();
        
        //3.发布到mysql
        Connection conn = DriverManager.getConnection("jdbc:mysql://doitedu:3306/rtmk", "root", "root");
        PreparedStatement preparedStatement = conn.prepareStatement("INSERT into rtmk_rule_def VALUES(?,?)");
        preparedStatement.setString(1,ruleId);
        preparedStatement.setBytes(2,byteArray);
        preparedStatement.execute();
        preparedStatement.close();
        conn.close();
    }
}
