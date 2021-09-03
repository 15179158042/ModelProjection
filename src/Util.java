import java.math.BigDecimal;

/**
 * @author Liuhaifeng
 * @date 2021/8/30 - 16:46
 */
public class Util {

    public static double remainPoint4(double number){
        BigDecimal bd = new BigDecimal(number);
        BigDecimal bd2 = bd.setScale(6,BigDecimal.ROUND_HALF_UP);
        return Double.parseDouble(bd2.toString());
    }
}
