<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "SearchItemsByCategories.php";
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
                printError($scriptName, $startTime, "Search Items By Category", "You must provide a $description!<br>");
                exit();
            }
        }

        $categoryName = post_or_get('categoryName', 'category name');
        $categoryId = post_or_get('category', 'category identifier');


        if (isset($_POST['page'])) {
            $page = $_POST['page'];
        } else if (isset($_GET['page'])) {
            $page = $_GET['page'];
        } else {
            $page = 0;
        }

        if (isset($_POST['nbOfItems'])) {
            $nbOfItems = $_POST['nbOfItems'];
        } else if (isset($_GET['nbOfItems'])) {
            $nbOfItems = $_GET['nbOfItems'];
        } else {
            $nbOfItems = 25;
        }

        printHTMLheader("RUBiS: Items in category $categoryName");
        print("<h2>Items in category $categoryName</h2><br><br>");

        $result = $DBQueries->selectItemsByCategory($categoryId, $page, $nbOfItems);
      
        if (count($result) == 0) {
            if ($page == 0)
                print("<h2>Sorry, but there are no items available in this category !</h2>");
            else {
                print("<h2>Sorry, but there are no more items available in this category !</h2>");
                print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByCategory.php?category=$categoryId" .
                        "&categoryName=" . urlencode($categoryName) . "&page=" . ($page - 1) . "&nbOfItems=$nbOfItems\">Previous page</a>\n</CENTER>\n");
            }
            
            printHTMLfooter($scriptName, $startTime);
            exit();
        } else {
            print("<TABLE border=\"1\" summary=\"List of items\">" .
                    "<THEAD>" .
                    "<TR><TH>Designation<TH>Price<TH>Bids<TH>End Date<TH>Bid Now" .
                    "<TBODY>");
        }

        foreach ($result as $row) {
            $maxBid = $row["max_bid"];
            if ($maxBid == 0)
                $maxBid = $row["initial_price"];

            print("<TR><TD><a href=\"/PHP/ViewItem.php?itemId=" . $row["id"] . "\">" . $row["name"] .
                    "<TD>$maxBid" .
                    "<TD>" . $row["nb_of_bids"] .
                    "<TD>" . $row["end_date"] .
                    "<TD><a href=\"/PHP/PutBidAuth.php?itemId=" . $row["id"] . "\"><IMG SRC=\"/PHP/bid_now.jpg\" height=22 width=90></a>");
        }
        print("</TABLE>");

        if ($page == 0)
            print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByCategory.php?category=$categoryId" .
                    "&categoryName=" . urlencode($categoryName) . "&page=" . ($page + 1) . "&nbOfItems=$nbOfItems\">Next page</a>\n</CENTER>\n");
        else
            print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByCategory.php?category=$categoryId" .
                    "&categoryName=" . urlencode($categoryName) . "&page=" . ($page - 1) . "&nbOfItems=$nbOfItems\">Previous page</a>\n&nbsp&nbsp&nbsp" .
                    "<a href=\"/PHP/SearchItemsByCategory.php?category=$categoryId" .
                    "&categoryName=" . urlencode($categoryName) . "&page=" . ($page + 1) . "&nbOfItems=$nbOfItems\">Next page</a>\n\n</CENTER>\n");

        $DBQueries = null;

        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
