<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "StoreBid.php";
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
                printError($scriptName, $startTime, "Store bid", "You must provide a $description!<br>");
                exit();
            }
        }

        $userId = post_or_get('userId', 'user identifier');
        $itemId = post_or_get('itemId', 'item identifier');
        $minBid = post_or_get('minBid', 'minimum bid');
        $bid = post_or_get('bid', 'bid');
        $maxBid = post_or_get('maxBid', 'maximum bid');
        $maxQty = post_or_get('maxQty', 'maximum quantity');
        $qty = post_or_get('qty', 'quantity');


        /* Check for invalid values */

        if ($qty > $maxQty) {
            printError("<h3>You cannot request $qty items because only $maxQty are proposed !<br></h3>");
            return;
        }
        if ($bid < $minBid) {
            printError("<h3>Your bid of \$$bid is not acceptable because it is below the \$$minBid minimum bid !<br></h3>");
            return;
        }
        if ($maxBid < $minBid) {
            printError("<h3>Your maximum bid of \$$maxBid is not acceptable because it is below the \$$minBid minimum bid !<br></h3>");
            return;
        }
        if ($maxBid < $bid) {
            printError("<h3>Your maximum bid of \$$maxBid is not acceptable because it is below your current bid of \$$bid !<br></h3>");
            return;
        }

        // Add bid to database and update values in item
        $DBQueries->process_bid($itemId, $maxBid, $userId, $qty, $bid);


        printHTMLheader("RUBiS: Bidding result");
        print("<center><h2>Your bid has been successfully processed.</h2></center>\n");

        $DBQueries = null;
        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
