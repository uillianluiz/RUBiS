package edu.rice.rubis.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This program check and get all information for the rubis.properties file
 * found in the classpath.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class RUBiSProperties
{
  private static ResourceBundle configuration = null;
  private URLGenerator          urlGen = null;

  // Information about web server
  private String webSiteName;
  private int    webSitePort;
  private String EJBServer;
  private String EJBHTMLPath;
  private String EJBScriptPath;
  private String ServletsServer;
  private String ServletsHTMLPath;
  private String ServletsScriptPath;
  private String PHPHTMLPath;
  private String PHPScriptPath;
  private String useVersion;

  // Information about Workload
  private Vector  remoteClients;
  private String  remoteCommand;
  private int     nbOfClients;
  private String  transitionTable;
  private int     nbOfColumns;
  private int     nbOfRows;
  private int     maxNbOfTransitions;
  private boolean useTPCWThinkTime;
  private int     nbOfItemsPerPage;
  private int     upTime;
  private float   upSlowdown;
  private int     sessionTime;
  private int     downTime;
  private float   downSlowdown;

  // Policy to generate database information
  private String  dbServerName;
  
  private Integer nbOfUsers;

  private int     nbOfRegions;
  private int     nbOfCategories;
  private Vector  regions;
  private Vector  categories;
  private int[]   itemsPerCategory;
  private int     totalActiveItems;

  private Integer nbOfOldItems;
  private Float   percentUniqueItems;
  private Float   percentReservePrice;
  private Float   percentBuyNow;
  private Integer maxItemQty;
  private Integer itemDescriptionLength;

  private Integer maxBidsPerItem;

  private Integer maxCommentsPerUser;
  private Integer commentMaxLength;


  // Monitoring information
  private Integer monitoringDebug;
  private String  monitoringProgram;
  private String  monitoringOptions;
  private Integer monitoringSampling;
  private String  monitoringRsh;
  private String  monitoringGnuPlot;
  
  /**
   * Creates a new <code>RUBiSProperties</code> instance.
   * If the rubis.properties file is not found in the classpath,
   * the current thread is killed.
   */
  public RUBiSProperties()
  {
    // Get and check database.properties
    System.out.println("Looking for rubis.properties in classpath ("+System.getProperty("java.class.path",".")+")<p>");
    try
    {
      configuration = ResourceBundle.getBundle("rubis");
    }
    catch (java.util.MissingResourceException e)
    {
      System.err.println("No rubis.properties file has been found in your classpath.<p>");
      Runtime.getRuntime().exit(1);
    }
  }

  
  /**
   * Creates a new <code>RUBiSProperties</code> instance.
   * If the filename.properties file is not found in the classpath,
   * the current thread is killed.
   *
   * @param filename name of the property file
   */
  public RUBiSProperties(String filename)
  {
    // Get and check database.properties
    System.out.println("Looking for "+filename+".properties in classpath ("+System.getProperty("java.class.path",".")+")<p>");
    try
    {
      configuration = ResourceBundle.getBundle(filename);
    }
    catch (java.util.MissingResourceException e)
    {
      System.err.println("No "+filename+".properties file has been found in your classpath.<p>");
      Runtime.getRuntime().exit(1);
    }
  }

  
  /**
   * Returns the value corresponding to a property in the rubis.properties file.
   *
   * @param property the property name
   * @return a <code>String</code> value
   */
  protected String getProperty(String property)
  {
    String s = configuration.getString(property);
    return s;
  }


  /**
   * Check for all needed fields in rubis.properties and inialize corresponding values.
   * This function returns the corresponding URLGenerator on success.
   *
   * @return returns null on any error or the URLGenerator corresponding to the configuration if everything was ok.
   */
  public URLGenerator checkPropertiesFileAndGetURLGenerator()
  {
    try
    {
      // # HTTP server information
      System.out.println("\n<h3>### HTTP server information ###</h3>");
      System.out.print("Server name       : ");
      webSiteName  = getProperty("httpd_hostname");
      System.out.println(webSiteName+"<br>");
      System.out.print("Server port       : ");
      Integer foo  = new Integer(getProperty("httpd_port"));
      webSitePort = foo.intValue();
      System.out.println(webSitePort+"<br><br>");

      System.out.print("EJB Server            : ");
      EJBServer  = getProperty("ejb_server");
      System.out.println(EJBServer+"<br>");
      System.out.print("EJB HTML files path   : ");
      EJBHTMLPath  = getProperty("ejb_html_path");
      System.out.println(EJBHTMLPath+"<br>");
      System.out.print("EJB Script files path : ");
      EJBScriptPath  = getProperty("ejb_script_path");
      System.out.println(EJBScriptPath+"<br><br>");

      System.out.print("Servlets server            : ");
      ServletsServer  = getProperty("servlets_server");
      System.out.println(ServletsServer+"<br>");
      System.out.print("Servlets HTML files path   : ");
      ServletsHTMLPath  = getProperty("servlets_html_path");
      System.out.println(ServletsHTMLPath+"<br>");
      System.out.print("Servlets Script files path : ");
      ServletsScriptPath  = getProperty("servlets_script_path");
      System.out.println(ServletsScriptPath+"<br><br>");

      System.out.print("PHP HTML files path   : ");
      PHPHTMLPath  = getProperty("php_html_path");
      System.out.println(PHPHTMLPath+"<br>");
      System.out.print("PHP Script files path : ");
      PHPScriptPath  = getProperty("php_script_path");
      System.out.println(PHPScriptPath+"<br><br>");
      
      // # Workload
      System.out.println("\n<h3><br>### Workload ###</h3>");
      System.out.print("Remote client nodes            : ");
      StringTokenizer nodes = new StringTokenizer(getProperty("workload_remote_client_nodes"),",");
      remoteClients = new Vector(nodes.countTokens());
      while (nodes.hasMoreTokens())
        remoteClients.add(nodes.nextToken().trim());
      nbOfClients = remoteClients.size();
      System.out.println(nbOfClients+"<br>");
      System.out.print("Remote client command          : ");
      remoteCommand  = getProperty("workload_remote_client_command");
      System.out.println(remoteCommand+"<br>");
      System.out.print("Number of clients              : ");
      foo = new Integer(getProperty("workload_number_of_clients_per_node"));
      nbOfClients = foo.intValue();
      System.out.println(nbOfClients+"<br>");

      System.out.print("Transition Table               : ");
      transitionTable = getProperty("workload_transition_table");
      System.out.println(transitionTable+"<br>");
      System.out.print("Number of columns              : ");
      foo = new Integer(getProperty("workload_number_of_columns"));
      nbOfColumns = foo.intValue();
      System.out.println(nbOfColumns+"<br>");
      System.out.print("Number of rows                 : ");
      foo = new Integer(getProperty("workload_number_of_rows"));
      nbOfRows = foo.intValue();
      System.out.println(nbOfRows+"<br>");
      System.out.print("Maximum number of transitions  : ");
      foo = new Integer(getProperty("workload_maximum_number_of_transitions"));
      maxNbOfTransitions = foo.intValue();
      System.out.println(maxNbOfTransitions+"<br>");
      System.out.print("Number of items per page       : ");
      foo = new Integer(getProperty("workload_number_of_items_per_page"));
      nbOfItemsPerPage = foo.intValue();
      System.out.println(nbOfItemsPerPage+"<br>");
      System.out.print("Think time                     : ");
      useTPCWThinkTime = getProperty("workload_use_tpcw_think_time").compareTo("yes") == 0;
      if (useTPCWThinkTime)
        System.out.println("TPCW compatible with 7s mean<br>");
      else
        System.out.println("Using Transition Matrix think time information<br>");
      System.out.print("Up ramp time in ms             : ");
      foo = new Integer(getProperty("workload_up_ramp_time_in_ms"));
      upTime = foo.intValue();
      System.out.println(upTime+"<br>");
      System.out.print("Up ramp slowdown factor        : ");
      Float floo = new Float(getProperty("workload_up_ramp_slowdown_factor"));
      upSlowdown = floo.intValue();
      System.out.println(upSlowdown+"<br>");
      System.out.print("Session run time in ms         : ");
      foo = new Integer(getProperty("workload_session_run_time_in_ms"));
      sessionTime = foo.intValue();
      System.out.println(sessionTime+"<br>");
      System.out.print("Down ramp time in ms           : ");
      foo = new Integer(getProperty("workload_down_ramp_time_in_ms"));
      downTime = foo.intValue();
      System.out.println(downTime+"<br>");
      System.out.print("Down ramp slowdown factor      : ");
      floo = new Float(getProperty("workload_down_ramp_slowdown_factor"));
      downSlowdown = floo.intValue();
      System.out.println(downSlowdown+"<br>");

      // # Database Information
      System.out.println("\n<h3><br>### Database Information ###</h3>");
      System.out.print("Database server                        : ");
      dbServerName = getProperty("database_server");
      System.out.println(dbServerName+"<br>");

      // # Users policy
      System.out.println("\n<h3><br>### Users policy ###</h3>");
      System.out.print("Number of users                        : ");
      nbOfUsers = new Integer(getProperty("database_number_of_users"));
      System.out.println(nbOfUsers+"<br>");
      
      // # Region & Category definition files
      System.out.println("\n<h3><br>### Region & Category definition files ###</h3>");
      System.out.print("Regions description file               : ");
      BufferedReader regionsReader = new BufferedReader(new FileReader(getProperty("database_regions_file")));
      System.out.println(getProperty("database_regions_file")+"<br>");
      System.out.print("&nbsp &nbsp Reading file ... ");
      regions = new Vector();
      nbOfRegions = 0;
      while (regionsReader.ready())
      {
        nbOfRegions++;
        regions.add(regionsReader.readLine());
      }
      regionsReader.close();
      System.out.println(nbOfRegions+" regions found.<br>");

      System.out.print("Categories description file            : ");
      BufferedReader categoriesReader = new BufferedReader(new FileReader(getProperty("database_categories_file")));
      System.out.println(getProperty("database_categories_file")+"<br>");
      System.out.print("&nbsp &nbsp Reading file ... ");
      categories = new Vector();
      Vector itemsPerCategoryV = new Vector();
      nbOfCategories = 0;
      totalActiveItems = 0;
      while (categoriesReader.ready())
      {
        String line = categoriesReader.readLine();
        int openParenthesis = line.lastIndexOf('(');
        int closeParenthesis = line.lastIndexOf(')');
        nbOfCategories++;
        if ((openParenthesis == -1) || (closeParenthesis == -1) || (openParenthesis > closeParenthesis))
        {
          System.err.println("Syntax error in categories file on line "+nbOfCategories+": "+line);
          return null;
        }
        Integer nbOfItems = new Integer(line.substring(openParenthesis+1, closeParenthesis));
        totalActiveItems += nbOfItems.intValue();
        categories.add(line.substring(0, openParenthesis-1));
        itemsPerCategoryV.add(nbOfItems);
      }
      categoriesReader.close();
      itemsPerCategory = new int[nbOfCategories];
      for (int i = 0 ; i < nbOfCategories ; i++)
        itemsPerCategory[i] = ((Integer)itemsPerCategoryV.elementAt(i)).intValue();
      System.out.println(nbOfCategories+" categories found.<br>");
      System.out.println("Total number of items to generate: "+totalActiveItems+"<br>");
      
      // # Items policy
      System.out.println("\n<h3><br>### Items policy ###</h3>");
      System.out.print("Number of old items                    : ");
      nbOfOldItems = new Integer(getProperty("database_number_of_old_items"));
      System.out.println(nbOfOldItems+"<br>");
      System.out.print("Percentage of unique items             : ");
      percentUniqueItems    = new Float(getProperty("database_percentage_of_unique_items"));
      System.out.println(percentUniqueItems+"%"+"<br>");
      System.out.print("Percentage of items with reserve price : ");
      percentReservePrice   = new Float(getProperty("database_percentage_of_items_with_reserve_price"));
      System.out.println(percentReservePrice+"%"+"<br>");
      System.out.print("Percentage of buy now items            : ");
      percentBuyNow         = new Float(getProperty("database_percentage_of_buy_now_items"));
      System.out.println(percentBuyNow+"%"+"<br>");
      System.out.print("Maximum quantity for multiple items    : ");
      maxItemQty            = new Integer(getProperty("database_max_quantity_for_multiple_items"));
      System.out.println(maxItemQty+"<br>");
      System.out.print("Item description maximum lenth         : ");
      itemDescriptionLength = new Integer(getProperty("database_item_description_length"));
      System.out.println(itemDescriptionLength+"<br>");

      // # Bids policy
      System.out.println("\n<h3><br>### Bids policy ###</h3>");
      System.out.print("Maximum number of bids per item        : ");
      maxBidsPerItem        = new Integer(getProperty("database_max_bids_per_item"));
      System.out.println(maxBidsPerItem+"<br>");

      // # Comments policy
      System.out.println("\n<h3><br>### Comments policy ###</h3>");
      System.out.print("Maximum number of comments per user    : ");
      maxCommentsPerUser    = new Integer(getProperty("database_max_comments_per_user"));
      System.out.println(maxCommentsPerUser+"<br>");
      System.out.print("Comment maximum length                 : ");
      commentMaxLength      = new Integer(getProperty("database_comment_max_length"));
      System.out.println(commentMaxLength+"<br>");

      // # Monitoring Information
      System.out.println("\n<h3><br>### Database Information ###</h3>");
      System.out.print("Monitoring debugging level     : ");
      monitoringDebug  = new Integer(getProperty("monitoring_debug_level"));
      System.out.println(monitoringDebug+"<br>");
      System.out.print("Monitoring program             : ");
      monitoringProgram  = getProperty("monitoring_program");
      System.out.println(monitoringProgram+"<br>");
      System.out.print("Monitoring options             : ");
      monitoringOptions  = getProperty("monitoring_options");
      System.out.println(monitoringOptions+"<br>");
      System.out.print("Monitoring sampling in seconds : ");
      monitoringSampling = new Integer(getProperty("monitoring_sampling_in_seconds"));
      System.out.println(monitoringSampling+"<br>");
      System.out.print("Monitoring rsh                 : ");
      monitoringRsh      = getProperty("monitoring_rsh");
      System.out.println(monitoringRsh+"<br>");
      System.out.print("Monitoring Gnuplot Terminal    : ");
      monitoringGnuPlot  = getProperty("monitoring_gnuplot_terminal");
      System.out.println(monitoringGnuPlot+"<br>");

      // Create a new URLGenerator according to the version the user has chosen
      System.out.println("\n");
      useVersion = getProperty("httpd_use_version");
      if (useVersion.compareTo("PHP") == 0)
        urlGen = new URLGeneratorPHP(webSiteName, webSitePort, PHPHTMLPath, PHPScriptPath);
      else if (useVersion.compareTo("EJB") == 0)
        urlGen = new URLGeneratorEJB(webSiteName, webSitePort, EJBHTMLPath, EJBScriptPath);
      else if (useVersion.compareTo("Servlets") == 0)
        urlGen = new URLGeneratorServlets(webSiteName, webSitePort, ServletsHTMLPath, ServletsScriptPath);
      else
      {
        System.err.println("Sorry but '"+useVersion+"' is not supported. Only PHP, EJB and Servlets are accepted.");
        return null;
      }
      System.out.println("Using "+useVersion+" version.<br>");
    }
    catch (Exception e)
    {
      System.err.println("Error while checking database.properties: "+e.getMessage());
      return null;
    }
    return urlGen;
  }


  /**
   * Get the web server name
   *
   * @return web server name
   */
  public String getWebServerName()
  {
    return webSiteName;
  }


  /**
   * Get the database server name
   *
   * @return database server name
   */
  public String getDBServerName()
  {
    return dbServerName;
  }


  /**
   * Get the EJB server name
   *
   * @return EJB server name
   */
  public String getEJBServerName()
  {
    return EJBServer;
  }


  /**
   * Get the Servlets server name
   *
   * @return Servlets server name
   */
  public String getServletsServerName()
  {
    return ServletsServer;
  }


  /**
   * Get the total number of users given in the number_of_users field
   *
   * @return total number of users
   */
  public int getNbOfUsers()
  {
    return nbOfUsers.intValue();
  }


  /**
   * Get the total number of regions found in the region file given in the regions_file field
   *
   * @return total number of regions
   */
  public int getNbOfRegions()
  {
    return nbOfRegions;
  }


  /**
   * Get the total number of categories found in the categories file given in the categories_file field
   *
   * @return total number of categories
   */
  public int getNbOfCategories()
  {
    return nbOfCategories;
  }

  /**
   * Get a vector of region names as found in the region file given in the regions_file field
   *
   * @return vector of region names
   */
  public Vector getRegions()
  {
    return regions;
  }


  /**
   * Get a vector of category names as found in the categories file given in the categories_file field
   *
   * @return vector of category names
   */
  public Vector getCategories()
  {
    return categories;
  }


  /**
   * Return an array of number of items per category as described in the categories file given in the categories_file field
   *
   * @return array of number of items per category
   */
  public int[] getItemsPerCategory()
  {
    return itemsPerCategory;
  }


  /**
   * Get the total number of items computed from information found in the categories file given in the categories_file field
   *
   * @return total number of active items (auction date is not passed)
   */
  public int getTotalActiveItems()
  {
    return totalActiveItems;
  }


  /**
   * Get the total number of old items (auction date is over) to be inserted in the database.
   *
   * @return total number of old items (auction date is over)
   */
  public int getNbOfOldItems()
  {
    return nbOfOldItems.intValue();
  }


  /**
   * Get the percentage of unique items given in the percentage_of_unique_items field
   *
   * @return percentage of unique items
   */
  public float getPercentUniqueItems()
  {
    return percentUniqueItems.floatValue();
  }


  /**
   * Get the percentage of items with a reserve price given in the percentage_of_items_with_reserve_price field
   *
   * @return percentage of items with a reserve price
   */
  public float getPercentReservePrice()
  {
    return percentReservePrice.floatValue();
  }


  /**
   * Get the percentage of items that users can 'buy now' given in the percentage_of_buy_now_items field
   *
   * @return percentage of items that users can 'buy now' 
   */
  public float getPercentBuyNow()
  {
    return percentBuyNow.floatValue();
  }


  /**
   * Get the maximum quantity for multiple items given in the max_quantity_for_multiple_items field
   *
   * @return maximum quantity for multiple items
   */
  public int getMaxItemQty()
  {
    return maxItemQty.intValue();
  }

  /**
   * Get the maximum item description length given in the item_description_length field
   *
   * @return maximum item description length
   */
  public int getItemDescriptionLength()
  {
    return itemDescriptionLength.intValue();
  }

  /**
   * Get the maximum number of bids per item given in the max_bids_per_item field
   *
   * @return maximum number of bids per item
   */
  public int getMaxBidsPerItem()
  {
    return maxBidsPerItem.intValue();
  }

  /**
   * @deprecated Comments are now generated per item and no more per user, so this
   * function should not be used anymore.
   *
   * Get the maximum number of comments per user given in the max_comments_per_user field
   *
   * @return maximum number of comments per user
   */
  public int getMaxCommentsPerUser()
  {
    return maxCommentsPerUser.intValue();
  }

  /**
   * Get the maximum comment length given in the comment_max_length field
   *
   * @return maximum comment length
   */
  public int getCommentMaxLength()
  {
    return commentMaxLength.intValue();
  }


  /**
   * Get the transition table file name given in the transition_table field
   *
   * @return transition table file name
   */
  public String getTransitionTable()
  {
    return transitionTable;
  }


  /**
   * Get the number of columns in the transition table
   *
   * @return number of columns
   */
  public int getNbOfColumns()
  {
    return nbOfColumns;
  }


  /**
   * Get the number of rows in the transition table
   *
   * @return number of rows
   */
  public int getNbOfRows()
  {
    return nbOfRows;
  }


  /**
   * Returns true if TPC-W compatible think time must be used,
   * false if transition matrix think time has to be used.
   *
   * @return if think time should be TPC-W compatible
   */
  public boolean useTPCWThinkTime()
  {
    return useTPCWThinkTime;
  }


  /**
   * Get the number of items to display per page (when browsing) given in the number_of_items_per_page field
   *
   * @return number of items to display per page
   */
  public int getNbOfItemsPerPage()
  {
    return nbOfItemsPerPage;
  }


  /**
   * Get the total number of clients user sessions to launch in parallel
   *
   * @return total number of clients
   */
  public int getNbOfClients()
  {
    return nbOfClients;
  }


  /**
   * Get a vector of remote node names to launch clients on
   *
   * @return vector of remote node names to launch clients on
   */
  public Vector getRemoteClients()
  {
    return remoteClients;
  }


  /**
   * Get a vector of remote node names to launch clients on
   *
   * @return vector of remote node names to launch clients on
   */
  public String getClientsRemoteCommand()
  {
    return remoteCommand;
  }


  /**
   * Get the maximum number of transitions a client may perform
   *
   * @return maximum number of transitions
   */
  public int getMaxNbOfTransitions()
  {
    return maxNbOfTransitions;
  }


  /**
   * Get up ramp time in milliseconds
   *
   * @return up ramp time
   */
  public int getUpRampTime()
  {
    return upTime;
  }


  /**
   * Get up ramp slowdown factor
   *
   * @return up ramp slowdown
   */
  public float getUpRampSlowdown()
  {
    return upSlowdown;
  }


  /**
   * Get session time in milliseconds
   *
   * @return session time
   */
  public int getSessionTime()
  {
    return sessionTime;
  }


  /**
   * Get down ramp time in milliseconds
   *
   * @return down ramp time
   */
  public int getDownRampTime()
  {
    return downTime;
  }


  /**
   * Get down ramp slowdown factor
   *
   * @return down ramp slowdown
   */
  public float getDownRampSlowdown()
  {
    return downSlowdown;
  }


  /**
   * Get the monitoring debug level. Level is defined as follow: <pre>
   * 0 = no debug message
   * 1 = just error messages
   * 2 = error messages+HTML pages
   * 3 = everything!
   * </pre>
   *
   * @return monitoring program full path and name
   */
  public int getMonitoringDebug()
  {
    return monitoringDebug.intValue();
  }


  /**
   * Get the monitoring program full path and name
   *
   * @return monitoring program full path and name
   */
  public String getMonitoringProgram()
  {
    return monitoringProgram;
  }


  /**
   * Get the monitoring program options
   *
   * @return monitoring program options
   */
  public String getMonitoringOptions()
  {
    return monitoringOptions;
  }


  /**
   * Get the interval of time in seconds between 2 sample collection by the monitoring program.
   *
   * @return monitoring program sampling time in seconds
   */
  public Integer getMonitoringSampling()
  {
    return monitoringSampling;
  }


  /**
   * Get the rsh program path that should be used to run the monitoring program remotely
   *
   * @return rsh program path
   */
  public String getMonitoringRsh()
  {
    return monitoringRsh;
  }


  /**
   * Get the terminal to use for gnuplot. Usually it is set to 'gif' or 'jpeg'.
   *
   * @return gnuplot terminal
   */
  public String getGnuPlotTerminal()
  {
    return monitoringGnuPlot;
  }

}
