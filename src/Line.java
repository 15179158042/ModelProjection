import java.util.Objects;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:30
 */
public class Line {
    private Point begin;
    private Point end;

    @Override
    public String toString() {
        return "Line{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
    }

    public Line(Point begin, Point end){
        this.begin = begin;
        this.end = end;
    }

    public Point getBegin() {
        return begin;
    }

    public void setBegin(Point begin) {
        this.begin = begin;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return Objects.equals(begin, line.begin) && Objects.equals(end, line.end)
                || Objects.equals(end, line.begin) && Objects.equals(begin, line.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end) + Objects.hash(end, begin);
    }
}
