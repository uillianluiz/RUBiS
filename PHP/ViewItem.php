<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "ViewItem.php";
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
                printError($scriptName, $startTime, "Viewing item", "You must provide a $description!<br>");
                exit();
            }
        }

        $itemId = post_or_get('itemId', 'item identifier');

        $result = $DBQueries->selectItemById($itemId, 1);

        if ($DBQueries->selectCountFrom("items") == 0) {
            $result = $DBQueries->selectItemById($itemId, 2);
            if ($DBQueries->selectCountFrom("old_items") == 0)
                die("<h3>ERROR: Sorry, but this item does not exist.</h3><br>\n");
        }

        $row = $result[0];
        $maxBid = $DBQueries->selectMaxItemBid($itemId);
        if ($maxBid == 0) {
            $maxBid = $row["initial_price"];
            $buyNow = $row["buy_now"];
            $firstBid = "none";
        } else {
            //$buyNow = -1;
            if ($row["quantity"] > 1) {
                $xRes = $DBQueries->selectBidsByItem($itemId, $row["quantity"]);
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
            $nbOfBids = $DBQueries->selectNumBidsByItem($itemId);
      
        }

        printHTMLheader("RUBiS: Viewing " . $row["name"]);
        printHTMLHighlighted($row["name"]);
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

        $sellerName = $DBQueries->selectUserById($row["seller"])[0]['nickname']; 
        
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

        print("<a href=\"/PHP/PutBidAuth.php?itemId=" . $row["id"] . "\"><IMG SRC=\"/PHP/bid_now.jpg\" height=22 width=90> on this item</a>\n");

        printHTMLHighlighted("Item description");
        print($row["description"]);
        print("<br><p>\n");

        $DBQueries = null;
        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
