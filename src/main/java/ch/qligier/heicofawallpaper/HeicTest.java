package ch.qligier.heicofawallpaper;

import ch.qligier.heicofawallpaper.service.DynamicWallpaperService;
import ch.qligier.heicofawallpaper.utils.heic.MetadataExtractor;

import java.io.File;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class HeicTest {
    public static void main(String[] args) throws Exception {
        final var e = new MetadataExtractor();
        final var s = new DynamicWallpaperService(e);
        e.start();

        final var heicFile = new File(
            "D:\\Programmation\\Java\\HeicOfAWallpaper\\src\\main\\resources\\heic\\Catalina.heic");

        //s.extract(heicFile, null);

        final var definitions = s.loadDefinitionFromFile(heicFile);


        System.out.println("Stop");
    }
}
