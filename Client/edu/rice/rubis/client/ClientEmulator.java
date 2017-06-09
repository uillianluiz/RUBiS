package edu.rice.rubis.client;

import edu.rice.rubis.beans.TimeManagement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Runtime;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Vector;

/**
 * RUBiS client emulator. 
 * This class plays random user sessions emulating a Web browser.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class ClientEmulator
{
  private RUBiSProperties rubis = null;         // access to rubis.properties file
  private URLGenerator    urlGen = null;        // URL generator corresponding to the version to be used (PHP, EJB or Servlets)
  private static float    slowdownFactor = 0;
  private static boolean  endOfSimulation = false;

  /**
   * Creates a new <code>ClientEmulator</code> instance.
   * The program is stopped on any error reading the configuration files.
   */
  public ClientEmulator(String propertiesFileName)
  {
    // Initialization, check that all files are ok
    rubis = new RUBiSProperties(propertiesFileName);
    urlGen = rubis.checkPropertiesFileAndGetURLGenerator();
    if (urlGen == null)
      Runtime.getRuntime().exit(1);
    // Check that the transition table is ok and print it
    TransitionTable transition = new TransitionTable(rubis.getNbOfColumns(), rubis.getNbOfRows(), null, rubis.useTPCWThinkTime());
    if (!transition.ReadExcelTextFile(rubis.getTransitionTable()))
      Runtime.getRuntime().exit(1);
    else
      transition.displayMatrix();
  }


  /**
   * Updates the slowdown factor.
   *
   * @param newValue new slowdown value
   */
  private synchronized void setSlowDownFactor(float newValue)
  {
    slowdownFactor = newValue;
  }


  /**
   * Get the slowdown factor corresponding to current ramp (up, session or down).
   *
   * @return slowdown factor of current ramp
   */
  public static synchronized float getSlowDownFactor()
  {
    return slowdownFactor;
  }


  /**
   * Set the end of the current simulation
   */
  private synchronized void setEndOfSimulation()
  {
    endOfSimulation = true;
  }


  /**
   * True if end of simulation has been reached.
   * @return true if end of simulation
   */
  public static synchronized boolean isEndOfSimulation()
  {
    return endOfSimulation;
  }


  /**
   * Start the monitoring program specified in rubis.properties
   * on a remote node and redirect the output in a file local
   * to this node (we are more happy if it is on a NFS volume)
   *
   * @param node node to launch monitoring program on
   * @param outputFileName full path and name of file to redirect output into
   * @return the <code>Process</code> object created
   */
  private Process startMonitoringProgram(String node, String outputFileName)
  {
    int fullTimeInSec = (rubis.getUpRampTime()+rubis.getSessionTime()+rubis.getDownRampTime())/1000 + 5; // Give 5 seconds extra for init
    try
    {
      String[] cmd = new String[3];
      cmd[0] = rubis.getMonitoringRsh();
      cmd[1] = node.trim();
      cmd[2] = rubis.getMonitoringProgram()+" "+rubis.getMonitoringOptions()+" "+
        rubis.getMonitoringSampling()+" "+fullTimeInSec+" > "+outputFileName;
      System.out.println("&nbsp &nbsp Command is: "+cmd[0]+" "+cmd[1]+" "+cmd[2]+"<br>\n");
      return Runtime.getRuntime().exec(cmd);
    }
    catch (IOException ioe)
    {
      System.out.println("An error occured while executing monitoring program ("+ioe.getMessage()+")");
      return null;
    }
  }


  /**
   * Run the node_info.sh script on the remote node and
   * just forward what we get from standard output.
   *
   * @param node node to get information from
   */
  private void printNodeInformation(String node)
  {
    try
    {
      File dir = new File(".");
      String nodeInfoProgram = dir.getCanonicalPath()+"/bench/node_info.sh";
      
      String[] cmd = new String[3];
      cmd[0] = rubis.getMonitoringRsh();
      cmd[1] = node;
      cmd[2] = nodeInfoProgram;
      Process p = Runtime.getRuntime().exec(cmd);
      BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String msg;
      while ((msg = read.readLine()) != null)
        System.out.println(msg+"<br>");
      read.close();
    }
    catch (Exception ioe)
    {
      System.out.println("An error occured while getting node information ("+ioe.getMessage()+")");
    }
  }

  /**
   * Main program take an optional output file argument only 
   * if it is run on as a remote client.
   *
   * @param args optional output file if run as remote client
   */
  public static void main(String[] args)
  {
    GregorianCalendar startDate;
    GregorianCalendar endDate;
    GregorianCalendar upRampDate;
    GregorianCalendar runSessionDate;
    GregorianCalendar downRampDate;
    GregorianCalendar endDownRampDate;
    Process           webServerMonitor = null;
    Process           dbServerMonitor = null;
    Process           ejbServerMonitor = null;
    Process           servletsServerMonitor = null;
    Process           clientMonitor;
    Process[]         remoteClientMonitor = null;
    Process[]         remoteClient = null;
    String            reportDir = "";
    boolean           isMainClient = (args.length <= 1); // Check if we are the main client
    String            propertiesFileName;

    if (isMainClient)
    {
      // Start by creating a report directory and redirecting output to an index.html file
      System.out.println("RUBiS client emulator - (C) Rice University/INRIA 2001\n");
      reportDir = "bench/"+TimeManagement.currentDateToString()+"/";
      reportDir = reportDir.replace(' ', '@');
      try
      {
        System.out.println("Creating report directory "+reportDir);
        File dir = new File(reportDir);
        dir.mkdirs();
        if (!dir.isDirectory())
        {
          System.out.println("Unable to create "+reportDir+" using current directory instead");
          reportDir = "./";
        }
        else
          reportDir = dir.getCanonicalPath()+"/";
        System.out.println("Redirecting output to '"+reportDir+"index.html'");
        PrintStream outputStream = new PrintStream(new FileOutputStream(reportDir+"index.html"));
        System.out.println("Please wait while experiment is running ...");
        System.setOut(outputStream);
        System.setErr(outputStream);
      }
      catch (Exception e)
      {
        System.out.println("Output redirection failed, displaying results on standard output ("+e.getMessage()+")");
      }
      System.out.println("<h2>RUBiS client emulator - (C) Rice University/INRIA 2001</h2><p>\n");
      startDate = new GregorianCalendar();
      System.out.println("<h3>Test date: "+TimeManagement.dateToString(startDate)+"</h3><br>\n");
    
      System.out.println("<A HREF=\"#config\">Test configuration</A><br>");
      System.out.println("<A HREF=\"trace_client0.html\">Test trace</A><br>");
      System.out.println("<A HREF=\"perf.html\">Test performance report</A><br><p>");
      System.out.println("<p><hr><p>");

      System.out.println("<CENTER><A NAME=\"config\"></A><h2>*** Test configuration ***</h2></CENTER>");
      if (args.length == 0)
        propertiesFileName = "rubis";
      else
        propertiesFileName = args[0];
    }
    else
    {
      System.out.println("RUBiS remote client emulator - (C) Rice University/INRIA 2001\n");
      startDate = new GregorianCalendar();
      propertiesFileName = args[2];
    }

    ClientEmulator client = new ClientEmulator(propertiesFileName); // Get also rubis.properties info

    Stats          stats = new Stats(client.rubis.getNbOfRows());
    Stats          upRampStats = new Stats(client.rubis.getNbOfRows());
    Stats          runSessionStats = new Stats(client.rubis.getNbOfRows());
    Stats          downRampStats = new Stats(client.rubis.getNbOfRows());
    Stats          allStats = new Stats(client.rubis.getNbOfRows());
    UserSession[]  sessions = new UserSession[client.rubis.getNbOfClients()];
    
    System.out.println("<p><hr><p>");

    if (isMainClient)
    {
      // Start remote clients
      System.out.println("Total number of clients for this experiment: "+(client.rubis.getNbOfClients()*(client.rubis.getRemoteClients().size()+1))+"<br>");
      remoteClient = new Process[client.rubis.getRemoteClients().size()];
      for (int i = 0 ; i < client.rubis.getRemoteClients().size() ; i++)
      {
        try
        {
          System.out.println("ClientEmulator: Starting remote client on "+client.rubis.getRemoteClients().get(i)+"<br>\n");
          String[] rcmdClient = new String[3];
          rcmdClient[0] = client.rubis.getMonitoringRsh();
          rcmdClient[1] = (String)client.rubis.getRemoteClients().get(i);
          rcmdClient[2] = client.rubis.getClientsRemoteCommand()+" "+reportDir+"trace_client"+(i+1)+".html "+reportDir+"stat_client"+(i+1)+".html"+" "+propertiesFileName;
          remoteClient[i] = Runtime.getRuntime().exec(rcmdClient);
          System.out.println("&nbsp &nbsp Command is: "+rcmdClient[0]+" "+rcmdClient[1]+" "+rcmdClient[2]+"<br>\n");
        }
        catch (IOException ioe)
        {
          System.out.println("An error occured while executing remote client ("+ioe.getMessage()+")");
        }
      }

      // Start monitoring programs
      System.out.println("<CENTER></A><A NAME=\"trace\"><h2>*** Monitoring ***</h2></CENTER>");

      // Monitor Web server
      System.out.println("ClientEmulator: Starting monitoring program on Web server "+client.rubis.getWebServerName()+"<br>\n");
      webServerMonitor = client.startMonitoringProgram(client.rubis.getWebServerName(), reportDir+"web_server");

      // Monitor Database server
      System.out.println("ClientEmulator: Starting monitoring program on Database server "+client.rubis.getDBServerName()+"<br>\n");
      dbServerMonitor = client.startMonitoringProgram(client.rubis.getDBServerName(), reportDir+"db_server");

      // Monitoring EJB server, if any
      String EJBServer = client.rubis.getEJBServerName().trim();
      if (EJBServer.length() > 0)
      {
        System.out.println("ClientEmulator: Starting monitoring program on EJB server "+client.rubis.getEJBServerName()+"<br>\n");
        ejbServerMonitor = client.startMonitoringProgram(client.rubis.getEJBServerName(), reportDir+"ejb_server");
      }

      // Monitoring Servlet server, if any
      String ServletsServer = client.rubis.getServletsServerName().trim();
      if (ServletsServer.length() > 0)
      {        
        System.out.println("ClientEmulator: Starting monitoring program on Servlets server "+client.rubis.getServletsServerName()+"<br>\n");
        servletsServerMonitor = client.startMonitoringProgram(client.rubis.getServletsServerName(), reportDir+"servlets_server");
      }

      // Monitor local client
      System.out.println("ClientEmulator: Starting monitoring program locally on client<br>\n");
      clientMonitor = client.startMonitoringProgram("localhost", reportDir+"client0");

      remoteClientMonitor = new Process[client.rubis.getRemoteClients().size()];
      // Monitor remote clients
      for (int i = 0 ; i < client.rubis.getRemoteClients().size() ; i++)
      {
        System.out.println("ClientEmulator: Starting monitoring program locally on client<br>\n");
        remoteClientMonitor[i] = client.startMonitoringProgram((String)client.rubis.getRemoteClients().get(i), reportDir+"client"+(i+1));
      }

      // Redirect output for traces
      try
      {
        PrintStream outputStream = new PrintStream(new FileOutputStream(reportDir+"trace_client0.html"));
        System.setOut(outputStream);
        System.setErr(outputStream);
      }
      catch (FileNotFoundException fnf)
      {
        System.err.println("Unable to redirect main client output, got error ("+fnf.getMessage()+")<br>");
      }
    }
    else
    { // Redirect output of remote clients
      System.out.println("Redirecting output to '"+args[0]+"'");
      try
      {
        PrintStream outputStream = new PrintStream(new FileOutputStream(args[0]));
        System.out.println("Please wait while experiment is running ...");
        System.setOut(outputStream);
        System.setErr(outputStream);
      }
      catch (Exception e)
      {
        System.out.println("Output redirection failed, displaying results on standard output ("+e.getMessage()+")");
      }
      startDate = new GregorianCalendar();
    }


    // #############################
    // ### TEST TRACE BEGIN HERE ###
    // #############################

    System.out.println("<CENTER></A><A NAME=\"trace\"><h2>*** Test trace ***</h2></CENTER><p>");
    System.out.println("<A HREF=\"trace_client0.html\">Main client traces</A><br>");
    for (int i = 0 ; i < client.rubis.getRemoteClients().size() ; i++)
      System.out.println("<A HREF=\"trace_client"+(i+1)+".html\">client1 ("+client.rubis.getRemoteClients().get(i)+") traces</A><br>");
    System.out.println("<br><p>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#up\">Up ramp trace</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#run\">Runtime session trace</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#down\">Down ramp trace</A><br><p><p>");

    // Run user sessions
    System.out.println("ClientEmulator: Starting "+client.rubis.getNbOfClients()+" session threads<br>");
    for (int i = 0 ; i < client.rubis.getNbOfClients() ; i++)
    {
      sessions[i] = new UserSession("UserSession"+i, client.urlGen, client.rubis, stats);
      sessions[i].start();
    }

    // Start up-ramp
    System.out.println("<br><A NAME=\"up\"></A>");
    System.out.println("<h3>ClientEmulator: Switching to ** UP RAMP **</h3><br><p>");
    client.setSlowDownFactor(client.rubis.getUpRampSlowdown());
    upRampDate = new GregorianCalendar();
    try
    {
      Thread.currentThread().sleep(client.rubis.getUpRampTime());
    }
    catch (java.lang.InterruptedException ie)
    {
      System.err.println("ClientEmulator has been interrupted.");
    }
    upRampStats.merge(stats);
    stats.reset(); // Note that as this is not atomic we may lose some stats here ...

    // Start runtime session
    System.out.println("<br><A NAME=\"run\"></A>");
    System.out.println("<h3>ClientEmulator: Switching to ** RUNTIME SESSION **</h3><br><p>");
    client.setSlowDownFactor(1);
    runSessionDate = new GregorianCalendar();
    try
    {
      Thread.currentThread().sleep(client.rubis.getSessionTime());
    }
    catch (java.lang.InterruptedException ie)
    {
      System.err.println("ClientEmulator has been interrupted.");
    }
    runSessionStats.merge(stats);
    stats.reset(); // Note that as this is not atomic we may lose some stats here ...

    // Start down-ramp
    System.out.println("<br><A NAME=\"down\"></A>");
    System.out.println("<h3>ClientEmulator: Switching to ** DOWN RAMP **</h3><br><p>");
    client.setSlowDownFactor(client.rubis.getDownRampSlowdown());
    downRampDate = new GregorianCalendar();
    try
    {
      Thread.currentThread().sleep(client.rubis.getDownRampTime());
    }
    catch (java.lang.InterruptedException ie)
    {
      System.err.println("ClientEmulator has been interrupted.");
    }
    downRampStats.merge(stats);
    endDownRampDate = new GregorianCalendar();

    // Wait for completion
    client.setEndOfSimulation();
    System.out.println("ClientEmulator: Shutting down threads ...<br>");
    for (int i = 0 ; i < client.rubis.getNbOfClients() ; i++)
    {
      try
      {
        sessions[i].join(2000);
      }
      catch (java.lang.InterruptedException ie)
      {
        System.err.println("ClientEmulator: Thread "+i+" has been interrupted.");
      }
    }
    System.out.println("Done\n");
    endDate = new GregorianCalendar();
    allStats.merge(stats);
    allStats.merge(runSessionStats);
    allStats.merge(upRampStats);
    System.out.println("<p><hr><p>");


    // #############################################
    // ### EXPERIMENT IS OVER, COLLECT THE STATS ###
    // #############################################

    // All clients completed, here is the performance report !
    // but first redirect the output
    try
    {
      PrintStream outputStream;
      if (isMainClient)
        outputStream = new PrintStream(new FileOutputStream(reportDir+"perf.html"));
      else
        outputStream = new PrintStream(new FileOutputStream(args[1]));
      System.setOut(outputStream);
      System.setErr(outputStream);
    }
    catch (Exception e)
    {
      System.out.println("Output redirection failed, displaying results on standard output ("+e.getMessage()+")");
    }

    System.out.println("<center><h2>*** Performance Report ***</h2></center><br>");    
    System.out.println("<A HREF=\"perf.html\">Overall performance report</A><br>");
    System.out.println("<A HREF=\"stat_client0.html\">Main client (localhost) statistics</A><br>");
    for (int i = 0 ; i < client.rubis.getRemoteClients().size() ; i++)
      System.out.println("<A HREF=\"stat_client"+(i+1)+".html\">client1 ("+client.rubis.getRemoteClients().get(i)+") statistics</A><br>");

    System.out.println("<p><br>&nbsp&nbsp&nbsp<A HREF=\"perf.html#node\">Node information</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#time\">Test timing information</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#up_stat\">Up ramp statistics</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#run_stat\">Runtime session statistics</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#down_stat\">Down ramp statistics</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#all_stat\">Overall statistics</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#cpu_graph\">CPU usage graphs</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#procs_graph\">Processes usage graphs</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#mem_graph\">Memory usage graph</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#disk_graph\">Disk usage graphs</A><br>");
    System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#net_graph\">Network usage graphs</A><br>");

    if (isMainClient)
    {
      // Get information about each node
      System.out.println("<br><A NAME=\"node\"></A><h3>Node Information</h3><br>");

      // Web server
      System.out.println("<B>Web server</B><br>");
      client.printNodeInformation(client.rubis.getWebServerName());
      
      // Database server
      System.out.println("<br><B>Database server</B><br>");
      client.printNodeInformation(client.rubis.getDBServerName());
        
      // EJB server, if any
      if (ejbServerMonitor != null)
      {
        System.out.println("<br><B>EJB server</B><br>");
        client.printNodeInformation(client.rubis.getEJBServerName());
      }

      // Servlets server, if any
      if (servletsServerMonitor != null)
      {
        System.out.println("<br><B>Servlets server</B><br>");
        client.printNodeInformation(client.rubis.getServletsServerName());
      }

      // Client
      System.out.println("<br><B>Local client</B><br>");
      client.printNodeInformation("localhost");

      // Remote Clients
      for (int i = 0 ; i < client.rubis.getRemoteClients().size() ; i++)
      {
        System.out.println("<br><B>Remote client "+i+"</B><br>");
        client.printNodeInformation((String)client.rubis.getRemoteClients().get(i));
      }

      try
      {
        PrintStream outputStream = new PrintStream(new FileOutputStream(reportDir+"stat_client0.html"));
        System.setOut(outputStream);
        System.setErr(outputStream);
        System.out.println("<center><h2>*** Performance Report ***</h2></center><br>");    
        System.out.println("<A HREF=\"perf.html\">Overall performance report</A><br>");
        System.out.println("<A HREF=\"stat_client0.html\">Main client (localhost) statistics</A><br>");
        for (int i = 0 ; i < client.rubis.getRemoteClients().size() ; i++)
          System.out.println("<A HREF=\"stat_client"+(i+1)+".html\">client1 ("+client.rubis.getRemoteClients().get(i)+") statistics</A><br>");
        System.out.println("<p><br>&nbsp&nbsp&nbsp<A HREF=\"perf.html#node\">Node information</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#time\">Test timing information</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#up_stat\">Up ramp statistics</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#run_stat\">Runtime session statistics</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#down_stat\">Down ramp statistics</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#all_stat\">Overall statistics</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#cpu_graph\">CPU usage graphs</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#procs_graph\">Processes usage graphs</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#mem_graph\">Memory usage graph</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#disk_graph\">Disk usage graphs</A><br>");
        System.out.println("&nbsp&nbsp&nbsp<A HREF=\"#net_graph\">Network usage graphs</A><br>");
      }
      catch (Exception ioe)
      {
        System.out.println("An error occured while getting node information ("+ioe.getMessage()+")");
      }
    }

    // Test timing information
    System.out.println("<br><p><A NAME=\"time\"></A><h3>Test timing information</h3><p>");
    System.out.println("<TABLE BORDER=1>");
    System.out.println("<TR><TD><B>Test start</B><TD>"+TimeManagement.dateToString(startDate));
    System.out.println("<TR><TD><B>Up ramp start</B><TD>"+TimeManagement.dateToString(upRampDate));
    System.out.println("<TR><TD><B>Runtime session start</B><TD>"+TimeManagement.dateToString(runSessionDate));
    System.out.println("<TR><TD><B>Down ramp start</B><TD>"+TimeManagement.dateToString(downRampDate));
    System.out.println("<TR><TD><B>Test end</B><TD>"+TimeManagement.dateToString(endDate));
    System.out.println("<TR><TD><B>Up ramp length</B><TD>"+TimeManagement.diffTime(upRampDate, runSessionDate)+
                       " (requested "+client.rubis.getUpRampTime()+" ms)");
    System.out.println("<TR><TD><B>Runtime session length</B><TD>"+TimeManagement.diffTime(runSessionDate, downRampDate)+
                       " (requested "+client.rubis.getSessionTime()+" ms)");
    System.out.println("<TR><TD><B>Down ramp length</B><TD>"+TimeManagement.diffTime(downRampDate, endDownRampDate)+
                       " (requested "+client.rubis.getDownRampTime()+" ms)");
    System.out.println("<TR><TD><B>Total test length</B><TD>"+TimeManagement.diffTime(startDate, endDate));
    System.out.println("</TABLE><p>");

    // Stats for each ramp
    System.out.println("<br><A NAME=\"up_stat\"></A>");
    upRampStats.display_stats("Up ramp", TimeManagement.diffTimeInMs(upRampDate, runSessionDate), false);
    System.out.println("<br><A NAME=\"run_stat\"></A>");
    runSessionStats.display_stats("Runtime session", TimeManagement.diffTimeInMs(runSessionDate, downRampDate), false);
    System.out.println("<br><A NAME=\"down_stat\"></A>");
    downRampStats.display_stats("Down ramp", TimeManagement.diffTimeInMs(downRampDate, endDownRampDate), false);
    System.out.println("<br><A NAME=\"all_stat\"></A>");
    allStats.display_stats("Overall", TimeManagement.diffTimeInMs(upRampDate, endDownRampDate), false);


    if (isMainClient)
    {
      // Wait for end of all monitors and remote clients
      try
      {
        for (int i = 0 ; i < client.rubis.getRemoteClients().size() ; i++)
        {
          remoteClientMonitor[i].waitFor();
          remoteClient[i].waitFor();
        }
        webServerMonitor.waitFor();
        dbServerMonitor.waitFor();
        if (ejbServerMonitor != null)
          ejbServerMonitor.waitFor();
        if (servletsServerMonitor != null)
          servletsServerMonitor.waitFor();
      }

      catch (Exception e)
      {
        System.out.println("An error occured while waiting for remote processes termination ("+e.getMessage()+")");
      }
      
      // Generate the graphics 
      try
      {
        String[] cmd = new String[4];
        if (ejbServerMonitor != null)
          cmd[0] = "bench/ejb_generate_graphs.sh";
        else if (servletsServerMonitor != null)
          cmd[0] = "bench/servlets_generate_graphs.sh";
        else
          cmd[0] = "bench/generate_graphs.sh";
        cmd[1] = reportDir;
        cmd[2] = client.rubis.getGnuPlotTerminal();
        cmd[3] = Integer.toString(client.rubis.getRemoteClients().size()+1);
        Process graph = Runtime.getRuntime().exec(cmd);
        graph.waitFor();
      }
      catch (Exception e)
      {
        System.out.println("An error occured while generating the graphs ("+e.getMessage()+")");
      }
    }

    System.out.println("<br><A NAME=\"cpu_graph\"></A>");
    System.out.println("<br><h3>CPU Usage graphs</h3><p>");
    System.out.println("<TABLE>");
    System.out.println("<TR><TD><IMG SRC=\"cpu_busy."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_cpu_busy."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"cpu_idle."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_cpu_idle."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"cpu_user_kernel."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_cpu_user_kernel."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");

    System.out.println("<br><A NAME=\"procs_graph\"></A>");
    System.out.println("<TABLE>");
    System.out.println("<br><h3>Processes Usage graphs</h3><p>");
    System.out.println("<TR><TD><IMG SRC=\"procs."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_procs."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"ctxtsw."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_ctxtsw."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");

    System.out.println("<br><A NAME=\"mem_graph\"></A>");
    System.out.println("<br><h3>Memory Usage graph</h3><p>");
    System.out.println("<TABLE>");
    System.out.println("<TR><TD><IMG SRC=\"mem_usage."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_mem_usage."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"mem_cache."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_mem_cache."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");

    System.out.println("<br><A NAME=\"disk_graph\"></A>");
    System.out.println("<br><h3>Disk Usage graphs</h3><p>");
    System.out.println("<TABLE>");
    System.out.println("<TR><TD><IMG SRC=\"disk_rw_req."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_disk_rw_req."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"disk_tps."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_disk_tps."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");

    System.out.println("<br><A NAME=\"net_graph\"></A>");
    System.out.println("<br><h3>Network Usage graphs</h3><p>");
    System.out.println("<TABLE>");
    System.out.println("<TR><TD><IMG SRC=\"net_rt_byt."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_net_rt_byt."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"net_rt_pack."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_net_rt_pack."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("<TR><TD><IMG SRC=\"socks."+client.rubis.getGnuPlotTerminal()+"\"><TD><IMG SRC=\"client_socks."+client.rubis.getGnuPlotTerminal()+"\">");
    System.out.println("</TABLE><p>");


    if (isMainClient)
    {
      // Compute the global stats
      try
      {
        String[] cmd = new String[6];
        cmd[0] = "bench/compute_global_stats.awk";
        cmd[1] = "-v";
        cmd[2] = "path="+reportDir;
        cmd[3] = "-v";
        cmd[4] = "nbscript="+Integer.toString(client.rubis.getRemoteClients().size()+1);
        cmd[5] = reportDir+"stat_client0.html";
        Process computeStats = Runtime.getRuntime().exec(cmd);
        computeStats.waitFor();
      }
      catch (Exception e)
      {
        System.out.println("An error occured while generating the graphs ("+e.getMessage()+")");
      }
    }

    Runtime.getRuntime().exit(0);
  }

}
