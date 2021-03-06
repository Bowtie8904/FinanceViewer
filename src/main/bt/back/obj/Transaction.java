package main.bt.back.obj;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONObject;

import bt.db.constants.SqlType;
import bt.db.store.anot.Column;
import bt.db.store.anot.Identity;
import bt.db.store.anot.Table;
import bt.utils.id.LongID;
import bt.utils.json.JSONBuilder;
import bt.utils.json.Jsonable;

/**
 * @author &#8904
 *
 */
@Table("bankTransaction")
public class Transaction implements Jsonable
{
    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Identity
    @Column(name = "transactionID", type = SqlType.LONG)
    private long transactionID;

    @Column(name = "transactionType", type = SqlType.VARCHAR)
    private String type;

    @Column(name = "transactionUsage", type = SqlType.VARCHAR)
    private String usage;

    @Column(name = "senderReceiver", type = SqlType.VARCHAR)
    private String senderReceiver;

    @Column(name = "source", type = SqlType.VARCHAR)
    private String source;

    @Column(name = "transactionAmount", type = SqlType.DOUBLE)
    private double amount;

    @Column(name = "bookDate", type = SqlType.DATE)
    private Date bookDate;

    private Transaction()
    {

    }

    public Transaction(Calendar bookDate, double amount, String type, String senderReceiver, String usage,
                       String source)
    {
        this.transactionID = LongID.uniqueID();
        this.bookDate = new Date(bookDate.getTimeInMillis());
        this.amount = amount;
        this.type = type;
        this.senderReceiver = senderReceiver;
        this.usage = usage;
        this.source = source;
    }

    public long getID()
    {
        return this.transactionID;
    }

    public String getType()
    {
        return this.type;
    }

    public String getUsage()
    {
        return this.usage;
    }

    public String getSenderReceiver()
    {
        return this.senderReceiver;
    }

    public String getSource()
    {
        return this.source;
    }

    public double getAmount()
    {
        return this.amount;
    }

    public String getFormattedAmount()
    {
        return String.format("%.2f",
                             this.amount);
    }

    public Calendar getBookDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.bookDate.getTime());
        return cal;
    }

    public String getDateString()
    {
        return format.format(this.bookDate);
    }

    public long getBookDateMillis()
    {
        return this.bookDate.getTime();
    }

    public String[] getFormattedData()
    {
        String[] data = new String[6];
        data[0] = this.transactionID + "";
        data[1] = this.type;
        data[2] = this.usage;
        data[3] = this.senderReceiver;
        data[4] = String.format("%.2f",
                                this.amount);
        data[5] = this.bookDate.toString();

        return data;
    }

    /**
     * @see bowt.json.Jsonable#toJSON()
     */
    @Override
    public JSONObject toJSON()
    {
        return new JSONBuilder()
                                .put("id",
                                     this.transactionID)
                                .put("type",
                                     this.type)
                                .put("usage",
                                     this.usage)
                                .put("senderReceiver",
                                     this.senderReceiver)
                                .put("amount",
                                     this.amount)
                                .put("bookDate",
                                     this.bookDate.toString())
                                .put("source",
                                     this.source)
                                .toJSON();
    }
}