<?php

require_once 'DBConnection.php';

class DBQueries {

    function __construct() {
        $this->database = new DBConnection();
        $this->database->connect();
        $this->connection = $this->database->get_connection();
    }

    function __destruct() {
        $this->database->close_connection();
    }

    function insert_users($firstname, $lastname, $nickname, $password, $email, $regionId) {
        $stmt = $this->connection->prepare("INSERT INTO users VALUES (NULL, :firstname, :lastname, :nickname, :password, :email, 0, 0, NOW(), :regionId);");

        $stmt->bindValue(':firstname', $firstname, PDO::PARAM_STR);
        $stmt->bindValue(':lastname', $lastname, PDO::PARAM_STR);
        $stmt->bindValue(':nickname', $nickname, PDO::PARAM_STR);
        $stmt->bindValue(':password', $password, PDO::PARAM_STR);
        $stmt->bindValue(':email', $email, PDO::PARAM_STR);
        $stmt->bindValue(':regionId', $regionId, PDO::PARAM_INT);

        return $stmt->execute() ? 1 : 0;
    }

    function insert_items($name, $description, $initialPrice, $qty, $reservePrice, $buyNow, $end, $userId, $categoryId) {
        $stmt = $this->connection->prepare("INSERT INTO items VALUES (NULL, :name, :description, :initialPrice, :qty, :reservePrice, :buyNow, 0, 0, NOW(), :end, :userId, :categoryId);");

        $stmt->bindValue(':name', $name, PDO::PARAM_STR);
        $stmt->bindValue(':description', $description, PDO::PARAM_STR);
        $stmt->bindValue(':initialPrice', $initialPrice, PDO::PARAM_STR);
        $stmt->bindValue(':qty', $qty, PDO::PARAM_STR);
        $stmt->bindValue(':reservePrice', $reservePrice, PDO::PARAM_STR);
        $stmt->bindValue(':buyNow', $buyNow, PDO::PARAM_INT);
        $stmt->bindValue(':end', $end, PDO::PARAM_INT);
        $stmt->bindValue(':userId', $userId, PDO::PARAM_INT);
        $stmt->bindValue(':categoryId', $categoryId, PDO::PARAM_INT);

        return $stmt->execute() ? 1 : 0;
    }

    function process_comment($from, $to, $itemId, $rating, $comment) {
        $this->connection->beginTransaction();

        $stmt = $this->connection->prepare("SELECT count(*) AS count FROM users WHERE id=:to");
        $stmt->bindValue(':to', $to, PDO::PARAM_INT);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        if ($result[0]['count'] == 0)
            return -1;

        $stmt = $this->connection->prepare("UPDATE users SET rating=rating+:rating WHERE id=:to");
        $stmt->bindValue(':rating', $rating, PDO::PARAM_INT);
        $stmt->bindValue(':to', $to, PDO::PARAM_INT);
        $stmt->execute();

        $stmt = $this->connection->prepare("INSERT INTO comments VALUES (NULL, :from, :to, :itemId, :rating, NOW(), :comment);");

        $stmt->bindValue(':from', $from, PDO::PARAM_INT);
        $stmt->bindValue(':to', $to, PDO::PARAM_INT);
        $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
        $stmt->bindValue(':rating', $rating, PDO::PARAM_INT);
        $stmt->bindValue(':comment', $comment, PDO::PARAM_STR);

        $stmt->execute();

        $this->connection->commit();
        return 1;
    }

    function process_bid($itemId, $maxBid, $userId, $qty, $bid) {

        $this->connection->beginTransaction();

        $stmt = $this->connection->prepare("SELECT max_bid FROM items WHERE id=:itemId");
        $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        if ($maxBid > $result[0]['max_bid']) {
            $stmt = $this->connection->prepare("UPDATE items SET max_bid=:maxBid WHERE id=:itemId");
            $stmt->bindValue(':max_bid', $maxBid, PDO::PARAM_INT);
            $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
            $stmt->execute();
        }

        $stmt = $this->connection->prepare("INSERT INTO bids VALUES (NULL, :userId, :itemId, :qty, :bid, :maxBid, NOW())");
        $stmt->bindValue(':userId', $userId, PDO::PARAM_INT);
        $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
        $stmt->bindValue(':qty', $qty, PDO::PARAM_INT);
        $stmt->bindValue(':bid', $bid, PDO::PARAM_INT);
        $stmt->bindValue(':maxBid', $maxBid, PDO::PARAM_INT);
        $stmt->execute();

        $stmt = $this->connection->prepare("UPDATE items SET nb_of_bids=nb_of_bids+1 WHERE id=:itemId");
        $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
        $stmt->execute();

        $this->connection->commit();
    }

    function process_buyNow($itemId, $qty, $userId) {

        $this->connection->beginTransaction();

        $stmt = $this->connection->prepare("SELECT * FROM items WHERE items.id=:itemId");
        $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        if (count($result) == 0)
            return -1;

        $row = $result[0];
        $newQty = $row["quantity"] - $qty;
        if ($newQty == 0) {
            $stmt = $this->connection->prepare("UPDATE items SET end_date=NOW(),quantity=:newQty WHERE id=:itemId");
            $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
            $stmt->bindValue(':newQty', $newQty, PDO::PARAM_INT);
            $stmt->execute();
        } else {
            $stmt = $this->connection->prepare("UPDATE items SET quantity=:newQty WHERE id=:itemId");
            $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
            $stmt->bindValue(':newQty', $newQty, PDO::PARAM_INT);
            $stmt->execute();
        }

        $stmt = $this->connection->prepare("INSERT INTO buy_now VALUES (NULL, :userId, :itemId, :qty, NOW())");
        $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
        $stmt->bindValue(':userId', $userId, PDO::PARAM_INT);
        $stmt->bindValue(':qty', $qty, PDO::PARAM_INT);
        $stmt->execute();

        $this->connection->commit();
        
        return 1;
    }

    function user_authenticate($nickname, $password) {
        $stmt = $this->connection->prepare("SELECT id FROM users WHERE nickname=:nickname AND password=:password");
        $stmt->bindValue(':nickname', $nickname, PDO::PARAM_STR);
        $stmt->bindValue(':password', $password, PDO::PARAM_STR);

        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return isset($result[0]['id']) ? $result[0]['id'] : -1;
    }

    function selectFrom($table) {
        $stmt = $this->connection->prepare("SELECT * FROM $table;");

        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectCountFrom($table) {
        $stmt = $this->connection->prepare("SELECT count(*) AS count FROM $table;");

        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result[0]['count'];
    }

    function selectItemsByCategory($categoryId, $page, $nbOfItems) {
        if ($this->database->get_host_type() == "mssql") {
            $query = "SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items WHERE category=:categoryId AND end_date>=NOW() BETWEEN :start AND :end";

            $start = $page * $nbOfItems;
            $end = $start + $nbOfItems;
        } else {
            $query = "SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items WHERE category= :categoryId LIMIT :start, :end";

            $start = $page * $nbOfItems;
            $end = $nbOfItems;
        }
        $stmt = $this->connection->prepare($query);

        $stmt->bindValue(':categoryId', $categoryId, PDO::PARAM_INT);
        $stmt->bindValue(':start', $start, PDO::PARAM_INT);
        $stmt->bindValue(':end', $end, PDO::PARAM_INT);

        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectItemsByRegion($categoryId, $regionId, $page, $nbOfItems) {
        if ($this->database->get_host_type() == "mssql") {
            $query = "SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items,users WHERE items.category=:categoryId AND items.seller=users.id AND users.region=:regionId AND end_date>=NOW() BETWEEN :start AND :end";

            $start = $page * $nbOfItems;
            $end = $start + $nbOfItems;
        } else {
            $query = "SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items,users WHERE items.category=:categoryId AND items.seller=users.id AND users.region=:regionId AND end_date>=NOW() LIMIT :start,:end";

            $start = $page * $nbOfItems;
            $end = $nbOfItems;
        }

        $stmt = $this->connection->prepare($query);

        $stmt->bindValue(':categoryId', $categoryId, PDO::PARAM_INT);
        $stmt->bindValue(':regionId', $regionId, PDO::PARAM_INT);
        $stmt->bindValue(':start', $start, PDO::PARAM_INT);
        $stmt->bindValue(':end', $end, PDO::PARAM_INT);

        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectRegionIdWhereName($region) {
        $stmt = $this->connection->prepare("SELECT id FROM regions WHERE name=:region");

        $stmt->bindValue(':region', $region, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return isset($result[0]['id']) ? $result[0]['id'] : -1;
    }

    function doesNicknameExist($nickname) {
        $stmt = $this->connection->prepare("SELECT count(*) AS count FROM users WHERE nickname=:nickname");

        $stmt->bindValue(':nickname', $nickname, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result[0]['count'];
    }

    function selectUserByNickname($nickname) {
        $stmt = $this->connection->prepare("SELECT * FROM users WHERE nickname=:nickname");

        $stmt->bindValue(':nickname', $nickname, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectUserById($idUser) {
        $stmt = $this->connection->prepare("SELECT * FROM users WHERE id=:idUser");

        $stmt->bindValue(':idUser', $idUser, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectItemById($idItem, $table) {
        if ($table == 1)
            $stmt = $this->connection->prepare("SELECT * FROM items WHERE items.id=:id");
        else
            $stmt = $this->connection->prepare("SELECT * FROM old_items WHERE items.id=:id");

        $stmt->bindValue(':id', $idItem, PDO::PARAM_INT);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectMaxItemBid($idItem) {
        $stmt = $this->connection->prepare("SELECT MAX(bid) AS bid FROM bids WHERE item_id=:id");

        $stmt->bindValue(':id', $idItem, PDO::PARAM_INT);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result[0]['bid'];
    }

    function selectNumBidsByItem($idItem) {
        $stmt = $this->connection->prepare("SELECT COUNT(*) AS bid FROM bids WHERE item_id=:id");

        $stmt->bindValue(':id', $idItem, PDO::PARAM_INT);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result[0]['bid'];
    }

    function selectBidsByItem($itemId, $quantity) {
        if ($this->database->get_host_type() == "mssql") {
            $stmt = $this->connection->prepare("SELECT * FROM bids WHERE item_id=:itemId ORDER BY bid DESC BETWEEN 0 AND :quantity");
        } else {
            $stmt = $this->connection->prepare("SELECT * FROM bids WHERE item_id=:itemId ORDER BY bid DESC LIMIT :quantity");
        }

        $stmt->bindValue(':itemId', $itemId, PDO::PARAM_INT);
        $stmt->bindValue(':quantity', $quantity, PDO::PARAM_INT);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectCommentsByToUser($userId) {
        $stmt = $this->connection->prepare("SELECT * FROM comments WHERE comments.to_user_id=:idUser");

        $stmt->bindValue(':idUser', $userId, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectUserBids($userId) {
        if ($this->database->get_host_type() == "mysql")
            $this->connection->exec("SET sql_mode = ''");

        $stmt = $this->connection->prepare("SELECT item_id, bids.max_bid FROM bids, items WHERE bids.user_id=:userId AND bids.item_id=items.id AND items.end_date>=NOW() GROUP BY item_id;");

        $stmt->bindValue(':userId', $userId, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        if ($this->database->get_host_type() == "mysql")
            $this->connection->exec("SET sql_mode = 'ONLY_FULL_GROUP_BY'");
        return $result;
    }

    function selectWonItems30Days($userId) {
        if ($this->database->get_host_type() == "mysql")
            $this->connection->exec("SET sql_mode = ''");

        $stmt = $this->connection->prepare("SELECT item_id FROM bids, old_items WHERE bids.user_id=:userId AND bids.item_id=old_items.id AND TO_DAYS(NOW()) - TO_DAYS(old_items.end_date) < 30 GROUP BY item_id;");

        $stmt->bindValue(':userId', $userId, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        if ($this->database->get_host_type() == "mysql")
            $this->connection->exec("SET sql_mode = 'ONLY_FULL_GROUP_BY'");
        return $result;
    }

    function selectBoughtItems30Days($userId) {

        $stmt = $this->connection->prepare("SELECT * FROM buy_now WHERE buy_now.buyer_id=:userId AND TO_DAYS(NOW()) - TO_DAYS(buy_now.date)<=30;");

        $stmt->bindValue(':userId', $userId, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectOnSaleItems($userId) {
        $stmt = $this->connection->prepare("SELECT * FROM items WHERE items.seller=:userId AND items.end_date>=NOW()");

        $stmt->bindValue(':userId', $userId, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectSoldItems($userId) {
        $stmt = $this->connection->prepare("SELECT * FROM old_items WHERE old_items.seller=:userId AND TO_DAYS(NOW()) - TO_DAYS(old_items.end_date) < 30");

        $stmt->bindValue(':userId', $userId, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

    function selectCommentsToUser($userId) {
        $stmt = $this->connection->prepare("SELECT * FROM comments WHERE comments.to_user_id=:userId");

        $stmt->bindValue(':userId', $userId, PDO::PARAM_STR);
        $stmt->execute();
        $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $result;
    }

}
