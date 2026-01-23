package com.demo.common.util;

public class VerificationUtil {

    //The time out of the verification code, default to 60 seconds
    public static int TIME_OUT = 1000 * 60;

    //the frequency of sending email
    //it means an email can only apply code MAX_COUNT times during DURATION hours
    public static int MAX_COUNT = 10;
    public static int DURATION = 24;

    /**
     * Generate the verification code.
     * The code has six numbers of 0~9;
     * @return The verification code.
     */
    public static String generateVerificationCode() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            sb.append((int)(Math.random() * 10));
        }
        return sb.toString();
    }


}
