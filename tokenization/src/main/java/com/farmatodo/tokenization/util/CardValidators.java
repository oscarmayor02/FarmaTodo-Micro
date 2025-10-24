package com.farmatodo.tokenization.util;

import java.time.YearMonth;

public final class CardValidators {
    private CardValidators(){}
    public static boolean luhn(String pan){
        int sum=0; boolean alt=false;
        for(int i=pan.length()-1;i>=0;i--){
            int n=pan.charAt(i)-'0';
            if(n<0||n>9) return false;
            if(alt){ n*=2; if(n>9) n-=9; }
            sum+=n; alt=!alt;
        }
        return sum%10==0;
    }
    public static boolean notExpired(int month, int year){
        YearMonth now= YearMonth.now();
        YearMonth exp=YearMonth.of(year, month);
        return !exp.isBefore(now);
    }
    public static boolean validCvv(String brand, String cvv){
        if(cvv==null) return false;
        return switch(brand){
            case "AMEX" -> cvv.matches("\\d{4}");
            default -> cvv.matches("\\d{3}");
        };
    }
}