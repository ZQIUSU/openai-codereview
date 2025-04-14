package site.zqiusu.sdk.test;

import org.junit.Test;

public class solution {
    public int jump(int[] nums) {
        int length = nums.length;
        int end = 0;
        int maxPosition = 0;
        int steps = 0;
         for (int i = 0; i < length - 1; i++) {
            maxPosition = Math.max(maxPosition, i + nums[i]);
            if (i == end) {
                end = maxPosition;
                steps++;
            }
        }
        return steps;
    }

    @Test
    public void test(){
        int[] arr = new int[]{3,5,1,4,1,1,1,1};
        System.out.println(jump(arr));
    }
}
