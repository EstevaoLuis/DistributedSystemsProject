package it.sdp.gestore;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class Initializer implements ServletContextListener{
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		new Thread(new GatewayMultiThread()).start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {}
}
