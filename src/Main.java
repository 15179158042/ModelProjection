
/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:38
 */
public class Main {
    public static void main(String[] args) {
        try {
            Model model = new Model("C:\\Users\\10484\\IdeaProjects\\ModelProjection\\ESB零件库\\Rectangular-Cubic-Prism\\Rectangular-Cubic Prism\\Bearing Blocks\\advgr01.STL");
            model.getPicture(2);
            System.out.println(ViewPoint.matrixList);
        }catch (Exception e){
            System.out.println(1);
        }
    }
}
