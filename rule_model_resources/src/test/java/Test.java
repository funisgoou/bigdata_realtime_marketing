
public class Test {
    public static int solution(int n, int k, int[] data) {
        // dp[i] 表示完成前 i 天的最小花费
        int[] dp = new int[n + 1];

        // 初始化 dp 数组，dp[0] 代表0天时的花费为0，其他值初始化为无穷大
        for (int i = 1; i <= n; i++) {
            dp[i] = Integer.MAX_VALUE;
        }

        // 动态规划求解
        for (int i = 1; i <= n; i++) {
            // 对于每一天 i，我们可以选择从 j 天到 i 天购买食物
            for (int j = i; j >= Math.max(1, i - k + 1); j--) {
                int foodCount = i - j + 1;  // 从第 j 天到第 i 天购买食物的份数
                dp[i] = Math.min(dp[i], dp[j - 1] + data[j - 1] * foodCount);
            }
        }

        return dp[n];
    }
    public static void main(String[] args) {
        // Add your test cases here

        // 测试样例
        System.out.println(solution(5, 2, new int[]{1, 2, 3, 3, 2}));  // 输出: 9
        System.out.println(solution(6, 3, new int[]{4, 1, 5, 2, 1, 3}));  // 输出: 9
        System.out.println(solution(4, 1, new int[]{3, 2, 4, 1}));  // 输出: 10
    }
}
