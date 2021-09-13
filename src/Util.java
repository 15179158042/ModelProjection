import java.math.BigDecimal;

/**
 * @author Liuhaifeng
 * @date 2021/8/30 - 16:46
 */
public class Util {

    //保留小数点后四位
    public static double remainPoint4(double number){
        BigDecimal bd = new BigDecimal(number);
        BigDecimal bd2 = bd.setScale(6,BigDecimal.ROUND_HALF_UP);
        return Double.parseDouble(bd2.toString());
    }

    //对于Math.pow的优化
    public static double integerPow(double number, int n){
        if (n == 0)
            return 1;
        if (n % 2 == 0)
            return integerPow(number, n/2) * integerPow(number, n/2);
        else
            return integerPow(number, n/2) * integerPow(number, n/2) * number;
    }
}
