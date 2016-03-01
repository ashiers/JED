package com.tacticalenterprisesltd;

import java.util.Date;
import java.util.Observable;
/**
 * <p>This class allows you to monitor SQL Insertion Attack alerts.  This class is a Singleton. It works in conjunction with the Validate class.
 * The Validate class has a handle on this object and if there are any Observers connected, each will receive an alert notification if inappropriate
 * character sequences are detected during the validation of any field on the client side.
 * To implement this class as part of your application, create a class that implements java.util.Observer.  In a servlet, initialize this class
 * by passing an instance of the Observer class.</p>
 * <code>
 * import java.util.*;<br>
 * public class MyObserver implements Observer<br>
 * {<br>
 * &nbsp;&nbsp;public void update(Observable o, Object arg)<br>
 * &nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;//Perform whatever action you want here.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println(o.toString());<br>
 * &nbsp;&nbsp;}<br>
 * }<br><br>
 * import javax.servlet.http.*;<br>
 * import com.tacticalenterprisesltd.*;<br>
 * public class SQLObserverServlet extends HttpServlet<br>
 * {<br>
 * &nbsp;&nbsp;public void init()<br>
 * &nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;try<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SQLInsertionAttackObservable obs = SQLInsertionAttackObservable.getInstance();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;obs.addObserver(new MyObserver());<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;catch(Exception e){e.printStackTrace();}<br>
 * &nbsp;&nbsp;}<br>
 * }
 *  </code>
 *  <p>In your web.xml file add the following to initialize the servlet:</p>
 *  <code>
 *  &lt;servlet&gt;<br>
 *  &nbsp;&nbsp;&lt;servlet-name&gt;SQLObserverServlet&lt;/servlet-name&gt;<br>
 *  &nbsp;&nbsp;&lt;servlet-class&gt;com.tacticalenterprisesltd.SQLObserverServlet&lt;/servlet-class&gt;<br>
 *  &nbsp;&nbsp;&lt;load-on-startup&gt;1&lt;/load-on-startup&gt;<br>
 *  &lt;/servlet&gt;<br><br>	
 *  &lt;servlet-mapping&gt;<br>
 *  &nbsp;&nbsp;&lt;servlet-name&gt;SQLObserverServlet&lt;/servlet-name&gt;<br>
 *  &nbsp;&nbsp;&lt;url-pattern&gt;/SQLObserverServlet/*&lt;/url-pattern&gt;<br>
 *  &lt;/servlet-mapping&gt;
 *  </code>
 *  <p>In the JSP or Servlet acting as a controller you can add additional information to this object with:</p>
 *  <code>
 *  //is client behind something?<br>
 *  String ipAddress = request.getHeader("X-FORWARDED-FOR");  <br>
 *  if (ipAddress == null)<br>
 *  {  <br>
 *  &nbsp;&nbsp;ipAddress = request.getRemoteAddr();<br>  
 *  }<br>
 *  String urlAddress = request.getRequestURL().toString();<br>
 *  SQLInsertionAttackObservable obs = SQLInsertionAttackObservable.getInstance();<br>
 *  obs.setSourceURLAddress(urlAddress);<br>
 *  obs.setSourceIPAddress(ipAddress);<br>
 *  </code>
 * @author Alan Shiers
 * @version 1.5.0
 *
 */
public class SQLInsertionAttackObservable extends Observable
{
	private String URL = "";
	private String IPAddress = "";	
	private static SQLInsertionAttackObservable instance;

    static {
    	instance = new SQLInsertionAttackObservable();
    }

    private SQLInsertionAttackObservable() { 
        // hidden constructor
    }    

    public static SQLInsertionAttackObservable getInstance() {
        return instance;
    }
    
    public void setSourceURLAddress(String url)
    {
    	URL = url;
    }
    
    public void setSourceIPAddress(String ip)
    {
    	IPAddress = ip;
    }
    
    public String getSourceURLAddress()
    {
    	return URL;
    }
    
    public String getSourceIPAddress()
    {
    	return IPAddress;
    }
    
    public void alert()
    {
    	// alert Observers to an SQL Insertion Attack
	    setChanged();
        // trigger notification
	    notifyObservers();            	
    }
    
    public String toString()
    {
    	Date today = new Date();
    	return today.toString() + " " + Constants.SQLINSERTIONATTACKMESSAGE + "\nSOURCE URL: " + URL + "\nSOURCE IP: " + IPAddress;
    }

}
