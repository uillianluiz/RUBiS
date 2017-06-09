<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "StoreComment.php";
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
                printError($scriptName, $startTime, "StoreComment", "You must provide a $description!<br>");
                exit();
            }
        }

        $to = post_or_get('to', "'to user'");
        $from = post_or_get('from', "'from user'");
        $itemId = post_or_get('itemId', "item identifier");
        $rating = post_or_get('rating', "rating");
        $comment = post_or_get('comment', "comment");

        $DBQueries = new DBQueries();

        $ret = $DBQueries->process_comment($from, $to, $itemId, $rating, $comment);
        
        if($ret == -1){
            printError($scriptName, $startTime, "StoreComment", "<h3>Sorry, but this user $to does not exist.</h3><br>");
            exit();
        }
        
        printHTMLheader("RUBiS: Comment posting");
        print("<center><h2>Your comment has been successfully posted.</h2></center>\n");

        $DBQueries = null;

        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
