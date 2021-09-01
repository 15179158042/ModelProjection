import com.sun.deploy.util.StringUtils;

import java.util.Arrays;
import java.util.List;
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
            Model newModel = model.Split(2);
            newModel.getPicture("D:\\Desktop\\1");




        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
