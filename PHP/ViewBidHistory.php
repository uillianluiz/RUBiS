<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "ViewBidHistory.php";
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
                printError($scriptName, $startTime, "Bid history", "You must provide a $description!<br>");
                exit();
            }
        }

        $itemId = post_or_get('itemId', 'item identifier');

        // Get the item name
        $itemNameResult = $DBQueries->selectItemById($itemId, 1);

        if ($DBQueries->selectCountFrom("items") == 0) {
            $itemNameResult = $DBQueries->selectItemById($itemId, 2);
            if ($DBQueries->selectCountFrom("old_items") == 0)
                die("<h3>ERROR: Sorry, but this item does not exist.</h3><br>\n");
        }

        $itemName = $itemNameResult[0]['name'];
        
        // Get the list of bids for this item
        $bidsListResult = $DBQueries->selectBidsByItem($itemId, PHP_INT_MAX);

        if ($DBQueries->selectNumBidsByItem($itemId) == 0)
            print ("<h2>There is no bid for $itemName. </h2><br>");
        else
            print ("<h2><center>Bid history for $itemName</center></h2><br>");


        if ($DBQueries->selectNumBidsByItem($itemId))
            printHTMLheader("RUBiS: Bid history for $itemName.");
        print("<TABLE border=\"1\" summary=\"List of bids\">\n" .
                "<THEAD>\n" .
                "<TR><TH>User ID<TH>Bid amount<TH>Date of bid\n" .
                "<TBODY>\n");

        foreach ($bidsListResult as $bidsListRow) {
            $bidAmount = $bidsListRow["bid"];
            $bidDate = $bidsListRow["date"];
            $userId = $bidsListRow["user_id"];
            // Get the bidder nickname	
            if ($userId != 0) {
                $usernameResult = $DBQueries->selectUserById($userId);
                
                $nickname = $usernameResult[0]['nickname'];
            } else {
                print("Cannot lookup the user!<br>");
                printHTMLfooter($scriptName, $startTime);
                exit();
            }
            print("<TR><TD><a href=\"/PHP/ViewUserInfo.php?userId=" . $userId . "\">$nickname</a>"
                    . "<TD>" . $bidAmount . "<TD>" . $bidDate . "\n");
        }
        print("</TABLE>\n");

        $DBQueries = null;

        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
