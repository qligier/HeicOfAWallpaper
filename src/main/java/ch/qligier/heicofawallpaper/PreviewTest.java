package ch.qligier.heicofawallpaper;

import ch.qligier.heicofawallpaper.heic.MetadataExtractor;
import ch.qligier.heicofawallpaper.heic.PreviewGenerator;
import ch.qligier.heicofawallpaper.model.DynamicWallpaperDefinition;
import ch.qligier.heicofawallpaper.service.DynamicWallpaperService;
import ch.qligier.heicofawallpaper.service.FileSystemService;

import java.io.File;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class PreviewTest {

    public static void main(String[] args) throws Exception {

        System.out.println(FileSystemService.getDataPath());

        final var generator = new PreviewGenerator();

        final MetadataExtractor metadataExtractor = new MetadataExtractor();
        metadataExtractor.start();
        final DynamicWallpaperService dynamicWallpaperService = new DynamicWallpaperService(metadataExtractor);

        final File heicFile = new File(
            "D:\\Programmation\\Java\\HeicOfAWallpaper\\src\\main\\resources\\heic\\Catalina.heic");
        //dynamicWallpaperService.extract(heicFile, null);
        final DynamicWallpaperDefinition wallpaper = dynamicWallpaperService.loadDefinition(heicFile);

        generator.generate(wallpaper, heicFile);
    }
}
