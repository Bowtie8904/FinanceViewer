package core.api;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import bowt.console.ConsoleRowList;
import bowt.db.listener.DeleteListener;
import bowt.db.listener.InsertListener;
import bowt.db.listener.UpdateListener;
import bowt.db.listener.evnt.DeleteEvent;
import bowt.db.listener.evnt.InsertEvent;
import bowt.db.listener.evnt.UpdateEvent;
import bowt.db.store.SqlEntry;
import bowt.json.JSONBuilder;
import core.Main;
import core.financ.Transaction;

/**
 * @author &#8904
 *
 */
@Controller
public class DataController implements InsertListener, DeleteListener, UpdateListener
{
    private List<Transaction> transactions;

    public DataController()
    {
        Main.db.registerListener(this);
        loadTransactions();
    }

    private void loadTransactions()
    {
        this.transactions = SqlEntry.init(Main.db, Transaction.class);
        this.transactions.sort(Comparator.comparing(Transaction::getBookDateMillis).reversed());
    }

    @GetMapping("/finances")
    @ResponseBody
    public String getData(@RequestParam(name = "beginDate", defaultValue = "0") String beginDate,
            @RequestParam(name = "endDate", defaultValue = "99999999999999999") String endDate,
            @RequestParam(name = "filter", defaultValue = "") String filter, HttpServletRequest request)
    {
        if (!isAuthorized(request.getRemoteAddr()))
        {
            return "Access denied.";
        }

        long begin = Long.parseLong(beginDate);
        long end = Long.parseLong(endDate);

        var builder = new JSONBuilder();
        var list = new JSONArray();

        for (var t : applyFilter(begin, end, filter))
        {
            list.put(t.toJSON());
        }

        builder.put("transactions", list);

        return builder.toJSON().toString(4);
    }

    @GetMapping("/finances/pretty")
    @ResponseBody
    public String getFormattedData(@RequestParam(name = "beginDate", defaultValue = "0") String beginDate,
            @RequestParam(name = "endDate", defaultValue = "99999999999999999") String endDate,
            @RequestParam(name = "filter", defaultValue = "") String filter, HttpServletRequest request)
    {
        if (!isAuthorized(request.getRemoteAddr()))
        {
            return "Access denied.";
        }

        long begin = Long.parseLong(beginDate);
        long end = Long.parseLong(endDate);

        var rows = new ConsoleRowList(25, 40, 50, 50, 10, 14);
        rows.addTitle(true, "ID", "Type", "Usage", "Sender/Receiver", "Amount", "Bookdate");

        for (var t : applyFilter(begin, end, filter))
        {
            rows.addRow(true, t.getFormattedData());
        }

        String formatted = rows.toString();
        formatted = formatted.replace("\n", "<br>");
        formatted = formatted.replace(" ", "&nbsp;");
        formatted = "<font face=\"courier\">" + formatted + "</font>";

        return formatted;
    }

    private List<Transaction> applyFilter(long begin, long end, String filter)
    {
        var filtered = this.transactions;

        if (!filter.isEmpty())
        {
            filtered = this.transactions.stream().filter(t ->
            {
                return t.getSenderReceiver().toLowerCase().contains(filter.toLowerCase())
                        || t.getType().toLowerCase().contains(filter.toLowerCase())
                        || t.getUsage().toLowerCase().contains(filter.toLowerCase());
            }).collect(Collectors.toList());
        }

        filtered = filtered.stream().filter(t ->
        {
            return t.getBookDateMillis() >= begin && t.getBookDateMillis() <= end;
        }).collect(Collectors.toList());

        return filtered;
    }

    private boolean isAuthorized(String ip)
    {
        return ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1");
    }

    /**
     * @see bowt.db.listener.UpdateListener#onUpdate(bowt.db.listener.evnt.UpdateEvent)
     */
    @Override
    public void onUpdate(UpdateEvent e)
    {
        loadTransactions();
    }

    /**
     * @see bowt.db.listener.DeleteListener#onDelete(bowt.db.listener.evnt.DeleteEvent)
     */
    @Override
    public void onDelete(DeleteEvent e)
    {
        loadTransactions();
    }

    /**
     * @see bowt.db.listener.InsertListener#onInsert(bowt.db.listener.evnt.InsertEvent)
     */
    @Override
    public void onInsert(InsertEvent e)
    {
        loadTransactions();
    }
}