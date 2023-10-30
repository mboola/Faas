public interface Observer {

    public <T, R> void update(Metric<T, R> metric);

}