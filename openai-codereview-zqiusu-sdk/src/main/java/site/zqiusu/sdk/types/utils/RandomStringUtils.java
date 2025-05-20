package site.zqiusu.sdk.types.utils;

import java.util.Random;

public class RandomStringUtils {
    //随机生成length长度的字符串
    private static String generateFileName(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();

    }
}
