<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "AboutMe.php";
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
                printError($scriptName, $startTime, "About me", "You must provide your $description!<br>");
                exit();
            }
        }

        $nickname = post_or_get('nickname', 'nick name');
        $password = post_or_get('password', 'password');


        // Authenticate the user 
        $userId = $DBQueries->user_authenticate($nickname, $password);
        if ($userId == -1) {
            $DBQueries = null;
            die("<h2>ERROR: You don't have an account on RUBis! You have to register first.</h2><br>");
        }

        $userResult = $DBQueries->selectUserById($userId);
        if (count($userResult) == 0) {
            $DBQueries = null;
            die("<h3>ERROR: Sorry, but this user does not exist.</h3><br>\n");
        }

        printHTMLheader("RUBiS: About me");

        // Get general information about the user
        $userRow = $userResult[0];
        $firstname = $userRow["firstname"];
        $lastname = $userRow["lastname"];
        $nickname = $userRow["nickname"];
        $email = $userRow["email"];
        $creationDate = $userRow["creation_date"];
        $rating = $userRow["rating"];

        printHTMLHighlighted("<h2>Information about " . $nickname . "<br></h2>");
        print("Real life name : " . $firstname . " " . $lastname . "<br>");
        print("Email address  : " . $email . "<br>");
        print("User since     : " . $creationDate . "<br>");
        print("Current rating : <b>" . $rating . "</b><br><p>");

        // Get the items the user has bid on
        $bidsResult = $DBQueries->selectUserBids($userId);

        if (count($bidsResult) == 0)
            printHTMLHighlighted("<h2>You did not bid on any item.</h2>\n");
        else {
            printHTMLHighlighted("<h3>Items you have bid on.</h3>\n");
            print("<TABLE border=\"1\" summary=\"Items You've bid on\">\n" .
                    "<THEAD>\n" .
                    "<TR><TH>Designation<TH>Initial Price<TH>Current price<TH>Your max bid<TH>Quantity" .
                    "<TH>Start Date<TH>End Date<TH>Seller<TH>Put a new bid\n" .
                    "<TBODY>\n");
            foreach ($bidsResult as $bidsRow) {
                $maxBid = $bidsRow["max_bid"];
                $itemId = $bidsRow["item_id"];
                $itemResult = $DBQueries->selectItemById($itemId, 1);

                if (count($itemResult) == 0) {
                    $itemResult = $DBQueries->selectItemById($itemId, 2);
                    if (count($itemResult) == 0) {
                        $DBQueries = null;
                        die("<h3>ERROR: This item does not exist.</h3><br>\n");
                    }
                }

//	  $currentPriceResult = mysql_query("SELECT MAX(bid) AS bid FROM bids WHERE item_id=$itemId", $link) or die("ERROR: Query failed for getting the item current price.");
//	  if (mysql_num_rows($currentPriceResult) == 0)
//	    die ("ERROR: Cannot get the current price (sold item).");
//	  $currentPriceRow = mysql_fetch_array($currentPriceResult);
                $itemRow = $itemResult[0];
                $currentPrice = $itemRow["max_bid"];
                if ($currentPrice == null)
                    $currentPrice = "none";

                $itemName = $itemRow["name"];
                $itemInitialPrice = $itemRow["initial_price"];
                $quantity = $itemRow["quantity"];
                $itemReservePrice = $itemRow["reserve_price"];
                $startDate = $itemRow["start_date"];
                $endDate = $itemRow["end_date"];
                $sellerId = $itemRow["seller"];


                $sellerResult = $DBQueries->selectUserById($sellerId);
                if (count($sellerResult) == 0) {
                    $DBQueries = null;
                    die("<h3>ERROR: This seller does not exist.</h3><br>\n");
                }
                $sellerRow = $sellerResult[0];
                $sellerNickname = $sellerRow["nickname"];

                print("<TR><TD><a href=\"/PHP/ViewItem.php?itemId=" . $itemId . "\">" . $itemName .
                        "<TD>" . $itemInitialPrice . "<TD>" . $currentPrice . "<TD>" . $maxBid . "<TD>" . $quantity .
                        "<TD>" . $startDate . "<TD>" . $endDate .
                        "<TD><a href=\"/PHP/ViewUserInfo.php?userId=" . $sellerId . "\">" . $sellerNickname .
                        "<TD><a href=\"/PHP/PutBid.php?itemId=" . $itemId . "&nickname=" . urlencode($nickname) . "&password=" . urlencode($password) . "\"><IMG SRC=\"/PHP/bid_now.jpg\" height=22 width=90></a>\n");
            }
            print("</TBODY></TABLE><p>\n");
        }

        // Get the items the user won in the past 30 days
        $wonItemsResult = $DBQueries->selectWonItems30Days($userId);
        if (count($wonItemsResult) == 0)
            printHTMLHighlighted("<h3>You didn't win any item.</h3>\n");
        else {
            printHTMLHighlighted("<h3>Items you won in the past 30 days.</h3>\n");
            print("<p><TABLE border=\"1\" summary=\"List of items\">\n" .
                    "<THEAD>\n" .
                    "<TR><TH>Designation<TH>Price you bought it<TH>Seller" .
                    "<TBODY>\n");
            foreach ($wonItemsResult as $wonItemsRow) {
                $itemId = $wonItemsRow["item_id"];

                $itemResult = $DBQueries->selectItemById($itemId, 2);
                if (count($itemResult) == 0) {
                    $DBQueries = null;
                    die("<h3>This item does not exist.</h3><br>\n");
                }

                $itemRow = $itemResult[0];
                $currentPrice = $itemRow["max_bid"];
                if ($currentPrice == null)
                    $currentPrice = "none";
                $itemName = $itemRow["name"];
                $sellerId = $itemRow["seller"];

                $sellerResult = $DBQueries->selectUserById($sellerId);
                if (count($sellerResult) == 0) {
                    $DBQueries = null;
                    die("<h3>ERROR: This seller does not exist.</h3><br>\n");
                }
                $sellerRow = $sellerResult[0];
                $sellerNickname = $sellerRow["nickname"];

                print("<TR><TD><a href=\"/PHP/ViewItem.php?itemId=" . $itemId . "\">" . $itemName .
                        "<TD>" . $currentPrice .
                        "<TD><a href=\"/PHP/ViewUserInfo.php?userId=" . $sellerId . "\">" . $sellerNickname .
                        "\n");
            }

            print("</TBODY></TABLE><p>\n");
        }

        // Get the items the user bought in the past 30 days
        $buyNowResult = $DBQueries->selectBoughtItems30Days($userId);
        if (count($buyNowResult) == 0)
            printHTMLHighlighted("<h3>You didn't buy any item in the past 30 days.</h3>\n");
        else {
            printHTMLHighlighted("<h3>Items you bought in the past 30 days.</h3>\n");
            print("<p><TABLE border=\"1\" summary=\"List of items\">\n" .
                    "<THEAD>\n" .
                    "<TR><TH>Designation<TH>Quantity<TH>Price you bought it<TH>Seller" .
                    "<TBODY>\n");

            foreach ($buyNowResult as $buyNowRow) {
                $itemId = $buyNowRow["item_id"];
                $itemResult = $DBQueries->selectItemById($itemId, 2);
                if (count($itemResult) == 0) {
                    $DBQueries = null;
                    die("<h3>ERROR: This item does not exist.</h3><br>\n");
                }

                $itemRow = $itemResult[0];
                $itemName = $itemRow["name"];
                $sellerId = $itemRow["seller"];
                $price = $itemRow["buy_now"] * $buyNowRow["qty"];

                $sellerResult = $DBQueries->selectUserById($sellerId);
                if (count($sellerResult) == 0) {
                    $DBQueries = null;
                    die("<h3>ERROR: This seller does not exist.</h3><br>\n");
                }
                $sellerRow = $sellerResult[0];
                $sellerNickname = $sellerRow["nickname"];

                print("<TR><TD><a href=\"/PHP/ViewItem.php?itemId=" . $itemId . "\">" . $itemName .
                        "<TD>" . $buyNowRow["qty"] . "<TD>$price" .
                        "<TD><a href=\"/PHP/ViewUserInfo.php?userId=" . $sellerId . "\">" . $sellerNickname .
                        "\n");

            }

            print("</TBODY></TABLE><p>\n");
        }

        // Get the items the user is currently selling
        $currentSellsResult = $DBQueries->selectOnSaleItems($userId);
        if (count($currentSellsResult) == 0)
            printHTMLHighlighted("<h3>You are currently selling no item.</h3>\n");
        else {
            printHTMLHighlighted("<h3>Items you are selling.</h3>\n");
            print("<p><TABLE border=\"1\" summary=\"List of items\">\n" .
                    "<THEAD>\n" .
                    "<TR><TH>Designation<TH>Initial Price<TH>Current price<TH>Quantity<TH>ReservePrice<TH>Buy Now" .
                    "<TH>Start Date<TH>End Date\n" .
                    "<TBODY>\n");
            foreach ($currentSellsResult as $currentSellsRow) {
                $itemName = $currentSellsRow["name"];
                $itemInitialPrice = $currentSellsRow["initial_price"];
                $quantity = $currentSellsRow["quantity"];
                $itemReservePrice = $currentSellsRow["reserve_price"];
                $buyNow = $currentSellsRow["buy_now"];
                $endDate = $currentSellsRow["end_date"];
                $startDate = $currentSellsRow["start_date"];
                $itemId = $currentSellsRow["id"];
                
                $currentPrice = $currentSellsRow["max_bid"];
                if ($currentPrice == null)
                    $currentPrice = "none";

                print("<TR><TD><a href=\"/PHP/ViewItem.php?itemId=" . $itemId . "\">" . $itemName .
                        "<TD>" . $itemInitialPrice . "<TD>" . $currentPrice . "<TD>" . $quantity .
                        "<TD>" . $itemReservePrice . "<TD>" . $buyNow .
                        "<TD>" . $startDate . "<TD>" . $endDate . "\n");

            }
            print("</TABLE><p>\n");
        }

        // Get the items the user sold the last 30 days
        $pastSellsResult = $DBQueries->selectSoldItems($userId);
        if (count($pastSellsResult) == 0)
            printHTMLHighlighted("<h3>You didn't sell any item in the last 30 days.</h3>\n");
        else {
            printHTMLHighlighted("<h3>Items you sold in the last 30 days.</h3>\n");
            print("<p><TABLE border=\"1\" summary=\"List of items\">\n" .
                    "<THEAD>\n" .
                    "<TR><TH>Designation<TH>Initial Price<TH>Current price<TH>Quantity<TH>ReservePrice<TH>Buy Now" .
                    "<TH>Start Date<TH>End Date\n" .
                    "<TBODY>\n");
             foreach ($pastSellsResult as $pastSellsRow) {
                $itemName = $pastSellsRow["name"];
                $itemInitialPrice = $pastSellsRow["initial_price"];
                $quantity = $pastSellsRow["quantity"];
                $itemReservePrice = $pastSellsRow["reserve_price"];
                $buyNow = $pastSellsRow["buy_now"];
                $endDate = $pastSellsRow["end_date"];
                $startDate = $pastSellsRow["start_date"];
                $itemId = $pastSellsRow["id"];
                
                $currentPrice = $pastSellsResult["max_bid"];
                if ($currentPrice == null)
                    $currentPrice = "none";

                print("<TR><TD><a href=\"/PHP/ViewItem.php?itemId=" . $itemId . "\">" . $itemName .
                        "<TD>" . $itemInitialPrice . "<TD>" . $currentPrice . "<TD>" . $quantity .
                        "<TD>" . $itemReservePrice . "<TD>" . $buyNow .
                        "<TD>" . $startDate . "<TD>" . $endDate . "\n");

            }
            print("</TABLE><p>\n");
        }

        // Get the comments about the user
        $commentsResult = $DBQueries->selectCommentsToUser($userId);
        if (count($commentsResult) == 0)
            printHTMLHighlighted("<h2>There is no comment for this user.</h2>\n");
        else {
            print("<p><DL>\n");
            printHTMLHighlighted("<h3>Comments about you.</h3>\n");
            foreach ($commentsResult as $commentsRow) {
                $authorId = $commentsRow["from_user_id"];
                
                
                $authorResult = $DBQueries->selectUserById($authorId);
                        
                if (count($authorResult) == 0) {
                    $DBQueries = null;
                    die("ERROR: This author does not exist.<br>\n");
                } else {
                    $authorRow = $authorResult[0];
                    $authorName = $authorRow["nickname"];
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
