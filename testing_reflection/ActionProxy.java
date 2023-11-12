//THIS IS A CONCEPTUAL VERSION, NOT A FUNCTIONAL ONE

public interface ActionProxy {

	public void registerAction(String id, Object f, int ram);
	public void removeAction(String id);
	public void	listActions();

	public <T, R> R invoke(String id, T args) throws Exception;
	public void showInterfaces();
    public void showMethods();

}
