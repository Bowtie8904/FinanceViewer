package core.financ;

import java.io.File;
import java.util.concurrent.TimeUnit;

import bowt.db.DatabaseAccess;
import bowt.db.store.SqlEntry;
import bowt.prop.Properties;
import bowt.thread.Threads;

/**
 * @author &#8904
 *
 */
public class TransactionReader
{
    private long readInterval;
    private File searchDir;
    private DatabaseAccess db;

    public TransactionReader(DatabaseAccess db)
    {
        this.db = db;
        this.searchDir = new File(Properties.getValueOf("searchDir"));
        this.readInterval = Long.parseLong(Properties.getValueOf("readInterval"));
    }

    public void start()
    {
        Threads.get().scheduleAtFixedRate(() ->
        {
            read();
        }, readInterval, readInterval, TimeUnit.MILLISECONDS);
    }

    public void read()
    {
        File[] files = this.searchDir.listFiles();
        var parser = new TransactionParser();

        for (File file : files)
        {
            this.db.select().from("parsedFile")
                    .where("fileName").equals(file.getName())
                    .onLessThan(1, (i, set) ->
                    {
                        for (Transaction t : parser.parseTransactions(file))
                        {
                            SqlEntry.persist(this.db, t);
                        }

                        this.db.insert().into("parsedFile")
                                .set("fileName", file.getName())
                                .execute();

                        return set;
                    })
                    .execute();
        }
    }
}