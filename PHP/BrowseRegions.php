<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <body>
        <?php
        $scriptName = "BrowseRegions.php";
        include("PHPprinter.php");
        include("DBQueries.php");
        $startTime = getMicroTime();

        $DBQueries = new DBQueries();
        printHTMLheader("RUBiS available regions");

        $result = $DBQueries->selectFrom("regions");

        if ($DBQueries->selectCountFrom("regions") == 0)
            print("<h2>Sorry, but there is no region available at this time. Database table is empty</h2><br>");
        else
            print("<h2>Currently available regions</h2><br>");

        foreach($result as $row){
            print("<a href=\"/PHP/BrowseCategories.php?region=" . $row["id"] . "\">" . $row["name"] . "</a><br>\n");
        }

        $DBQueries = null;
        printHTMLfooter($scriptName, $startTime);
        ?>
    </body>
</html>
