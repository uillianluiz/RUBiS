<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "PutBid.php";
        include("PHPprinter.php");
        include("DBQueries.php");
        $startTime = getMicroTime();
        $DBQueries = new DBQueries();

        function post_or_get($index, $description) {
            if (isset($_POST[$index])) {
                return $_POST[$index];
            } else if (isset($_GET[$index])) {
                return $_GET[$index];
            } else {
                printError($scriptName, $startTime, "PutBid", "You must provide a $description!<br>");
                exit();
            }
        }

        $nickname = post_or_get('nickname', 'nick name');
        $password = post_or_get('password', 'password');
        $itemId = post_or_get('itemId', 'item identifier');

        // Authenticate the user 
        $userId = $DBQueries->user_authenticate($nickname, $password);
        if ($userId == -1) {
            $DBQueries = null;
            die("<h2>ERROR: You don't have an account on RUBis! You have to register first.</h2><br>");
        }

        $result = $DBQueries->selectItemById($itemId, 1);
             
        if (count($result) == 0) {
            printError($scriptName, $startTime, "PutBid", "<h3>Sorry, but this item does not exist.</h3><br>");
            $DBQueries = null;
            exit();
        }

        $row = $result[0];
        $maxBidResult = $DBQueries->selectMaxItemBid($row["id"]);
        $maxBidRow = $maxBidResult[0];
        $maxBid = $maxBidRow["bid"];
        if ($maxBid == 0) {
            $maxBid = $row["initial_price"];
            $buyNow = $row["buy_now"];
            $firstBid = "none";
        } else {
            if ($row["quantity"] > 1) {
                $xRes = $DBQueries->selectBidsByItem($row["id"], $row["quantity"]);
                        
                $nb = 0;
                foreach ($xRes as $xRow) {
                    $nb = $nb + $xRow["qty"];
                    if ($nb > $row["quantity"]) {
                        $maxBid = $row["bid"];
                        break;
                    }
                }
            }
            $firstBid = $maxBid;
            $nbOfBidsResult = $DBQueries->selectNumBidsByItem($row["id"]);
            $nbOfBidsRow = $nbOfBidsResult[0];
            $nbOfBids = $nbOfBidsRow["bid"];
        }

        printHTMLheader("RUBiS: Bidding");
        printHTMLHighlighted("You are ready to bid on: " . $row["name"]);
        print("<TABLE>\n" .
                "<TR><TD>Currently<TD><b><BIG>$maxBid</BIG></b>\n");

        // Check if the reservePrice has been met (if any)
        $reservePrice = $row["reserve_price"];
        if ($reservePrice > 0) {
            if ($maxBid >= $reservePrice) {
                print("(The reserve price has been met)\n");
            } else {
                print("(The reserve price has NOT been met)\n");
            }
        }

        $sellerNameResult = $DBQueries->selectUserById($row["seller"]);
                
        $sellerNameRow = $sellerNameResult[0];
        $sellerName = $sellerNameRow["nickname"];

        print("<TR><TD>Quantity<TD><b><BIG>" . $row["quantity"] . "</BIG></b>\n");
        print("<TR><TD>First bid<TD><b><BIG>$firstBid</BIG></b>\n");
        print("<TR><TD># of bids<TD><b><BIG>$nbOfBids</BIG></b> (<a href=\"/PHP/ViewBidHistory.php?itemId=" . $row["id"] . "\">bid history</a>)\n");
        print("<TR><TD>Seller<TD><a href=\"/PHP/ViewUserInfo.php?userId=" . $row["seller"] . "\">$sellerName</a> (<a href=\"/PHP/PutCommentAuth.php?to=" . $row["seller"] . "&itemId=" . $row["id"] . "\">Leave a comment on this user</a>)\n");
        print("<TR><TD>Started<TD>" . $row["start_date"] . "\n");
        print("<TR><TD>Ends<TD>" . $row["end_date"] . "\n");
        print("</TABLE>\n");

        // Can the user by this item now ?
        if ($buyNow > 0)
            print("<p><a href=\"/PHP/BuyNowAuth.php?itemId=" . $row["id"] . "\">" .
                    "<IMG SRC=\"/PHP/buy_it_now.jpg\" height=22 width=150></a>" .
                    "  <BIG><b>You can buy this item right now for only \$$buyNow</b></BIG><br><p>\n");

        printHTMLHighlighted("Item description");
        print($row["description"]);
        print("<br><p>\n");

        printHTMLHighlighted("Bidding");
        $minBid = $maxBid + 1;
        print("<form action=\"/PHP/StoreBid.php\" method=POST>\n" .
                "<input type=hidden name=minBid value=$minBid>\n" .
                "<input type=hidden name=userId value=$userId>\n" .
                "<input type=hidden name=itemId value=" . $row["id"] . ">\n" .
                "<input type=hidden name=maxQty value=" . $row["quantity"] . ">\n" .
                "<center><table>\n" .
                "<tr><td>Your bid (minimum bid is $minBid):</td>\n" .
                "<td><input type=text size=10 name=bid></td></tr>\n" .
                "<tr><td>Your maximum bid:</td>\n" .
                "<td><input type=text size=10 name=maxBid></td></tr>\n");
        if ($row["quantity"] > 1)
            print("<tr><td>Quantity:</td><td><input type=text size=5 name=qty></td></tr>\n");
        else
            print("<input type=hidden name=qty value=1>\n");
        print("</table><p><input type=submit value=\"Bid now!\"></center><p>\n");

        $DBQueries = null;

        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
