package main.bt.back;

import java.io.File;

import bt.db.store.SqlEntry;
import main.bt.back.obj.Transaction;
import main.bt.web.Application;

/**
 * @author &#8904
 *
 */
public class TransactionReader
{
    public void read()
    {
        File[] files = Application.searchDir.listFiles();
        var parser = new TransactionParser();

        for (File file : files)
        {
            Application.database.select()
                                .from("parsedFile")
                                .where("fileName")
                                .equals(file.getName())
                                .onLessThan(1,
                                            (i, set) ->
                                            {
                                                for (Transaction t : parser.parseTransactions(file))
                                                {
                                                    SqlEntry.persist(Application.database,
                                                                     t);
                                                }

                                                Application.database.insert()
                                                                    .into("parsedFile")
                                                                    .set("fileName",
                                                                         file.getName())
                                                                    .commit()
                                                                    .execute();

                                                return set;
                                            })
                                .execute();
        }
    }
}