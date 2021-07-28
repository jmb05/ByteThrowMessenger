package net.jmb19905.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    /**
     * Checks if the provided String is at least 8 characters long, contains at least one Upper and one Lowercase letter, at least one digit and at least one symbol
     * @param password the provided Password as String
     * @return if the password is valid
     */
    public static boolean checkPasswordRules(String password){
        if(password.length() < 8){
            return false;
        }
        Pattern pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        boolean symbolFlag = matcher.find();
        if(!symbolFlag){
            return false;
        }
        char currentChar;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        for(int i=0;i < password.length();i++) {
            currentChar = password.charAt(i);
            if( Character.isDigit(currentChar)) {
                numberFlag = true;
            }else if (Character.isUpperCase(currentChar)) {
                capitalFlag = true;
            }else if (Character.isLowerCase(currentChar)) {
                lowerCaseFlag = true;
            }
            if(numberFlag && capitalFlag && lowerCaseFlag) {
                return true;
            }
        }
        return false;
    }

}
