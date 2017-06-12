package edu.rice.rubis.client;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.Thread;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * This program initializes the RUBiS database according to the rubis.properties file
 * found in the classpath.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class InitDBSQL {

    public static Connection getConnection() throws SQLException {
        String DB_CONN_STRING = "jdbc:mysql://localhost:3306/rubis?useSSL=true&serverTimezone=UTC&rewriteBatchedStatements=true";
        String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
        String USER_NAME = "root";
        String PASSWORD = "";

        Connection result = null;
        try {
            Class.forName(DRIVER_CLASS_NAME).newInstance();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return result;
        }

        try {
            result = DriverManager.getConnection(DB_CONN_STRING, USER_NAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
        return result;
    }


    private URLGenerator urlGen = null;
    private Random rand = new Random();
    private RUBiSProperties rubis = null;
    private int[] itemsPerCategory;


    /**
     * Creates a new <code>InitDB</code> instance.
     */
    public InitDBSQL() {
        rubis = new RUBiSProperties();
        urlGen = rubis.checkPropertiesFileAndGetURLGenerator();
        if (urlGen == null)
            Runtime.getRuntime().exit(1);
        itemsPerCategory = rubis.getItemsPerCategory();
    }

    /**
     * Main program accepts any combination of the following arguments: <pre>
     * all: generate the complete database
     * users: generate only users
     * items: generate only items
     * bids: generate bids and items (it is not possible to create bids without creating the related items)
     * comments: generate comments and items (it is not possible to create comments without creating the related items)
     * <p>
     * @param args all|users|items|bids|comments
     */
    public static void main(String[] args) {
        System.out.println("RUBiS database initialization - (C) Rice University/INRIA 2001\n");

        InitDBSQL initDB = new InitDBSQL();
        int argc = Array.getLength(args);
        String params = "";

        if (argc == 0) {
            System.out.println("Command line  : java -classpath .:./database edu.rice.rubis.client.InitDBSQL parameters");
            System.out.println("Using Makefile: make initDB PARAM=\"parameters\"");
            System.out.println("where parameter is one or any combination of the following arguments:");
            System.out.println(" all: generate the complete database");
            System.out.println(" users: generate only users");
            System.out.println(" items: generate only items");
            System.out.println(" bids: generate bids and items (it is not possible to create bids without creating the related items)");
            System.out.println(" comments: generate comments and items (it is not possible to create comments without creating the related items)");
            Runtime.getRuntime().exit(1);
        }

        for (int i = 0; i < argc; i++)
            params = params + " " + args[i];

        if ((params.indexOf("users") != -1) || (params.indexOf("all") != -1))
            initDB.generateUsers();

        if ((params.indexOf("items") != -1) || (params.indexOf("bids") != -1) ||
                (params.indexOf("comments") != -1) || (params.indexOf("all") != -1))
            initDB.generateItems((params.indexOf("bids") != -1) || (params.indexOf("all") != -1), (params.indexOf("comments") != -1) || (params.indexOf("all") != -1));
    }


    /**
     * This method add users to the database according to the parameters
     * given in the database.properties file.
     */
    public void generateUsers() {
        String firstname;
        String lastname;
        String nickname;
        String email;
        String password;
        String regionName;
        String HTTPreply;
        int i;
        URL url;
        int regionNameId;

        // Cache variables
        int getNbOfUsers = rubis.getNbOfUsers();
        int getNbOfRegions = rubis.getNbOfRegions();
        Connection c;
        try {
            c = InitDBSQL.getConnection();

            c.setAutoCommit(false);
            PreparedStatement ps = c.prepareStatement("INSERT INTO users VALUES (DEFAULT, ?, ?, ?, ?, ?, 0, 0, NOW(), ?)");

            System.out.print("Generating " + getNbOfUsers + " users ");
            for (i = 0; i < getNbOfUsers; i++) {
                firstname = "Great" + (i + 1);
                lastname = "User" + (i + 1);
                nickname = "user" + (i + 1);
                email = firstname + "." + lastname + "@rubis.com";
                password = "password" + (i + 1);
                regionName = (String) rubis.getRegions().elementAt(i % getNbOfRegions);
                regionNameId = i % getNbOfRegions;

                ps.setString(1, firstname);
                ps.setString(2, lastname);
                ps.setString(3, nickname);
                ps.setString(4, password);
                ps.setString(5, email);
                ps.setInt(6, regionNameId);
                ps.addBatch();
            }
            ps.executeBatch();
            c.setAutoCommit(true);
        } catch (Exception e) {
            System.err.println("Error while generating users: " + e.getMessage());
        }
        System.out.println("Done!");
    }


    /**
     * This method add items to the database according to the parameters
     * given in the database.properties file.
     */
    public void generateItems(boolean generateBids, boolean generateComments) {
        // Items specific variables
        String name;
        String description;
        float initialPrice;
        float reservePrice;
        float buyNow;
        int duration;
        int quantity;
        int categoryId;
        int sellerId;
        int oldItems = rubis.getNbOfOldItems();
        int activeItems = rubis.getTotalActiveItems();
        int totalItems = oldItems + activeItems;
        String staticDescription = "This incredible item is exactly what you need !<br>It has a lot of very nice features including " +
                "a coffee option.<p>It comes with a free license for the free RUBiS software, that's really cool. But RUBiS even if it " +
                "is free, is <B>(C) Rice University/INRIA 2001</B>. It is really hard to write an interesting generic description for " +
                "automatically generated items, but who will really read this ?<p>You can also check some cool software available on " +
                "http://sci-serv.inrialpes.fr. There is a very cool DSM system called SciFS for SCI clusters, but you will need some " +
                "SCI adapters to be able to run it ! Else you can still try CART, the amazing 'Cluster Administration and Reservation " +
                "Tool'. All those software are open source, so don't hesitate ! If you have a SCI Cluster you can also try the Whoops! " +
                "clustered web server. Actually Whoops! stands for something ! Yes, it is a Web cache with tcp Handoff, On the fly " +
                "cOmpression, parallel Pull-based lru for Sci clusters !! Ok, that was a lot of fun but now it is starting to be quite late " +
                "and I'll have to go to bed very soon, so I think if you need more information, just go on <h1>http://sci-serv.inrialpes.fr</h1> " +
                "or you can even try http://www.cs.rice.edu and try to find where Emmanuel Cecchet or Julie Marguerite are and you will " +
                "maybe get fresh news about all that !!<p>";

        // Comments specific variables
        int staticDescriptionLength = staticDescription.length();
        String[] staticComment = {"This is a very bad comment. Stay away from this seller !!<p>",
                "This is a comment below average. I don't recommend this user !!<p>",
                "This is a neutral comment. It is neither a good or a bad seller !!<p>",
                "This is a comment above average. You can trust this seller even if it is not the best deal !!<p>",
                "This is an excellent comment. You can make really great deals with this seller !!<p>"};
        int[] staticCommentLength = {staticComment[0].length(), staticComment[1].length(), staticComment[2].length(),
                staticComment[3].length(), staticComment[4].length()};
        int[] ratingValue = {-5, -3, 0, 3, 5};
        int rating;
        String comment;

        // Bids specific variables
        int nbBids;

        // All purpose variables
        int i, j;
        URL url;
        String HTTPreply;

        // Cache variables
        int getItemDescriptionLength = rubis.getItemDescriptionLength();
        float getPercentReservePrice = rubis.getPercentReservePrice();
        float getPercentBuyNow = rubis.getPercentBuyNow();
        float getPercentUniqueItems = rubis.getPercentUniqueItems();
        int getMaxItemQty = rubis.getMaxItemQty();
        int getCommentMaxLength = rubis.getCommentMaxLength();
        int getNbOfCategories = rubis.getNbOfCategories();
        int getNbOfUsers = rubis.getNbOfUsers();
        int getMaxBidsPerItem = rubis.getMaxBidsPerItem();

        System.out.println("Generating " + oldItems + " old items and " + activeItems + " active items.");
        if (generateBids)
            System.out.println("Generating up to " + getMaxBidsPerItem + " bids per item.");
        if (generateComments)
            System.out.println("Generating 1 comment per item");

        Connection c;

        try {
            c = InitDBSQL.getConnection();
            c.setAutoCommit(false);                   //(NULL, :name, :description, :initialPrice, :qty, :reservePrice, :buyNow, 0, 0, NOW(), :end, :userId, :categoryId)
            PreparedStatement ps_items = c.prepareStatement("INSERT INTO items VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement ps_old_items = c.prepareStatement("INSERT INTO old_items VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement ps = null;
            PreparedStatement ps_bids = c.prepareStatement("INSERT INTO bids VALUES (DEFAULT, ?,?,?,?,?, NOW())");
            PreparedStatement ps_comments = c.prepareStatement("INSERT INTO comments VALUES (DEFAULT, ?,?,?,?,NOW(), ?)");
            PreparedStatement ps_user_update = c.prepareStatement("UPDATE users SET rating=rating+? WHERE id=?");

            for (i = 0; i < totalItems; i++) {
                // Generate the item
                name = "RUBiS automatically generated item #" + (i + 1);
                int descriptionLength = rand.nextInt(getItemDescriptionLength) + 1;
                description = "";
                while (staticDescriptionLength < descriptionLength) {
                    description = description + staticDescription;
                    descriptionLength -= staticDescriptionLength;
                }
                description += staticDescription.substring(0, descriptionLength);
                initialPrice = rand.nextInt(5000) + 1;
                duration = rand.nextInt(7) + 1;
                if (i < oldItems) { // This is an old item
                    ps = ps_old_items;
                    duration = -duration; // give a negative auction duration so that auction will be over
                    if (i < getPercentReservePrice * oldItems / 100)
                        reservePrice = rand.nextInt(1000) + initialPrice;
                    else
                        reservePrice = 0;
                    if (i < getPercentBuyNow * oldItems / 100)
                        buyNow = rand.nextInt(1000) + initialPrice + reservePrice;
                    else
                        buyNow = 0;
                    if (i < getPercentUniqueItems * oldItems / 100)
                        quantity = 1;
                    else
                        quantity = rand.nextInt(getMaxItemQty) + 1;
                } else {
                    ps = ps_old_items;
                    if (i < getPercentReservePrice * activeItems / 100)
                        reservePrice = rand.nextInt(1000) + initialPrice;
                    else
                        reservePrice = 0;
                    if (i < getPercentBuyNow * activeItems / 100)
                        buyNow = rand.nextInt(1000) + initialPrice + reservePrice;
                    else
                        buyNow = 0;
                    if (i < getPercentUniqueItems * activeItems / 100)
                        quantity = 1;
                    else
                        quantity = rand.nextInt(getMaxItemQty) + 1;
                }
                categoryId = i % getNbOfCategories;
                // Hopefully everything is ok and we will not have a deadlock here
                while (itemsPerCategory[categoryId] == 0)
                    categoryId = (categoryId + 1) % getNbOfCategories;
                if (i >= oldItems)
                    itemsPerCategory[categoryId]--;
                sellerId = rand.nextInt(getNbOfUsers) + 1;


                Calendar now = Calendar.getInstance();

                String start = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(now.getTime());
                now.add(Calendar.DATE, duration);
                String end = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(now.getTime());
                ps.setString(1, name);
                ps.setString(2, description);
                ps.setFloat(3, initialPrice);
                ps.setInt(4, quantity);
                ps.setFloat(5, reservePrice);
                ps.setFloat(6, buyNow);

                ps.setString(9, start);
                ps.setString(10, end);

                ps.setInt(11, sellerId);
                ps.setInt(12, categoryId + 1);

                nbBids=0;
                float maxBid = 0;
                if (generateBids) { // Now deal with the bids
                    nbBids = rand.nextInt(getMaxBidsPerItem);
                    for (j = 0; j < nbBids; j++) {
                        int addBid = rand.nextInt(10) + 1;

                        int itemId = i+1;
                        int userId = rand.nextInt(getNbOfUsers) + 1;
                        float bid = initialPrice + addBid;
                        maxBid = Math.max(maxBid, bid);

                        ps_bids = c.prepareStatement("INSERT INTO bids VALUES (DEFAULT, ?,?,?,?,?, NOW())");
                        ps_bids.setInt(1, userId);
                        ps_bids.setInt(2, itemId);
                        ps_bids.setInt(3, rand.nextInt(quantity) + 1); //qty
                        ps_bids.setFloat(4, bid); //bid
                        ps_bids.setFloat(5, maxBid);

                        ps_bids.addBatch();

                        initialPrice += addBid; // We use initialPrice as minimum bid
                    }
                }

                ps.setInt(7, nbBids);
                ps.setFloat(8, maxBid);
                ps.addBatch();

                if (generateComments) { // Generate the comment
                    rating = rand.nextInt(5);
                    int commentLength = rand.nextInt(getCommentMaxLength) + 1;
                    comment = "";
                    while (staticCommentLength[rating] < commentLength) {
                        comment = comment + staticComment[rating];
                        commentLength -= staticCommentLength[rating];
                    }
                    comment += staticComment[rating].substring(0, commentLength);

                    int itemId = i+1;
                    int userId = rand.nextInt(getNbOfUsers) + 1;

                    ps_comments.setInt(1, userId);
                    ps_comments.setInt(2, sellerId);
                    ps_comments.setInt(3, itemId);
                    ps_comments.setInt(4, ratingValue[rating]);
                    ps_comments.setString(5, comment);
                    ps_comments.addBatch();

                    ps_user_update.setInt(1, ratingValue[rating]);
                    ps_user_update.setInt(2, sellerId);
                    ps_user_update.addBatch();
                }
            }
            ps_items.executeBatch();
            ps_old_items.executeBatch();
            ps_bids.executeBatch();
            ps_comments.executeBatch();
            ps_user_update.executeBatch();
            c.setAutoCommit(true);
        } catch (Exception e) {
            System.err.println("Error while generating items: " + e.getMessage());
        }
        System.out.println(" Done!");
    }


    /**
     * Call the HTTP Server according to the given URL and get the reply
     *
     * @param url URL to access
     * @return <code>String</code> containing the web server reply (HTML file)
     */
    private String callHTTPServer(URL url) {
        String HTMLReply = "";
        BufferedInputStream in = null;
        int retry = 0;

        while (retry < 5) {
            // Open the connexion
            try {
                in = new BufferedInputStream(url.openStream(), 4096);
            } catch (IOException ioe) {
                System.err.println("Unable to open URL " + url + " (" + ioe.getMessage() + ")");
                retry++;
                try {
                    Thread.currentThread().sleep(1000L);
                } catch (InterruptedException i) {
                    System.err.println("Interrupted in callHTTPServer()");
                    return null;
                }
                continue;
            }

            // Get the data
            try {
                byte[] buffer = new byte[4096];
                int read;

                while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                    if (read > 0)
                        HTMLReply = HTMLReply + new String(buffer, 0, read);
                }
            } catch (IOException ioe) {
                System.err.println("Unable to read from URL " + url + " (" + ioe.getMessage() + ")");
                return null;
            }

            // No retry at this point
            break;
        }

        try {
            if (in != null)
                in.close();
        } catch (IOException ioe) {
            System.err.println("Unable to close URL " + url + " (" + ioe.getMessage() + ")");
        }
        return HTMLReply;
    }

}
