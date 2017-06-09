package edu.rice.rubis.client;

/**
 * This class provides the needed URLs to access all features of RUBiS (Servlets version).
 * You must provide the name and port of the Web site running RUBiS as well
 * as the directories where the scripts and HTML files reside. For example:
 * <pre>
 * URLGenerator rubisWeb = new URLGeneratorServlets("www.testbed.cs.rice.edu", 80, "/Servlet_HTML", "/servlet");
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class URLGeneratorServlets extends URLGenerator
{

  /**
   * Set the name and port of the Web site running RUBiS as well as the
   * directories where the HTML and servlets reside. Examples:
   * <pre>
   * URLGenerator rubisWeb = new URLGenerator("www.testbed.cs.rice.edu", 80, "/Servlet_HTML", "/servlet");
   * </pre>
   *
   * @param host Web site address
   * @param port HTTP server port
   * @param HTMLFilesPath path where HTML files reside
   * @param ScriptFilesPath path to the script files
   */
  public URLGeneratorServlets(String host, int port, String HTMLFilesPath, String ScriptFilesPath)
  {
    super(host, port, HTMLFilesPath, ScriptFilesPath);
  }


  /**
   * Returns the name of the About Me servlet.
   *
   * @return About Me servlet name
   */
  public String AboutMeScript()
  {
    return "edu.rice.rubis.servlets.AboutMe";
  }
 

  /**
   * Returns the name of the Browse Categories servlet.
   *
   * @return Browse Categories servlet name
   */
  public String BrowseCategoriesScript()
  {
    return "edu.rice.rubis.servlets.BrowseCategories";
  }

  /**
   * Returns the name of the Browse Regions servlet.
   *
   * @return Browse Regions servlet name
   */
  public String BrowseRegionsScript()
  {
    return "edu.rice.rubis.servlets.BrowseRegions";
  }

  /**
   * Returns the name of the Store Buy Now servlet.
   *
   * @return Store Buy Now servlet name
   */
  public String StoreBuyNowScript()
  {
    return "edu.rice.rubis.servlets.StoreBuyNow";
  }

  /**
   * Returns the name of the Buy Now servlet.
   *
   * @return Buy Now servlet name
   */
  public String BuyNowScript()
  {
    return "edu.rice.rubis.servlets.BuyNow";
  }

  /**
   * Returns the name of the Buy Now Auth servlet.
   *
   * @return Buy Now Auth servlet name
   */
  public String BuyNowAuthScript()
  {
    return "edu.rice.rubis.servlets.BuyNowAuth";
  }

  /**
   * Returns the name of the Put Bid servlet.
   *
   * @return Put Bid servlet name
   */
  public String PutBidScript()
  {
    return "edu.rice.rubis.servlets.PutBid";
  }

  /**
   * Returns the name of the Put Bid Auth servlet.
   *
   * @return Put Bid Auth servlet name
   */
  public String PutBidAuthScript()
  {
    return "edu.rice.rubis.servlets.PutBidAuth";
  }

  /**
   * Returns the name of the Put Comment servlet.
   *
   * @return Put Comment servlet name
   */
  public String PutCommentScript()
  {
    return "edu.rice.rubis.servlets.PutComment";
  }

  /**
   * Returns the name of the Put Comment Auth servlet.
   *
   * @return Put Comment Auth servlet name
   */
  public String PutCommentAuthScript()
  {
    return "edu.rice.rubis.servlets.PutCommentAuth";
  }

  /**
   * Returns the name of the Register Item servlet.
   *
   * @return Register Item servlet name
   */
  public String RegisterItemScript()
  {
    return "edu.rice.rubis.servlets.RegisterItem";
  }

  /**
   * Returns the name of the Register User servlet.
   *
   * @return Register User servlet name
   */
  public String RegisterUserScript()
  {
    return "edu.rice.rubis.servlets.RegisterUser";
  }

  /**
   * Returns the name of the Search Items By Category servlet.
   *
   * @return Search Items By Category servlet name
   */
  public String SearchItemsByCategoryScript()
  {
    return "edu.rice.rubis.servlets.SearchItemsByCategory";
  }

  /**
   * Returns the name of the Search Items By Region servlet.
   *
   * @return Search Items By Region servlet name
   */
  public String SearchItemsByRegionScript()
  {
    return "edu.rice.rubis.servlets.SearchItemsByRegion";
  }

  /**
   * Returns the name of the Sell Item Form servlet.
   *
   * @return Sell Item Form servlet name
   */
  public String SellItemFormScript()
  {
    return "edu.rice.rubis.servlets.SellItemForm";
  }

  /**
   * Returns the name of the Store Bid servlet.
   *
   * @return Store Bid servlet name
   */
  public String StoreBidScript()
  {
    return "edu.rice.rubis.servlets.StoreBid";
  }

  /**
   * Returns the name of the Store Comment servlet.
   *
   * @return Store Comment servlet name
   */
  public String StoreCommentScript()
  {
    return "edu.rice.rubis.servlets.StoreComment";
  }

  /**
   * Returns the name of the View Bid History servlet.
   *
   * @return View Bid History servlet name
   */
  public String ViewBidHistoryScript()
  {
    return "edu.rice.rubis.servlets.ViewBidHistory";
  }

  /**
   * Returns the name of the View Item servlet.
   *
   * @return View Item servlet name
   */
  public String ViewItemScript()
  {
    return "edu.rice.rubis.servlets.ViewItem";
  }

  /**
   * Returns the name of the View User Info servlet.
   *
   * @return View User Info servlet name
   */
  public String ViewUserInfoScript()
  {
    return "edu.rice.rubis.servlets.ViewUserInfo";
  }
}
