package core.financ;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

import bowt.log.Logger;

/**
 * @author &#8904
 *
 */
public class TransactionParser
{
    private Pattern startLinePattern = Pattern
            .compile("(\\d{2})\\.(\\d{2})\\.\\s\\d{2}\\.\\d{2}\\.(.*)\\s+(\\d+,\\d{2})\\s*([SH])");

    /** Used for older PDFs due to their different formatting */
    private Pattern startLinePattern2 = Pattern
            .compile("(\\d{2})\\.(\\d{2})\\.\\s+(.*)\\s+(\\d+,\\d{2})\\s*([\\+-])");

    private Pattern yearPattern = Pattern
            .compile("\\d{2}\\.\\d{2}\\.(\\d{4})");

    public List<Transaction> parseTransactions(File file)
    {
        List<Transaction> transactions = new ArrayList<>();
        String text = "";
        String separator = null;

        try (var document = PDDocument.load(file))
        {
            var stripper = new PDFTextStripper();
            separator = stripper.getLineSeparator();

            for (int p = 1; p <= document.getNumberOfPages(); ++ p)
            {
                stripper.setStartPage(p);
                stripper.setEndPage(p);

                text += stripper.getText(document);
            }
        }
        catch (InvalidPasswordException e)
        {
            Logger.global().print(e);
        }
        catch (IOException e)
        {
            Logger.global().print(e);
        }

        String[] lines = text.split(separator);

        String type;
        String senderReceiver = "";
        String usage;
        Calendar bookDate;
        int day;
        int month;
        int year = 0;
        double amount;
        String operator;

        Matcher matcher;
        matcher = yearPattern.matcher(text);
        matcher.find();
        year = Integer.parseInt(matcher.group(1));
        String line;

        for (int i = 0; i < lines.length; i ++ )
        {
            line = lines[i];

            matcher = startLinePattern.matcher(line);

            if (matcher.matches())
            {
                day = Integer.parseInt(matcher.group(1).trim());
                month = Integer.parseInt(matcher.group(2).trim());
                type = matcher.group(3).trim();
                amount = Double.parseDouble(matcher.group(4).trim().replace(",", "."));
                amount = matcher.group(5).trim().equals("S") ? amount * -1 : amount;
                bookDate = Calendar.getInstance();
                bookDate.set(year, month - 1, day);
                senderReceiver = lines[i + 1].trim();
                usage = lines[i + 2].trim();
                transactions.add(new Transaction(bookDate, amount, type, senderReceiver, usage, file.getName()));
            }
            else
            {
                matcher = startLinePattern2.matcher(line.trim());

                if (matcher.matches())
                {
                    day = Integer.parseInt(matcher.group(1).trim());
                    month = Integer.parseInt(matcher.group(2).trim());
                    type = matcher.group(3).trim();
                    amount = Double.parseDouble(matcher.group(4).trim().replace(",", "."));
                    amount = matcher.group(5).trim().equals("-") ? amount * -1 : amount;
                    bookDate = Calendar.getInstance();
                    bookDate.set(year, month - 1, day);

                    if (line.contains("Kontoführung"))
                    {
                        // old account fee bookings only have one usefull line after them
                        usage = lines[i + 1].trim();
                    }
                    else
                    {
                        senderReceiver = lines[i + 1].trim();
                        usage = lines[i + 2].trim();
                    }

                    transactions.add(new Transaction(bookDate, amount, type, senderReceiver, usage, file.getName()));
                }
            }
        }

        return transactions;
    }
}