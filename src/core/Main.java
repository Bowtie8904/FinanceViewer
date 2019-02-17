package core;

import java.awt.Image;
import java.awt.MenuItem;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import bowt.gui.tray.DefaultSystemTrayJFrame;
import bowt.log.Logger;
import core.api.DataController;
import core.db.Database;
import core.financ.TransactionReader;

/**
 * @author &#8904
 *
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = DataController.class)
public class Main
{
    public static Database db;

    public static void main(String[] args)
    {
        SpringApplication.run(Main.class, args);
    }

    public Main()
    {
        System.setProperty("java.awt.headless", "false");

        Image icon = null;
        try
        {
            icon = ImageIO.read(new File("icon.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        var frame = new DefaultSystemTrayJFrame(icon);
        var settings = frame.getSystemTraySettings();
        settings.setToolTip("Finance API (127.0.0.1:8090/finances)");

        MenuItem option = new MenuItem("Terminate");
        option.addActionListener((e) ->
        {
            Logger.global().print("Terminating process.");
            System.exit(0);
        });
        settings.addOption(option);
        frame.sendToSystemTray();

        db = new Database();
        new TransactionReader(db).start();
    }
}
