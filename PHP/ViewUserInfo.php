<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "ViewUserInfo.php";
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
                printError($scriptName, $startTime, "Viewing user information", "You must provide a $description!<br>");
                exit();
            }
        }
        
        $userId = post_or_get('userId', 'item identifier');

   
        $userResult = $DBQueries->selectUserById($userId);
        

        if($DBQueries->selectCountFrom("users") == 0){
            $DBQueries = null;
            die("<h3>ERROR: Sorry, but this user does not exist.</h3><br>\n");
        }

        printHTMLheader("RUBiS: View user information");

        // Get general information about the user
        $userRow = $userResult[0];
        $firstname = $userRow["firstname"];
        $lastname = $userRow["lastname"];
        $nickname = $userRow["nickname"];
        $email = $userRow["email"];
        $creationDate = $userRow["creation_date"];
        $rating = $userRow["rating"];

        print("<h2>Information about " . $nickname . "<br></h2>");
        print("Real life name : " . $firstname . " " . $lastname . "<br>");
        print("Email address  : " . $email . "<br>");
        print("User since     : " . $creationDate . "<br>");
        print("Current rating : <b>" . $rating . "</b><br>");

        // Get the comments about the user
        $commentsResult = $DBQueries->selectCommentsByToUser($userId);
        
        if (count($commentsResult) == 0)
            print("<h2>There is no comment for this user.</h2><br>\n");
        else {
            print("<DL>\n");
            foreach ($commentsResult as $commentsRow) {
                $authorId = $commentsRow["from_user_id"];
                $authorResult = $DBQueries->selectUserById($authorId);
                
                if (count($authorResult) == 0)
                    die("ERROR: This author does not exist.<br>\n");
                else {
                    $authorName = $authorResult[0]["nickname"];
                }
                $date = $commentsRow["date"];
                $comment = $commentsRow["comment"];
                print("<DT><b><BIG><a href=\"/PHP/ViewUserInfo.php?userId=" . $authorId . "\">$authorName</a></BIG></b>" . " wrote the " . $date . "<DD><i>" . $comment . "</i><p>\n");
            }
            print("</DL>\n");
        }
        $DBQueries = null;

        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
