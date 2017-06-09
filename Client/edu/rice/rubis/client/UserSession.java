package edu.rice.rubis.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.Vector;

/**
 * RUBiS user session emulator. 
 * This class plays a random user session emulating a Web browser.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class UserSession extends Thread
{
  private RUBiSProperties rubis = null;         // access to rubis.properties file
  private URLGenerator    urlGen = null;        // URL generator corresponding to the version to be used (PHP, EJB or Servlets)
  private TransitionTable transition = null;    // transition table user for this session
  private String          lastHTMLReply = null; // last HTML reply received from 
  private Random          rand = new Random();  // random number generator
  private int             userId;               // User id for the current session
  private String          username = null;      // User name for the current session
  private String          password = null;      // User password for the current session
  private URL             lastURL = null;       // Last accessed URL
  private int             lastItemId = -1;      // This is to deal with back because the itemId cannot be retrieved from the current page
  private int             lastUserId = -1;      // This is to deal with back because the itemId cannot be retrieved from the current page
  private Stats           stats;                // Statistics to collect errors, time, ...
  private int             debugLevel = 0;       // 0 = no debug message, 1 = just error messages, 2 = error messages+HTML pages, 3 = everything!
 
  /**
   * Creates a new <code>UserSession</code> instance.
   * @param threadId a thread identifier
   * @param URLGen the URLGenerator to use
   * @param RUBiS rubis.properties
   * @param statistics where to collect statistics
   */
  public UserSession(String threadId, URLGenerator URLGen, RUBiSProperties RUBiS, Stats statistics)
  {
    super(threadId);
    urlGen = URLGen;
    rubis  = RUBiS;
    stats  = statistics;
    debugLevel = rubis.getMonitoringDebug(); // debugging level: 0 = no debug message, 1 = just error messages, 2 = error messages+HTML pages, 3 = everything!

    transition = new TransitionTable(rubis.getNbOfColumns(), rubis.getNbOfRows(), statistics, rubis.useTPCWThinkTime());
    if (!transition.ReadExcelTextFile(rubis.getTransitionTable()))
      Runtime.getRuntime().exit(1);
  }


  /**
   * Call the HTTP Server according to the given URL and get the reply
   *
   * @param url URL to access
   * @return <code>String</code> containing the web server reply (HTML file)
   */
  private String callHTTPServer(URL url)
  {
    String              HTMLReply = "";
    BufferedInputStream in = null;
    int                 retry = 0;
        
    while (retry < 5)
    {
      // Open the connexion
      try
      {
        in = new BufferedInputStream(url.openStream(), 4096);
        //System.out.println("Thread "+this.getName()+": "+url);
      }
      catch (IOException ioe) 
      {
        if (debugLevel>0)
          System.err.println("Thread "+this.getName()+": Unable to open URL "+url+" ("+ioe.getMessage()+")<br>");
        retry++;
        try
        {
          Thread.currentThread().sleep(1000L);
        }
        catch (InterruptedException i) 
        {
          if (debugLevel>0)
            System.err.println("Thread "+this.getName()+": Interrupted in callHTTPServer()<br>");
          return null;
        }
        continue;
      }

      // Get the data
      try 
      {
        byte[] buffer = new byte[4096];
        int    read;

        while ((read = in.read(buffer, 0, buffer.length)) != -1)
        {
          if (read > 0) 
            HTMLReply = HTMLReply + new String(buffer, 0, read);
        }
      }
      catch (IOException ioe) 
      {
        if (debugLevel>0)
          System.err.println("Thread "+this.getName()+": Unable to read from URL "+url+" ("+ioe.getMessage()+")<br>");
        return null;
      }

      // No retry at this point
      break;
    }        
    try
    {
      if (in != null)
        in.close();
    } 
    catch (IOException ioe) 
    {
      if (debugLevel>0)
        System.err.println("Thread "+this.getName()+": Unable to close URL "+url+" ("+ioe.getMessage()+")<br>");
    }
    if (retry == 5)
      return null;

    // Look for any image to download
    Vector images = new Vector();
    int index = HTMLReply.indexOf("<IMG SRC=\"");
    while (index != -1)
    {
      int startQuote = index + 10; // 10 equals to length of <IMG SRC"
      int endQuote = HTMLReply.indexOf("\"", startQuote+1);
      images.add(HTMLReply.substring(startQuote, endQuote));
      index = HTMLReply.indexOf("<IMG SRC=\"", endQuote);
    }
    
    // Download all images
    byte[] buffer = new byte[4096];
    while (images.size() > 0)
    {
      URL imageURL = urlGen.genericHTMLFile((String)images.elementAt(0));
      try
      {
        BufferedInputStream inImage = new BufferedInputStream(imageURL.openStream(), 4096);
        while (inImage.read(buffer, 0, buffer.length) != -1); // Just download, skip data
        inImage.close();
      }          
      catch (IOException ioe) 
      {
        if (debugLevel>0)
          System.err.println("Thread "+this.getName()+": Error while downloading image "+imageURL+" ("+ioe.getMessage()+")<br>");
      }
      images.removeElementAt(0);
    }

    return HTMLReply;
  }


  /**
   * Internal method that returns the min between last_index 
   * and x if x is not equal to -1.
   *
   * @param last_index last_index value
   * @param x value to compare with last_index
   * @return x if (x<last_index and x!=-1) else last_index
   */
  private int isMin(int last_index, int x)
  {
    if (x == -1)
      return last_index;
    if (last_index<=x)
      return last_index;
    else
      return x;
  }

  /**
   * Extract an itemId from the last HTML reply. If several itemId entries
   * are found, one of them is picked up randomly.
   *
   * @return an item identifier or -1 on error
   */
  private int extractItemIdFromHTML()
  {
    if (lastHTMLReply == null)
    {
      if (debugLevel>0)
        System.err.println("Thread "+this.getName()+": There is no previous HTML reply<br>");
      return -1;
    }

    // Count number of itemId
    int count = 0;
    int keyIndex = lastHTMLReply.indexOf("itemId=");
    while (keyIndex != -1)
    {
      count++;
      keyIndex = lastHTMLReply.indexOf("itemId=", keyIndex+7); // 7 equals to itemId=
    }
    if (count == 0)
    {
      if (lastItemId >= 0)
        return lastItemId;
      if (debugLevel>0)
        System.err.println("Thread "+this.getName()+": Cannot found item id in last HTML reply<br>");
      if (debugLevel>1)
        System.err.println("Thread "+this.getName()+": Last HTML reply is: "+lastHTMLReply+"<br>");
      return -1;
    }

    // Choose randomly an item
    count = rand.nextInt(count)+1;
    keyIndex = -7;
    while (count > 0)
    {
      keyIndex = lastHTMLReply.indexOf("itemId=", keyIndex+7); // 7 equals to itemId=
      count--;
    }
    int lastIndex = isMin(Integer.MAX_VALUE, lastHTMLReply.indexOf('\"', keyIndex+7));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('?', keyIndex+7));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('&', keyIndex+7));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('>', keyIndex+7));
    Integer foo = new Integer(lastHTMLReply.substring(keyIndex+7, lastIndex));
    lastItemId = foo.intValue();
    return lastItemId;
  }


  /**
   * Extract a page value from the last HTML reply (used from BrowseCategories like functions)
   *
   * @return a page value
   */
  private int extractPageFromHTML()
  {
    if (lastHTMLReply == null)
      return 0;

    int firstPageIndex = lastHTMLReply.indexOf("&page=");
    if (firstPageIndex == -1)
      return 0;
    int secondPageIndex = lastHTMLReply.indexOf("&page=", firstPageIndex+6); // 6 equals to &page=
    int chosenIndex = 0;
    if (secondPageIndex == -1)
      chosenIndex = firstPageIndex; // First or last page => go to next or previous page
    else
    {  // Choose randomly a page (previous or next)
      if (rand.nextInt(100000) < 50000)
        chosenIndex = firstPageIndex;
      else
        chosenIndex = secondPageIndex;
    }
    int lastIndex = isMin(Integer.MAX_VALUE, lastHTMLReply.indexOf('\"', chosenIndex+6));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('?', chosenIndex+6));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('&', chosenIndex+6));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('>', chosenIndex+6));
    Integer foo = new Integer(lastHTMLReply.substring(chosenIndex+6, lastIndex));
    return foo.intValue();
  }


  /**
   * Extract an int value corresponding to the given key
   * from the last HTML reply. Example : 
   * <pre>int userId = extractIdFromHTML("&userId=")</pre>
   *
   * @param key the pattern to look for
   * @return the <code>int</code> value or -1 on error.
   */
  private int extractIntFromHTML(String key)
  {
    if (lastHTMLReply == null)
    {
      if (debugLevel>0)
        System.err.println("Thread "+this.getName()+": There is no previous HTML reply");
      return -1;
    }

    // Look for the key
    int keyIndex = lastHTMLReply.indexOf(key);
    if (keyIndex == -1)
    {
      // Dirty hack here, ugly but convenient
      if ((key.compareTo("userId=") == 0) && (lastUserId >= 0))
        return lastUserId;
      if (debugLevel > 0)
        System.err.println("Thread "+this.getName()+": Cannot found "+key+" in last HTML reply<br>");
      if (debugLevel > 1)
        System.err.println("Thread "+this.getName()+": Last HTML reply is: "+lastHTMLReply+"<br>");
      return -1;
    }
    int lastIndex = isMin(Integer.MAX_VALUE, lastHTMLReply.indexOf('\"', keyIndex+key.length()));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('?', keyIndex+key.length()));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('&', keyIndex+key.length()));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('>', keyIndex+key.length()));
    Integer foo = new Integer(lastHTMLReply.substring(keyIndex+key.length(), lastIndex));
    // Dirty hack again here, ugly but convenient
    if (key.compareTo("userId=") == 0)
      lastUserId = foo.intValue();
    return foo.intValue();
  }


  /**
   * Extract a float value corresponding to the given key
   * from the last HTML reply. Example : 
   * <pre>float minBid = extractFloatFromHTML("name=minBid value=")</pre>
   *
   * @param key the pattern to look for
   * @return the <code>float</code> value or -1 on error.
   */
  private float extractFloatFromHTML(String key)
  {
    if (lastHTMLReply == null)
    {
      if (debugLevel > 0)
        System.err.println("Thread "+this.getName()+": There is no previous HTML reply");
      return -1;
    }

    // Look for the key
    int keyIndex = lastHTMLReply.indexOf(key);
    if (keyIndex == -1)
    {
      if (debugLevel > 0)
        System.err.println("Thread "+this.getName()+": Cannot found "+key+" in last HTML reply<br>");
      if (debugLevel > 1)
        System.err.println("Thread "+this.getName()+": Last HTML reply is: "+lastHTMLReply+"<br>");
      return -1;
    }
    int lastIndex = isMin(Integer.MAX_VALUE, lastHTMLReply.indexOf('\"', keyIndex+key.length()));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('?', keyIndex+key.length()));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('&', keyIndex+key.length()));
    lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('>', keyIndex+key.length()));
    Float foo = new Float(lastHTMLReply.substring(keyIndex+key.length(), lastIndex));
    return foo.floatValue();
  }


  /**
   * Computes the URL to be accessed according to the given state.
   * If any parameter are needed, they are computed from last HTML reply.
   *
   * @param state current state
   * @return URL corresponding to the state
   */
  public URL computeURLFromState(int state)
  {
    if (lastHTMLReply != null)
    {
      if (lastHTMLReply.indexOf("Sorry") != -1) // Nothing matched the request, we have to go back
        state = transition.backToPreviousState();
    }
    switch (state)
    {
    case -1: // An error occured, reset to home page
      transition.resetToInitialState();
    case 0: // Home Page
      return urlGen.homePage();
    case 1: // Register User Page
      return urlGen.register();
    case 2: // Register the user in the database
      { // Choose a random nb over already known attributed ids
        int i = rubis.getNbOfUsers()+rand.nextInt(1000000)+1; 
        String firstname = "Great"+i;
        String lastname = "User"+i;
        String nickname = "user"+i;
        String email = firstname+"."+lastname+"@rubis.com";
        String password = "password"+i;
        String regionName = (String)rubis.getRegions().elementAt(i % rubis.getNbOfRegions());
        
        return urlGen.registerUser(firstname, lastname, nickname, email, password, regionName);
      }
    case 3: // Browse Page
      return urlGen.browse();
    case 4: // Browse Categories
      return urlGen.browseCategories();
    case 5: // Browse items in a category
      { // We randomly pickup a category from the generated data instead of from the HTML page (faster)
        int    categoryId = rand.nextInt(rubis.getNbOfCategories());
        String categoryName = (String)rubis.getCategories().elementAt(categoryId);
        return urlGen.browseItemsInCategory(categoryId, categoryName, extractPageFromHTML(), rubis.getNbOfItemsPerPage());
      }
    case 6: // Browse Regions
      return urlGen.browseRegions();
    case 7: // Browse categories in a region
      String regionName = (String)rubis.getRegions().elementAt(rand.nextInt(rubis.getNbOfRegions()));
      return urlGen.browseCategoriesInRegion(regionName);
    case 8: // Browse items in a region for a given category
      { // We randomly pickup a category and a region from the generated data instead of from the HTML page (faster)
        int    categoryId = rand.nextInt(rubis.getNbOfCategories());
        String categoryName = (String)rubis.getCategories().elementAt(categoryId);
        int    regionId = rand.nextInt(rubis.getNbOfRegions());
        return urlGen.browseItemsInRegion(categoryId, categoryName, regionId, extractPageFromHTML(), rubis.getNbOfItemsPerPage());
      }
    case 9: // View an item
      {
        int itemId = extractItemIdFromHTML();
        if (itemId == -1)
          computeURLFromState(transition.backToPreviousState()); // Nothing then go back
        else
          return urlGen.viewItem(itemId);
      }
    case 10: // View user information
      {
        int userId = extractIntFromHTML("userId=");
        if (userId == -1)
          computeURLFromState(transition.backToPreviousState()); // Nothing then go back
        else
          return urlGen.viewUserInformation(userId);
      }
    case 11: // View item bid history
      return urlGen.viewBidHistory(extractItemIdFromHTML());
    case 12: // Buy Now Authentication
      return urlGen.buyNowAuth(extractItemIdFromHTML());
    case 13: // Buy Now confirmation page
      return urlGen.buyNow(extractItemIdFromHTML(), username, password);
    case 14: // Store Buy Now in the database
      {
        int maxQty = extractIntFromHTML("name=maxQty value=");
        if (maxQty < 1)
          maxQty = 1;
        int qty = rand.nextInt(maxQty)+1;
        return urlGen.storeBuyNow(extractItemIdFromHTML(), userId, qty, maxQty);
      }
    case 15: // Bid Authentication
      return urlGen.putBidAuth(extractItemIdFromHTML());
    case 16: // Bid confirmation page
      {
        int itemId = extractItemIdFromHTML();
        if (itemId == -1)
          computeURLFromState(transition.backToPreviousState()); // Nothing then go back
        else
          return urlGen.putBid(itemId, username, password);
      }
    case 17: // Store Bid in the database
      { /* Generate randomly the bid, maxBid and quantity values,
           all other values are retrieved from the last HTML reply */
        int maxQty = extractIntFromHTML("name=maxQty value=");
        if (maxQty < 1)
          maxQty = 1;
        int qty = rand.nextInt(maxQty)+1;
        float minBid = extractFloatFromHTML("name=minBid value=");
        float addBid = rand.nextInt(10)+1;
        float bid = minBid+addBid;
        float maxBid = minBid+addBid*2;
        return urlGen.storeBid(extractItemIdFromHTML(), userId, minBid, bid, maxBid, qty, maxQty);
      }
    case 18: // Comment Authentication page
      return urlGen.putCommentAuth(extractItemIdFromHTML(), extractIntFromHTML("to="));
    case 19: // Comment confirmation page
      return urlGen.putComment(extractItemIdFromHTML(), extractIntFromHTML("to="), username, password);
    case 20: // Store Comment in the database
      { // Generate a random comment and rating
        String[] staticComment = { "This is a very bad comment. Stay away from this seller !!<br>",
                                   "This is a comment below average. I don't recommend this user !!<br>",
                                   "This is a neutral comment. It is neither a good or a bad seller !!<br>",
                                   "This is a comment above average. You can trust this seller even if it is not the best deal !!<br>",
                                   "This is an excellent comment. You can make really great deals with this seller !!<br>" };
        int[]    staticCommentLength = { staticComment[0].length(), staticComment[1].length(), staticComment[2].length(),
                                         staticComment[3].length(), staticComment[4].length()};
        int[]    ratingValue = { -5, -3, 0, 3, 5 };
        int      rating;
        String   comment;

        rating = rand.nextInt(5);
        int commentLength = rand.nextInt(rubis.getCommentMaxLength())+1;
        comment = "";
        while (staticCommentLength[rating] < commentLength)
        {
          comment = comment+staticComment[rating];
          commentLength -= staticCommentLength[rating];
        }
        comment = staticComment[rating].substring(0, commentLength);

        return urlGen.storeComment(extractItemIdFromHTML(), extractIntFromHTML("name=to value="), userId, ratingValue[rating], comment);
      }
    case 21: // Sell page
      return urlGen.sell();
    case 22: // Select a category to sell item
      return urlGen.selectCategoryToSellItem(username, password);
    case 23:
      {
        int categoryId = rand.nextInt(rubis.getNbOfCategories());
        return urlGen.sellItemForm(categoryId, userId);
      }
    case 24: // Store item in the database
      {
        String name;
        String description;
        float  initialPrice; 
        float  reservePrice;
        float  buyNow;
        int    duration;
        int    quantity;
        int    categoryId;
        String staticDescription = "This incredible item is exactly what you need !<br>It has a lot of very nice features including "+
          "a coffee option.<br>It comes with a free license for the free RUBiS software, that's really cool. But RUBiS even if it "+
          "is free, is <B>(C) Rice University/INRIA 2001</B>. It is really hard to write an interesting generic description for "+
          "automatically generated items, but who will really read this ?<br>You can also check some cool software available on "+
          "http://sci-serv.inrialpes.fr. There is a very cool DSM system called SciFS for SCI clusters, but you will need some "+
          "SCI adapters to be able to run it ! Else you can still try CART, the amazing 'Cluster Administration and Reservation "+
          "Tool'. All those software are open source, so don't hesitate ! If you have a SCI Cluster you can also try the Whoops! "+
          "clustered web server. Actually Whoops! stands for something ! Yes, it is a Web cache with tcp Handoff, On the fly "+
          "cOmpression, parallel Pull-based lru for Sci clusters !! Ok, that was a lot of fun but now it is starting to be quite late "+
          "and I'll have to go to bed very soon, so I think if you need more information, just go on <h1>http://sci-serv.inrialpes.fr</h1> "+
          "or you can even try http://www.cs.rice.edu and try to find where Emmanuel Cecchet or Julie Marguerite are and you will "+
          "maybe get fresh news about all that !!<br>";
        int    staticDescriptionLength = staticDescription.length();
        int    totalItems = rubis.getTotalActiveItems()+rubis.getNbOfOldItems();
        int    i = totalItems+rand.nextInt(1000000)+1; 

        name = "RUBiS automatically generated item #"+i;
        int descriptionLength = rand.nextInt(rubis.getItemDescriptionLength())+1;
        description = "";
        while (staticDescriptionLength < descriptionLength)
        {
          description = description+staticDescription;
          descriptionLength -= staticDescriptionLength;
        }
        description = staticDescription.substring(0, descriptionLength);
        initialPrice = rand.nextInt(5000)+1;
        if (rand.nextInt(totalItems) < rubis.getPercentReservePrice()*totalItems/100)
          reservePrice = rand.nextInt(1000)+initialPrice;
        else
          reservePrice = 0;
        if (rand.nextInt(totalItems) < rubis.getPercentBuyNow()*totalItems/100)
          buyNow = rand.nextInt(1000)+initialPrice+reservePrice;
        else
          buyNow = 0;
        duration = rand.nextInt(7)+1;
        if (rand.nextInt(totalItems) < rubis.getPercentUniqueItems()*totalItems/100)
          quantity = 1;
        else
          quantity = rand.nextInt(rubis.getMaxItemQty())+1;
        categoryId =  rand.nextInt(rubis.getNbOfCategories());
        return urlGen.registerItem(name, description, initialPrice, reservePrice, buyNow, duration, quantity, userId, categoryId);
      }
    case 25: // About Me authentification
      return urlGen.aboutMe();
    case 26: // About Me information page
      return urlGen.aboutMe(username, password);
    default:
      if (debugLevel > 0)
        System.err.println("Thread "+this.getName()+": This state is not supported ("+state+")<br>");
      return null;
    }
  }


  /**
   * Emulate a user session using the current transition table.
   */
  public void run()
  {
    int  nbOfTransitions=0;
    int  next=0;
    long time=0;
    long startSession=0;
    long endSession=0;

    while (!ClientEmulator.isEndOfSimulation())
    {
      // Select a random user for this session
      userId = rand.nextInt(rubis.getNbOfUsers());
      username = "user"+(userId+1);
      password = "password"+(userId+1);
      nbOfTransitions = rubis.getMaxNbOfTransitions();
      if (debugLevel > 2)
        System.out.println("Thread "+this.getName()+": Starting a new user session for "+username+" ...<br>");
      startSession = System.currentTimeMillis();
      // Start from Home Page
      transition.resetToInitialState();
      next = transition.getCurrentState();
      while (!ClientEmulator.isEndOfSimulation() && !transition.isEndOfSession() && (nbOfTransitions > 0))
      {
        // Compute next step and call HTTP server (also measure time spend in server call)
        lastURL = computeURLFromState(next);
        time = System.currentTimeMillis();
        lastHTMLReply = callHTTPServer(lastURL);
        stats.updateTime(next, System.currentTimeMillis() - time);

        // If an error occured, reset to Home page
        if (lastHTMLReply.indexOf("ERROR") != -1)
        {
          if (debugLevel > 0)
            System.out.println("Thread "+this.getName()+": Error returned from access to "+lastURL+"<br>");
          stats.incrementError(next);
          if (debugLevel > 1)
            System.out.println("Thread "+this.getName()+": HTML reply was: "+lastHTMLReply+"<br>");
          transition.resetToInitialState();
          next = transition.getCurrentState();
        }
        else
          next = transition.nextState();
        nbOfTransitions--;
      }
      if ((transition.isEndOfSession()) || (nbOfTransitions == 0))
      {
        if (debugLevel > 2)
          System.out.println("Thread "+this.getName()+": Session of "+username+" successfully ended<br>");
        endSession= System.currentTimeMillis();
        long sessionTime = endSession - startSession;
        stats.addSessionTime(sessionTime);
      }
      else
      {
        if (debugLevel > 2)
          System.out.println("Thread "+this.getName()+": Session of "+username+" aborted<br>");
      }
    }
  }

}
