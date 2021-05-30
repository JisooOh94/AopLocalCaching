package view;

public enum ViewNameSpace {
	USER_INFO("userInfo")
	;

	private String viewName;

	ViewNameSpace(String viewName) {
		this.viewName = viewName;
	}

	public String getViewName() {
		return viewName;
	}
}
