<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "PutComment.php";
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
                printError($scriptName, $startTime, "PutComment", "You must provide your $description!<br>");
                exit();
            }
        }

        $to = post_or_get('to', 'user identifier');
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
            printError($scriptName, $startTime, "PutComment", "<h3>Sorry, but this item does not exist.</h3><br>");
            $DBQueries = null;
            exit();
        }

        $toRes = $DBQueries->selectUserById($to);

        if (count($toRes) == 0) {
            printError($scriptName, $startTime, "PutComment", "<h3>Sorry, but this user does not exist.</h3><br>");
            $DBQueries = null;
            exit();
        }

        $row = $result[0];
        $userRow = $toRes[0];

        printHTMLheader("RUBiS: Comment service");

        print("<center><h2>Give feedback about your experience with " . $userRow["name"] . "</h2><br>\n");
        print("<form action=\"/PHP/StoreComment.php\" method=POST>\n" .
                "<input type=hidden name=to value=$to>\n" .
                "<input type=hidden name=from value=$userId>\n" .
                "<input type=hidden name=itemId value=$itemId>\n" .
                "<center><table>\n" .
                "<tr><td><b>From</b><td>$nickname\n" .
                "<tr><td><b>To</b><td>" . $userRow["nickname"] . "\n" .
                "<tr><td><b>About item</b><td>" . $row["name"] . "\n" .
                "<tr><td><b>Rating</b>\n" .
                "<td><SELECT name=rating>\n" .
                "<OPTION value=\"5\">Excellent</OPTION>\n" .
                "<OPTION value=\"3\">Average</OPTION>\n" .
                "<OPTION selected value=\"0\">Neutral</OPTION>\n" .
                "<OPTION value=\"-3\">Below average</OPTION>\n" .
                "<OPTION value=\"-5\">Bad</OPTION>\n" .
                "</SELECT></table><p><br>\n" .
                "<TEXTAREA rows=\"20\" cols=\"80\" name=\"comment\">Write your comment here</TEXTAREA><br><p>\n" .
                "<input type=submit value=\"Post this comment now!\"></center><p>\n");


        $DBQueries = null;

        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
