<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "BuyNow.php";
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
                printError($scriptName, $startTime, "BuyNow", "You must provide a $description!<br>");
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
            printError($scriptName, $startTime, "BuyNow", "<h3>ERROR: Sorry, but this item does not exist.</h3><br>");
            $DBQueries = null;
            exit();
        }
        $row = $result[0];
        
        $sellerNameResult = $DBQueries->selectUserById($userId);
                

        $sellerNameRow = $sellerNameResult[0];
        $sellerName = $sellerNameRow["nickname"];
        

        printHTMLheader("RUBiS: Buy Now");
        printHTMLHighlighted("You are ready to buy this item: " . $row["name"]);
        print("<TABLE>\n");
        print("<TR><TD>Quantity<TD><b><BIG>" . $row["quantity"] . "</BIG></b>\n");
        print("<TR><TD>Seller<TD><a href=\"/PHP/ViewUserInfo.php?userId=" . $row["seller"] . "\">$sellerName</a> (<a href=\"/PHP/PutCommentAuth.php?to=" . $row["seller"] . "&itemId=" . $row["id"] . "\">Leave a comment on this user</a>)\n");
        print("<TR><TD>Started<TD>" . $row["start_date"] . "\n");
        print("<TR><TD>Ends<TD>" . $row["end_date"] . "\n");
        print("</TABLE>\n");

        printHTMLHighlighted("Item description");
        print($row["description"]);
        print("<br><p>\n");

        printHTMLHighlighted("Buy Now");
        print("<form action=\"/PHP/StoreBuyNow.php\" method=POST>\n" .
                "<input type=hidden name=userId value=$userId>\n" .
                "<input type=hidden name=itemId value=" . $row["id"] . ">\n" .
                "<input type=hidden name=maxQty value=" . $row["quantity"] . ">\n");
        if ($row["quantity"] > 1)
            print("<center><table><tr><td>Quantity:</td><td><input type=text size=5 name=qty></td></tr></table></center>\n");
        else
            print("<input type=hidden name=qty value=1>\n");
        print("</table><p><center><input type=submit value=\"Buy now!\"></center><p>\n");

        $DBQueries = null;

        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
