<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "StoreBuyNow.php";
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
        $maxQty = post_or_get('maxQty', 'maximum quantity');
        $qty = post_or_get('qty', 'quantity');


        /* Check for invalid values */

        if ($qty > $maxQty) {
            printError("<h3>You cannot request $qty items because only $maxQty are proposed !<br></h3>");
            return;
        }


        $status = $DBQueries->process_buyNow($itemId, $qty, $userId);

        if ($status == -1) {
            printError($scriptName, $startTime, "BuyNow", "<h3>Sorry, but this item does not exist.</h3><br>");
            $DBQueries = null;
            exit();
        }
        $DBQueries = null;

        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
