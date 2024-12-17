package cn.rtmk.test.bitmap_inject;

import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Arrays;

public class BitmapFromMySqlBytes {
    public static void main(String[] args) throws SQLException, IOException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://doitedu:3306/rtmk", "root", "root");
        Statement statement = conn.createStatement();
        ResultSet resultSet1 = statement.executeQuery("select rule_id, profile_users_bitmap from rtmk_rule_def");
        while(resultSet1.next()){
            String ruleId=resultSet1.getString("rule_id");
            byte[] bitmapBytes = resultSet1.getBytes("profile_users_bitmap");
            //反序列化字节数组
            RoaringBitmap roaringBitmap = RoaringBitmap.bitmapOf();
            roaringBitmap.deserialize(ByteBuffer.wrap(bitmapBytes));
            //测试反序列化bitmap是否正确
            int[] result = roaringBitmap.toArray();
            String people = Arrays.toString(result);
            System.out.println(String.format("规则:%s,人群id:%s",ruleId,people));
        };
    }
}
