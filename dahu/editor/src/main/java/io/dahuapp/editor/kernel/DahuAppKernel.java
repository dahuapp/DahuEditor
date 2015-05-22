package io.dahuapp.editor.kernel;

import io.dahuapp.common.javascript.WebEngineRuntime;
import io.dahuapp.common.kernel.Kernel;
import io.dahuapp.common.net.DahuFileAccessManager;
import io.dahuapp.common.net.RegexURLRewriter;
import io.dahuapp.editor.kernel.module.*;
import javafx.application.Platform;
import javafx.stage.Stage;


/**
 * Kernel implementation for DahuApp.
 */
public class DahuAppKernel implements Kernel {

    // kernel modules
    public Media media;
    public Logger console;
    public FileSystem filesystem;
    public Keyboard keyboard;
    public Browser browser;
    public ContextManager contextmanager;

    // shared objects
    private Stage primaryStage;
    private WebEngineRuntime webEngineRuntime;

    public DahuAppKernel(Stage stage, WebEngineRuntime webEngineRuntime, DahuFileAccessManager fileAccessManager, RegexURLRewriter rewriter) {
        this.primaryStage = stage;
        this.webEngineRuntime = webEngineRuntime;

        media = new Media();
        console = new Logger();
        keyboard = new Keyboard(webEngineRuntime);
        filesystem = new FileSystem(primaryStage, fileAccessManager, rewriter);
        browser = new Browser();
        contextmanager = new ContextManager(stage);
    }

    @Override
    public void start() {
        console.load();
        filesystem.load();
        media.load();
        keyboard.load();
        browser.load();
        contextmanager.load();
        // done loading
        console.info("Dahuapp kernel started!");
    }

    @Override
    public void stop() {
        console.info("Dahuapp kernel stopped!");
        // unload
        filesystem.unload();
        media.unload();
        keyboard.unload();
        browser.unload();
        console.unload();
        browser.unload();
        contextmanager.unload();
        // exit app
        Platform.exit();
    }
}
