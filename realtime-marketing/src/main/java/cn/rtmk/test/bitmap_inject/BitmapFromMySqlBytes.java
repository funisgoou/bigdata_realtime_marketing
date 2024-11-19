package cn.rtmk.test.bitmap_inject;

import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.*;

public class BitmapFromMySqlBytes {
    public static void main(String[] args) throws SQLException, IOException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://doitedu:3306/rtmk", "root", "root");
        PreparedStatement preparedStatement = conn.prepareStatement("select rule_id, profile_users_bitmap\n" +
                "from rtmk_rule_def\n" +
                "where rule_id=?");
        preparedStatement.setString(1,"g01_rule01");
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        byte[] bitmapBytes = resultSet.getBytes("profile_users_bitmap");
        
        //反序列化字节数组
        RoaringBitmap roaringBitmap = RoaringBitmap.bitmapOf();
        roaringBitmap.deserialize(ByteBuffer.wrap(bitmapBytes));
        
        //测试反序列化bitmap是否正确
        int[] result = roaringBitmap.toArray();
        for (int i : result) {
            System.out.println(i);
        }

    }
}
