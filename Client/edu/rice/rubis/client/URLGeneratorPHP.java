package edu.rice.rubis.client;

/**
 * This class provides the needed URLs to access all features of RUBiS (PHP version).
 * You must provide the name and port of the Web site running RUBiS as well
 * as the directories where the scripts and HTML files reside. For example:
 * <pre>
 * URLGenerator rubisWeb = new URLGeneratorEJB("www.testbed.cs.rice.edu", 80, "/PHP", "/PHP");
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class URLGeneratorPHP extends URLGenerator
{

  /**
   * Set the name and port of the Web site running RUBiS as well as the
   * directories where the HTML and PHP scripts reside. Examples:
   * <pre>
   * URLGenerator rubisWeb = new URLGenerator("www.testbed.cs.rice.edu", 80, "/PHP", "/PHP");
   * </pre>
   *
   * @param host Web site address
   * @param port HTTP server port
   * @param HTMLFilesPath path where HTML files reside
   * @param ScriptFilesPath path to the script files
   */
  public URLGeneratorPHP(String host, int port, String HTMLFilesPath, String ScriptFilesPath)
  {
    super(host, port, HTMLFilesPath, ScriptFilesPath);
  }


  /**
   * Returns the name of the About Me script.
   *
   * @return About Me script name
   */
  public String AboutMeScript()
  {
    return "AboutMe.php";
  }
 

  /**
   * Returns the name of the Browse Categories script.
   *
   * @return Browse Categories script name
   */
  public String BrowseCategoriesScript()
  {
    return "BrowseCategories.php";
  }

  /**
   * Returns the name of the Browse Regions script.
   *
   * @return Browse Regions script name
   */
  public String BrowseRegionsScript()
  {
    return "BrowseRegions.php";
  }

  /**
   * Returns the name of the Buy Now script.
   *
   * @return Buy Now script name
   */
  public String BuyNowScript()
  {
    return "BuyNow.php";
  }

  /**
   * Returns the name of the Buy Now Auth script.
   *
   * @return Buy Now Auth script name
   */
  public String BuyNowAuthScript()
  {
    return "BuyNowAuth.php";
  }

  /**
   * Returns the name of the Put Bid script.
   *
   * @return Put Bid script name
   */
  public String PutBidScript()
  {
    return "PutBid.php";
  }

  /**
   * Returns the name of the Put Bid Auth script.
   *
   * @return Put Bid Auth script name
   */
  public String PutBidAuthScript()
  {
    return "PutBidAuth.php";
  }

  /**
   * Returns the name of the Put Comment script.
   *
   * @return Put Comment script name
   */
  public String PutCommentScript()
  {
    return "PutComment.php";
  }

  /**
   * Returns the name of the Put Comment Auth script.
   *
   * @return Put Comment Auth script name
   */
  public String PutCommentAuthScript()
  {
    return "PutCommentAuth.php";
  }

  /**
   * Returns the name of the Register Item script.
   *
   * @return Register Item script name
   */
  public String RegisterItemScript()
  {
    return "RegisterItem.php";
  }

  /**
   * Returns the name of the Register User script.
   *
   * @return Register User script name
   */
  public String RegisterUserScript()
  {
    return "RegisterUser.php";
  }

  /**
   * Returns the name of the Search Items By Category script.
   *
   * @return Search Items By Category script name
   */
  public String SearchItemsByCategoryScript()
  {
    return "SearchItemsByCategory.php";
  }

  /**
   * Returns the name of the Search Items By Region script.
   *
   * @return Search Items By Region script name
   */
  public String SearchItemsByRegionScript()
  {
    return "SearchItemsByRegion.php";
  }

  /**
   * Returns the name of the Sell Item Form script.
   *
   * @return Sell Item Form script name
   */
  public String SellItemFormScript()
  {
    return "SellItemForm.php";
  }

  /**
   * Returns the name of the Store Bid script.
   *
   * @return Store Bid script name
   */
  public String StoreBidScript()
  {
    return "StoreBid.php";
  }

  /**
   * Returns the name of the Store Buy Now script.
   *
   * @return Store Buy Now script name
   */
  public String StoreBuyNowScript()
  {
    return "StoreBuyNow.php";
  }

  /**
   * Returns the name of the Store Comment script.
   *
   * @return Store Comment script name
   */
  public String StoreCommentScript()
  {
    return "StoreComment.php";
  }

  /**
   * Returns the name of the View Bid History script.
   *
   * @return View Bid History script name
   */
  public String ViewBidHistoryScript()
  {
    return "ViewBidHistory.php";
  }

  /**
   * Returns the name of the View Item script.
   *
   * @return View Item script name
   */
  public String ViewItemScript()
  {
    return "ViewItem.php";
  }

  /**
   * Returns the name of the View User Info script.
   *
   * @return View User Info script name
   */
  public String ViewUserInfoScript()
  {
    return "ViewUserInfo.php";
  }
}
