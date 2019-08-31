package main.bt.web;

import java.awt.Image;
import java.awt.MenuItem;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import bt.gui.tray.DefaultSystemTrayFrame;
import bt.utils.log.Logger;
import bt.utils.prop.Properties;
import main.bt.back.TransactionReader;
import main.bt.back.db.Database;

/**
 * @author &#8904
 *
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer
{
    public static File searchDir;
    public static Database database;

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class,
                              args);
    }

    public Application()
    {
        System.setProperty("java.awt.headless",
                           "false");

        Image icon = null;
        try
        {
            icon = ImageIO.read(new File("icon.png"));
        }
        catch (IOException e)
        {
            Logger.global().print(e);
        }

        searchDir = new File(Properties.getValueOf("searchDir"));

        var frame = new DefaultSystemTrayFrame(icon);
        var settings = frame.getSystemTraySettings();
        settings.setToolTip("FinanceViewer (127.0.0.1:8090/list)");

        MenuItem option = new MenuItem("Terminate");
        option.addActionListener((e) ->
        {
            Logger.global().print("Terminating process.");
            System.exit(0);
        });
        settings.addOption(option);
        frame.sendToSystemTray();

        database = new Database();

        new TransactionReader().read();
    }
}