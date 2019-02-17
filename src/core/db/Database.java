package core.db;

import bowt.db.LocalDatabase;
import bowt.db.constants.SqlType;

/**
 * @author &#8904
 *
 */
public class Database extends LocalDatabase
{
    /**
     * @see bowt.db.DatabaseAccess#createTables()
     */
    @Override
    protected void createTables()
    {
        create().table("bankTransaction")
                .column("transactionID", SqlType.LONG).notNull().primaryKey().add()
                .column("transactionType", SqlType.VARCHAR).size(400).add()
                .column("transactionUsage", SqlType.VARCHAR).size(400).add()
                .column("senderReceiver", SqlType.VARCHAR).size(400).add()
                .column("transactionAmount", SqlType.DOUBLE).add()
                .column("bookDate", SqlType.DATE).add()
                .column("source", SqlType.VARCHAR).size(500).add()
                .onFail((statement, e) ->
                {
                    return 0;
                })
                .commit()
                .execute();

        create().table("parsedFile")
                .column("fileName", SqlType.VARCHAR).size(400).primaryKey().notNull().add()
                .onFail((statement, e) ->
                {
                    return 0;
                })
                .commit()
                .execute();
    }
}