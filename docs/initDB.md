# RUBiS database initialization

The original command `make initDB PARAM="all"` takes several days to finish inserting all information.

It is recommended to use the command `make initDBSQL PARAM="all"`, which takes some hours to finish.

The problem with the `initDB` is due to some reasons:

* It inserts the data by making calls to the web server.
  * Inserting 1 million users makes 1 million calls to the server, and each call inserts one user.
  * This becomes worse for the items, which inserts bids and comments.
* The updated version uses bulk insertion.
  * For example, items are inserted by every 1k

Also, the insertion by using the original method has a bug on the insertion of the items:

* Old items are not correctly inserted.
* According to the documentation, by default it inserts 500k old items and 32k active items. However, all items are inserted in the active table. **This bug is fixed in the updated version**.
* You can fix this insertion problem by copying the items to the old_items table:
  * `mysql -uroot rubis -p`
  * `INSERT INTO old_items SELECT FROM items WHERE id < 500000;`
  * `DELETE FROM items WHERE id < 500000;`
