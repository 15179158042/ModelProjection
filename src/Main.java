import com.sun.deploy.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:38
 */
public class Main {
    public static void main(String[] args) {
        try {
            Model model = new Model(
                    "D:\\Desktop\\文件\\任务\\毕业设计\\3D\\ESB零件库\\Rectangular-Cubic-Prism\\Rectangular-Cubic Prism\\Bearing Blocks\\advgr01.STL");
//            model.getPictures("D:\\Desktop\\2");
            double[] d1 = model.getDescriptor(61);
            double[] d2 = model.getDescriptor(61);
            double ans = 0;
            for (int i = 0;i< d1.length;i++){
                System.out.println(d1[i]);
                System.out.println(d2[i]);
                ans += Util.integerPow(d1[i] - d2[i],2);
            }
            System.out.println(Math.sqrt(ans));
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

