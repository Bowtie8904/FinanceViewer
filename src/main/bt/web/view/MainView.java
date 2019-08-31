package main.bt.web.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import bt.db.store.SqlEntry;
import bt.utils.log.Logger;
import main.bt.back.TransactionReader;
import main.bt.back.obj.Transaction;
import main.bt.web.Application;

/**
 * @author &#8904
 *
 */
@Route("list")
@PageTitle("FinanceViewer")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainView extends VerticalLayout implements PageConfigurator
{
    private List<Transaction> transactions;
    private List<Transaction> filteredTransactions;
    private Grid<Transaction> grid;
    private String filter = "";
    private H3 transactionCountLabel;
    private H3 amountLabel;

    public MainView()
    {
        String ip = UI.getCurrent().getSession().getBrowser().getAddress();

        if (!ip.equals("127.0.0.1") && !ip.equals("0:0:0:0:0:0:0:1"))
        {
            this.setVisible(false);
        }
        else
        {
            loadTransactions();
            setupComponents();
        }
    }

    private void loadTransactions()
    {
        new TransactionReader().read();
        this.transactions = SqlEntry.init(Application.database,
                                          Transaction.class);

        this.transactions.sort(Comparator.comparing(Transaction::getBookDateMillis).reversed());
        this.filteredTransactions = this.transactions;
    }

    private void setupComponents()
    {
        this.setHeightFull();

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setHeight("12%");

        upload.addFailedListener(e ->
        {
            Logger.global().print(e.getReason());
        });

        upload.addSucceededListener(event ->
        {
            try
            {
                File file = new File(Application.searchDir.getAbsolutePath() + "/" + event.getFileName());

                if (!file.exists())
                {
                    Files.copy(buffer.getInputStream(),
                               Paths.get(Application.searchDir.getAbsolutePath() + "/" + event.getFileName()));
                }
                UI.getCurrent().getPage().reload();
            }
            catch (IOException e1)
            {
                Logger.global().print(e1);
            }
        });

        this.grid = new Grid<>();
        this.grid.setItems(this.filteredTransactions);
        this.grid.addColumn(Transaction::getDateString)
                 .setHeader("Bookdate")
                 .setWidth("10%")
                 .setTextAlign(ColumnTextAlign.CENTER);
        this.grid.addColumn(Transaction::getType)
                 .setHeader("Type")
                 .setWidth("20%")
                 .setTextAlign(ColumnTextAlign.CENTER);
        this.grid.addColumn(Transaction::getUsage)
                 .setHeader("Usage")
                 .setWidth("30%");
        this.grid.addColumn(Transaction::getSenderReceiver)
                 .setHeader("Sender/Receiver")
                 .setWidth("30%");
        this.grid.addColumn(Transaction::getFormattedAmount)
                 .setHeader("Amount")
                 .setWidth("10%")
                 .setTextAlign(ColumnTextAlign.CENTER);

        this.grid.setHeightFull();

        this.grid.setSelectionMode(Grid.SelectionMode.NONE);
        this.grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS,
                                   GridVariant.LUMO_ROW_STRIPES,
                                   GridVariant.LUMO_WRAP_CELL_CONTENT);

        ListDataProvider<Transaction> provider = DataProvider.ofCollection(this.filteredTransactions);
        this.grid.setDataProvider(provider);

        TextField searchField = new TextField("Search");
        searchField.addValueChangeListener(
                                           e ->
                                           {
                                               updateFilteredList(e.getValue());
                                           });

        this.setAlignItems(Alignment.CENTER);

        this.transactionCountLabel = new H3(this.filteredTransactions.size() + " transactions");
        this.amountLabel = new H3("+0 / -0 (Total +0)");

        updateAmountLabel();

        this.add(this.transactionCountLabel);
        this.add(this.amountLabel);
        this.add(new Hr());
        this.add(upload);
        this.add(searchField);
        this.add(this.grid);
    }

    private void updateAmountLabel()
    {
        double positive = 0;
        double negative = 0;
        double total = 0;

        for (Transaction t : this.filteredTransactions)
        {
            if (t.getAmount() < 0)
            {
                negative += t.getAmount();
            }
            else
            {
                positive += t.getAmount();
            }
        }

        total = positive + negative;

        this.amountLabel.setText("+" + String.format("%.2f",
                                                     positive)
                                 + " / " + String.format("%.2f",
                                                         negative)
                                 + " (Total " + (total < 0 ? String.format("%.2f",
                                                                           total)
                                                           : "+" + String.format("%.2f",
                                                                                 total))
                                 + ")");
    }

    private void updateFilteredList(String filter)
    {
        this.filter = filter.trim();

        if (filter.trim().isEmpty())
        {
            this.filteredTransactions = this.transactions;
        }
        else
        {
            this.filteredTransactions = this.transactions.stream()
                                                         .filter(
                                                                 t ->
                                                                 {
                                                                     return t.getSenderReceiver()
                                                                             .toLowerCase()
                                                                             .contains(filter.toLowerCase())
                                                                            || t.getType()
                                                                                .toLowerCase()
                                                                                .contains(filter.toLowerCase())
                                                                            || t.getUsage()
                                                                                .toLowerCase()
                                                                                .contains(filter.toLowerCase());
                                                                 })
                                                         .collect(Collectors.toList());
        }

        this.grid.setItems(this.filteredTransactions);
        this.transactionCountLabel.setText(this.filteredTransactions.size() + " transactions");
        updateAmountLabel();
    }

    /**
     * @see com.vaadin.flow.server.PageConfigurator#configurePage(com.vaadin.flow.server.InitialPageSettings)
     */
    @Override
    public void configurePage(InitialPageSettings settings)
    {
        settings.addFavIcon("icon",
                            "icons/icon.png",
                            "512x512");
    }
}