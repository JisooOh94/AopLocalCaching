package config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer {
	private static final String ROOT = "/";
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		registerApplicationContext(servletContext);
		registerDispatcherServlet(servletContext);
	}

	private void registerApplicationContext(ServletContext servletContext) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(ApplicationContextConfig.class);

		servletContext.addListener(new ContextLoaderListener(applicationContext));
	}

	private void registerDispatcherServlet(ServletContext servletContext) {
		AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
		webContext.register(ServletContextConfig.class);

		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("DefaultDispatcherServlet", new DispatcherServlet(webContext));
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping(ROOT);
	}
}
